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

import edu.lafayette.metadb.model.attributes.AdminDescAttribute;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.metadata.Metadata;

/**
 * Class to represent administrative/descriptive data for one item in a project.
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class AdminDescItem {

	private Metadata metadata;
	private AdminDescAttribute attr;
	private String vocab;
	private int index;
	
	
	public AdminDescItem(String projname, int itemNumber, int attrIndex, String element, String label, 
			 String data, String vocab, AdminDescAttribute attribute) {
		metadata = new Metadata(projname, itemNumber, element, label, data);
		attr = attribute;
		this.vocab = vocab;
		this.index = attrIndex;
		
	}
	
	public AdminDescItem(String projname, String mdType, int itemNumber, int attrIndex, String element, String label, 
						 String data, String vocab, boolean isLarge, boolean isReadableDate, boolean isSearchableDate, boolean isControlled, 
						 boolean isMultiple, boolean isAdditions, boolean isSorted) {
		metadata = new Metadata(projname, itemNumber, element, label, data);
		attr = new AdminDescAttribute(projname, element, label, mdType, isLarge, isReadableDate, isSearchableDate, isControlled, isMultiple, isAdditions, isSorted, attrIndex);
		this.vocab = vocab;
		this.index = attrIndex;
	}

	public Metadata getMetadata() {
		return metadata;
	}
	
	public String getElement() {
		return attr.getElement();
	}
	
	public String getLabel() {
		return attr.getLabel();
	}
	
	public int getItemNumber()
	{
		return metadata.getItemNumber();
	}
	
	public String getData()
	{
		if (!attr.isSorted())
			return metadata.getData();
		return MetaDbHelper.sortVocab(metadata.getData());
	}
	
	/**
	 * @return the isLarge
	 */
	public boolean isLarge() {
		return attr.isLarge();
	}

	/**
	 * @return the isControlled
	 */
	public boolean isControlled() {
		return attr.isControlled();
	}

	/**
	 * @return the isMultiple
	 */
	public boolean isMultiple() {
		return attr.isMultiple();
	}

	/**
	 * @return the isAdditions
	 */
	public boolean isAdditions() {
		return attr.isAdditions();
	}
	
	/**
	 * @return the isSorted
	 */
	public boolean isSorted() {
		return attr.isSorted();
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the vocab
	 */
	public String getVocab() {
		return vocab;
	}
	
	public boolean isReadableDate() {
		return attr.isReadableDate();
	}
	
	public boolean isSearchableDate() {
		return attr.isSearchableDate();
	}
	
	public int getID() {
		return attr.getId();
	}
}
