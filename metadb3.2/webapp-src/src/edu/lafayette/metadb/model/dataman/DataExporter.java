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
package edu.lafayette.metadb.model.dataman;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.attributes.Attribute;
import edu.lafayette.metadb.model.attributes.TechAttributesDAO;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;

import edu.lafayette.metadb.model.items.AdminDescItem;
import edu.lafayette.metadb.model.items.Item;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.Metadata;

/**
 * Class which handles data export for the servlet.
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0
 */
 
@SuppressWarnings("unchecked")
public class DataExporter 
{	
	/**
	 * Export data from a project for one item. 
	 * @param projectName The project name.
	 * @param itemNumber The item number to export.
	 * @param delimiter The delimiter to use when writing the export (currently TSV/CSV)
	 * @param encoder The character set encoding.
	 * @param technical Flag indicating whether to include technical data in the export file.
	 * @param replaceEntity Flag indicating whether to escape special characters with their HTML entity codes.
	 * @return a String[] representing one row in the exported data file.
	 */
	public static String[] exportData(String projectName, int itemNumber, char delimiter, String encoder, boolean technical, boolean replaceEntity) {
		ArrayList adminDescData = null;
		Item it = ItemsDAO.getItem(projectName, itemNumber);
		adminDescData = it.getData(Global.MD_TYPE_DESC);
		adminDescData.addAll(it.getData(Global.MD_TYPE_ADMIN));
		
		ArrayList techData = new ArrayList();
		if (technical)
			techData = ItemsDAO.getTechData(projectName, itemNumber);
		
		String[] out = new String[adminDescData.size() + techData.size()];
		for (int i = 0; i< adminDescData.size(); i++) {
			try {
				String outStr = StringUtils.trimToEmpty(new String(((AdminDescItem)adminDescData.get(i)).getData().getBytes("UTF-8"), encoder).replace('\t', ' '));
				out[i] = 
					(replaceEntity  ? StringEscapeUtils.escapeHtml(outStr)//outStr.replaceAll("&", "&#38;").replaceAll("[\"]", "&#34;").replaceAll("%", "&#37;").replaceAll("'", "&#39;").replaceAll(",", "&#44;")
									: outStr
					);
			} catch (Exception e) {
				MetaDbHelper.logEvent(e);
				out[i] = ((AdminDescItem)adminDescData.get(i)).getData();
			}
		}
		//MetaDbHelper.note("Exporting tech data");
		for (int i = 0; i< techData.size(); i++)
			try {
				out[i+adminDescData.size()] = StringUtils.trimToEmpty(new String(((Metadata)techData.get(i)).getData().getBytes("UTF-8"), encoder).replace('\t', ' '));
			} catch (Exception e) {
				MetaDbHelper.logEvent(e);
				out[i + adminDescData.size()] = ((Metadata)techData.get(i)).getData();
			}
			
			
		return out;
	}
	
	/**
	 * Export a project's attributes.
	 * @param projectName The project name.
	 * @param delimiter The delimiter to use. 
	 * @param encoder The character encoding to use.
	 * @param technical A flag indicating whether to include technical attributes.
	 * @return A String[] representing headers for the export file.
	 */
	public static String[] exportAttributes(String projectName, char delimiter, String encoder, boolean technical) {
		ArrayList attrList = null;
		attrList = AdminDescAttributesDAO.getAdminDescAttributes(projectName, Global.MD_TYPE_DESC);
		attrList.addAll(AdminDescAttributesDAO.getAdminDescAttributes(projectName, Global.MD_TYPE_ADMIN));
		if (technical)
			attrList.addAll(TechAttributesDAO.getTechAttributes(projectName));
		
		String[] out = new String[attrList.size()];
		for (int i = 0; i< out.length; i++)
			try {
				String label = ((Attribute) attrList.get(i)).getLabel();
				out[i] = StringUtils.trimToEmpty(new String((((Attribute)attrList.get(i)).getElement()+
									(label.equals("") ? "" : "."+label)).getBytes("UTF-8"), encoder).replace('\t', ' '));
			} catch (UnsupportedEncodingException e) {
				MetaDbHelper.logEvent(e);
				out[i] = ((Attribute) attrList.get(i)).getElement()+"."+((Attribute) attrList.get(i)).getLabel();
			}
		return out;
	}
	
	
}