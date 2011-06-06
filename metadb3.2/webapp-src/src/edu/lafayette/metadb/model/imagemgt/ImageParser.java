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

import java.util.*;
import java.io.*; //Sanselan libraries

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;

import edu.lafayette.metadb.model.commonops.*;
import edu.lafayette.metadb.model.fileio.FileManager;
import edu.lafayette.metadb.model.metadata.AutoTechData;
import edu.lafayette.metadb.model.syslog.SysLogDAO;

/**
 * Class to handle extraction of technical metadata from master files.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */

public class ImageParser {
	public ImageParser() {

	}

	/**
	 * Get a list of Technical Metadata from an input image.
	 * 
	 * @param projectName
	 *            The project this data will get added to
	 * @param filePath
	 *            The originating source image file.
	 * @return an AutoTechData object containing the retrieved metadata.
	 */
	public static AutoTechData parseImage(String projectName,
			String filePath, int itemNumber) {
		try {
			//MetaDbHelper.note("Parsing image for project: " + projectName);
			//MetaDbHelper.note("Sanselan: Trying to process " + filePath + "...");
			HashMap<String, String> parsedData = getImageData(
					filePath	);
			if (parsedData == null) {
				SysLogDAO.log("MetaDB", Global.SYSLOG_ERROR, "Error: Failed to parse "+filePath+" for "+projectName);
				throw new Exception("Parse error!");
			}

			else {
				//MetaDbHelper.note("Image metadata retrieved");

				AutoTechData testData=new AutoTechData(projectName, itemNumber, parsedData);
				return testData;
			}
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return null;
	}

	/**	
	 * Gets technical image data from a filepath.
	 * @param filePath The file to parse
	 * @return a HashMap from tech field->value for this image file.
	 */
	private static HashMap<String, String> getImageData(String filePath) {
		try {
			HashMap<String, String> metadataMap = new HashMap<String, String>();
			File image=new File(filePath);
			ImageInfo info=Sanselan.getImageInfo(image);

			metadataMap.put("FileName", image.getName());
			metadataMap.put("DPI", Integer.toString(info.getPhysicalHeightDpi()));
			metadataMap.put("BitDepth", Integer.toString(info.getBitsPerPixel()));
			metadataMap.put("DateModified", FileManager.computeLastModified(image.getAbsolutePath()));
			metadataMap.put("FileFormat", info.getFormatName());
			metadataMap.put("Checksum", FileManager.computeChecksum(image.getAbsolutePath()));
			metadataMap.put("PixelHeight", Integer.toString(info.getHeight()));
			metadataMap.put("PixelWidth", Integer.toString(info.getWidth()));			
			metadataMap.put("PhysicalHeight", Float.toString(info.getPhysicalHeightInch()));
			metadataMap.put("PhysicalWidth", Float.toString(info.getPhysicalWidthInch()));

			return metadataMap;

		} 	
		catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return null;
	}
}
