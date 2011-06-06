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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import edu.lafayette.metadb.model.commonops.Conn;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

/**
 * This class serves as the access object for a project's technical
 * attributes in MetaDB. It provides various functions, such as 
 * adding, retrieving, or deleting attributes. 
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 February 2011
 */
public class TechAttributesDAO {



	private static final String CREATE_TECH_ATTRIBUTE=

		"INSERT INTO "+Global.TECH_TABLE+" "+
		"("+Global.PROJECT_NAME+","+Global.TECH_ELEMENT+","+Global.TECH_LABEL+
		")"+" "+
		"VALUES (? , ?, ?)";

	@SuppressWarnings("unused")
	private static final String DELETE_TECH_ATTRIBUTES=

		"DELETE FROM "+Global.TECH_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	@SuppressWarnings("unused")
	private static final String DELETE_TECH_ATTRIBUTE=

		"DELETE FROM "+Global.TECH_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.TECH_ELEMENT+"=?"+" "+
		"AND "+Global.TECH_LABEL+"=?";

	private static final String GET_TECH_ATTRIBUTES=	

		"SELECT "
		+Global.PROJECT_NAME+","+
		Global.TECH_ELEMENT+","+Global.TECH_LABEL+" "+

		"FROM "+Global.TECH_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"ORDER BY "+Global.TECH_ELEMENT+", "+Global.TECH_LABEL;

	private static final String CHECK_EXISTS= 

		"SELECT * "+" "+
		"FROM "+Global.TECH_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.TECH_ELEMENT+"=?"+" "+
		"AND "+Global.TECH_LABEL+"=?";
	
	@SuppressWarnings("unused")
	private static final String UPDATE=
		"UPDATE "+Global.TECH_TABLE+
		" SET "+Global.TECH_LABEL+"=?"+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.TECH_ELEMENT+"=? "+" ";

	private static final String CREATE_BLANK_FIELDS=

		"INSERT INTO "+Global.ITEMS_TECH_TABLE+
		"("+
		Global.PROJECT_NAME+","+
		Global.ITEM_NUMBER+","+
		Global.TECH_ELEMENT+","+
		Global.TECH_LABEL+","+
		Global.ITEM_TECH_DATA+")"+" "+	
		"VALUES (?, ?, ?, ?, ?)";

	public TechAttributesDAO()
	{

	}

	/**
	 * Create a new technical attribute in a project. 
	 * @param projectName The project to create a new technical attribute in. 
	 * @param element The element for the technical attribute to be created. 
	 * @param label The label for the technical attribute to be created. 
	 * @return true if the technical attribute was successfully created, false otherwise.
	 */
	public static boolean createTechnicalAttribute(String projectName, String element, String label)
	{
		if(!MetaDbHelper.projectExists(projectName))
		{
			return false;//Project doesn't exist
		}
		else if(attributeExists(projectName, element, label)) //Attribute already exists
		{
			return false;
		}

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{	
			try
			{	

				PreparedStatement createAttribute=conn.prepareStatement(CREATE_TECH_ATTRIBUTE); 
				createAttribute.setString(1, projectName); //Set the parameters
				createAttribute.setString(2, element);
				createAttribute.setString(3, label);

				//MetaDbHelper.logEvent("TechAttributesDAO", "createTechnicalAttribute", "Parameters have been set for createAttribute SQL");
				createAttribute.executeUpdate(); //Update the DB

				createBlankFields(projectName, element, label);
				createAttribute.close();

				MetaDbHelper.logEvent("TechAttributesDAO", "createTechnicalAttribute", "createTechnicalAttribute: Attribute "+element+"."+label+" added to project "+projectName+"."); //debugging statement

				conn.close(); //Close statement and connection
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
	 * Gets a list of technical attributes for a project. 
	 * @param projectName The project whose technical attributes are desired. 
	 * @return An ArrayList of technical attributes for this project. 
	 */
	public static ArrayList<TechAttribute> getTechAttributes(String projectName)
	{
		if(!MetaDbHelper.projectExists(projectName))
		{ 
			//MetaDbHelper.logEvent("TechAttributesDAO", "getTechAttributes","The project "+projectName+" doesn't exist!");
			return null;
		}
		ArrayList<TechAttribute> attributeList=new ArrayList<TechAttribute>();

		Connection conn=Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getTechAttListQuery=conn.prepareStatement(GET_TECH_ATTRIBUTES); //Set to get all elements SQL
				getTechAttListQuery.setString(1, projectName);
				ResultSet getTechAttListQueryResult=getTechAttListQuery.executeQuery();

				//MetaDbHelper.logEvent("TechAttributesDAO", "getTechAttributes","getAttributeList: Attributes found.");
				while(getTechAttListQueryResult.next())
					{
						attributeList.add
						(new TechAttribute 
								(
										getTechAttListQueryResult.getString(Global.PROJECT_NAME), 
										getTechAttListQueryResult.getString(Global.TECH_ELEMENT),
										getTechAttListQueryResult.getString(Global.TECH_LABEL)										
								)
						);
					}
					getTechAttListQuery.close();
					getTechAttListQueryResult.close();
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
	 * Check if a technical attribute exists in a project. 
	 * @param projectName The project to be searched.
	 * @param element The element that is sought for. 
	 * @param label The label to be sought for
	 * @return true if the technical attribute exists in the project, false otherwise.
	 */
	public static boolean attributeExists(String projectName, String element, String label)
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
				if (check.next())
					exists=true;

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
		//MetaDbHelper.note("Technical attribute "+element+"."+label+" in "+projectName+": "+exists);
		return exists;
	}

	/**
	 * Populate the tech data table with blank values for one element-label pairing.
	 * @param projectName The project in which to generate blank data.
	 * @param element The element of the attribute for which to generate blank data.
	 * @param label The label of the attribute for which to generate blank data.
	 */
	private static boolean createBlankFields(String projectName, String element, String label)
	{
		ArrayList<Integer> itemNumbers=MetaDbHelper.getItemNumbers(projectName);
		if(itemNumbers!=null)
		{
			Connection conn=Conn.initialize();
			if(conn!=null)
			{
				try
				{
					//MetaDbHelper.note("Adding blank data..project "+projectName+" has "+itemNumbers.size()+" items");
					for(Integer itemNumber: itemNumbers)
					{

						//MetaDbHelper.note("Adding blank data for project: "+projectName+", item "+itemNumber+", tech element "+element+"."+label);
						PreparedStatement addBlank=conn.prepareStatement(CREATE_BLANK_FIELDS);
						addBlank.setString(1, projectName);
						addBlank.setInt(2, itemNumber);
						addBlank.setString(3, element);
						addBlank.setString(4, label);
						addBlank.setString(5, "");
						addBlank.executeUpdate();	
						addBlank.close();
					}
					conn.close();
					return true;
				}
				catch(Exception e)
				{
					MetaDbHelper.logEvent(e);
				}
			}
		}
		return false;
	}




}

