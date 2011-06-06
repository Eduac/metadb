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
package edu.lafayette.metadb.web.projectman;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.permission.Permission;
import edu.lafayette.metadb.model.permission.PermissionManDAO;
import edu.lafayette.metadb.model.projects.ProjectsDAO;
import edu.lafayette.metadb.model.userman.User;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet implementation class ShowProjects
 */
public class ShowProjects extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(false);
		StringBuilder bigList = new StringBuilder();;
		try {
			if (session != null && session.getAttribute(Global.SESSION_BYPASS) == null) {
				String username = (String) session.getAttribute(Global.SESSION_USERNAME);
				String chosen_project = (String) session.getAttribute(Global.SESSION_PROJECT);
				if (chosen_project == null)
					chosen_project = "";
				User user = UserManDAO.getUserData(username);
				if (user.getType().equals("admin")) {
					List<String> list = ProjectsDAO.getProjectList();
					if(list.size() !=0)
					{
						for (String projectName : list) 
						{
							if (projectName.equals(chosen_project))
								bigList.append("<option name='"+projectName+"' selected>"+projectName+"</option>");
							else
								bigList.append("<option name='"+projectName+"'>"+projectName+"</option>");
						}
						if (chosen_project.equals(""))
							session.setAttribute(Global.SESSION_PROJECT, list.get(0));
					}
				}

				else {
					ArrayList<Permission> list = PermissionManDAO.getAllProjectPermission(username);
					if(list.size()!=0)
					{
						Collections.sort(list);
						for (Permission permission : list) {
							String projectName = permission.getProjectName();
							if (projectName.equals(chosen_project))
								bigList.append("<option name='"+projectName+"' selected>"+projectName+"</option>");
							else
								bigList.append("<option name='"+projectName+"'>"+projectName+"</option>");
							if (chosen_project.equals(""))
								session.setAttribute(Global.SESSION_PROJECT, list.get(0));
						}
					}
				}
			}
		} catch (NullPointerException e) {
			// TODO: Handle cases when there're no projects
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.print(bigList.toString());
		out.flush();
	}
}
