var Session = require('../models/drivers/core/Session')
,   AuthenticationHandler = require('../models/drivers/core/AuthenticationHandler')
,   LoginController = function() {
        return {
            handle: function(req, res) {
                console.log('LoginController handling ' + req.method);
                var context = req.session.context,
                    action = req.param('action');
                if (!action) {
                    !context || !context.coreSession
                    ? res.render('index') 
                    : res.redirect('/home');
                }
                else if (action === 'logout') {
                    if (context) delete req.session.context;
                    res.redirect('/');
                }
                else if (action === 'login') {
                    var session = new Session('localhost', 5000, 'metadb-ui')
                    ,   authHandler = new AuthenticationHandler(session);
                    authHandler.authenticate(
                        req.param('username'), 
                        req.param('password'), 
                        function (coreSession) {
                            if (!coreSession) return res.render('login');
                            authHandler.session.importCore(coreSession);
                            req.session.context = authHandler.session;
                            res.redirect('/home');
                        }, function () {
                            res.redirect('/');
                        });
                }
            }
        }
    };
module.exports = LoginController;