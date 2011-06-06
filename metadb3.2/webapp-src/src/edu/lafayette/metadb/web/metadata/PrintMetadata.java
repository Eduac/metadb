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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.AdminDescItem;
import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.Metadata;

/**
 * Servlet implementation class PrintMetadata
 */
public class PrintMetadata extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PrintMetadata() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String output = "";
		try {
			String projname = request.getParameter("projname");
			int itemNumber = Integer.parseInt(request
					.getParameter("item-number"));
			output = this.getTemplate(projname, itemNumber);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.println(output);
	}
	
	protected String getTemplate(String projname, int index) {
		StringBuilder template = new StringBuilder();
		
		template.append("<div style='width:95%'>");
		template.append("<table align='left' cellpadding='10' style='width:100%'>");
		template.append("<tbody>");
		template.append("<tr valign='top'>");
		template.append("<td id='image-holder' width='310px' align='center' style='width:310px'>");
		
		String thumbPath = ItemsDAO.getThumbFilePath(projname, index);
		String thumbName = new File(thumbPath).getName();
		String thumbFilePath = Global.PATH_PROJECT + projname + "/" + thumbName;
		template.append("<img style='max-width: 300px; max-height: 300px;' src='" + thumbFilePath +"'>");
		
		template.append("</td>");
		template.append("<td align='center'>");
		template.append("<p><span style='font-weight: bold;'>Project</span>:<br/>"+projname+"</p>");
		template.append("<p><span style='font-weight: bold;'>Item Number</span>:<br/>"+index+"</p>");
		template.append("<p><span style='font-weight: bold;'>Filename</span>:<br/>"+ShowImageMetadata.getItemInfo(projname, index)+"</p>");
		template.append("</td>");
		template.append("</tr>");
		template.append("</tbody>");
		template.append("</table>");
		template.append("</div><br/>");
		template.append("<div style='width:95%; margin-top:310px'>");
		
		
		Item item = ItemsDAO.getItem(projname, index);
		
		ArrayList<AdminDescItem> mdList = item.getData(Global.MD_TYPE_DESC);
		if (!mdList.isEmpty()) {
			template.append("<div><h3 style='text-decoration: underline;'>Descriptive Metadata</h3>");
			for (AdminDescItem m: mdList)
				template.append(this.getMetadataDisplay(m.getMetadata()));
			template.append("</div>");
		}
		
		mdList = item.getData(Global.MD_TYPE_ADMIN);
		if (!mdList.isEmpty()) {
			template.append("<div><h3 style='text-decoration: underline;'>Administrative Metadata</h3>");
			for (AdminDescItem m: mdList)
				template.append(this.getMetadataDisplay(m.getMetadata()));
			template.append("</div>");
		}
		
		template.append("<div><h3 style='text-decoration: underline;'>Technical Metadata</h3>");
		List<Metadata> techMD = ItemsDAO.getTechData(projname, index);
		for (Metadata m: techMD)
			template.append(this.getMetadataDisplay(m));
		template.append("</div>");
		
		template.append("</div>");
		return template.toString();
	}
	
	private String getMetadataDisplay(Metadata m) {
		String attribute = m.getElement() + "." + m.getLabel();
		return "<p><span style='font-weight: bold; padding-left:25px'>"+attribute+"</span>:</p>" +
				"<p style='padding-left:50px; margin-top:-3px'>" +StringEscapeUtils.escapeHtml(m.getData())+"</p>";
	}
	

}
