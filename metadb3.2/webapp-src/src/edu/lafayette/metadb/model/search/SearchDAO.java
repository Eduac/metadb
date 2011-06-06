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
package edu.lafayette.metadb.model.search;
import java.sql.*;
import java.util.ArrayList;

import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;

/**
 * Class to perform regex-based searches in the metadata.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0  
 *
 */
public class SearchDAO 
{
	private static final String SEARCH=
		
		"SELECT "+Global.PROJECT_NAME+","+Global.ITEM_NUMBER+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE lower("+Global.ITEM_ADMIN_DESC_DATA+") "+
		"LIKE ? "+" "+
		"UNION"+" "+		
		"SELECT "+Global.PROJECT_NAME+","+Global.ITEM_NUMBER+" "+
		"FROM "+Global.ITEMS_TECH_TABLE+" "+
		"WHERE lower("+Global.ITEM_TECH_DATA+") "+
		"LIKE ?";
	
	private static final String SEARCH_ONE_PROJECT=
		
		"SELECT "+Global.ITEM_NUMBER+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE lower("+Global.ITEM_ADMIN_DESC_DATA+") "+
		"LIKE ? "+"AND "+ Global.PROJECT_NAME+"=? " +
		"UNION"+" "+		
		"SELECT "+Global.ITEM_NUMBER+" "+
		"FROM "+Global.ITEMS_TECH_TABLE+" "+
		"WHERE lower("+Global.ITEM_TECH_DATA+") "+
		"LIKE ? "+ "AND "+ Global.PROJECT_NAME+"=? ";

	public SearchDAO()
	{
	}
	
	/**
	 * Get all the items that were found containing the tokens in the given search string.
	 * @param projname The name of the project to search for, "" for all project search
	 * @param searchString The search string containing the tokens.
	 * @return an ArrayList of Item objects containing the metadata items that matched
	 * the terms in the search string.
	 */
	public static ArrayList<Item> search(String projname, String searchString)
	{
		ArrayList<String> searchTokens=MetaDbHelper.getSQLTokens(searchString);
		ArrayList<Item> allResults=new ArrayList<Item>();
		if (projname == null)
			return allResults;
		else if (projname.equals("")) 
			for(String token: searchTokens)
				allResults.addAll(getResults(token));
		else
			for(String token: searchTokens)
				allResults.addAll(getResults(projname, token));
				
		return allResults;
	}
	
	/**
	 * Return a list of Items which match a search token.
	 * @param projname The name of the project to search for, "" to search in all projects
	 * @param searchToken A string containing the pre-processed search term
	 * @return an ArrayList of Item objects matching the search token in the specified project.
	 */
	public static ArrayList<Item> getResults(String projname, String searchToken)
	{
		//MetaDbHelper.note("Searching for "+searchToken+" in project "+projname+"...");
		ArrayList<Item> matchingItems=new ArrayList<Item>();
		//MetaDbHelper.note("Search query for \'"+searchToken+"\' in project "+projname+"...");
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement searchQuery=conn.prepareStatement(SEARCH_ONE_PROJECT);
				searchQuery.setString(1, searchToken);
				searchQuery.setString(2, projname);
				searchQuery.setString(3, searchToken);
				searchQuery.setString(4, projname);
				
				ResultSet searchResults=searchQuery.executeQuery();
				while(searchResults.next())
				{
					matchingItems.add(ItemsDAO.getItem(projname, searchResults.getInt(Global.ITEM_NUMBER)));
				}
				
				searchResults.close();
				searchQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return matchingItems;
	}
	
	
	/**
	 * Return a list of Items which match the search token
	 * @param searchToken A string containing the pre-processed search term
	 * @return an ArrayList of items which match the search token from all projects.
	 */
	public static ArrayList<Item> getResults(String searchToken)
	{
		//MetaDbHelper.note("Searching for "+searchToken+"...");
		ArrayList<Item> matchingItems=new ArrayList<Item>();
		//MetaDbHelper.note("Search query for \'"+searchToken+"\'...");
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement searchQuery=conn.prepareStatement(SEARCH);
				searchQuery.setString(1, searchToken);
				searchQuery.setString(2, searchToken);
				
				ResultSet searchResults=searchQuery.executeQuery();
				while(searchResults.next())
				{
					matchingItems.add(ItemsDAO.getItem(searchResults.getString(Global.PROJECT_NAME), searchResults.getInt(Global.ITEM_NUMBER)));
				}
				
				searchResults.close();
				searchQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return matchingItems;
	}

}
