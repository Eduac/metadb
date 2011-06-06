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
import edu.lafayette.metadb.model.userman.User;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet to check whether a valid session still exists.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class CheckSession extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		
		JSONObject output = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			if (session != null && checkUser(session)) {
				String name = (String) session.getAttribute(Global.SESSION_USERNAME);
				User user = UserManDAO.getUserData(name);
				long last_login = ((Long) session.getAttribute(Global.SESSION_LOGIN_TIME)).longValue();
				Date date = new Date(last_login+5*3600*1000);
				output.put("username", name);
		    	output.put("admin", user.getType().equals(Global.USER_ADMIN));
		    	output.put("local", user.getAuthType().equals("Local"));
		    	output.put("last_login", date.toString());
		    	output.put("success", true);
		    	output.put("parser_running", MetaDbHelper.getParserStatus());
		    	output.put("record_count", MetaDbHelper.getItemCount());
		    	output.put("log_types", Global.eventTypes);
		    	String[] last_page = UserManDAO.getLastProj(name).split(";");
		    	if (last_page.length > 1) {
		    		output.put("last_proj", last_page[0]);
		    		output.put("last_item", last_page[1]);
		    	}
			}
			else {
				output.put("success", false);
				output.put("message", "Your session has expired");
			}
			out.print(output);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		response.setContentType("application/x-json");
		out.flush();
	}
	
	public boolean checkUser(HttpSession session) {
		if (session.getAttribute(Global.SESSION_USERNAME) == null)
			return false;
		return MetaDbHelper.userExists((String) session.getAttribute(Global.SESSION_USERNAME));
	}

}
