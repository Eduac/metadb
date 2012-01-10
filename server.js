var express = require('express')
,   app = express.createServer()
,   MemoryStore = express.session.MemoryStore
,   jqtpl = require('jqtpl')
,   PORT = 8080;

// Controllers
app.controllers = {
	LoginController : new (require('./controllers/LoginController'))(),
	HomeController : new (require('./controllers/HomeController'))()
};

// Configurations
app.use(express.static(__dirname + '/public', {maxAge : 86400000}));        
app.use(express.bodyParser());        
app.use(express.cookieParser());        
app.use(express.session({ secret: 'MDB-L0ngH0-H4rUk!', store: new MemoryStore({ reapInterval: 60000 * 10 }) }));
app.set("view engine", "html");        
app.register(".html", jqtpl.express);        
app.set('view options', { layout: false });        
app.use(express.logger());

//Handle different environments
require('./config/environments')(app, express);

//Handles routing
require('./config/routes')(app);

console.log("Server running at port " +  PORT);
app.listen(PORT);
