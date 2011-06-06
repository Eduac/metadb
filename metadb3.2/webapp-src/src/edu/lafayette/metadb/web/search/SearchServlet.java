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
package edu.lafayette.metadb.web.search;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.AdminDescItem;
import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.search.SearchDAO;

/**
 * Servlet implementation class SearchServlet
 */
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
		JSONObject output = new JSONObject();
		ArrayList<String> tokens = new ArrayList<String>();
		try {
			String options = request.getParameter("search-options");
			String projname = "";
			String query = request.getParameter("query");
			if (options.equals("current"))
				projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
			if (query != null && !query.equals("")) {
				ArrayList<Item> resultList = SearchDAO.search(projname, query);
				tokens.addAll(MetaDbHelper.getStringTokens(query));
				output.put("size", resultList.size());
				if (resultList.isEmpty())
					output.put("data", "No entry found with query \""+query+"\"");
				else
					output.put("data", this.getFormattedResult(resultList, tokens));
			}
			else {
				output.put("data", "Please type in some keywords");
				output.put("size", 0);
			}
			output.put("queries", tokens);
			out.print(output);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}
	
	private JSONObject getFormattedResult(ArrayList<Item> results, ArrayList<String> tokens) {
		JSONObject output = new JSONObject();
		
		try {
			JSONArray thumb_urls = new JSONArray();
			JSONArray zoom_urls = new JSONArray();
			JSONArray projnames = new JSONArray();
			JSONArray indices = new JSONArray();
			JSONArray data = new JSONArray();
			for (Item item : results) {
				String projname = item.getProjname();
				int index = item.getItemNumber();
				String thumbPath = ItemsDAO.getThumbFilePath(projname, index);
				String thumbName = new File(thumbPath).getName();
				String thumbFilePath = Global.PATH_PROJECT + projname + "/" + thumbName;
				String mediumPath = new File(ItemsDAO.getZoomDerivPath(projname, index)).getName();
				String mediumFilePath = Global.PATH_PROJECT + projname + "/" + mediumPath;
				thumb_urls.put(thumbFilePath);
				zoom_urls.put(mediumFilePath);
				projnames.put(projname);
				indices.put(index);
				JSONArray attrs = new JSONArray();
				for (AdminDescItem adminDescData : item.search(tokens)) { 
					String label = adminDescData.getLabel();
					String attrData = "<p><span style='font-weight:bold'>"+adminDescData.getElement();
					if (!label.equals(""))
						attrData += "."+ label;
					attrData += ":</span>"+StringEscapeUtils.escapeHtml(adminDescData.getData())+"</p>";
					attrs.put(attrData);
				}
				data.put(attrs);
			}
			output.put("zoom_urls", zoom_urls);
			output.put("thumb_urls", thumb_urls);
			output.put("projectNames", projnames);
			output.put("indices", indices);
			output.put("data", data);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return output;
	}

}
