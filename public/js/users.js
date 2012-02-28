var Metadb = Metadb || {};
Metadb.Model = Metadb.Model || {};
Metadb.View = Metadb.View || {};
(function (M, BB) {

  M.Model.Profile = BB.Model.extend({
    idAttribute : 'profile_id'
  });

  M.View.ProfileView = BB.View.extend({
    tagName : 'li',
    className : 'form-inline',
    template : _.template($('#profileTmpl').html()),

    events : {
      'mouseover' : 'showControls',
      'mouseout' : 'hideControls'
    },
    render : function () {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    },
    showControls : function () {
      this.$el.find('.controls').show();
    },
    hideControls : function () {
      this.$el.find('.controls').hide();
    }

  });

  M.Model.Profiles = BB.Collection.extend({
    model : M.Model.Profile,

    fetch : function () {
      API.query('ProfileHandler.getAllUsers', null, {
        success : _.bind(this.reset, this)
      });
    }
  });

  M.View.ProfilesView = BB.View.extend({
    el : $('#users'), 
 
    initialize : function () {
      this.collection.on('reset', _.bind(this.render, this));
    },

    render : function () {
      var _$userList = this.$el.find('.user-list');
      this.collection.each(function (profile) {
        _$userList.append(new M.View.ProfileView({ model : profile }).render().el);
      });
      return this;
    }

  });

})(Metadb, Backbone);
