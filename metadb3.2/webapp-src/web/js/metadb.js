/*
MetaDB: A Distributed Metadata Collection Tool
Copyright 2011, Lafayette College, Eric Luhrs, Haruki Yamaguchi, Long Ho.

This file is part of MetaDB.

    MetaDB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MetaDB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MetaDB.  If not, see <http://www.gnu.org/licenses/>.
*/

/**********************************************************************
 * Javascript for MetaDB Web Application
 * The script has 4 main components: Authentication, Management, Navigation and Helper.
 * Authentication mainly handles login, logout, session management and such.
 * Management has User Management, Project Management, Data Management and Metadata Management
 * and Controlled Vocab.
 * Navigation handles browsing through Project Management, User Management and Data Management page
 * Helper provides static support methods and Table Handler.
 * Fast navigation search keywords:
 * - Tabs:							"TScr"
 * - Home:							"HomeScr"
 * - Dashboard:						"DashScr
 * - Authentication: 				"AuthScr"
 * - User Management: 				"UMS"
 * - Project Management: 			"PMS"
 * - Image Settings: 				"ISS"
 * - Data Management: 				"DMS"
 * - Metadata Management: 			"MMS"
 * - Controlled Vocab Management: 	"CVMS"
 * - Search Script: 				"SS"
 * - Page Navigation Script:		"PNS"
 * - Project Management Navigation: "PMNS"
 * - Data Management Navigation: 	"DMNS"
 * - User Management Navigation: 	"UMNS"
 * - Helper: 						"HelpS"
 * - Table Handler: 				"TS"
 **********************************************************************/

/**********************************************************************
 *               G E N E R A L   S C R I P T			  			  *
 **********************************************************************/

/**
 * List of tabs
 * ID: TScr
 */
var Tabs = {
		home_tab: 		"#home-tab",
		userman_tab: 	"#userman-tab",
		projman_tab:	"#projman-tab",
		dataman_tab:	"#dataman-tab",
		item_tab:		"#item-tab",
		search_tab:		"#search-tab"
};

/**
 * Scripts manipulating overall/main events and settings
 * ID: HomeScr
 */
var Home = { 

		//static variables, specific to web layout
		project_select: '#project-choice',
		logout_button: '#logout-button',
		context: '#context',
		general_warning: '#general-warning',
		login_form: '#loginHome',
		current_project: "",

		/**
		 * Initialize the webapp, configuring settings, logout, projects
		 */
		init : function() {
			//Initialize the main tabs
			$(Home.context).tabs({
				selected : 0,
				disabled : [ 1, 2, 3, 4, 5, 6 ],
				cache : false,
				select: function(event, ui) {
					// Wipe out other tags
					$(event.target).find('.ui-tabs-panel').html('');
					if (ui.index == 0) { // Check the session again if navigate to home tab
						Authentication.checkSession();
					} else { // Otherwise get rid of the background image
						$(Home.context).css({
							'background-image': ''
						});
					}

					// Clean up when navigate to a new tab
					$('#main').siblings(':not(script, .hidden-info, .static-info)').remove();
				},
				load: function(ev, ui) { // Call initialize scripts after HTML is loaded
					switch (ui.index) {
					case 1: UserMan.init(); break;
					case 2: ProjectMan.init(); break;
					case 3: DataMan.init(); break;
					default: break;
					}
				}
			});

			// Add the project choice select box
			$(Home.context + ' > ul').append(
					"<li class='not-tab' style='float:right; display:none;'>" +
					"Project: " +
					"<select style='margin-top:3px' name='project-choice' id='project-choice' class='project-list-main ui-widget-content'>" +
					"</select>" +
					"</li>"
			);

			// Check the session after initialize page
			Authentication.checkSession();

			//Setup events for project select box
			$(Home.project_select).unbind('change').bind('change', function() {
				ProjectMan.changeWorkingProject();//Call change working project to set the working project to the 1st one in select box
				if ($(ProjectNav.action_indicator).html() == "image settings")
					ImageSettings.loadSettings();
				else if ($(ProjectNav.action_indicator).html() == "edit project")
					ProjectMan.reloadProject();
				else if ($(Home.context).tabs('option', 'selected') == 4)
					MetadataMan.reload();
				else if ($(Pages.current_page).html() == 'view_metadata')
					DataNav.gotoViewData();
			});

			// Set up logout warning
			$(Authentication.logout_warning).html('<p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>You have been idle for 15 minutes. Please re-login</p>');
			$(Authentication.logout_warning).dialog('destroy');
			$(Authentication.logout_warning).dialog( {
				autoOpen : false,
				bgiframe : true,
				modal : true,
				buttons : {
					'Logout' : function() {
						$(this).dialog('close');
					}
				},
				close: function() {
					Authentication.logout();
				},
				dialogClass: 'hidden-info'
			});

			// Setup logout button
			$(Home.logout_button).button().unbind('click').click(function() {
				Authentication.logout();
			});
		}
};

/**
 * In charge of configuring Dashboard
 * ID: DashScr
 */
var Dashboard = {

		init: function() {
			ProjectMan.changeWorkingProject();
			$('#self-change-form .update-pw-self-button').button().click(function(){
				UserMan.updateSelf();
			});

			$('#self-change-form .reset-pw-self-button').button();

			$(".column").sortable({
				connectWith: '.column'
			});

			$(".portlet").addClass("ui-widget ui-widget-content ui-helper-clearfix ui-corner-all")
			.find(".portlet-header")
			.addClass("ui-widget-header ui-corner-all")
			.end()
			.find(".portlet-content");

			$(".column").disableSelection();
			$(".dashboard").children().each(function(i) {
				$(this).children().css('min-height', '140px');
			});
		},


		/**
		 * Setup dashboard events
		 * @param data data from Login servlet
		 */
		setupEvents: function(data) {

			$('.greetings').html("Greetings, "+data.username);

			if (data.last_login == "error")
				$('.last-login').html("Error updating last login time");
			else if (data.last_login == "")
				$('.last-login').html("This is your first login");
			else
				$('.last-login').html("Your last login was: <br/> "+data.last_login);

			$('.last-page a').attr('href','#');
			$('.last-page a').html(data.last_proj+", item number "+data.last_item);
			$('.last-page a').unbind('click').click(function(){
				ProjectMan.changeMainProject(data.last_proj);
				$(Search.item_to_go).html(data.last_item);
				$(Home.context).tabs('option', 'selected', 4);
			});

			if (data.admin) {
				$('.log-types').append(Helper.setupSelectBox(data.log_types, ""));
				if (data.parser_running) {
					$('.parser-status').html("Parser is currently running");
					$('.parser-status').css('color','green');
				} else {
					$('.parser-status').html("Parser is not running");
					$('.parser-status').css('color','red');
				}
				var proj_count = $(Home.project_select+' option').length;
				$('.record-status').html("MetaDB currently has "+proj_count+" projects <br/> with a total of "+data.record_count+" records<br/><br/>");
				$('.show-log').button().unbind('click').click(function(){ Helper.showLog(); });
				$('.flush-log').button().unbind('click').click(function(){ Helper.flushLog(); });

			} else {
				$('.restricted').css('display', 'none');
			}

			if (data.local)
			{			
				$('.password-change-trigger a').attr('href','#').unbind('click').click(function(e){
					$(UserMan.self_pwd_form).css('display','block');
				});
			}
			else
			{
				$('.password-change-trigger a').css('visibility', 'hidden');				
			}

		}
};

/**
 * Script in charge of login/logout and setup main page
 * ID: AuthScr
 */
var Authentication = {

		//static global variables, specific to web layout
		login_form : '#loginHome',
		welcome_message : '#welcome',
		main_content : '#context',
		home_page : '#home',
		main_page : 'main.html',
		user_info : '#user-info',
		logout_warning: '#logout-warning',

		timeout: 1800000,
		timer: '', 

		/**
		 * In charge of configuring stuff when authentication is successful
		 * @param data data from Login servlet
		 */ 
		loginSuccess : function(data) {
			// Show project choice, change display:none to display:block
			$('.not-tab').css('display','block');

			// Load main information to home div
			$(Tabs.home_tab).load(Authentication.main_page, null, function() {

				// Display logout name in logout button
				$(Home.logout_button).attr('value', 'Logout: '+data.username);
				$(Home.logout_button).css('display', 'block');

				// Initialize dashboard layout
				Dashboard.init();

				//Reload project list with current project as selected, after that setup Dashboard
				ProjectMan.reloadProjectList(Home.current_project, function(){
					Dashboard.setupEvents(data);
				});

				//Setup timeout
				$('body').unbind('mousemove').bind('mousemove', function(){
					Authentication.resetTimeout();
				});

				//Bind the close/refresh/move page events to clear locks the user holds.

				$(window).unload(function(){ 
					$.ajax({
						type: 'POST',
						async: false,
						url: 'ClearLocks.do'
					});
				});

				//Setup metadb logo
				$(Home.context).css({
					'background-image': 'url("images/metadb-logo.png")',
					'background-repeat': 'no-repeat',
					'background-position': 'right bottom'
				});
			});
		},

		// Reset logout timeout
		resetTimeout: function() {
			clearTimeout(Authentication.timer);
			Authentication.timer = setTimeout(function(){
				Helper.clearLocks();
				$(Authentication.logout_warning).dialog('open');
			}, Authentication.timeout);
		},

		/**
		 * Reset tabs when login fails
		 * @param data data from Login servlet
		 */
		loginFailure : function(data) {
			//Navigate to home tab if in other tabs 
			if ($(Authentication.main_content).tabs('option', 'selected') != 0)
				$(Authentication.main_content).tabs('select', 0);
			else // Otherwise, refresh that tab
				$(Authentication.main_content).tabs('load', 0);
			for (var i = 1; i < 6; i++) //Disable all other tabs
				$(Authentication.main_content).tabs('disable', i);

			// Hide project choice
			$('.not-tab').css('display','none');

			clearTimeout(Authentication.timer);

			//Disable logout timeout
			$('body').unbind('mousemove');

			// Load the login form and display a different message
			$(Home.logout_button).attr('value', 'Logout');
			$(Home.logout_button).css('display', 'none');

			// Display the metadb logo again
			$(Authentication.main_content).css({
				'background-image': 'url("images/metadb-logo.png")',
				'background-repeat': 'no-repeat',
				'background-position': 'right bottom'
			});
		},

		// Check the current session, if not expired then login automatically
		checkSession : function() {
			success = false;
			//Send a request to the CheckSession servlet to check whether the session is valid
			$.post("CheckSession.do", null, function(data) {
				success = data.success;
				if (!success) {
					Authentication.loginFailure();
				} else {
					Authentication.loginSuccess(data);
				}
			}, 'json');
			return success;
		},

		// Validate the login form in the home page, display message accordingly
		loginValidate : function() {
			var info = $(Home.login_form).serialize();
			$.post("Login.do", info, function(auth) {
				if (auth.success) {
					Authentication.loginSuccess(auth);
				} else {
					$(Authentication.welcome_message).html('<div class="ui-state-error ui-corner-all" style="padding: 2px; width:310px;">'
							+ '<p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>'
							+ '<strong>Alert: </strong>'
							+ auth.message + '</p>');
				}
			}, 'json');
		},

		//Logout, simply call loginFailure
		logout : function() {
			//Send an AJAX request to kill the session
			$.post("Logout.do", null, function(data) {
				Authentication.loginFailure();
				Helper.showNotification(data);
			}, 'text');
		},

		// Show the tabs according to the user permission and project chosen
		showMenu: function() {
			$.post("GetPermission.do",null, function(settings){
				if (!settings.admin) {
					$(Authentication.main_content).tabs('disable', 1);
					$(Authentication.main_content).tabs('disable', 2);
					$(Authentication.main_content).tabs('enable', 3);
					$(Authentication.main_content).tabs('enable', 4);
					$(Authentication.main_content).tabs('enable', 5);
				} else {
					for (var i = 0; i < 6; i++)
						$(Authentication.main_content).tabs('enable', i);
				}

			}, 'json');
		}
};
/**********************************************************************
 *                E N D  O F  G E N E R A L  S C R I P T			  *
 **********************************************************************/


/**********************************************************************
 *               M A N A G E M E N T  S C R I P T S					  *
 **********************************************************************/

/**
 * Script in charge of userman folder htmls
 * ID: UMS
 */
var UserMan = {

		// static global variables
		create_pwd_field : '#create-user-password', //Used to un-require password for LDAP users
		create_pwd_field_confirm: '#create-confirm-password',
		change_pwd_field : '#change-user-password', //Used to un-require password for LDAP users
		change_pwd_field_confirm: '#change-confirm-password',
		create_user_form : '#create-user-form',
		change_user_info_form : '#change-user-info-form',
		change_user_info : '#change-user-info',
		self_pwd_form : '#self-change-form',
		change_access_username : '#change-access-username',
		proj_choice_access : '#project-choice-access',
		permission_cell : '.permission-cell',
		user_access_table : '#user-access-table',
		user_manipulation: '#user-manipulation',
		create_user_div: '#create-user',
		user_list: '.user-list',
		auth_type: '.auth-type',
		user_type: '.user-type',
		delete_form: '#delete-form',
		delete_confirmation: '#delete-user-confirmation',

		current_user: '',

		// Initialize the page
		init: function() {
			Helper.clearLocks();
			$('#user-sub-nav').buttonset();
			$('label[for="user-sub-nav-create"]').click(function(){
				UserNav.gotoCreateUser();
			});
			$('label[for="user-sub-nav-delete"]').click(function(){
				UserNav.gotoDeleteUser();
			});
			$('label[for="user-sub-nav-settings"]').click(function(){
				UserNav.gotoChangeUserInfo();
			}).trigger('click');
		},

		setupDeleteUserConfirmation: function() {
			$(UserMan.delete_confirmation).dialog({
				autoOpen: false,
				bgiframe: true,
				resizable: false,
				height:180,
				modal: true,
				overlay: {
					backgroundColor: '#000',
					opacity: 0.5
				},
				buttons: {
					'Delete User': function() {
						UserMan.deleteUser();
						$(this).dialog('close');
					},
					Cancel: function() {
						$(this).dialog('close');
					}
				}

			});
		},

		/**
		 * Create user based on serialized form
		 */
		createUser: function() {
			var validator = $(UserMan.create_user_form).validate();
			if ($(UserMan.create_user_form).valid()) { //Validate the form
				var info = $(UserMan.create_user_form).serialize();
				$.post("CreateUser.do", info, function(results) {
					Helper.showNotification(results.message);
					if (results.success) {
						// Wipe out old data 
						$(UserMan.create_user_form+' input:text').val('');
						$(UserMan.create_user_form+' input:password').attr('value','');	

						if (!results.admin) 
							UserMan.current_user = results.username;
						$('label[for$="settings"]').trigger('click');
					}
				}, 'json');
			}
		},

		// If LDAP option is chosen in the "type" box, disable password field
		checkLDAP: function() {
			var choice = $(UserMan.auth_type+' option:selected');
			if (choice.val()=="LDAP")
			{
				$('.local-pwd').css('visibility','hidden');
				$(UserMan.create_pwd_field).removeClass('required');
				$(UserMan.create_pwd_field_confirm).removeAttr('equalTo');
				$(UserMan.change_pwd_field).removeClass('required');
				$(UserMan.change_pwd_field_confirm).removeAttr('equalTo');
			}
			else
			{
				$('.local-pwd').css('visibility','visible');
				$(UserMan.create_pwd_field).addClass('required');
				$(UserMan.create_pwd_field_confirm).attr('equalTo', 'change-user-password');
				$(UserMan.change_pwd_field).addClass('required');
				$(UserMan.change_pwd_field_confirm).attr('equalTo', 'change-user-password');
			}
		},

		// Reload the list of users 
		// NOTE: There is a hack here which updates the auth type to the user selected. 
		// Might look into moving it out elsewhere--see JSON creation in ShowUsers.java
		reloadUserList: function(fn) {
			var page = "edit";
			$.post("ShowUsers.do",{'page':page},function(results){
				var workers = results.workers;
				var admins = results.admins;
				var permissions = results.permissions;

				$(UserMan.user_list).empty();
				$(UserMan.user_list).append("<optgroup label='admin'>");
				for (var i in admins) 
					$(UserMan.user_list).append("<option disabled>"+admins[i].username+"</option>");
				$(UserMan.user_list).append("</optgroup>");

				$(UserMan.user_list).append("<optgroup label='workers'>");
				for (var i in workers) 
					$(UserMan.user_list).append("<option>"+workers[i].username+"</option>");
				$(UserMan.user_list).append("</optgroup>");

				var username = UserMan.current_user;

				if (username != "") {
					Helper.choose($(UserMan.user_list), username);
					Helper.choose($(UserMan.auth_type), permissions[username].auth_type);
					Helper.choose($(UserMan.user_type), permissions[username].user_type);
				}
				else
				{
					Helper.choose($(UserMan.auth_type), permissions[$(UserMan.user_list)[0].value].auth_type);
					Helper.choose($(UserMan.user_type), permissions[$(UserMan.user_list)[0].value].user_type);					
				}

				UserMan.current_user = '';
				$(UserMan.user_list).unbind('change').bind('change', function(ev){
					UserMan.showPermissions();
					UserMan.checkLDAP();
				});
				if ($.isFunction(fn)) {
					fn();
				}
			}, 'json');	
		},

		// Delete a user
		deleteUser: function() {
			$.post("DeleteUser.do",{"username":$(UserNav.delete_user_div+' select').val()}, function(data){ 
				Helper.showNotification(data);
				UserMan.reloadUserList();
			}, 'text');
		},

		// Update a user
		updateUser: function() {
			var $select = $(UserMan.user_list)[0];
			UserMan.current_user = $select.value;
			var check = $(UserMan.change_user_info_form).validate();
			if ($(UserMan.change_user_info_form).valid())
				$.post('UpdateUserInfo.do', $(UserMan.change_user_info_form).serialize(), function(data){
					Helper.showNotification(data);
					UserMan.reloadUserList();
					$(UserMan.change_user_info_form+' input:text').attr('value','');
					$(UserMan.change_user_info_form+' input:password').attr('value','');
				}, 'text');
		},

		// Update your own password
		updateSelf: function() {
			//Validate the form first, display the error on the next row
			var check = $(UserMan.self_pwd_form).validate({
				errorPlacement: function(error, element) {
					error.appendTo(element.parents('tr:first').next('tr'));
				},
				debug:true
			});

			// If valid, submit the info
			if ($(UserMan.self_pwd_form).valid())
				$.post('UpdateSelfPassword.do', $(UserMan.self_pwd_form).serialize(), function(data){
					Helper.showNotification(data);
					$(UserMan.self_pwd_form+' input:text').attr('value','');
					$(UserMan.self_pwd_form+' input:password').attr('value','');
				}, 'text');
		},

		// Get the list of projects the user has access to
		getProjectPermissionList: function(username) {
			if (username === undefined)
				username = $(UserMan.user_list).val();
			$.post('ShowProjectPermission.do', {'username':username}, function(results){
				var displayData = "";
				for (var i in results)
					displayData += "<option>"+results[i]+"</option>";
				$(UserMan.proj_choice_access).empty();
				$(UserMan.proj_choice_access).html(displayData);
			}, 'json');
		},

		// Show the permission table
		showPermissions: function() {
			var $select = $(UserMan.user_list)[0];
			var username = $select.value;
			UserMan.current_user = username;
			$.post('ShowPermissions.do',{'username':username},function(results){
				$(UserMan.permission_cell).remove();
				$(UserMan.user_access_table+' tbody tr:last').before(results.data);
				$('input[value="Revoke"]').button().unbind('click').bind('click',function(e) {
					UserMan.revoke(e.target);
					return false;
				});
				$(UserMan.user_access_table+' input:radio[name*="-"]').unbind('click').bind('click',function(e) {
					UserMan.updatePermission(e.target);		
				});
				TableHandler.altColor($(UserMan.user_access_table));
				Helper.choose($(UserMan.auth_type), results.info.auth_type);
				Helper.choose($(UserMan.user_type), results.info.user_type);
				UserMan.checkLDAP();
			}, 'json');
		},

		/**
		 * Revoke a user's permission to a project
		 * @param button the revoke button that got clicked
		 */ 
		revoke: function(button) {
			var username = $(UserMan.user_list+' option:selected').val();
			var projname = $(button).parents('tr').children('td:first').html();

			$.post('DeletePermission.do', {'username':username, 'projname': projname}, function(data){
				Helper.showNotification(data);
				UserMan.showPermissions();
				UserMan.getProjectPermissionList();
			}, 'text');
		},

		// Grant permission of a project to a user
		grant: function() {
			var username = $(UserMan.user_list+' option:selected').val();
			var projname = $(UserMan.proj_choice_access).val();
			var data = $('input[name="data"]:checked').val();
			var admin = $('input[name="admin_md"]:checked').val();
			var desc = $('input[name="desc_md"]:checked').val();
			var table_edit = $('input[name="table_edit"]:checked').val();
			var controlled_vocab = $('input[name="controlled_vocab"]:checked').val();

			$.post('AddPermission.do',{
				'username':username,
				'projname':projname,
				'data':data,
				'admin_md':admin,
				'desc_md': desc,
				'table_edit': table_edit,
				'controlled_vocab': controlled_vocab
			},function(data){
				Helper.showNotification(data);
				UserMan.showPermissions();
				UserMan.getProjectPermissionList();
			}, 'text');
		},

		/**
		 * Update permission based on the radio buttons in permission page
		 * @param checkbox the permission checkbox that got checked
		 */ 
		updatePermission: function(checkbox) {
			var username = $(UserMan.user_list+' option:selected').val();
			var $row = $(checkbox).parents('tr:first');
			var query = '&username='+username+'&'+UserMan.serializePermissionRow($row);
			$.post('UpdatePermission.do', query,function(data){
				Helper.showNotification(data);
			}, 'text');
		},

		/**
		 * Serialize a permission row
		 * @param $row row that get serialized, *important*: wrapped in jQuery object (ex. $(row))
		 */
		serializePermissionRow: function($row) {
			var projname = 'projname='+$row.children('td:first').html();
			var data = $row.find('input:radio').serialize();
			return projname+'&'+data;
		}

};

/**
 * Script in charge of managing project html events
 * ID: PMS
 */
var ProjectMan = {
		custom_md_table : '#custom-fields',
		custom_md_indicator : '#add-custom-md',
		custom_md_form : '#custom-md-data',

		admin_md_table : '#custom-fields-am',
		admin_md_indicator : '#add-admin-md',
		admin_md_form : '#admin-md-data',

		desc_md_table : '#custom-fields-dm',
		desc_md_indicator : '#add-desc-md',
		desc_md_form : '#desc-md-data',

		project_choice : '#project-choice-edit',
		project_info_form : '#project-info-data',
		project_choice_main_page: '#project-choice-main',
		project_image_settings: '#project-image-settings',

		project_list_main: '.project-list-main',

		old_project: '#old-project',
		project_template: '.project-template',

		delete_form: '#delete-form',
		delete_confirmation: '#delete-project-confirmation',
		delete_attr_confirmation: '#delete-attr-confirmation',
		delete_id: undefined,

		current_action: "",

		// Initialize the project management page
		init: function() {
			Helper.clearLocks();
			ControlledVocab.showVocabListModal();
			$('#project-sub-nav').buttonset();
			$('label[for="project-sub-nav-create"]').click(function(){
				ProjectNav.gotoCreateProject();
			});
			$('label[for="project-sub-nav-edit"]').click(function(){
				ProjectNav.gotoEditProject();
			}).trigger('click');
			$('label[for="project-sub-nav-delete"]').click(function(){
				ProjectNav.gotoDeleteProject();
			});

		},


		setupDeleteProjConfirmation: function() {
			$(ProjectMan.delete_confirmation).dialog({
				autoOpen: false,
				bgiframe: true,
				resizable: false,
				height:180,
				modal: true,
				overlay: {
					backgroundColor: '#000',
					opacity: 0.5
				},
				buttons: {
					'Delete Project Data': function() {
						ProjectMan.deleteProject();
						$(this).dialog('close');
					},
					Cancel: function() {
						$(this).dialog('close');
					}
				}

			});
		},

		setupDeleteAttrConfirmation: function() {
			$(ProjectMan.delete_attr_confirmation).dialog({
				autoOpen: false,
				bgiframe: true,
				resizable: false,
				height:180,
				modal: true,
				overlay: {
					backgroundColor: '#000',
					opacity: 0.5
				},
				buttons: {
					'Delete Attribute': function() {
						ProjectMan.deleteMD();
						$(this).dialog('close');
					},
					Cancel: function() {
						$(this).dialog('close');
					}
				}

			});
		},

		/**
		 * Reload the list of the projects
		 * @param selected the project to be set as active
		 * @param fn callback function
		 */ 
		reloadProjectList: function(selected, fn) {
			$.post("ShowProjects.do", null, function(data){
				$(Home.project_select).html(data);
				ProjectMan.changeMainProject(selected, function(){
					Authentication.showMenu();
				});
				if ($.isFunction(fn))
					fn();
			}, 'text');
		},

		// Get the working project
		getWorkingProject: function() {
			$.post("GetWorkingProject.do", null, function(data){
				Home.current_project = data;
			}, 'text');
		},

		// Change the working project in the session
		changeWorkingProject: function() {
			var newProj = $(ProjectMan.project_list_main+' option:selected').val();
			if (newProj === undefined)
				newProj = "";
			$.post("ChangeWorkingProject.do", 
					{"working-project":newProj, 
					}, 
					function(data){ 
						Home.current_project = data;
						switch ($(Pages.current_page).html()) {
						case Pages.export_page:
							DataNav.gotoExportData();
							break;
						case Pages.import_page:
							DataNav.gotoImportData();
							break;

						}
					}, 'text');
		},

		// Reload the tabs based on the project chosen
		reloadProject: function() {
			ProjectMan.reloadProjectData();
			ProjectMan.showMD("administrative");
			ProjectMan.showMD("descriptive");
			ProjectMan.showImageSettings();
		},

		// Show the image settings in Edit Project
		showImageSettings: function() {
			$(ProjectMan.project_image_settings).load('management/projectman/image_settings.html '+ ImageSettings.settings_form, null, function(data){
				ImageSettings.loadDerivativeSettings();
				Helper.setUpColorPicker('#text-colorpicker-custom','color');
				Helper.setUpColorPicker('#background-colorpicker-custom','background-color');
				Helper.setUpColorPicker('#text-colorpicker-fullsize','color');
				Helper.setUpColorPicker('#background-colorpicker-fullsize','background-color');

			});

		},

		/**
		 * Reload the project info in the edit project page
		 * @param fn the callback function after AJAX request response
		 */ 
		reloadProjectData: function(fn) {
			var name = $(ProjectMan.project_list_main+' option:selected').val();
			$.post("ShowProjectData.do", {"projname":name}, function(data){
				var info = data.split(";");
				$('#projname').val(info[0]);
				$('#projnotes').val(info[1]);
				if ($.isFunction(fn))
					fn();
			}, 'text');
		},

		// Delete a project
		deleteProject: function() {
			var options = $('#delete-form option:selected').attr('name');
			$.post('DeleteProject.do', {'delete-choices' : options}, function(data){
				Helper.showNotification(data);
				var current = $(Home.project_select).val();
				ProjectMan.reloadProjectList(current);
			}, 'text');
		},

		// Wrapper method to change the project information based on the action (create or edit)
		changeProject: function() {
			var action = $('#current-action').html();
			if (action.indexOf('edit')!=-1) {
				return false;
			}
			else
				$(ProjectMan.project_info_form).submit();
		},

		// Create a new project
		createProject: function() {
			var check = $(ProjectMan.project_info_form).validate();
			var info = $(ProjectMan.project_info_form).serialize();
			if ($(ProjectMan.project_info_form).valid()) {
				$.post("CreateProject.do", info, function(data){
					Helper.showNotification(data.message);
					if (data.success) {
						ProjectMan.reloadProjectList();
						$('label[for$="edit"]').trigger('click');
					}
				}, 'json');
			}
		},

		/**
		 * Setup events for admin or desc table
		 * @param table the actual admin/desc table
		 */
		setupEvents: function(table) {
			//bind events to handlers
			var type = 'descriptive';
			if (table == ProjectMan.admin_md_table)
				type = 'administrative';

			$(table+' span[name*="add"]').css('cursor','pointer').live('click',function(e) {
				TableHandler.addMDRow(table);
				ProjectMan.refreshMDTable(table);
			});

			var controlled_boxes = table+' input[name*="control"]';

			//Show vocab manager when controlled checkbox is clicked
			$(controlled_boxes).live('click',function(e) {
				var row = $(e.target).parents('tr:first');
				if (!this.checked) {
					$(e.target).removeAttr('title');
					ProjectMan.changeControlledBox(e.target,'disable');
				}
				else
					ControlledVocab.showVocabModal(this);
			});

			var date_boxes = table+' input[name$="date"]';
			$(date_boxes).live('click', function(e) { 
				var row = $(e.target).parents('tr:first');
				var isDate = this.checked;
				if (isDate) {
					var $others = row.find('input:checkbox:[name!="'+this.name+'"]');
					$others.attr("disabled","true");
					$others.removeAttr("checked");
				}
				else {
					row.find('input[name="large"]').removeAttr("disabled");
					row.find('input[name="large"]').removeAttr("checked");
					row.find('input[name="controlled"]').removeAttr("disabled");
					row.find('input[name="controlled"]').removeAttr("checked");
					row.find('input[name$="date"]').removeAttr("disabled");
					row.find('input[name$="date"]').removeAttr("checked");
				}
			});
			ProjectMan.bindDeleteAttrConfirm();
		},

		bindDeleteAttrConfirm: function() {
			$(ProjectMan.admin_md_table+' span[name*="delete"]').live('click',
					function(e) {
				ProjectMan.delete_id = $(e.target).parents('tr:first').find('input:hidden').val();
				$(ProjectMan.delete_attr_confirmation).dialog('open');
			});
			$(ProjectMan.desc_md_table+' span[name*="delete"]').live('click',
					function(e) {
				ProjectMan.delete_id = $(e.target).parents('tr:first').find('input:hidden').val();
				$(ProjectMan.delete_attr_confirmation).dialog('open');
			});				
		},

		/**
		 * Teardown all the events in admin/desc table
		 * @param table the admin or desc table
		 */
		teardownEvents: function(table) {
			$(table+' span[name*="delete"]').die('click');
			$(table+' span[name*="add"]').die('click');
			$(table+' input[name*="control"]').die('click');
			$(table+' input[name*="date"]').die('click');
		},

		/**
		 * Refresh all the events in admin/desc table
		 * @param table the admin or desc table
		 */
		refreshEvents: function(table) {
			ProjectMan.teardownEvents(table);
			ProjectMan.setupEvents(table);
		},

		/**
		 * Get the default seed row for admin/desc table
		 * @param elementList the list of elements
		 */
		getDefaultMDRow: function(elementList) {
			var row = '<tr>';
			row += "<td class='last-attribute'></td>";
			row += "<td class='last-attribute'><select name='elements' class='ui-widget-content'>"+elementList+"</select></td>";
			row += "<td class='last-attribute'><input type='text' class='ui-widget-content' name='label' size='25'/></td>\n";
			row += "<td style='width:0px'><input type='hidden' name='id' size='25' value='-1'/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='search-date'/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='display-date'/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='large'/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='controlled'/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='multiple' disabled/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='additions' disabled/></td>\n";
			row += "<td class='last-attribute'><input type='checkbox' name='sorted' disabled/></td>\n";
			row += "<td><span class='ui-icon ui-icon-plusthick' name='add' style='cursor:pointer; margin: auto' /></td>\n";
			row += '</tr>';
			return row;
		},


		/**
		 * Setup 1 metadata row in admin/desc table
		 * @param $row the row getting processed, wrapped in jQuery
		 * @param attr the attributes of an element.label (controlled, search, large...)
		 */
		setupMDRow: function($row, attr) {
			if (attr.error)
				$row.addClass('ui-state-error');
			$row.children('td').removeClass('last-attribute');
			$row.children('td:last').html("<span class='ui-icon ui-icon-minusthick' name='delete' style='cursor:pointer; margin:auto'/>");
			$row.children('td:first').html("<td class='dragHandle'><span class='ui-icon ui-icon-arrowthick-2-n-s' style='cursor: move; margin:auto'/></td>");
			Helper.choose($row.find('select'), attr.element);

			$row.find('input:text[name="label"]').attr('value', attr.label);
			$row.find('input:hidden[name="id"]').attr('value', attr.id);

			var isControlled = attr.controlled;
			if (attr.displayDate) {
				$row.find('input:checkbox[name="display-date"]').attr('checked', true);
				$row.find('input:checkbox[name!="display-date"]').attr('disabled', true);
			}
			else if (attr.searchDate) {
				$row.find('input:checkbox[name="search-date"]').attr('checked', true);
				$row.find('input:checkbox[name!="search-date"]').attr('disabled', true);
			}
			else {
				$row.find('input:checkbox[name="large"]').attr('checked', attr.large);
				if (isControlled) {
					$row.find('input:checkbox[name="controlled"]').attr('checked', true);
					$row.find('input:checkbox[name="controlled"]').attr('title', attr.vocab);
					$row.find('input:checkbox[name="sorted"]').attr('checked', attr.sorted);
					$row.find('input:checkbox[name="sorted"]').removeAttr('disabled');
					$row.find('input:checkbox[name="multiple"]').attr('checked', attr.multiple);
					$row.find('input:checkbox[name="multiple"]').removeAttr('disabled');
					$row.find('input:checkbox[name="additions"]').attr('checked', attr.additions);
					$row.find('input:checkbox[name="additions"]').removeAttr('disabled');
				}
			}
		},

		/**
		 * Setup a metadata table
		 * @param table the admin/desc table
		 * @param data the data to be filled in that table from the servlet
		 */
		setupMDTable: function(table, data) {
			var items = 'tbody > tr';
			var type = 'descriptive';
			var indicator = ProjectMan.desc_md_indicator;

			if (table == ProjectMan.admin_md_table) {
				type = 'administrative';
				indicator = ProjectMan.admin_md_indicator;

			}

			$(table+' thead').after('<tbody></tbody><tfoot></tfoot>');
			var elementList = Helper.setupSelectBox(data.elements, "");
			var defaultRowHTML = ProjectMan.getDefaultMDRow(elementList);

			$(table+' tfoot').html(defaultRowHTML);

			var attributeList = data.data;
			for (var i in attributeList) {
				$(table+' tbody').append(defaultRowHTML).hide();
				var $defaultRow = $(table+' tbody tr:last'); 
				ProjectMan.setupMDRow($defaultRow, attributeList[i]);
			}

			$(table+' tbody').show();

			$(table).sortable({
				connectWith: '.connectedMD',
				items : items,
				forcePlaceholderSize: true, 
				handle: '.dragHandle',
				cursor: 'move',
				start: function(e, ui) {
					$(ui.item).addClass('.ui-state-highlight');
				},
				update: function(e, ui) {
					$(ui.item).removeClass('.ui-state-highlight');
					ProjectMan.updateAllMD(function(){
						ProjectMan.showMD('descriptive');
						ProjectMan.showMD('administrative');
					});
				}
			});

			$(indicator+' tr:first td:last').html($(table+' tbody tr').length);
		},



		/**
		 * Refresh admin/desc table events if there're data changes
		 * @param table the admin/desc table
		 */
		refreshMDTable: function(table) {
			var indicator = ProjectMan.desc_md_indicator;

			if (table == ProjectMan.admin_md_table) 
				indicator = ProjectMan.admin_md_indicator;

			//refresh sortable table
			$(table).sortable('refresh');

			var rowNumber = $(table+' tbody tr').length;
			$(indicator+' tr:first td:last').html(rowNumber);
		},

		/**
		 * Enable/disable controlled checkbox
		 * @param button the controlled checkbox of a particular row
		 * @param type either enable or disable
		 */
		changeControlledBox: function(checkbox, type) {
			var row = $(checkbox).parents('tr:first');
			if (type == "enable") {
				row.children('td:has(input[name*="sorted"])').children().removeAttr("disabled");
				row.children('td:has(input[name*="multiple"])').children().removeAttr("disabled");
				row.children('td:has(input[name*="addition"])').children().removeAttr("disabled");
			}
			else if (type == "disable") {
				row.children('td:has(input[name*="sorted"])').children().attr("disabled","disabled");
				row.children('td:has(input[name*="sorted"])').children().removeAttr("checked");
				row.children('td:has(input[name*="multiple"])').children().attr("disabled","disabled");
				row.children('td:has(input[name*="multiple"])').children().removeAttr("checked");
				row.children('td:has(input[name*="addition"])').children().attr("disabled","disabled");
				row.children('td:has(input[name*="addition"])').children().removeAttr("checked");
			}
		},

		/**
		 * Update the admin/desc table
		 * @param type either admin or desc
		 * @param fn callback function after update is completed (can be success or failure)
		 */
		updateMD: function(type, fn) {
			var table = ProjectMan.desc_md_table;
			if (type == 'administrative')
				table = ProjectMan.admin_md_table;
			var info = {};
			info['data'] = [];
			info['type'] = type;
			var i = 0;
			$(table+' tbody > tr').each(function(){
				info['data'][i] = ProjectMan.processRow($(this));
				i++;
			});

			var sending = JSON.stringify(info);
			$.post("UpdateAdminDescAttributes.do", {'data':sending}, function(data){
				ProjectMan.refreshEvents(table);
				if (!data.success) {
					Helper.alert("Update Results", data.message);
				}
				if ($.isFunction(fn))
					fn();
			}, 'json');
		},

		/**
		 * Serialize 1 row in admin/desc table
		 * @param $row the row to be serialized, wrapped in jQuery
		 */
		processRow: function($row) {
			var inputs = {};
			$row.find('input, select').each(function(){
				if (this.type == 'checkbox')
					inputs[this.name] = this.checked;
				else
					inputs[this.name] = this.value;
			});
			return inputs;
		},

		/**
		 * Update both admin and desc table
		 * @param fn callback function after update is successful
		 */
		updateAllMD: function(fn) {
			ProjectMan.updateMD('administrative', function() {
				ProjectMan.updateMD('descriptive', function() {
					if ($.isFunction(fn))
						fn();
				});
			});
		},

		/**
		 * Delete 1 attribute from admin/desc row
		 * @param button the delete button (- sign)
		 */
		deleteMD: function() {
			$.post("DeleteAdminDescAttribute.do", {'id':ProjectMan.delete_id}, function(data){
				Helper.showNotification(data);
				ProjectMan.showMD("administrative");
				ProjectMan.showMD("descriptive");
			}, 'text');
			ProjectMan.bindDeleteAttrConfirm();
			ProjectMan.delete_id = undefined; //reset the delete attribute id
		},

		/**
		 * Show admin/desc table
		 * @param type either admin/desc
		 */
		showMD: function(type) {
			var projname = $(ProjectMan.project_choice+' option:selected').val();
			$.post('ShowAdminDescAttributes.do',{'projname':projname, 'type': type}, function(data){
				var table = ProjectMan.desc_md_table;
				if (type == 'administrative')
					table = ProjectMan.admin_md_table;
				$(table+' tbody').remove();
				$(table+' tfoot').remove();
				ProjectMan.setupMDTable(table, data);

				$(table).parents('form:first')
				.find('input:button, input:submit')
				.each(function(){
					$(this).button();
					if ($(this).hasClass('add-admin-md-button'))
						$(this).unbind('click').click(function(){
							TableHandler.addAdminMDRows();
							return false;
						});
					else if ($(this).hasClass('add-desc-md-button'))
						$(this).unbind('click').click(function(){
							TableHandler.addDescMDRows();
							return false;
						}); 
					else if ($(this).hasClass('update-desc-md-button'))
						$(this).unbind('click').click(function(){
							ProjectMan.updateMD('descriptive', function(){
								ProjectMan.showMD('descriptive');
							});
							return false;
						}); 
					else if ($(this).hasClass('update-admin-md-button'))
						$(this).unbind('click').click(function(){
							ProjectMan.updateMD('administrative', function(){
								ProjectMan.showMD('administrative');
							});
							return false;
						}); 
				});
			}, 'json');	
		},

		/**
		 * Change the current project
		 * @param projname the project to change to
		 * @param fn the callback function after change is made
		 */
		changeMainProject: function(projname, fn) {
			Helper.choose($(ProjectMan.project_list_main), projname);
			if ($.isFunction(fn))
				$(ProjectMan.project_list_main).bind('change', fn);
			$(ProjectMan.project_list_main).trigger('change');
			if ($.isFunction(fn))
				$(ProjectMan.project_list_main).unbind('change', fn);
		}	
};

/**
 * Script in charge of image settings html
 * ID: ISS
 */
var ImageSettings = {

		project_name: '#projname',
		thumb_row: '#thumbnail-row',
		custom_row: '#custom-row',
		zoom_row: '#zoom-row',
		fullsize_row: '#fullsize-row',
		color_picker: '.color-picker',
		
		custom_txt_colorpckr: '#text-colorpicker-custom',
		custom_bg_colorpckr: '#background-colorpicker-custom',
		
		fullsize_txt_colorpckr: '#text-colorpicker-fullsize',
		fullsize_bg_colorpckr: '#background-colorpicker-fullsize',
		
		custom_txt_color: '#text-color-custom',
		custom_bg_color: '#background-color-custom',
		
		fullsize_txt_color: '#text-color-fullsize',
		fullsize_bg_color: '#background-color-fullsize',
		
		preview_custom_brand: '#preview-custom-brand',
		preview_fullsize_brand: '#preview-fullsize-brand',
		
		settings_form: '#image-derivative-settings',
		default_instruction: 'ex: 1,2,7-10',
		settings_form: '#image-derivative-settings',
		parse_confirmation:'#parse-confirmation',
		zoom_hostname: '.zoom-hostname',

		// Wrapper for loading project settings and image settings
		loadSettings: function() {
			$.post('ShowProjectSettings.do', null, function(data){
				$('fieldset:first').html(data);
				ImageSettings.loadDerivativeSettings();
			}, 'text');
		},

		// Load derivative settings and set up layout
		loadDerivativeSettings: function() {
			$.post("ShowDerivativeSettings.do", null, function(settings){

				$(ImageSettings.thumb_row).find('input:text:first').attr('value', settings.thumb.height);
				$(ImageSettings.thumb_row).find('input:text:last').attr('value', settings.thumb.width);

				$(ImageSettings.zoom_row).find('input:text:first').attr('value', settings.zoom.height);
				$(ImageSettings.zoom_row).find('input:text:last').attr('value', settings.zoom.width);

				$(ImageSettings.custom_row).find('input:not(:first)').attr('disabled', 'true');
				$(ImageSettings.custom_row).find('input:checkbox:first').removeAttr('checked');

				$(ImageSettings.custom_row).find('input:checkbox:first').unbind('click').bind('click', function() {
					if (this.checked) {
						this.checked = true;
						$(ImageSettings.custom_row).find('input:not(:first)').removeAttr('disabled');
						$(ImageSettings.custom_row).find('input:radio').removeAttr('checked');
						$(ImageSettings.custom_row).find('input:radio[value="none"]').attr('checked', true);
					}
					else {
						this.checked = false;
						$(ImageSettings.custom_row).find('input:not(:first)').attr('disabled', 'true');
					}
				});


				if (settings.custom.enabled) { 
					$(ImageSettings.custom_row).find('input:checkbox:first').trigger('click');
					$(ImageSettings.custom_row).find('input:not(:first)').removeAttr('disabled');
					$(ImageSettings.custom_row).find('input:text:first').attr('value', settings.custom.height);
					$(ImageSettings.custom_row).find('input:text:last').attr('value', settings.custom.width);
					$(ImageSettings.custom_row).find('input:radio:eq(' + settings.custom.brand_mode + ')').attr('checked', 'checked');
					$(ImageSettings.custom_row).find(ImageSettings.color_picker+':first').children('div').css('background-color', settings.custom.text_color);
					$(ImageSettings.custom_row).find(ImageSettings.color_picker+':last').children('div').css('background-color', settings.custom.background_color);
					$(ImageSettings.custom_row).find('td:last').css('color', settings.custom.text_color);
					$(ImageSettings.custom_row).find('td:last').css('background-color', settings.custom.background_color);
				}


				$(ImageSettings.fullsize_brand).unbind('click').bind('click', function() {
					if (this.checked) {
						this.checked = true;
						$(ImageSettings.fullsize_row).find('input:not(:first)').removeAttr('disabled');
						$(ImageSettings.fullsize_row).find('input:radio').removeAttr('checked');
						$(ImageSettings.fullsize_row).find('input:radio[value="none"]').attr('checked', true);
					}
					else {
						this.checked = false;
						$(ImageSettings.fullsize_row).find('input:not(:first)').attr('disabled', 'true');
					}
				});

				if (settings.fullsize.enabled) {
					$(ImageSettings.fullsize_row).find('input:checkbox:first').trigger('click');
					$(ImageSettings.fullsize_row).find('input:not(:first)').removeAttr('disabled');
					$(ImageSettings.fullsize_row).find('input:radio:eq(' + settings.fullsize.brand_mode + ')').attr('checked', 'checked');
					$(ImageSettings.fullsize_row).find(ImageSettings.color_picker+':first').children('div').css('background-color', settings.fullsize.text_color);
					$(ImageSettings.fullsize_row).find(ImageSettings.color_picker+':last').children('div').css('background-color', settings.fullsize.background_color);
					$(ImageSettings.fullsize_row).find('td:last').css('color', settings.fullsize.text_color);
					$(ImageSettings.fullsize_row).find('td:last').css('background-color', settings.fullsize.background_color);
				}
				//Hide/show the color pickers upon first load
				ImageSettings.handleCustomRadioToggle();
				ImageSettings.handleFullSizeRadioToggle();

				//Handle the radio toggles for branding
				$("input[name='custom-brand-radio']").change(function(){
					ImageSettings.handleCustomRadioToggle();
				});

				$("input[name='fullsize-brand-radio']").change(function(){
					ImageSettings.handleFullSizeRadioToggle();
				});


				$(ImageSettings.settings_form+' textarea').html(settings.fullsize.brand_text);

				// Set up the select box for process these/all
				$(ImageSettings.settings_form).find("select").unbind('change').bind('change', function(){
					if ($(this).find('option:selected').val().toLowerCase() == 'these') {
						$(this).siblings('input:text').removeAttr('style');
						$(this).siblings('input:text').attr('value', ImageSettings.default_instruction);
						$(this).siblings('input:text').bind('click', function() { 
							if ($(this).attr('value') == ImageSettings.default_instruction)
								$(this).attr('value','');
						});
					}
					else {
						$(this).siblings('input:text').unbind('click');
						$(this).siblings('input:text').css('visibility', 'hidden');
					}
				});

				// Set up save settings button
				$(ImageSettings.settings_form).find('input:button:first').button().bind('click', function(){
					ImageSettings.saveSettings();
					return false;
				});

				//$(ImageSettings.zoom_hostname).attr('value', settings.hostname);

				// Set up confirmation dialog box
				$(ImageSettings.parse_confirmation).dialog('destroy');
				ImageSettings.setupParseConfirmation();
				$(ImageSettings.settings_form).find('input:button:last').button().bind('click', function(){
					$(ImageSettings.parse_confirmation).dialog('open');
				});


			}, 'json');
		},

		handleCustomRadioToggle: function()
		{
			if ($("input[name='custom-brand-radio']:checked").val() == 'none') //no brand
			{
				$(ImageSettings.custom_txt_colorpckr).css('visibility', 'hidden');
				$(ImageSettings.custom_bg_colorpckr).css('visibility', 'hidden');
				$(ImageSettings.preview_custom_brand).css('visibility', 'hidden');
			}	
			else if ($("input[name='custom-brand-radio']:checked").val() == 'under') //under
			{
				$(ImageSettings.custom_txt_colorpckr).css('visibility', 'visible');
				$(ImageSettings.custom_bg_colorpckr).css('visibility', 'visible');
				$(ImageSettings.preview_custom_brand).css('visibility', 'visible');
				$(ImageSettings.preview_custom_brand).css('color', $(ImageSettings.custom_txt_color).css('background-color'));
				$(ImageSettings.preview_custom_brand).css('background-color', $(ImageSettings.custom_bg_color).css('background-color'));
			}
			else //over
			{
				$(ImageSettings.custom_txt_colorpckr).css('visibility', 'visible');
				$(ImageSettings.custom_bg_colorpckr).css('visibility', 'hidden');
				$(ImageSettings.preview_custom_brand).css('visibility', 'visible');
				$(ImageSettings.preview_custom_brand).css('color', $(ImageSettings.custom_txt_color).css('background-color'));
				$(ImageSettings.preview_custom_brand).css('background-color', '#FFFFFF');
			}
		},

		handleFullSizeRadioToggle: function()
		{
			if ($("input[name='fullsize-brand-radio']:checked").val() == 'none') //no brand
			{
				$(ImageSettings.fullsize_txt_colorpckr).css('visibility', 'hidden');
				$(ImageSettings.fullsize_bg_colorpckr).css('visibility', 'hidden');
				$(ImageSettings.preview_fullsize_brand).css('visibility', 'hidden');
			}	
			else if ($("input[name='fullsize-brand-radio']:checked").val() == 'under') //under
			{
				$(ImageSettings.fullsize_txt_colorpckr).css('visibility', 'visible');
				$(ImageSettings.fullsize_bg_colorpckr).css('visibility', 'visible');
				$(ImageSettings.preview_fullsize_brand).css('visibility', 'visible');
				$(ImageSettings.preview_fullsize_brand).css('color', $(ImageSettings.fullsize_txt_color).css('background-color'));
				$(ImageSettings.preview_fullsize_brand).css('background-color', $(ImageSettings.fullsize_bg_color).css('background-color'));
			}
			else //over
			{
				$(ImageSettings.fullsize_txt_colorpckr).css('visibility', 'visible');
				$(ImageSettings.fullsize_bg_colorpckr).css('visibility', 'hidden');
				$(ImageSettings.preview_fullsize_brand).css('visibility', 'visible');
				$(ImageSettings.preview_fullsize_brand).css('color', $(ImageSettings.fullsize_txt_color).css('background-color'));
				$(ImageSettings.preview_fullsize_brand).css('background-color', '#FFFFFF');
			}
		},

		// Setup parse confirmation box
		setupParseConfirmation: function() {
			$(ImageSettings.parse_confirmation).dialog({
				autoOpen: false,
				bgiframe: true,
				resizable: false,
				height:180,
				modal: true,
				overlay: {
					backgroundColor: '#000',
					opacity: 0.5
				},
				buttons: {
					'Parse selected images': function() {
						ImageSettings.processSettings();
						$(this).dialog('close');
					},
					Cancel: function() {
						$(this).dialog('close');
					}
				}

			});
		},

//		/**
//		* Update the color preview div
//		* @param selector the selector of the div
//		* @param cssProp the css property of the div (tex or background)
//		*/
//		updatePreview: function(selector, cssProp) {
//		Helper.setUpColorPicker(selector, function(){
//		var color = $(selector + ' div').css('background-color');
//		$(selector).parents('tr:first').children('td:last').css(cssProp, color);
//		});
//		},


		/**
		 * Send a request to servlet and save those settings
		 * @param fn the callback function after settings are saved
		 */ 
		saveSettings: function(fn) {
			var info = $(ImageSettings.settings_form).serialize();
			info += "&custom-background=" + Helper.rgbToHex($(ImageSettings.custom_row).find('td:last').css('background-color'));
			info += "&custom-text=" + Helper.rgbToHex($(ImageSettings.custom_row).find('td:last').css('color'));
			info += "&fullsize-background=" + Helper.rgbToHex($(ImageSettings.fullsize_row).find('td:last').css('background-color'));
			info += "&fullsize-text=" + Helper.rgbToHex($(ImageSettings.fullsize_row).find('td:last').css('color'));
			Helper.showNotification(info);
			$.post("UpdateImageSettings.do", info, function(data){
				Helper.showNotification(data);
				if ($.isFunction(fn))
					fn();
			}, 'text');
		},

		// Process the settings after they're saved
		processSettings: function() {
			var option = $(ImageSettings.settings_form).serialize();
			ImageSettings.saveSettings(function(){
				$.post("ProcessImageSettings.do", option, function(data){
					Helper.showNotification(data);
				}, 'text');
			});
		}

};

/**
 * Data Management Script
 * ID: DMS
 */
var DataMan = {		
		delimiter : "#delimiter",
		file_import : "import-file",
		view_all : '#view-all',
		all_attributes_form : '#all-attributes',
		view_table : "#view-metadata",
		pager : '#pager',
		item_edit_modal : "#item-edit-modal",
		page_size: '#pagesize',
		page_index: '#page-index',
		page_total: "#page-total",
		technical_all: '#technical-all',
		admin_all: '#administrative-all',
		desc_all: '#descriptive-all',
		export_images: '#export-images',
		export_projname: '#export-projname',
		import_form : "#import-data-form",
		import_confirmation: '#import-data-confirmation',

		// Initialize all the buttons
		init: function() {
			Helper.clearLocks();
			$('#data-sub-nav').buttonset();

			$('label[for="data-sub-nav-import"]').click(function(){
				DataNav.gotoImportData();
			});
			$('label[for="data-sub-nav-export"]').click(function(){
				DataNav.gotoExportData();
			});
			$('label[for="data-sub-nav-view"]').click(function(){
				DataNav.gotoViewData();
			}).trigger('click');

			$('label[for="data-sub-nav-controlled-vocab"]').click(function(){
				DataNav.gotoEditControlledVocab();
			});
		},

		setupImportDataConfirmation: function() {
			$(DataMan.import_confirmation).dialog({
				autoOpen: false,
				bgiframe: true,
				resizable: false,
				height:180,
				modal: true,
				overlay: {
					backgroundColor: '#000',
					opacity: 0.5
				},
				buttons: {
					'Import Data': function() {
						$(this).dialog('close');
						Helper.showNotification("Importing metadata");
						$(DataMan.import_form).submit();
					},
					Cancel: function() {
						$(this).dialog('close');
					}
				}

			});
		},

		// Reload the export page if project is changed
		reloadExport: function() {
			$.post("ShowExport.do", null, function(results) {
				//$(DataMan.export_projname).html(results.projname);
				$(DataMan.export_images).attr('value','1-'+results.max);
			}, 'json');
		},

		// Show all the available attributes to choose
		showAllAttributes: function(admin_perm, desc_perm) {
			$.post('ShowAllAttributes.do', null, function(data){
				$(DataMan.all_attributes_form).empty();
				DataMan.populateViewAllAttributeTable($(DataMan.all_attributes_form), "technical", '(Read-only)', data.technical);
				DataMan.populateViewAllAttributeTable($(DataMan.all_attributes_form), "administrative", admin_perm,
						data.administrative);

				DataMan.populateViewAllAttributeTable($(DataMan.all_attributes_form), "descriptive", desc_perm, data.descriptive);
				$(DataMan.all_attributes_form).append("<br/><button>View Table</button>");

				$('.md_checkboxes :input').click(function () {
					selected = $('.md_checkboxes :input').filter(':checked').length;
					if (selected > 3){
						$('.md_checkboxes :input:not(:checked)').each(function () {
							$(this).attr('disabled', true);
						});
					}
					else
					{
						$('.md_checkboxes :input:not:(:checked)').each(function(){
							$(this).attr('disabled', false);
						});
						//$(this).attr('checked', !($(this).attr('checked')));
					}
				});

				$(DataMan.all_attributes_form+" > button:last").button().unbind('click').click(function(){
					DataMan.getSelectedAttributes();
					return false;
				});
				//$(DataMan.all_attributes_form+' table').each(function(i) {
				//	DataMan.bindSelectAll($(this));
				//});					
			}, 'json');
		},

		/**
		 * Populate the View Metadata table
		 * @param $form: the metadata form wrapped in jQuery
		 * @header technical, administrative or descriptive
		 * @data attributes from the servlet
		 */
		populateViewAllAttributeTable: function($form, header, permission, data) {
			$form.append("<fieldset style='width: 90%;'><legend><b>Select "+header+" settings</b></legend>" +
					"<table width='100%' class='md_checkboxes'>" +
					"<thead>" +
					"<tr align='left'>" +
					"<th style='width:33%'>"+header+" metadata <font style='font-weight: normal'>"+permission+"</font></th>" +
					"<th style='width:33%'></th>" +
					"<th style='width:33%'></th>" +
					"</tr>" +
					"</thead>" +
					"<tbody></tbody>" +
					"</table>" +
			"</fieldset><br/>");
			$tbody = $form.find("table:last > tbody");
			for (var i in data) {
				if ( i%3 == 0 )
					$tbody.append("<tr align='left'></tr>");
				var attr = data[i].element;
				var label = data[i].label; 
				if (label != "")
					attr += "." + label;
				var $addedTR = $tbody.find('tr:last');
				var newRow = "<td><input id='attr-"+attr+"' class='md_checkbox' type='checkbox' name='"+header+"' value='"+data[i].element+"-"+label+"' />"+attr+"</td>";
				$addedTR.append(newRow);				
			}
			if (data.length < 3)
				$tbody.find('tr:last').append("<td></td>");
			//$tbody.find('input:checkbox').button();
		},

		// Bind the select all checkbox
		/*
		bindSelectAll: function($table) {
			$table.find('input:checkbox').each(function(index){
				if (index == 0) 
					$(this).bind('click', function(ev){
						if (!ev.target.checked)
							$table.find('input:checkbox:not(:first)').removeAttr('checked');
						else
							$table.find('input:checkbox:not(:first)').attr('checked', 'true');
					});
				else
					$(this).bind('click', function(ev){
						if (!ev.target.checked)
							$table.find('input:checkbox:first').removeAttr('checked');
					});					
			});	
		},
		 */

		// Retrieve the selected attributes and send them to the servlet
		getSelectedAttributes: function() {
			var info = $(DataMan.all_attributes_form).serialize();
			Helper.showNotification("Retrieving metadata");
			$.post('ViewMetadata.do', info, function(data){
				DataMan.buildMetadataViewTable(data);
				var oTable;

				$(DataMan.view_table+' tbody tr > td:nth-child(1)').css('text-decoration', 'underline');

				//Setup in-place editable (plugin jEditable for jQuery)
				$(DataMan.view_table+' tbody tr > td:not(".uneditable")').editable('UpdateMetadata.do', {
					type: 'textarea',
					rows: 5,
					name: 'content',
					itemNumber: 'itemNumber',
					content: 'content',
					attribute: 'attribute',
					submitdata: function(value, settings) {
						var colNum = $(this).parent('tr').children('td').index(this);
						var index = $(this).parent('tr').attr('item');
						var attr = $(this).attr('attribute');
						return {
							itemNumber: index,
							attribute: attr
						};
					},
					indicator: "Saving...",
					cancel: "Cancel",
					submit: "Ok",
					/**
					 * callback function after request is sent
					 * @param sValue value from the servlet response
					 * @param y 
					 */
					callback: function( sValue, y ) { 
						var aPos = oTable.fnGetPosition( this );
						oTable.fnUpdate( sValue, aPos[0], aPos[1] );

					}
				});

				// Set up data table (dataTable plugin for jQuery)
				oTable = $(DataMan.view_table).dataTable({
					"bJQueryUI": true, 
					"sPaginationType": "full_numbers",
					"iDisplayLength": 50,
					"sDom": '<"top"flip<"clear">>rt<"bottom"p<"clear">>',
					"fnDrawCallback": function() {
						$(DataMan.view_table+' tbody > tr').each(function(){
							$(this).find('td:first').unbind('click').bind('click', function(ev){
								DataMan.editMetadataModal($(ev.target).parent('tr').attr('item'));
							});
						});
					}
				});

				$('.dataTables_length select').empty();
				$('.dataTables_length select').append('<option value="50" selected>50</option><option value="100">100</option><option value="150">150</option><option value="200">200</option>');
				$('.dataTables_info').css('padding-top', '5px');
				$('.dataTables_paginate').css('padding-top', '5px');
				$(DataMan.view_table).width('100%');
				$(DataMan.view_table+' thead > tr > th').each(function(){
					$(this).children('span').css('float', 'left');			//fix icon on headers
					$(this).css('padding', '5px');
					if ($(this).height() >= $(this).children('span').height())
						$(this).width(function(index, width){
							return width + $(this).children('span').width() + 25;
						});
				});

				// Initiate the edit metadata in modal dialog
				DataMan.initiateMetadataModal();

//				// Only show the dialog when the 1st cell (filename) in each row is clicked
//				$(DataMan.view_table+' tbody > tr').each(function(){
//				$(this).find('td:first').unbind('click').bind('click', function(ev){
//				DataMan.editMetadataModal($(ev.target).parent('tr').attr('item'));
//				});
//				});

				Helper.hideStatus();
			}, 'json');
		},

		/**
		 * Build the view metadata table
		 * @param data data from servlet
		 */
		buildMetadataViewTable: function(data) {

			// Create the table
			$(DataMan.all_attributes_form).siblings('div').empty();
			$(DataMan.all_attributes_form).siblings('div').append('<table id="view-metadata"></table>');
			$(DataMan.view_table).hide();
			$(DataMan.view_table).append('<thead><tr></tr></thead>');
			for (var i in data.headers) {
				var header = data.headers[i];
				$(DataMan.view_table+' thead tr').append('<th>'+ header.name +'</th>');
			}
			$(DataMan.view_table).append('<tbody></tbody>');
			var body = $(DataMan.view_table+ ' tbody'); 
			for (var i in data.items) {
				var item = data.items[i];
				var itemNumber = Number(i)+1;
				body.append('<tr item="'+itemNumber+'"></tr>');
				var newRow = body.children('tr:last');
				for (var j in item) {
					var header = data.headers[j];
					newRow.append('<td attribute="'+header.name+'">'+item[j]+'</td>');
					var cell = newRow.children('td:last');
					if (j == 0) {
						cell.css('cursor', 'pointer');
					}
					if (!header.editable) {
						cell.addClass('uneditable');
						if(j!=0)
						{
							cell.css('color', '#707070');
						}
					}
				}
				newRow.css('border', '0.5px solid');
			}
			$(DataMan.view_table).show();
		},

		/**
		 * Navigate to a new item in the edit item metadata view
		 * @param direction the direction to navigate (next, previous, first, last or a number)
		 */
		navigate: function(direction) {
			var numPerPage = $(DataMan.page_size+' option:selected').val();
			var currentPage = $(DataMan.page_index).val() === undefined ? 1 : parseInt($(DataMan.page_index).val());
			$(DataMan.page_total).attr('value', TableHandler.getTotalPages(DataMan.view_table, numPerPage));
			var totalPage = $(DataMan.page_total).val();
			var dest = currentPage;
			if (direction == "next")
				dest = currentPage + 1 > totalPage ? totalPage : currentPage + 1;
				else if (direction == "previous")
					dest = currentPage - 1 < 1 ? 1 : currentPage - 1;
				else if (direction == "first")
					dest = 1;
				else if (direction == "end")
					dest = totalPage;
				else if (direction === undefined)
					dest = currentPage > totalPage ? totalPage : currentPage < 1 ? 1 : currentPage;
					$(DataMan.page_index).attr('value', dest);
		},

		// Initiate the edit item metadata modal
		initiateMetadataModal: function() {
			$(DataMan.item_edit_modal).load("metadata/item_metadata.html", null, function(){
				$(Pages.current_page).html('view_metadata');
				$(DataMan.item_edit_modal).find('.metadata-nav').hide();
			});
			$(DataMan.item_edit_modal).dialog('destroy');
			$(DataMan.item_edit_modal).dialog({
				modal: true,
				resizable: true,
				height: 600,
				width: 1030,
				autoOpen: false,
				draggable: false,
				close: function(ev, ui) {
					var currentProj = Helper.getWorkingProject();
					var oldProj = Helper.trim($(ProjectMan.old_project).text());
					if (oldProj != "" && oldProj != currentProj) {
						ProjectMan.changeMainProject(oldProj, function(){
							$(ProjectMan.old_project).html("");
						});
					}
				}
			});
		},

		/**
		 * Call up the edit item metadata dialog
		 * @param index item number to navigate to
		 */
		editMetadataModal: function(index) {
			$(MetadataMan.item_index).attr('value', index);
			MetadataMan.navigate();
			$(DataMan.item_edit_modal).dialog('open');
		}

};

/**
 * Metadata Management Script
 * ID: MMS
 */
var MetadataMan = {

		page_id : 'metadata_management',
		metadata_navigation : ".metadata-navigation",
		metadata_holder : "#metadata-holder",
		admin_metadata_form : "#administrative-metadata",
		desc_metadata_form : "#descriptive-metadata",
		item_total : "#item-total",
		item_index : "#item-index",
		some_records : ".apply-to-some",
		content_holder : '#item-content',
		image_holder : '#image-holder',
		tech_metadata : '#tech-metadata',
		admin_metadata : '#admin-metadata',
		desc_metadata : '#desc-metadata',
		image_display : '.image-display',
		item_content: '#item-content',
		image_zoom: '#image-zoom',
		metadata_update_div: '.metadata-update',
		navigation: '.metadata-nav',
		vocab_cache: {},
		active_subtab: 0,

		tags: {
			'controlled':'<b>Controlled:</b> select from a list of approved terms.', 
			'multiple': '<b>Multiple:</b> more than one approved term may be selected.',
			'additions': '<b>Additions:</b> new terms will be added to the approved list.',
			'alphabetical': '<b>Alphabetical:</b> terms will be automatically sorted after update.',
			'searchdate' : '<b>Search Date:</b> Machine readable date (without text): YYYY, YYYY-MM, YYYY-MM-DD, or YYYY-YYYY.',
			'displaydate' : '<b>Display Date:</b> Human readable date (with text):<br/>'+
			'Known year-month-day:  YYYY-MM-DD<br/>'+
			'Known year-month: YYYY-MM<br/>'+
			'Known year: YYYY<br/>'+
			'One year of another: YYYY or YYYY<br/>'+
			'Circa year-month: circa YYYY-MM<br/>'+
			'Decade certain: YYYYs<br/>'+
			'Before at time period: before YYYY<br/>'+
			'After a time period: after YYYY '
		},



		// Manually parse a project
		processProjects: function() {
			$.post('ProcessProject.do', null, function(data){
				Helper.showNotification(data);
			}, 'text');
		},

		/**
		 * Initialize the edit metadata view
		 * @param modal init in modal mode or not
		 */ 
		init: function(modal) {
			MetadataMan.active_subtab = 0;
			if (modal === undefined)
				$(Pages.current_page).html(MetadataMan.page_id);

			$(MetadataMan.navigation + ' .item-jump').unbind('submit').submit(function() {
				MetadataMan.navigate(); 
				return false;
			});

			$(MetadataMan.metadata_holder).tabs('destroy');
			$(MetadataMan.metadata_holder).tabs({
				cache: false,
				select: function(event, ui) {
					if (ui.index == 0)
					{
						MetadataMan.active_subtab = 0;
						MetadataMan.showAdminDescMetadata('descriptive');
					}
					else if (ui.index == 1)
					{
						MetadataMan.active_subtab = 1;
						MetadataMan.showAdminDescMetadata('administrative');
					}
					else if (ui.index == 2)
					{
						MetadataMan.active_subtab = 2;
						MetadataMan.showTechMetadata();
					}
					$liParent = $(ui.tab).parents('li:first');
					$liParent.css('background-color', '#dddddd');
					$liParent.siblings('li').css('background-color', '#f3f3f3');
				}
			});
			$(MetadataMan.metadata_holder).tabs('select', 0);

			// Find out whether navigation to a specific item is requested
			var index = $(Search.item_to_go).html(); 
			if (index != '' && index !== undefined) {
				MetadataMan.showImageMetadata('', index, index);
				MetadataMan.showAdminDescMetadata('descriptive', '', index, index);
			}
			else {
				MetadataMan.showImageMetadata('first');
				MetadataMan.showAdminDescMetadata('descriptive','first');
			}
			$(Search.item_to_go).empty();
		},


		reload: function() {
			MetadataMan.showImageMetadata('first');
			current = $(MetadataMan.metadata_holder).tabs('option', 'selected');
			if (current == 2)
				MetadataMan.showTechMetadata();
			else if (current == 1)
				MetadataMan.showAdminDescMetadata('administrative', '', 1, 1);
			else if (current == 0)
				MetadataMan.showAdminDescMetadata('descriptive', '', 1, 1);

		},

		// Inititate the resizable text
		makeResizableText: function() {
			$('.bigtext').resizable({
				maxWidth: 303,
				minWidth: 302,
				minHeight: 70,
				handles: "s",
				resize: function(event, ui) { 
					$(this).children('textarea').css('height', $(this).height() - 6 + 'px');
				}
			});
		},

		/**
		 * Show image metadata including the image and filename. All parameters are
		 * optional, if they're not given, the function will try to extract them
		 * from the parent form.
		 * @param direction the navigation direction (back/next/first/last or nothing)
		 * @param currentItem the current item number
		 * @param itemIndex the item number to navigate to
		 */
		showImageMetadata: function(direction, currentItem, itemIndex) {
			$(window).unload(function(){ 
				$.ajax({
					type: 'POST',
					async: false,
					url: 'ClearLocks.do'
				});
			});


			var $holder = $(MetadataMan.image_holder); 
			if (direction === undefined)
				direction = '';
			var current = currentItem === undefined ? $(MetadataMan.image_display).attr('current') : currentItem;
			var index = itemIndex === undefined ? $(MetadataMan.item_index).val() : itemIndex;
			index = index === undefined ? 1 : index;
			current = current === undefined ? 1: current;
			$.post("ShowImageMetadata.do", {
				'current' : current,
				'item-index': index,
				'direction' : direction
			}, function(data) {
				$holder.html(data);

				MetadataMan.showAvailableSizes();

				//Fix CSS for sub-tab. HACK.
				if(MetadataMan.active_subtab == 0)
					$liparent = $holder.parents('tr:first').find("li:first");
				else if (MetadataMan.active_subtab == 1)
					$liparent = $holder.parents('tr:first').find("li:eq(1)");
				else if (MetadataMan.active_subtab == 2)
					$liparent = $holder.parents('tr:first').find("li:eq(2)");
				else
					$liparent = $holder.parents('tr:first').find("li:first");

				$liparent.css('background', '#dddddd none repeat scroll 0 0');
				$liparent.siblings('li').css('background',  '#f3f3f3 none repeat scroll 0 0');

				$holder.parents('tr:first').find('a').css('color', '#000000');

				//Fix img margin 
				imgHeight = $holder.find(MetadataMan.image_display).find('img').height();
				divHeight = $holder.find(MetadataMan.image_display).height();
				$holder.find(MetadataMan.image_display).find('img').css('margin-top', (divHeight - imgHeight)/2);

				var itemNumber = $(MetadataMan.image_display).attr('current');
				var projname = Helper.getWorkingProject();
				var imageZoomURL = MetadataMan.getImageZoomURL(projname, itemNumber);
				$(MetadataMan.image_display).siblings().find('a').attr('href', imageZoomURL);
				$(MetadataMan.image_display).siblings().find('a').html(imageZoomURL);
				$(MetadataMan.image_display).find('a').attr('href', imageZoomURL);

				//Setup the navigation
				$(MetadataMan.item_total).attr('value','');
				$(MetadataMan.item_index).attr('value','');

				$(MetadataMan.item_total).attr('value',$(MetadataMan.image_display).attr('size'));
				$(MetadataMan.item_index).attr('value',$(MetadataMan.image_display).attr('current'));

			}, 'text');
		},

		/**
		 * Generate the URL of the zoom image
		 * @param projname the project name
		 * @param itemNumber the item number
		 */
		getImageZoomURL: function(projname, itemNumber) {
			if (itemNumber < 10)
				itemNumber = '000'+itemNumber;
			else if (itemNumber < 100)
				itemNumber = '00'+itemNumber;
			else if (itemNumber < 1000)
				itemNumber = '0'+itemNumber;
			return 'http://' + window.location.hostname+'/zoom.html?item=' + projname + '-' + itemNumber;
		},

		// Show all available image sizes
		showAvailableSizes: function() {
			var current = $(MetadataMan.image_display).attr('current');
			current = current === undefined ? 0 : current;
			$.post('ShowAvailableSizes.do', {'current': current}, function(data){
				$(MetadataMan.content_holder+' table > tbody > tr:first > td:first').html(data);
			}, 'text');
		},

		/**
		 * Show admin/desc metadata. All parameters are optional, 
		 * if they're not given, the function will try to extract them
		 * from the parent form.
		 * @param direction the navigation direction (back/next/first/last or nothing)
		 * @param currentItem the current item number
		 * @param itemIndex the item number to navigate to
		 */
		showAdminDescMetadata: function(type, direction, currentItem, itemIndex) {
			if (direction === undefined)
				direction = '';
			var current = currentItem === undefined ? $(MetadataMan.image_display).attr('current') : currentItem;
			var index = itemIndex === undefined ? $(MetadataMan.item_index).val() : itemIndex;
			index = index === undefined ? 1 : index;
			current = current === undefined ? 1: current;
			var form = MetadataMan.admin_metadata_form;
			var displayDiv = MetadataMan.admin_metadata;
			$.post("ShowAdminDescMetadata.do",{		
				'type' : type,
				'current' : current,
				'item-index': index,
				'direction' : direction		
			}, function(data){
				if (type == 'descriptive') {
					displayDiv = MetadataMan.desc_metadata;
					form = MetadataMan.desc_metadata_form;
				}	
				$(displayDiv).html(data.data);
				if (data.error) {
					$('.error-message').html(data.message);
					//$('.error-message').addClass("ui-state-error");
				} else {
					$('.error-message').html("");
					//$('.error-message').removeClass("ui-state-error");
				}

				MetadataMan.setUpAutoComplete();


				MetadataMan.bindSelectAll($(form));
				MetadataMan.makeResizableText();

				$('.data-input').die('focus').live('focus', function(){
					if ($(this).hasClass('autocomplete')) {
						$(this).parents("ul:first").addClass('edited');
					} else
						$(this).addClass('edited');
				})
				.die('blur').live('blur', function() {
					if (! $(this).hasClass('autocomplete') )
						MetadataMan.updateAdminDescMetadata(type);
				});
				///.live('keypress', function(event){
				///	if (event.keyCode == "13") { event.preventDefault(); } 
				//});



				$('.qtip').remove();
				$('input.data-input[data-tags]').each(function(){
					var data_tags = $(this).attr('data-tags');
					if (data_tags != '') {
						var tags = data_tags.split(' ');
						//						if (tags.length == 0)
						//							return;
						var tip = '';
						for (var i in tags)
							tip += MetadataMan.tags[tags[i]]+'<br/>';
						//$(this).attr('title', tip);
						$(this).qtip({ 
							style: { 
								title: { 'font-size': 15 },
								width: 250
							},
							content: tip,
							position: {
								corner: {
									target: 'topMiddle',
									tooltip: 'bottomMiddle'
								}
							},
							show : { delay: 1000}
						});
					}
				});



				$(MetadataMan.metadata_update_div).find('input:button')
				.button()
				.unbind('click')
				.bind('click', function() { 
					MetadataMan.updateAdminDescMetadata(type); 
				});

				$(MetadataMan.metadata_update_div).find('select').unbind('change').bind('change', function() {
					if ($(this).find('option:selected').val().toLowerCase() == 'these') {
						var $input = $(this).siblings('input:text');
						$input.val(ImageSettings.default_instruction);

						$input.removeAttr('style');
						$input.click(function(){
							if ($input.val() == ImageSettings.default_instruction)
								$input.val("");
						});
						$(this).siblings('input:button').unbind('click')
						.bind('click', function() { 
							MetadataMan.updateSomeRecords(type, $(this).parents(MetadataMan.metadata_update_div)); 
						});
					}
					else {
						$(this).siblings('input:text').css('visibility', 'hidden');
						if ($(this).find('option:selected').attr('value').toLowerCase() == 'all') 
							$(this).siblings('input:button').unbind('click')
							.bind('click', function() { 
								MetadataMan.updateAllRecords(type); 
							});
						else
							$(this).siblings('input:button').unbind('click')
							.bind('click', function() { 
								MetadataMan.updateAdminDescMetadata(type); 
							});
					}
				});

				$('ul.controlled-collection').sortable({
					handle: '.dragHandle',
					items: 'li.ui-widget-content',
					stop: function(event, ui) { 
						$(event.target).addClass('edited');
					}
				});
			}, 'json');
		},

		/**
		 * Bind the select all checkbox on top
		 * @param $table the admin/desc table wrapped in jQuery
		 */
		bindSelectAll: function($table) {
			$table.find('input:checkbox').each(function(index){
				var $applyOptions = $(MetadataMan.metadata_update_div).find('select');
				if (index == 0) 
					$(this).bind('click', function(ev){
						if (!ev.target.checked) {
							$table.find('input:checkbox:not(:first)').removeAttr('checked');
							$applyOptions.css('visibility', 'hidden');
							$applyOptions.each(function() {
								$(this).siblings('input:text:last').css('visibility', 'hidden');
							});
						}
						else {
							$table.find('input:checkbox:not(:first)').attr('checked', 'true');
							$applyOptions.css('visibility', 'visible');
							$applyOptions.trigger('change');
						}
					});
				else
					$(this).bind('click', function(ev){
						var showApplyOptions = false;
						if (!ev.target.checked) {
							$table.find('input:checkbox:first').removeAttr('checked');
							$table.find('input:checkbox').each(function(){
								showApplyOptions = showApplyOptions || this.checked;
							});
						}
						else
							showApplyOptions = true;
						if (!showApplyOptions) {
							$applyOptions.css('visibility', 'hidden');
							$applyOptions.siblings('input:text').css('visibility', 'hidden');
							$applyOptions.siblings('input:button').unbind('click').bind('click', function() { 
								MetadataMan.updateAdminDescMetadata(MetadataMan.getType($(MetadataMan.metadata_holder).tabs('option', 'selected'))); 
							});
						}
						else {
							$applyOptions.css('visibility', 'visible');
							$applyOptions.trigger('change');
						}
					});					
			});	
		},

		/**
		 * Get the metadata type based on a metadata tab
		 * @param tab
		 */
		getType: function(tab) {
			if (tab == 0)
				return 'descriptive';
			return 'administrative';
		},

		/**
		 * Show tech metadata. All parameters are optional, 
		 * if they're not given, the function will try to extract them
		 * from the parent form.
		 * @param direction the navigation direction (back/next/first/last or nothing)
		 * @param currentItem the current item number
		 * @param itemIndex the item number to navigate to
		 */
		showTechMetadata: function(direction, currentItem, itemIndex) {
			if (direction === undefined)
				direction = '';
			var current = currentItem === undefined ? $(MetadataMan.image_display).attr('current') : currentItem;
			var index = itemIndex === undefined ? $(MetadataMan.item_index).val() : itemIndex;
			index = index === undefined ? 1 : index;
			current = current === undefined ? 1: current;
			$.post("ShowTechMetadata.do", {
				'current' : current,
				'item-index': index,
				'direction' : direction
			}, function(data){
				$(MetadataMan.tech_metadata).html(data);
			}, 'text');
		},

//		saveData: function() {
//		MetadataMan.updateAdminDescMetadata('administrative');
//		MetadataMan.updateAdminDescMetadata('descriptive');
//		},

		/**
		 * Update admin/desc metadata
		 * @param type the type of metadata to be updated
		 * @param options right now it's the records to be updated
		 * @paran fn the callback function after response is received
		 */
		updateAdminDescMetadata: function(type, options, fn) {
			var $edited_items = $('.edited'); 
			if ($edited_items.length == 0) {
				if ($.isFunction(fn))
					fn();
				return;
			}
			var form = MetadataMan.admin_metadata_form;
			if (type == 'descriptive')
				form = MetadataMan.desc_metadata_form;
			var current = $(form).attr('current');
			var info = "";
			var edited_ids = '';
			$edited_items.each(function(){
				if ($(this).hasClass('autocomplete-entry'))
					info += "&"+$(this).parents('ul:first').find('input').serialize();
				else if ($(this).is('ul'))
					info += "&"+$(this).find('input').serialize();
				else
					info += "&"+$(this).serialize();
				edited_ids += $(this).attr('name') + " ";
			});

			info += "&type="+Helper.urlencode(type);
			info += "&current="+current;
			info += '&edited-ids=' + edited_ids;
			info += '&records=' + options; 

			$.post("UpdateAdminDescMetadata.do", info, function(results) {
				var message = "";
				if(results!==null && results!==undefined)
					var failures = results.failure;
				if(failure !== undefined) 
				{
					for (var i = 0; i < failures.length; i++) {
						var failure = failures[i];
						message += "Cannot update "+failure.element+"."+failure.label+"<br/>";
						message += failure.message+"<br/>";
					}
				}
				Helper.showNotification(message);
				$edited_items.removeClass('edited');
				if ($.isFunction(fn))
					fn();
			}, 'json');
		},

		/**
		 * Navigate to a new item
		 * @param direction back/next/first/last
		 */
		navigate: function(direction) {
			var type = "administrative";
			var selected = $(MetadataMan.metadata_holder).tabs('option', 'selected');
			if (selected == 0)
				type = "descriptive";

			MetadataMan.updateAdminDescMetadata(type, '', function(){
				MetadataMan.showImageMetadata(direction);
				if (selected == 2)
					MetadataMan.showTechMetadata(direction);
				else
					MetadataMan.showAdminDescMetadata(type, direction);
				$(MetadataMan.metadata_holder).tabs('load', selected);
			});
		},

		// Click all the checkboxes for metadata
		checkAllMetadata: function() {
			var other_checkboxes = $('input:checkbox');
			var check = $('input:checkbox[name=select]').attr('checked');
			other_checkboxes.each(function() {
				if ($(this).attr('name')!='select')
					$(this).attr('checked', check);
			});
		},

		/**
		 * Get the selected items to batch update
		 * @param type the type of metadata to be updated
		 */
		getSelectedItems: function(type) {
			var form = "";

			if (type=="administrative")
				form = MetadataMan.admin_metadata_form;
			else if (type=="descriptive")
				form = MetadataMan.desc_metadata_form;

			var table = form+" table tbody";
			var rows = $(table+" tr:odd");
			var rowsSelected = "";
			rows.each(function(){
				var checkbox_column = $(this).find("input:checkbox");
				if (checkbox_column.attr("checked")) {
					var $text_input = $(this).find('.data-input');
					if ($text_input.hasClass('autocomplete-entry'))
						rowsSelected += "&"+$text_input.parents('div:first').find('input').serialize();
					else if ($text_input.is('div'))
						rowsSelected += "&"+$text_input.find('input').serialize();
					else
						rowsSelected += "&"+$text_input.serialize();
				}
			});
			return rowsSelected;
		},

		/**
		 * Update all records based on an/some attributes
		 * @param type the type of metadata to be updated
		 */
		updateAllRecords: function(type) {
			if (type === undefined) {
				var selected = $(MetadataMan.metadata_holder).tabs('option', 'selected');
				type = 'administrative';
				if (selected == 0)
					type = 'descriptive';
			}
			var info = MetadataMan.getSelectedItems(type);
			info += "&type="+Helper.urlencode(type)+"&project-name="+Helper.urlencode($('.current-project').html());
			info += "&records=all";
			//Helper.showNotification(info);
			$.post("UpdateMultipleMetadata.do", info, function(data){
				Helper.showNotification(data.message);
			}, 'json');
		},

		/**
		 * Update the selected attributes to some items
		 * @param type the type of metadata to be updated
		 * @param $wrapper the div wrapper for controlled vocab fields, optional
		 */ 
		updateSomeRecords: function(type, $wrapper) {
			if (type === undefined) {
				var selected = $(MetadataMan.metadata_holder).tabs('option', 'selected');
				type = 'administrative';
				if (selected == 0)
					type = 'descriptive';
			}
			var info = MetadataMan.getSelectedItems(type);
			info += "&type="+Helper.urlencode(type)+"&project-name="+Helper.urlencode($('.current-project').html());
			info += "&records="+Helper.urlencode($wrapper.find(MetadataMan.some_records).val());
			//Helper.showNotification(info);
			$.post("UpdateMultipleMetadata.do", info, function(data){
				Helper.showNotification(data);
			}, 'json');
		},

		/**
		 * Setup an autocomplete row
		 * @param $row the row to be set up, wrapped in jQuery
		 */
		setUpAutoCompleteRow: function($row) {

			var $handler = $row.find('.controlled_handler');
			//Hack for locked
			var disabled = ($('ul').is('.disabled'))||($('li').is('.disabled'));
			var addPointer = (disabled==true) ? 'default' : 'pointer';

			//Hack for alpha sort (disable drag)

			$handler.css('cursor', addPointer);
			$handler.unbind('click');

			$handler.click(function() {
				if(disabled==true)
				{
					return;
				}
				if ($(this).hasClass('ui-icon-plusthick')) {
					MetadataMan.addControlledRow($row);
					$row.parents("ul:first").sortable('refresh');
				}
				else {
					$row.parents('ul:first').addClass('edited');
					$row.remove();
					MetadataMan.updateAdminDescMetadata("administrative");
					MetadataMan.updateAdminDescMetadata("descriptive");
				}
			});

			if ($handler.hasClass('ui-icon-plusthick')) {
				$handler.siblings().css('visibility', 'hidden');
				return;
			}
			$row.css({ 
				height: '20px', 
				'overflow-y': 'visible',
				'padding-top': '4px'
			});
			$row.addClass('ui-widget-content');

			var $dragHandle = $row.find('.dragHandle');
			var $btn = $row.find('.show-all');
			var $input = $row.find('.autocomplete');
			var movePointer = (disabled==true)? 'default' : 'move';

			var cache = {};
			var vocab = $input.attr('vocab');
			var alpha = $handler.hasClass('alpha');
			if(alpha==false) {
				$dragHandle.addClass('ui-icon ui-icon-arrowthick-2-n-s')
				.css({
					"valign" : 'middle',
					float: 'left',
					cursor: movePointer,
				});
			}
			//else {
			//	$dragHandle.remove();
			//}

			$input.autocomplete({
				source: function(request, response) {
//					if (MetadataMan.vocab_cache[vocab].length < 50)
//					response(MetadataMan.vocab_cache[vocab]);
					if (cache.term == request.term && cache.content) {
						response(cache.content);
					}
					if (new RegExp(cache.term).test(request.term) && cache.content && cache.content.length < 13) {
						var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
						response($.grep(cache.content, function(value) {
							return matcher.test(value.value);
						}));
					}
					$.ajax({
						url: 'ShowVocab.do?vocab-name='+vocab,
						dataType: "json",
						data: request,
						success: function(data) {
							cache.term = request.term;
							cache.content = data;
							response(data);
						}
					});
				},
				// Remove error if the user selected a vocab from the list.
				// HACK! 
				select: function(event, ui) {
					$row.removeClass("ui-state-error");
				},
				change: function(event, ui) {
					// provide must match checking if what is in the input
					// is in the list of results. HACK!
					var source = $(this).val();
					var found = null;
					var autoText = source;
					var regexpSource = new RegExp(source, 'i');
					$('.ui-autocomplete li').each(function(){
						if (found != null)
							return;
						autoText = $(this).text(); 
						found = autoText.match(regexpSource);
						if (found != null && (found.length != 1 || found[0] != autoText))
							found = null;
					}); 
					var isAdd = $(this).attr('data-tags').search('additions');
					if(found == null) {
						if (isAdd < 0) {
							$(this).val("SELECT A TERM FROM THE LIST");
							$row.addClass("ui-state-error");
						}
						else
							$row.removeClass("ui-state-error");
					} else {
						$(this).val(autoText);
						$row.removeClass("ui-state-error");
					}

				},
				minLength: 0,
				delay: 1000
			})
			.attr('size', 65)
			.css( {
				float : 'left',
				border: 'none',
				'background': 'none', 
				'height' : '14px'
			});

			var autocompletePointer = (disabled==true) ? 'default' : 'pointer';
			$btn.addClass("ui-button-icon-primary ui-icon ui-icon-triangle-1-s")
			.css({
				float: 'right',
				cursor: autocompletePointer,
				"valign": 'middle'
			});

			$btn.click(function() {
				if(disabled==true)
					return;
				else
				{		// close if already visible
					if ($input.autocomplete("widget").is(":visible")) 
					{
						$input.autocomplete("close");
						return false;
					}
					// pass empty string as value to search for, displaying all results
					$input.autocomplete("search", "");
					$input.focus();
					return false;
				}
			});
		},

		// Setup the auto complete field
		setUpAutoComplete: function() {
			$('.autocomplete-entry').each(function(){
				MetadataMan.setUpAutoCompleteRow($(this));
			});


		},

		/**
		 * Add a new input field to the controlled vocab attribute
		 * $lastRow the last row in the attribute block (seed row used for cloning)
		 */
		addControlledRow: function($lastRow) {
			var newRow = $lastRow.clone();
			$lastRow.find('.controlled_handler').removeClass('ui-icon-plusthick').addClass('ui-icon-closethick');
			$lastRow.find('.controlled_handler').siblings().css('visibility', 'visible');

			$lastRow.parents('ul:first').append(newRow);
			MetadataMan.setUpAutoCompleteRow($lastRow);
			MetadataMan.setUpAutoCompleteRow(newRow);			
		}


};

/**
 * Controlled Vocab Management Script
 * ID: CVMS
 */
var ControlledVocab = {

		vocab_create_form : "#create-vocab-form",
		vocab_edit_form : "#vocab-management",

		vocab_choice_modal : "#vocab-choice-modal",
		vocab_choose_modal : "#choose-vocab",
		vocab_form_modal : "#vocab-modal",
		vocab_manage_modal : "#manage-vocab-modal",
		vocab_create_button: "#upload",


		// Setup the controlled vocab interface
		setupVocab: function() {
			var $input = $('#vocab-name');
			var cache = {};
			$input.autocomplete({
				source: function(request, response) {
					if (cache.term == request.term && cache.content) {
						response(cache.content);
					}
					if (new RegExp(cache.term).test(request.term) && cache.content && cache.content.length < 13) {
						var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
						response($.grep(cache.content, function(value) {
							return matcher.test(value.value);
						}));
					}
					$.ajax({
						url: "ShowVocabList.do",
						dataType: "json",
						data: request,
						success: function(data) {
							cache.term = request.term;
							cache.content = data;
							response(data);
						}
					});
				},

				select: function(ev, ui) {
					ControlledVocab.showVocab(ui.item.value);
				},
				//change: function(ev, ui) {
				//	ControlledVocab.showVocab(ui.item);
				//},
				minLength: 0,
				delay: 1000,

			})
			.attr('size', 45)
			.css('float', 'left');

			var $btn = $input.siblings('button'); 
			$btn.button({
				icons: {
					primary: "ui-icon-triangle-1-s"
				},
				text: false
			})
			.removeClass("ui-corner-all")
			.addClass("ui-button-icon")
			.css({
				height: "17px",
				width: "18px",
				float: 'left'
			})
			.unbind('click')
			.click(function() {
				// close if already visible
				if ($input.autocomplete("widget").is(":visible")) {
					$input.autocomplete("close");
					return false;
				}
				// pass empty string as value to search for, displaying all results
				$input.autocomplete("search", "");
				$input.focus();
				return false;
			});
		},

		/**
		 * Show the selected words in the vocab
		 * @param name the name of the controlled vocab
		 */ 
		showVocab: function(name) {
			var vocab_name = name;
			if (vocab_name == "" || vocab_name === undefined)
				vocab_name = $(ControlledVocab.vocab_edit_form+' #vocab-name').val();
			$.post('ShowVocab.do',{'vocab-name':vocab_name}, function(data){
				var output = "";
				for (var i in data) {
					output += data[i] + "\n";
				}
				$(ControlledVocab.vocab_edit_form+' textarea').val(output);
			}, 'json');
		},

		// Show the list of controlled vocabs
		showVocabList: function() {
			$.post('ShowVocabList.do',null,function(data){
				$(ControlledVocab.vocab_edit_form+' select').html(data);
				var name = $(ControlledVocab.vocab_create_form+' input[type="text"]').val();
				$(ControlledVocab.vocab_edit_form+' select option').each(function(){
					$(this).removeAttr('selected');
					if ($(this).val() == name)
						$(this).attr('selected','true');
				});
				ControlledVocab.showVocab();
			}, 'text');
		},

		/**
		 * Update a selected controlled vocab
		 * This method is currently used as a wrapper
		 * to submit the controlled vocab form
		 * @param fn callback function
		 */ 
		updateVocab: function(fn) {
			$(ControlledVocab.vocab_edit_form).submit();
			$('#upload').val("");
			setTimeout("ControlledVocab.showVocab()",500);

		},

		/**
		 * Remove a controlled vocab
		 * Vocab name is extracted from the form.
		 * This method can easily be extended to incorporate
		 * individual vocab name
		 */ 
		removeVocab: function() {
			var vocab_name = $('#vocab-name').val();
			$.post('RemoveVocab.do',{'vocab-name':vocab_name},function(data){
				Helper.showNotification(data);
				$('#vocab-name').attr('value', '');
			}, 'text');
			$(ControlledVocab.vocab_edit_form+' textarea').val("");
			$('#upload').val("");
		},

		/**
		 * Export the controlled vocab by simply
		 * redirecting to the corresponding servlet
		 */
		exportVocab: function() {
			var vocab_name = $('#vocab-name').val();
			window.open('ExportVocab.do?vocab-name='+vocab_name);
		},

		/**
		 * Handle AJAX upload form
		 */
		handleVocabUploadForm: function() {
			Helper.showNotification("File uploaded"); 
			setTimeout("ControlledVocab.showVocabList()",500);
		},

		/************************************
		 *    Controlled Vocab Modal Box   *
		 ************************************/

		/**
		 * Show the vocab page in the modal form
		 * @param button the button in the controlled vocab
		 * modal dialog box in edit project
		 */
		showVocabModal: function(button) {
			var row = $(button).parents('tr:first');
			var element = $(row).find('select').val();
			var label = $(row).find('input[type="text"]').val();
			var id = $(row).find('input:hidden[name="id"]').val();
			ControlledVocab.openVocabModal(button, element, label, id);
			$(ControlledVocab.vocab_choose_modal).dialog('open');
		},

		// Show the list of vocabs in the modal select box
		showVocabListModal: function() {
			$.post('ShowVocabList.do',null,function(data){
				$(ControlledVocab.vocab_choice_modal).html(data);
			}, 'text');

		},


		/**
		 * Set the vocab associated to a button but not yet update the database entry
		 * @param checkbox the associated controlled checkbox in edit project page
		 * @param element element of the attribute
		 * @param label label of the attribute
		 * @param fn callback function
		 */
		chooseVocab: function(checkbox, element, label, fn) {
			var vocab = $('#vocab-name').val();
			$(ControlledVocab.vocab_edit_form).submit();
			$(checkbox).attr('title',vocab);
			$(ControlledVocab.vocab_edit_form+' input:text').attr('value','');
			$(ControlledVocab.vocab_edit_form+' textarea').attr('value','');
			$(ControlledVocab.vocab_edit_form+' input:file').attr('value','');
			if ($.isFunction(fn))
				fn();

		},

		/**
		 * Actual update of the vocab associated to a button when loading admin/desc table
		 * @param checkbox the controlled vocab checkbox in edit project 
		 */
		setVocab: function(checkbox) {
			var info = '&id='+$(checkbox).parents('tr:first').find('input:hidden').val();
			var vocab = $(checkbox).attr('title');
			info += '&vocab='+ vocab;
			$.post("SetVocab.do", info ,function(data){
				Helper.showNotification(data);
			}, 'text');
		},

		// Setup the modal form display script
		initiateVocabModal: function() {
			$(ControlledVocab.vocab_choose_modal).dialog('destroy');
			$(ControlledVocab.vocab_choose_modal).dialog({
				modal: true,
				autoOpen: false,
				height: 560,
				width: 450, 
				draggable: false
			});
		},

		/**
		 * Open the controlled vocab modal box in edit project
		 * @param checkbox the controlled vocab checkbox of a certain attribute
		 * @param element element of an attribute
		 * @param label label of an attribute
		 * @param id id of the attribute
		 */
		openVocabModal: function(checkbox, element, label, id) {
			var modal_title = "Define controlled vocabulary for "+element+"."+label;
			var $assigned = $(ControlledVocab.vocab_choose_modal).find('input[name="assigned-field"]');
			$assigned.attr('value', id);

			$(ControlledVocab.vocab_choose_modal).dialog('option', 'buttons', {
				Cancel: function() {
					$(this).dialog('close');
				},
				"Update": function() {
					ControlledVocab.updateVocab();
					var vocab = $('#vocab-name').val();
					$(checkbox).attr('title',vocab);
					$(this).dialog('close');
				}
			});
			$(ControlledVocab.vocab_choose_modal).dialog('option', 'title', modal_title);
			$(ControlledVocab.vocab_choose_modal).unbind('dialogclose').bind('dialogclose', function(event, ui) {
				var title = $(checkbox).attr('title'); 
				if (title=="" || title==null) {
					$(checkbox).removeAttr('checked');
					ProjectMan.changeControlledBox(checkbox, 'disable');
				}
				else {
					$(checkbox).attr('checked','true');
					ProjectMan.changeControlledBox(checkbox, 'enable');
				}
				$(ControlledVocab.vocab_choose_modal).find('input:text, textarea, input:file').val('');
			});

		}

};

/**********************************************************************
 *                E N D  O F  M A N A G E M E N T					  *
 **********************************************************************/


/**
 * Search Script
 * ID: SS
 */
var Search = {

		search_form: '#search-form',
		search_options: '#search_options',
		search_field: '#search-field',
		search_results: '#search-results',
		item_to_go: '#current-item',

		// Initialize the search page
		init: function() {
			$(Pages.current_page).html('search');
			DataMan.initiateMetadataModal();

			$(window).unload(function(){ 
				$.ajax({
					type: 'POST',
					async: false,
					url: 'ClearLocks.do'
				});
			});

			$(Search.search_form).find('input:submit').button().unbind('click').click(function(e){
				Search.search(); 
				return false;
			});
		},

		/**
		 * Trigger the search form, including
		 * handling of the search result (populating table,
		 * wiring events)
		 */
		search: function() {
			Helper.showStatus("Searching...");
			var info = $(Search.search_form).serialize();
			var query = $(Search.search_field).val();
			$.post('SearchServlet.do', info, function(results){
				Helper.hideStatus();

				$(Search.search_results).empty();

				var index;
				if (results.size == 0)
					$(Search.search_results).html(results.data);
				else {
					$(Search.search_results).append("<table><thead></thead><tbody></tbody></table>");
					$(Search.search_results+" table thead").append("<tr></tr>");
					var $headerRow = $(Search.search_results+" table thead tr");
					$headerRow.append("<th>Image</th>");
					$headerRow.append("<th>Project</th>");
					$headerRow.append("<th>Item #</th>");
					$headerRow.append("<th>Metadata</th>");

					var body = $(Search.search_results+" table tbody");
					$(Search.search_results).css('display', 'block');
					for (var i = 0; i < results.data.indices.length; i++) {
						var row = "<tr>";
						row += "<td align='center' style='padding:15px'>";
						row += "<a target='_blank' href='"+MetadataMan.getImageZoomURL(results.data.projectNames[i], results.data.indices[i])+"'>";
						row += "<img src='" + results.data.thumb_urls[i] + "' style='border: thin solid black;'></img>";
						row += "</a></td>";
						row += "<td align='center' >" + results.data.projectNames[i] + "</td>";
						row += "<td align='center' style='cursor:pointer; text-decoration:underline;'><a>" + results.data.indices[i] + "</a></td>";
						row += '<td>';
						for (var j = 0; j < results.data.data[i].length; j++)
							row += results.data.data[i][j];
						row += '</td>';
						$(body).append(row);
						var $row = $(body).find('tr:last');
						$row.find('td:eq(2)').bind('click', function(ev) {
							var projname = $(ev.target).parents('tr:first').children('td:eq(1)').text();
							var num = $(ev.target).text();
							var current_projname = $(ProjectMan.project_list_main+' option:selected').text();
							if (projname != current_projname) {
								$(ProjectMan.old_project).html($(ProjectMan.project_list_main + ' option:selected').text());
								ProjectMan.changeMainProject(projname, function(){
									DataMan.editMetadataModal(num);
								});
							} else {
								$(ProjectMan.old_project).html("");
								DataMan.editMetadataModal(num);
							}


						});
						$row.css('border','0.5px solid');
					}


					var oTable = $(Search.search_results+" > table").dataTable({
						"bJQueryUI": true, 
						"sPaginationType": "full_numbers",
						"sDom": '<"top"flip<"clear">>rt<"bottom"p<"clear">>',
						"iDisplayLength": 50});
					$('.dataTables_length select').empty();
					$('.dataTables_length select').append('<option value="50" selected>50</option><option value="100">100</option><option value="150">150</option><option value="200">200</option>');
					$('.dataTables_info').css('padding-top', '5px');
					$('.dataTables_paginate').css('padding-top', '5px');
					$(Search.search_results+' table > thead > tr > th').each(function(){
						$(this).children('span').css('float', 'left');			//fix icon on headers
						$(this).css('padding', '5px');
						if ($(this).height() >= $(this).children('span').height())
							$(this).width(function(index, width){
								return width + $(this).children('span').width() + 10;
							});
					});
					$(Search.search_results+' table tr').each(function(){
						Helper.searchHighlight($(this).find('td:last'), query);
					});

				}
			}, 'json');
		},

		/**
		 * Navigate to a certain item in the fast item edit 
		 * modal popup
		 * @param projname the project to be navigated to
		 * @param itemNumber the item number
		 */
		goToItem: function(projname, itemNumber) {
			if (projname != $(ProjectMan.project_choice_main_page+' option:selected').text()) {
				Helper.choose($(ProjectMan.project_choice_main_page), projname);
				$(ProjectMan.project_list_main).trigger('change');
			}
			$(Search.item_to_go).html(itemNumber);
		}
};


/**********************************************************************
 *               N A V I G A T I O N  S C R I P T S					  *
 **********************************************************************/

/**
 * Page Navigation Script
 * Under development
 * ID: PNS 
 */
var PageNav = {

		context: '#context',
		pages: ['home', 'user management', 'project management', 'data management', 'item metadata', 'search'],

		/**
		 * Go to a certain page by manually
		 * triggering the jQuery UI tabs
		 */
		goToPage: function(name, fn) {
			var index = PageNav.pages.indexOf(name);
			$(PageNav.context).tabs('select', index);
			$('#sub-nav').find('a:eq(2)').click();
			if ($.isFunction(fn))
				fn();
		}

};

/**
 * Project Management Navigation Script
 * ID: PMNS
 */
var ProjectNav = {

		project_content : '#project-manipulation',
		project_direction : '#projectman-direction',
		project_info : '#project-info',
		project_admin_md : '#project-admin-md',
		project_custom_md : '#custom-md',
		project_desc_md : '#project-desc-md',
		action_indicator : '#current-page',
		project_name : '#projname',
		project_notes : '#projnotes',
		create_project: '#createproj',
		vocab_modal : '#choose-vocab',

		// Empty the content of the project page 
		emptyContent: function() {
			$(ProjectNav.project_content).empty();
			$(ProjectNav.project_info).empty();
			$(ProjectNav.project_admin_md).empty();
			$(ProjectNav.project_desc_md).empty();
			$(ProjectNav.project_custom_md).empty();
			$(ProjectMan.project_image_settings).empty();
		},

		gotoEditProject: function(fn) {
			$(Pages.current_page).html('edit_project');

			ProjectNav.emptyContent();
			$(ProjectNav.project_direction).css('display','none');
			$(ProjectNav.project_info).load('management/projectman/project_info.html', null, function(data){
				$(ProjectNav.project_name).attr('disabled','disabled');
				$(ProjectNav.project_name).val($('#current-editing-project').html());
				$(ProjectNav.project_notes).attr('disabled','disabled');		
				$('.optional-import').css('display','none');
				$(ProjectNav.action_indicator).html("edit project");
				ProjectMan.reloadProjectData();
				$(ProjectMan.project_template).css('display','none');

				$(ProjectMan.delete_attr_confirmation).dialog('destroy');
				ProjectMan.setupDeleteAttrConfirmation();
				ProjectMan.bindDeleteAttrConfirm();
			});

			$(ProjectNav.project_admin_md).load('management/projectman/project_admin_md.html '+ProjectMan.admin_md_form, null, 
					function(data){
				ProjectMan.showMD('administrative');
				ProjectMan.refreshEvents(ProjectMan.admin_md_table);
				TableHandler.checkMDTableAdd(ProjectMan.admin_md_table, ProjectMan.admin_md_indicator);
			});

			$(ProjectNav.project_desc_md).load('management/projectman/project_desc_md.html '+ProjectMan.desc_md_form, null, 
					function(data){
				ProjectMan.showMD('descriptive');
				ProjectMan.refreshEvents(ProjectMan.desc_md_table);
				TableHandler.checkMDTableAdd(ProjectMan.desc_md_table, ProjectMan.desc_md_indicator);
			});

			$(ControlledVocab.vocab_choose_modal).remove();
			$('body').append('<div id="choose-vocab" style="display:none" align="center"></div>');
			$(ControlledVocab.vocab_choose_modal).load('management/dataman/controlled_vocab.html', null, function(){
				ControlledVocab.setupVocab();
				$('#vocab-control-buttons').remove();
				ControlledVocab.initiateVocabModal();
			});


			ProjectMan.showImageSettings();
			if ($.isFunction(fn))
				fn();
		},

		gotoCreateProject: function() {
			$(Pages.current_page).html('create_project');
			ProjectNav.emptyContent();
			$(ProjectNav.project_direction).css('display','none');
			$(ProjectNav.project_content).load('management/projectman/project_create.html', null, function(data){
				$(ProjectNav.project_name).removeAttr('disabled');
				$(ProjectNav.project_notes).removeAttr('disabled');
				$(ProjectNav.action_indicator).html("create project");
				$('.optional-import').css('display','');
				$(ProjectMan.project_template+' select').html($(Home.project_select).html());
				$(ProjectMan.project_template+' input:checkbox').unbind('click').bind('click', function(e){
					if (!e.target.checked) {
						$(ProjectMan.project_template+' select').css('display', 'none');
						$('.optional-import').css('display','');
					} else {
						$(ProjectMan.project_template+' select').css('display', '');
						$('.optional-import').css('display','none');
					}
				});

				$(ProjectNav.project_name).change(function(event, ui) {
					// provide must match checking if what is in the input
					// is in the list of results. HACK!
					var source = $(ProjectNav.project_name).val();
					// Hack to validate project name.
					// 'invalid' tries to match illegal characters. If invalid==true, means there is an illegal character.
					var invalid = /[^a-zA-Z0-9-]/i.test(source); 
					if(invalid==true)
						{
							$(ProjectNav.project_name).val("ALPHANUMERIC-CHARACTERS-AND-HYPHENS-ONLY");
							$(ProjectNav.project_name).addClass("ui-state-error");
							$(ProjectNav.create_project).attr('disabled', 'true');
						}
					else
						{
							$(ProjectNav.project_name).removeClass("ui-state-error");
							$(ProjectNav.create_project).removeAttr('disabled');
						}
				});
				
				$(ProjectMan.project_template).find('input:submit').click(function(){
					Helper.showStatus("Creating project");
				});
				$('input:button, input:reset, input:submit').button();
			});
		},

		gotoDeleteProject: function() {
			$(Pages.current_page).html('delete_project');
			ProjectNav.emptyContent();
			$(ProjectNav.project_direction).css('display','none');

			$(ProjectNav.project_content).load('management/projectman/delete_project.html', null, function(){
				$(ProjectNav.action_indicator).html("delete project");
				$(ProjectNav.project_content+' input:button, input:reset, input:submit').button();
				$(ProjectMan.delete_confirmation).dialog('destroy');
				ProjectMan.setupDeleteProjConfirmation();
				$(ProjectMan.delete_form).find('input:button:last').button().bind('click', function(){
					$(ProjectMan.delete_confirmation).dialog('open');
				});
			});
		},

		gotoImageSettings: function() {
			$(Pages.current_page).html('image_settings');
			ProjectNav.emptyContent();
			$(ProjectNav.project_direction).css('display','none');
			$(ProjectNav.project_content).load('management/projectman/image_settings.html', null, function(data){
				$(ProjectNav.action_indicator).html("image settings");
				ImageSettings.loadSettings();
			});
		}

};

/**
 * Data Management Navigation Script
 * ID: DMNS
 */
var DataNav = {

		dataman_page : 'management/dataman/data_services.html', 
		data_manipulation : '#data-manipulation',
		dataman_project : '#dataman-project-area',
		direction : '#dataman-direction',
		export_form : '#export-data-form',
		view_all : '#view-all',
		all_attributes_form: '#all-attributes',
		current_page: '#current-page',

		// Go to import metadata page
		gotoImportData: function() {

			$(DataNav.direction).css('display','none');
			$(DataNav.data_manipulation).empty();
			$.post("GetPermission.do", null, function(result){
				var showImport = result.admin || (result.data.indexOf('import') != -1);
				if (showImport) {
					$(DataNav.data_manipulation).load(DataNav.dataman_page+' #import-data', function()
							{
						$(DataMan.import_confirmation).dialog('destroy');
						DataMan.setupImportDataConfirmation();
						$(DataMan.import_form).find('input:button:last').button().bind('click', function(){
							$(DataMan.import_confirmation).dialog('open')}); 
							}
					);}
				else {
					$(DataNav.data_manipulation).html("You don't have permission to import data");
				}
				$(Pages.current_page).html(Pages.import_page);
			}, 'json');
		},

		// Goto export metadata page
		gotoExportData: function() {
			$(DataNav.direction).css('display','none');
			$(DataNav.data_manipulation).empty();
			$.post("GetPermission.do", null, function(result){
				var showExport = result.admin || (result.data.indexOf('export') != -1);
				if (showExport) {
					$(DataNav.data_manipulation).load(DataNav.dataman_page+' #export-data', null, function(data){
						$(DataNav.export_form + " input:hidden").attr("value", Home.current_project);
						$submit_button = $('#export-data').find("input:submit:last");
						$submit_button.button();
						DataMan.reloadExport();
					});
				} else {
					$(DataNav.data_manipulation).html("You don't have permission to export data");
				}
				$(Pages.current_page).html(Pages.export_page);
			}, 'json');

		},

		gotoViewData: function() {
			$(DataNav.direction).css('display','none');
			$(DataNav.data_manipulation).empty();
			$.post("GetPermission.do", null, function(result){
				var showViewTable = result.admin || (result.table_edit=='allow');
				if(showViewTable)
				{
					admin_perm = ( result.admin? '(read/write)': 
						result.administrative== 'read'? '(read-only)' : 
							'read_write' ? '(read/write)' : 
					'none');

					desc_perm = ( result.admin ? '(read/Write)' : 
						result.descriptive=='read'? '(read-only)' : 
							'read_write' ? '(read/write)' : 
					'none');
					$(DataNav.data_manipulation).load(DataNav.dataman_page+' #view-all', null, function(data){						
						DataMan.showAllAttributes(admin_perm, desc_perm);
					});
				}
				else {
					$(DataNav.data_manipulation).html("You don't have permission to view metadata");
				}				
				$(Pages.current_page).html('view_metadata');
			}, 'json');
		},

		// Go to edit controlled vocab page
		gotoEditControlledVocab: function() {
			$(DataNav.direction).css('display','none');
			$(DataNav.data_manipulation).empty();
			$.post("GetPermission.do", null, function(result){
				var showVocab = result.admin || (result.controlled_vocab=='allow');
				if(showVocab)
				{
					$(DataNav.data_manipulation).load('management/dataman/controlled_vocab.html', null, function(data){
						ControlledVocab.setupVocab();
						$('#vocab-control-buttons').find('input:button, input:submit').button().click(function(e){
							if ($(e.target).attr('value') == 'Remove') {
								ControlledVocab.removeVocab();
							} else if ($(e.target).attr('value') == 'Export') {
								ControlledVocab.exportVocab();
							} else if ($(e.target).attr('value') == 'Update') {
								ControlledVocab.updateVocab();
							}
							return false;
						});
					});
				}
				else {
					$(DataNav.data_manipulation).html("You don't have permission to change controlled vocabulary");
				} 
				$(Pages.current_page).html('controlled_vocab');
			}, 'json');
		}
};

/**
 * User Management Navigation Script
 * ID: UMNS
 */
var UserNav = {
		user_content : '#user-manipulation',
		delete_user_div: '#delete-user',
		create_user_div: '#create-user',
		userman_page : 'management/userman/user_services.html',

		// Go to create user page
		gotoCreateUser: function() {
			$(Pages.current_page).html('create_user');
			$(UserNav.user_content).empty();
			$(UserNav.user_content).load(UserNav.userman_page + ' ' + UserNav.create_user_div, null, function(){
				$(UserNav.user_content+' input:button, input:reset').button();
				UserMan.checkLDAP();
			});

		},

		// Go to delete user page
		gotoDeleteUser: function() {
			$(Pages.current_page).html('delete_user');
			$(UserNav.user_content).empty();
			$(UserNav.user_content).load(UserNav.userman_page+' '+ UserNav.delete_user_div, null, function(data){
				UserMan.reloadUserList();
				$(UserNav.user_content+' input:button, input:reset').button();
				$(UserMan.delete_confirmation).dialog('destroy');
				UserMan.setupDeleteUserConfirmation();
				$(UserMan.delete_form).find('input:button:last').button().bind('click', function(){
					$(UserMan.delete_confirmation).dialog('open')});
			});
		},

		// Go to change user info page
		gotoChangeUserInfo: function() {
			$(Pages.current_page).html('change_user_info');
			$(UserNav.user_content).empty();
			$(UserNav.user_content).load(UserNav.userman_page+' '+UserMan.change_user_info, null, function(data){
				UserMan.reloadUserList(function() {
					UserMan.showPermissions();
					UserMan.getProjectPermissionList();
					$(UserNav.user_content+' input:button, input:reset').button();
					UserMan.checkLDAP();
				});
			});			
		}
};

/**********************************************************************
 *                E N D  O F  N A V I G A T I O N					  *
 **********************************************************************/


/**********************************************************************
 *               H E L P E R  S C R I P T S					  		  *
 **********************************************************************/

/**
 * Helper Script
 * ID: HelpS
 */
var Helper = {

		notification : '#global-notification',
		tempVar : null,

		// Simple database connection test
		dbTest: function() {
			$.post("DatabaseTest.do",null,function(data) {
				Helper.showNotification(data);
			}, 'text');
		},

		// Show the log
		showLog: function() {
			var type = $('.log-types').serialize();
			window.open('ShowLog.do?'+type);

		},

		// Flush the log
		flushLog: function() {
			var type = $('.log-types').serialize();
			$.post("FlushLog.do", type ,function(data) {
				Helper.showNotification(data);
			}, 'text');

		},

		/**
		 * URL encode a string
		 * @param inputString string to be URL encoded
		 */
		urlencode: function(inputString) {
			var encodedInputString = escape(inputString);
			encodedInputString = encodedInputString.replace("+", "%2B");
			encodedInputString = encodedInputString.replace("/", "%2F");
			return encodedInputString;
		},


		// Reload the list of dublin-core elements
		reloadElementList: function() {
			$.post('ShowElements.do', null, function(data){
				$('.element-list').html(data);
			}, 'text');
		},

		/**
		 * Check the input for numbers only
		 * @param input input to be checked
		 */
		checkNumbers: function(input) {
			$(input).live('keypress', function (e) {
				if(e.which != 8 && e.which != 0 && (e.which < 48 || e.which > 57) && e.which != 13)
				{
					//display error message
					Helper.showNotification('Numbers Only');
					return false;
				}
			}); 
		},

		/**
		 * Setup color picker for edit project
		 * @param selector the color select div
		 * @param cssProp this is for preview to dictate whether it's
		 * bg or fg
		 * 
		 */
		setUpColorPicker: function(selector, cssProp) {
			$(selector).ColorPicker({
				onShow: function (colpkr) {
					$(colpkr).fadeIn(500);
					return false;
				},
				onHide: function (colpkr) {
					$(colpkr).fadeOut(500);
					return false;
				},
				onChange: function (hsb, hex, rgb) {
					$(selector).children('div').css('background-color', '#' + hex);
					$(selector).parents('tr:first').children('td:last').css(cssProp, '#' + hex);
				}
			});
		},

		/**
		 * Convert an URL ti tinyurl
		 * This function is currently not in use
		 */
		getTinyURL: function(longURL, success) {
			var API = 'http://json-tinyurl.appspot.com/?url=',
			URL = API + encodeURIComponent(longURL) + '&callback=?';

			$.getJSON(URL, function(data){
				success && success(data.tinyurl);
			});
		},

		/**
		 * Credit to Trending.us: http://www.trending.us/2009/01/07/javascript-rgb-to-hexadecimal-color-converter/
		 * Convert RGB color value to HEX
		 * @param rgbval the rgb value
		 */
		rgbToHex: function(rgbval){
			var s = rgbval.match(/rgb\s*\x28((?:25[0-5])|(?:2[0-4]\d)|(?:[01]?\d?\d))\s*,\s*((?:25[0-5])|(?:2[0-4]\d)|(?:[01]?\d?\d))\s*,\s*((?:25[0-5])|(?:2[0-4]\d)|(?:[01]?\d?\d))\s*\x29/);

			if(s){ s=s.splice(1); }
			if(s && s.length==3){
				d='';
				for(i in s){
					e=parseInt(s[i],10).toString(16);
					e == "0" ? d+="00":d+=e;
				} return '#'+d;
			}else{ return rgbval; }
		},

		/**
		 * Show the notification (not fading out)
		 * @param text the status text to be displayed
		 */
		showStatus: function(text) {
			if (text == '')
				return;
			$(Helper.notification).html(text);
			$(Helper.notification).fadeIn(500);
		},

		/**
		 * Hide the notification (using fade out)
		 */
		hideStatus: function() {
			$(Helper.notification).fadeOut(500);
		},

		/**
		 * Show the notification with fade in and fade out in 2 seconds
		 * @param text the status text
		 */
		showNotification: function(text) {
			if (text == '')
				return;
			$(Helper.notification).html(text);
			$(Helper.notification).fadeIn(500).fadeTo(2000, 1).fadeOut(500);
		},

		/**
		 * Highlight search term in a page
		 * @param $content the page content wrapped in jQuery object
		 * @param term the search term
		 */
		searchHighlight : function($content, term) {
			if (term === undefined || term == '' )
				return;
			$content.each(function(){
				var highlighted = $(this).html().replace(new RegExp(term,'gi'), '<span class="ui-state-highlight">'+term+'</span>');
				$(this).html(highlighted);
			});
		},

		/**
		 * Trigger a select box to choose a certain value
		 * @param $select the select box wrapped in jQuery
		 * @param name the value to be chosen
		 */
		choose: function($select, name) {
			var options = $select.find('option');
			for (var i = 0; i < options.length; i++) {
				options[i].selected = false;
				if (options[i].value == name) {
					options[i].selected = true;
					return options[i];				
				}
			}
			return null;
		},

		/**
		 * Handle upload response for iFrame since AJAX upload is more complicated than
		 * a normal AJAX call
		 * @param target the iFrame selector
		 */
		handleUploadResponse: function(target) {
			var current = $(Home.project_select).val();
			if ($(Pages.current_page).text() != Pages.import_page)
				ProjectMan.reloadProjectList(current);
			$(target).contents().find('body').each(function(){
				if ($(this).html() != "") {
					var jsonExpression = "(" + $(this).find('pre').html() + ")";
					var result = eval(jsonExpression);
					var message = result.message+'<br/>';
					if (!result.success && result.fields != "" && result.fields !== undefined && result.fields !== null) {

						var messageArray = result.fields.split(',');
						for (var i = 0; i < messageArray.length; i++)
							if (messageArray[i] != '')
								message += messageArray[i] + ' ----> description.' + messageArray[i]+'<br/>';
						message += "Please navigate to Project Management > Edit Project to fix the fields highlighted in red";
					}



					if (result.projname !== undefined && result.projname !== null) {
						Helper.tempVar = result.projname;
						setTimeout("Helper.handleCreateProject()", 500);
					}

					$('#hidden-notice').html(message);					
					$('#hidden-notice').css('display','');

				}
			});
			Helper.hideStatus();
		},

		/**
		 * Handle create project AJAX upload separately since
		 * the user is navigated to edit project afterwards
		 */
		handleCreateProject: function() {
			var projname = Helper.tempVar;
			Helper.choose($(ProjectMan.project_list_main), projname);
			$(ProjectMan.project_list_main).trigger('change');
			$('label[for$="edit"]').trigger('click');
		},

		/**
		 * Wait a certain millis by setting timeout
		 * and call a dummy function after
		 * @param millis the number of milliseconds to wait
		 */
		wait: function(millis) {
			setTimeout('Helper.dummy()', millis);
		},

		/**
		 * Dummy function which does absolutely nothing
		 */
		dummy: function() {
			return 1;
		},

		/**
		 * Populate a select box with predefined selected value
		 * @param elementList the list of elements in the select box
		 * @param selectedElement the element to be pre-selected
		 */
		setupSelectBox: function(elementList, selectedElement) {
			var elementSelectBox = "";
			for (var i in elementList) {
				if (elementList[i] == selectedElement)
					elementSelectBox += "<option selected>"+elementList[i]+"</option>";
				else
					elementSelectBox += "<option>"+elementList[i]+"</option>";
			}
			return elementSelectBox;
		},

		/**
		 * Get the site session ID (currently not in use)
		 */
		getSiteSessionID: function() {
			return $.cookie('JSESSIONID');
		},

		setSSLCookie: function(value) { 
			$.cookie('requireSSL', value);
		},

		/**
		 * Invoke the print button in item metadata page
		 */
		print: function() {
			var current = $('form[id$="-metadata"]').attr('current');
			var projname = $(Home.project_select).val();
			window.open("PrintMetadata.do?projname="+projname+"&item-number="+current);
		},

		/**
		 * Get the currently working project
		 */
		getWorkingProject: function() {
			return $(Home.project_select).val();
		},

		/**
		 * Clean up the user's locks
		 */
		clearLocks: function() { 
			$.post("ClearLocks.do", null, function(data) {}, 'json'); 
		},

		/**
		 * Pop up alert box using jQuery dialog box theme
		 * @param title the title of the alert box
		 * @param message the message in the alert box
		 */
		alert: function(title, message) {
			$(Home.general_warning).html(message);
			$(Home.general_warning).attr('title',title);
			$(Home.general_warning).dialog('destroy');
			$(Home.general_warning).dialog({
				bgiframe: true,
				modal: true,
				buttons: {
					Ok: function() {
						$(this).dialog('close');
					}
				}
			});
		},

		/**
		 * Trim trailing whitespace off a string
		 * @param str string to be processed
		 */
		trim: function(str) {
			return str.replace(/^\s+|\s+$/g, '');
		}


};

//Table Handler Script: TS14
var TableHandler = {

		pager : '#pager',
		pagesize : '#pagesize',


		/**
		 * Initiate zebra table
		 * @param $table the table wrapped in jQuery
		 */
		altColor: function($table) {
			$table.find('tr:even').css('background-color','#CCCCCC');
			$table.find('tr:odd').css('background-color','#F3F3F3');
		},

		/**
		 * Add a row to the user access table
		 * @param table the attribute table selector
		 */
		addTableRow: function(table) {
			var lastRow = table + ' tr:last';
			var newRow = $(lastRow).clone();
			var radio = $(lastRow + ' input[type="radio"]');

			var projname = $('#project-choice-access option:selected').val();
			radio.each(function() {
				this.name = this.name+'-'+projname;
			});

			$(lastRow + ' td:first').html(projname);
			$(lastRow + ' td:last input').attr('value','Revoke');
			$(table).append(newRow);

		},

		/**
		 * Add a row to the metadata table
		 * @param table the attribute table selector
		 */
		addMDRow: function(table) {
			var body = table+" tbody";
			var foot = table+" tfoot";

			var rowNum = $(body+' tr').length;

			var newRow = $(foot + ' tr').clone();

			$(body).append(newRow);
			var lastRow = body + ' tr:last';
			$(lastRow+' td:last').html('<span class="ui-icon ui-icon-minusthick" name="delete-'+rowNum+'" style="cursor: pointer; margin:auto"/>');
			$(lastRow+' td:first').html('<span class="ui-icon ui-icon-arrowthick-2-n-s" style="cursor: move; margin: auto"/>');
			$(lastRow+' td:first').addClass('dragHandle');
			$(lastRow+' td').removeClass('last-attribute');
			rowNum = $(body+' tr').length;

			ProjectMan.refreshMDTable(table);
			$(foot+' tr td:last').html('<span class="ui-icon ui-icon-plusthick" style="cursor: pointer; margin:auto" name="add-'+rowNum+'"/>');
		},

		/**
		 * Update the row index in the table
		 * @param object a row in the attribute table
		 * @param rowNum the next row number to be updated
		 */
		updateIndex: function(object, rowNum) {
			var name = $(object).attr('name').split('-');
			var newName = name[0]+'-'+rowNum;
			$(object).attr('name',newName);
		},

		/**
		 * Delete a row from the metadata table
		 * @param button the minus sign cell
		 */
		deleteMDRow: function(button) {
			var row = $(button).parents('tr:first');
			$(row).remove();
		},

		/**
		 * Add multiple rows to the metadata table
		 * @param table the attribute table selector
		 * @param input the text field that indicates the number of
		 * rows to be added
		 */
		addMDRows: function(table, input) {
			var number = $(input).val();
			for (var i=0; i<number;i++)
				TableHandler.addMDRow(table);
			$(input).attr("value", "");
			return false;
		},

		// Add multiple rows to the admin metadata table
		addAdminMDRows: function() {
			TableHandler.addMDRows(ProjectMan.admin_md_table, ProjectMan.admin_md_indicator+' input:text');
		},

		// Add multiple rows to the desc metadata table
		addDescMDRows: function() {
			TableHandler.addMDRows(ProjectMan.desc_md_table, ProjectMan.desc_md_indicator+' input:text');
		},

		/**
		 * Make sure the "add multiple admin rows" field only accepts numbers
		 * @param table the attribute table selector
		 * @param indicator the total number of rows indicator
		 */
		checkMDTableAdd: function(table, indicator) {
			$(indicator+' input').live('keypress', function (e) {
				//if the letter is not digit then display error and don't type anything
				if(e.which!=8 && e.which!=0 && (e.which<48 || e.which>57) && e.which!=13)
				{
					//display error message
					Helper.showNotification('Numbers Only');
					return false;
				} 

				if (e.which==13) {
					TableHandler.addMDRows(table,this);
					return false;
				}
			});
		}
};

var Pages = {
		current_page : '#current-page',
		export_page : "exportProject",
		import_page : "importProject"
};

/*-------------------------------------------------------------------- 
 * JQuery Plugin: "EqualHeights"
 * by:	Scott Jehl, Todd Parker, Maggie Costello Wachs (http://www.filamentgroup.com)
 *
 * Copyright (c) 2008 Filament Group
 * Licensed under GPL (http://www.opensource.org/licenses/gpl-license.php)
 *
 * Description: Compares the heights or widths of the top-level children of a provided element 
 		and sets their min-height to the tallest height (or width to widest width). Sets in em units 
 		by default if pxToEm() method is available.
 * Dependencies: jQuery library, pxToEm method	(article: 
		http://www.filamentgroup.com/lab/retaining_scalable_interfaces_with_pixel_to_em_conversion/)							  
 * Usage Example: $(element).equalHeights();
  		Optional: to set min-height in px, pass a true argument: $(element).equalHeights(true);
 * Version: 2.0, 08.01.2008
--------------------------------------------------------------------*/

$.fn.equalHeights = function(px) {
	$(this).each(function(){
		var currentTallest = 0;
		$(this).children().each(function(i){
			if ($(this).height() > currentTallest) { currentTallest = $(this).height(); }
		});
		//if (!px || !Number.prototype.pxToEm) currentTallest = currentTallest.pxToEm(); //use ems unless px is specified
		// for ie6, set height since min-height isn't supported
		if ($.browser.msie && $.browser.version == 6.0) { $(this).children().css({'height': currentTallest}); }
		$(this).children().css({'min-height': currentTallest}); 
	});
	return this;
};

/*******************************************************************************
 * E N D O F J A V A S C R I P T *
 ******************************************************************************/



