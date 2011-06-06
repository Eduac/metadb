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
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;

/**
 * Servlet implementation class ShowVocabList
 */
public class ShowVocabList extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String term = request.getParameter("term");
		term = term == null ? "" : term.toLowerCase();
		JSONArray vocabs = new JSONArray();
		try {
			String userName=null;
			HttpSession session = request.getSession(false);
			if(session!=null)
					userName = (String)session.getAttribute("username");

			ArrayList<String> vocabList = new ArrayList<String>(ControlledVocabDAO.getControlledVocabs(userName));
			if (!vocabList.isEmpty()) {
				for (String v: vocabList) {
					if (v.toLowerCase().startsWith(term))
						vocabs.put(v);
				}
			}
			out.print(vocabs);
		}
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
