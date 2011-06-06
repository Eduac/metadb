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
package edu.lafayette.metadb.model.projects;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.attributes.TechAttributesDAO;
import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.imagemgt.*;
import edu.lafayette.metadb.model.fileio.*;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Class which performs operations at the project level, such as creating, deleting, clearing data, etc.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
public class ProjectsDAO {

	private static final String CREATE_PROJECT=

		"INSERT INTO "+Global.PROJECTS_TABLE+
		"("+Global.PROJECT_NAME+","+Global.PROJECT_NOTES+","+Global.PROJECT_BASE_URL+")"+" "+
		"VALUES (?, ?, ?)";

	private static final String UPDATE_PROJECT=

		"UPDATE "+Global.PROJECTS_TABLE+" "+
		"SET "+Global.PROJECT_BASE_URL+"=? "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String DELETE_PROJECT=

		"DELETE FROM "+Global.PROJECTS_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String DELETE_ADMIN_DESC_DATA=

		"UPDATE "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"SET "+Global.ITEM_ADMIN_DESC_DATA+"=''"+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String DELETE_TECH_DATA=

		"DELETE FROM "+Global.ITEMS_TECH_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String GET_PROJECT_DATA=

		"SELECT "+Global.PROJECT_NAME+","+Global.PROJECT_NOTES+","+Global.PROJECT_BASE_URL+" "+
		"FROM "+Global.PROJECTS_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String GET_PROJECT_LIST = 
		"SELECT * FROM "+Global.PROJECTS_LIST_VIEW;

	public ProjectsDAO()
	{					
	}

	/**
	 * @param projectName The name of the project to add.
	 * @param notes Notes for the project.
	 * @return true if project was added, false otherwise.
	 * Creates a new project with the given parameters.
	 */
	public static boolean createProject(String projectName, String notes)
	{
		if(MetaDbHelper.projectExists(projectName))
		{
			//MetaDbHelper.logEvent("ProjectsDAO", "createProject", "Project "+projectName+" already exists.");
			return false;
		}
		boolean success = true;
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{	
			try
			{					
				PreparedStatement createProj=conn.prepareStatement(CREATE_PROJECT); 

				createProj.setString(1, projectName); //Set the parameters
				createProj.setString(2, notes);
				createProj.setString(3, Global.DEFAULT_BASE_URL);

				createProj.executeUpdate(); //Update the DB

				createProj.close();
				conn.close(); //Close statement and connection

				//Create default derivative-settings.
				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_THUMB_SETTING, Global.DERIV_DEFAULT_SIZE_THUMB, Global.DERIV_DEFAULT_SIZE_THUMB,
						Global.DERIV_DEFAULT_BRAND_TEXT, Global.DERIV_DEFAULT_THUMB_BG, Global.DERIV_DEFAULT_THUMB_FG, Global.DERIV_DEFAULT_ANNOTATION_MODE);

				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING, Global.DERIV_DEFAULT_SIZE_CUSTOM, Global.DERIV_DEFAULT_SIZE_CUSTOM, 
						Global.DERIV_DEFAULT_BRAND_TEXT, Global.DERIV_DEFAULT_CUSTOM_BG, Global.DERIV_DEFAULT_CUSTOM_FG, Global.DERIV_DEFAULT_ANNOTATION_MODE);

				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_ZOOM_SETTING, Global.DERIV_DEFAULT_SIZE_ZOOM, Global.DERIV_DEFAULT_SIZE_ZOOM, 
						Global.DERIV_DEFAULT_BRAND_TEXT, Global.DERIV_DEFAULT_ZOOM_BG, Global.DERIV_DEFAULT_ZOOM_FG, Global.DERIV_DEFAULT_ANNOTATION_MODE);

				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_FULLSIZE_SETTING, 0, 0, 
						Global.DERIV_DEFAULT_BRAND_TEXT, Global.DERIV_DEFAULT_FULL_BG, Global.DERIV_DEFAULT_FULL_FG, Global.DERIV_DEFAULT_ANNOTATION_MODE);
				//MetaDbHelper.note("Derivative settings (default) for "+projectName+" created");

				//Create the automatically-generated fields
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "FileName");			
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "DPI");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "BitDepth");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "DateModified");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "FileFormat");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "Checksum");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PixelHeight");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PixelWidth" );
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PhysicalHeight");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PhysicalWidth");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_ZOOM_ELEMENT, Global.TECH_ZOOM_LABEL);
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_DOWNLOAD_ELEMENT, Global.TECH_DOWNLOAD_LABEL);

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				success = false;
			}
		}
		return success;
	}


	/**
	 * Creates a new project using an existing project as a template.
	 * @param baseProjectName The name of the project to use as a template.
	 * @param projectName The name of the new project to create.
	 * @param notes Notes for the project.
	 * @param baseUrl The derivative host for the project.
	 * @return true if project was added, false otherwise.
	 * 
	 */
	public static boolean createProject(String baseProjectName, String projectName, String notes, String baseUrl)
	{
		if(MetaDbHelper.projectExists(projectName) || !MetaDbHelper.projectExists(baseProjectName))
		{
			//MetaDbHelper.logEvent("ProjectsDAO", "createProject", "Duplicate project or base project doesn't exist.");
			return false;
		}
		boolean success = true;
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{	
			try
			{					
				PreparedStatement createProj=conn.prepareStatement(CREATE_PROJECT); 

				createProj.setString(1, projectName); //Set the parameters
				createProj.setString(2, notes);
				createProj.setString(3, getProjectData(baseProjectName).getBaseUrl());

				createProj.executeUpdate(); //Update the DB

				createProj.close();
				conn.close(); //Close statement and connection
				//MetaDbHelper.note("Project "+projectName+" created");
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_SYSTEM, "Project created: "+projectName);

				//Copy the deriv. setings.
				DerivativeSetting custom=DerivativeDAO.getDerivativeSetting(baseProjectName, Global.DERIV_CUSTOM_SETTING);
				DerivativeSetting large=DerivativeDAO.getDerivativeSetting(baseProjectName, Global.DERIV_ZOOM_SETTING);
				DerivativeSetting full=DerivativeDAO.getDerivativeSetting(baseProjectName, Global.DERIV_FULLSIZE_SETTING);

				//Create default derivative-settings.
				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_THUMB_SETTING, Global.DERIV_DEFAULT_SIZE_THUMB, Global.DERIV_DEFAULT_SIZE_THUMB,
						 Global.DERIV_DEFAULT_BRAND_TEXT, Global.DERIV_DEFAULT_THUMB_BG, Global.DERIV_DEFAULT_THUMB_FG, Global.DERIV_DEFAULT_ANNOTATION_MODE);

				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING, custom.getMaxWidth(), custom.getMaxHeight(), 
						custom.getBrand(), custom.getBgColor(), custom.getFgColor(), custom.getAnnotationMode());

				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_ZOOM_SETTING, large.getMaxWidth(), large.getMaxHeight(), 
						large.getBrand(), large.getBgColor(), large.getFgColor(), large.getAnnotationMode());

				success &= DerivativeDAO.addDerivativeSetting(projectName, Global.DERIV_FULLSIZE_SETTING, 0, 0, 
						full.getBrand(), full.getBgColor(), full.getFgColor(), full.getAnnotationMode());

				MetaDbHelper.note("Derivative settings for "+projectName+" based on project "+baseProjectName+" created");

				//Create the automatically-generated fields
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "FileName");			
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "DPI");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "BitDepth");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "DateModified");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "FileFormat");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "Checksum");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PixelHeight");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PixelWidth" );
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PhysicalHeight");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "PhysicalWidth");
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_ZOOM_ELEMENT, Global.TECH_ZOOM_LABEL);
				TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_DOWNLOAD_ELEMENT, Global.TECH_DOWNLOAD_LABEL);


				//Copy the fields
				ArrayList<AdminDescAttribute> adminList=AdminDescAttributesDAO.getAdminDescAttributes(baseProjectName, Global.MD_TYPE_ADMIN);
				ArrayList<AdminDescAttribute> descList=AdminDescAttributesDAO.getAdminDescAttributes(baseProjectName, Global.MD_TYPE_DESC);
				adminList.addAll(descList);

				for (AdminDescAttribute attr: adminList)
				{
					AdminDescAttributesDAO.createAdminDescAttribute(projectName, attr.getElement(), attr.getLabel(), 
							attr.getMdType(), attr.isLarge(), attr.isReadableDate(), attr.isSearchableDate(), attr.isControlled(), attr.isMultiple(), 
							attr.isAdditions(), attr.isSorted(), attr.isError());

					/*
					 * Quick hack to get the vocab associations working.
					 */
					AdminDescAttribute newAttr=AdminDescAttributesDAO.getAttributeByName(projectName, attr.getElement(), attr.getLabel());
					int attrId=newAttr.getId();
					String vocabName=AdminDescAttributesDAO.getControlledVocab(baseProjectName, attr.getElement(), attr.getLabel());
					MetaDbHelper.note("The vocab "+vocabName+" will be linked to "+attrId+"-"+newAttr.getElement()+"."+newAttr.getLabel());
					AdminDescAttributesDAO.setControlledVocab(attrId, vocabName);
				}

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				success = false;
			}
		}
		return success;
	}

	/**
	 * Updates a project's deriv host URL.
	 * @param name The name of the project to change
	 * @param newURL new deriv host URL for project
	 * @return true if project was updated successfully, false otherwise.
	 */
	public static boolean updateURL(String name, String newURL)
	{
		if(!MetaDbHelper.projectExists(name))
		{
			//MetaDbHelper.logEvent("ProjectsDAO", "updateProject", "Project "+name+" doesn't exist.");
			return false;
		}
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{	
				PreparedStatement updateProj=conn.prepareStatement(UPDATE_PROJECT); 

				updateProj.setString(1, newURL);
				updateProj.setString(2, name); //Set the parameters

				updateProj.executeUpdate(); //Update the DB

				updateProj.close();
				conn.close(); //Close statement and connection
				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				//debugging statement
			}
		}
		return false;
	}

	/** 
	 * Deletes a project from the system.
	 * Also deletes all derivatives created from this project.
	 * @param projectName The name of the project to be deleted.
	 * @return true if the project was deleted, false otherwise
	 */
	public static boolean deleteProject(String projectName)
	{
		boolean dataDeleted = false;
		boolean imagesDeleted = false;
		if(!MetaDbHelper.projectExists(projectName))
		{
			MetaDbHelper.logEvent("ProjectsDAO", "deleteProject", "The project "+projectName+" doesn't exist!");
			return false;

		}

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement deleteProj=conn.prepareStatement(DELETE_PROJECT);

				deleteProj.setString(1, projectName); //Set parameters

				deleteProj.executeUpdate();
				MetaDbHelper.logEvent("ProjectsDAO", "deleteProject","Project "+projectName+" deleted"); //debugging

				deleteProj.close();
				conn.close(); //Close statement and connection
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_SYSTEM, "Project deleted: "+projectName);
				dataDeleted=true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}
		}
		else
		{
			return false;
		}
		if(dataDeleted==true)
		{
			//MetaDbHelper.note("Data deleted successfully");
			imagesDeleted=FileManager.deleteDerivatives(projectName);
			//MetaDbHelper.note("Images deleted? "+imagesDeleted);
		}
		return dataDeleted&&imagesDeleted;
	}

	/**
	 * Delete admin/descriptive metadata for a project.
	 * @param projectName Project to clear data for.
	 * @return true if data cleared, false otherwise
	 */
	public static boolean deleteAdminDescData(String projectName)
	{
		if(!MetaDbHelper.projectExists(projectName))
		{
			//MetaDbHelper.logEvent("ProjectsDAO", "deleteProject", "The project "+projectName+" doesn't exist!");
			return false;
		}

		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement deleteAdminDescData=conn.prepareStatement(DELETE_ADMIN_DESC_DATA);

				deleteAdminDescData.setString(1, projectName); //Set parameters

				deleteAdminDescData.executeUpdate();
				MetaDbHelper.logEvent("ProjectsDAO", "deleteAdminDescData","Admin/desc data for "+projectName+" deleted"); //debugging

				deleteAdminDescData.close();
				conn.close(); //Close statement and connection
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_SYSTEM, "Admin/Desc data deleted: "+projectName);
				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	public static boolean deleteDerivatives(String projectName)
	{
		return FileManager.deleteDerivatives(projectName);
	}

	/**
	 * Delete all technical metadata for a project.
	 * @param projectName Project to clear data for.
	 * @return true if data cleared, false otherwise
	 */
	public static boolean deleteTechData(String projectName)
	{
		if(!MetaDbHelper.projectExists(projectName))
		{
			//MetaDbHelper.logEvent("ProjectsDAO", "deleteTechData", "The project "+projectName+" doesn't exist!");
			return false;
		}
		
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement deleteAdminDescData=conn.prepareStatement(DELETE_TECH_DATA);

				deleteAdminDescData.setString(1, projectName); //Set parameters

				deleteAdminDescData.executeUpdate();
				MetaDbHelper.logEvent("ProjectsDAO", "deleteTechData","Technical data for "+projectName+" deleted"); //debugging

				deleteAdminDescData.close();
				conn.close(); //Close statement and connection
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_SYSTEM, "Technical data deleted: "+projectName);
				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	/**
	 * Gets a project from the system. 
	 * @param projectName The project name whose data is to be obtained.
	 * @return a Project object representing the information for the project
	 */
	public static Project getProjectData(String projectName)
	{
		Project requestedProject=null; //initialize return object
		if(!MetaDbHelper.projectExists(projectName)) //If project doesn't exist
		{
			//MetaDbHelper.logEvent("ProjectsDAO", "getProjectData","Error: project "+projectName+" doesn't exist.");
			return null;
		}
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement getProjectQuery=conn.prepareStatement(GET_PROJECT_DATA);
				getProjectQuery.setString(1, projectName); //set parameter

				ResultSet projectData = getProjectQuery.executeQuery();


				if(projectData.next())
				{
					String projectNotes = projectData.getString(Global.PROJECT_NOTES);
					String projectUrl=projectData.getString(Global.PROJECT_BASE_URL);
					requestedProject = new Project(projectName, projectNotes, projectUrl);
				}
				projectData.close();
				getProjectQuery.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return requestedProject;
	}

	/**
	 * Get a list of projects.
	 * @return a List of the names of all the projects.
	 */
	public static List<String> getProjectList()
	{
		ArrayList<String> list = new ArrayList<String>();

		Connection conn =Conn.initialize() ; //Establish connection
		if(conn!=null)
		{
			try
			{

				PreparedStatement getProjectQuery=conn.prepareStatement(GET_PROJECT_LIST);
				ResultSet projectList = getProjectQuery.executeQuery();

				while (projectList.next()) 
				{ 
					String projectName=projectList.getString(Global.PROJECT_NAME);
					list.add(projectName);
				}
				projectList.close();

				getProjectQuery.close();
				conn.close();
			}	
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return list;
	}

}