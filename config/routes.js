var urlParser = require('url');
function intercept (req, res, next) { 
	var pathname = urlParser.parse(req.url).pathname,
		context = req.session.context;
		
	console.log('Intercepted ' + pathname);
	if (pathname != '/login' && (!context || !context.coreSession))
		res.redirect('/login');
	else
		next();
}

module.exports = function(app) {
	var _routes = {
		'/' : app.controllers.HomeController,
		'/login' : app.controllers.LoginController,
		'/home' : app.controllers.HomeController
	};

	for (var r in _routes) {
		app.all(r, intercept, function (req, res) {
			console.log('Resolving ' + req.method + ' ' + req.url);
			_routes[urlParser.parse(req.url).pathname].handle(req, res);
		});
	}
}
