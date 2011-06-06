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
import org.json.JSONException;
import org.json.JSONObject;

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.attributes.TechAttribute;
import edu.lafayette.metadb.model.attributes.TechAttributesDAO;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

/**
 * Servlet to display all administrative/descriptive/technical attributes for a project.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class ShowAllAttributes extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
		if (projname == null || projname.equals(""))
			return;
		int attrPerRow = 3;
		JSONObject output = new JSONObject();
		try {
			output.put("technical", this.getTechAttributes(projname, attrPerRow));
			output.put("administrative", this.getAdminDescAttributes(projname, Global.MD_TYPE_ADMIN, attrPerRow));
			output.put("descriptive", this.getAdminDescAttributes(projname, Global.MD_TYPE_DESC, attrPerRow));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			MetaDbHelper.logEvent(e);
		}
		
//		out.println("<br/><input type='button' onclick='DataMan.getSelectedAttributes()' value='View Table' class='ui-state-default'/>");
		out.print(output);
		out.close();
	}
	
	private JSONArray getTechAttributes(String projname, int attrPerRow) {
		JSONArray attrArray = new JSONArray();
		try {
			ArrayList<TechAttribute> mdList = TechAttributesDAO.getTechAttributes(projname);
			if (mdList!=null) {
				for (int i = 0; i < mdList.size() ; i++) {
					TechAttribute t = mdList.get(i);
					JSONObject tech = new JSONObject();
					tech.put("element", t.getElement());
					tech.put("label", t.getLabel());
					attrArray.put(tech);
				}
			}
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return attrArray;
	}
	
//	private String getTechAttributes(String projname, int attrPerRow) {
//		JSONArray attrArray = new JSONArray();
//		float cellWidth = 100/attrPerRow;
//		String tech = "<table width='100%'><thead>";
//		tech += "<tr align='left'>";
//		tech += "<th>technical metadata<input id='technical-all' type='checkbox'/></th>";
//		tech += "<th></th>";
//		tech += "</tr>";
//		tech += "</thead><tbody>";
//        
//		try {
//			ArrayList<TechAttribute> mdList = TechAttributesDAO.getTechAttributes(projname);
//			if (mdList!=null) {
//				if (mdList.isEmpty())
//					return "<b>No tech attributes in this project</b><br/>";
//				tech += "<tr align='left'>\n";
//				for (int i = 0; i < mdList.size() ; i++) {
//					TechAttribute t = mdList.get(i);
//					if (i % attrPerRow == 0)
//						tech += "</tr><tr align='left'>\n";
//					tech += "<td style='width:" + cellWidth + "%'><input class='ui-state-default' type='checkbox' name='technical' value='" + t.getElement() + "-" + t.getLabel() + "'>";
//					String label = t.getLabel();
//					tech += t.getElement() + (label.equals("") ? "" : "." + label);
//					tech += "</td>";
//					if (i == mdList.size()-1 && (i+1)%attrPerRow != 0)
//						for (int j = 0; j < attrPerRow - i%attrPerRow; j++)
//							tech += "<td></td>";
//				}
//				tech += "</tr>";
//			}
//		}
//		catch (Exception e) {
//			MetaDbHelper.logEvent("ShowAllAttributes", "gettechAttributes", MetaDbHelper.getStackTrace(e));
//		}
//		tech += "</tbody></table>";
//		return tech; 
//	}
	
	private JSONArray getAdminDescAttributes(String projname, String type, int attrPerRow) {
		JSONArray attrArray = new JSONArray();
		
		try {
			ArrayList<AdminDescAttribute> mdList = AdminDescAttributesDAO.getAdminDescAttributes(projname, type);
			if (mdList!=null) {
				
				for (int i = 0; i < mdList.size() ; i++) {
					AdminDescAttribute t = mdList.get(i);
					JSONObject adminDesc = new JSONObject();
					adminDesc.put("element", t.getElement());
					adminDesc.put("label", t.getLabel());
					attrArray.put(adminDesc);
				}
				
			}
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return attrArray;
	
	}
	
//	private String getAdminDescAttributes(String projname, String type, int attrPerRow) {
//		float cellWidth = 100/attrPerRow;
//		String adminDesc = "<table width='100%'><thead>";
//		adminDesc += "<tr align='left'>";
//		adminDesc += "<th>" + type + " metadata<input id='" + type + "-all' type='checkbox'/></th>";
//		adminDesc += "<th></th>";
//		adminDesc += "</tr>";
//		adminDesc += "</thead><tbody>";
//        
//		try {
//			ArrayList<AdminDescAttribute> mdList = AdminDescAttributesDAO.getAdminDescAttributes(projname, type);
//			if (mdList!=null) {
//				if (mdList.isEmpty())
//					return "<b>No "+type+" attributes in this project</b><br/>";
//				adminDesc += "<tr align='left'>\n";
//				for (int i = 0; i < mdList.size() ; i++) {
//					AdminDescAttribute t = mdList.get(i);
//					if (i % attrPerRow == 0)
//						adminDesc += "</tr><tr align='left'>\n";
//					adminDesc += "<td style='width:" + cellWidth + "%'><input class='ui-state-default' type='checkbox' name='" + t.getMdType() + "' value='" + t.getElement() + "-" + t.getLabel() + "'>";
//					String label = t.getLabel();
//					adminDesc += t.getElement() + (label.equals("") ? "" : "." + label);
//					adminDesc += "</td>";
//					if (i == mdList.size()-1 && (i+1)%attrPerRow != 0)
//						for (int j = 0; j < attrPerRow - i%attrPerRow; j++)
//							adminDesc += "<td></td>";
//				}
//				adminDesc += "</tr>";
//			}
//		}
//		catch (Exception e) {
//			MetaDbHelper.logEvent("ShowAllAttributes", e);
//		}
//		adminDesc += "</tbody></table>";
//		return adminDesc;
//	
//	}

}
