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
package edu.lafayette.metadb.web;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.syslog.Event;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Servlet to retrieve and display the system log for a certain type.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
public class ShowLog extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		String eventType = request.getParameter("log-type");
		out.println("<H1>MetaDB "+eventType+" Log</H1>");
		out.println("<P>");
		out.println("<TABLE>");
		out.println("<TR >" +
				"		<TD style='border: thin solid;'>" +
				"			<B>User</B>" +
				"		</TD>" +
				"		<TD style='border: thin solid;'>" +
				"			<B>Event Time</B>" +
				"		</TD>" +
				"		<TD style='border: thin solid;'>" +
				"			<B>Event Type</B>" +
				"		</TD>" +
				"		<TD style='border: thin solid;'>" +
				"			<B>Event Content</B>" +
				"		</TD>" +
				"	</TR>");
		try {
			
			ArrayList<Event> entryList=SysLogDAO.getEvents(eventType);
			if(entryList.isEmpty())
				out.println("The log is empty.");
			
			int logSize=entryList.size();
			
			for (int i=0; i<logSize; i++) {
				Event entry = entryList.get(i);
				
				out.println("<TR >");
				out.println("<TD style='border: thin solid;'>"+entry.getUserName()+"</TD");
				out.println("<TD style='border: thin solid;'>"+entry.getTime()+"</TD>");
				out.println("<TD style='border: thin solid;'>"+entry.getEventType()+"</TD>");				
				out.println("<TD style='border: thin solid;'>"+entry.getEventText()+"</TD>");
				out.println("</TR>");
			}
		} 
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}
}
