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
package edu.lafayette.metadb.model.items;

import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.fileio.FileManager;
import edu.lafayette.metadb.model.imagemgt.DerivativeSetting;
import edu.lafayette.metadb.model.imagemgt.ImageParser;
import edu.lafayette.metadb.model.imagemgt.DerivativeDAO;
import edu.lafayette.metadb.model.metadata.AutoTechData;
import edu.lafayette.metadb.model.metadata.Metadata;
import edu.lafayette.metadb.model.metadata.TechnicalDataDAO;
import edu.lafayette.metadb.model.projects.ProjectsDAO;
import edu.lafayette.metadb.model.syslog.SysLogDAO;
import edu.lafayette.metadb.model.attributes.*;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.*;

/**
 * Class which performs operations within a project, at the item level. 
 * Performs processing of master files for projects, creating new items in projects, and 
 * acts as a helper to pass complicated item information to the UI. 
 * 
 * Also handles some path translation and item editing concurrency.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0
 *
 */
public class ItemsDAO extends Thread
{
	private String projectName="";
	private int[] itemNumbers={};
	private boolean newOnly=false;

	private static final String ADD_ITEM=

		"INSERT INTO "+Global.ITEM_TABLE+" "+
		"("+Global.PROJECT_NAME+","+Global.ITEM_NUMBER+","+Global.ITEM_FILE_NAME+","+Global.ITEM_THUMB_FILE_NAME+
		","+Global.ITEM_CUSTOM_FILE_NAME+","+Global.ITEM_ZOOM_FILE_NAME+","+Global.ITEM_FULLSIZE_FILE_NAME+","+Global.ITEM_LOCKER+","+Global.ITEM_CHECKSUM+","+Global.ITEM_DATE+")"+
		" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String ADD_TECH_DATA=

		"INSERT INTO "+Global.ITEMS_TECH_TABLE+
		"("+Global.PROJECT_NAME+","+Global.ITEM_NUMBER+","+Global.TECH_ELEMENT+","+
		Global.TECH_LABEL+","+Global.ITEM_TECH_DATA+")"+" VALUES (?, ?, ?, ?, ?)";

	private static final String ADD_ADMIN_DATA=

		"INSERT INTO "+Global.ITEMS_ADMIN_DESC_TABLE+
		"("+Global.PROJECT_NAME+","+Global.ITEM_NUMBER+","+Global.MD_TYPE+","+Global.ELEMENT+","+Global.ADMIN_DESC_LABEL+","+Global.ITEM_ADMIN_DESC_DATA+")"+
		"VALUES ( ?, ?, ?, ?, ?, ?)";

	private static final String ADD_DESC_DATA=

		"INSERT INTO "+Global.ITEMS_ADMIN_DESC_TABLE+
		"("+Global.PROJECT_NAME+","+Global.ITEM_NUMBER+","+Global.MD_TYPE+","+Global.ELEMENT+","+Global.ADMIN_DESC_LABEL+","+Global.ITEM_ADMIN_DESC_DATA+")"+
		"VALUES ( ?, ?, ?, ?, ?, ?)";

	private static final String ITEM_EXISTS=

		"SELECT "+Global.ITEM_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_FILE_NAME+"=?";

	private static final String GET_FILE_NAME=

		"SELECT "+Global.ITEM_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String GET_THUMB_FILE_NAME=

		"SELECT "+Global.ITEM_THUMB_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String GET_CUSTOM_FILE_NAME=

		"SELECT "+Global.ITEM_CUSTOM_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String GET_ZOOM_FILE_NAME=

		"SELECT "+Global.ITEM_ZOOM_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String GET_FULL_FILE_NAME=

		"SELECT "+Global.ITEM_FULLSIZE_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";


	private static final String GET_ITEM_NUMBER=

		"SELECT "+Global.ITEM_NUMBER+" " +
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_FILE_NAME+"=?";

	private static final String GET_NEXT_ITEM_NUMBER=

		"SELECT MAX("+Global.ITEM_NUMBER+") "+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String GET_CHECKSUM=

		"SELECT "+Global.ITEM_CHECKSUM+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String GET_DATE=

		"SELECT "+Global.ITEM_DATE+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	@SuppressWarnings("unused")
	private static final String UPDATE_CHECKSUM=

		"UPDATE "+Global.ITEM_TABLE+" "+
		"SET "+Global.ITEM_CHECKSUM+"=?, "+Global.ITEM_DATE+"=?"+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String UPDATE_DERIV_PATHS=

		"UPDATE "+Global.ITEM_TABLE+" "+
		"SET "+Global.ITEM_CUSTOM_FILE_NAME+"=?, "+	Global.ITEM_ZOOM_FILE_NAME+"=? "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String GET_LOCKER=

		"SELECT "+Global.ITEM_LOCKER+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String SET_LOCKER=

		"UPDATE "+Global.ITEM_TABLE+" "+
		"SET "+Global.ITEM_LOCKER+"=?"+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_NUMBER+"=?";

//	private static final String CLEAN_LOCKER_ONE=
//
//		"UPDATE "+Global.ITEM_TABLE+" "+
//		"SET "+Global.ITEM_LOCKER+"=?"+" "+
//		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
//		"AND "+Global.ITEM_LOCKER+"=?"+" "+
//		"AND "+Global.ITEM_NUMBER+"=?";

	private static final String CLEAN_LOCKER=

		"UPDATE "+Global.ITEM_TABLE+" "+
		"SET "+Global.ITEM_LOCKER+"=?"+" "+
		"WHERE "+Global.PROJECT_NAME+"=?"+" "+
		"AND "+Global.ITEM_LOCKER+"=?";

	private static final String CLEAN_LOCKER_ALL_PROJECTS=

		"UPDATE "+Global.ITEM_TABLE+" "+
		"SET "+Global.ITEM_LOCKER+"=?"+" "+
		"WHERE "+Global.ITEM_LOCKER+"=?";

	private static final String GET_TECH_DATA =
		"SELECT "+ Global.TECH_ELEMENT+","+ Global.TECH_LABEL + "," +
		Global.ITEM_TECH_DATA + " " +
		"FROM "+Global.ITEMS_TECH_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?"+" "+
		"ORDER BY "+Global.TECH_ELEMENT+","+Global.TECH_LABEL;	

	private static final String GET_ADMIN_DESC_DATA =
		"SELECT "+ Global.ELEMENT+","+ Global.ADMIN_DESC_LABEL + "," +
		Global.ITEM_ADMIN_DESC_DATA+ " " +
		"FROM "+Global.ITEMS_ADMIN_DESC_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.ITEM_NUMBER+"=?"+" "+
		"AND "+Global.MD_TYPE+"=?";

	public ItemsDAO(String projectName, int[] itemNumbers, boolean newOnly)
	{
		this.projectName=projectName;	
		this.itemNumbers=itemNumbers;
		this.newOnly=newOnly;
	}

	/**
	 * Main running method of the thread. 
	 * 
	 * Will execute different update processes based on the constructor values. 
	 * 
	 * If the project name is null and item numbers are null, it is assumed that
	 * the user wants all of the projects processed in full. 
	 * 
	 * If the project name is not null and an item number list is null, 
	 * it will check whether the user wants all of the master files processed or 
	 * only the new ones and process appropriately.
	 * 
	 * If the project has been specified and the item number list has been supplied, 
	 * then it is assumed that the user wants only the item numbers supplied in the
	 * project supplied to be updated.
	 */
	public void run()
	{
		//Attempt to create derivative path if not existing already
		String accessDirPath=Global.DERIV_DIRECTORY+"/"+projectName;
		File accessDir=new File(accessDirPath);
		if (!(accessDir.exists()&&accessDir.isDirectory()))
			accessDir.mkdir();

		//Branch off actions depending on settings.		
		//Project specified, no item numbers specified. 
		if(projectName!=null&&itemNumbers==null)
		{
			long currentTime=System.currentTimeMillis();
			String stString=(newOnly==true ? "NewOnly": "All");
			if(newOnly)
			{
				MetaDbHelper.parserOn(projectName, stString);
				processProjectNew(projectName);
			}
			else
			{
				MetaDbHelper.parserOn(projectName, stString);
				processProject(projectName);
			}
			long elapsedTime=System.currentTimeMillis()-currentTime;
			double secondsTaken=(double)elapsedTime/(double)1000;

			String timeString=toHMS((int)secondsTaken);

			MetaDbHelper.parserOff(projectName, stString, timeString);

		}
		//Both project and item numbers specified.
		else if(projectName!=null&&itemNumbers!=null)
		{

			MetaDbHelper.parserOn(projectName, itemNumbers[0]+" to "+itemNumbers[itemNumbers.length-1]);
			long currentTime=System.currentTimeMillis();

			ArrayList<File> toProcess = new ArrayList<File>(itemNumbers.length);
			for(int i=0; i<itemNumbers.length; i++)
			{
				try
				{
				toProcess.add(new File(getMasterPath(projectName, itemNumbers[i])));
				}
				catch(Exception e)
				{
					String errorText = "Failed to process "+projectName+", item number "+itemNumbers[i]+". " +
					"Check that the project has been processed in full.";
					MetaDbHelper.note(errorText);
					SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PARSER, errorText);
					break;
				}
			}
			processItems(projectName, itemNumbers, toProcess);

			long elapsedTime=System.currentTimeMillis()-currentTime;
			double secondsTaken=(double)elapsedTime/(double)1000;

			String timeString=toHMS((int)secondsTaken);

			if(MetaDbHelper.getParserStatus())
				MetaDbHelper.parserOff(projectName, "Range Specified", timeString);
			else
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error: Parser interrupted before finish. " +
				"Check the log for details.");
		}
	}




	/**
	 * Retrieve an item with all of its attributes and data
	 * @param projectName the name of the project to get the items from
	 * @param index the item number
	 * @return an Item object containing the item information.
	 */
	public static Item getItem(String projectName, int index) {
		try {
			int itemNumber = index;
			if (index < 1)
				itemNumber = 1;
			else if (index >= nextItemNumber(projectName))
				itemNumber = nextItemNumber(projectName)-1;
			String filePath = getMasterPath(projectName, itemNumber);
			String checksum = getChecksum(projectName, itemNumber);
			String locker = getLocker(projectName, itemNumber);
			String date = getLastModified(projectName, itemNumber);
			Item item = new Item(projectName, itemNumber, 
					filePath, 
					checksum, 
					locker, 
					date );
			item.setThumbFilePath(getThumbFilePath(projectName, itemNumber));
			item.setData(AdminDescItemsDAO.getItems(projectName, Global.MD_TYPE_ADMIN, itemNumber), Global.MD_TYPE_ADMIN);
			item.setData(AdminDescItemsDAO.getItems(projectName, Global.MD_TYPE_DESC, itemNumber), Global.MD_TYPE_DESC);
			MetaDbHelper.note("Get item: Project "+projectName+": Item "+itemNumber +" path "+filePath);
			return item;
		}
		catch (Exception e) {
			MetaDbHelper.note("Cannot get item: Project "+projectName+": Item "+index);
			MetaDbHelper.logEvent(e);
			return null;
		}
	}

	/**
	 * Clear all locks a user is holding.
	 * @param locker The user name to clear all locks for.
	 * @return true if the clearing was completed, false otherwise.
	 */
	public static boolean cleanLockAllProjects(String locker) {
		boolean success = false;
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement cleanLocker=conn.prepareStatement(CLEAN_LOCKER_ALL_PROJECTS);
				cleanLocker.setString(1, "");
				cleanLocker.setString(2, locker);

				cleanLocker.executeUpdate();
				cleanLocker.close();

				conn.close();
				success = true;
			} catch (Exception e) {
				MetaDbHelper.logEvent(e);
			}
		}
		return success;
	}

	/**
	 * Clear all item locks held by a user for one project.
	 * @param projname The project the locked item is in.
	 * @param locker The user name of the user holding the lock.
	 * @return true if the lock is released successfully, false otherwise.
	 */
	public static boolean cleanLock(String projname, String locker) {
		boolean success = false;
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement cleanLocker=conn.prepareStatement(CLEAN_LOCKER);
				cleanLocker.setString(1, "");
				cleanLocker.setString(2, projname);
				cleanLocker.setString(3, locker);


				cleanLocker.executeUpdate();
				cleanLocker.close();

				conn.close();
				success = true;
			} catch (Exception e) {
				MetaDbHelper.logEvent(e);
			}
		}
		return success;
	}

	/**
	 * Lock/unlock an item for editing. 
	 * @param projectName The project the item is in.
	 * @param itemNumber The item number of the item to lock. 
	 * @return true if successfully locked, false otherwise. 
	 */
	public static synchronized boolean changeLock(String projectName, int itemNumber, String locker)
	{
		boolean success = false;
		String oldLocker = getLocker(projectName, itemNumber);

		if(oldLocker.equals(locker)) //Allow multiple sessions of editing by the same user.
			return true;

		if (!(cleanLock(projectName, locker))) {
			MetaDbHelper.note("Failed to clean old locks in the project "+projectName+" for "+locker);
			return false;
		}
		if(!(oldLocker.trim().equals("")) && !(oldLocker.equals(locker))) { //another user trying to edit-false
			MetaDbHelper.note("Old locker is different from new. Disabling edit");
			return false;
		}

		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				if (oldLocker != null && oldLocker.equals("")) {
					PreparedStatement updateLocker = conn.prepareStatement(SET_LOCKER);
					updateLocker.setString(1, locker);
					updateLocker.setString(2, projectName);
					updateLocker.setInt(3, itemNumber);

					updateLocker.executeUpdate();
					updateLocker.close();

					success = true;
				}	
				conn.close();
			}			
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return success;

	}

	/**
	 * Retrieves master file path of an item in a project.
	 * @param projectName The project name for which to retrieve the file name.
	 * @param itemNumber The item number for the desired filename.
	 * @return The filename of the item in the project with the given item number.
	 */
	public static String getMasterPath(String projectName, int itemNumber)
	{
		String fileName=null;
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getFileName=conn.prepareStatement(GET_FILE_NAME);
				getFileName.setString(1, projectName);
				getFileName.setInt(2, itemNumber);	

				ResultSet res=getFileName.executeQuery();
				if(res.next())
					fileName = res.getString(Global.ITEM_FILE_NAME);
				//else 
				//	MetaDbHelper.logEvent("ItemsDAO", "getFileName", "File name entry not found for item "+itemNumber+" in project "+projectName);

				res.close();
				getFileName.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getFileName", "Database error");

		return Global.MASTERS_DIRECTORY+"/"+projectName+"/"+fileName;
	}

	/**
	 * Retrieves master file name of an item in a project.
	 * @param projectName The project name for which to retrieve the file name.
	 * @param itemNumber The item number for the desired filename.
	 * @return The filename of the item in the project with the given item number.
	 */
	public static String getMasterFileName(String projectName, int itemNumber)
	{
		String fileName=null;
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getFileName=conn.prepareStatement(GET_FILE_NAME);
				getFileName.setString(1, projectName);
				getFileName.setInt(2, itemNumber);	

				ResultSet res=getFileName.executeQuery();
				if(res.next())
					fileName = res.getString(Global.ITEM_FILE_NAME);
				//else 
				MetaDbHelper.logEvent("ItemsDAO", "getFileName", "File name entry not found for item "+itemNumber+" in project "+projectName);

				res.close();
				getFileName.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getFileName", "Database error");

		return fileName;
	}
	/**
	 * Retrieves thumbnail filepath of an item in a project.
	 * @param projectName The project name for which to retrieve the thumbnail path.
	 * @param itemNumber The item number for the desired thumbnail filepath.
	 * @return The thumbnail filepath of the item in the project with the given item number.
	 */
	public static String getThumbFilePath(String projectName, int itemNumber)
	{
		String fileName="null";
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getFileName=conn.prepareStatement(GET_THUMB_FILE_NAME);
				getFileName.setString(1, projectName);
				getFileName.setInt(2, itemNumber);	

				ResultSet res=getFileName.executeQuery();
				if(res.next())
					fileName=res.getString(Global.ITEM_THUMB_FILE_NAME);
				//else
				MetaDbHelper.logEvent("ItemsDAO", "getThumbFileName", "File name entry not found for item "+itemNumber+" in project "+projectName);

				res.close();
				getFileName.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getThumbFileName", "Database error");
		return fileName;
	}

	/**
	 * Retrieves the custom derivative filepath of an item in a project. 
	 * @param projectName The project name for which to retrieve the derivative path.
	 * @param itemNumber The item number for the desired derivative filepath.
	 * @return The derivative filepath of the item in the project with the given item number.
	 */
	public static String getCustomDerivPath(String projectName, int itemNumber)
	{
		MetaDbHelper.note("Trying to get custom deriv path for "+projectName+", item "+itemNumber);
		String fileName="null";
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getFileName=conn.prepareStatement(GET_CUSTOM_FILE_NAME);
				getFileName.setString(1, projectName);
				getFileName.setInt(2, itemNumber);	

				ResultSet res=getFileName.executeQuery();
				if(res.next())
				{
					fileName=res.getString(Global.ITEM_CUSTOM_FILE_NAME);
					MetaDbHelper.note("Custom file name retrieved: "+fileName);
				}		

				//else 
				//	MetaDbHelper.logEvent("ItemsDAO", "getCustomDerivPath", "File name entry not found for item "+itemNumber+" in project "+projectName);

				res.close();
				getFileName.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getCustomDerivPath", "Database error");

		MetaDbHelper.note("Custom deriv path: "+fileName);
		return fileName;
	}

	/**
	 * Retrieves the zoom derivative filepath of an item in a project. 
	 * @param projectName The project name for which to retrieve the derivative path.
	 * @param itemNumber The item number for the desired derivative filepath.
	 * @return The derivative filepath of the item in the project with the given item number.
	 */
	public static String getZoomDerivPath(String projectName, int itemNumber)
	{
		String fileName="null";
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getFileName=conn.prepareStatement(GET_ZOOM_FILE_NAME);
				getFileName.setString(1, projectName);
				getFileName.setInt(2, itemNumber);	

				ResultSet res=getFileName.executeQuery();
				if(res.next())
					fileName=res.getString(Global.ITEM_ZOOM_FILE_NAME);

				//else 
				//	MetaDbHelper.logEvent("ItemsDAO", "gerZoomDerivPath", "File name entry not found for item "+itemNumber+" in project "+projectName);

				res.close();
				getFileName.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getZoomDerivPath", "Database error");
		return fileName;
	}

	/**
	 * Retrieves the fullsize derivative filepath of an item in a project. 
	 * @param projectName The project name for which to retrieve the derivative path.
	 * @param itemNumber The item number for the desired derivative filepath.
	 * @return The derivative filepath of the item in the project with the given item number.
	 */
	public static String getFullDerivFilePath(String projectName, int itemNumber)
	{
		String fileName="null";
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getFileName=conn.prepareStatement(GET_FULL_FILE_NAME);
				getFileName.setString(1, projectName);
				getFileName.setInt(2, itemNumber);	

				ResultSet res=getFileName.executeQuery();
				if(res.next())
					fileName=res.getString(Global.ITEM_FULLSIZE_FILE_NAME);
				//else 
				//	MetaDbHelper.logEvent("ItemsDAO", "getFullDerivPath", "File name entry not found for item "+itemNumber+" in project "+projectName);

				res.close();
				getFileName.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getFullDerivPath", "Database error");
		return fileName;
	}

	/**
	 * Get all the tech data for one item in a project.
	 * @param projectName the name of the project
	 * @param itemNumber the index of the item
	 * @return a map of tech element.label and its data
	 */
	public static ArrayList<Metadata> getTechData(String projectName, int itemNumber) {
		ArrayList<Metadata> techList = new ArrayList<Metadata>();

		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{

				PreparedStatement getTechData=conn.prepareStatement(GET_TECH_DATA);
				getTechData.setString(1, projectName);
				getTechData.setInt(2, itemNumber);

				ResultSet res=getTechData.executeQuery();
				while(res.next())
				{
					techList.add(new Metadata(projectName, itemNumber, res.getString(Global.TECH_ELEMENT), res.getString(Global.TECH_LABEL), res.getString(Global.ITEM_TECH_DATA)));
				}

				res.close();
				getTechData.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return techList;
	}

	public static ArrayList<Metadata> getMetadata(String projectName, int itemNumber, String metadataType) {
		ArrayList<Metadata> dataList = new ArrayList<Metadata>();

		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{

				PreparedStatement getAdminDescData=conn.prepareStatement(GET_ADMIN_DESC_DATA);
				getAdminDescData.setString(1, projectName);
				getAdminDescData.setInt(2, itemNumber);
				getAdminDescData.setString(3, metadataType);
				ResultSet res=getAdminDescData.executeQuery();
				while(res.next())
				{
					dataList.add(new Metadata(projectName, itemNumber, res.getString(Global.ELEMENT), res.getString(Global.ADMIN_DESC_LABEL), res.getString(Global.ITEM_ADMIN_DESC_DATA)));
				}

				res.close();
				getAdminDescData.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return dataList;
	}

	/**
	 * Return the next available item number for a project.
	 * @param projectName The project to retrieve the next item number in sequence.
	 * @return an int representing the next item number available in the project.
	 */

	public static int nextItemNumber(String projectName) {
		MetaDbHelper.note("Running nextItemNumber");
		int nextNo = -1;
		Connection conn = Conn.initialize();


		if(conn!=null)
		{
			try
			{
				PreparedStatement getItemNumber=conn.prepareStatement(GET_NEXT_ITEM_NUMBER);
				getItemNumber.setString(1, projectName);

				ResultSet res=getItemNumber.executeQuery();
				if(res.next())
					nextNo=res.getInt("max")+1;
				MetaDbHelper.note("Project "+projectName+": next item number="+nextNo);
				//else 
				//	MetaDbHelper.logEvent("ItemsDAO", "nextItemNumber", "No values for item entries found in project "+Global.PROJECT_NAME);

				res.close();
				getItemNumber.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				nextNo = -1;
			}
		}
		return nextNo;
	}

	/**
	 * Returns the checksum of an item. 
	 * @param projectName The project in which the item is located
	 * @param itemNumber The item number of the item to be retrieved
	 * @return The checksum, as a hex string, of the item.
	 */
	public static String getChecksum(String projectName, int itemNumber)
	{
		String Checksum=null;
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getChecksum=conn.prepareStatement(GET_CHECKSUM);
				getChecksum.setString(1, projectName);
				getChecksum.setInt(2, itemNumber);

				ResultSet res=getChecksum.executeQuery();
				if(res.next())
				{
					Checksum=res.getString("Checksum");
					MetaDbHelper.note("Project "+projectName+": Item "+itemNumber+" Checksum "+Checksum);
				}
				//else
				//	MetaDbHelper.logEvent("ItemsDAO", "getChecksum", "Could not retrieve Checksum!");

				res.close();
				getChecksum.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}

		return Checksum;
	}

	/**
	 * Returns the last modified date of an item. 
	 * @param projectName The project in which the item is located
	 * @param itemNumber The item number of the item to be retrieved
	 * @return The last modified date of the item, as a String.
	 */
	public static String getLastModified(String projectName, int itemNumber)
	{
		String date=null;
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getLastModified=conn.prepareStatement(GET_DATE);
				getLastModified.setString(1, projectName);
				getLastModified.setInt(2, itemNumber);

				ResultSet res=getLastModified.executeQuery();
				if(res.next())
				{
					date=res.getString("date");
					MetaDbHelper.note("Project "+projectName+": Item "+itemNumber+" last modified: "+date);
				}
				//else
				MetaDbHelper.logEvent("ItemsDAO", "getLastModified", "Could not retrieve last modified date!");

				res.close();
				getLastModified.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}

		return date;
	}

	/**
	 * Process only the newly added images in a single project.
	 * @param projectName
	 * @return true if processed successfully, false otherwise.
	 */
	private static boolean processProjectNew(String projectName)
	{
		String masterPath=Global.MASTERS_DIRECTORY+"/"+projectName;
		MetaDbHelper.logEvent("ItemsDAO", "processProjectNew", "Processing only the new images in project directory: "+masterPath);

		File projectDir=new File(masterPath);
		if (!projectDir.exists())
		{			
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Cannot find the path "+masterPath);
			MetaDbHelper.parserOff(projectName, "All", "0");
			return false;
		}

		if(MetaDbHelper.projectExists(projectName)) //Check for existence of project in database.
		{
			MetaDbHelper.logEvent("ItemsDAO", "processProjectNew", "Project "+projectName+" found. Processing masterimage-directory.");
			MetaDbHelper.note("Project dir: "+masterPath);

			//Get a list of all the newly added items (defined as being in filesystem but not in DB)
			ArrayList<File> newItems=getNewImages(projectName, masterPath);
			MetaDbHelper.note("processProjectNew: Got new items list...");
			MetaDbHelper.note("processProjectNew: "+newItems.size()+" new items...");
			//Process the files. 
			//NOTE: If the derivative settings have changed, there may be inconsistency in the filenames/sizes, 
			//including those on the item metadata page.
			if(updateProjectNew(masterPath, projectName, newItems))
				return DerivativeDAO.updateDerivatives(projectName, newItems);
			//Failed to update full
			else
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Unable to update project "+projectName);

		}
		return false;			
	}

	/**
	 * Process a single project in full.
	 * @param projectName
	 * @return true if processed successfully, false otherwise.
	 */
	private static boolean processProject(String projectName)
	{
		checkRequiredAttributes(projectName);
		String masterPath=Global.MASTERS_DIRECTORY+"/"+projectName;
		File projectDir=new File(masterPath);
		if (!projectDir.exists())
		{			
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Cannot find the path "+masterPath);
			MetaDbHelper.parserOff(projectName, "All", "0");
			return false;
		}

		MetaDbHelper.logEvent("ItemsDAO", "processProject", "Processing project directory: "+projectDir.getAbsolutePath());

		if(MetaDbHelper.projectExists(projectName)) //Check for existence of project in database.
		{
			MetaDbHelper.logEvent("ItemsDAO", "processProjects", "Project "+projectName+" found. Processing masterimage-directory.");
			MetaDbHelper.note("Project dir: "+projectDir.getAbsolutePath());

			//Process (full process) the master directory.
			if(updateProjectFull(projectDir, projectName))
			{	
				//Clear all existing derivatives.
				FileManager.deleteDerivatives(projectName);
				//Generate derivatives.
				return DerivativeDAO.updateDerivatives(projectName, FileManager.getProjectFiles(new File(masterPath)));
			}
			else
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Unable to update project "+projectName);

		}
		return false;

	}

	/**
	 * Updates one item in a project provided that it exists
	 * @param projectName the project to update
	 * @param itemNumber the item number to update
	 * @return true if the item was successfully updated, false otherwise.
	 */
	private static boolean processItems(String projectName, int[] itemNumbers, ArrayList<File> masterImages)
	{
		boolean success = false;
		for(int itemNumber=1; itemNumber<=itemNumbers.length; itemNumber++)
		{
			boolean itemExists = getItem(projectName, itemNumber)!=null;
			//Item doesn't exist; add as a new one.
			if(!itemExists)
			{
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error: Tried processing item "+itemNumber+
						" in project "+projectName+" but the item doesn't exist in MetaDB.");
				success = false;
			}
			//Item exists; update it if necessary.
			else
			{
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PARSER, "Updating item "+itemNumber+" in "+projectName);
				try
				{
					//Re-populate auto technical data and thumb.
					updateItem(projectName, itemNumber);

					//Get old paths
					File[] oldPaths=new File[2];
					oldPaths[0]=new File(getCustomDerivPath(projectName, itemNumber));
					oldPaths[1]=new File(getZoomDerivPath(projectName, itemNumber));
					//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "# of OLD FILES: " +oldPaths.length);

					//Delete the old dynamic derivatives.
					FileManager.deleteFiles(oldPaths);

					//Get master file name
					String masterFileName=getMasterFileName(projectName, itemNumber);
					//String newThumbPath=getThumbFilePath(projectName, itemNumber);
					//String fullSizePath=getFullDerivFilePath(projectName, itemNumber);
					//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "Current master filename- "+projectName+"-"+itemNumber+": "+masterFileName);
					//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "New thumb path for "+projectName+"-"+itemNumber+": "+newThumbPath);
					//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "Fullsize path for "+projectName+"-"+itemNumber+": "+fullSizePath);

					//Get new paths
					//NOTE: If the derivative settings were updated, then there may be incosistency in the item metadata view
					//(the most recent settings will be displayed, but the links may be of the old settings)
					DerivativeSetting customSetting = DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING);
					boolean generateCustom = (customSetting.isEnabled()? true : false);
					String newCustomPath=FileManager.getCustomDerivPath(projectName, masterFileName);
					String newZoomPath=FileManager.getZoomDerivPath(projectName, masterFileName);

					//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "New custom path for "+projectName+"-"+itemNumber+": "+newCustomPath);
					//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "New zoom path for "+projectName+"-"+itemNumber+": "+newZoomPath);

					//Update DB information with new paths
					updateDerivPaths(projectName, itemNumber, (generateCustom==true? newCustomPath : ""), newZoomPath);
				}
				catch(Exception e)
				{
					MetaDbHelper.parserOff(projectName, "Range Specified", "EXCEPTION: Error updating item "+itemNumber+" in project "+projectName);
					success = false;
				}
			}
		}
		success = DerivativeDAO.updateDerivatives(projectName, masterImages);
		return success;
		//Item exists already
	}

	/**
	 * Fully updates the items and technical metadata for a project. 
	 * If, while scanning the files in the project folder, 
	 * it is detected that the image is new, then add that image.
	 * 
	 * If an image has has been changed, 
	 * update the image entry. 
	 * 
	 * @param masterPath the filesystem path for the project (ex. /projects/testProj )
	 * @param projectName the name of the project.
	 */
	private static boolean updateProjectFull(File masterPath, String projectName)
	{
		boolean updateSuccess=false;
		MetaDbHelper.note("Updating project "+projectName);
		MetaDbHelper.note("Master path: "+masterPath.getAbsolutePath());
		//Get a lexically sorted list of all of the master images 
		ArrayList<File> projectItems=FileManager.getProjectFiles(masterPath);
		MetaDbHelper.note("The project "+projectName+" has "+projectItems.size()+" items");

		if(projectItems==null || projectItems.size()==0)
		{
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_NOTIFICATION, "No images found in "+projectName);
			return false;
		}
		//Traverse items while detecting sequencing errors
		try
		{
			int itemCount=1; //filenames start with 0001 ; start this at 1 as well
			for(File image: projectItems)	
			{
				String fileName=image.getName();
				String filePath = image.getAbsolutePath();
				MetaDbHelper.note("Processing "+filePath);
				String number=getNumSubString(fileName); //get the #### section of the file path

				MetaDbHelper.note("Image Number: "+number);
				if(number.equals("")||number==null) //formatting error
				{
					SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_FATAL_ERROR, "Fatal error: Improperly formatted master-image: "+fileName);
					return false;
				}
				else
				{
					//Remove leading zeros from the #### substring
					int itemIndex=Integer.parseInt(removeZeroes(number));
					//Make sure index matches. If not, terminate.
					if(itemIndex!=itemCount)
					{
						SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_FATAL_ERROR, "Fatal error: There is an inconsistency in " +
								"item numbering. Check near the file "+fileName+" in project "+projectName+". Terminating update sequence...");
						return false;
					}

					//No errors-increment the item count.
					itemCount++;

					boolean itemExists=itemExists(projectName, fileName);
					//Item doesn't exist; add as a new one.
					if(!itemExists)
					{
						int nextItemNumber=nextItemNumber(projectName);
						addItem(projectName, image, nextItemNumber);
					}

					//Item exists; update it.
					else if(itemExists==true)
					{
						int itemNumber=getItemNumber(projectName, fileName);
						//The checksum or date changed; update
						if(needsUpdate(projectName, itemNumber))
						{
							SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_NOTIFICATION, "Notice: Checksum or modification date for the file "+fileName+" " +
									"in project "+projectName+ " has changed");
						}
						if (!updateItem(projectName, itemNumber))
						{
							SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error at item "+itemNumber+" in "+projectName);
							MetaDbHelper.parserOff(projectName, "All", "Error");
							MetaDbHelper.note("Error updating project!");
						}
						String newCustomPath=FileManager.getCustomDerivPath(projectName, fileName);
						String newZoomPath=FileManager.getZoomDerivPath(projectName, fileName);
						DerivativeSetting customSetting = DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING);
						boolean generateCustom = (customSetting.isEnabled()? true : false);
						//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "New custom path for "+projectName+"-"+itemNumber+": "+newCustomPath);
						//SysLogDAO.log("MetaDB", Global.SYSLOG_DEBUG, "New zoom path for "+projectName+"-"+itemNumber+": "+newZoomPath);

						//Update DB information with new paths
						updateDerivPaths(projectName, itemNumber, (generateCustom == true? newCustomPath : ""), newZoomPath);
						//Checksum hasn't changed
					} //Item exists already
				}//Generic path error

			} //Main loop.

			//Logging
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_NOTIFICATION, "Finished updating project "+projectName);
			MetaDbHelper.note("Finished parsing "+projectName);
			updateSuccess=true;
		}//try-block
		catch(Exception e)
		{
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error updating project "+projectName);
			MetaDbHelper.parserOff(projectName, "All", "Error");
			MetaDbHelper.logEvent(e);
			MetaDbHelper.note("Error updating project!");
			updateSuccess = false;
		}
		return updateSuccess;

	}
	/**
	 * Fully updates the items and technical metadata for a project. 
	 * If, while scanning the files in the project folder, 
	 * it is detected that the image is new, then add that image.
	 * 
	 * If an image has has been changed, 
	 * update the image entry. 
	 * 
	 * @param masterPath the filesystem path for the project (ex. /projects/testProj )
	 * @param projectName the name of the project.
	 * @param newImages a lexically sorted ArrayList of File objects pointing to the new item files.
	 */
	private static boolean updateProjectNew(String masterPath, String projectName, ArrayList<File> newImages)
	{
		boolean updateSuccess=false;
		MetaDbHelper.note("Updating new items in project "+projectName);

		MetaDbHelper.note("The project "+projectName+" has "+newImages.size()+" new items");

		//If no new images, terminate.
		if(newImages==null||newImages.size()==0)
		{
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_NOTIFICATION, "No new images were found for "+projectName);
			return true;
		}
		//Go through the items.
		try
		{
			//Get the item sequence to start at.
			int itemCount=Integer.parseInt(removeZeroes(getNumSubString(newImages.get(0).getCanonicalPath())));

			for(File image: newImages)	
			{
				//File image=new File(imagePath);
				String filePath=image.getName();
				String number=getNumSubString(filePath); //extract ##### from file path

				//Improper format of master image path
				if(number.equals("")||number==null)
				{
					SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_FATAL_ERROR, "Fatal error: Improperly formatted master-image: "+filePath);
					return false;
				}
				else
				{
					//Remove leading zeros from the ##### string
					int itemIndex=Integer.parseInt(removeZeroes(number));
					//Make sure index matches. If not, terminate.
					if(itemIndex!=itemCount)
					{
						SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_FATAL_ERROR, "Fatal error: There is an inconsistency in " +
								"item numbering. Check near the file "+filePath+" in project "+projectName+". Terminating update sequence...");
						return false;
					}

					//No errors-increment the item count.
					itemCount++;

					//Check for the item's status (exists or not)
					boolean itemExists=itemExists(projectName, filePath);
					//Item doesn't exist; add as a new one.
					if(itemExists==false)
					{
						int nextItemNumber=nextItemNumber(projectName);
						if(!addItem(projectName, image, nextItemNumber) )
						{
							SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error at item "+nextItemNumber+" in "+projectName);
							MetaDbHelper.parserOff(projectName, "New", "Error");
							MetaDbHelper.note("Error updating the project");
						}
					}

					//Item exists already; do not update
					else
					{
						SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Inconsistency found in project "+projectName+
								". The file "+filePath+" was detected as new by the parser but is already existent in MetaDB.");
					}
				}//Generic path error

			} //Main loop.

			//Logging
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_NOTIFICATION, "Finished adding new images to project "+projectName);

			MetaDbHelper.note("Finished updating project "+projectName);
			updateSuccess=true;
		}//try-block
		catch(Exception e)
		{
			SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error adding new images to project "+projectName);
			MetaDbHelper.parserOff(projectName, "New", "Error");
			MetaDbHelper.logEvent(e);
			MetaDbHelper.note("Error updating project!");
			updateSuccess = false;
		}
		return updateSuccess;

	}

	/**
	 * Check if an item with the given filename exists in a project. 
	 * @param projectName The project in which to search for the file name. 
	 * @param fileName The filename of the file to search for. 
	 * @return true if an item with the filename was found in the project, 
	 * false otherwise.
	 */
	private static boolean itemExists(String projectName, String fileName)
	{
		boolean exists=false;
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement itemExists=conn.prepareStatement(ITEM_EXISTS);
				itemExists.setString(1, projectName);
				itemExists.setString(2, fileName);

				ResultSet res=itemExists.executeQuery();
				if(res.next())
					exists = true;
				//else 
				//	MetaDbHelper.logEvent("ItemsDAO", "itemExists", "Item number not found for file "+fileName+" in project "+projectName);
				res.close();
				itemExists.close();

				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				exists = false;
			}
		}
		return exists;

	}

	/**
	 * This method adds a blank item and creates appropriate blank admin/desc/technical fields according to 
	 * the project. 
	 * 
	 * @param projectName Project to insert blank item to. 
	 * @param image the file on the filesystem to process.
	 * @param itemNumber the item number of the file to process.
	 * @return true if successfully added blank item to project, false otherwise.
	 */
	private static boolean addItem(String projectName, File image, int itemNumber)
	{ 
		boolean added=false;
		try
		{
			//Do the auto tech


			// Add the project-filename entry to item table
			if(addItemEntry(projectName, itemNumber, image))
			{
				String fileName=image.getName();
				String systemPath=Global.MASTERS_DIRECTORY+"/"+projectName+"/"+fileName;
				MetaDbHelper.note("Getting the file path and parsing "+fileName);

				//Parse the image and store the tech data into an AutoTechData object.
				AutoTechData parsedData=ImageParser.parseImage(projectName, systemPath , itemNumber);

				MetaDbHelper.note("Next item number for project "+projectName+" is "+itemNumber);

				if( 	//Blank data successfully populated.

						//Add blank-value fields to custom and admin/desc data tables. 
						addCustomTechData(projectName, itemNumber)&&
						addAdminData(projectName, itemNumber)&&
						addDescriptiveData(projectName, itemNumber)
				)
				{
					TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_AUTO_ELEMENT, Global.TECH_CHECKSUM, getChecksum(projectName, itemNumber));
					TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_AUTO_ELEMENT, Global.TECH_FILENAME, fileName);
					TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_AUTO_ELEMENT, Global.TECH_DATE_MODIFIED, getLastModified(projectName, itemNumber));
					TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_ZOOM_ELEMENT, Global.TECH_ZOOM_LABEL, buildZoomUrl(projectName, itemNumber));
					TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_DOWNLOAD_ELEMENT, Global.TECH_DOWNLOAD_LABEL, buildDownloadUrl(projectName, itemNumber));
					MetaDbHelper.note("Critical-tech fields added.");
				}

				else //Blank data creation failure.
				{
					MetaDbHelper.note("Error! Blank fields could not be added.");
				}

				//Valid parsed data.
				if(parsedData!=null) //Add the standard generated technical metadata to the project tech data table, and generate the thumbnail-file.
					addTechnicalData(projectName,itemNumber, parsedData);
			}
			else //Item entry not added.
			{
				MetaDbHelper.note("Could not add the item entry for "+image.getName()+" in project "+projectName);
				added = false;
			}
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return added;
	}


	/**
	 * Adds a blank row to items table. 
	 * @param projectName The project to add a blank row to in items. 
	 * @param image The file for this item. 
	 * @return true if successfully added, false if otherwise.
	 */
	private static boolean addItemEntry(String projectName, int itemNumber, File image)
	{
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				DerivativeSetting customSetting = DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING);
				boolean generateCustom = (customSetting.isEnabled()? true : false);
				//Extract filepaths and checksum.
				String fileName=image.getName();
				String filePath=image.getAbsolutePath();

				String thumbFileName=FileManager.getThumbFileName(projectName, fileName);
				String customFileName=FileManager.getCustomDerivFileName(projectName, (generateCustom==true? fileName : ""));
				String zoomFileName=FileManager.getZoomDerivFileName(projectName, fileName);
				String fullFileName=FileManager.getFullSizeDerivFileName(projectName, fileName);
				String checksum=FileManager.computeChecksum(filePath);
				String date=FileManager.computeLastModified(filePath);

				MetaDbHelper.note("Adding new item entry...."+fileName);
				PreparedStatement addItem=conn.prepareStatement(ADD_ITEM);
				addItem.setString(1, projectName);
				addItem.setInt(2, itemNumber);
				addItem.setString(3, fileName);
				addItem.setString(4, thumbFileName);
				addItem.setString(5, customFileName);
				addItem.setString(6, zoomFileName);
				addItem.setString(7, fullFileName);
				addItem.setString(8, "");
				addItem.setString(9, checksum);
				addItem.setString(10, date);

				addItem.executeUpdate();
				MetaDbHelper.note("Item entry for "+fileName+" added.");
				addItem.close();
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
	 * Adds blank administrative fields to the project to accommodate one new item.
	 * @param projectName The project for which to add blank administrative fields for a new item. 
	 * @param itemNumber The number of the new item that is to be added. 
	 * @return true if successfully added, false otherwise.
	 */
	private static boolean addAdminData(String projectName, int itemNumber)
	{
		try{
			ArrayList<AdminDescAttribute> adminDescAttributes=AdminDescAttributesDAO.getAdminDescAttributes(projectName, "administrative");

			if(!adminDescAttributes.isEmpty())
			{
				for(AdminDescAttribute ada: adminDescAttributes)
				{
					MetaDbHelper.note("Adding data for "+projectName+", item "+itemNumber+", "+ada.getElement()+"."+ada.getLabel()+"; type="+ada.getMdType());
					if(ada.getMdType().equals(Global.MD_TYPE_ADMIN))
						addBlankAdminData(projectName, itemNumber, ada.getElement(), ada.getLabel());
				}
				MetaDbHelper.note(adminDescAttributes.size()+" blank admin fields for project "+projectName+" and item no. "+itemNumber+" added.");
			}
			MetaDbHelper.note("No administrative fields to populate with blank data!");
			return true;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}	
	/**
	 * Adds blank descriptive fields to the project to accommodate one new item.
	 * @param projectName The project for which to add blank descriptive fields for a new item. 
	 * @param itemNumber The number of the new item that is to be added. 
	 * @return true if successfully added, false otherwise.
	 */
	private static boolean addDescriptiveData(String projectName, int itemNumber)
	{
		try
		{
			ArrayList<AdminDescAttribute> adminDescAttributes=AdminDescAttributesDAO.getAdminDescAttributes(projectName, "descriptive");

			if(!adminDescAttributes.isEmpty())
			{
				for(AdminDescAttribute ada: adminDescAttributes)
				{					
					MetaDbHelper.note("Adding data for "+projectName+", item "+itemNumber+", "+ada.getElement()+"."+ada.getLabel()+"; type="+ada.getMdType());
					if(ada.getMdType().equals("descriptive"))
						addBlankDescData(projectName, itemNumber, ada.getElement(), ada.getLabel());
				}
				MetaDbHelper.note(adminDescAttributes.size()+" blank descriptive fields added for project "+projectName+ " and item number "+itemNumber);
			}
			MetaDbHelper.note("No descriptive fields to populate with blank data!");
			return true;
		}	
		catch(Exception e)
		{	
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Adds blank custom technical fields to the project to accommodate one item. 
	 * @param projectName The project for which to add blank custom technical fields for a new item. 
	 * @param itemNumber The number of the new item is to be added. 
	 * @return true if successfully added, false otherwise.
	 */
	private static boolean addCustomTechData(String projectName, int itemNumber)
	{ 

		try
		{
			ArrayList<TechAttribute> techAttributes=TechAttributesDAO.getTechAttributes(projectName);
			if(!techAttributes.isEmpty())
			{
				for(TechAttribute ta: techAttributes)
					addBlankCustomTechData(projectName, itemNumber, ta.getElement(), ta.getLabel());
				MetaDbHelper.note(techAttributes.size()+" blank custom tech fields added for project "+projectName+ " and item number "+itemNumber);
			}
			//else
			MetaDbHelper.note("No custom tech fields to be populated with blank data!");
			return true;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Populates one item's auto technical fields. 
	 * @param projectName the project name in which the item to be updated is contained
	 * @param itemNumber The item number to be populated
	 * @param techData The (pre-parsed) technical metadata for the item.
	 * @return true if the technical metadata was correctly added for the item, false otherwise
	 */
	private static boolean addTechnicalData(String projectName, int itemNumber, AutoTechData techData)
	{
		MetaDbHelper.note("addTechnicalData: Attempting to populate auto technical data for item "+itemNumber+" in project "+projectName);
		try
		{
			HashMap<String, String> data=techData.getData();

			for(String autoLabel: data.keySet())
			{
				MetaDbHelper.note("Viewing parsed metadata: auto."+autoLabel+": "+data.get(autoLabel));
				TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_AUTO_ELEMENT, autoLabel, data.get(autoLabel));
			}
			return true;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}
	/**
	 * Adds a blank row to contain data for an administrative field to a particular item in a project.
	 * @param projectName The project where the blank field will be inserted.
	 * @param itemNumber The item number for which to insert the blank field. 
	 * @param element The element name of the metadata field. 
	 * @param label The label name of the metadata field. 
	 */
	private static boolean addBlankAdminData(String projectName, int itemNumber, String element, String label)
	{	
		if(itemNumber!=-1)
		{
			Connection conn=Conn.initialize();
			if(conn!=null)

			{
				try

				{
					PreparedStatement addBlankAdmin=conn.prepareStatement(ADD_ADMIN_DATA);

					addBlankAdmin.setString(1, projectName);
					addBlankAdmin.setInt(2, itemNumber);
					addBlankAdmin.setString(3, Global.MD_TYPE_ADMIN);
					addBlankAdmin.setString(4, element);
					addBlankAdmin.setString(5, label);
					addBlankAdmin.setString(6, "");

					addBlankAdmin.executeUpdate();

					addBlankAdmin.close();
					conn.close();
					return true;
				}
				catch(SQLException sqle)
				{
					MetaDbHelper.logEvent(sqle);
				}
			}
		}
		return false;
	}	

	/**
	 * Adds a blank row to contain data for a descriptive field to a particular item in a project.
	 * @param projectName The project where the blank field will be inserted.
	 * @param itemNumber The item number for which to insert the blank field. 
	 * @param element The element name of the metadata field. 
	 * @param label The label name of the metadata field. 
	 */
	private static boolean addBlankDescData(String projectName, int itemNumber, String element, String label)
	{
		if(itemNumber!=-1)
		{
			Connection conn=Conn.initialize();
			if(conn!=null)
			{
				try

				{
					PreparedStatement addBlankDesc=conn.prepareStatement(ADD_DESC_DATA);

					addBlankDesc.setString(1, projectName);
					addBlankDesc.setInt(2, itemNumber);
					addBlankDesc.setString(3, Global.MD_TYPE_DESC);
					addBlankDesc.setString(4, element);
					addBlankDesc.setString(5, label);
					addBlankDesc.setString(6, "");//blank data

					addBlankDesc.executeUpdate();

					addBlankDesc.close();
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

	/**
	 * Adds a blank row to contain data for a custom technical metadata field for a particular item in a project. 
	 * @param projectName The project where the blank field will be inserted. 
	 * @param itemNumber The item number for which to insert the blank field. 
	 * @param element The element name of the custom technical metadata field.
	 */
	private static boolean addBlankCustomTechData(String projectName, int itemNumber, String element, String label)
	{
		if(itemNumber!=-1)
		{
			Connection conn=Conn.initialize();
			if(conn!=null)

			{
				try

				{
					PreparedStatement addBlankCustomTech=conn.prepareStatement(ADD_TECH_DATA);

					addBlankCustomTech.setString(1, projectName);
					addBlankCustomTech.setInt(2, itemNumber);
					addBlankCustomTech.setString(3, element);
					addBlankCustomTech.setString(4, label);
					addBlankCustomTech.setString(5, "");

					addBlankCustomTech.executeUpdate();

					addBlankCustomTech.close();
					conn.close();
					return true;
				}
				catch(SQLException sqle)
				{
					MetaDbHelper.logEvent(sqle);
				}
			}
		}
		return false;
	}
	/**
	 * Checks if an item has been updated since its last update.
	 * @param itemNumber the item number to check for update.
	 * @return true if the file has been modified, false otherwise.
	 */
	private static boolean needsUpdate(String projectName, int itemNumber)
	{
		String currentChecksum=getChecksum(projectName, itemNumber);
		String currentDate=getLastModified(projectName, itemNumber);
		MetaDbHelper.note("Check for update: Current Checksum: "+currentChecksum);
		MetaDbHelper.note("Check for update: Current last-modified date: "+currentDate);
		String filePath=getMasterPath(projectName, itemNumber);
		MetaDbHelper.note("Updating file path: "+filePath);
		String newChecksum=FileManager.computeChecksum(filePath);
		String newDate=FileManager.computeLastModified(filePath);
		MetaDbHelper.note("New checksum: "+newChecksum);
		MetaDbHelper.note("New last-modified date: "+newDate);
		boolean checkSumNeed=(!(currentChecksum.equals(newChecksum)))||currentChecksum.equals("")||currentChecksum==null;
		boolean dateNeed=(!(currentDate.equals(newDate)))||currentDate.equals("")||currentDate==null;
		boolean needsUpdate=checkSumNeed||dateNeed;
		/*
		if(!needsUpdate)
			MetaDbHelper.note("Checksum and modification dates for "+projectName+"-"+filePath+" equal. No need for updating.");

		else
		{
			MetaDbHelper.note("Checksum or modification date for "+projectName+"-"+filePath+" not equal or invalid. Updating item...");
		}
		 */
		return needsUpdate;

	}

	/**	
	 * Updates an existing item in a project, re-populating the auto-generated technical data.  
	 * @param projectName The project name to update.
	 * @param itemNumber The item number to update. 
	 * @return true if the item was successfully updated, false otherwise.
	 */
	private static boolean updateItem(String projectName, int itemNumber)
	{
		if (!TechnicalDataDAO.techDataExists(projectName, itemNumber))
			addCustomTechData(projectName, itemNumber);
		String filePath=getMasterPath(projectName, itemNumber);
		MetaDbHelper.note("Parsing and updating item entry : Project "+projectName+", item# "+itemNumber+", file name "+filePath);
		AutoTechData techData=ImageParser.parseImage(projectName, filePath, itemNumber);

		if(techData!=null)
		{
			try
			{
				HashMap<String, String> data=techData.getData();
				for(String techLabel: data.keySet())
				{
					TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_AUTO_ELEMENT, techLabel, data.get(techLabel));
				}										
				TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_ZOOM_ELEMENT, Global.TECH_ZOOM_LABEL, buildZoomUrl(projectName, itemNumber));
				TechnicalDataDAO.updateTechData(projectName, itemNumber, Global.TECH_DOWNLOAD_ELEMENT, Global.TECH_DOWNLOAD_LABEL, buildDownloadUrl(projectName, itemNumber));

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
	 * Update the derivative paths of an item.
	 * @param projectName The project which the item is in 
	 * @param itemNumber The item number of the item to update
	 * @param newChecksum The new checksum for this item.
	 * @return true if the checksum was successfully updated, false otherwise.
	 */
	private static boolean updateDerivPaths(String projectName, int itemNumber, String newCustomDerivPath, String newZoomDerivPath)
	{
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement updateDerivPaths=conn.prepareStatement(UPDATE_DERIV_PATHS);
				updateDerivPaths.setString(1, newCustomDerivPath);
				updateDerivPaths.setString(2, newZoomDerivPath);
				updateDerivPaths.setString(3, projectName);
				updateDerivPaths.setInt(4, itemNumber);

				updateDerivPaths.executeUpdate();
				updateDerivPaths.close();

				conn.close();
				MetaDbHelper.note("Deriv paths updated");
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
	 * Get the locker's user name of an item
	 * @param projectName the project to get the item from
	 * @param itemNumber the item number
	 * @return a String representation of the locker user name
	 */
	public static String getLocker(String projectName, int itemNumber) {
		String locker="";
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getLocker=conn.prepareStatement(GET_LOCKER);
				getLocker.setString(1, projectName);
				getLocker.setInt(2, itemNumber);

				ResultSet res=getLocker.executeQuery();
				if(res.next())
				{
					locker=res.getString(Global.ITEM_LOCKER);
					MetaDbHelper.note("Project "+projectName+": Item "+itemNumber+" locker "+locker);
				}
				//else
				//	MetaDbHelper.logEvent("ItemsDAO", "getLocker", "Could not retrieve locker!");

				res.close();
				getLocker.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		else
			MetaDbHelper.logEvent("ItemsDAO", "getLocker", "Database error!");
		return locker;
	}

	/**
	 * Gets the item number from items table based on the filename it is stored under. 
	 * @param projectName The project to get the item for. 
	 * @param fileName The filename of the desired item. 
	 * @return the item number of the item with the given filename in the given project. 
	 * If the item cannot be found, the method will return -1. 
	 */
	private static int getItemNumber(String projectName, String fileName)
	{
		MetaDbHelper.note("getItemNumber: Project: "+projectName+" file name: "+fileName);
		int itemNo=-1;
		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{
				PreparedStatement getItemNumber=conn.prepareStatement(GET_ITEM_NUMBER);
				getItemNumber.setString(1, projectName);
				getItemNumber.setString(2, fileName);

				ResultSet res=getItemNumber.executeQuery();
				if(res.next())
					itemNo=res.getInt(Global.ITEM_NUMBER);

				else 
				{
					MetaDbHelper.logEvent("ItemsDAO", "getItemNumber", "Item number not found for file "+fileName+" in project "+projectName);
					itemNo = -1;
				}

				res.close();
				getItemNumber.close();

				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				itemNo = -1;
			}
		}

		return itemNo;
	}

	private static String buildZoomUrl(String projectName, int itemNumber)
	{
		String buildUrl=new String(Global.ZOOM_URL_BASE);
		buildUrl=buildUrl.replaceAll("\\"+Global.ZOOM_URL_PROJ, projectName);
		buildUrl=buildUrl.replaceAll("\\"+Global.ZOOM_URL_ITEM, padZeros(itemNumber));
		String projectBaseUrl=ProjectsDAO.getProjectData(projectName).getBaseUrl();
		char lastChar=projectBaseUrl.charAt(projectBaseUrl.length()-1);
		return (lastChar=='/'? projectBaseUrl+buildUrl: projectBaseUrl+"/"+buildUrl);	
	}

	private static String buildDownloadUrl(String projectName, int itemNumber)
	{
		DerivativeSetting customSetting = DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING);
		boolean generateCustom = (customSetting.isEnabled()? true : false);
		if (generateCustom==false)
			return "";
		String buildUrl=new String(Global.DOWNLOAD_URL_BASE);
		buildUrl=buildUrl.replaceAll("\\"+Global.DOWNLOAD_URL_PROJ, projectName);
		buildUrl=buildUrl.replaceAll("\\"+Global.DOWNLOAD_URL_ITEM, padZeros(itemNumber));
		String projectBaseUrl=ProjectsDAO.getProjectData(projectName).getBaseUrl();
		char lastChar=projectBaseUrl.charAt(projectBaseUrl.length()-1);
		return (lastChar=='/'? projectBaseUrl+buildUrl: projectBaseUrl+"/"+buildUrl);	
	}

	/**
	 * Get a sorted list of all the new items from a project master path.
	 * @param projectName project name to get new files of.
	 * @param masterPath The master-image path of the project.
	 * @return an ArrayList of Files containing only those items that are not already
	 * in the DB.
	 */
	private static ArrayList<File> getNewImages(String projectName, String masterPath)
	{
		MetaDbHelper.note("Getting new images for "+projectName+"...");
		File projectDir=new File(masterPath);
		FileFilter fileFilter=new FileFilter()
		{
			public boolean accept(File file)
			{
				return !(file.isDirectory());
			}
		};
		//Get a list of all the files in the project master path
		File[] allProjectItems=projectDir.listFiles(fileFilter);		

		ArrayList<File> newItems=new ArrayList<File>(allProjectItems.length);
		Arrays.sort(allProjectItems);
		for(File image: allProjectItems)
		{
			try
			{
				String filePath=image.getAbsolutePath();
				if(!itemExists(projectName, filePath))
					newItems.add(image);
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				break;
			}
		}
		MetaDbHelper.note("Found "+newItems.size()+" new images in "+projectName+"...");
		return newItems;
	}

	/**
	 * 
	 * Removes leading zeroes from a string.
	 * @param str the String to remove leading zeroes from.
	 * @return the String, with the zeroes removed.
	 */
	private static String removeZeroes(String str)
	{
		if (str == null)
		{
			return null;
		}
		char[] chars = str.toCharArray();
		int index=0;
		for (; index < str.length();
		index++)
		{
			if (chars[index] != '0')
			{
				break;
			}
		}
		return (index == 0) ? str :
			str.substring(index);
	}

	/**
	 * Recreate required attributes if they are missing (ex. after import)
	 * @param projectName the name of the project to check for missing attributes. 
	 */
	private static void checkRequiredAttributes(String projectName)
	{
		if (!TechAttributesDAO.attributeExists(projectName, Global.TECH_AUTO_ELEMENT, "FileName"))
			TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "FileName");
		if (!TechAttributesDAO.attributeExists(projectName, Global.TECH_AUTO_ELEMENT, "Checksum"))
			TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "Checksum");
		if (!TechAttributesDAO.attributeExists(projectName, Global.TECH_AUTO_ELEMENT, "DateModified"))
			TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_AUTO_ELEMENT, "DateModified");	
		if (!TechAttributesDAO.attributeExists(projectName, Global.TECH_ZOOM_ELEMENT, Global.TECH_ZOOM_LABEL))
			TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_ZOOM_ELEMENT, Global.TECH_ZOOM_LABEL);
		if (!TechAttributesDAO.attributeExists(projectName, Global.TECH_DOWNLOAD_ELEMENT, Global.TECH_DOWNLOAD_LABEL))
			TechAttributesDAO.createTechnicalAttribute(projectName, Global.TECH_DOWNLOAD_ELEMENT, Global.TECH_DOWNLOAD_LABEL);
	}

	/**
	 * Get a digit sequence representing the item number from the given filepath.
	 * @param path the file path to extract the digit sequence from.
	 * @return the extracted digit sequence, or an empty string ("") if not found
	 */
	private static String getNumSubString(String path)
	{
		MetaDbHelper.note("Trying to extract digits from "+path);
		//Match any digits +fileExt.
		Pattern digExt=Pattern.compile("\\d+\\.+.*"); //get consecutive integers and file extension
		Matcher stringMatcher=digExt.matcher(path);
		String number="";

		//Regex match found; extract the first occurrence 
		if(stringMatcher.find())
		{
			MetaDbHelper.note("Pattern match: "+stringMatcher.group());			
			number=(stringMatcher.group().split("\\.")[0]);
		}

		return number;
	}		

	private static String padZeros(int itemNumber)
	{
		int limit=5-(Integer.toString(itemNumber)).length();
		String prefix="";
		for (int i=0; i<limit; i++)
			prefix+="0";

		return prefix+itemNumber;
	}
	private static String toHMS(int secs)
	{
		int hours=secs/3600;
		int remainder=secs%3600;

		int minutes=remainder/60;
		int seconds=remainder%60;

		return ((hours <10 ? "0":"")+hours 
				+":"+(minutes<10 ? "0":"")+minutes
				+":"+(seconds<10 ? "0":"")+seconds);
	}
}
