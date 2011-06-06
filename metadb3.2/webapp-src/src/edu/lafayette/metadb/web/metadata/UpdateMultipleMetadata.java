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
package edu.lafayette.metadb.web.metadata;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.AdminDescDataDAO;

/**
 * Servlet implementation class UpdateMultipleMetadata
 */
public class UpdateMultipleMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		try {
			HttpSession session = request.getSession(false);
			String projname = (String) session.getAttribute(Global.SESSION_PROJECT);
			String type = request.getParameter("type");
			String records = request.getParameter("records");
			JSONArray failed_items = new JSONArray();
			JSONObject output = new JSONObject();
			String message = "";
			String failedRecords = "";
			if (records != null) {
				List<Integer> chosenRecords = null;
				if (records.equals("all")) 
					chosenRecords = AdminDescDataDAO.getAdminDescItemNumbers(projname, type);
				else
					chosenRecords = new ArrayList<Integer>(this.filterRecords(records));
				
				if (chosenRecords!=null && !chosenRecords.isEmpty()) {
					Map<String, String[]> attributes = new HashMap<String, String[]>(request.getParameterMap());
					attributes.remove("project-name");
					attributes.remove("type");
					attributes.remove("records");
					//message = type+" Records are updating...\n";
					for (int i = 0; i< chosenRecords.size(); i++) 
					{
						String userName=(String)session.getAttribute("username");
						String locker=ItemsDAO.getLocker(projname, chosenRecords.get(i));
						boolean process=true;
						if(!(userName.equals(locker) || locker.trim().equals("") || locker == null))
						{
							failedRecords+= " "+chosenRecords.get(i);
							process=false; //Do not touch this record if it is locked.
						}

						//Update all the attributes for item i
						Set keys = attributes.keySet();
						Iterator itr = keys.iterator();
						while (itr.hasNext()) {
							String entry = (String) itr.next();
							int id = Integer.parseInt(entry);
//							String newData = attributes.get(entry)[0] == null ? "": attributes.get(entry)[0];
							JSONObject whitelisted = UpdateAdminDescMetadata.whitelist(entry, projname, type, chosenRecords.get(i), request);
							if (!whitelisted.getBoolean("success")) {
								//MetaDbHelper.note(whitelisted.toString());
								//MetaDbHelper.note(entry+" "+projname+" "+type+" "+chosenRecords.get(i));
								failed_items.put(whitelisted);
								break;
							}
							if(process)
							{
								if(!AdminDescDataDAO.updateAdminDescData(projname, chosenRecords.get(i), id, whitelisted.getString("data")))
									failedRecords+=" "+chosenRecords.get(i); //even if locker check succeeded, could still fail.
							}
						}
					}
					message = "Data updates complete.\n";
					if(!(failedRecords.trim().equals("")))
						message+="The following item(s) could not be updated:"+failedRecords;
				}
			}
			else
				message = "No attributes in project";
			output.put("failure", failed_items);
			output.put("message", message);
			output.put("type", type);
			out.print(output);
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.close();
	}
	
	private Set<Integer> filterRecords(String source) {
		Set<Integer> records = new HashSet<Integer>();
		String[] processedEntries = source.split(",");
		try {
			for (String entry:processedEntries) {
				if (entry!=null && entry!="") {
					if (entry.indexOf("-")!=-1) {
						int min = Integer.parseInt(entry.split("-")[0]);
						int max = Integer.parseInt(entry.split("-")[1]);
						for (int i = min; i <= max; i++) 
							records.add(i);
					}
					else
						records.add(Integer.parseInt(entry));
				}
			}
		}
		catch (Exception e) {
			records.clear();
		}
		return records;
		
	}

}
