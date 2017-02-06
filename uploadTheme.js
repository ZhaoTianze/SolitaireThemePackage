#!/usr/bin/env node
var AV = require('leancloud-storage');
// file system
var fs = require('fs');
var fileEx = require('./fileEx');
// shell tools
var shell = require('shelljs');
// underscore
var _ = require('underscore');

var tmp2RootPath = 'tmp2';
var tmp3RootPath = 'tmp3';
var leancloudConfig = {
  us : {
    appId:'CdfVxNwi50pWQ9whlLSXCnrh-MdYXbMMI',
    appKey:'hqe16ogoDi6FOT3GziI5Jilc',
    region:'us'
  },
  cn : {
    appId:'F6CNXSFaVsOVSqSfcGjTdXWY-gzGzoHsz',
    appKey:'XdlPwBR5aQTF7y4QVIVvzHYR'
  }
};

var CDNHttp = {
  us:'https://d3388t5oqr1jn9.cloudfront.net/'
}

var ThemePackage = AV.Object.extend('ThemePackage');

var agv = require('yargs')
            .options({
                "n":{
                    alias:'name',
                    demand:true,
                    description:'theme name',
                    type:'string'
                },
                "s":{
                    alias:'server',
                    demand:true,
                    description:'upload to server',
                    default:'cn',
                    type:'string'
                }
            })
            .usage('Usage: uploadTheme [options]')
            .example('uploadTheme -n basketball','upload ad info to us server')
            .help('h')
            .epilog('copyright 2016')
            .argv;

//初始化
var initLeanCloud = function(server) {
  server = server || 'cn';
  var configInfo = leancloudConfig[server];
  if (configInfo) {
    AV.init(configInfo);
    console.log('leancloud init completed');
    // return true;
    return AV.User.logIn('ScriptUser', 'tOUdTedyMU');
  }
  return AV.Promise.error({message: 'init failed'});
};

var parseFiles = function(dir,netFiles,needUploadFiles){
  var subObjects = fs.readdirSync(dir);
  subObjects = _.without(subObjects,'.DS_Store');
  subObjects.forEach(function(object) {
    var path = dir+'/'+object;
    var stats = fs.statSync(path);
    if (stats.isFile()){
      //是文件，跟线上文件对比
      // var netFile = netFiles[object];
      var index = _.findIndex(netFiles,function(netFileObject){
        return object === netFileObject.name;
      });
      var localFileMd5 = fileEx.md5(path);
      if (index > -1) {
        var netFile = netFiles[index];
        console.log('network file md5:'+netFile.md5);
        if (localFileMd5 !== netFile.md5 || stats.size !== netFile.size){
          console.log('loacl md5 : ' + localFileMd5)
          //两个文件的md5不相同，需要重新上传
          needUploadFiles.push({path:path,md5:localFileMd5,name:object,size:stats.size});
        }
      } else {
        needUploadFiles.push({path:path,md5:localFileMd5,name:object,size:stats.size});
      }
    }else if (stats.isDirectory()){
      //是文件夹，进行下一级判断
      parseFiles(path,netFiles,needUploadFiles);
    }
  }, this);
  return needUploadFiles;
}

var beginToUpload = function(needUploadFiles,themePackageObject){
  if (needUploadFiles.length == 0){
    console.log('no need upload files');
    return;
  }
  var promises = needUploadFiles.map(function(object){
    var fileData = fs.readFileSync(object.path);
    var file = new AV.File(object.name,fileData);
    return file.save();
  });
  console.log('uploading files, please wait...');
  var needDeleteFiles = [];
  Promise.all(promises).then(function(files){
    console.log('all files save completed');
    var packageFilesList = themePackageObject.get('files') || [];
    files.map(function(avFile){
      // console.log('file >>>>>',avFile);
      var name = avFile.get('name');
      var size = avFile.get('metaData').size;
      var key = avFile.get('id');
      var fileUrl = avFile.get('url');
      if (CDNHttp[agv.s]){
        //处理CDN加速
        fileUrl = CDNHttp[agv.s]+avFile.get('key');
      }
      //通过名字去查找needUploadFiles里面的数据
      var index = _.findIndex(needUploadFiles,function(object){
        return name === object.name;
      });
      var needUploadObject = needUploadFiles[index];
      if (size === needUploadObject.size){
        // var packageFile = packageFilesList[name];
        var mIndex = _.findIndex(packageFilesList,function(object){
            return name === object.name;
        });
        if (mIndex > -1) {
          var packageFile = packageFilesList[mIndex];
          needDeleteFiles.push(packageFile);//将需要删除的文件存储
          packageFilesList.splice(mIndex,1);
        } 
        var newObject = {
          name:name,//文件名字
          size:size,//文件尺寸
          md5:needUploadObject.md5,//md5验证码
          url:fileUrl,//文件url下载路径
          key:key,//文件的唯一索引值
          path:needUploadObject.path.slice("tmp3/".length)//本地的存放路径
        }
        packageFilesList.push(newObject);
      }else{
        console.error('upload file ' + name + ' compare with loacl file, error size.');
      }
    });
    themePackageObject.set('files',packageFilesList);
    //更新主题版本号
    var version = themePackageObject.get('version') || 0;
    version ++;
    themePackageObject.set('version',version);
    console.log('begin to save themePackageObject \n',themePackageObject);
    return themePackageObject.save();
  }).then(function(object){
    console.log('themePackageObject save completed:');
    console.log(object);
    //开始删除不需要的线上旧文件
    promises = needDeleteFiles.map(function(fileObject){
      var query = new AV.Query('_File');
      query.equalTo('id',fileObject.key);
      return query.first();
    });
    return Promise.all(promises);
  }).then(function(needDeleteAVFiles){
    promises = needDeleteAVFiles.map(function(avfile){
      return avfile.destroy();
    });
    return Promise.all(promises);
  }).then(function(){
    console.log('all is completed');
  }).catch(function(error){
    console.error("beginToUpload error : ");
    console.error(error);
  })
}

var prepareUploadObjectData = function(dirName){
  //1.首先读取json配置文件
  var jsonFile = dirName + '/themeData/theme_' + agv.n + '.json';
  console.log('read theme json file : ' + jsonFile);
  var themeConfig = JSON.parse(fs.readFileSync(jsonFile));
  console.log(themeConfig);
  //用key从leancloud数据库查询已有的主题信息
  var query = new AV.Query(ThemePackage);
  query.equalTo('key',themeConfig.key);
  return query.first().then(function(object){
    if (object) {
      console.log('found themePackage on Leancloud，begin compare with loacl files.');
      return object;
    }else{
      console.log('not found themePackage on Leancloud，begin a new upload.');
      var newTheme = new ThemePackage();
      newTheme.set('key',themeConfig.key);
      return newTheme;
    }
  }).then(function(object){
    var netFiles = object.get('files') || [];
    var needUploadFiles = [];
    parseFiles(dirName,netFiles,needUploadFiles);
    console.log('needUploadFiles:');
    console.log(needUploadFiles);
    beginToUpload(needUploadFiles,object);
  }).catch(function(error){
    console.error('prepareUploadObjectData error:');
    console.error(error);
  });
};

var operationTmp2Files = function(){
  var tmp2SubPath = tmp2RootPath+'/'+agv.n;
  if (fileEx.exists(tmp2SubPath)) {
    var tmp3SubPath = tmp3RootPath+'/'+agv.n;
    fileEx.copyDir(tmp2SubPath,tmp3SubPath);
    //调整UI_Resource的文件结构
    fs.mkdirSync(tmp3SubPath+'/themeData');
    fileEx.mvFile(tmp3SubPath+'/UI_Resources/*.cardcolor',tmp3SubPath+'/themeData/');
    fileEx.mvFile(tmp3SubPath+'/UI_Resources/*.json',tmp3SubPath+'/themeData/');
    prepareUploadObjectData(tmp3SubPath);
  }else{
    console.error('no found ' + agv.n + ' in tmp2 dir.');
  }
};

var createRole = function(){
  var administratorRole = new AV.Role('ScriptRole');
  var relation = administratorRole.getUsers();
  relation.add(AV.User.current());
  administratorRole.save().then(function(role){
    console.log(role);
  }).catch(function(error){
    console.error(error);
  });
}

var beginFunction = function(){
	  initLeanCloud(agv.s).then(function(user){
          // createRole();
          fileEx.rmDir(tmp3RootPath);
          fs.mkdirSync(tmp3RootPath);
          //读取tmp2中的文件信息
          if (fileEx.exists(tmp2RootPath)){
            operationTmp2Files();
          }else{
            console.error('not found tmp2 dir, please run ./packageTheme.js at first');
          }
    }).catch(function(error){
        console.error(error);
    });
}

beginFunction();