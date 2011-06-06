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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.AdminDescDataDAO;
import edu.lafayette.metadb.model.metadata.TechnicalDataDAO;
import edu.lafayette.metadb.model.permission.Permission;
import edu.lafayette.metadb.model.permission.PermissionManDAO;

/**
 * Servlet implementation class ViewMetadata
 */
public class ViewMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
		String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
		String username = (String) request.getSession(false).getAttribute(Global.SESSION_USERNAME);

		JSONObject output = new JSONObject();
		JSONArray headers = new JSONArray();
		JSONArray items = new JSONArray();
		
		if (projname == null || projname.equals(""))
			return;

		int itemCount = ItemsDAO.nextItemNumber(projname)-1;
		
		
		try {
			JSONObject itemNumberHeader = new JSONObject();
			itemNumberHeader.put("name", "Filename");
			itemNumberHeader.put("editable", false);
			headers.put(itemNumberHeader);
			
			
			for (int i = 0; i < itemCount; i++) {
				items.put(new JSONArray());

				int itemNumber = i+1;
				String[] filePath = ItemsDAO.getMasterPath(projname, itemNumber).split("/");
				//String[] itemFilename = filePath[filePath.length - 1].split("-");
				String itemName = filePath[filePath.length - 1];
				items.getJSONArray(i).put(itemName);
				
			}
			
			
			
			
			String[] techList = request.getParameterValues(Global.MD_TYPE_TECH);
			if (techList != null)
				for (String attr:techList) {
					
					String element = attr.split("-")[0];
					String[] labelArray = attr.split("-");
					String label = labelArray.length > 1 ? labelArray[1] : "";
					
					
					JSONObject techHeader = new JSONObject();
					techHeader.put("name", element+ (label.equals("") ? "" : "."+label));
					techHeader.put("editable", false);
					headers.put(techHeader);
					
					List<String> techData = TechnicalDataDAO.getMultipleTechnicalData(projname, element, label);
					for (int i = 0; i < itemCount; i++) {
						String data = "";
						if (i < techData.size())
							data = techData.get(i);
						items.getJSONArray(i).put(data);
					}
				}
			
			Permission metadataPermission = PermissionManDAO.getUserPermission(username, projname);
			boolean editAdmin = metadataPermission.getAdmin_md().equals("read_write");
			boolean editDesc = metadataPermission.getDesc_md().equals("read_write");
			String[] adminList = request.getParameterValues(Global.MD_TYPE_ADMIN);
			if (adminList != null)
				for (String attr:adminList) {
					String[] labelArray = attr.split("-");
					
					String element = labelArray[0];
					String label = labelArray.length > 1 ? labelArray[1] : "";
					List<String> adminDescData = AdminDescDataDAO.getMultipleAdminDescData(projname, element, label, Global.MD_TYPE_ADMIN);
					
					//MetaDbHelper.note("Select size "+adminDescData.size());
				
					JSONObject adminHeader = new JSONObject();
					adminHeader.put("name", element+(label.equals("") ? "" : "."+label));
					adminHeader.put("editable", editAdmin);
					headers.put(adminHeader);
					
					for (int i = 0; i < itemCount; i++) {
						String data = "";
						if (i < adminDescData.size())
							data = StringEscapeUtils.escapeHtml(adminDescData
									.get(i));
						items.getJSONArray(i).put(data);
						
					}
				}
			
			String[] descList = request.getParameterValues(Global.MD_TYPE_DESC);
			
			if (descList != null)
				for (String attr:descList) {
					String[] labelArray = attr.split("-");
					
					
					
					String element = labelArray[0];
					String label = labelArray.length > 1 ? labelArray[1] : "";
					List<String> adminDescData = AdminDescDataDAO.getMultipleAdminDescData(projname, element, label, Global.MD_TYPE_DESC);
					
					//MetaDbHelper.note("Select size "+adminDescData.size());
					
					JSONObject descHeader = new JSONObject();
					descHeader.put("name", element+(label.equals("") ? "" : "."+label));
					descHeader.put("editable", editDesc);
					headers.put(descHeader);
					
					for (int i = 0; i < itemCount; i++) {
						String data = "";
						if (i < adminDescData.size()) 
							if (AdminDescAttributesDAO.getAttributeByName(projname, element, label).isSorted())
								data = StringEscapeUtils.escapeHtml(MetaDbHelper.sortVocab(adminDescData.get(i)));
							else
								data = StringEscapeUtils.escapeHtml(adminDescData.get(i));
						items.getJSONArray(i).put(data);
					}
				}
			
			output.put("headers", headers);
			output.put("items", items);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		
		out.print(output);
		out.flush();
	}
	
}
