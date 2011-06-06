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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;


import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

import edu.lafayette.metadb.model.items.AdminDescItem;
import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.permission.Permission;
import edu.lafayette.metadb.model.permission.PermissionManDAO;
import edu.lafayette.metadb.model.userman.User;
import edu.lafayette.metadb.model.userman.UserManDAO;


/**
 * Servlet implementation class ShowAdminDescMetadata
 */
public class ShowAdminDescMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = new PrintWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"), true);
		String type = request.getParameter("type");
		String raw_index = request.getParameter("item-index");
		String current_item = request.getParameter("current");
		String direction = request.getParameter("direction");
		JSONObject output = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			if (session != null) {
				String username = (String) session
				.getAttribute(Global.SESSION_USERNAME);
				String projname = (String) session
				.getAttribute(Global.SESSION_PROJECT);
				if (username != null && !username.equals("")
						&& projname != null && !projname.equals("")) {




					int index = MetadataUIHelper.getNextIndex(projname, raw_index, current_item,
							direction);
					Item item = ItemsDAO.getItem(projname, index);

					if (item == null) {
						output.put("error", true);
						output.put("message", "No item exists this project. Please contact administrator");
					} else {


						boolean enabled = false;
						if (checkPermission(projname, type, username)) {
							if (ItemsDAO.changeLock(projname, item.getItemNumber(), username)) {
								output.put("error", false);
								item.setLocker(username);
								enabled = true;
							} else {
								output.put("error", true);
								output.put("message", "<div style=\"height: 20px; padding-top:5px\" class=\"error-message ui-state-error\">This item is locked by user "+ item.getLocker() +". Please try again later.</div>");
								enabled = false;
							}
						} else {
							output.put("error", true);
							output.put("message", "<div style=\"height: 20px; padding-top:5px\" class=\"error-message ui-state-error\">You do not have write permission to this record.</div>");
							enabled = false;
						}
						output.put("enabled", enabled);
						output.put("project", projname);
						output.put("type", type);
						output.put("data", this.getData(projname, type, item, enabled));	
						UserManDAO.updateLastProject(username, projname+";"+item.getItemNumber());	
					}
				} else {
					output.put("error", true);
					output.put("message", "Cannot authenticate username " + username
							+ ", project " + projname);
				}
			} else {
				output.put("error", true);
				output.put("message", "Session doesn't exist, please login.");
			}

		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.print(output);
		out.close();
	}

	private boolean checkPermission(String projectName, String type,
			String username) {
		try {
			User user = UserManDAO.getUserData(username);
			if (user.getType().equals("admin"))
				return true;
			Permission permissionList = PermissionManDAO.getUserPermission(
					username, projectName);
			if (permissionList == null)
				return false;
			else {
				if (type.equals(Global.MD_TYPE_ADMIN))
					return permissionList.getAdmin_md().equals("read_write");
				else if (type.equals(Global.MD_TYPE_DESC))
					return permissionList.getDesc_md().equals("read_write");
			}

		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	private String getData(String projname, String type, Item item,
			boolean enabled) {
		StringBuilder metadata = new StringBuilder();
		try {
			if (item != null) {
				int size = ItemsDAO.nextItemNumber(projname) - 1;
				ArrayList<AdminDescItem> itemList = item.getData(type);
				metadata.append(this.getMetadataTable(size, type, item
						.getItemNumber(), itemList, enabled));
			} else
				metadata.append("No items in the project");
		} catch (Exception e) {
			metadata=new StringBuilder("Error retrieving data<br/>");
			metadata.append(e.toString());
			MetaDbHelper.logEvent(e);
		}
		return metadata.toString();
	}


	/**
	 * 
	 * @param size
	 * @param type
	 * @param index
	 * @param itemList
	 * @param enabled
	 * @return
	 */
	private String getMetadataTable(int size, String type, int index,
			ArrayList<AdminDescItem> itemList, boolean enabled) {
		StringBuilder metadata = new StringBuilder();
		String allowance = enabled ? "" : " disabled ";
		metadata.append("<form id='" + type + "-metadata' current='" + index
				+ "' size='" + size + "' method='post' onsubmit='return false;'>");
		metadata.append("<table>");
		metadata.append("<thead>");
		if (enabled) {
			metadata.append("<tr align='left'>");
			metadata.append("<th><div class='metadata-update' style='margin-top:10px'>");
			metadata.append("<input type='button' class='ui-state-default' value='Update' "
					+ allowance + "/>");
			metadata.append("<select name='update-option' style='margin-left:10px; margin-right:10px; visibility: hidden;'>");
			metadata.append("<option value='all'>All records</option>");
			metadata.append("<option value='these'>These records</option>");
			metadata.append("</select><input type='text' class='apply-to-some' name='apply-to-some' class='ui-widget-content' size='14' style='visibility: hidden'/>");
			metadata.append("</div></th>");
			metadata.append("</tr>");
		}
		metadata.append("<tr>");
		metadata.append("<th><span style='position:absolute; z-index:2'>Attributes</span><span style='float:right;'>Select all fields</span></th>");
		metadata.append("<th><input type='checkbox' class='ui-state-default' name='select' "
				+ allowance + "/></th>");
		metadata.append("</tr>");

		metadata.append("</thead><tbody>");
		if (!itemList.isEmpty()) {
			for (int i = 0; i < itemList.size(); i++) {
				AdminDescItem adminDescItem = itemList.get(i);
				String element = adminDescItem.getElement();
				String label = adminDescItem.getLabel();
				String tag = getTag(adminDescItem);
				boolean contr = adminDescItem.isControlled();
				boolean multiple = adminDescItem.isMultiple();
				boolean additions = adminDescItem.isAdditions();
				boolean large = adminDescItem.isLarge();
				metadata.append("<tr align='left'>");
				metadata.append("<td>");
				metadata.append("<b>"+element + (label.equals("") ? "" : "." + label)+"</b>");

				metadata.append(":" + tag + "<br/>");
				metadata.append("</td>");
				metadata.append("<td></td>");
				metadata.append("<tr>");
				if(large)
					metadata.append("<td style='padding-left:25px; padding-bottom: 20px'>");
				else
					metadata.append("<td style='padding-left:25px'>");
				metadata.append(getDisplayInput(adminDescItem, allowance));
				metadata.append("</td>");
				if(contr&& !(multiple || additions))
					metadata.append("<td style='position: relative; display: block;'><input type='checkbox' style='position: absolute; top:4px' " + allowance
							+ "/></td>");
				else if(multiple || additions)
					metadata.append("<td style='position: relative; display: block;'><input type='checkbox' style='position: absolute; top:4px' " + allowance
						+ "/></td>");
				else if(large)
					metadata.append("<td style='position: relative; display: block;'><input type='checkbox' style='position: absolute; top:50px' " + allowance
							+ "/></td>");
				else
					metadata.append("<td style='position: relative; display: block;'><input type='checkbox' style='position: absolute; top:4px' " + allowance
							+ "/></td>");
				metadata.append("</tr>");

				//				metadata.append("<br/>");
			}
			if (enabled) {
				metadata.append("<tr>\n");
				metadata.append("<td>");
				metadata.append("<div class='metadata-update'>");
				metadata.append("<input type='button' class='ui-state-default' value='Update' "
						+ allowance + "/>");
				metadata.append("<select name='update-option' style='margin-left:10px; margin-right:10px; visibility: hidden;'>");
				metadata.append("<option value='all'>All records</option>");
				metadata.append("<option value='these'>These records</option>");
				metadata.append("</select><input type='text' class='apply-to-some' name='apply-to-some' class='ui-widget-content' size='14' style='visibility: hidden'/>");
				metadata.append("</div></td>");
				metadata.append("</tr>");
			}

			metadata.append("</tbody>");
			metadata.append("</table>");
			metadata.append("</form>");
		} else
			metadata = new StringBuilder("Please add " + type + " metadata fields");
		return metadata.toString();
	}

	/**
	 * Get the displayed tag of an attribute
	 * 
	 * @param attr
	 *            the attribute to search for
	 * @return string containing controlled/additions or multiple tag
	 */
	private static String getTag(AdminDescItem attr) {
		String tag = "";

		if (attr.isControlled()) {
			tag = "(Controlled";
			tag += attr.isMultiple() ? ", Multiple" : "";
			tag += attr.isAdditions() ? ", Additions" : "";
			tag += attr.isSorted() ? ", Alphabetical" : "";
			tag += ")";
		} else if (attr.isReadableDate()) {
			tag = "(Display Date)";
		} else if (attr.isSearchableDate()) {
			tag = "(Search Date)";
		}
		return tag;
	}

	/**
	 * Get the HTML tag of an attribute
	 * 
	 * @param attr
	 *            the attribute to search for
	 * @return string containing controlled/additions or multiple tag
	 */
	private static String getHTMLTag(AdminDescItem attr) {
		String tag = "";
		if (attr.isControlled()) {
			tag = "controlled";
			tag += attr.isMultiple() ? " multiple" : "";
			tag += attr.isAdditions() ? " additions" : "";
			tag += attr.isSorted() ? " alphabetical" : "";
		} else if (attr.isReadableDate()) {
			tag = "displaydate";
		} else if (attr.isSearchableDate()) {
			tag = "searchdate";
		}
		return tag;
	}



	private static String getDisplayInput(AdminDescItem item, String allowance) {

		String id = String.valueOf(item.getID());
		String data = item.getData();
		if (item.isControlled()) {
			return getControlledDisplayInput(item, allowance);
		}

		data = StringEscapeUtils.escapeHtml(data);

		String smallInput = "<input class='ui-widget-content data-input' " 
			+ "data-tags='"+getHTMLTag(item)+"'"
			+ " type='text' value=\"" + data + "\" name='" + id + "' size='77' style='height:20px; margin-bottom: 20px;'" + allowance + "/><br/>";
		String bigInput = "<div class='bigtext'><textarea class='ui-widget-content data-input' "+ "data-tags='"+getHTMLTag(item)+"'"
		+ " rows='7' cols='74' name='"+ id+ "' "+ allowance + ">"+data+"</textarea></div>";
		return item.isLarge() ? bigInput : smallInput;
	}

	private static String getControlledDisplayInput(AdminDescItem item, String allowance) {
		String id = String.valueOf(item.getID());
		String data = item.getData();
		String tag = getHTMLTag(item);
		String vocab = "vocab='" + item.getVocab() + "'";
		String alpha = item.isSorted() ? " alpha " : "";
		
		//hack to unify background for disabled
		String background=(allowance.equals("") ? "" : " style='background: #bdbdbd ' ");

		if (item.isMultiple()) {
			Collection<String> dataCol = Arrays.asList(data.split(";"));
			StringBuilder bigInput = new StringBuilder("<ul name='"+id+"' class='controlled-collection "+allowance+" ' style='list-style-type: none; padding:0px; margin:0px;'>");
			
			for (String value : dataCol) {
				bigInput.append("<li class='autocomplete-entry "+allowance+" ' name='"+id+"'"+background);
				bigInput.append("<span class='dragHandle'></span>");
				bigInput.append("<input type='text' class='autocomplete data-input' "+ vocab+ " "
						+ tag + " name='"+ id + "' "+ allowance + " value='"+StringEscapeUtils.escapeHtml(value).trim().replace("'", "&apos;") +"' data-tags='" + tag + "'/>" 
						+ "<span class='ui-icon ui-icon-closethick controlled_handler "+alpha+" ' style='float:right;'></span><span class='show-all'></span></li>");
			}
			bigInput.append("<li class='autocomplete-entry "+allowance+" ' name='"+id+"'> " + "<span class='dragHandle'></span>" +
					"<input type='text' class='autocomplete data-input' "+ vocab+ " "
					+ tag + " name='"+ id + "' "+ allowance + " data-tags='" + 	tag + "'/>"
					+ "<span class='ui-icon ui-icon-plusthick controlled_handler "+alpha+" ' style='float:right;'></span><span class='show-all'></span></li>");
			bigInput.append("</ul>");
			return bigInput.toString();
		} else if (item.isAdditions()) {
			return "<ul name='"+id+"' style='list-style-type: none; padding:0px; margin:0px'><li class='autocomplete-entry "+allowance+"' name='"+id+"'"+background+">" + "<input type='text' class='autocomplete data-input' "+ vocab+ " "
			+ tag + " name='"+ id + "' "+ allowance + " value='" + StringEscapeUtils.escapeHtml(data.replace(';', '\r')).trim().replace("'", "&apos;") + "' data-tags='" + tag + "'/><span class='show-all'></span>" 
			+ "</li></ul>";
		} 
		return "<ul name='"+id+"' style='list-style-type: none; padding-left: 0px; margin-top: 0px'><li class='autocomplete-entry "+allowance+"' name='"+id+"'"+background+">" + "<input type='text' class='autocomplete data-input' "+ vocab+ " "
		+ tag + " name='"+ id + "' "+ allowance + " value='" + StringEscapeUtils.escapeHtml(data.replace(';', '\r')).trim().replace("'", "&apos;") + "' data-tags='" + tag + "'/><span class='show-all'></span>" 
		+ "</li></ul>";
	}

}
