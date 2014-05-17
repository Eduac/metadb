module.exports = function(app) {
    var env = process.env.NODE_ENV || 'development';
    var errorHandler = require('errorhandler');
    if ('development' == env) {
        app.use(errorHandler({
            dumpExceptions: true,
            showStack: true
        }));
    }

    else if ('production' == env) {
        app.use(errorHandler());
    }

    console.log('Done configuring server!');
};