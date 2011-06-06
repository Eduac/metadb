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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
//import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.Metadata;

/**
 * Servlet implementation class ShowImageMetadata
 */
public class ShowImageMetadata extends HttpServlet {
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
					int last = ItemsDAO.nextItemNumber(projname);
					int index = MetadataUIHelper.getNextIndex(projname, raw_index, current_item, direction);
					int size = last - 1;
					if (index > 0) { 
						
						out.print(ShowImageMetadata.getImageDisplay(projname, index, size));
					}
					else
						out.print("<div style='background-color:#DDDDDD; width:348px; height:348px'><p style='padding-top:170px'>Please parse master images<br> to generate derivatives</p></div>");
				}
				else
					out.print("Cannot authenticate username");
			}
			else
				out.print("Session doesn't exist, please login");
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
		
	}
	
	public static String getImageDisplay(String projname, int index, int size) {
		String thumbPath = ItemsDAO.getThumbFilePath(projname, index);
		String thumbName = new File(thumbPath).getName();
		String thumbFilePath = Global.PATH_PROJECT + projname + "/" + thumbName;
		String mediumPath = new File(ItemsDAO.getZoomDerivPath(projname, index)).getName();
		String mediumFilePath = Global.PATH_PROJECT + projname + "/" + mediumPath;
		
		ArrayList<Metadata> techList = ItemsDAO.getTechData(projname, index);
		int imgWidth = 0;
		int imgHeight = 0;
		for (Metadata m : techList) {
			if (m.getElement().equals(Global.TECH_AUTO_ELEMENT)) {
				if (m.getLabel().equals("PixelWidth"))
					imgWidth = Integer.parseInt(m.getData());
				else if (m.getLabel().equals("PixelHeight"))
					imgHeight = Integer.parseInt(m.getData());
			}
		}

		
		int scaledHeight = MetaDbHelper.getScaledDimensions(imgWidth, imgHeight, Global.IMAGE_DIMENSION, Global.IMAGE_DIMENSION)[1];
		int imgMargin = (Global.IMAGE_FRAME_HEIGHT - scaledHeight)/2;
		int imgMarginBottom = Global.IMAGE_FRAME_HEIGHT - imgMargin - scaledHeight;
		StringBuilder displayedItem = new StringBuilder();
		displayedItem.append("<div class='image-display' current='" + index + "' size='" + size + "' style='width:" + Global.IMAGE_FRAME_WIDTH + "px; background-color:#DDDDDD; padding-top:" + imgMargin + "px; padding-bottom:" + imgMarginBottom + "px; border: 1px solid #999999'>");
		displayedItem.append("<a target='_blank' class='big-image' title='" + mediumFilePath + "'>");
		displayedItem.append("<img style='max-width: "+Global.IMAGE_DIMENSION+"px; max-height: "+Global.IMAGE_DIMENSION+"px;' src='" + thumbFilePath +"'>");
		displayedItem.append("</a></div>");
		displayedItem.append("<div class='ui-widget-content' style='width:348px'>");
		displayedItem.append("Filename: "+ShowImageMetadata.getItemInfo(projname, index));
		displayedItem.append("</div>");
		displayedItem.append("</td>");
		return displayedItem.toString();
	}
	
	
	public static String getItemInfo(String projname, int index) {
		String[] path = ItemsDAO.getMasterPath(projname, index).split("/");
		String filename = path[path.length - 1];
		String itemInfo = filename != null ? filename + "<br/>" : "No filename";
		//itemInfo += checksum != null && filename != null ? "URL:<a></a>" : "";
		return itemInfo;
	}
	
}


