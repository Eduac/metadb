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
package edu.lafayette.metadb.model.elements;

/**
 * This class represents an element of some 
 * specified metadata format that is present in the MetaDB system. 
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 */
public class Element
{
	
	private String element;
	private String format;
	
	public Element(String element, String format)
	{
		this.element=element;
		this.format=format;
	}

	public String getElement() {
		return element;
	}

	public String getFormat() {
		return format;
	}

}
