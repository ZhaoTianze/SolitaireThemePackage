#!/usr/bin/env node

// file system
var fs = require('fs');
// shell tools
var shell = require('shelljs');
// underscore
var _ = require('underscore');
var zipFolder = require('zip-folder');
var child_process = require('child_process');

var resourcePath = '../SolitaireResource/图片资源/UI/lite';
var tmpRootPath = 'lite_tmp';
var tmp2RootPath = 'lite_tmp2';
var zipRootPath = 'lite_zip';
var projScrRootPath = '../SolitaireClient/Proj.android-studio/solitaireLite/src/main';
var packageFilePath = '../SolitaireClient/scr/packageLite.json';
var SolitaireClient_Path = '../SolitaireClient'

var exists = function (path) {
  try {
    return fs.statSync(path,fs.F_OK);
  } catch (error) {
    return false;
  }
}

var copyFile = function (src,dist) {
  // console.log('复制文件：'+src+' 到 '+dist);
  var isExists = exists(src);
  if (isExists){
    // fs.createReadStream(src).pipe(fs.createWriteStream(dist));
    shell.exec('cp -f '+src+' '+dist);
  }
}

var rmDir = function (path) {
  var isExists = exists(path);
  if (isExists) {
    shell.exec('rm -rf '+path);//强制删除整个tmp文件夹
  }
}

var copyDir = function (src,dist) {
  var isExists = exists(src);
  if (isExists) {
    var distState = exists(dist);
    if (!distState){
      fs.mkdirSync(dist);//创建目标文件夹
    }
    var files = fs.readdirSync(src);
    files.forEach(function (file) {
      var _src = src + '/' + file;
      var _dist = dist + '/' + file;
      var state = exists(_src);
      if (state && state.isFile()) {
        copyFile(_src,_dist);
      }else if (state && state.isDirectory()) {
        copyDir(_src,_dist);
      }
    })	
  }
}

var readThemeJsonFile = function (fileName) {
  var filePath = resourcePath + '/' + fileName;
  var files = fs.readdirSync(filePath);
  var jsonFileName = "config.json";
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
    var textureName = "UI_CardResources";
    //生成大图
    shell.exec('TexturePacker --sheet ' + UI_ResourcesPath + '/' + textureName + '.png --data ' + UI_ResourcesPath + '/' + textureName + '.plist ' +filePath + '/UI_CardResources/*png');

    //拷贝color文件
    var colorFileName = "color"+themeConfig.faceIndex+'.cardcolor';
    copyDir(filePath+'/themeData',tmpFilePath+'/themeData');
    //拷贝paticle文件夹
    var paticleDirPath = filePath+'/particle';
    copyDir(paticleDirPath,tmpFilePath+'/particle');
    //拷贝theme文件夹
    var themeDirPath = filePath+'/theme';
    copyDir(themeDirPath,UI_ResourcesPath+"/theme");
    //拷贝cardBack文件夹
    var cardBackDirPath = filePath+'/cardBack';
    copyDir(cardBackDirPath,UI_ResourcesPath+'/cardBack');
    // //进行文件加密
    var shellEx = '$QUICK_COCOS2DX_ROOT/bin/pack_files.sh -i ' + tmpFilePath + ' -o ' + tmp2RootPath + '/' + fileName + ' -ek 8VVTJ-UC2R1 -es XXTEA -x .json,.cardcolor';
    console.log(shellEx);
    shell.exec(shellEx);
}

var supportLan = [];

var changeAppName = function (file,themeConfig) {
  var resDirPath = projScrRootPath + '/res_' + file;
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
  var newJson = {}
  files.forEach(function(file) {
    var themeConfig = readThemeJsonFile(file);
    if (themeConfig) {
      var packageKey = "com.cardgame.solitaire."+file;
      var resDirName = 'res_'+file;
      var assetsDirName = 'assets_'+file;
      //创建相关的文件夹
      var resDirPath = projScrRootPath + '/' + resDirName;
      var assetsDirPath = projScrRootPath + '/' + assetsDirName;
      console.log(file+'开始准备asset和res文件夹');
      rmDir(assetsDirPath);
      fs.mkdirSync(assetsDirPath);
      //拷贝res资源文件到assets下
      var zipFilePath = tmp2RootPath+'/'+file;
      copyDir(zipFilePath,assetsDirPath+'/res');
      //拷贝icon到res文件下
      var iconDirPath = resourcePath+'/'+file+'/icon';
      var iconDirs = fs.readdirSync(iconDirPath)
      iconDirs.forEach(function(iconDir) {
        if (iconDir.indexOf('mipmap') === 0){
          copyDir(iconDirPath + '/' + iconDir, resDirPath + '/' + iconDir);
        }
      }, this);
      copyDir(projScrRootPath+'/resDefault',resDirPath);
      //修改res下value文件的appName
      changeAppName(file,themeConfig);
      
      var table = {}
      table.name = file
      for(var key in themeConfig){
        if (key != 'appName') {
          table[key] = themeConfig[key]
        }
      }
      newJson[packageKey] = table
    }
  }, this);
  console.log('修改工程配置文件packge.json');
  fs.writeFileSync(packageFilePath,JSON.stringify(newJson),'utf8');
}

var rmProjectResource = function() {
  var floders = fs.readdirSync(projScrRootPath);
  for (var index = 0; index < floders.length; index++) {
    var floderName = floders[index];
    var assetsIndex = floderName.indexOf('assets_');
    var resIndex = floderName.indexOf('res_');
    if (assetsIndex != -1 || resIndex != -1) {
      rmDir(projScrRootPath + '/' + floderName);
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
    rmDir(tmpRootPath);
    rmDir(tmp2RootPath);
    fs.mkdirSync(tmpRootPath);
    fs.mkdirSync(tmp2RootPath);
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
    

    Operation3(files);
}

OperationBegin();