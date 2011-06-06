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
import java.util.Map;

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.metadata.AdminDescDataDAO;

/**
 * Class to retrieve and update administrative and descriptive metadata.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class AdminDescItemsDAO {

	/**
	 * Get all the attributes along with its properties for an item in a project based on item index
	 * @param projectName the project to be looked up
	 * @param type the type of data (administrative or descriptive)
	 * @param itemIndex the item number
	 * @return an ArrayList of AdminDescItem objects that have all the necessary information
	 */
	public static ArrayList<AdminDescItem> getItems(String projectName, String type, int itemIndex) {
		ArrayList<AdminDescItem> itemList = new ArrayList<AdminDescItem>();
		ArrayList<AdminDescAttribute> attrList = AdminDescAttributesDAO.getAdminDescAttributes(projectName, type);
		Map<String, String> dataMap = AdminDescDataDAO.getAdminDescDataMap(projectName, type, itemIndex);
		if (attrList != null && dataMap != null) {
			for (int i = 0; i < attrList.size(); i++) {
				AdminDescAttribute attribute = attrList.get(i);
				String element = attribute.getElement();
				String label = attribute.getLabel();
				String key = element+"-"+label;
				String vocab = AdminDescAttributesDAO.getControlledVocab(projectName, element, label);
				AdminDescItem item = new AdminDescItem(projectName, itemIndex, i, element, label, dataMap.get(key), vocab,
													   attribute);
				itemList.add(item);
			}
		}
		return itemList;
	}
	
	/**
	 * Get data for 1 specific attribute from an item in a project by name.
	 * @param projectName the project to be looked up
	 * @param type the type of metadata (administrative or descriptive)
	 * @param element the name of the element
	 * @param label the name of the label
	 * @param itemIndex the item number
	 * @return an AdminDescItem object containing the data for the item and project specific settings.
	 */
	public static AdminDescItem getItem(String projectName, String type, String element, String label, int itemIndex) {
		ArrayList<AdminDescAttribute> attrList = AdminDescAttributesDAO.getAdminDescAttributes(projectName, type);
		Map<String, String> dataMap = AdminDescDataDAO.getAdminDescDataMap(projectName, type, itemIndex);
		if (attrList != null && dataMap != null) {
			for (int i = 0; i < attrList.size(); i++) {
				AdminDescAttribute attribute = attrList.get(i);
				if (element.equals(attribute.getElement()) && label.equals(attribute.getLabel())) {
					String key = element+"-"+label;
					String vocab = AdminDescAttributesDAO.getControlledVocab(projectName, element, label);
					return new AdminDescItem(projectName, itemIndex, i, element, label, dataMap.get(key), vocab,
								   attribute);
				}
			}
		}
		return null;
	}

	/**
	 * Get data for 1 specific attribute from an item in a project by name.
	 * @param projectName the project to be looked up
	 * @param type the type of metadata (administrative or descriptive)
	 * @param id the unique ID of the attribute being looked up.
	 * @param itemIndex the item number
	 * @return an AdminDescItem object containing the data for the item and project specific settings.
	 */
	public static AdminDescItem getItem(String projectName, String type, int id, int itemIndex) {
		ArrayList<AdminDescAttribute> attrList = AdminDescAttributesDAO.getAdminDescAttributes(projectName, type);
		Map<String, String> dataMap = AdminDescDataDAO.getAdminDescDataMap(projectName, type, itemIndex);
		if (attrList != null && dataMap != null) {
			for (int i = 0; i < attrList.size(); i++) {
				AdminDescAttribute attribute = attrList.get(i);
				if (id == attribute.getId()) {
					String key = attribute.getElement()+"-"+attribute.getLabel();
					String vocab = AdminDescAttributesDAO.getControlledVocab(id);
					return new AdminDescItem(projectName, itemIndex, i, attribute.getElement(), attribute.getLabel(), dataMap.get(key), vocab,
								   attribute);
				}
			}
		}
		return null;
	}
}
