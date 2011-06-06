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
package edu.lafayette.metadb.web.projectman.imagesettings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;

/**
 * Servlet implementation class ShowProjectSettings
 */
public class ShowProjectSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try {
			String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
			String projPath = Global.MASTERS_DIRECTORY + '/' + projname+ '/';
			int size = ItemsDAO.nextItemNumber(projname) - 1;
			out.println(this.getSettingsOutput(projname, projPath, size));
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(this.getClass().toString(), "doPost", MetaDbHelper.getStackTrace(e));
		}
		out.flush();
	}
	
	private String getSettingsOutput(String projname, String projPath, int size) {
		StringBuilder output = new StringBuilder("Cannot verify project");
		if (!projname.equals("") && projname != null) {
			output = new StringBuilder("<table id=\"required-fields\" border=\"0\">");
			output.append( "<thead><tr>");
			output.append( "<th><div align=\"right\">setting</div></th>");
			output.append( "<th>value</th>");
			output.append( "</tr></thead>");
			output.append( "<tbody>");
			output.append( "<tr>");
			output.append( "<td><div align=\"right\">Project Name:</div></td>");
			output.append( "<td><div align=\"left\">" + projname + "</div></td>");
			output.append( "</tr>");
			output.append( "<tr>");
			output.append( "<td><div align=\"right\">Project Image Path:</div></td>");
			output.append( "<td>" + projPath + "</td>");
			output.append( "</tr>");
			output.append( "<tr>");
			output.append( "<td><div align=\"right\">Current Number of Images:</div></td>");
			output.append( "<td><div align=\"left\" id='image-number'>" + size + "</div></td>");
			output.append( "</tr></tbody></table>");
			output.append( "<br /><input type=\"button\" value=\"Verify Project Settings\" class=\"ui-state-default\" onclick=\"return false;\" />");
		}
		return output.toString();
	}

}
