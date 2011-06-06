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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.controlledvocab.ControlledVocabDAO;

/**
 * Servlet implementation class ShowVocab
 */
public class ShowVocab extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String vocabName = request.getParameter("vocab-name");
		//MetaDbHelper.note("Vocab Name in Servlet: "+vocabName);
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF8"), true);
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		JSONArray vocab = new JSONArray();
		try {
			Set<String> vocabList = ControlledVocabDAO.getControlledVocab(vocabName);
			Iterator<String> itr = vocabList.iterator();
			String[] term = request.getParameterValues("term");
			
			boolean autocomplete = term != null && !(term[0].equals("") );
			while (itr.hasNext()) {
				String entry = itr.next();
				if (autocomplete) {
					//MetaDbHelper.note("Entry: "+entry+", query: "+term[0]);
					if(entry.toLowerCase().startsWith(term[0].toLowerCase()))
						vocab.put(entry);
				}
				else
					vocab.put(entry);
			}
			out.print(vocab);
		}
		catch(Exception e) {
			MetaDbHelper.logEvent(e);
		}
		out.flush();
	}

}
