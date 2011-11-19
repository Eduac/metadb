var Login = (function() {
	
	return {
		init : function() {
			
			$('button').button();
			$('input[type="text"]:first').focus();
			var $notificationDiv = $('div.notification'); 
			$notificationDiv.dialog({
				autoOpen : false,
				modal : true
			});
			
			$('footer span.links > a').each(function() {
				$(this).click(function(ev) {
					if ($(this).hasClass('internal')) {
						$notificationDiv.dialog('option', 'title', $(this).html());
						$notificationDiv.dialog('option', 'width', 650);
						$notificationDiv.load($(this).attr('href'), function(){
							$notificationDiv.dialog('open');
						});
						ev.preventDefault();
					}
				});
			});
		}
	};
})();

var NavBar = (function () {
	var _$menu = $('nav span.menu');
	return {
		render : function () {
			_$menu.find('a').click(function () {
				$(this).toggleClass('selected');
				$(this).siblings('a').removeClass('selected');
			});	
		}
	}
})();
