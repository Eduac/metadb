var http = require('http')
,   CoreDispatcher = (function() {
        return {
            dispatch: function(session, method, params, callbackFn, errorFn) {
                var options = {
                    host: session.host,
                    port: session.port,
                    path: session.path,
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                }
                ,   jsReq = {
                        method: method,
                        params: params,
                        id : 99,
                        header: {
                            profile_id : session.profile_id,
                            token: session.id,
                            client: session.client
                        }
                    };
                if (session.verbose) console.log('Request' + JSON.stringify(jsReq));
                http.request(options, function(res) {
                    var data = '', jsRes;
                    res.on('data', function(chunk) { data += chunk; });
                    res.on('end', function() {
                        if (session.verbose) console.log('Response: ' + data);
                        jsRes = JSON.parse(data);
                        if (jsRes.error) {
                            if (typeof errorFn === 'function') errorFn(jsRes.error);
                        }
                        else if (typeof callbackFn === 'function') callbackFn(jsRes.result);
                    });
                }).end(JSON.stringify(jsReq));
            }
        };
    })();
module.exports = CoreDispatcher;