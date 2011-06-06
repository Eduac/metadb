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
package edu.lafayette.metadb.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;

/**
 * Class to retrieve file paths for displaying thumbnails and zoom images in the UI.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class ImageZoom extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageZoom() {
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
		PrintWriter out = response.getWriter();
		JSONObject output = new JSONObject();
		
		try {
			
			String[] item = request.getParameter("item").split("-");
			int index = Integer.parseInt(item[item.length - 1]);
			String projname ="";
			for (int i = 0; i < item.length - 1; i++) {
				projname += item[i];
				if (i != item.length - 2)
					projname += '-';
			}
			String thumbPath = ItemsDAO.getThumbFilePath(projname, index);
			String thumbName = new File(thumbPath).getName();
			String thumbFilePath = Global.PATH_PROJECT + projname + "/" + thumbName;
			String mediumPath = new File(ItemsDAO.getZoomDerivPath(projname, index)).getName();
			String mediumFilePath = Global.PATH_PROJECT + projname + "/" + mediumPath;
			
			output.put("thumb", thumbFilePath);
			output.put("zoom", mediumFilePath);
		} catch (NumberFormatException e) {
			// No item number for zoom
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.print(output);
		out.close();
	}

}
