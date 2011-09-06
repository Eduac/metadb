var express = require('express');
var app = express.createServer();
var fs = require('fs');

var public_path = 'public/';
var PORT = 80;
var oneDay = 86400000;

app.use(express.static(__dirname + '/public', { maxAge: oneDay }));

console.log("Server running at port " +  PORT);
app.listen(PORT);
