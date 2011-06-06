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
package edu.lafayette.metadb.web.authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.projects.ProjectsDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;
import edu.lafayette.metadb.model.userman.User;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet to handle user authentication and login.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username-login");
		String pwd = request.getParameter("password-login");
		JSONObject output = new JSONObject();
		try {
			User user = UserManDAO.getUserData(username);
			//SysLogDAO.log(username, Global.SYSLOG_AUTH, "User "+username+" trying to login.");
			
			//User != null means DB conn succeeded
			if (user!=null && !user.getUserName().equals("")){
				MetaDbHelper.note("User is not null.");
				if (UserManDAO.checkPassword(username, pwd)) {
					SysLogDAO.log(username, Global.SYSLOG_AUTH, "User "+username+": successfully logged in.");
					long last_login = new Long(user.getLast_login());
					HttpSession session = request.getSession();
					String project = ProjectsDAO.getProjectList().isEmpty() ? "" : ProjectsDAO.getProjectList().get(0);
					setUpSession(session, username, project);

					String last_date = "";
					if (!UserManDAO.updateLoginTime(username, session.getLastAccessedTime()))
						last_date = "error";

					else if (last_login != 0) {
						Date date = new Date(last_login+5*3600*1000);
						last_date = date.toString();
					}
					session.setAttribute(Global.SESSION_LOGIN_TIME, last_login);
					output.put("username", username);
					output.put("admin", user.getType().equals(Global.USER_ADMIN));
					output.put("local", user.getAuthType().equals("Local"));
					output.put("last_login", last_date);
					output.put("success", true);
					output.put("parser_running", MetaDbHelper.getParserStatus());
					output.put("record_count", MetaDbHelper.getItemCount());
					output.put("log_types", Global.eventTypes);
					String[] last_page = UserManDAO.getLastProj(username).split(";");
					if (last_page.length > 1) {
						output.put("last_proj", last_page[0]);
						output.put("last_item", last_page[1]);
					}
				}
				else {
					SysLogDAO.log(username, Global.SYSLOG_AUTH, "User "+username+": Authentication error, could not log in.");
					output.put("success", false);
					output.put("message", "Username/Password mismatch");
				}
			} 
			else if (user!=null && user.getUserName().equals(""))
				{
				SysLogDAO.log(Global.UNKNOWN_USER, Global.SYSLOG_AUTH, "UNKNOWN user: "+username);
				output.put("success", false);
				output.put("message", "Username/Password mismatch");
			}
			else
			{
				output.put("success", false);
				output.put("message", "Connection to database cannot be established");
			}		
			out.print(output);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

	private void setUpSession(HttpSession session, String username, String project) {
		if (session.isNew()) {
			session.setAttribute(Global.SESSION_USERNAME, username);
			session.setAttribute(Global.SESSION_PROJECT, project);
			session.setAttribute(Global.SESSION_BINDER, new SessionBinder());
		}
		if (session.getAttribute(Global.SESSION_USERNAME) == null) 
			session.setAttribute(Global.SESSION_USERNAME, username);
		if (session.getAttribute(Global.SESSION_PROJECT) == null) 
			session.setAttribute(Global.SESSION_PROJECT, project);
		if (session.getAttribute(Global.SESSION_BINDER) == null)
			session.setAttribute(Global.SESSION_BINDER, new SessionBinder());
	}

}
