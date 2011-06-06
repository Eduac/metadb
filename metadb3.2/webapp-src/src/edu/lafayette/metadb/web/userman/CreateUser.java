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
package edu.lafayette.metadb.web.userman;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.syslog.SysLogDAO;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet implementation class CreateUser
 */
public class CreateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();
		JSONObject output = new JSONObject();
		try {
			String username = request.getParameter("username");
			String type = request.getParameter("permission");
			String authType = request.getParameter("auth-type");
			//MetaDbHelper.note("Auth Type: "+authType);
			String pwd = (authType.equals("Local") ? request.getParameter("confirm-password") : getRandomPass()) ;

			if (UserManDAO.createUser(username, pwd, type, authType))
			{
				output.put("success", true);
				output.put("message", type+" "+username+" created successfully!!");
				output.put("username", username);
				output.put("local", authType.equals("Local"));
				output.put("admin", type.equals("admin"));
				
				HttpSession session = request.getSession(false);
  				if(session!=null && session.getAttribute(Global.SESSION_BYPASS) == null)
  				{
  					String userName = (String)session.getAttribute(Global.SESSION_USERNAME);
  					SysLogDAO.log(userName, Global.SYSLOG_SYSTEM, "User "+username+" created with permission "+type);
  				}
			}
			else
			{
				output.put("success", false);
				output.put("message", "User cannot be created");
				HttpSession session = request.getSession(false);
  				if(session!=null && session.getAttribute(Global.SESSION_BYPASS) == null)
  				{
  					String userName = (String)session.getAttribute(Global.SESSION_USERNAME);
  					SysLogDAO.log(userName, Global.SYSLOG_ERROR, "Failure to create user "+userName);
  				}
			}
			out.print(output);
		}
		catch(Exception e) {
			MetaDbHelper.logEvent(e);		
		}
		out.flush();	
	}

	/**
	 * Return a random password to use for LDAP users.
	 * @return a random password, using the hash value of the current system time (long)
	 */
	public String getRandomPass()
	{
		return Integer.toString(new Random(System.currentTimeMillis()).hashCode());
	}
}
