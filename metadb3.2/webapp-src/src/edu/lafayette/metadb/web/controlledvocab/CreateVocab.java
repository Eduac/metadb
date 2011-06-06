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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class CreateVocab
 */
public class CreateVocab extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String vocabName = null;
		String name="nothing";
		String status = "Upload failed ";

		try {
			
			if (ServletFileUpload.isMultipartContent(request)){
				name="isMultiPart";
				ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
				List fileItemsList = servletFileUpload.parseRequest(request);
				DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
				diskFileItemFactory.setSizeThreshold(40960); /* the unit is bytes */
				
				InputStream input = null;
				Iterator it = fileItemsList.iterator();
				String result = "";
				String vocabs = null;
				
				while (it.hasNext()){
					FileItem fileItem = (FileItem)it.next();
					result += "CreateVocab: Form Field: " + fileItem.isFormField() + " Field name: " + fileItem.getFieldName() + " Name: " + fileItem.getName() + " String: " + fileItem.getString() + "\n";
					if (fileItem.isFormField()) {
						/* The file item contains a simple name-value pair of a form field */
						if (fileItem.getFieldName().equals("vocab-name"))
							vocabName = fileItem.getString();
						else if (fileItem.getFieldName().equals("vocab-terms"))
							vocabs = fileItem.getString();
					}
					else{

						@SuppressWarnings("unused")
						String content="nothing";
						/* The file item contains an uploaded file */
					  
						/* Create new File object
						File uploadedFile = new File("test.txt");
						if(!uploadedFile.exists())
							uploadedFile.createNewFile();
						// Write the uploaded file to the system
						fileItem.write(uploadedFile);
						*/
					  	name = fileItem.getName();
					  	content = fileItem.getContentType();
					  	input = fileItem.getInputStream();
					}
				}
				//MetaDbHelper.note(result);
				if (vocabName != null) {
					Set<String> vocabList = new TreeSet<String>();
					if (input != null) {
						Scanner fileSc=new Scanner(input);
						while(fileSc.hasNextLine())
						{
							String vocabEntry=fileSc.nextLine();
							vocabList.add(vocabEntry.trim());
						}
							
						
						HttpSession session = request.getSession(false);
						if(session!=null)
		      				{
		      					String userName=(String)session.getAttribute("username");
		      					SysLogDAO.log(userName, Global.SYSLOG_PROJECT, "User "+userName+" created vocab "+vocabName);
		      				}
					  		status = "Vocab name: "+vocabName+". File name: "+name+"\n";
						
					 }
					else {
	//					status = "Form is not multi-part";
	//					vocabName = request.getParameter("vocab-name");
	//					String vocabs = request.getParameter("vocab-terms");
						MetaDbHelper.note(vocabs);
						for (String vocab : vocabs.split("\n"))
							vocabList.add(vocab);
					}
					if (!vocabList.isEmpty()) {
						if (ControlledVocabDAO.addControlledVocab(vocabName, vocabList))
							status = "Vocab " + vocabName + " created successfully";
						else if (ControlledVocabDAO.updateControlledVocab(vocabName, vocabList))
							status = "Vocab " + vocabName + " updated successfully ";
						else
							status = "Vocab " + vocabName + " cannot be updated/created";
					}
				}
			}
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		MetaDbHelper.note(status);
		out.print(status);
		out.flush();
	}

}
