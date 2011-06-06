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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet to clear a user's locks on project items. Clears all locks held within one project.
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
public class ClearLocks extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject output = new JSONObject();
		try {
			MetaDbHelper.note("Clearing Locks");
			HttpSession session = request.getSession(false);
			if (session != null )
			{
				MetaDbHelper.note("ClearLocks: Session not null--trying to fetch username/project data");
				String userName = (String) session.getAttribute(Global.SESSION_USERNAME);
				String lastProj = UserManDAO.getLastProj(userName);
				String oldProj = lastProj.split(";")[0].trim();
				MetaDbHelper.note("ClearLocks: Data retrieved? username="+userName+", project="+oldProj);
				ItemsDAO.cleanLock(oldProj, userName); //clean up locks from last item.
				MetaDbHelper.note("ClearLocks: cleanLocks() run");
				output.put("success", true);
			}
			else {
				output.put("success", false);
			}
			} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		response.setContentType("application/x-json");
	}
	
}
