var express = require('express');
var app = express.createServer();
var fs = require('fs');
var MemoryStore = express.session.MemoryStore;

var PORT = 8081;
app.use(express.static(__dirname + '/public', {maxAge : 86400000}));


app.use(express.bodyParser());
app.use(express.cookieParser());
app.use(express.session({ secret: 'MDB-L0ngH0-H4rUk!', store: new MemoryStore({ reapInterval: 60000 * 10 }) }));
app.set("view engine", "html");
app.register(".html", require("jqtpl").express);
app.set('view options', { layout: false });

app.get('/', function(req, res) {
	res.render('login');
});

app.post('/login', function(req, res) {
	console.log(req.query);
	req.session.token=123;
	req.session.username='bernie';
	res.redirect('/viewer');
});

app.get('/viewer', function(req, res) {
	console.log(req.query);
	if (!req.session || !req.session.token || !req.session.username) {
		res.redirect('/');
	}
	res.render('viewer'); 
});

console.log("Server running at port " +  PORT);
app.listen(PORT);
