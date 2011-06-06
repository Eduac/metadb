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
package edu.lafayette.metadb.web.dataman;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.AdminDescDataDAO;

/**
 * Servlet implementation class UpdateMetadata
 */
public class UpdateMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"), true);
		try {
			
			String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
			int itemNumber = Integer.parseInt(request.getParameter("itemNumber"));
			
			String userName=(String)request.getSession(false).getAttribute("username");

			String locker=ItemsDAO.getLocker(projname, itemNumber);
			//MetaDbHelper.note("Locker: "+locker+" , item number: "+itemNumber);
			if(!(userName.equals(locker) || locker.trim().equals("") || locker == null))
				return;

			String data = request.getParameter("content");
			String attribute = request.getParameter("attribute");
			//MetaDbHelper.note("Editing from View Metadata "+itemNumber+","+ data+","+attribute );
			String[] elementLabelArray = attribute.split("\\.", 2);
			String element = elementLabelArray[0];
			String label = "";

			try {
				label = elementLabelArray[1];
			} catch (ArrayIndexOutOfBoundsException e) {
				label = "";
			}
			if (AdminDescDataDAO.updateAdminDescData(projname, itemNumber, element, label, data))
				out.print(data);
			else {
				//MetaDbHelper.note("Cannot update from View Metadata "+itemNumber+", "+ data+", "+element+", "+label );
			}
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.close();
		
	}

}
