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
package edu.lafayette.metadb.model.permission;

/**
 * Class representing a single permission for a user.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
@SuppressWarnings("rawtypes")
public class Permission implements Comparable {
	
	private String projectName;
	private String userName;
	private String data;
	private String admin_md;
	private String desc_md;
	private String table_edit;
	private String controlled_vocab;
	
	public Permission(String projectName, String userName, String data, String admin_md, String desc_md,
					  String table_edit, String controlled_vocab) {
		this.projectName = projectName;
		this.userName = userName;
		this.data = data;
		this.admin_md = admin_md;
		this.desc_md = desc_md;
		this.table_edit = table_edit;
		this.controlled_vocab = controlled_vocab;
		
	}

	public String getProjectName() {
		return projectName;
	}


	public String getDesc_md() {
		return desc_md;
	}

	public String getData() {
		return data;
	}

	public String getAdmin_md() {
		return admin_md;
	}
	
	public String getTable_edit() {
		return table_edit;
	}
	
	public String getControlled_vocab() {
		return controlled_vocab;
	}

	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String toString() {
		String permission = "projname="+projectName+";";
		permission += "username="+userName+";";
		permission += "data="+data+";";
		permission += "admin_md="+admin_md+";";
		permission += "desc_md="+desc_md+";";
		permission += "table_edit="+table_edit+";";
		permission += "controlled_vocab="+controlled_vocab+";";
		return permission;
	}

	public int compareTo(Object o) {
		return projectName.compareTo(((Permission)o).getProjectName());
	}
	
	
}
