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

import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class UpdateVocab
 */
public class UpdateVocab extends HttpServlet {
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
				status = "isMultiPart";
				ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
				List fileItemsList = servletFileUpload.parseRequest(request);
				DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
				diskFileItemFactory.setSizeThreshold(40960); /* the unit is bytes */
				
				InputStream input = null;
				Iterator it = fileItemsList.iterator();
				String result = "";
				String vocabs = null;
				int assigned = -1;
				
				while (it.hasNext()){
					FileItem fileItem = (FileItem)it.next();
					result += "UpdateVocab: Form Field: " + fileItem.isFormField() + " Field name: " + fileItem.getFieldName() + " Name: " + fileItem.getName() + " String: " + fileItem.getString("utf-8") + "\n";
					if (fileItem.isFormField()) {
						/* The file item contains a simple name-value pair of a form field */
						if (fileItem.getFieldName().equals("vocab-name"))
							vocabName = fileItem.getString();
						else if (fileItem.getFieldName().equals("vocab-terms"))
							vocabs = fileItem.getString("utf-8");
						else if (fileItem.getFieldName().equals("assigned-field"))
							assigned = Integer.parseInt(fileItem.getString());
					}
					else{
						if (fileItem.getString() != null && !fileItem.getString().equals("")) {
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
						//MetaDbHelper.note(vocabs);
						for (String vocabTerm : vocabs.split("\n"))
							vocabList.add(vocabTerm);
					}
					if (!vocabList.isEmpty()) {
						boolean updated = ControlledVocabDAO.addControlledVocab(vocabName, vocabList) ||
						ControlledVocabDAO.updateControlledVocab(vocabName, vocabList);
						if (updated) {
							status = "Vocab " + vocabName + " updated successfully ";
							if (assigned != -1)
								if (!AdminDescAttributesDAO.setControlledVocab(assigned, vocabName)) {
									status = "Vocab " + vocabName + " cannot be assigned to "+assigned;
								}
									
						}
						else
							status = "Vocab " + vocabName + " cannot be updated/created";
					} else
						status = "Vocab " + vocabName + " has empty vocabList";
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
