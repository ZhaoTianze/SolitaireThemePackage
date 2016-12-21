#!/usr/bin/env node

// file system
var fs = require('fs');
var fileEx = require('./fileEx');
// shell tools
var shell = require('shelljs');
// underscore
var _ = require('underscore');
var zipFolder = require('zip-folder');

var resourcePath = '../SolitaireResource/图片资源/UI/新主题';
var tmpRootPath = 'tmp';
var tmp2RootPath = 'tmp2';
var zipRootPath = 'zip';
var projScrRootPath = 'SolitaireTheme/theme/src/main';
var packageFilePath = 'packageTheme.json';
var SolitaireClient_Path = '../SolitaireClient'
var SolitaireThemePakeage_Path = '../SolitaireThemePakeage'

var readThemeJsonFile = function (fileName) {
  var filePath = resourcePath + '/' + fileName;
  var files = fs.readdirSync(filePath);
  var jsonFileName = "theme_"+fileName+".json";
  var index = _.indexOf(files,jsonFileName);
  if (index === -1) {
    return undefined;
  }
  var jsonFilePath = filePath+'/'+jsonFileName;
  var themeConfig = JSON.parse(fs.readFileSync(jsonFilePath));
  return themeConfig;
}

var Operation1 = function (fileName) {
    if (fileName === '.DS_Store'){
      return;
    }
    var tmpFilePath = tmpRootPath+'/'+fileName;
    var UI_ResourcesPath = tmpFilePath+'/UI_Resources';
    fs.mkdirSync(tmpFilePath);
    fs.mkdirSync(UI_ResourcesPath);
    console.log('开始遍历文件：'+fileName);
    var filePath = resourcePath + '/' + fileName;
    var themeConfig = readThemeJsonFile(fileName);
    if (!themeConfig){
      return console.error('在%s文件夹下，没有找到可用的json配置文件',fileName);
    }
    var textureName = themeConfig.cardPlist;
    //生成大图
    shell.exec('TexturePacker --sheet ' + UI_ResourcesPath + '/' + textureName + '.png --data ' + UI_ResourcesPath + '/' + textureName + '.plist ' +filePath + '/UI_CardResources/*png');
    var jsonFileName = "theme_"+fileName+".json";
    //拷贝json文件
    fileEx.copyFile(filePath+'/'+jsonFileName,UI_ResourcesPath+'/'+jsonFileName);
    //拷贝color文件
    var colorFileName = "color"+themeConfig.faceIndex+'.cardcolor';
    fileEx.copyFile(filePath+'/'+colorFileName,UI_ResourcesPath+'/'+colorFileName);
    //拷贝paticle文件夹
    var paticleDirPath = filePath+'/particle';
    fileEx.copyDir(paticleDirPath,tmpFilePath+'/particle');
    //拷贝theme文件夹
    var themeDirPath = filePath+'/theme';
    fileEx.copyDir(themeDirPath,UI_ResourcesPath+"/theme");
    //拷贝cardstyle文件夹
    var cardstyleDirPath = filePath+'/cardstyle';
    fileEx.copyDir(cardstyleDirPath,UI_ResourcesPath+"/cardstyle");
    //拷贝cardBack文件夹
    var cardBackDirPath = filePath+'/cardBack';
    fileEx.copyDir(cardBackDirPath,UI_ResourcesPath+'/cardBack');
    // //进行文件加密
    var shellEx = '$QUICK_COCOS2DX_ROOT/bin/pack_files.sh -i ' + tmpFilePath + ' -o ' + tmp2RootPath + '/' + fileName + ' -ek 8VVTJ-UC2R1 -es XXTEA -x .json,.cardcolor';
    console.log(shellEx);
    shell.exec(shellEx);
}

var Operation2 = function(floders,index,callback) {
  if (floders.length <= index) {
    callback();
    return;
  }
  //生成.zip操作
  if (fileName === '.DS_Store'){
    Operation2(floders,index+1,callback);
  }else{
    var fileName = floders[index]; 
    var tmpFilePath = tmp2RootPath+'/'+fileName;
    zipFolder(tmpFilePath,zipRootPath+'/'+fileName+'.aac',function(error) {
      if (error){
        console.log(fileName + '打包zip失败：'+error);
        Operation2(floders,index+1,callback);
      }else{
        //修改后缀为.aac
        // fs.renameSync(oldPath, newPath)
        Operation2(floders,index+1,callback);
      }
    });
  }
}

var supportLan = ['ar','de','es','fr','id','it','ja','ko','nl','pt','ru','tr','zh-rCN','zh-rHK','zh-rTW'];

var changeAppName = function (file,themeConfig) {
  var resDirPath = projScrRootPath + '/res_' + themeConfig.key;
  var files = fs.readdirSync(resDirPath);
  changeXML(resDirPath+'/values/strings.xml',themeConfig.appName);
  supportLan.forEach(function(lan) {
    changeXML(resDirPath+'/values-'+lan+'/values-'+lan+'.xml',themeConfig.appName);
  }, this);
}

var changeXML = function (xmlPath,appName) {
  var stringsXml = fs.readFileSync(xmlPath,'utf8');
  stringsXml = stringsXml.replace(/Soccer Theme/,appName);
  fs.writeFileSync(xmlPath,stringsXml,'utf8');
}

//准备工程文件
var Operation3 = function(files) {
  console.log('开始准备工程文件');
  var packageJson = JSON.parse(fs.readFileSync(packageFilePath));
  var newThemes = {}
  files.forEach(function(file) {
    var themeConfig = readThemeJsonFile(file);
    if (themeConfig) {
      var packageKey = "com.hipanda.solitaire.theme."+themeConfig.key;
      var resDirName = 'res_'+themeConfig.key;
      var assetsDirName = 'assets_'+themeConfig.key;
      //创建相关的文件夹
      var resDirPath = projScrRootPath + '/' + resDirName;
      var assetsDirPath = projScrRootPath + '/' + assetsDirName;
      console.log(file+'开始准备asset和res文件夹');
      fs.mkdirSync(assetsDirPath);
      fileEx.copyDir(projScrRootPath+'/resDefault',resDirPath);
      //拷贝zip文件到assets下
      var zipFilePath = zipRootPath+'/'+file+'.aac';
      fileEx.copyFile(zipFilePath,assetsDirPath+'/'+file+'.aac');
      //拷贝bg.png到res/drawable
      var drawablePath = resDirPath+'/drawable';
      var isExists = fileEx.exists(drawablePath);
      if (!isExists){
        fs.mkdirSync(drawablePath);
      }
      var bgPngFilePath = resourcePath+'/'+file+'/bg.png';
      fileEx.copyFile(bgPngFilePath,drawablePath+'/bg.png');
      //拷贝icon到res文件下
      var iconDirPath = resourcePath+'/'+file+'/icon';
      var iconDirs = fs.readdirSync(iconDirPath)
      iconDirs.forEach(function(iconDir) {
        if (iconDir.indexOf('mipmap') === 0){
          fileEx.copyDir(iconDirPath + '/' + iconDir, resDirPath + '/' + iconDir);
        }
      }, this);
      //修改res下value文件的appName
      changeAppName(file,themeConfig);
      //
      newThemes[packageKey] = {
        name:themeConfig.key,
        resDir:resDirName,
        assetsDir:assetsDirName
      }
    }
  }, this);
  console.log('修改工程配置文件packge.json');
  packageJson.theme = newThemes;
  fs.writeFileSync(packageFilePath,JSON.stringify(packageJson),'utf8');
}

var rmProjectResource = function() {
  var floders = fs.readdirSync(projScrRootPath);
  for (var index = 0; index < floders.length; index++) {
    var floderName = floders[index];
    var assetsIndex = floderName.indexOf('assets_');
    var resIndex = floderName.indexOf('res_');
    if (assetsIndex != -1 || resIndex != -1) {
      fileEx.rmDir(projScrRootPath + '/' + floderName);
    }
  }
}

var Operation4 = function () {
  shell.exec('cd ../SolitaireClient');
  // shell.exec('./buildAPK.sh');
  // shell.exec('cd '+ SolitaireThemePakeage_Path);
}

var OperationBegin = function() {
    //1.删除原有的临时文件，创建新的文件夹
    fileEx.rmDir(tmpRootPath);
    fileEx.rmDir(tmp2RootPath);
    fileEx.rmDir(zipRootPath);
    fs.mkdirSync(tmpRootPath);
    fs.mkdirSync(tmp2RootPath);
    fs.mkdirSync(zipRootPath);
    //读取资源文件夹
    var files = fs.readdirSync(resourcePath);
    var index = _.indexOf(files,'.DS_Store')
    if (index != -1){
      files.splice(index,1);
    }
    console.log(files);
    files.forEach(function(file) {
      Operation1(file);
    }, this);
    

    Operation2(files,0,function() {
      rmProjectResource();
      Operation3(files);
    })
}

OperationBegin();