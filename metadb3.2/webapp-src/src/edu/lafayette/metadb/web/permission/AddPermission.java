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
package edu.lafayette.metadb.web.permission;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.permission.PermissionManDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class AddPermission
 */
public class AddPermission extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		String projname = request.getParameter("projname");
		String data = request.getParameter("data");
		
		String admin_md = request.getParameter("admin_md");
		String desc_md = request.getParameter("desc_md");
		
		String table_edit = request.getParameter("table_edit");
		String controlled_vocab = request.getParameter("controlled_vocab");
		String notification = "Cannot create permission";
		try {
			if (PermissionManDAO.createPermission(projname, username, data, admin_md, desc_md, table_edit, controlled_vocab))
			{
				notification = "Permission created successfully!";
				HttpSession session = request.getSession(false);
  				if(session!=null)
  				{
  					String userName=(String)session.getAttribute(Global.SESSION_USERNAME);
  					SysLogDAO.log(userName, Global.SYSLOG_PERMISSION, "User "+userName+" given permissions for project "+projname);
  				}
			}
			out.print(notification);
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
