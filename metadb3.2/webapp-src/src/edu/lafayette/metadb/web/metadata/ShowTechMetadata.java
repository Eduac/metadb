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
import java.util.Iterator;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.AdminDescItem;
import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.Metadata;

/**
 * Servlet implementation class ShowTechMetadata
 */
@SuppressWarnings("unused")
public class ShowTechMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
		
		String raw_index = request.getParameter("item-index");
		String current_item = request.getParameter("current");
		String direction = request.getParameter("direction");
		
		try {
			HttpSession session = request.getSession(false);
			if (session!=null) {
				String username = (String) session.getAttribute("username");
				String projname = (String) session.getAttribute(Global.SESSION_PROJECT);
				if (username != null && !username.equals("") && 
						projname != null && !projname.equals("")) {
					int dest = MetadataUIHelper.getNextIndex(projname, raw_index, current_item, direction);
					ArrayList<Metadata> techList = ItemsDAO.getTechData(projname, dest);
					out.print(this.getData(projname, dest, techList));
				}
				else
					out.print("Cannot authenticate username and/or project name");
			}
			else
				out.print("Session doesn't exist, please login");
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
		
	}
	
	private String getData(String projname, int current, ArrayList<Metadata> techList) {
		String metadata = "";
		try {
			if (techList != null && !techList.isEmpty()) {
				int size = ItemsDAO.nextItemNumber(projname)-1;
				metadata = this.getMetadataTable(size, current, techList);
			}
			else
				metadata = "Please parse images to generate technical metadata";
		}
		catch (Exception e) {
			metadata = "Error retrieving data<br/>";
			metadata += e.toString();
		}
		return metadata;
	}
	
	/**
	 * 
	 * @param projname
	 * @param type
	 * @param index
	 * @param dataNumberList
	 * @param itemList
	 * @return
	 */
	private String getMetadataTable(int size, int current, ArrayList<Metadata> techList) {
		StringBuilder metadata = new StringBuilder();
		metadata.append("<form id='technical-metadata' current='" + current + "' method='post' style='margin: 0px 0px 0px 0px'>");
		metadata.append("<br>");
		metadata.append("<table>");
		metadata.append("<thead><tr>");
		metadata.append("<th>Attributes</th>");
		metadata.append("</thead><tbody>");
		if (!techList.isEmpty()) {
			for (Metadata m : techList) {
				String attribute = m.getElement() + "." + m.getLabel();
				metadata.append("<tr align='left'>");
				metadata.append("<td>");
				metadata.append(attribute+":<br/>");
				metadata.append("</td>");
				metadata.append("<td>");
				metadata.append("</td>");
				metadata.append("<tr>");
				metadata.append("<td style='padding-left:25px'>");
				String data = m.getData();
				if (data.length() >= 40)
					metadata.append("<textarea class='ui-widget-content' rows='2' cols='78' style='background:#BDBDBD; color:black;' disabled>"+data+"</textarea><br/>");
				else
					metadata.append("<input class='ui-widget-content' type='text' value='"+data+"' size='80' disabled/><br/>");
				metadata.append("</td>");
				metadata.append("</tr>");
				metadata.append("<br/>");
			}
			metadata.append("</tbody>");
			metadata.append("</table>");
			metadata.append("</form>");
		}
		else
			metadata=new StringBuilder("No technical attributes in the project");
		return metadata.toString();
	}

}
