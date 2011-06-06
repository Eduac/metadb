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
package edu.lafayette.metadb.model.syslog;
import edu.lafayette.metadb.model.commonops.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Class which handles retrieval and updates of events in the system log.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0  
 *
 */

public class SysLogDAO 
{
	private static final String LOG_EVENT=
		"INSERT INTO "+Global.SYSLOG_TABLE+" "+
		"("+Global.USER_NAME+","+
		Global.SYSLOG_EVENT_TYPE+","+Global.SYSLOG_EVENT_TEXT+","+
		Global.SYSLOG_EVENT_TIME+")"+" "+
		"VALUES (?, ?, ?, ?)";	

	private static final String GET_ALL_EVENTS=

		"SELECT * FROM "+Global.SYSLOG_TABLE+" "+
		"ORDER BY "+Global.SYSLOG_EVENT_TIME+" DESC";

	private static final String GET_EVENTS_BY_TYPE=

		"SELECT * FROM "+Global.SYSLOG_TABLE+" "+
		"WHERE "+Global.SYSLOG_EVENT_TYPE+"=?"+" "+
		"ORDER BY "+Global.SYSLOG_EVENT_TIME+" DESC";

	private static final String CLEAR_LOG_BY_TYPE=
		
		"DELETE FROM "+Global.SYSLOG_TABLE+" "+
		"WHERE "+Global.SYSLOG_EVENT_TYPE+"=?";
	
	public SysLogDAO()
	{
	}
	
	/**
	 * Inserts a new event into the system log.
	 * @param request Servlet request for getting session attributes
	 * @param eventType The type of event.
	 * @param eventText The details of the event.
	 * @return true if the event was logged successfully, false otherwise.
	 */
	public static boolean log(HttpServletRequest request, String eventType, String eventText)
	{
		HttpSession session = request.getSession(false);
		session.setAttribute(Global.SESSION_PROJECT, null);
		if(session!=null)
		{
			String userName=(String)session.getAttribute(Global.SESSION_USERNAME);
			return SysLogDAO.log(userName, eventType, eventText);
		}
		return false;
	}
	
	
	/**
	 * Inserts a new event into the system log.
	 * @param userName The username associated with the system event.
	 * @param eventType The type of event.
	 * @param eventText The details of the event.
	 * @return true if the event was logged successfully, false otherwise.
	 */
	public static boolean log(String userName, String eventType, String eventText)
	{
		Connection conn=Conn.initialize();
		if(conn!=null)
			{
				try
				{

					Timestamp time=new Timestamp(new Date().getTime());
					
					PreparedStatement log=conn.prepareStatement(LOG_EVENT);
					log.setString(1, userName);
					log.setString(2, eventType);
					log.setString(3, eventText);
					log.setTimestamp(4, time);
					log.executeUpdate();
					log.close();
					conn.close();
					return true;
				}
				catch(Exception e)
				{
					MetaDbHelper.logEvent(e);
					return false;
				}
			}
		return false;
	}

	/**
	 * Get the entire system log.
	 * @return The system log, as an array list of Events.
	 */
	public static ArrayList<Event> getEvents()
	{	
		ArrayList<Event> systemLog=new ArrayList<Event>(300);
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{

				PreparedStatement getLog=conn.prepareStatement(GET_ALL_EVENTS);

				ResultSet log=getLog.executeQuery();
				while(log.next())
				{
					Event event=new Event(
							log.getInt(Global.SYSLOG_EVENT_ID),
							log.getString(Global.USER_NAME),
							log.getString(Global.SYSLOG_EVENT_TYPE),
							log.getString(Global.SYSLOG_EVENT_TEXT),
							log.getTimestamp(Global.SYSLOG_EVENT_TIME)
					);
					systemLog.add(event);
				}
				log.close();
				getLog.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return systemLog;
	}
	

	/**
	 * Get all events of a certain type.
	 * @param eventType The type of events to get from the log.
	 * @return The system log, as an array list of Events.
	 */
	public static ArrayList<Event> getEvents(String eventType)
	{	
		ArrayList<Event> systemLog=new ArrayList<Event>(300);
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{

				PreparedStatement getLog=conn.prepareStatement(GET_EVENTS_BY_TYPE);
				getLog.setString(1, eventType);
				ResultSet log=getLog.executeQuery();
				while(log.next())
				{
					Event event=new Event(
							log.getInt(Global.SYSLOG_EVENT_ID),
							log.getString(Global.USER_NAME),
							log.getString(Global.SYSLOG_EVENT_TYPE),
							log.getString(Global.SYSLOG_EVENT_TEXT),
							log.getTimestamp(Global.SYSLOG_EVENT_TIME)
					);
					systemLog.add(event);
				}
				log.close();
				getLog.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return systemLog;
	}
	
	/**
	 * Clear the log of a particular event type. 
	 * @param eventType The type of event to clear from the log.
	 * @return true if update succeeded, false otherwise.
	 */
	public static boolean clearLog(String eventType)
	{	
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement clearLog=conn.prepareStatement(CLEAR_LOG_BY_TYPE);
				clearLog.setString(1, eventType);
				clearLog.executeUpdate();
				clearLog.close();
				conn.close();
				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}
		}
		return false;
	}

	/**
	 * CURRENTLY UNUSED.
	 * Checks if an event type is valid. 
	 * @param eventType The event type to be checked. 
	 * @return true if lookForEventType is a valid event type, false otherwise. 
	 */
	@SuppressWarnings("unused")
	private static boolean eventTypeExists(String eventType)
	{
		return true;
		/**
		for (String eventType: Global.eventTypes)
		{
			if(eventType.equals(lookForEventType))
				return true;
		}
		return false;
		**/
	}

}


