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
package edu.lafayette.metadb.web.attributesman;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.elements.ElementsDAO;

/**
 * Servlet to display the list of administrative/descriptive attributes for a project.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class ShowAdminDescAttributes extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		try {
			String projname = (String)request.getSession(false).getAttribute(
					Global.SESSION_PROJECT);
			String type = request.getParameter("type");
			//MetaDbHelper.note("Trying to get "+type+" attributes for "+projname+"...");

			ArrayList<AdminDescAttribute> mdList = AdminDescAttributesDAO
					.getAdminDescAttributes(projname, type);
			
			JSONObject output = new JSONObject();
			JSONArray attrList = new JSONArray();
			output.put("elements", ElementsDAO.getElementList());
			output.put("type", type);
			
			if (mdList != null) {
				//if (mdList.isEmpty())
					//mdList.add(new AdminDescAttribute(projname, (type.equals("administrative") ? "identifier" : "title"), "",
						//	type, false, false, false, false, false, false, false, 0));
				
				for (int i = 0; i < mdList.size(); i++) {
					
					JSONObject attribute = new JSONObject();
					
					AdminDescAttribute t = mdList.get(i);

					
					attribute.put("element", t.getElement());
					attribute.put("label", t.getLabel());
					attribute.put("id", t.getId());
					attribute.put("displayDate", t.isReadableDate());
					attribute.put("searchDate", t.isSearchableDate());
					attribute.put("large", t.isLarge());
					attribute.put("controlled", t.isControlled());
					attribute.put("multiple", t.isMultiple());
					attribute.put("additions", t.isAdditions());
					attribute.put("sorted", t.isSorted());
					attribute.put("error", t.isError());
					attribute.put("vocab", AdminDescAttributesDAO.getControlledVocab(t.getId()));
					attrList.put(attribute);
					

				}
				output.put("data", attrList);
			}
			out.print(output);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.close();
	}

}
