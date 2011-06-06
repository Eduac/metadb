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

 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;

 import org.json.JSONArray;
 import org.json.JSONObject;

 import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO.DuplicateAttributeException;
 import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

 /**
  * Servlet to handle the updating of administrative/descriptive attributes.
  * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
  * 
  */
 public class UpdateAdminDescAttributes extends HttpServlet {	
	 private static final long serialVersionUID = 1L;

	 /**
	  * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	  */
	 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 PrintWriter out = response.getWriter();
		 JSONObject output = new JSONObject();
		 String project = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
		 String notification = "";
		 boolean success = true;
		 try {
			 JSONObject inputs = new JSONObject(request.getParameter("data"));
			 String type = inputs.getString("type");
			 JSONArray data = (JSONArray) inputs.get("data");
			 for (int i = 0; i < data.length(); i++) {
				 JSONObject datum = (JSONObject) data.get(i);
				 String element = datum.getString("elements");
				 String label = datum.getString("label");
				 int id = -1;
				 try {
					 id = Integer.parseInt(datum.getString("id"));
				 } catch (NumberFormatException e) {
					 id = -1;
				 }
				 boolean isSorted = datum.getString("sorted").equals("true");
				 boolean isLarge = datum.getString("large").equals("true");
				 boolean isControlled = datum.getString("controlled").equals("true");
				 boolean isMultiple = datum.getString("multiple").equals("true");
				 boolean isAdditions = datum.getString("additions").equals("true");
				 boolean isReadableDate = datum.getString("display-date").equals("true");
				 boolean isSearchableDate = datum.getString("search-date").equals("true");
				 
				 try
				 {
					 AdminDescAttributesDAO.update(project, id, element, label, type, 
							 isLarge, isReadableDate, isSearchableDate, isControlled, isMultiple, isAdditions, isSorted, i);					 
				 }
				 catch(DuplicateAttributeException e)
				 {
					// MetaDbHelper.note("DuplicateAttributeException caught in servlet");
					 success = false;
					 notification += "Duplicate element.qualifier fields are not permitted. The second instance of <b><i>"+element+(label.equals("") ? "" : "."+label)+"</b></i> could not be saved. <br/>";
					//MetaDbHelper.note("Duplicate fields found."); 	
					 			 
				 }
				 catch(Exception e)
				 {
					 success = false;
					 notification += "Attr "+i+" cannot be updated:<br/>";
					 notification += "Project "+project+", field "+element+" "+label+" at index " + i + "<br/>";
					 //MetaDbHelper.note("Cannot update: "+project+" "+element+" "+label+" "+type+" "+isLarge+" "+isControlled+" "+isSorted+" "+isMultiple+" "+isAdditions+" "+i);	
				 } 
			 }
			 output.put("success", success);
			 output.put("message", notification);
			 out.print(output);
		 }
		 catch (Exception e) {
			 MetaDbHelper.logEvent(e);
		 }
		 out.close();
	 }

 }
