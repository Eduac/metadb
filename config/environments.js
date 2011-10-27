module.exports = function (app, express, jqtpl) {
	app.configure('development', function() {
		app.use(express.errorHandler({
    		dumpExceptions: true,
    		showStack: true
		}));
	});

	app.configure('production', function() {
		app.use(express.errorHandler());
	});
	console.log('Done configuring server!');
};
