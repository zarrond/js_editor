var http = require('http');
var fs = require('fs');

http.createServer(function (req,res){
    if(req.url === '../index.html') {
      fs.readFile('../index.html',function(err,data){
        res.writeHead(200,{'Content-Type': 'text/html'});
       res.write(data);
        res.end();
      });
    }
    else if(req.url === '../css/new.css') {
      fs.readFile('../css/new.css',function(err,data){
        res.writeHead(200,{"Content-Type": "text/css"});
        res.write(data);
        res.end();
      });
    }

}).listen(2002, '127.0.0.1', () => {
  console.log('Сервер запущен http://127.0.0.1:2002/');
});
//start "" "file:///C:\Users\TrofimovDM\JS_projects\index.html"