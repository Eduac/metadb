var Class = require('../../Class')
,   CoreDispatcher = require('./CoreDispatcher')
,   BaseHandler = Class.extend({
        addMethod: function(method) {
            var _this = this;
            this[method] = function() {
                var callbackFn, errorFn 
                ,   argLen = arguments.length
                ,   args = Array.prototype.slice.call(arguments);
                if (argLen > 1 && typeof arguments[argLen - 1] === 'function' && typeof arguments[argLen - 2] === 'function') {
                    errorFn = args.pop();
                    callbackFn = args.pop();
                } else if (argLen > 0 && typeof args[argLen - 1] === 'function') {
                    callbackFn = args.pop();  
                }
                CoreDispatcher.dispatch(_this.session, _this.name + '.' + method, args, callbackFn, errorFn);
            }
        }
});
module.exports = BaseHandler;