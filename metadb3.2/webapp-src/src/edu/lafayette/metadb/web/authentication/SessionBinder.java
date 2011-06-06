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
package edu.lafayette.metadb.web.authentication;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

public class SessionBinder implements HttpSessionBindingListener {

	private String username = "";
	public SessionBinder() {

	}
	
	public void valueBound(HttpSessionBindingEvent arg0) {
		// TODO Auto-generated method stub
		HttpSession session = arg0.getSession();
		username = (String) session.getAttribute(Global.SESSION_USERNAME);
		//MetaDbHelper.logEvent(this.getClass().toString(), "valueBound", "session bound, username "+username+" logged in");
	}

	public void valueUnbound(HttpSessionBindingEvent arg0) {
		// TODO Auto-generated method stub
		
		if (username != null && !username.equals(""))
		{
			SysLogDAO.log(username, Global.SYSLOG_AUTH, "User "+username+" logged out.");
			ItemsDAO.cleanLockAllProjects(username);
		}
	}
}
