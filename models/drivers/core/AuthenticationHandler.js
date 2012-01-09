var BaseHandler = require('./BaseHandler')
,   AuthenticationHandler = BaseHandler.extend({
        init : function (session) {
            this.session = session;
            for (var i = 0; i < this.methods.length; i++) 
                this.addMethod(this.methods[i]);
        },
        name : 'AuthenticationHandler',
	    methods : ['authenticate', 'isValid', 'invalidate', 'extend', 'changeUser']
});
module.exports = AuthenticationHandler;