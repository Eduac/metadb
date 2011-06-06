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
package edu.lafayette.metadb.model.elements;

import java.sql.*;
import java.util.*;

import edu.lafayette.metadb.model.commonops.*;

/**
 * This class serves as the access object for metadata 
 * specified elements in the MetaDB system. It provides 
 * various functions such as adding, removing, and retrieving
 * elements. 
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class ElementsDAO 
{
	private static final String ADD_ELEMENT=

		"INSERT INTO "+Global.ELEMENTS_TABLE+						
		"("+Global.ELEMENT+","+Global.ELEMENT_FORMAT+")"+" "+

		"VALUES (?, ?)"; 

	private static final String DELETE_ELEMENT=

		"DELETE FROM "+Global.ELEMENTS_TABLE+" "+
		"WHERE "+Global.ELEMENT+"=?";

	private static final String GET_ELEMENT_LIST=

		"SELECT "+Global.ELEMENT+","+Global.ELEMENT_FORMAT+" "+
		"FROM "+Global.ELEMENTS_TABLE+" ORDER BY "+Global.ELEMENT; 

	private static final String GET_ELEMENT_LIST_BY_FORMAT=
		"SELECT "+Global.ELEMENT+","+Global.ELEMENT_FORMAT+" "+
		"FROM "+Global.ELEMENTS_TABLE+" "+
		"WHERE "+Global.ELEMENT_FORMAT+"=?";

	public ElementsDAO()
	{
	}

	/**
	 * Adds an element to MetaDB.
	 * @param elementName The desired element to be added.
	 * @param format The format (specification) of the element to be added.
	 * @return true if an element with elementName and format was added, false otherwise
	 * @throws SQLException
	 */
	public static boolean addElement(String elementName, String format)
	{	
		Connection conn = Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement addElement=conn.prepareStatement(ADD_ELEMENT);
				addElement.setString(1, elementName);
				addElement.setString(2, format); 

				addElement.executeUpdate();

				addElement.close();
				conn.close(); 

				return true;				
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;

	}

	/**
	 * Deletes an element from MetaDB. 
	 * @param elementName The desired element name to be added
	 * @return true if the element with name elementName was deleted, false otherwise
	 */
	public static boolean deleteElement(String elementName)
	{
		Connection conn = Conn.initialize(); 
		if(conn!=null)
		{
			try
			{
				PreparedStatement deleteElement=conn.prepareStatement(DELETE_ELEMENT);

				deleteElement.setString(1, elementName);
				deleteElement.executeUpdate();

				deleteElement.close();
				conn.close();

				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;	
	}

	/**
	 * Check if an element exists in the system. 
	 * @param elementName The element to be checked for existence. 
	 * @return true if the element exists, false otherwise.
	 */
	public static boolean elementExists(String elementName)
	{
		try
		{
			ArrayList<String> elementList=(ArrayList<String>) getElementList();
			for (String element: elementList)
			{
				if(element.equals(elementName))
					return true;
			}
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Returns all the elements in the database. 
	 * @return a List of all elements in MetaDB. 
	 */
	public static ArrayList<String> getElementList()
	{
		ArrayList<String> elementList=new ArrayList<String>();

		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{

				PreparedStatement getElementListQuery=conn.prepareStatement(GET_ELEMENT_LIST);
				ResultSet getElementListQueryResult=getElementListQuery.executeQuery();

				while(getElementListQueryResult.next())
					elementList.add(getElementListQueryResult.getString(Global.ELEMENT));

				getElementListQueryResult.close();
				getElementListQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return elementList;
	}

	/**
	 * Gets elements depending on the format specified. 
	 * @param format The format specification of the elements to be returned.
	 * @return a List of all elements in MetaDB with the specified format.
	 */
	public List<Element> getElementList(String format)
	{
		List<Element> elementList=new ArrayList<Element>();

		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement getElementListQuery=conn.prepareStatement(GET_ELEMENT_LIST_BY_FORMAT);
				getElementListQuery.setString(1, format);

				ResultSet getElementListQueryResult=getElementListQuery.executeQuery();

				while(getElementListQueryResult.next())
					elementList.add(new Element (
							getElementListQueryResult.getString(Global.ELEMENT), 
							getElementListQueryResult.getString(Global.ELEMENT_FORMAT)
					)
					);

				getElementListQueryResult.close();
				getElementListQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return elementList;
	}
}
