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
package edu.lafayette.metadb.web.dataman;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.dataman.DataImporter;
import edu.lafayette.metadb.model.result.Result;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class ImportAdminDesc
 */
public class ImportAdminDesc extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String delimiter = "comma";
		boolean replaceEntity = false;
		String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
		JSONObject output = new JSONObject();
		
		try {
			if (ServletFileUpload.isMultipartContent(request)){
				ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
				List fileItemsList = servletFileUpload.parseRequest(request);
				DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
				diskFileItemFactory.setSizeThreshold(40960); /* the unit is bytes */

				
				Iterator it = fileItemsList.iterator();
				InputStream input = null;
				while (it.hasNext()){
					FileItem fileItem = (FileItem)it.next();
					
					if (fileItem.isFormField()) {
						/* The file item contains a simple name-value pair of a form field */
						if(fileItem.getFieldName().equals("delimiter"))
							delimiter = fileItem.getString();
						else if(fileItem.getFieldName().equals("replace-entity"))
							replaceEntity = true;
					}
					else {
						input = fileItem.getInputStream();
					}
				}
				
				String delimiterType = "csv";
			  	if (delimiter.equals("tab")) {
			  		delimiterType = "tsv";
			  	}
			  	if (input != null) {
			  		Result res = DataImporter.importFile(delimiterType, projname, input, replaceEntity);
				  	if (res.isSuccess()) {
				  		HttpSession session = request.getSession(false);
	      				if(session != null) {
	      					String userName=(String)session.getAttribute(Global.SESSION_USERNAME);
	      					SysLogDAO.log(userName, Global.SYSLOG_PROJECT, "Data imported into project "+projname);
	      				}
	      				output.put("message", "Data import successfully");
				  	}
				  	else {
				  		output.put("message", "The following fields have been changed:");
				  		StringBuilder fields = new StringBuilder();
				  		for (String field:(ArrayList<String>)res.getData())
				  			fields.append(field+',');
				  		output.put("fields",fields.toString());
				  	}
				  	output.put("success", res.isSuccess());
			  	}
			  	else {
			  		output.put("success", false);
			  		output.put("message", "Null data");
			  	}
			}
			else {
				output.put("success", false);
		  		output.put("message", "Form is not multi-part");
			}				
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.print(output);
		out.flush();
	}

}
