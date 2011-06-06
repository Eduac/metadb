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
package edu.lafayette.metadb.model.attributes;

import java.sql.*;
import java.util.*;

import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.elements.*;
import edu.lafayette.metadb.model.controlledvocab.*;


/** 
 * Class to access administrative/descriptive attributes.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 February 2011
 */
public class AdminDescAttributesDAO 
{	 
	//Create attribute SQL
	private static final String CREATE_ATTRIBUTE=

		"INSERT INTO " +
		Global.ADMIN_DESC_TABLE+
		"("+
		Global.PROJECT_NAME+","+
		Global.ELEMENT+","+Global.ADMIN_DESC_LABEL+","+
		Global.MD_TYPE+","+
		Global.ADMIN_DESC_LARGE+","+
		Global.ADMIN_DESC_R_DATE+","+
		Global.ADMIN_DESC_S_DATE+","+
		Global.ADMIN_DESC_CONTROLLED+","+Global.ADMIN_DESC_MULTIPLE+","+
		Global.ADMIN_DESC_ADDITIONS+","+
		Global.ADMIN_DESC_SORTED+","+
		Global.ADMIN_DESC_ROW_INDEX+","+
		Global.CONTROLLED_VOCAB_NAME+","+
		Global.ADMIN_DESC_ERROR+")"+" "+

		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	//Create blank fields SQL
	private static final String CREATE_BLANK_FIELDS=

		"INSERT INTO "+Global.ITEMS_ADMIN_DESC_TABLE+
		"("+
		Global.PROJECT_NAME+","+
		Global.MD_TYPE+","+
		Global.ITEM_NUMBER+","+
		Global.ELEMENT+","+
		Global.ADMIN_DESC_LABEL+","+
		Global.ITEM_ADMIN_DESC_DATA+") "
		+
		"VALUES (?, ?, ?, ?, ?, ?)";

	//Delete SQL
	private static final String DELETE_BY_ID=

		"DELETE FROM "+Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.ADMIN_DESC_ID+"=?";

	//SQL to check if attr. exists in a project.
	private static final String CHECK_EXISTS= 

		"SELECT * "+" "+
		"FROM "+Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ELEMENT+"=?"+" "+
		"AND "+Global.ADMIN_DESC_LABEL+"=?";

	//Get attributes
	private static final String GET_ATTRIBUTES=

		"SELECT " +

		Global.PROJECT_NAME+","+Global.ELEMENT+","+ Global.ADMIN_DESC_LABEL+","+
		Global.MD_TYPE+","+
		Global.ADMIN_DESC_LARGE+","+Global.ADMIN_DESC_R_DATE+","+Global.ADMIN_DESC_S_DATE+","+
		Global.ADMIN_DESC_CONTROLLED+","+Global.ADMIN_DESC_MULTIPLE+","+
		Global.ADMIN_DESC_ADDITIONS+","+Global.ADMIN_DESC_SORTED+","+Global.ADMIN_DESC_ROW_INDEX+","+
		Global.ADMIN_DESC_ERROR+","+Global.ADMIN_DESC_ID+" "+


		"FROM "+Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+	
		"AND "+Global.MD_TYPE+"=? " +
		"ORDER BY "+Global.ADMIN_DESC_ROW_INDEX;

	//Get attribute by ID
	private static final String GET_BY_ID=

		"SELECT " +

		Global.PROJECT_NAME+","+Global.ELEMENT+","+ Global.ADMIN_DESC_LABEL+","+
		Global.MD_TYPE+","+
		Global.ADMIN_DESC_LARGE+","+Global.ADMIN_DESC_R_DATE+","+Global.ADMIN_DESC_S_DATE+","+
		Global.ADMIN_DESC_CONTROLLED+","+Global.ADMIN_DESC_MULTIPLE+","+
		Global.ADMIN_DESC_ADDITIONS+","+Global.ADMIN_DESC_SORTED+","+Global.ADMIN_DESC_ROW_INDEX+","+
		Global.ADMIN_DESC_ERROR+","+Global.ADMIN_DESC_ID+" "+

		"FROM "+Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.ADMIN_DESC_ID+"=?";
	
	//Get attribute by name
	private static final String GET_BY_NAME=

		"SELECT " +

		Global.PROJECT_NAME+","+Global.ELEMENT+","+ Global.ADMIN_DESC_LABEL+","+
		Global.MD_TYPE+","+
		Global.ADMIN_DESC_LARGE+","+Global.ADMIN_DESC_R_DATE+","+Global.ADMIN_DESC_S_DATE+","+
		Global.ADMIN_DESC_CONTROLLED+","+Global.ADMIN_DESC_MULTIPLE+","+
		Global.ADMIN_DESC_ADDITIONS+","+Global.ADMIN_DESC_SORTED+","+Global.ADMIN_DESC_ROW_INDEX+","+
		Global.ADMIN_DESC_ERROR+","+Global.ADMIN_DESC_ID+" "+

		"FROM "+Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ELEMENT+"=?"+" "+
		"AND "+Global.ADMIN_DESC_LABEL+"=?";

	//Get all attributes for a project
	private static final String GET_ATTRIBUTES_LIST=

		"SELECT " +

		Global.ELEMENT+","+ Global.ADMIN_DESC_LABEL+" "+

		"FROM "+Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+	
		"AND "+Global.MD_TYPE+"=? " +
		"ORDER BY "+Global.ADMIN_DESC_ROW_INDEX;

	//Get the number of attributes in a project and md type
	private static final String GET_ROW_COUNT=

		"SELECT COUNT(*) AS row_count FROM (" +
		"SELECT * FROM " + Global.ADMIN_DESC_TABLE + " " +
		"WHERE " + Global.PROJECT_NAME +"=? AND " + Global.MD_TYPE+"=?"+
		") AS admin_rows";

	//Update based on ID
	private static final String UPDATE_ID=

		"UPDATE "+Global.ADMIN_DESC_TABLE+" "+
		"SET "+Global.ELEMENT+"=?, "+Global.ADMIN_DESC_LABEL+"=?, "+
		Global.ADMIN_DESC_LARGE+"=?, "+Global.ADMIN_DESC_R_DATE+"=?, "+Global.ADMIN_DESC_S_DATE+"=?, "+
		Global.ADMIN_DESC_CONTROLLED+"=?, "+Global.ADMIN_DESC_MULTIPLE+"=?, "+
		Global.ADMIN_DESC_ADDITIONS+"=?, "+ Global.ADMIN_DESC_SORTED+"=?, "+Global.ADMIN_DESC_ROW_INDEX+"=?, "+
		Global.MD_TYPE+"=?, "+Global.ADMIN_DESC_ERROR+"=? "+

		"WHERE "+Global.ADMIN_DESC_ID+"=?";

	//Update the controlled vocab association
	private static final String UPDATE_VOCAB=
		"UPDATE "+Global.ADMIN_DESC_TABLE+" "+
		"SET "+ Global.CONTROLLED_VOCAB_NAME+"=?, "+
		Global.ADMIN_DESC_CONTROLLED+"=? "+
		"WHERE "+Global.ADMIN_DESC_ID+"=?";

	//Get the associated controlled vocab
	private static final String GET_VOCAB=
		"SELECT "+Global.CONTROLLED_VOCAB_NAME+" "+
		"FROM "+ Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ELEMENT+"=?"+" "+
		"AND "+Global.ADMIN_DESC_LABEL+"=?";

	//Get the associated controlled vocab based on ID
	private static final String GET_VOCAB_ID=
		"SELECT "+Global.CONTROLLED_VOCAB_NAME+" "+
		"FROM "+ Global.ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.ADMIN_DESC_ID+"=?";

	//Clean up vocab association
	private static final String EMPTY_ATTRIBUTE_VOCAB=
		"UPDATE "+ Global.ADMIN_DESC_TABLE+" "+
		"SET "+Global.CONTROLLED_VOCAB_NAME+"='' "+
		"WHERE "+Global.CONTROLLED_VOCAB_NAME+"=? ";

	public AdminDescAttributesDAO()
	{
	}


	/**
	 * 
	 * Creates an administrative/descriptive attribute.
	 * @param projectName The project to insert new attribute into. 
	 * @param element The attribute's element.
	 * @param label The attribute's label.
	 * @param isLarge Flag indicating whether the attribute will be displayed with a large edit box.
	 * @param isReadableDate Flag indicating whether the attribute contains a "readable" date.
	 * @param isSearchableDate Flag indicating whether the attribute contains a "searchable" date.
	 * @param isControlled Flag indicating whether the attribute is controlled--that is, tied to a controlled vocabulary.
	 * @param isMultiple (Only applies to controlled attributes) Flag indicating whether multiple vocabulary terms are permitted.
	 * @param isAdditions (Only applies to controlled attributes) Flag indicating whether new vocabulary terms can be added upon edit.
	 * @param isSorted (Only applies to controlled attributes) Flag indicating the controlled vocabulary terms for this attribute will be displayed in alphabetical order.
	 * @param error Flag used by a data import to indicating a non-standard or faulty attribute. Use "false" for normal operations.
	 * @return true if attribute added successfully, false otherwise
	 */
	public static boolean createAdminDescAttribute(String projectName, String element, String label,
			String mdType, boolean isLarge, boolean isReadableDate, boolean isSearchableDate,
			boolean isControlled, boolean isMultiple, boolean isAdditions, boolean isSorted, boolean error)
	{
		//MetaDbHelper.note("Trying to add "+element+"."+label+" to project "+projectName);
		//Check if project, element, attr exist 
		if((!MetaDbHelper.projectExists(projectName)) || (!ElementsDAO.elementExists(element)))
		{
			//MetaDbHelper.note("Error creating attribute.");
			return false;
		}
		boolean attributeAdded = false;

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{	
			try
			{
				int attributeIndex=getNextAttributeIndex(projectName, mdType); //Get the next attribute index
				PreparedStatement createAttribute = conn.prepareStatement(CREATE_ATTRIBUTE); 

				createAttribute.setString(1, projectName); //Set the parameters
				createAttribute.setString(2, element);
				createAttribute.setString(3, label);
				createAttribute.setString(4, mdType);
				createAttribute.setBoolean(5, isLarge);
				createAttribute.setBoolean(6, isReadableDate);
				createAttribute.setBoolean(7, isSearchableDate);
				createAttribute.setBoolean(8, isControlled);
				createAttribute.setBoolean(9, isMultiple);
				createAttribute.setBoolean(10, isAdditions);
				createAttribute.setBoolean(11, isSorted);
				createAttribute.setInt(12, attributeIndex); 
				createAttribute.setString(13, "");
				createAttribute.setBoolean(14, error);

				int res = createAttribute.executeUpdate(); //Update the DB
				attributeAdded=true;
				MetaDbHelper.note("Successfully created attribute, result: "+res);
				//Populate blank fields for the newly created attribute.
				createBlankFields(projectName, element, label, mdType);

				createAttribute.close();
				conn.close(); //Close statement and connection

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				attributeAdded=false;
			}

		}

		return attributeAdded;
	}

	/**
	 * Get an administrative or descriptive attribute by ID.
	 * @param id the unique ID of the attribute desired.
	 * @return the administrative or descriptive attribute with the given ID.
	 */
	public static AdminDescAttribute getAttributeById(int id)
	{
		AdminDescAttribute result=null;

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{		
			try
			{
				PreparedStatement getAttListQuery=conn.prepareStatement(GET_BY_ID); //Set to get all elements SQL

				getAttListQuery.setInt(1, id);

				ResultSet getAttListQueryResult=getAttListQuery.executeQuery();

				if (getAttListQueryResult.next())
				{	
					result=new AdminDescAttribute (

							getAttListQueryResult.getString(Global.PROJECT_NAME), 
							getAttListQueryResult.getString(Global.ELEMENT),
							getAttListQueryResult.getString(Global.ADMIN_DESC_LABEL),
							getAttListQueryResult.getString(Global.MD_TYPE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_LARGE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_R_DATE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_S_DATE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_CONTROLLED),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_MULTIPLE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_ADDITIONS),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_SORTED),
							getAttListQueryResult.getInt(Global.ADMIN_DESC_ROW_INDEX),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_ERROR), 
							getAttListQueryResult.getInt(Global.ADMIN_DESC_ID));
				}
				getAttListQueryResult.close();
				getAttListQuery.close();		
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return result;		
	}

	/**
	 * Get an administrative or descriptive attribute by project name, element name and label.
	 * @param projectName the name of the project in which the desired attribute is contained.
	 * @param element The element of the attribute desired.
	 * @param label The label of the attribute desired.
	 * @return the administrative/descriptive attribute matching the project, element and label.
	 */
	public static AdminDescAttribute getAttributeByName(String projectName, String element, String label)
	{
		AdminDescAttribute result=null;

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{		
			try
			{
				PreparedStatement getAttListQuery=conn.prepareStatement(GET_BY_NAME); //Set to get all elements SQL

				getAttListQuery.setString(1, projectName);
				getAttListQuery.setString(2, element);
				getAttListQuery.setString(3, label);
				
				ResultSet getAttListQueryResult=getAttListQuery.executeQuery();

				if (getAttListQueryResult.next())
				{	
					result=new AdminDescAttribute (

							getAttListQueryResult.getString(Global.PROJECT_NAME), 
							getAttListQueryResult.getString(Global.ELEMENT),
							getAttListQueryResult.getString(Global.ADMIN_DESC_LABEL),
							getAttListQueryResult.getString(Global.MD_TYPE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_LARGE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_R_DATE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_S_DATE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_CONTROLLED),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_MULTIPLE),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_ADDITIONS),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_SORTED),
							getAttListQueryResult.getInt(Global.ADMIN_DESC_ROW_INDEX),
							getAttListQueryResult.getBoolean(Global.ADMIN_DESC_ERROR), 
							getAttListQueryResult.getInt(Global.ADMIN_DESC_ID));
				}
				getAttListQueryResult.close();
				getAttListQuery.close();		
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return result;		
	}
	
	/**
	 * Get a list of admin or desc attributes of a project.
	 * @param projectName The project whose attributes are desired.
	 * @param mdType The metadata type for the attributes which are desired.
	 * @return an ArrayList of AdminDescAttributes for this project of type mdType.
	 */
	public static ArrayList<AdminDescAttribute> getAdminDescAttributes(String projectName, String mdType)
	{
		ArrayList<AdminDescAttribute> attributeList=new ArrayList<AdminDescAttribute>();

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getAttListQuery=conn.prepareStatement(GET_ATTRIBUTES); //Set to get all elements SQL

				getAttListQuery.setString(1, projectName);
				getAttListQuery.setString(2, mdType);

				ResultSet getAttListQueryResult=getAttListQuery.executeQuery();

				if(getAttListQueryResult!=null) //Query result not null.
				{
					//Add AdminDescAttribute objects to the return list.
					while(getAttListQueryResult.next())
					{
						AdminDescAttribute ada=new AdminDescAttribute (
								getAttListQueryResult.getString(Global.PROJECT_NAME), 
								getAttListQueryResult.getString(Global.ELEMENT),
								getAttListQueryResult.getString(Global.ADMIN_DESC_LABEL),
								getAttListQueryResult.getString(Global.MD_TYPE),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_LARGE),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_R_DATE),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_S_DATE),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_CONTROLLED),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_MULTIPLE),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_ADDITIONS),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_SORTED),
								getAttListQueryResult.getInt(Global.ADMIN_DESC_ROW_INDEX),
								getAttListQueryResult.getBoolean(Global.ADMIN_DESC_ERROR), 
								getAttListQueryResult.getInt(Global.ADMIN_DESC_ID));
						attributeList.add(ada);
					}
					getAttListQueryResult.close();
					getAttListQuery.close();
				}									
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return attributeList;
	}


	/**
	 * Get a list of names of admin or desc attributes of a project.
	 * Note, this does not return any AdminDescAttribute objects, only names.
	 * @param projectName The project whose attributes are desired.
	 * @param mdType The metadata type for the attributes which are desired.
	 * @return an ArrayList of String for this project of type mdType.
	 * 
	 */
	public static ArrayList<String> getAdminDescAttributesList(String projectName, String mdType)
	{
		ArrayList<String> attributeList=new ArrayList<String>();
		if(!MetaDbHelper.mdTypeExists(mdType))
		{
			return attributeList;
		}

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getAttListQuery=conn.prepareStatement(GET_ATTRIBUTES_LIST); //Set to get all elements SQL

				getAttListQuery.setString(1, projectName);
				getAttListQuery.setString(2, mdType);

				ResultSet getAttListQueryResult=getAttListQuery.executeQuery();

				if(getAttListQueryResult!=null) //Query result not null.
				{
					//Add AdminDescAttribute objects to the return list.
					while(getAttListQueryResult.next())
					{
						attributeList.add(
								getAttListQueryResult.getString(Global.ELEMENT)+"."+
								getAttListQueryResult.getString(Global.ADMIN_DESC_LABEL)
						);
					}
					getAttListQueryResult.close();
					getAttListQuery.close();
				}									
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return attributeList;
	}

	/**
	 * Delete an administrative/descriptive attribute from a project.
	 * @param id The id of the attribute to delete.
	 * @return true if the attribute was deleted successfully, false otherwise.
	 */
	public static boolean deleteAttribute(int id)
	{
		//MetaDbHelper.note("Attempting to delete attribute "+id);
		boolean deleteSuccessful = false;  //return var

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement deleteAttributeSql = conn.prepareStatement(DELETE_BY_ID);
				deleteAttributeSql.setInt(1, id);

				deleteAttributeSql.executeUpdate();
				deleteAttributeSql.close();
				conn.close(); //Close statement and connection

				deleteSuccessful = true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return deleteSuccessful;
	}

	/**
	 * Delete all attributes from a project. 
	 * Mainly used in import.
	 * @param projectName the project to clean.
	 * @return true if all the attributes have been deleted successfully; false otherwise. 
	 */
	public static boolean deleteAllAttributes(String projectName) {
		if(MetaDbHelper.projectExists(projectName))
		{
			try {
				ArrayList<AdminDescAttribute> attrList = getAdminDescAttributes(projectName, Global.MD_TYPE_ADMIN);
				attrList.addAll(getAdminDescAttributes(projectName, Global.MD_TYPE_DESC));
				//MetaDbHelper.note("Deleting attributes..." +attrList.size()+" attributes in the project "+projectName);

				if (attrList.isEmpty())
					return true;

				for (AdminDescAttribute attr : attrList )
					if (!deleteAttribute(attr.getId()))
						return false;
				return true; 
			}
			catch (Exception e) {
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Set a controlled vocab list for a field in a project. 
	 * @param id attribute ID to tie controlled vocabulary for.
	 * @param vocabName The name of the vocab list to be associated with this element-label pair.
	 * @return true if the controlled vocab settings were updated successfully, false otherwise.
	 */
	public static boolean setControlledVocab(int id, String vocabName)
	{		
		if (vocabName == null || !ControlledVocabDAO.vocabNameExists(vocabName))
			return false;

		boolean updateSuccessful = false;

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{


				PreparedStatement updateVocabName = conn.prepareStatement(UPDATE_VOCAB);
				updateVocabName.setString(1, vocabName);
				updateVocabName.setBoolean(2, true);
				updateVocabName.setInt(3, id);

				updateVocabName.executeUpdate();
				updateVocabName.close();
				conn.close(); //Close statement and connection

				updateSuccessful = true;

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return updateSuccessful;
	}

	/**
	 * An action before deleting a vocab from the Controlled Vocab table, setting
	 * all old vocab names to blank
	 * @param vocabName vocab to be emptied
	 * @return true if successful, false otherwise
	 */
	public static boolean emptyControlledVocab(String vocabName)
	{
		if(!ControlledVocabDAO.vocabNameExists(vocabName)) //Make sure the vocab name exists
			return false;

		boolean emptySuccessful = false;

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement emptyVocabName=conn.prepareStatement(EMPTY_ATTRIBUTE_VOCAB);
				emptyVocabName.setString(1, vocabName);
				emptyVocabName.executeUpdate();
				emptyVocabName.close();
				conn.close(); //Close statement and connection

				emptySuccessful = true;

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return emptySuccessful;
	}

	/**
	 * Get the controlled vocab name from an attribute ID
	 * @param id The unique ID of the attribute to get the tied controlled vocabulary for.
	 */
	public static String getControlledVocab(int id) {
		String result = "";

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getVocabQuery=conn.prepareStatement(GET_VOCAB_ID);

				getVocabQuery.setInt(1, id);

				ResultSet getVocabQueryResult=getVocabQuery.executeQuery();
				if (getVocabQueryResult.next())
					result = getVocabQueryResult.getString(Global.CONTROLLED_VOCAB_NAME);
				//if (!result.equals(""))
				//MetaDbHelper.logEvent("AdminDescAttributesDAO", "getControlledVocab", "Vocab retrieved: "+result);
				getVocabQueryResult.close();
				getVocabQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return result;
	}
	
	/**
	 * Get a controlld vocab name tied to a field based on element-label pair
	 * @param projectName Project name to get the vocab.
	 * @param element The element for the field that the tied vocab is desired.
	 * @param label The label for the field that the tied vocab is desired.
	 * @return a String representing the name of the controlled vocabulary tied to element-label.
	 */
	public static String getControlledVocab(String projectName, String element, String label) {
		String result = "";

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getVocabQuery=conn.prepareStatement(GET_VOCAB);

				getVocabQuery.setString(1, projectName);
				getVocabQuery.setString(2, element);
				getVocabQuery.setString(3, label);

				ResultSet getVocabQueryResult=getVocabQuery.executeQuery();
				if (getVocabQueryResult.next())
					result = getVocabQueryResult.getString(Global.CONTROLLED_VOCAB_NAME);
				//if (!result.equals(""))
				//	MetaDbHelper.logEvent("AdminDescAttributesDAO", "getControlledVocab", "Vocab retrieved: "+result);
				getVocabQueryResult.close();
				getVocabQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return result;
	}

	/**
	 * Checks if a given attribute exists in a project.
	 * @param projectName The project to be checked for the attribute. 
	 * @param element The requested element. 
	 * @param label The requested label.  
	 * @param id The unique ID of the attribute.
	 * @return True if attribute exists, false otherwise
	 */
	public static boolean duplicateAttribute(String projectName, String element, String label, int id)
	{
		boolean exists=false;
		Connection conn=Conn.initialize();
		if (conn!=null)
		{
			try
			{
				PreparedStatement checkExists=conn.prepareStatement(CHECK_EXISTS);
				checkExists.setString(1, projectName);
				checkExists.setString(2, element);
				checkExists.setString(3, label);

				ResultSet check=checkExists.executeQuery();
				if (check.next()) //Search for rows 
				{
					int foundId=check.getInt(Global.ADMIN_DESC_ID);
					if ((id != foundId ) || id==-1)
					{
						exists=true; // Same ID	or flagged as 'new'
						//MetaDbHelper.note("Found row: element ="+check.getString(Global.ELEMENT)+", label="+check.getString(Global.ADMIN_DESC_LABEL));
						//MetaDbHelper.note("ID is -1: "+(id==-1));
						//MetaDbHelper.note("ID differs: "+(id != foundId));
						//MetaDbHelper.note("Duplicate attribute: "+element+"."+label+" already exists in the project "+projectName);
						//MetaDbHelper.note("Updating ID: "+id+", found ID: "+foundId);

					}
				}

				check.close();
				checkExists.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}			
		}
		return exists;
	}
	/**
	 * Get the next available attribute index for a project and metadata-type.
	 * @param projectName The project name.
	 * @param mdType The type of metadata.
	 * @return an int representing the next available 
	 */
	private static int getNextAttributeIndex(String projectName, String mdType)
	{
		int result = 0;

		Connection conn=Conn.initialize(); //Establish connection

		if(conn!=null)
		{
			try
			{
				PreparedStatement getRowQuery=conn.prepareStatement(GET_ROW_COUNT); //Bind subquery to PreparedStatement 
				getRowQuery.setString(1, projectName);
				getRowQuery.setString(2, mdType);
				ResultSet getRowResult=getRowQuery.executeQuery();

				if(getRowResult!=null) //Query result not null.
				{
					getRowResult.next();
					result = getRowResult.getInt("row_count");
					getRowResult.close();

				}									
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return result;
	}



	/**
	 * Update an administrative or descriptive attribute
	 * @param projectName The project to insert new attribute into. 
	 * @param id The attribute's unique ID.
	 * @param newElement The attribute's new element.
	 * @param newLabel The attribute's new label.
	 * @param isLarge Flag indicating whether the attribute will be displayed with a large edit box.
	 * @param isReadableDate Flag indicating whether the attribute contains a "readable" date.
	 * @param isSearchableDate Flag indicating whether the attribute contains a "searchable" date.
	 * @param isControlled Flag indicating whether the attribute is controlled--that is, tied to a controlled vocabulary.
	 * @param isMultiple (Only applies to controlled attributes) Flag indicating whether multiple vocabulary terms are permitted.
	 * @param isAdditions (Only applies to controlled attributes) Flag indicating whether new vocabulary terms can be added upon edit.
	 * @param isSorted (Only applies to controlled attributes) Flag indicating the controlled vocabulary terms for this attribute will be displayed in alphabetical order.
	 * @param rowIndex the index of the attribute
	 * @return true if successfully updated, false otherwise
	 */
	public static boolean update(String projectName, int id, String newElement,
			String newLabel, String mdType, boolean isLarge, boolean isReadableDate,
			boolean isSearchableDate, boolean isControlled, boolean isMultiple, boolean isAdditions,
			boolean isSorted, int rowIndex) throws DuplicateAttributeException

			{
		boolean success = false;
		boolean isNewAttribute = false;
		if(duplicateAttribute(projectName, newElement, newLabel, id))
			throw new DuplicateAttributeException("Duplicate attribute.");

		// This is a new attribute if the input id=-1.
		if (id == -1) 
			isNewAttribute = true;

		try {
			// If it is a new attribute, create it.
			if (isNewAttribute)
				try
			{		
					createAdminDescAttribute(projectName, newElement,
							newLabel, mdType, isLarge, isReadableDate, isSearchableDate, isControlled,
							isMultiple, isAdditions, isSorted, false);
					return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}

			// Otherwise, the attribute should exist in the system already.
			else {
				Connection conn = Conn.initialize(); // Establish connection
				if (conn != null) {
					PreparedStatement update = conn.prepareStatement(UPDATE_ID);
					if (!isControlled) {
						isAdditions = false;
						setControlledVocab(id, "");
					}

					update.setString(1, newElement);
					update.setString(2, newLabel);
					update.setBoolean(3, isLarge);
					update.setBoolean(4, isReadableDate);
					update.setBoolean(5, isSearchableDate);
					update.setBoolean(6, isControlled);
					update.setBoolean(7, isMultiple);
					update.setBoolean(8, isAdditions);
					update.setBoolean(9, isSorted);
					update.setInt(10, rowIndex);
					update.setString(11, mdType);
					update.setBoolean(12, false);
					update.setInt(13, id);

					//					MetaDbHelper.logEvent("AdminDescAttributesDAO", "update",
					//							"Field id " + id + " in project " + projectName
					//									+ " modified."); // debugging
					update.executeUpdate();

					update.close();
					conn.close(); // Close statement and connection

					success = true;

				}
			}

		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return success;
			}

	/**
	 * Populate the data table with blank values for one element-label pairing.
	 * @param projectName The project to populate.
	 * @param element The element of the attribute to insert blank-data for.
	 * @param label The label of the attribute to insert blank-data for.
	 * @param mdType The type of metadata this attribute belongs to.
	 */
	public static boolean createBlankFields(String projectName,
			String element, String label, String mdType) 
	{
		if (!MetaDbHelper.projectExists(projectName))
			return false;

		ArrayList<Integer> itemNumbers = MetaDbHelper.getItemNumbers(projectName);
		if (itemNumbers != null) 
		{
			Connection conn = Conn.initialize();
			if (conn != null) 
				try
			{

					for (Integer itemNumber : itemNumbers) 
					{
						// MetaDbHelper.note("Adding blank data for project: "+projectName+", item "+itemNumber+", element "+element+"."+label);
						PreparedStatement addBlank = conn.prepareStatement(CREATE_BLANK_FIELDS);
						addBlank.setString(1, projectName);
						addBlank.setString(2, mdType);
						addBlank.setInt(3, itemNumber);
						addBlank.setString(4, element);
						addBlank.setString(5, label);
						addBlank.setString(6, "");
						addBlank.executeUpdate();
						addBlank.close();
					}
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
	 * Populate the data table with blank values for one element-label pairing.
	 * @param projectName The project to populate.
	 * @param itemNumber the item number to create blank
	 * @param element The element of the attribute to insert blank-data for.
	 * @param label The label of the attribute to insert blank-data for.
	 * @param mdType The type of metadata this attribute belongs to.
	 */
	public static boolean createBlankFields(String projectName,
			int itemNumber, String element, String label, String mdType) 
	{
		if (!MetaDbHelper.projectExists(projectName))
			return false;

		Connection conn = Conn.initialize();
		if (conn != null) 
			try
		{

				PreparedStatement addBlank = conn.prepareStatement(CREATE_BLANK_FIELDS);
				addBlank.setString(1, projectName);
				addBlank.setString(2, mdType);
				addBlank.setInt(3, itemNumber);
				addBlank.setString(4, element);
				addBlank.setString(5, label);
				addBlank.setString(6, "");
				addBlank.executeUpdate();
				addBlank.close();
				conn.close();
				return true;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);	
			return false;
		}
		return false; 
	}

	public static class DuplicateAttributeException extends Exception 
	{
		private static final long serialVersionUID = 1L;

		public DuplicateAttributeException() {}
		public DuplicateAttributeException(String msg) {
			super(msg);
		}
	}

}

