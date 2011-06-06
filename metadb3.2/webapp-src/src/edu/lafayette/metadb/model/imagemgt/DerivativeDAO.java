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

package edu.lafayette.metadb.model.imagemgt;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Class to handle all generation of derivative images and updating derivative generation settings.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */

@SuppressWarnings("unused")
public class DerivativeDAO 
{

	private static final String CREATE_DERIV_SETTING =

		"INSERT INTO " +
		Global.DERIVATIVE_SETTINGS_TABLE+
		"(" +
		Global.PROJECT_NAME+","+
		Global.DERIVATIVE_SETTING_NAME+","+
		Global.DERIVATIVE_MAX_WIDTH+","+
		Global.DERIVATIVE_MAX_HEIGHT+","+
		Global.DERIVATIVE_BRAND_TEXT+","+
		Global.DERIVATIVE_BG_COLOR+","+
		Global.DERIVATIVE_FG_COLOR+","+
		Global.DERIVATIVE_ANNOTATION_MODE+","+
		Global.DERIVATIVE_ENABLED+
		")"+" "+

		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, 't')";

	private static final String UPDATE_DERIV_SETTING=

		"UPDATE "+Global.DERIVATIVE_SETTINGS_TABLE+" "+
		"SET "+
		Global.DERIVATIVE_MAX_WIDTH+"=?, "+
		Global.DERIVATIVE_MAX_HEIGHT+"=?, "+
		Global.DERIVATIVE_BG_COLOR+"=?, "+
		Global.DERIVATIVE_FG_COLOR+"=?, "+
		Global.DERIVATIVE_ANNOTATION_MODE+"=?, "+
		Global.DERIVATIVE_ENABLED+"=? "+

		"WHERE "+Global.PROJECT_NAME+"=? "+" "+
		"AND "+Global.DERIVATIVE_SETTING_NAME+"=?";

	private static final String TOGGLE_SETTING=
		"UPDATE "+Global.DERIVATIVE_SETTINGS_TABLE+" "+
		"SET "+Global.DERIVATIVE_ENABLED+"=? "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.DERIVATIVE_SETTING_NAME+"=?";

	private static final String UPDATE_BRAND_TEXT=

		"UPDATE "+Global.DERIVATIVE_SETTINGS_TABLE+" "+
		"SET "+
		Global.DERIVATIVE_BRAND_TEXT+"=? "+

		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String REMOVE_DERIV_SETTING=

		"DELETE FROM "+
		Global.DERIVATIVE_SETTINGS_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+" "+
		"AND "+Global.DERIVATIVE_SETTING_NAME+"=?";

	private static final String GET_DERIV_SETTING=

		"SELECT * FROM "+
		Global.DERIVATIVE_SETTINGS_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=? "+
		"AND "+Global.DERIVATIVE_SETTING_NAME+"=?";


	public DerivativeDAO()
	{
	}
	/**
	 * Wrapper method for generating a thumbnail image. 
	 * Will produce a thumbnail for the supplied image.
	 * @param projectName the project name.
	 * @return true if the thumbnail was successfully generated; false otherwise.
	 */
	public static boolean generateIndividualThumb(String projectName, String inputFileName, String outputFileName)
	{
		try
		{
			//MetaDbHelper.note("Generate individual thumb: input-"+inputFileName+"; output="+outputFileName);
			DerivativeSetting thumbSetting=getDerivativeSetting(projectName, Global.DERIV_THUMB_SETTING);
			if(thumbSetting==null || !thumbSetting.isEnabled())
				return false;

			int width=thumbSetting.getMaxWidth();
			int height=thumbSetting.getMaxHeight();
			//MetaDbHelper.note("Thumbnail setting for project "+projectName+" found. W="+width+", H="+height);
			boolean success= ImageGenerator.generateDerivative(inputFileName, outputFileName, width, height);
			
			//MetaDbHelper.note("Thumb Succeeded: "+success);
			return success;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Process a medium derivative for one image. 
	 * @param projectName project name.
	 * @param inputFileName The input file name.
	 * @param outputFileName The output file.
	 * @return true if successfully generated; false otherwise.
	 */
	public static boolean generateIndividualCustom(String projectName, String inputFileName, String outputFileName)
	{
		try{
			DerivativeSetting customSetting=getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING);
			if(customSetting == null || !customSetting.isEnabled())
				return false;

			//MetaDbHelper.note("Generate individual CUSTOM: input-"+inputFileName+"; output="+outputFileName);

			int width=customSetting.getMaxWidth();
			int height=customSetting.getMaxHeight();
			String brand=customSetting.getBrand();
			String bgColor=customSetting.getBgColor();
			String fgColor=customSetting.getFgColor();
			int annotationMode=customSetting.getAnnotationMode();
			//MetaDbHelper.note("CUSTOM setting for project "+projectName+" found. W="+width+", H="+height);

			boolean success= ImageGenerator.generateDerivative(inputFileName, outputFileName, width, height);
			
			//MetaDbHelper.note("Custom Succeeded: "+success);
			if(annotationMode!=0)
				success &= ImageGenerator.processAnnotation(outputFileName, annotationMode, brand, bgColor, fgColor);
	
			//MetaDbHelper.note("Both generating derivative and annotation succeeded: "+success);
			return success;
			}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Process a zoom derivative for one image. 
	 * @param projectName project name.
	 * @param inputFileName The input file name.
	 * @param outputFileName The output file.
	 * @return true if successfully generated; false otherwise.
	 */
	public static boolean generateIndividualZoom(String projectName, String inputFileName, String outputFileName)
	{
		try
		{
			DerivativeSetting zoomSetting=getDerivativeSetting(projectName, Global.DERIV_ZOOM_SETTING);
			if(zoomSetting==null || !zoomSetting.isEnabled())
				return false;
			//MetaDbHelper.note("Generate individual ZOOM: input-"+inputFileName+"; output="+outputFileName);

			int width=zoomSetting.getMaxWidth();
			int height=zoomSetting.getMaxHeight();
			String brand=zoomSetting.getBrand();
			String bgColor=zoomSetting.getBgColor();
			String fgColor=zoomSetting.getFgColor();
			int annotationMode=zoomSetting.getAnnotationMode();
			
			boolean success = ImageGenerator.generateDerivative(inputFileName, outputFileName, width, height);
			
			//MetaDbHelper.note("Zoom Succeeded: "+success);
			if(annotationMode!=0)
				success &= ImageGenerator.processAnnotation(outputFileName, annotationMode, brand, bgColor, fgColor);
	
			//MetaDbHelper.note("Both generating derivative and annotation succeeded: "+success);
			return success;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Process a full size derivative for one image. 
	 * @param projectName project name.
	 * @param inputFileName The input file name.
	 * @param outputFileName The output file.
	 * @return true if successfully generated; false otherwise.
	 */
	public static boolean generateIndividualFullSize(String projectName, String inputFileName, String outputFileName)
	{
		try
		{
			DerivativeSetting fullSizeSetting=getDerivativeSetting(projectName, Global.DERIV_FULLSIZE_SETTING);
			if(fullSizeSetting == null || !fullSizeSetting.isEnabled())
				return false;

			int width=fullSizeSetting.getMaxWidth();
			int height=fullSizeSetting.getMaxHeight();

			String brand=fullSizeSetting.getBrand();
			String bgColor=fullSizeSetting.getBgColor();
			String fgColor=fullSizeSetting.getFgColor();
			int annotationMode=fullSizeSetting.getAnnotationMode();
			
			boolean success=ImageGenerator.generateDerivative(inputFileName, outputFileName, width, height);
			//MetaDbHelper.note("Fullsize Succeeded: "+success);
			if(annotationMode!=0)
				success &= ImageGenerator.processAnnotation(outputFileName, annotationMode, brand, bgColor, fgColor);
	
			//MetaDbHelper.note("Both generating derivative and annotation succeeded: "+success);
			return success;
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}
	/**
	 * Process the small, medium and fullsize derivatives for one project. 
	 * @param projectName The project to process.
	 * @return true if derivative generation succeeded, false otherwise.
	 */
	public static boolean updateDerivatives(String projectName, ArrayList<File> masterImages)
	{
		//MetaDbHelper.note("Creating all enabled derivatives for "+projectName+"...");
		//Only succeed if all generations succeed.
		return generateThumb(projectName, masterImages)&& generateCustom(projectName, masterImages)&&generateZoom(projectName, masterImages) && generateFullSize(projectName, masterImages);
			
	}

	/**
	 * Generates thumbnails for one project.
	 * @param projectName The project to generate thumbnails for.
	 * @return true if the images have been processed, false otherwise.
	 */
	private static boolean generateThumb(String projectName, ArrayList<File> masterImages)
	{
		//Retrieve the derivative settings.
		DerivativeSetting thumbSetting=getDerivativeSetting(projectName, Global.DERIV_THUMB_SETTING);

		int maxWidth=thumbSetting.getMaxWidth();
		int maxHeight=thumbSetting.getMaxHeight();

		//Check for missing required variables.
		if(projectName == null || projectName.trim().equals("") || maxWidth==0 || maxHeight==0)
			return false;
		else
		{
			//Construct the master image dir and derivative path in which to generate derivatives.
			String masterPath = Global.MASTERS_DIRECTORY+"/"+projectName+"/";
			String derivPath = Global.DERIV_DIRECTORY+"/"+projectName+"/";
			int dimension=Global.DERIV_DEFAULT_SIZE_THUMB;

			//Check for filesystem errors.
			if(!new File(masterPath).exists() || !new File(derivPath).exists()){
				//MetaDbHelper.note("Thumbnail generation error: Master path or derivatives path does not exist!");
				return false;
			}

			//MetaDbHelper.note("Generating thumbnails for project "+projectName+"..."+masterImages.size()+" master files");
			try
			{
				for(File originalImage: masterImages)
				{
					//Do the generation.
					String outputFileName=getDerivFilePath(projectName, originalImage.getName(), "-"+dimension+(Global.THUMB_EXTENSION));
					ImageGenerator.generateDerivative(originalImage.getAbsolutePath(), outputFileName, Global.DERIV_DEFAULT_SIZE_THUMB, Global.DERIV_DEFAULT_SIZE_THUMB);
					//MetaDbHelper.note("Deriv path: "+outputFileName);
				}
				SysLogDAO.log("MetaDB", Global.SYSLOG_PROJECT, "Thumbnails generated for "+projectName);
				return true;
			}
			catch(Exception e)
			{
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error: Thumbnails for "+projectName+" not generated correctly.");
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}
	
	/**
	 * Generates custom derivatives for one project along with
	 * any tied branding settings.
	 * 
	 * @param projectName The project to generate custom derivatives for.
	 * @return true if the images have been processed, false otherwise.
	 */
	private static boolean generateCustom(String projectName, ArrayList<File> masterImages)
	{
		//Retrieve the derivative settings.
		DerivativeSetting customSetting=getDerivativeSetting(projectName, Global.DERIV_CUSTOM_SETTING);
		if(customSetting == null || !customSetting.isEnabled())
			return true;

		int maxWidth=customSetting.getMaxWidth();
		int maxHeight=customSetting.getMaxHeight();

		//Check for missing required variables.
		if(projectName == null || projectName.trim().equals("") || maxWidth==0 || maxHeight==0)
			return false;
		else
		{
			//Construct the master image dir and derivative path in which to generate derivatives.
			String masterPath = Global.MASTERS_DIRECTORY+"/"+projectName+"/";
			String derivPath = Global.DERIV_DIRECTORY+"/"+projectName+"/";
			int dimension=Math.max(maxWidth, maxHeight);
			//Check for filesystem errors.
			if(!new File(masterPath).exists() || !new File(derivPath).exists()){
				MetaDbHelper.note("Custom-derivative generation error: Master path or derivatives path does not exist!");
				return false;
			}

			//MetaDbHelper.note("Generating custom derivatives for project "+projectName+"...");
			try
			{
				for(File originalImage: masterImages)
				{
					//Do the generation.
					String outputFileName=getDerivFilePath(projectName, originalImage.getName(), "-"+dimension+(Global.CUSTOM_DERIV_EXTENSION));
					ImageGenerator.generateDerivative(originalImage.getAbsolutePath(), outputFileName, maxWidth, maxHeight);
					//MetaDbHelper.note("Deriv path: "+outputFileName);

					//Annotation is turned on, process the branding.
					String brand=customSetting.getBrand();
					String bgColor=customSetting.getBgColor();
					String fgColor=customSetting.getFgColor();
					int annotationMode=customSetting.getAnnotationMode();

					if(annotationMode!=0)
					{
						//MetaDbHelper.note("Annotation for "+outputFileName+" in process...");
						ImageGenerator.processAnnotation(outputFileName, annotationMode, brand, bgColor, fgColor);
					}
				}
				SysLogDAO.log("MetaDB", Global.SYSLOG_PROJECT, "Custom derivatives generated for "+projectName);
				return true;
			}
			catch(Exception e)
			{
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error: custom derivatives for "+projectName+" not generated correctly..");
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Generates all zoom derivatives for one project along with
	 * any tied branding settings.
	 * @param projectName The project to generate zoom derivatives for.
	 * @return true if the images have been processed, false otherwise.
	 */
	private static boolean generateZoom(String projectName, ArrayList<File> masterImages)
	{
		DerivativeSetting zoomSetting=getDerivativeSetting(projectName, Global.DERIV_ZOOM_SETTING);
		if(zoomSetting==null||!zoomSetting.isEnabled()) //Terminate if disabled
			return true;

		int maxWidth=zoomSetting.getMaxWidth();
		int maxHeight=zoomSetting.getMaxHeight();
		int dimension=Math.max(maxWidth, maxHeight);
		if(projectName==null||projectName.trim().equals("")||maxWidth==0||maxHeight==0)
			return false;
		else
		{
			//Construct the master image dir
			String masterPath=Global.MASTERS_DIRECTORY+"/"+projectName+"/";
			String derivPath=Global.DERIV_DIRECTORY+"/"+projectName+"/";

			if(new File(masterPath).exists()==false||new File(derivPath).exists()==false){
				MetaDbHelper.note("Zoom derivative generation error: Master path or derivatives path does not exist!");
				return false;
			}

			MetaDbHelper.note("Generating zoom derivatives for project "+projectName+"...");
			try{
				for(File originalImage: masterImages) 
				{
					String outputFileName=getDerivFilePath(projectName, originalImage.getName(), "-"+dimension+Global.ZOOM_DERIV_EXTENSION);
					MetaDbHelper.note("Deriv path: "+outputFileName);
					ImageGenerator.generateDerivative(originalImage.getAbsolutePath(), outputFileName, maxWidth, maxHeight);
				}
				SysLogDAO.log("MetaDB", Global.SYSLOG_PROJECT, "Zoom derivatives generated for "+projectName);
				return true;
			}
			catch(Exception e)
			{
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error: zoom derivatives for "+projectName+" not generated correctly.");
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Generates fullsize derivatives for one project along with
	 * any tied branding settings.
	 * @param projectName The project to generate medium derivatives for.
	 * @return true if the images have been processed, false otherwise.
	 */
	private static boolean generateFullSize(String projectName, ArrayList<File> masterImages)
	{
		DerivativeSetting fullSizeSetting=getDerivativeSetting(projectName, Global.DERIV_FULLSIZE_SETTING);
		if(fullSizeSetting==null||!fullSizeSetting.isEnabled())
			return true;

		if(projectName==null||projectName.trim().equals(""))
			return false;
		else
		{
			//Construct the master image dir
			String masterPath=Global.MASTERS_DIRECTORY+"/"+projectName+"/";
			String derivPath=Global.DERIV_DIRECTORY+"/"+projectName+"/";

			if(new File(masterPath).exists()==false||new File(derivPath).exists()==false){
				//MetaDbHelper.note("Fullsize-derivative generation error: Master path or derivatives path does not exist!");
				return false;
			}

			//Filter that only accepts non-directories (in case.)
			FileFilter fileFilter = new FileFilter() 
			{
				public boolean accept(File file){
					return !(file.isDirectory());
				}
			};

			//MetaDbHelper.note("Generating fullsize derivatives for project "+projectName+"...");
			String brand=fullSizeSetting.getBrand();
			String bgColor=fullSizeSetting.getBgColor();
			String fgColor=fullSizeSetting.getFgColor();
			int annotationMode=fullSizeSetting.getAnnotationMode();
			
			try
			{
				for(File originalImage: masterImages)
				{
					//MetaDbHelper.note("Processing "+originalImage.getName());
					String outputFileName=getDerivFilePath(projectName ,originalImage.getName(), Global.FULLSIZE_DERIV_EXTENSION);
					//MetaDbHelper.note("Deriv path: "+outputFileName);

					//Set width/height to 0 in underlying ImageGenerator call to signal full size (1.0 scale)
					ImageGenerator.generateDerivative(originalImage.getAbsolutePath(), outputFileName, 0, 0);
					//Append annotation, if applicable.
					if(annotationMode!=0)
						//File(outputFileName) should now have the correct dimensions. 
						//Use it directly to process the text annotating.
						ImageGenerator.processAnnotation(outputFileName, annotationMode, brand, bgColor, fgColor);
				}

				SysLogDAO.log("MetaDB", Global.SYSLOG_PROJECT, "Full size derivatives generated for "+projectName);
				return true;
			}
			catch(Exception e)
			{
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_ERROR, "Error: full size derivatives for "+projectName+" not generated correctly.");
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Adds a new derivative setting.
	 * @param projectName The project to add the setting to.
	 * @param settingName The name of the setting.
	 * @param maxWidth The maximum width for derivatives created by this setting. 
	 * @param maxHeight The maximum height for derivatives created by this setting. 
	 * @param brandText The branding text with which to annotate the derivative created by this setting.
	 * @param bgColor The background color for the branding text created by this derivative setting.
	 * @param fgColor The foreground(fill) color for the branding text created by this derivative setting.
	 * @param annotationMode The annotation mode for the derivative setting. (0=none, 1=band, 2=brand)
	 * @return true if the derivative setting was successfully added, false otherwise.
	 */
	public static boolean addDerivativeSetting(String projectName, String settingName, int maxWidth, int maxHeight, 
			String brandText, String bgColor, String fgColor, int annotationMode)
	{
		boolean additionSuccessful=false;
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement addDerivSetting=conn.prepareStatement(CREATE_DERIV_SETTING);
				addDerivSetting.setString(1, projectName);
				addDerivSetting.setString(2, settingName);
				addDerivSetting.setInt(3, maxWidth);
				addDerivSetting.setInt(4, maxHeight);
				addDerivSetting.setString(5, brandText);
				addDerivSetting.setString(6, bgColor);
				addDerivSetting.setString(7, fgColor);
				addDerivSetting.setInt(8, annotationMode);

				addDerivSetting.executeUpdate();
				additionSuccessful = true;

				//MetaDbHelper.logEvent("DerivativeDAO", "addDerivativeSetting", "New derivative setting \'"+settingName+"\', maxWidth="+maxWidth+", maxHeight="+maxHeight+") added to" +
				//		"project "+projectName);

				addDerivSetting.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				additionSuccessful = false;
			}
		}
		return additionSuccessful;
	}

	/**
	 * Get a derivative setting. 
	 * @param projectName The project the setting is associated with.
	 * @param derivativeName The derivative setting's name.
	 * @return a DerivativeSetting POJO containing the properties of the derivative setting.
	 */
	public static DerivativeSetting getDerivativeSetting(String projectName, String derivativeName)
	{
		DerivativeSetting setting=null;
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement getDerivSetting=conn.prepareStatement(GET_DERIV_SETTING);
				getDerivSetting.setString(1, projectName);
				getDerivSetting.setString(2, derivativeName);
				ResultSet rs=getDerivSetting.executeQuery();
				if(rs.next())
				{
					setting=new DerivativeSetting(
							rs.getString(Global.PROJECT_NAME), 
							rs.getString(Global.DERIVATIVE_SETTING_NAME),
							rs.getInt(Global.DERIVATIVE_MAX_WIDTH),
							rs.getInt(Global.DERIVATIVE_MAX_HEIGHT), 
							rs.getString(Global.DERIVATIVE_BRAND_TEXT),
							rs.getString(Global.DERIVATIVE_BG_COLOR),
							rs.getString(Global.DERIVATIVE_FG_COLOR),
							rs.getInt(Global.DERIVATIVE_ANNOTATION_MODE),
							rs.getBoolean(Global.DERIVATIVE_ENABLED));

					rs.close();
					getDerivSetting.close();
					
				}
				//No row
				//if(setting == null)
				//	MetaDbHelper.note("Derivative setting is null for "+projectName+ "("+derivativeName+")");
				//else
				//	MetaDbHelper.note("Project name: "+projectName+" derivative setting-"+setting.getSettingName()+" "+setting.getMaxWidth()+"x"+setting.getMaxWidth());
				conn.close();
				//DB error?	
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				setting = null;
			}
		}
		return setting;
	}

	/**
	 * Updates a derivative-setting.
	 * @param projectName The project which contains the derivative setting to be updated.
	 * @param settingName The setting name to be updated.
	 * @param maxWidth The new max width of the derivative setting.
	 * @param maxHeight The new max height of the derivative setting.
	 * @param bgColor The new background color of the derivative setting.
	 * @param fgColor The new foreground(fill) color of the derivative setting.
	 * @param annotationMode The new annotation mode of the derivative setting. 
	 * @param enabled Whether to enable the setting or not. 
	 * @return true if updated successfully, false otherwise.
	 */
	public static boolean updateDerivativeSetting(String projectName, String settingName, int maxWidth, int maxHeight, 
			String bgColor, String fgColor, int annotationMode, boolean enabled)
	{	
		//If the setting is not enabled, then just disable it and do nothing else.
		if(!enabled)
		{
			return disableSetting(projectName, settingName);
		}

		boolean updateSuccessful=false;
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement updateDerivSetting=conn.prepareStatement(UPDATE_DERIV_SETTING);
				updateDerivSetting.setInt(1, maxWidth);
				updateDerivSetting.setInt(2, maxHeight);
				updateDerivSetting.setString(3, bgColor);
				updateDerivSetting.setString(4, fgColor);
				updateDerivSetting.setInt(5, annotationMode);
				updateDerivSetting.setBoolean(6, enabled);
				updateDerivSetting.setString(7, projectName);
				updateDerivSetting.setString(8, settingName);

				//MetaDbHelper.logEvent("DerivativeDAO", "addDerivativeSetting", "Derivative setting \'"+settingName+"\' in project "+projectName+" updated");

				updateDerivSetting.executeUpdate();
				updateSuccessful = true;

				updateDerivSetting.close();
				conn.close();

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				updateSuccessful = false;
			}
		}
		return updateSuccessful;
	}

	/**
	 * Disable a derivative setting
	 * @param projectName project of the derivative setting
	 * @param settingName the name of the derivative setting. 
	 * @return true if successfully disabled.
	 */
	private static boolean disableSetting(String projectName, String settingName)
	{
		boolean updateSuccess=false;	
		boolean newEnabledSetting=false;

		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement toggleSetting=conn.prepareStatement(TOGGLE_SETTING);
				toggleSetting.setBoolean(1, newEnabledSetting);
				toggleSetting.setString(2, projectName);
				toggleSetting.setString(3, settingName);

				toggleSetting.executeUpdate();
				updateSuccess=true;

				toggleSetting.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				updateSuccess = false;
			}
		}
		String toggle="";
		if(newEnabledSetting)
			toggle="on";
		else
			toggle="off";

		//MetaDbHelper.note("Setting "+settingName+" setting for "+projectName+" to "+toggle+"...");

		return updateSuccess;
	}
	/**
	 * Update the brand text of a new setting
	 * @param projectName The project to update.
	 * @param brandText the brand text.
	 * @return true if the branding text was successfully updated, false otherwise.
	 */
	public static boolean updateBrand(String projectName, String brandText)
	{
		boolean updateSuccessful=false;
		Connection conn = Conn.initialize(); //Establish connection
		if(conn!=null)
		{
			try
			{
				PreparedStatement updateBrand=conn.prepareStatement(UPDATE_BRAND_TEXT);
				updateBrand.setString(1, brandText);
				updateBrand.setString(2, projectName);
				//MetaDbHelper.logEvent("DerivativeDAO", "updateBrand", "Brand text for "+projectName+" updated.");

				updateBrand.executeUpdate();
				updateSuccessful = true;

				updateBrand.close();
				conn.close();

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				updateSuccessful=false;
			}
		}
		return updateSuccessful;
	}
	
	/**
	 * Construct a derivative file path
	 * @param projectName project name.
	 * @param fileName The file name of the master file (not including the path)
	 * @param ext The desired extension
	 * @return The full path to the derivative, constructed from the given parameters and global vars.
	 */
	private static String getDerivFilePath(String projectName, String fileName, String ext)
	{
		String[] fileNameTokens=fileName.split("\\.");
		String name="";

		if(fileNameTokens.length<2)
			name=fileName+ext;
		
		else
			name=fileNameTokens[fileNameTokens.length-2]+ext;
		//MetaDbHelper.note("Constructed file path: "+name);
		return Global.DERIV_DIRECTORY+"/"+projectName+"/"+name;
	}
	
	/**
	 * Removes a derivative setting.
	 * @param projectName The project from which to remove the derivative setting.
	 * @param settingName The name of the derivative setting to remove.
	 * @return true if the setting was successfully removed, false otherwise.
	 */
	public static boolean removeDerivativeSetting(String projectName, String settingName)
	{
		return false;
	}
}
