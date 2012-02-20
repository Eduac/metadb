var HomeController = function () {
	return {
		handle : function(req, res) {
			var context = req.session.context;
    		res.render('users', {
				user : context.user,
				token : context.token
			});
		}
	}
};


module.exports = HomeController;
