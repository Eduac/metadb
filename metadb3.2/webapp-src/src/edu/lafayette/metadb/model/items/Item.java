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

import java.util.ArrayList;

import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

/**
 * Class to represent one item in a project.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *  
 */
public class Item {

	private String projname;
	private int itemNumber;
	private String fileName;
	private String checksum;
	private String locker;
	private String lastModified;
	private String filePath;
	private String thumbFilePath;
	private ArrayList<AdminDescItem> adminData;
	private ArrayList<AdminDescItem> descData;
	
	public Item(String projname, int itemNumber, String fileName, String checksum, String locker, String lastModified) {
		this.projname = projname;
		this.itemNumber = itemNumber;
		this.fileName = fileName;
		this.checksum = checksum;
		this.locker = locker;
		this.lastModified=lastModified;
		adminData = new ArrayList<AdminDescItem>();
		descData = new ArrayList<AdminDescItem>();
	}


	/**
	 * @return the projname
	 */
	public String getProjname() {
		return projname;
	}


	/**
	 * @return the itemNumber
	 */
	public int getItemNumber() {
		return itemNumber;
	}


	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}


	/**
	 * @return the locker
	 */
	public String getLocker() {
		return locker;
	}
	
	public void setLocker(String username) {
		locker = username;
	}

	public String getLastModified()
	{
		return lastModified;
	}
	
	public void setLastModified(String newDate)
	{
		lastModified=newDate;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}


	/**
	 * @param thumbFilePath the thumbFilePath to set
	 */
	public void setThumbFilePath(String thumbFilePath) {
		this.thumbFilePath = thumbFilePath;
	}


	/**
	 * @return the thumbFilePath
	 */
	public String getThumbFilePath() {
		return thumbFilePath;
	}
	
	public void setData(ArrayList<AdminDescItem> attributes, String type) {
		if (type.equals(Global.MD_TYPE_ADMIN))
			adminData = attributes;
		else if (type.equals("descriptive"))
			descData = attributes;
	}
	
	public void addData(AdminDescItem attribute, String type) {
		if (type.equals(Global.MD_TYPE_ADMIN))
			adminData.add(attribute);
		else if (type.equals("descriptive"))
			descData.add(attribute);
	}
	
	public ArrayList<AdminDescItem> getData(String type) {
		if (type.equals(Global.MD_TYPE_ADMIN))
			return adminData;
		else if (type.equals("descriptive"))
			return descData;
		return null;
	}

	
	public ArrayList<AdminDescItem> search(ArrayList<String> tokens) {
		ArrayList<AdminDescItem> metadata = new ArrayList<AdminDescItem>();
		try {
			ArrayList<AdminDescItem> dataPool = new ArrayList<AdminDescItem>();
			dataPool.addAll(this.adminData);
			dataPool.addAll(this.descData);
			for (AdminDescItem data : dataPool) {
				boolean found = false;
				String searchData = data.getData().toLowerCase();
				for (String token : tokens) 
					if (searchData.indexOf(token) >= 0)
						found = true;
				if (found)
					metadata.add(data);
			}
			
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return metadata;
		
	}
}
