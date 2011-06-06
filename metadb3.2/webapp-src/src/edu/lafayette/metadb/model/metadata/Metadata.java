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
package edu.lafayette.metadb.model.metadata;

import edu.lafayette.metadb.model.attributes.Attribute;

/**
 * Class to represent generic metadata with element, label, and data values.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
public class Metadata extends Attribute implements MetadataInterface{
	
	protected int itemNumber;
	protected String data = "";
	
	public Metadata(String projectName, int itemNumber, String element, String label, String data)
	{
		super(projectName, element, label);
		this.itemNumber=itemNumber;
		this.data = data == null ? "" : data;
	}
	

	public int getItemNumber()
	{
		return itemNumber;
	}
	
	public String getData()
	{
		return data;
	}
	
}
