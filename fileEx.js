var fileEx = {};
var fs = require('fs');
var md5 = require('md5');
var shell = require('shelljs');

fileEx.exists = function (path) {
  try {
    return fs.statSync(path,fs.F_OK);
  } catch (error) {
    return false;
  }
}

fileEx.copyFile = function (src,dist) {
  // console.log('复制文件：'+src+' 到 '+dist);
  var isExists = fileEx.exists(src);
  if (isExists){
    // fs.createReadStream(src).pipe(fs.createWriteStream(dist));
    shell.exec('cp -f '+src+' '+dist);
  }
}

fileEx.mvFile = function(src,dist) {
  // var isExists = fileEx.exists(src);
  // console.log(src);
  // if (isExists){
    shell.exec('mv ' +src+' '+dist);
  // }
}

fileEx.rmDir = function (path) {
  var isExists = fileEx.exists(path);
  if (isExists) {
    shell.exec('rm -rf '+path);//强制删除整个tmp文件夹
  }
}

fileEx.copyDir = function (src,dist) {
  var isExists = fileEx.exists(src);
  if (isExists) {
    var distState = fileEx.exists(dist);
    if (!distState){
      fs.mkdirSync(dist);//创建目标文件夹
    }
    var files = fs.readdirSync(src);
    files.forEach(function (file) {
      var _src = src + '/' + file;
      var _dist = dist + '/' + file;
      var state = fileEx.exists(_src);
      if (state && state.isFile()) {
        fileEx.copyFile(_src,_dist);
      }else if (state && state.isDirectory()) {
        fileEx.copyDir(_src,_dist);
      }
    })	
  }
}

fileEx.md5 = function(file) {
  var buf = fs.readFileSync(file);
  return md5(buf);
}

module.exports = fileEx;