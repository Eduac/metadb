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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.projects.ProjectsDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class DeleteProject
 */
public class DeleteProject extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String notification = "Project deletion failure... Database error";
		try {
			String projname = (String) request.getSession(false).getAttribute(
					Global.SESSION_PROJECT);
			String option = request.getParameter("delete-choices");

			if (option.equals("all"))
				if (ProjectsDAO.deleteProject(projname)) {
					notification = "All data and images in project " + projname
							+ " has been successfully deleted!";
					SysLogDAO.log(request, Global.SYSLOG_PROJECT,
							"User successfully deleted project " + projname);
				} else
					SysLogDAO.log(request, Global.SYSLOG_FATAL_ERROR,
							"Failure to delete project " + projname);
			else if (option.equals("admin-desc")) {
				if (ProjectsDAO.deleteAdminDescData(projname)) {
					notification = "All administrative/descriptive data in project "
							+ projname + " has been successfully deleted!";
					SysLogDAO.log(request, Global.SYSLOG_PROJECT,
							"User successfully deleted admin/desc data in project "
									+ projname);
				} else
					SysLogDAO.log(request, Global.SYSLOG_FATAL_ERROR,
							"Failure to delete admin/desc data in project "
									+ projname);
			} else if (option.equals("tech-images")) {
				if (ProjectsDAO.deleteTechData(projname)
						&& ProjectsDAO.deleteDerivatives(projname)) {
					notification = "All technical data and images in project "
							+ projname + " has been successfully deleted!";
					SysLogDAO.log(request, Global.SYSLOG_PROJECT,
							"User successfully deleted technical data and images in project "
									+ projname);
				} else
					SysLogDAO.log(request, Global.SYSLOG_FATAL_ERROR,
							"Failure to delete technical data and images in project "
									+ projname);
			}
			out.println(notification);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
