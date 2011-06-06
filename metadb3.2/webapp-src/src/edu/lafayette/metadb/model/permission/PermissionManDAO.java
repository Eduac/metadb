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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import edu.lafayette.metadb.model.commonops.Conn;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Class to handle retrieval and upate of project permissions.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0
 * 
 */
public class PermissionManDAO {


	private static final String CREATE_PERMISSION=

		"INSERT INTO "+Global.PERMISSIONS_TABLE+
		"("+
		Global.PROJECT_NAME+","+Global.USER_NAME+","+
		Global.PERMISSIONS_DATA+","+
		Global.PERMISSIONS_ADMIN_MD+","+Global.PERMISSIONS_DESC_MD+","+
		Global.PERMISSIONS_TABLE_EDIT+","+Global.PERMISSIONS_VOCAB+
		")"+
		" VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String DELETE_PROJECT_PERMISSION_BY_USER=

		"DELETE FROM "+Global.PERMISSIONS_TABLE+" "+
		"WHERE "+Global.USER_NAME+"=? " +
		"AND "+Global.PROJECT_NAME+"=?";

	private static final String GET_EACH_PROJECT_PERMISSION_BY_USER=
		"SELECT * "+
		"FROM "+Global.PERMISSIONS_TABLE+" "+
		"WHERE "+Global.USER_NAME+"=? " +
		"AND "+Global.PROJECT_NAME+"=?";

	private static final String GET_PROJECT_PERMISSION_BY_USER = 
		"SELECT * "+
		"FROM "+Global.PERMISSIONS_TABLE+" "+
		"WHERE "+Global.USER_NAME+"=?";

	private static final String UPDATE_PERMISSION=
		"UPDATE "+Global.PERMISSIONS_TABLE+" "+
		"SET "+Global.PERMISSIONS_DATA+"=?, "+
		Global.PERMISSIONS_ADMIN_MD+"=?, "+
		Global.PERMISSIONS_DESC_MD+"=?, "+" "+
		Global.PERMISSIONS_TABLE_EDIT+"=?, "+" "+
		Global.PERMISSIONS_VOCAB+"=?"+
		"WHERE "+Global.USER_NAME+"=?"+" "+
		"AND "+Global.PROJECT_NAME+"=?";


	/**
	 * Creates a new permission setting for a user.
	 * @param projectName the project to create the permission for.
	 * @param userName The user that will be given the permission.
	 * @param data User's data privilege. 
	 * @param admin_md User's administrative data privilege.
	 * @param desc_md User's descriptive data privilege.
	 * @return true if the permission was successfully created, false otherwise. 
	 */
	public static boolean createPermission(String projectName, String userName, String data, 
			String admin_md, String desc_md, String table_edit, String controlled_vocab)
	{
		Connection conn = Conn.initialize(); 
		if(conn!=null)
		{	
			try
			{	
				PreparedStatement createPermission = conn.prepareStatement(CREATE_PERMISSION); 

				createPermission.setString(1, projectName); 
				createPermission.setString(2, userName);
				createPermission.setString(3, data);
				createPermission.setString(4, admin_md);
				createPermission.setString(5, desc_md);
				createPermission.setString(6, table_edit);
				createPermission.setString(7, controlled_vocab);

				createPermission.executeUpdate();
				createPermission.close();
				conn.close();
				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Delete a user's permission.
	 * @param projectName The project name for which to remove the permission. 
	 * @param userName The user name to remove permissions from.
	 * @return true if the permission was successfully removed, false otherwise.
	 */
	public static boolean deletePermission(String projectName, String userName)
	{
		Connection conn = Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement deletePermission=conn.prepareStatement(DELETE_PROJECT_PERMISSION_BY_USER);

				deletePermission.setString(1, userName);
				deletePermission.setString(2, projectName);

				deletePermission.executeUpdate();

				deletePermission.close();
				conn.close();

				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
		
	}

	
	/**
	 * Get a user's permission
	 * @param userName the username to retrieve permissions for.
	 * @param projectName the project to retrieve user permissions for.
	 * @return a Permission object representing the user's permission for the project denoted by projectName.
	 */ 
	public static Permission getUserPermission(String userName, String projectName)
	{
		
		Permission requestedPermission = null;
		if (UserManDAO.getUserData(userName).getType().equals("admin"))
			return new Permission(projectName, userName, "import_export", "read_write", "read_write", "allow", "allow");
		Connection conn = Conn.initialize(); 
		if(conn!=null)
		{
			try
			{
				PreparedStatement getPermissionQuery = conn.prepareStatement(GET_EACH_PROJECT_PERMISSION_BY_USER);

				getPermissionQuery.setString(1, userName);
				getPermissionQuery.setString(2, projectName);
				ResultSet userPermission = getPermissionQuery.executeQuery();

			if (userPermission.next()) {
						//Get parameters 
						String data = userPermission.getString(Global.PERMISSIONS_DATA);
						String admin_md = userPermission.getString(Global.PERMISSIONS_ADMIN_MD);
						String desc_md = userPermission.getString(Global.PERMISSIONS_DESC_MD);
						String table_edit = userPermission.getString(Global.PERMISSIONS_TABLE_EDIT);
						String controlled_vocab = userPermission.getString(Global.PERMISSIONS_VOCAB);
						requestedPermission = new Permission( projectName, userName, data, admin_md, desc_md,
																table_edit, controlled_vocab);
						//MetaDbHelper.note("Permission retrieved: "+requestedPermission.toString());
					}
				
				getPermissionQuery.close();
				userPermission.close();

				conn.close();

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return requestedPermission;
	}

	/**
	 * Get all the avilable permissions for one user.
	 * @param userName the user name to retrieve all permissions for.
	 * @return an ArrayList of Permissions representing all of the permissions this user has.
	 */
	public static ArrayList<Permission> getAllProjectPermission(String userName)
	{
		ArrayList<Permission> requestedPermission = new ArrayList<Permission>();
		//MetaDbHelper.note("Trying to get permissions for "+userName);
		Connection conn = Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement getPermissionQuery = conn.prepareStatement(GET_PROJECT_PERMISSION_BY_USER);

				getPermissionQuery.setString(1, userName);

				ResultSet userPermission = getPermissionQuery.executeQuery();

					while (userPermission.next()) {
						String projectName = userPermission.getString(Global.PROJECT_NAME);
						String data = userPermission.getString(Global.PERMISSIONS_DATA);
						String admin_md = userPermission.getString(Global.PERMISSIONS_ADMIN_MD);
						String desc_md = userPermission.getString(Global.PERMISSIONS_DESC_MD);
						String table_edit = userPermission.getString(Global.PERMISSIONS_TABLE_EDIT);
						String controlled_vocab = userPermission.getString(Global.PERMISSIONS_VOCAB);
						
						Permission permission = new Permission( projectName, userName, data, admin_md, desc_md,
																table_edit, controlled_vocab);
						//MetaDbHelper.note("Permission retrieved: "+permission.toString());
						requestedPermission.add(permission);
					}				
				getPermissionQuery.close();
				userPermission.close();


				conn.close();

			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return requestedPermission;
	}

	/**
	 * Updates a permission setting for one user within a project.
	 * @param username The username to update permissions for.
	 * @param projectName The project to update permissions for.
	 * @param data The permission field to update.
	 * @param admin_md Flag indicating the level of administrative metadata access the user will have.
	 * @param desc_md Flag indicating the level of descriptive metadata access the user will have.
	 * @param table_edit Flag indicating whether the user will be able to access the table edit view.
	 * @param controlled_vocab A flag indicating whether the user will be able to access the controlled vocabulary manager.
	 * @return true if successfully updated, false otherwise.
	 */
	public static boolean updatePermission(String username, String projectName, String data, String admin_md, String desc_md,
											String table_edit, String controlled_vocab) {
		Connection conn = Conn.initialize();
		if(conn!=null)
		{	
			try
			{	
				PreparedStatement updatePermission = conn.prepareStatement(UPDATE_PERMISSION); 

				updatePermission.setString(1, data);
				updatePermission.setString(2, admin_md);
				updatePermission.setString(3, desc_md);
				updatePermission.setString(4, table_edit);
				updatePermission.setString(5, controlled_vocab);
				updatePermission.setString(6, username);
				updatePermission.setString(7, projectName);
				
				updatePermission.executeUpdate();
				
				updatePermission.close();
				conn.close();
				return true;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;

	}

}
