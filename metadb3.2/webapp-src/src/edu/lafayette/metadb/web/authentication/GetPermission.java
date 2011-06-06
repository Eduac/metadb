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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.permission.Permission;
import edu.lafayette.metadb.model.permission.PermissionManDAO;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet to retrieve a user's permission for a project.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class GetPermission extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetPermission() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		 PrintWriter out = response.getWriter();
		 Permission permission = null;
		 HttpSession session = request.getSession(false);
		 JSONObject output = new JSONObject();

		 try {
			 if (session.getAttribute(Global.SESSION_BYPASS) != null) {
				 output.put("admin", true);
				 return;
			 }
			 String projectName = (String) session.getAttribute(Global.SESSION_PROJECT);
			 String username = (String) session.getAttribute(Global.SESSION_USERNAME);

			 if (UserManDAO.getUserData(username).getType().equalsIgnoreCase("admin"))
				 output.put("admin", true);
			 else {
				 output.put("admin", false);
				 permission = PermissionManDAO.getUserPermission(username, projectName);
				 if(permission!=null)
				 {
					 output.put("administrative", permission.getAdmin_md());
					 output.put("descriptive", permission.getDesc_md());
					 output.put("data", permission.getData());
					 output.put("table_edit", permission.getTable_edit());
					 output.put("controlled_vocab", permission.getControlled_vocab());
				 }
				 else
				 {
					 output.put("administrative", "none");
					 output.put("descriptive", "none");
					 output.put("data", "none");
					 output.put("table_edit", "none");
					 output.put("controlled_vocab", "none");
				 }
			 }
			 out.print(output);
		 }
		 catch (Exception e) {
			 MetaDbHelper.logEvent(e);
		 }
		 response.setContentType("application/x-json");
		 out.flush();
	}

}
