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
package edu.lafayette.metadb.model.metadata;

import edu.lafayette.metadb.model.attributes.TechAttributesDAO;
import edu.lafayette.metadb.model.commonops.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class serves as the access object for technical metadata 
 * for one item in a MetaDB project. It provides functions for updating
 * and retrieving technical metadata for one item. 
 *  
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class TechnicalDataDAO 
{	
		
		private static final String UPDATE_TECHMD=
			
			"UPDATE "+Global.ITEMS_TECH_TABLE+" "+
			"SET "+Global.ITEM_TECH_DATA+"=? "+" "+
			"WHERE "+Global.PROJECT_NAME+"=?"+" "+
			"AND "+Global.ITEM_NUMBER+"=?"+" "+
			"AND "+Global.TECH_ELEMENT+"=?"+" "+
			"AND "+Global.TECH_LABEL+"=?";
		
		private static final String GET_TECH_DATA=
			
			"SELECT * "+
			"FROM "+Global.ITEMS_TECH_TABLE+" "+
			"WHERE "+Global.PROJECT_NAME+"=?"+" "+
			"AND "+Global.ITEM_NUMBER+"=?"+" "+
			"ORDER BY "+Global.TECH_ELEMENT+","+Global.TECH_LABEL;
		
		private static final String GET_ALL_TECH_DATA=
			
			"SELECT * "+
			"FROM "+Global.ITEMS_TECH_TABLE+" "+
			"WHERE "+Global.PROJECT_NAME+"=?"+" "+
			"ORDER BY "+Global.TECH_ELEMENT+","+Global.TECH_LABEL;
		
		private static final String GET_MULTIPLE_TECH_DATA=
			
			"SELECT "+ Global.ITEM_TECH_DATA+ " "+
			"FROM "+Global.ITEMS_TECH_TABLE+" "+
			"WHERE "+Global.PROJECT_NAME+"=?"+" "+
			"AND "+Global.TECH_ELEMENT+"=?" + " " +
			"AND "+Global.TECH_LABEL+"=?"+" "+
			"ORDER BY "+Global.TECH_ELEMENT+","+Global.TECH_LABEL;
		
		public TechnicalDataDAO()
		{
			
		}

		/**
		 * Updates a single technical attribute with new data. 
		 * @param projectName The project in which to update.
		 * @param itemNumber The item number to update.
		 * @param techElement The element to update.
		 * @param techLabel The label to update.
		 * @param newData The new data to replace with this element-label's current data.
		 * @return true if successfully updated, false otherwise.
		 */
		public static boolean updateTechData(String projectName, int itemNumber, String techElement, String techLabel, String newData )
		{
			
			if(!TechAttributesDAO.attributeExists(projectName, techElement, techLabel))
			{
					//MetaDbHelper.logEvent("TechnicalDataDAO", "updateTechData", "The technical attribute "+techElement+"."+techLabel+" doesn't exist "+
					//		"in the project "+projectName+" for item number "+itemNumber);
					return false;
			}

			boolean updateSuccessful=false;

			Connection conn = Conn.initialize(); //Establish connection
			if(conn!=null)
			{
				try
				{
					PreparedStatement updateTechData=conn.prepareStatement(UPDATE_TECHMD);
					updateTechData.setString(1, newData);
					updateTechData.setString(2, projectName);
					updateTechData.setInt(3, itemNumber);
					updateTechData.setString(4, techElement);
					updateTechData.setString(5, techLabel);

					//MetaDbHelper.logEvent("TechnicalDataDAO", "updateTechData","Tech Field "+techElement+"."+techLabel+" in project "+projectName+" for item number "+itemNumber+" modified."); 
					updateTechData.executeUpdate();

					updateTechData.close();
					conn.close(); //Close statement and connection
					
					updateSuccessful=true;
				}	
				catch(Exception e)
				{
					MetaDbHelper.logEvent(e);
					updateSuccessful = false;//need to be false?
				}
			}
			return updateSuccessful;
		}
		
		public static List<Metadata> getTechnicalData(String projectName, int itemNumber) 
		{		
			List<Metadata> techDataList=new ArrayList<Metadata>();

			Connection conn=Conn.initialize(); //Establish connection
			if(conn!=null)
			{
				try
				{
					PreparedStatement getTechnicalDataQuery=conn.prepareStatement(GET_TECH_DATA); //Set to get all elements SQL
					getTechnicalDataQuery.setString(1, projectName);
					getTechnicalDataQuery.setInt(2, itemNumber);

					ResultSet getTechnicalDataQueryResult=getTechnicalDataQuery.executeQuery();

					if(getTechnicalDataQueryResult!=null) //Query result not null.
					{
						while(getTechnicalDataQueryResult.next())
						{
							techDataList.add(new TechnicalData (
									getTechnicalDataQueryResult.getString(Global.PROJECT_NAME), 
									getTechnicalDataQueryResult.getInt(Global.ITEM_NUMBER),
									getTechnicalDataQueryResult.getString(Global.TECH_ELEMENT),
									getTechnicalDataQueryResult.getString(Global.TECH_LABEL),
									getTechnicalDataQueryResult.getString(Global.ITEM_TECH_DATA)							
				 					));
						}
					}
					getTechnicalDataQuery.close();
					getTechnicalDataQueryResult.close();
					conn.close();
				}
				catch(Exception e)
				{
					MetaDbHelper.logEvent(e);
				}
			}
			return techDataList;

		}
		
		/**
		 * Get a list of all the technical data of a project.
		 * @param projectName the project name to retrieve data from. 
		 * @return a List of TechnicalData objects.
		 */
		public static List<TechnicalData> getTechnicalData(String projectName) 
		{		
			List<TechnicalData> techDataList=new ArrayList<TechnicalData>();

			Connection conn=Conn.initialize(); //Establish connection
			if(conn!=null)
			{
				try
				{
					PreparedStatement getTechnicalDataQuery=conn.prepareStatement(GET_ALL_TECH_DATA); //Set to get all elements SQL
					getTechnicalDataQuery.setString(1, projectName);
					ResultSet getTechnicalDataQueryResult=getTechnicalDataQuery.executeQuery();

					if(getTechnicalDataQueryResult!=null) //Query result not null.
					{
						while(getTechnicalDataQueryResult.next())
						{
							techDataList.add(new TechnicalData (
									getTechnicalDataQueryResult.getString(Global.PROJECT_NAME), 
									getTechnicalDataQueryResult.getInt(Global.ITEM_NUMBER),
									getTechnicalDataQueryResult.getString(Global.TECH_ELEMENT),
									getTechnicalDataQueryResult.getString(Global.TECH_LABEL),
									getTechnicalDataQueryResult.getString(Global.ITEM_TECH_DATA)
							)
							);
						}
					}
					getTechnicalDataQuery.close();
					getTechnicalDataQueryResult.close();
					conn.close();
				}
				catch(Exception e)
				{
					MetaDbHelper.logEvent(e);
				}
			}
			return techDataList;

		}
		
		/**
		 * Get the technical data for all items and one technical element/label.
		 * @param projectName The project name to retrieve data from. 
		 * @param element The element to get data for. 
		 * @param label The label of the element to get data for.
		 * @return a List of Strings representing the technical data contents for the project and element/label.
		 */
		public static List<String> getMultipleTechnicalData(String projectName, String element, String label) 
		{		
			List<String> techDataList=new ArrayList<String>();

			Connection conn=Conn.initialize(); //Establish connection
			if(conn!=null)
			{
				try
				{
					PreparedStatement getMultipleTechnicalDataQuery=conn.prepareStatement(GET_MULTIPLE_TECH_DATA); //Set to get all elements SQL
					getMultipleTechnicalDataQuery.setString(1, projectName);
					getMultipleTechnicalDataQuery.setString(2, element);
					getMultipleTechnicalDataQuery.setString(3, label);
					ResultSet getMultipleTechnicalDataQueryResult=getMultipleTechnicalDataQuery.executeQuery();

					if(getMultipleTechnicalDataQueryResult!=null) //Query result not null.
					{
						while(getMultipleTechnicalDataQueryResult.next())
							techDataList.add(getMultipleTechnicalDataQueryResult.getString(Global.ITEM_TECH_DATA));
					}
					getMultipleTechnicalDataQuery.close();
					getMultipleTechnicalDataQueryResult.close();
					conn.close();
				}
				catch(Exception e)
				{
					MetaDbHelper.logEvent(e);
				}
			}
					
			return techDataList;

		}
		
		
		/**
		 * Check if technical data exists for a particular an item in a project.
		 * @param projectName The project in which to search for data.
		 * @param itemNumber the item number to check for metadata. 
		 * @return true if the data exists, false otherwise.
		 */
		public static boolean techDataExists(String projectName, int itemNumber)
		{
			boolean dataExists=false;

			try
			{
				List<Metadata> metadata=getTechnicalData(projectName, itemNumber);
				dataExists=!metadata.isEmpty();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
			return dataExists;
		}
}
			
			

			
