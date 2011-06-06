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
 * Class representing a generic attribute.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 February 2011
 */
public class Attribute implements AttributeInterface {
	
	protected String projectName;
	protected String element;
	protected String label;
	protected String mdType;
	
	public Attribute(String projname, String element, String label, String mdType) {
		this.projectName = projname;
		this.element = element;
		this.label = label;
		this.mdType = mdType;
	}
	
	public Attribute(String projname, String element, String label) {
		this.projectName = projname;
		this.element = element;
		this.label = label;
	}

	public String getMdType() {
		return mdType;
	}
	
	public String getElement() {
		return element;
	}

	public String getLabel() {
		return label;
	}

	public String getProjectName() {
		return projectName;
	}
	
	
}
