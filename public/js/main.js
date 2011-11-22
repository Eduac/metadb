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
		menu : _$menu,
		render : function (callbackFn) {
			_$menu.find('a').click(function () {
				$(this).toggleClass('selected');
				$(this).siblings('a').removeClass('selected');
			});	
			_$menu.find('a.home').click();
			if ($.isFunction(callbackFn)) callbackFn();
		}
	}
})();

var Home = (function () {
	var _$leftSection = $('#main section.left'),
		_$rightSection = $('#main section.right'),
		_renderControl = function ($ctrl) {
			$ctrl.find('span.backward > .first')
				.button({
					text : false,
					icons : {
						primary : "ui-icon-seek-first"
					}
				})
				.next()
				.button({
					text : false,
					icons : {
						primary : "ui-icon-seek-prev"
					}
				}).parent().buttonset();
				
			$ctrl.find('span.forward > .next')
				.button({
					text : false,
					icons : {
						primary : "ui-icon-seek-next"
					}
				})
				.next()
				.button({
					text : false,
					icons : {
						primary : "ui-icon-seek-end"
					}
				}).parent().buttonset();
				
			$ctrl.find('span.misc > .print').button({
				text: false,
				icons : {
					primary : 'ui-icon-print'
				}
			});
				
			$ctrl.find('button').css({
				width : '25px',
				height : '25px'
			});
		},
		_renderLeftSection = function ($selector) {
			_renderControl($selector.find('div.control'));
		},
		_renderRightSection = function ($selector) {
			var _$submenu = $selector.find('div.submenu'),
				_$ctrl = $selector.find('div.control');
				
				
			_$submenu.find('span').click(function () {
				$(this).toggleClass('selected');
				$(this).siblings().removeClass('selected');
			});
			_$submenu.find('span:first').click();
			
			_$ctrl.find('button').button();
			
		} 
	
	return {
		render : function () {
			_renderLeftSection(_$leftSection);
			_renderRightSection(_$rightSection);
		}
	}
})();
