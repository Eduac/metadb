var Session = require('../model/Session');
var LoginController = function () {
	return {
		handle : function(req, res) {
			console.log('LoginController handling ' + req.method);
			var context = req.session.context,
				action = req.param('action');
			if (!action) { 
				if (!context || !context.user || !context.token) {
					res.render('login');
				} else if (context && context.user && context.token) {
					res.redirect('/home');
				}
			}
			else if (action == 'logout') {
				if (context) {
					delete req.session.context;	
				}
				res.redirect('/login');
			}
			else if (action == 'login') {
				if (req.param('username') == 'metadb' && req.param('password') == '123123') {
					req.session.context = new Session(req.param('username'), Math.floor(Math.random() * 10000));
					res.redirect('/home');
				} else {
					res.render('login');
				}
			} 
		}
	}
};


module.exports = LoginController;
