var API = (function () {
  
  return {
    query : function (method, params, options) {
      $.getJSON('api', {
        method : method,
        params : params || []
      }, function (data) {
        if (!data || data.error) {
          if (_.isFunction(options.error)) {
            return options.error(data);
          } 
          return console.error(data);
        }
        if (_.isFunction(options.success)) {
          return options.success(data.result);
        }
      });
    }
  }

})();
