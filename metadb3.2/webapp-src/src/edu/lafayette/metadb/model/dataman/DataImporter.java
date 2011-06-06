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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.csvreader.CsvReader;

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.attributes.AdminDescAttributesDAO;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;
import edu.lafayette.metadb.model.metadata.AdminDescDataDAO;
import edu.lafayette.metadb.model.result.Result;

/**
 * Class handling data import.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class DataImporter {
	
	private static final String TSV = "tsv";
	private static final String CSV = "csv";
	
	/**
	 * Import a file to MetaDB.
	 * @param delimiter The delimiter to use when importing (currently commas and tabs)
	 * @param projectName The destination project.
	 * @param input InputStream containing the data to import.
	 * @param replaceEntity Flag indicating whether HTML entity codes should be unescaped.
	 * @return a Result containing the result of the import.
	 */
	public static Result importFile(String delimiter, String projectName, InputStream input, boolean replaceEntity) {
		
		try
		{
			CsvReader reader = null;
			if (delimiter.equals(TSV))
				reader = new CsvReader(new InputStreamReader(input), '\t');
			else if (delimiter.equals(CSV))
				reader = new CsvReader(new InputStreamReader(input), ',');
			//if(disableQualifier)
			//	reader.setUseTextQualifier(false);
			if (reader != null
					&& AdminDescAttributesDAO.deleteAllAttributes(projectName)) {
				if(!reader.readHeaders())
					return new Result(false);

				String[] header = reader.getHeaders();
				//for(String h: header)
					//MetaDbHelper.note("Header: "+h);
				Result res = importAttributes(projectName, header);
				MetaDbHelper.logEvent("DataImporter", "importFile",
						"Successfully imported all attributes to project "
								+ projectName);
				
				if(!reader.readRecord())
					return res;
				String[] data = reader.getValues();
				int index = 1;
				ArrayList<AdminDescAttribute> attrList = AdminDescAttributesDAO
						.getAdminDescAttributes(projectName, "descriptive");
				if (ItemsDAO.nextItemNumber(projectName) <= 0)
					return res;
				while (importData(projectName, data, attrList, index, replaceEntity)) {
					if(!reader.readRecord())
						return res;
					data = reader.getValues();
					index++;
				}
				return res;

			}
		} catch (FileNotFoundException e) {
			MetaDbHelper.logEvent(e);
		} catch (IOException e) {
			MetaDbHelper.logEvent(e);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return new Result(false);
	}
	
	/**
	 * Import a list of attribtues into a project.
	 * @param projectName The destination project.
	 * @param header The list of attributes, which are typically headers in an imported data file.
	 * @return a Result containing the result of the import.
	 */
	private static Result importAttributes(String projectName, String[] header) {
		Result res = new Result(false);
		if (header == null)
			return res;
		
		try {
			ArrayList<String> errorFields = new ArrayList<String>();
			for (String attribute: header) {
				String [] pair = attribute.split("\\.", 2);
				String element = pair[0].replace(" ", "_");
				String label = "";
				if (pair.length > 1)
					label = pair[1].replace(" ", "_");
				if (!AdminDescAttributesDAO.createAdminDescAttribute(projectName, element, label, "descriptive", 
						false, false, false, false, false, false, false, false)) {
					String field = element + (pair.length > 1 ? "" : "."+label);
					errorFields.add(field);
					if (!AdminDescAttributesDAO.createAdminDescAttribute(projectName, "description", field, "descriptive", 
						false, false, false, false, false, false, false, true))
						throw new Exception("Cannot import header: " + projectName + " description " + field);
				}
			}
			if (!errorFields.isEmpty())
				res.setData(errorFields);
			else
				res.setResult(true);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
			res.setResult(false);
		}
		return res;
	}
	
	/**
	 * Import a list of data into a project for one item.
	 * @param projectName The destination project.
	 * @param data Array of data strings.
	 * @param attrList A list of attributes associated with the data. The entries in the data array should be ordered in the same way as this list.
	 * @param itemNumber The item number which is being updated.
	 * @param replaceEntity Flag indicating whether HTML entity codes should be unescaped on import.
	 * @return true if the import completed, false otherwise.
	 */
	private static boolean importData(String projectName, String[] data, ArrayList<AdminDescAttribute> attrList, int itemNumber, boolean replaceEntity) {
		if (data == null) 
			return false;
		
		try {
			for (int i = 0; i< attrList.size(); i++) {
				AdminDescAttribute attribute = attrList.get(i);
				String filteredData = (replaceEntity ? StringEscapeUtils.unescapeHtml(data[i]) : data[i]);
				if (!AdminDescDataDAO.updateAdminDescData(projectName, itemNumber, attribute.getElement(), attribute.getLabel(), filteredData)) 
					throw new Exception("Cannot update data: "+projectName+" "+ 
							itemNumber+ " " + attribute.getElement() + " " + attribute.getLabel() + " " + filteredData);
				
			}
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
			return false;
		}
		
		return true;
		
	}
	
}
