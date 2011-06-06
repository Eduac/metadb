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
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.userman.User;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet implementation class ShowUsers
 */
public class ShowUsers extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		JSONObject output = new JSONObject();
		JSONArray admins = new JSONArray();
		JSONArray workers = new JSONArray();
		JSONObject permissions = new JSONObject();
		try {
			ArrayList<User> userList = UserManDAO.getUserList();
			for (User u: userList) {
				JSONObject user = new JSONObject();
				JSONObject info = new JSONObject();				
				info.put("auth_type", u.getAuthType());
				info.put("user_type", u.getType());
			
				user.put("username", u.getUserName());
				permissions.put(u.getUserName(), info);
				if (u.getType().equals("admin"))
					admins.put(user);
				else
					workers.put(user);
			}
			output.put("admins", admins);
			output.put("workers", workers);
			output.put("permissions", permissions);
			out.print(output);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
