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
package edu.lafayette.metadb.model.attributes;

/**
 * Class representing administrative/descriptive attributes.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 February 2011
 */
public class AdminDescAttribute extends Attribute {	
	private boolean isLarge = false; 
	private boolean isReadableDate=false;
	private boolean isSearchableDate=false;
	private boolean isControlled = false;
	private boolean isMultiple = false;
	private boolean isAdditions = false;
	private boolean isSorted = false;
	private int rowIndex;
	private boolean error = false;
	private int id = -1;
	
	/**
	 * Constructor for attribute. ID will automatically be created.
	 * @param projectName name of project associated
	 * @param element Element value
	 * @param label Label value
	 * @param mdType Administrative or descriptive.
	 * @param isLarge Large text box in display
	 * @param isReadableDate Readable date boolean
	 * @param isSearchableDate Searchable date boolean
	 * @param isControlled Controlled vocabulary tied or not
	 * @param isMultiple Multiple values in field allowed or not
	 * @param isAdditions New values in field allowed or not
	 * @param isSorted In case of multiple controlled vocab, sort the values alphabetically?
	 * @param rowIndex The row index for sorting in the UI.
	 */
	public AdminDescAttribute(String projectName, String element, String label,  String mdType, 
			boolean isLarge, boolean isReadableDate, boolean isSearchableDate, 
			boolean isControlled, boolean isMultiple, boolean isAdditions, 
			boolean isSorted, int rowIndex)
	{
		super(projectName, element, label, mdType);
		this.rowIndex=rowIndex;
		this.isLarge=isLarge;
		this.isReadableDate=isReadableDate;
		this.isSearchableDate=isSearchableDate;
		this.isControlled=isControlled;
		this.isMultiple=isMultiple;
		this.isAdditions=isAdditions;
		this.isSorted=isSorted;
	}
	
	
	/**
	 * Same constructor, except this one also has an error attribute available for specification.
	 * Used in import to indicate badly formatted fields.
	 * @param projectName
	 * @param element
	 * @param label
	 * @param mdType
	 * @param isLarge
	 * @param isReadableDate
	 * @param isSearchableDate
	 * @param isControlled
	 * @param isMultiple
	 * @param isAdditions
	 * @param isSorted
	 * @param rowIndex
	 * @param error
	 * @param id
	 */
	public AdminDescAttribute(String projectName, String element, String label,  String mdType, 
			boolean isLarge, boolean isReadableDate, boolean isSearchableDate, 
			boolean isControlled, boolean isMultiple, boolean isAdditions, boolean isSorted,
			int rowIndex, boolean error, int id)
	{
		super(projectName, element, label, mdType);
		this.rowIndex=rowIndex;
		this.isLarge=isLarge;
		this.isReadableDate=isReadableDate;
		this.isSearchableDate=isSearchableDate;
		this.isControlled=isControlled;
		this.isMultiple=isMultiple;
		this.isAdditions=isAdditions;
		this.isSorted=isSorted;
		this.error = error;
		this.id=id;
	}

	public boolean isSorted() {
		return isSorted;
	}
	public boolean isAdditions() {
		return isAdditions;
	}

	public boolean isControlled() {
		return isControlled;
	}

	public boolean isLarge() {
		return isLarge;
	}

	public boolean isMultiple() {
		return isMultiple;
	}

	public int getRowIndex()
	{
		return rowIndex;
	}
	
	public void setError(boolean error) {
		this.error = error;
	}
	
	public boolean isError() {
		return error;
	}
	
	public int getId()
	{
		return id;
	}

	public boolean isReadableDate() {
		return isReadableDate;
	}

	public boolean isSearchableDate() {
		return isSearchableDate;
	}
}
