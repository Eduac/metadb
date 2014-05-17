var express = require('express')
    ,session = require('express-session')
    ,body_parser = require('body-parser')
    ,cookie_parser = require('cookie-parser')
    ,logger = require('morgan')
    ,jqtpl = require('jqtpl')
    ,PORT = 8081
    ,http = require('http');

var app = express();
var MemoryStore = session.MemoryStore;
var server = http.createServer(app);

// Controllers
app.controllers = {
	LoginController : new (require('./controllers/LoginController'))(),
	UsersController : new (require('./controllers/UsersController'))(),
	HomeController : new (require('./controllers/HomeController'))(),
  CoreProxyController : new (require('./controllers/CoreProxyController'))()
};

// Configurations
app.use(express.static(__dirname + '/public', {maxAge : 86400000}));
app.use(body_parser());
app.use(cookie_parser());
app.use(session({ secret: 'MDB-L0ngH0-H4rUk!', store: new MemoryStore({ reapInterval: 60000 * 10 }) }));
app.use(logger());

app.set("view engine", "html");
app.set('view options', { layout: false });
app.engine(".html", require('jqtpl/lib/express').render);

//Handle different environments
require('./config/environments')(app, express);

//Handles routing
require('./config/routes')(app);

console.log("Server running at port " +  PORT);
app.listen(PORT);
