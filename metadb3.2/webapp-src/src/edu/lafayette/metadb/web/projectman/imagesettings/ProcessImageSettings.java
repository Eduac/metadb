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
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class ProcessImageSettings
 */
public class ProcessImageSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try {
			String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
			String option = request.getParameter("process-options");
			if (option.toLowerCase().equals("all")) {
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PROJECT, "Initiated processing of all images in "+projname+".");
				out.print("Processing all, project: " + projname);
				new ItemsDAO(projname, null, false).start();
			}
			else if (option.toLowerCase().equals("new")) {
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PROJECT, "Initiated processing of new images in "+projname+".");
				out.print("Processing new, project: " + projname);
				new ItemsDAO(projname, null, true).start();
			}
			else if (option.toLowerCase().equals("these")) {
				String indices = request.getParameter("items");
				Set<Integer> indexSet = MetaDbHelper.filterRecords(indices);
				Integer[] nums = new Integer[indexSet.size()];
				indexSet.toArray(nums);
				int[] items = new int[nums.length];
				indices = "";
				for (int i = 0; i< items.length; i++ ) {
					items[i] = nums[i].intValue();
					indices += items[i];
					indices += i == items.length - 1 ? "" : ",";
				}
				new ItemsDAO(projname, items, false).start();
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PROJECT, "Initiated processing of items "+indices+" in "+projname+".");
				out.print("Processing these: " + indices + " in project: " + projname);
			}
		} catch (Exception e) {
			MetaDbHelper.logEvent(this.getClass().toString(), "doPost", MetaDbHelper.getStackTrace(e));
		}
		out.flush();
	}
	
}
