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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.imagemgt.DerivativeDAO;
import edu.lafayette.metadb.model.imagemgt.DerivativeSetting;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.Metadata;

/**
 * Servlet implementation class ShowAvailableSizes
 */
public class ShowAvailableSizes extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
		try {
			int itemNumber = Integer.parseInt(request.getParameter("current"));
			StringBuilder data = new StringBuilder("<div style='display: inline-block;'>Available Sizes:<br/>&nbsp</div>");
			String pathPrefix = Global.PATH_PROJECT + projname + "/";
			

			ArrayList<Metadata> techList = ItemsDAO.getTechData(projname, itemNumber);
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
			int scaledWidth = imgWidth;
			int scaledHeight = imgHeight;
			//MetaDbHelper.note("Image technical data: width="+imgWidth+", height="+imgHeight);
			String filename = "";
			int[] scaledDims;
			DerivativeSetting deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_CUSTOM_SETTING);
			//MetaDbHelper.note("Custom setting get successful: "+(deriv!=null)+". Setting: "+deriv.getMaxHeight()+" max height");
			if (deriv != null) {
				filename = pathPrefix + new File(ItemsDAO.getCustomDerivPath(projname, itemNumber)).getName();
				scaledDims = MetaDbHelper.getScaledDimensions(imgWidth, imgHeight, deriv.getMaxWidth(), deriv.getMaxHeight()); 
				//MetaDbHelper.note("Scaled dimensions: "+scaledDims[0]+"x"+scaledDims[1]);
				scaledWidth = scaledDims[0];
				scaledHeight = scaledDims[1];
				data.append(deriv.isEnabled() ? "<div style='display:inline-block; padding-right:3%; padding-left: 3%; border-right: solid 1px'><a target='_blank' href='"+ filename +"'>Medium</a><br/>"+ scaledWidth +"x"+ scaledHeight +"</div>" : "");
			}
			
			deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_ZOOM_SETTING);
			//MetaDbHelper.note("Zoom setting get successful: "+(deriv!=null)+". Setting: "+deriv.getMaxHeight()+" max height");
			
			if (deriv != null) {
				filename = pathPrefix + new File(ItemsDAO.getZoomDerivPath(projname, itemNumber)).getName();
				scaledDims = MetaDbHelper.getScaledDimensions(imgWidth, imgHeight, deriv.getMaxWidth(), deriv.getMaxHeight());
				//MetaDbHelper.note("Scaled dimensions: "+scaledDims[0]+"x"+scaledDims[1]);
				//MetaDbHelper.note(imgWidth+" "+imgHeight+" "+deriv.getMaxWidth()+ " "+deriv.getMaxHeight()+" "+scaledDims[0]+ " "+scaledDims[1]);
				scaledWidth = scaledDims[0];
				scaledHeight = scaledDims[1];
				data.append(deriv.isEnabled() ? "<div style='display:inline-block; padding-right:3%; padding-left: 3%; border-right: solid 1px'><a target='_blank' href='"+ filename +"'>Large</a><br/>"+ scaledWidth +"x"+ scaledHeight +"</div>" : "");
			}
			
			deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_FULLSIZE_SETTING);
			//MetaDbHelper.note("Fullsize setting get successful: "+(deriv!=null)+". Setting: "+deriv.getMaxHeight()+" max height");

			if (deriv != null) {
				filename = pathPrefix + new File(ItemsDAO.getFullDerivFilePath(projname, itemNumber)).getName();
				scaledWidth = imgWidth;
				scaledHeight = imgHeight;
				data.append(deriv.isEnabled() ? "<div style='display:inline-block; padding-left: 3%'><a target='_blank' href='"+ filename +"'>Fullsize</a><br/>"+ scaledWidth +"x"+ scaledHeight +"</div>" : "");
			}
			
			out.print(data);
		} catch (NumberFormatException e) {
 
		} catch (Exception e) {
			//MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
