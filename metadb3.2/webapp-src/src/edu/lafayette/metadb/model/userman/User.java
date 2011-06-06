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
package edu.lafayette.metadb.model.userman;

/**
 * Class representing a single MetaDB user.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
public class User {
	private String userName;
	private String password;
	private String type;
	private String authType;
	private long last_login;
	private String lastProject;
	
	
	public User(String user_name, String pass, String permissionType, String authType, 
			long loginTime, String lastAccess) {
		setUserName(user_name);
		setPassword(pass);
		setType(permissionType);
		setAuthType(authType);
		setLast_login(loginTime);
		setLastProj(lastAccess);
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	
	/**
	 *  @param type new auth type.
	 */
	public void setAuthType(String type) {
		this.authType = type;
	}
	
	/**
	 * @return the user's authentication type.
	 */
	public String getAuthType() {
		return authType;
	}
	
	/**
	 * @param last_login the last_login to set
	 */
	public void setLast_login(long last_login) {
		this.last_login = last_login;
	}

	/**
	 * @return the last_login
	 */
	public long getLast_login() {
		return last_login;
	}
	
	public void setLastProj(String lastProj)
	{
		this.lastProject=lastProj;
	}

	public String getLastProj()
	{
		return this.lastProject;
	}
	
}
