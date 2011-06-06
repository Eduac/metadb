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

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.commonops.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to handle retrieval and updating of administrative/descriptive metadata.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class AdminDescDataDAO 
{
	private static final String GET_ADMIN_DESC_DATA=

		"SELECT "+Global.ITEM_NUMBER+", "+Global.ELEMENT+","+" "+
		Global.ADMIN_DESC_LABEL+", "+Global.ITEM_ADMIN_DESC_DATA+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+
		"AND "+Global.MD_TYPE+"=?";

	private static final String GET_ADMIN_DESC_ITEM_NUMBER=

		"SELECT DISTINCT "+Global.ITEM_NUMBER+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+
		"AND "+Global.MD_TYPE+"=?";

	private static final String GET_ADMIN_DESC_DATA_ITEM=

		"SELECT "+Global.ELEMENT+","+" "+
		Global.ADMIN_DESC_LABEL+", "+Global.ITEM_ADMIN_DESC_DATA+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?"+
		"AND "+Global.MD_TYPE+"=?";

	private static final String GET_MULTIPLE_ADMIN_DESC_DATA=

		"SELECT "+Global.ITEM_ADMIN_DESC_DATA+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ELEMENT+"=? "+
		"AND "+Global.ADMIN_DESC_LABEL+"=? " +
		"AND "+Global.MD_TYPE+"=?";

	private static final String UPDATE_ADMIN_DESC_DATA=

		"UPDATE "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"SET "+Global.ITEM_ADMIN_DESC_DATA+"=? "+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_NUMBER+"=?"+" "+
		"AND "+Global.ELEMENT+"=?"+" "+
		"AND "+Global.ADMIN_DESC_LABEL+"=?";

	private static final String UPDATE_ADMIN_DESC_DATA_ID=

		"UPDATE "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"SET "+Global.ITEM_ADMIN_DESC_DATA+"=? "+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_NUMBER+"=?"+" "+
		"AND "+Global.ELEMENT+"=?"+" "+
		"AND "+Global.ADMIN_DESC_LABEL+"=?";

	private static final String GET_SINGLE_ADMIN_DESC_DATA=

		"SELECT "+Global.ITEM_ADMIN_DESC_DATA+" "+
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_NUMBER+"=?"+" "+
		"AND "+Global.ELEMENT+"=?"+" "+
		"AND "+Global.ADMIN_DESC_LABEL+"=?";

	public AdminDescDataDAO()
	{
	}

	/**
	 * Get the metadata for an item  in the project. 
	 * @param projectName The project whose administrative metadata is to be retrieved. 
	 * @param type the type of the metadata
	 * @param itemNumber the index of the item
	 * @return A List of administrative metadata for the item. 
	 */
	@SuppressWarnings("unchecked")
	public static List<Metadata> getAdminDescData(String projectName, String type, int itemNumber)
	{				
		ArrayList attrList = AdminDescAttributesDAO.getAdminDescAttributesList(projectName, type);
		List<Metadata> adminDescDataList=new ArrayList<Metadata>(attrList.size());

		Connection conn=Conn.initialize(); 
		if(conn!=null)
		{
			try
			{
				PreparedStatement getAdminDescDataQuery=conn.prepareStatement(GET_ADMIN_DESC_DATA_ITEM); 
				getAdminDescDataQuery.setString(1, projectName);
				getAdminDescDataQuery.setInt(2, itemNumber);
				getAdminDescDataQuery.setString(3, type);

				ResultSet getAdminDescDataQueryResult=getAdminDescDataQuery.executeQuery();
				//MetaDbHelper.logEvent("AdminDescDataDAO", "getAdminDescData","getAdminDescData: Query success."); //debugging

				if(getAdminDescDataQueryResult!=null) //Query result not null.
				{

					while(getAdminDescDataQueryResult.next())
					{
						String element = getAdminDescDataQueryResult.getString(Global.ELEMENT);
						String label = getAdminDescDataQueryResult.getString(Global.ADMIN_DESC_LABEL);
						adminDescDataList.add(attrList.indexOf(element+"."+label),
								new AdminDescData (
										projectName, itemNumber, element, label,
										getAdminDescDataQueryResult.getString(Global.ITEM_ADMIN_DESC_DATA),
										type
								)
						);
					}

					getAdminDescDataQueryResult.close();
				}
				getAdminDescDataQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}

		}
		return adminDescDataList;
	}	


	/**
	 * Get the map of element/label to its metadata for an item
	 * @param projectName the project to get the item from
	 * @param type the type of the metadata
	 * @param itemNumber the item index
	 * @return a Map<String, String> with element-label as key and metadata as value
	 */
	public static Map<String, String> getAdminDescDataMap(String projectName, String type, int itemNumber)
	{				
		Map<String, String> adminDescDataMap = new HashMap<String, String>();

		Connection conn=Conn.initialize(); 
		if(conn!=null)
		{
			try
			{
				PreparedStatement getAdminDescDataMapQuery=conn.prepareStatement(GET_ADMIN_DESC_DATA_ITEM); 
				getAdminDescDataMapQuery.setString(1, projectName);
				getAdminDescDataMapQuery.setInt(2, itemNumber);
				getAdminDescDataMapQuery.setString(3, type);

				ResultSet getAdminDescDataQueryResult=getAdminDescDataMapQuery.executeQuery();
				//MetaDbHelper.logEvent("AdminDescDataDAO", "getAdminDescData","getAdminDescData: Query success."); //debugging

				if(getAdminDescDataQueryResult!=null) //Query result not null.
				{
					while(getAdminDescDataQueryResult.next())
					{
						String element = getAdminDescDataQueryResult.getString(Global.ELEMENT);
						String label = getAdminDescDataQueryResult.getString(Global.ADMIN_DESC_LABEL);
						adminDescDataMap.put(element+"-"+label ,
								getAdminDescDataQueryResult.getString(Global.ITEM_ADMIN_DESC_DATA)											
						);
					}

					getAdminDescDataQueryResult.close();
				}
				getAdminDescDataMapQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}

		}
		return adminDescDataMap;
	}



	/**
	 * Get data of all items based on a element/label pair
	 * @param projectName the project to search for
	 * @param element the attribute's element
	 * @param label the attribute's label
	 * @return a List<String> containing data of all items
	 */
	public static List<String> getMultipleAdminDescData(String projectName, String element, String label, String type)
	{				
		List<String> adminDescData = new ArrayList<String>();

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				//MetaDbHelper.note("Trying to get "+type+" metadata for "+projectName+"; Attribute: "+element+"."+label);
				PreparedStatement getMultipleAdminDescDataQuery = conn.prepareStatement(GET_MULTIPLE_ADMIN_DESC_DATA); 
				getMultipleAdminDescDataQuery.setString(1, projectName);
				getMultipleAdminDescDataQuery.setString(2, element);
				getMultipleAdminDescDataQuery.setString(3, label);
				getMultipleAdminDescDataQuery.setString(4, type);

				ResultSet getMultipleAdminDescDataQueryResult=getMultipleAdminDescDataQuery.executeQuery();
				//MetaDbHelper.logEvent("AdminDescDataDAO", "getAdminDescData","getAdminDescData: Query success."); //debugging

				while(getMultipleAdminDescDataQueryResult.next())
				{
					String data = getMultipleAdminDescDataQueryResult.getString(Global.ITEM_ADMIN_DESC_DATA);
					adminDescData.add(data != null ? data : "");
				}

				getMultipleAdminDescDataQueryResult.close();
				getMultipleAdminDescDataQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}

		}
		//MetaDbHelper.note("Reached end of method for getMultipleAdminDescData; size of result list: "+adminDescData.size());
		return adminDescData;
	}


	/**
	 * Get the contents of administrative or descriptive metadata for a project, for all the items in the project. 
	 * @param projectName The project whose metadata is to be retrieved. 
	 * @param type The type of metadata to retrieves
	 * @return A List of admin/desc metadata for the project. 
	 */	
	public static List<AdminDescData> getAdminDescData(String projectName, String type)
	{				
		List<AdminDescData> AdminDescDataList=new ArrayList<AdminDescData>();

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{

				PreparedStatement getAdminDescDataQuery=conn.prepareStatement(GET_ADMIN_DESC_DATA); 
				getAdminDescDataQuery.setString(1, projectName);
				getAdminDescDataQuery.setString(2, type);
				ResultSet getAdminDescDataQueryResult=getAdminDescDataQuery.executeQuery();
				//MetaDbHelper.logEvent("AdminDescDataDAO", "getAdminDescData","getAdminDescData: Query success."); //debugging

				if(getAdminDescDataQueryResult!=null) //Query result not null.
				{
					while(getAdminDescDataQueryResult.next())
					{
						AdminDescDataList.add(new AdminDescData (
								projectName, 
								getAdminDescDataQueryResult.getInt(Global.ITEM_NUMBER),
								getAdminDescDataQueryResult.getString(Global.ELEMENT),
								getAdminDescDataQueryResult.getString(Global.ADMIN_DESC_LABEL),
								getAdminDescDataQueryResult.getString(Global.ITEM_ADMIN_DESC_DATA),
								type
						)
						);
					}

					getAdminDescDataQueryResult.close();
				}
				getAdminDescDataQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}

		}
		return AdminDescDataList;
	}

	/**
	 * Get the item numbers in the table
	 * @param projectName name of the project
	 * @param type either "descriptive" or "administrative"
	 * @return the list of numbers
	 */
	public static List<Integer> getAdminDescItemNumbers(String projectName, String type)
	{				
		List<Integer> AdminDescDataList=new ArrayList<Integer>();

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getAdminDescDataQuery=conn.prepareStatement(GET_ADMIN_DESC_ITEM_NUMBER); 
				getAdminDescDataQuery.setString(1, projectName);
				getAdminDescDataQuery.setString(2, type);
				ResultSet getAdminDescDataQueryResult=getAdminDescDataQuery.executeQuery();
				//MetaDbHelper.logEvent("AdminDescDataDAO", "getAdminDescData","getAdminDescData: Query success."); //debugging

				if(getAdminDescDataQueryResult!=null) //Query result not null.
				{
					while(getAdminDescDataQueryResult.next())
					{
						AdminDescDataList.add(getAdminDescDataQueryResult.getInt(Global.ITEM_NUMBER));
					}

					getAdminDescDataQueryResult.close();
				}

				getAdminDescDataQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}

		}
		return AdminDescDataList;
	}

	/**
	 * Updates an item's administrative metadata. 
	 * @param projectName The project in which the item is to be updated
	 * @param itemNumber the item number of the item to update.
	 * @param id the id of the attribute to update.
	 * @param newData The new value for the data in the element-label field for this file in this project. 
	 * @return true if the data for the item is successfully updated, false otherwise.
	 */
	public static boolean updateAdminDescData(String projectName, int itemNumber, int id, String newData)
	{
		//Make sure the original data is present prior to updating.
		AdminDescAttribute attr=AdminDescAttributesDAO.getAttributeById(id);

		if(attr==null)
			{
			MetaDbHelper.note("Update admin/desc data: project="+projectName+", the field (ID="+id+") doesn't exist!");
			return false;
			}

		if(!adminDescDataExists(projectName, itemNumber, attr.getElement(), attr.getLabel()))
		{
			MetaDbHelper.note("updateAdminDescData: No field available for update. Creating blank.");
			AdminDescAttributesDAO.createBlankFields(projectName, itemNumber, attr.getElement(), attr.getLabel(), attr.getMdType());
		}
		else {
			MetaDbHelper.note("updateAdminDescData: Field appears to exist");
		}
		boolean updateSuccessful=false;

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement updateAdminDescData=conn.prepareStatement(UPDATE_ADMIN_DESC_DATA_ID);
				updateAdminDescData.setString(1, newData);
				updateAdminDescData.setString(2, projectName);
				updateAdminDescData.setInt(3, itemNumber);
				updateAdminDescData.setString(4, attr.getElement());
				updateAdminDescData.setString(5, attr.getLabel());
				MetaDbHelper.logEvent("AdminDescDataDAO", "updateAdminDescData","Field id "+id+" in project "+projectName+" for item "+itemNumber+" modified."); 
				updateAdminDescData.executeUpdate();

				updateAdminDescData.close();
				conn.close(); 

				updateSuccessful=true;					
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				updateSuccessful = false;//need to be false?
			}
			//Close statement and connection
		}
		return updateSuccessful;
	}

	/**
	 * Updates an item's administrative metadata. 
	 * @param projectName The project in which the item is to be updated
	 * @param itemNumber the item number of the item to update.
	 * @param element The element to be updated.
	 * @param label The label of the element to be updated. 
	 * @param newData The new value for the data in the element-label field for this file in this project. 
	 * @return true if the data for the item is successfully updated, false otherwise.
	 */
	public static boolean updateAdminDescData(String projectName, int itemNumber, String element, String label, String newData)
	{
		AdminDescAttribute attr=AdminDescAttributesDAO.getAttributeByName(projectName, element, label);
		if (attr==null)
		{
			MetaDbHelper.note("Update admin/desc data: project="+projectName+", the field "+element+"."+label+" doesn't exist!");
			return false;
		}
		//Make sure the original data is present prior to updating.
		if(!adminDescDataExists(projectName, itemNumber, element, label))
			return false;

		boolean updateSuccessful=false;

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement updateAdminDescData=conn.prepareStatement(UPDATE_ADMIN_DESC_DATA);
				updateAdminDescData.setString(1, newData);
				updateAdminDescData.setString(2, projectName);
				updateAdminDescData.setInt(3, itemNumber);
				updateAdminDescData.setString(4, element);
				updateAdminDescData.setString(5, label);

				//MetaDbHelper.logEvent("AdminDescDataDAO", "updateAdminDescData","Parameters have been set for updateAdminDescData SQL"); //debugging
				MetaDbHelper.logEvent("AdminDescDataDAO", "updateAdminDescData","Field "+element+"."+label+" in project "+projectName+" for item "+itemNumber+" modified."); 
				updateAdminDescData.executeUpdate();
				updateAdminDescData.close();
				conn.close(); 

				updateSuccessful=true;					
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				updateSuccessful = false;//need to be false?
			}
			//Close statement and connection
		}
		return updateSuccessful;
	}

	/**
	 * Checks if a file has existing administrative metadata field. 
	 * This does not check if the field has a value, merely whether the field 
	 * is present or not for updating and is necessary for integrity issues. 
	 * 
	 * @param projectName The project to be checked. 
	 * @param itemNumber The item number to be checked for the field. 
	 * @param element The element of the field to be checked for.
	 * @param label The label for the element of the field.
	 * @return true if the requested file exists in the project, and the field specified.
	 */	
	private static boolean adminDescDataExists(String projectName, int itemNumber, String element, String label)
	{
		boolean dataExists = false;
		//MetaDbHelper.note("Checking if "+projectName+": Item "+itemNumber+", field "+element+"."+label+" exists");
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getSingleAdminDescData=conn.prepareStatement(GET_SINGLE_ADMIN_DESC_DATA);
				getSingleAdminDescData.setString(1, projectName);
				getSingleAdminDescData.setInt(2, itemNumber);
				getSingleAdminDescData.setString(3, element);
				getSingleAdminDescData.setString(4, label);

				ResultSet result=getSingleAdminDescData.executeQuery();

				dataExists = result.next();
				//MetaDbHelper.note("Admin/desc data exists: "+dataExists);
				getSingleAdminDescData.close();
				conn.close(); 


			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
			//Close statement and connection
		}
		return dataExists;
	}
}
