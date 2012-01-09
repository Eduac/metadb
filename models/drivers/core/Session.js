var Session = function(host, port, client) {
    var _this = this;
    this.host = host;
    this.port = port;
    this.client = client;
    this.verbose = false;
    this.importCore = function (session) {
        _this.profile_id = session.profile_id;
        _this.id = this.session_id = session.id;
        _this.coreSession = session;
    };
}

module.exports = Session;