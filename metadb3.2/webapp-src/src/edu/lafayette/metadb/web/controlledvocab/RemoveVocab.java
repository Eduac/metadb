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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet implementation class RemoveVocab
 */
public class RemoveVocab extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String vocabName = request.getParameter("vocab-name");
		try {
			if (AdminDescAttributesDAO.emptyControlledVocab(vocabName)) { 
				if (ControlledVocabDAO.removeControlledVocab(vocabName))
					{
						HttpSession session = request.getSession(false);
						if(session!=null)
						{
							String userName=(String)session.getAttribute("username");
      						SysLogDAO.log(userName, Global.SYSLOG_PROJECT, "User "+userName+" deleted vocab "+vocabName);
						}
					out.print("Vocab "+vocabName+" deleted successfully");
					}
					
			}
			else
				out.print("Vocab "+vocabName+" cannot be deleted");
			
		}
		catch(Exception e) {
			MetaDbHelper.logEvent(this.getClass().toString(), e);
		}
		out.flush();
	}

}
