var http = require('http')
,   CoreProxyController = function() {
        return {
            handle: function(req, res) {
                var context = req.session.context 
                ,   options = {
                        host: 'localhost',
                        port: 5000,
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' }
                    }
                ,   jsReq = {
                        method: req.param('method'),
                        params: req.param('params') || [],
                        id : 99,
                        headers: {
                            profile_id : context.profile_id,
                            token: context.id,
                            client: context.client
                        }
                    };
                http.request(options, function(resp) {
                    var data = '';
                    resp.on('data', function(chunk) { data += chunk; });
                    resp.on('end', function() {
                      res.send(data);
                    });
                }).end(JSON.stringify(jsReq));
            }
        };
    };
module.exports = CoreProxyController;
