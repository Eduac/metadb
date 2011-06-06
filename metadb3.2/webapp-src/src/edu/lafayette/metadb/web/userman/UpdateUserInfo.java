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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet implementation class UpdateUserInfo
 */
public class UpdateUserInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		String type = request.getParameter("type");
		String authType = request.getParameter("auth-type");
		String password = request.getParameter("confirm-password");
		String notification = "User "+username+" cannot be changed... Database Error";
		try {
			if(authType.equals("Local"))
			{
				if (password != null && !password.equals("")) { 
				if (UserManDAO.updatePassword(username, password))
					notification = "Password changed successfully! ";
				else
					notification = "Error changing passwords";
				}
				else
					notification = "Passwords were blank, thus not changed!";
			}
			else
				notification = "LDAP user, password not affected. ";
			if(!type.equals(""))
			{
				if (UserManDAO.updateUserType(username, type))
					notification += "User type changed successfully! User "+username+" has been changed!!";
				else
					notification += "Error changing user type";
			}
			if (!authType.equals("")) {
				if (UserManDAO.updateAuthType(username, authType))
					notification += "User type changed successfully! User "+username+" has been changed!!";
				else
					notification += "Error changing user authentication method";
			}
			out.println(notification);
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
