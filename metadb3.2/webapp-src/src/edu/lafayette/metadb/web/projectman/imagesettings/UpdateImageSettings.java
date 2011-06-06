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
package edu.lafayette.metadb.web.projectman.imagesettings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.imagemgt.DerivativeDAO;
import edu.lafayette.metadb.model.projects.ProjectsDAO;

/**
 * Servlet implementation class UpdateImageSettings
 */
public class UpdateImageSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String projname = (String) request.getSession(false).getAttribute(
				Global.SESSION_PROJECT);
		String status = "Update Successfully";
		try {
			DerivativeDAO.updateDerivativeSetting(projname,
					Global.DERIV_ZOOM_SETTING, Integer.parseInt(request
							.getParameter("zoom-width")), Integer
							.parseInt(request.getParameter("zoom-height")), "",
					"", 0, true);

			if (request.getParameter("derivative-custom") != null) {
				int width = Integer.parseInt(request
						.getParameter("custom-width")); 
				int height = Integer.parseInt(request
						.getParameter("custom-height"));
				int brand = this.convertBrand(request
						.getParameter("custom-brand-radio"));
				String background = request.getParameter("custom-background");
				String text_color = request.getParameter("custom-text");
				DerivativeDAO.updateDerivativeSetting(projname,
						Global.DERIV_CUSTOM_SETTING, width, height, background,
						text_color, brand, true);
			} else
				DerivativeDAO.updateDerivativeSetting(projname,
						Global.DERIV_CUSTOM_SETTING, 0, 0, "", "", 0, false);

			if (request.getParameter("derivative-fullsize") != null) {
				int brand = this.convertBrand(request
						.getParameter("fullsize-brand-radio"));
				String background = request.getParameter("fullsize-background");
				String text_color = request.getParameter("fullsize-text");
				DerivativeDAO.updateDerivativeSetting(projname,
						Global.DERIV_FULLSIZE_SETTING, 0, 0, background,
						text_color, brand, true);
			} else
				DerivativeDAO.updateDerivativeSetting(projname,
						Global.DERIV_FULLSIZE_SETTING, 0, 0, "", "", 0, false);

			String brand_text = request.getParameter("branding-text");
			if (!DerivativeDAO.updateBrand(projname, brand_text))
				status = "Cannot update brand "+brand_text+"\n";
			
			
//			String hostname = request.getParameter("zoom-hostname");
//			if (!ProjectsDAO.updateURL(projname, hostname)) {
//				status += "Cannot update hostname "+hostname+"\n";
//			}
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
			status = "Image Settings update failure! Please correctly fill in all fields";
		}
		//MetaDbHelper.note(status);
		out.print(status);
		out.flush();
	}

	private int convertBrand(String brand) {
		if (brand.toLowerCase().equals("none"))
			return 0;
		else if (brand.toLowerCase().equals("under"))
			return 1;
		else if (brand.toLowerCase().equals("over"))
			return 2;
		return 0;
	}

}
