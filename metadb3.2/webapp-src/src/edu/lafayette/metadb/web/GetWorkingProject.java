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
package edu.lafayette.metadb.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

/**
 * Class to retrieve the user's working project.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class GetWorkingProject extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(false);
		try {
			if (session!=null) {
				String project = (String) session.getAttribute(Global.SESSION_PROJECT);
				if (project.equals("") && project==null)
					out.println("Please choose a project from the Home tab");
				else
					out.print(project);
			}
		}
		catch(Exception e) {
			MetaDbHelper.logEvent(this.getClass().toString(), e);
		}
		out.flush();
	}

}
