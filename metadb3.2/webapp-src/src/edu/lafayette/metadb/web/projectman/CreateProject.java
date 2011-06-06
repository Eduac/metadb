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
package edu.lafayette.metadb.web.projectman;

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
import org.json.JSONException;
import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.dataman.DataImporter;
import edu.lafayette.metadb.model.projects.ProjectsDAO;
import edu.lafayette.metadb.model.result.Result;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class CreateProject
 */
public class CreateProject extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		JSONObject output = new JSONObject();
		String delimiter = "";
		String projname = "";
		String projnotes = "";
		String template = "";
		boolean useTemplate = false;
		boolean disableQualifier = false;
		try {
			if (ServletFileUpload.isMultipartContent(request)) {
				ServletFileUpload servletFileUpload = new ServletFileUpload(
						new DiskFileItemFactory());
				List fileItemsList = servletFileUpload.parseRequest(request);
				DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
				diskFileItemFactory.setSizeThreshold(40960); /* the unit is bytes */

				Iterator it = fileItemsList.iterator();
				InputStream input = null;
				while (it.hasNext()) {
					FileItem fileItem = (FileItem) it.next();
					//MetaDbHelper.note("Field name: "+fileItem.getFieldName());
					//MetaDbHelper.note(fileItem.getString());
					if (fileItem.isFormField()) {
						/*
						 * The file item contains a simple name-value pair of a
						 * form field
						 */
						if (fileItem.getFieldName().equals("delimiter")
								&& delimiter.equals(""))
							delimiter = fileItem.getString();
						else if (fileItem.getFieldName().equals("projname")
								&& projname.equals(""))
							projname = fileItem.getString().replace(" ", "_");
						else if (fileItem.getFieldName().equals("projnotes")
								&& projnotes.equals(""))
							projnotes = fileItem.getString();
						else if (fileItem.getFieldName().equals("project-template-checkbox"))
							useTemplate = true;
						else if (fileItem.getFieldName().equals("project-template"))
							template = fileItem.getString().replace(" ", "_");
						//else if (fileItem.getFieldName().equals("disable-qualifier-checkbox"))
							//disableQualifier = true;
					} else
						input = fileItem.getInputStream();

				}
				String userName = Global.METADB_USER;
				
				HttpSession session = request.getSession(false);
				if (session != null) {
					userName = (String) session
							.getAttribute(Global.SESSION_USERNAME);
				}
				
				//MetaDbHelper.note("Delimiter: "+delimiter+", projname: "+projname+", projnotes: "+projnotes+", use template or not? "+useTemplate+" template: "+template);
				if (useTemplate) {
					ProjectsDAO.createProject(template, projname, projnotes, "");
					output.put("projname", projname);
					output.put("success", true);
					output.put("message","Project created successfully based on: "+template);
					SysLogDAO.log(userName, Global.SYSLOG_PROJECT,
							"User created project "
									+ projname+ " with template from "+template);
				} else {
					if (ProjectsDAO.createProject(projname, projnotes)) {
						output.put("projname", projname);
						String delimiterType = "csv";
						if (delimiter.equals("tab")) {
							delimiterType = "tsv";
							disableQualifier = true; // Disable text qualifiers for TSV files.
						}
						//MetaDbHelper.note("Delim: "+delimiterType+", projname: "+projname+", input: "+input);
						if (input != null) {
							Result res = DataImporter.importFile(delimiterType,
									projname, input, disableQualifier);
							output.put("success", res.isSuccess());
							if (res.isSuccess()) {
								output.put("message",
										"Data import successfully");
								SysLogDAO.log(userName, Global.SYSLOG_PROJECT,
										"User created project "
												+ projname+ " with file import");
							} else {
								
								output.put("message","The following fields have been changed");
								SysLogDAO.log(userName, Global.SYSLOG_PROJECT,
										"User created project "
												+ projname+ " with file import");
								String fields = "";
								ArrayList<String> data = (ArrayList<String>) res.getData();
								for (String field : data)
									fields += field + ',';
								output.put("fields", fields);
							}
						} else {
							output.put("success", true);
							output
									.put("message",
											"Project created successfully with default fields. No data imported");
							SysLogDAO.log(userName, Global.SYSLOG_PROJECT,
									"User created project "
											+ projname);
						}
					} else {
						output.put("success", false);
						output.put("message", "Cannot create project");
						SysLogDAO.log(userName, Global.SYSLOG_PROJECT,
								"User failed to create project "
										+ projname);
					}
				}
			} else {
				output.put("success", false);
				output.put("message", "Form is not multi-part");
			}
		} catch (NullPointerException e) {
			try {
				output.put("success", true);
				output.put("message", "Project created successfully");
			} catch (JSONException e1) {
				MetaDbHelper.logEvent(e1);
			}

		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
			try {
				output.put("success", false);
				output.put("message", "Project was not created");
			} catch (JSONException e1) {
				MetaDbHelper.logEvent(e1);
			}
		}
		out.print(output);
		out.flush();

	}

}
