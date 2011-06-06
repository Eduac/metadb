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
package edu.lafayette.metadb.web.controlledvocab;

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

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;


/**
 * Servlet implementation class ExportVocab
 */
public class ExportVocab extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			
			String vocabName = request.getParameter("vocab-name");
			String filename_ext = ".txt";
			
			File tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
			//MetaDbHelper.note(tempDir.getCanonicalPath());
			// create a temporary file in that directory
			File tempFile = File.createTempFile( vocabName, filename_ext, tempDir );
			FileWriter fw = new FileWriter(tempFile);
			Set<String> vocabList = ControlledVocabDAO.getControlledVocab(vocabName);
			for (String v : vocabList)
				fw.write(v + "\r\n");
			fw.flush();
			fw.close();
			
			int                 length   = 0;
			ServletOutputStream op       = response.getOutputStream();
			ServletContext      context  = getServletConfig().getServletContext();
			String              mimetype = context.getMimeType( tempFile.getName() );
	
	
			response.setContentType( (mimetype != null) ? mimetype : "application/x-download" );
			response.setContentLength( (int)tempFile.length() );
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + vocabName + filename_ext + "\"" );
			//MetaDbHelper.note(vocabName + filename_ext);
			byte[] bbuf = new byte[1024];
			DataInputStream in = new DataInputStream(new FileInputStream(tempFile));
	
			while ((in != null) && ((length = in.read(bbuf)) != -1))
			{
				op.write(bbuf,0,length);
			}
	
			in.close();
			op.flush();
			op.close();
			if (tempFile.delete())
			{
				//MetaDbHelper.note("Temp export file deleted");
			}
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
			//MetaDbHelper.logEvent("ExportVocab", "doPost", getServletContext().getAttribute("javax.servlet.context.tempdir").toString());
		}
	}

}
