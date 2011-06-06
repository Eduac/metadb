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


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;
import edu.lafayette.metadb.model.items.AdminDescItem;
import edu.lafayette.metadb.model.items.AdminDescItemsDAO;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.AdminDescDataDAO;

/**
 * Servlet implementation class UpdateAdminDescMetadata
 */
public class UpdateAdminDescMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"), true);
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		JSONObject output = new JSONObject();			

		String type = request.getParameter("type");
		String message = type + " metadata cannot be updated\n";
	
		try {
			HttpSession session = request.getSession(false);
			if (session == null)
				return;
			String projname = (String) session
					.getAttribute(Global.SESSION_PROJECT);
			String userName=(String)session.getAttribute("username");
			int itemNumber = Integer.parseInt(request.getParameter("current"));

			String locker=ItemsDAO.getLocker(projname, itemNumber);
			if(!(userName.equals(locker) || locker.trim().equals("") || locker == null))
				return;
		
			output.put("projname", projname);
			String[] ids_array = request.getParameter("edited-ids").split(" ");
			Set<String> ids = new HashSet<String>();
			for (String id : ids_array) {
				ids.add(id);
			}

			
			JSONArray failed_items = new JSONArray();
			message = "Metadata exist and will be updated\n";
			for (String id_string : ids) {
				int id = Integer.parseInt(id_string);
				JSONObject whitelisted = whitelist(id_string, projname, type, itemNumber, request);
				
				if (whitelisted.getBoolean("success")) {
					MetaDbHelper.note("Whitelist succeeded, proj "+projname+", item "+itemNumber+", id "+id+", data "+whitelisted.getString("data"));
					AdminDescDataDAO.updateAdminDescData(projname,
							itemNumber, id, whitelisted.getString("data").trim());
					
				}
				else {
					MetaDbHelper.note("Didn't pass whitelist");
					failed_items.put(whitelisted);
				}
					

			}
			output.put("failure", failed_items);
			output.put("message", message);
			output.put("type", type);
		} catch (NumberFormatException e) {
			//No problem, when splitting string last element is "", thus cannot be
			//converted to numbers
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.print(output);
		out.flush();
	}
	
	
	public static JSONObject whitelist(String id_string, String projname, String type, int itemNumber, HttpServletRequest request) throws JSONException {
		
		JSONObject result = new JSONObject();
		
		int id = Integer.parseInt(id_string);
		
		AdminDescItem item = AdminDescItemsDAO.getItem(projname,
				type, id, itemNumber);
		boolean validated = true;
		String newData = request.getParameter(id_string);
		
		
		
		String error_message = "Cannot update metadata";
		
		if (newData != null) {
			if (item.isReadableDate()) {
				validated &= isValidDisplayDate(newData);
				error_message = "Display date is not valid";
			} 
			else if (item.isSearchableDate()) {
				validated &= isValidSearchDate(newData);
				error_message = "Search date is not valid";
			}
			else if (item.isControlled()) {
				newData = "";
				String[] new_terms = request.getParameterValues(id_string);
				if (item.isAdditions()) {
					addTerm(item, new_terms);
				}
				
				
				/**
				 * Important: controlled vocab terms are sorted before updating the database
				 * To disable sorting, use another data structure since Set does not preserve order,
				 * only maintains uniqueness of elements.
				 */
				Set<String> vocabList = ControlledVocabDAO.getControlledVocab(item.getVocab());
				Collection<String> updatedVocab;
				if (item.isSorted())	//if sorted, use TreeSet
					updatedVocab = new TreeSet<String>();
				else					//otherwise, use LinkedList
					updatedVocab = new LinkedList<String>();
				for (int i = 0; i < new_terms.length; i++) {
					String term = StringUtils.strip(new_terms[i]);
					//MetaDbHelper.note(term);
					if (!term.equals("") && !vocabList.add(term))
						updatedVocab.add(term);
				}
				int i = 0;
				
				Iterator<String> itr = updatedVocab.iterator();
				while (itr.hasNext()) {
					newData += itr.next();
					if (i != updatedVocab.size() - 1)
						newData += ";";
					i++;
				}
			}
			if (validated) {
				result.put("success", true);
				result.put("data", newData);
				
			}
			else {
				result.put("success", false);
				result.put("id", id);
				result.put("element", item.getElement());
				result.put("label", item.getLabel());
				result.put("message", error_message);
			}
		}
		return result;
	}
	
	private static boolean addTerm(AdminDescItem item, String[] new_terms) {
		String vocabName = item.getVocab();
		Set<String> newVocab = ControlledVocabDAO
				.getControlledVocab(vocabName);
		for (String term : new_terms) {
			String added_term = StringUtils.strip(term);
			if (!added_term.equals("")) {
				//MetaDbHelper.note("Additions item, adding "+added_term+" to "+vocabName);
				newVocab.add(added_term);
			}
		}
		return ControlledVocabDAO.updateControlledVocab(vocabName,
			newVocab);
	}
	
	private static boolean isValidDisplayDate(String value) {
		//must be at least yyyy
		if (value.length() == 0) return true;
		if (value.length() < 4)
			return false;
		return isValidSearchDate(value) ||
				(Pattern.matches("\\d\\d\\d\\ds", value)) ||
				(Pattern.matches("\\d\\d\\d\\d or \\d\\d\\d\\d", value)) ||
				(Pattern.matches("(after|before) \\d\\d\\d\\d", value)) ||
				(Pattern.matches("circa \\d\\d\\d\\d(([-](0[1-9]|1[012]))?)", value));
		
	}
	
	private static boolean isValidSearchDate(String value) {
		//must be at least yyyy
		if (value.length() == 0) return true;
		if (value.length() < 4)
			return false;
		return (Pattern.matches("\\d\\d\\d\\d(([-](0[1-9]|1[012])(([-](0[1-9]|[12][0-9]|3[01]))?))?)", value)) ||
				(Pattern.matches("\\d\\d\\d\\d[-]\\d\\d\\d\\d", value));
	}
	
}

