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
package edu.lafayette.metadb.model.fileio;
import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.imagemgt.DerivativeDAO;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.SimpleDateFormat;

/**
 * Handles all translations between project-item paths and file system paths, as well as some other
 * minor operations (checksums, modified dates, deleting derivatives)
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class FileManager 
{
	private static final String GET_CUSTOM_DERIV_FILENAMES=

		"SELECT "+Global.ITEM_CUSTOM_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String GET_ZOOM_DERIV_FILENAMES=

		"SELECT "+Global.ITEM_ZOOM_FILE_NAME+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String HEXES = "0123456789abcdef"; //String used to compute checksum

	/**
	 * Delete all derivatives (except for thumbnails) of a project.
	 * @param projectName the project to clear derivatives for
	 * @return true if derivatives all cleared, or if there were no derivatives; false otherwise.
	 */
	public static boolean deleteDerivatives(String projectName)
	{
		String dirPath=Global.DERIV_DIRECTORY+File.separator+projectName;
		
		File derivativeDir=new File(dirPath);
		MetaDbHelper.note("Derivative dir path: "+dirPath);
		MetaDbHelper.note("Derivative dir exists: "+derivativeDir.exists());
		File[] fileList=derivativeDir.listFiles();
		if (fileList==null || fileList.length==0)
			return true;
		else
		{
			try
			{
				SecurityManager security = System.getSecurityManager();
				if (security == null) {
					security = new SecurityManager();
				}
				if (security != null) {

					MetaDbHelper.note("Security exists");
					for (File img: fileList)
					{
						if(img.exists())
						{
							String path = img.getAbsolutePath();

							MetaDbHelper.note("Trying to delete "+path);
							try 
							{
								//security.checkDelete(path);
								img.delete();
								if(!(img.exists()))
									MetaDbHelper.note("Deleted "+path);
								else
									MetaDbHelper.note("Cannot delete "+path);
							}	 	
							catch (Exception e) 
							{
								MetaDbHelper.logEvent(e);
							}
						}
					}
					return true;
				}
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Delete custom derivatives for one project
	 * @param projectName project to get rid of custom derivatives
	 * @return true if custom derivatives for this project were deleted, false otherwise.
	 */
	public static boolean deleteCustomDerivatives(String projectName)
	{
		ArrayList<File> toDelete=getCustomDerivFiles(projectName);
		try
		{
			for (File f: toDelete)
			{
				if (f.exists())
					f.delete();
			}
			return true;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
			return false;
		}
	}

	/**
	 * Delete zoom derivatives for one project
	 * @param projectName project to get rid of zoom derivatives
	 * @return true if zoom derivatives for this project were deleted, false otherwise.
	 */
	public static boolean deleteZoomDerivatives(String projectName)
	{
		ArrayList<File> toDelete=getZoomDerivFiles(projectName);
		try
		{
			for (File f: toDelete)
			{
				if (f.exists())
					f.delete();
			}
			return true;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
			return false;
		}
	}

	/**
	 * Get all the custom derivatives for a project
	 * @param projectName the project to retrieve all the custom derivative files for.
	 * @return an ArrayList of Files representing the retrieved custom derivative files.
	 */
	public static ArrayList<File> getCustomDerivFiles(String projectName)
	{
		ArrayList<File> imgs=new ArrayList<File>();

		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{

				PreparedStatement getCustomDerivFiles=conn.prepareStatement(GET_CUSTOM_DERIV_FILENAMES);
				getCustomDerivFiles.setString(1, projectName);

				ResultSet res=getCustomDerivFiles.executeQuery();
				while(res.next())
				{
					imgs.add(new File(res.getString(Global.ITEM_CUSTOM_FILE_NAME)));
				}

				res.close();
				getCustomDerivFiles.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return imgs;
	}

	/**
	 * Get all the zoom derivatives for a project
	 * @param projectName the project to retrieve all the zoom derivative files for.
	 * @return an ArrayList of Files representing the retrieved zoom derivative files.
	 */
	public static ArrayList<File> getZoomDerivFiles(String projectName)
	{
		ArrayList<File> imgs=new ArrayList<File>();

		Connection conn=Conn.initialize();

		if(conn!=null)
		{
			try
			{

				PreparedStatement getZoomDeriv=conn.prepareStatement(GET_ZOOM_DERIV_FILENAMES);
				getZoomDeriv.setString(1, projectName);

				ResultSet res=getZoomDeriv.executeQuery();
				while(res.next())
				{
					imgs.add(new File(res.getString(Global.ITEM_ZOOM_FILE_NAME)));
				}

				res.close();
				getZoomDeriv.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return imgs;
	}
	/**
	 * Delete a set of files
	 * @param toDelete Array of files to delete.
	 * @return true if all the files existed and were deleted, false otherwise.
	 */
	public static boolean deleteFiles(File[] toDelete)
	{
		boolean deleted = true;
		if(toDelete.length == 0)
			return true;

		try
		{
			SecurityManager security = System.getSecurityManager();
			if (security == null) {
				security = new SecurityManager();
			}
			if (security != null) {
				MetaDbHelper.note("Security exists");
				for (File img: toDelete)
				{
					if(img.exists())
					{
						String path = img.getAbsolutePath();

						MetaDbHelper.note("Trying to delete "+path);
						try 
						{
							//security.checkDelete(path);
							if(!img.delete())
								deleted=false;
							if(!(img.exists())) {
								MetaDbHelper.note("Deleted "+path);
								deleted=true;
							}
							else
								MetaDbHelper.note("Cannot delete "+path);
						}	 	
						catch (Exception e) 
						{
							MetaDbHelper.logEvent(e);
						}
					}
				}
			}
			return deleted;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
			return false;
		}
	}

	/**
	 * Get a sorted array of project master file directories.
	 * @return a sorted array of Files containing the project master file directories.
	 */
	public static File[] getMasterDirectories()
	{
		//Create a File object for the root projects directory.
		File dir = new File(Global.MASTERS_DIRECTORY);
		MetaDbHelper.logEvent("FileManager", "getMasterDirectories", "Getting all projects master directories...");
		File[] projects = null; 

		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};

		//Get a list of project directories.
		projects = dir.listFiles(fileFilter);
		Arrays.sort(projects);
		return projects;
	}

	/**
	 * Get a sorted list of all the files in a project directory.
	 * @param masterPath the master path of the project directory
	 * @return an ArrayList of Files containing all the files in the master path, 
	 * excluding directories.
	 */
	public static ArrayList<File> getProjectFiles(File masterPath)
	{
		MetaDbHelper.note("Trying to get project files from "+masterPath.getAbsolutePath());
		//Filter that only accepts non-directories (in case.)
		FileFilter fileFilter = new FileFilter() 
		{
			public boolean accept(File file)
			{
				//Ignore directories and non-TIFF/JPG files.
				String[] filenamePart = file.getName().split("\\.");
				boolean isTiff = filenamePart[filenamePart.length - 1].equals("tif");
				boolean isJpg = filenamePart[filenamePart.length - 1].equals("jpg");
				boolean isValid = isTiff || isJpg;
				return (!file.isDirectory() && isValid);
			}
		};

		//Get a list of items in the project.
		File[] projectItems=masterPath.listFiles(fileFilter);
		Arrays.sort(projectItems);
		ArrayList<File> projectImages = new ArrayList<File>(Arrays.asList(projectItems));
		return projectImages;
	}

	private static String getDerivFileName(String projectName, String fileName, String ext)
	{
		String[] fileNameTokens=fileName.split("\\.");
		String name="";

		if(fileNameTokens.length<2)
			name=fileName+ext;

		else
			name=fileNameTokens[fileNameTokens.length-2]+ext;
		return name;
	}

	private static String getDerivFilePath(String projectName, String fileName, String ext)
	{
		String[] fileNameTokens=fileName.split("\\.");
		String name="";

		if(fileNameTokens.length<2)
			name=fileName+ext;

		else
			name=fileNameTokens[fileNameTokens.length-2]+ext;
		return Global.DERIV_DIRECTORY+"/"+projectName+"/"+name;
	}

	/**
	 * Get the thumbnail path based on project name and filename, and predetermined globalvars.
	 * @param projectName The project name.
	 * @param fileName The filename to get the thumbnail-path of.
	 * @return a String representing the thumbnail path, which can be used to create a File or InputStream.
	 */
	public static String getThumbPath(String projectName, String fileName)
	{
		String path= getDerivFilePath(projectName, fileName, Global.THUMB_EXTENSION);
		//		MetaDbHelper.note("Thumbnail path (adding new): "+path+"...");
		return path;
	}

	/**
	 * Get the thumbnail path based on project name and filename, and predetermined globalvars.
	 * @param projectName The project name.
	 * @param fileName The filename to get the thumbnail-path of.
	 * @return a String representing the thumbnail path, which can be used to create a File or InputStream.
	 */
	public static String getThumbFileName(String projectName, String fileName)
	{
		String path= getDerivFileName(projectName, fileName, "-"+(DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_THUMB_SETTING).getMaxHeight())+(Global.THUMB_EXTENSION));
		//		MetaDbHelper.note("Thumbnail path (adding new): "+path+"...");
		return path;
	}

	/**
	 * Get the custom derivative path based on project name and filename, and db entry for custom derivs.
	 * @param projectName The project name.
	 * @param fileName The filename to get the custom derivative path of.
	 * @return a String representing the custom derivative path, which can be used to create a File or InputStream.
	 */
	public static String getCustomDerivFileName(String projectName, String fileName)
	{
		String path=getDerivFileName(projectName, fileName, "-"+(DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING).getMaxHeight())+(Global.CUSTOM_DERIV_EXTENSION));
		return path;
	}

	/**
	 * Get the zoom derivative path based on project name and filename, and predetermined globalvars.
	 * @param projectName The project name.
	 * @param fileName The filename to get the zoom derivative path of.
	 * @return a String representing the zoom derivative path, which can be used to create a File or InputStream.
	 */
	public static String getZoomDerivFileName(String projectName, String fileName)
	{
		String path=getDerivFileName(projectName, fileName, "-"+(DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_ZOOM_SETTING).getMaxHeight())+(Global.ZOOM_DERIV_EXTENSION));
		return path;
	}

	/**
	 * Get the fullsize derivative path based on project name and filename, and predetermined globalvars.
	 * @param projectName The project name.
	 * @param fileName The filename to get the full size derivative path of.
	 * @return a String representing the full size derivative path, which can be used to create a File or InputStream.
	 */
	public static String getFullSizeDerivFileName(String projectName, String fileName)
	{
		String path=getDerivFileName(projectName, fileName, Global.FULLSIZE_DERIV_EXTENSION);
		return path;
	}

	/**
	 * Compute MD5 checksum for a file as a hex string.
	 * @param fileName The full path of the file to be checksummed.
	 * @return the checksum, as a hex string, of the file.
	 */
	public static String computeChecksum(String fileName) {
		MetaDbHelper.note("Computing Checksum for file "+fileName);
		String Checksum = "";
		try {
			InputStream fis =  new FileInputStream(fileName);
			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) 
					complete.update(buffer, 0, numRead);
			} while (numRead != -1);
			fis.close();
			Checksum = asHex(complete.digest());
		}
		catch(Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return Checksum;
	}	

	/**
	 * Get last modified date for a filepath
	 * @param fileName The file path to get the last modified date of
	 * @return a String representing the last modification date for this file path
	 */
	public static String computeLastModified(String fileName) {
		MetaDbHelper.note("Getting last-modified date for file "+fileName);
		String date="";
		try {
			File itm=new File(fileName);
			//Date format: HH is in 24-hour format. Add "aa" after the string and change 
			// "HH" to "hh" to make it into 12-hour format and add the "am" or "pm" text.
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			
			date=formatter.format(new Date(itm.lastModified()));

		}
		catch(Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return date;
	}

	/**
	 * Converts a byte array to a hexadecimal string.
	 * @param buf the byte array to convert to a hex-string.
	 * @return buf, as a hex-string.
	 */
	public static String asHex( byte [] buf ) 
	{
		if ( buf == null ) {
			return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * buf.length );
		for ( final byte b : buf ) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4))
			.append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}


	/**
	 * Get the custom derivative path based on project name and filename, and db entry for custom derivs.
	 * @param projectName The project name.
	 * @param fileName The filename to get the custom derivative path of.
	 * @return a String representing the custom derivative path, which can be used to create a File or InputStream.
	 */
	public static String getCustomDerivPath(String projectName, String fileName)
	{
		String path=getDerivFilePath(projectName, fileName, "-"+(DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING).getMaxHeight())+(Global.CUSTOM_DERIV_EXTENSION));
		return path;
	}

	/**
	 * Get the zoom derivative path based on project name and filename, and predetermined globalvars.
	 * @param projectName The project name.
	 * @param fileName The filename to get the zoom derivative path of.
	 * @return a String representing the zoom derivative path, which can be used to create a File or InputStream.
	 */
	public static String getZoomDerivPath(String projectName, String fileName)
	{
		String path=getDerivFilePath(projectName, fileName, "-"+(DerivativeDAO.getDerivativeSetting(projectName, Global.DERIV_ZOOM_SETTING).getMaxHeight())+(Global.ZOOM_DERIV_EXTENSION));
		return path;
	}

	/**
	 * Get the fullsize derivative path based on project name and filename, and predetermined globalvars.
	 * @param projectName The project name.
	 * @param fileName The filename to get the full size derivative path of.
	 * @return a String representing the full size derivative path, which can be used to create a File or InputStream.
	 */
	public static String getFullSizeDerivPath(String projectName, String fileName)
	{
		String path=getDerivFilePath(projectName, fileName, Global.FULLSIZE_DERIV_EXTENSION);
		return path;
	}
}
