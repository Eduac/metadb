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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.csvreader.CsvWriter;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.dataman.DataExporter;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class ExportAdminDesc
 */
public class ExportAdminDesc extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {

			String projname = (String) request.getSession(false).getAttribute(Global.SESSION_PROJECT);
			String delimiterType = request.getParameter("delimiter");
			String indices = request.getParameter("export-images");
			boolean technical = request.getParameter("technical") != null;
			boolean replaceEntity = request.getParameter("replace-entity") != null;
			boolean disableQualifier = false; //request.getParameter("disable-qualifier") != null;
			String encoder = (request.getParameter("html-entities") != null ) ? "ISO-8859-1" : "UTF-8";

			char delimiter = ',';
			String filename_ext = ".csv";
			
			//MetaDbHelper.note("Project "+projname+", indices: "+indices+" ");
			
			if (delimiterType.equals("tab")) {
				delimiter = '\t';
				filename_ext = ".txt";
				disableQualifier = true;
			}

			File tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
			// create a temporary file in that directory
			File tempFile = File.createTempFile( projname, filename_ext, tempDir );
			CsvWriter writer = new CsvWriter(new FileWriter(tempFile, true), delimiter);
			if(disableQualifier)
				writer.setUseTextQualifier(false);
			
			// feed in your array (or convert your data to an array)
			String[] headers = DataExporter.exportAttributes(projname, delimiter, encoder, technical);
			String headerStr = "";
			for(String h: headers)
				headerStr+= " "+h;
			//MetaDbHelper.note(headerStr);
			writer.writeRecord(headers);

			//Begin writing
			Set<Integer> range = MetaDbHelper.filterRecords(indices);
			for (int i : range)
				writer.writeRecord(DataExporter.exportData(projname, i, delimiter, encoder, technical, replaceEntity));
			writer.flush();
			writer.close();

			int                 length   = 0;
			ServletOutputStream op       = response.getOutputStream();
			ServletContext      context  = getServletConfig().getServletContext();
			String              mimetype = context.getMimeType( tempFile.getName() );


			response.setContentType( (mimetype != null) ? mimetype : "application/x-download" );
			response.setContentLength( (int)tempFile.length() );
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + projname + filename_ext + "\"" );
			byte[] bbuf = new byte[1024];
			DataInputStream in = new DataInputStream(new FileInputStream(tempFile));

			while ((in != null) && ((length = in.read(bbuf)) != -1))
			{
				op.write(bbuf,0,length);
			}

			in.close();
			op.flush();
			op.close();
			if (tempFile.delete()) {
				//MetaDbHelper.note("Temp export file deleted");
			}

			HttpSession session = request.getSession(false);
			if(session!=null)
			{
				String userName=(String)session.getAttribute("username");
				SysLogDAO.log(userName, Global.SYSLOG_PROJECT, "Data exported from project "+projname);
			}

		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
			//MetaDbHelper.logEvent("ExportAdminDesc", "doPost", getServletContext().getAttribute("javax.servlet.context.tempdir").toString());
		}

	}
}
