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
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.imagemgt.DerivativeDAO;
import edu.lafayette.metadb.model.imagemgt.DerivativeSetting;
import edu.lafayette.metadb.model.projects.ProjectsDAO;

/**
 * Servlet implementation class ShowDerivativeSettings
 */
@SuppressWarnings("unchecked")
public class ShowDerivativeSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
		JSONObject output = new JSONObject();
		try {
			DerivativeSetting deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_THUMB_SETTING);
			output.put(Global.DERIV_THUMB_SETTING, this.extractDerivSettings(deriv));
			
			deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_CUSTOM_SETTING);
			output.put(Global.DERIV_CUSTOM_SETTING, this.extractDerivSettings(deriv));
			
			deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_ZOOM_SETTING);
			output.put("zoom", this.extractDerivSettings(deriv));
			
			deriv = DerivativeDAO.getDerivativeSetting(projname, Global.DERIV_FULLSIZE_SETTING);
			output.put(Global.DERIV_FULLSIZE_SETTING, this.extractDerivSettings(deriv));
			
			output.put("hostname", ProjectsDAO.getProjectData(projname).getBaseUrl());
		} catch (JSONException e) {
			MetaDbHelper.logEvent(this.getClass().toString(), "doPost", MetaDbHelper.getStackTrace(e));
		}
		response.setContentType("application/x-json");
		out.print(output);
	}
	
	private JSONObject extractDerivSettings(DerivativeSetting deriv) {
		HashMap map = new HashMap();
		map.put("enabled", deriv.isEnabled());
		map.put("height", deriv.getMaxHeight());
		map.put("width", deriv.getMaxWidth());
		map.put("brand_mode", deriv.getAnnotationMode());
		map.put("brand_text", deriv.getBrand());
		map.put("text_color", deriv.getFgColor());
		map.put("background_color", deriv.getBgColor());
		return new JSONObject(map);
	}
}
