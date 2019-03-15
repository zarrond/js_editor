var express = require('express');
var Three = require('../js/three')
var WEBGL = require('../js/WebGL')
var script = require('../src/editor')
var window = require('window')
// создаём Express-приложение
var app = express();

// создаём маршрут для главной страницы
// http://localhost:8080/
app.get('/', function(req, res) {
  res.sendfile('index.html');
});

// запускаем сервер на порту 8080
app.listen(8080);
// отправляем сообщение
console.log('Сервер стартовал!');