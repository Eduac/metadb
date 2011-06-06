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

import edu.lafayette.metadb.model.commonops.*;

import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

/**
 * Class to handle user authentication, creation, deletion, and updating.
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 *
 */
public class UserManDAO 
{
	private static final String CREATE_USER =

		"INSERT INTO " + Global.USERS_TABLE + "(" + Global.USER_NAME + ","
		+ Global.USER_PASSWORD + "," + Global.USER_TYPE + "," +
		Global.USER_AUTH_TYPE+","+ Global.USER_LAST_LOGIN+")" +

		" VALUES (?, ?, ?, ?, ?)";

	private static final String DELETE_USER =

		"DELETE FROM " + Global.USERS_TABLE + " " + "WHERE " + Global.USER_NAME
		+ "=?";

	private static final String GET_USER_DATA =

		"SELECT " + Global.USER_NAME + ","
		+ Global.USER_PASSWORD + "," + Global.USER_TYPE + ","
		+ Global.USER_AUTH_TYPE + "," + Global.USER_LAST_LOGIN+","+Global.USER_LAST_ACCESS+" "+

		"FROM " + Global.USERS_TABLE + " " + "WHERE "
		+ Global.USER_NAME + "=?";

	private static final String GET_USER_LIST = 

		"SELECT " + Global.USER_NAME + ","
		+ Global.USER_PASSWORD + "," + Global.USER_TYPE + ","
		+ Global.USER_AUTH_TYPE+ "," + Global.USER_LAST_LOGIN+" "+

		"FROM "+ Global.USERS_TABLE+" "+
		"ORDER BY "+Global.USER_NAME;

	private static final String UPDATE_LAST_PROJECT=

		"UPDATE "+Global.USERS_TABLE+" "+
		"SET "+Global.USER_LAST_ACCESS+"=? "+
		"WHERE "+Global.USER_NAME+"=?";

	private static final String UPDATE_LAST_LOGIN=

		"UPDATE "+Global.USERS_TABLE+" "+
		"SET "+Global.USER_LAST_LOGIN+"=? "+
		"WHERE "+Global.USER_NAME+"=?";
	
	private static final String UPDATE_USER_PASSWORD = 
		"UPDATE "+Global.USERS_TABLE+" "+
		"SET "+Global.USER_PASSWORD+"=? "+
		"WHERE "+Global.USER_NAME+"=?";
		
	private static final String UPDATE_AUTH_TYPE = 
		"UPDATE "+Global.USERS_TABLE+" "+
		"SET "+Global.USER_AUTH_TYPE+"=? "+
		"WHERE "+Global.USER_NAME+"=?";
	
	private static final String UPDATE_USER_TYPE = 
		"UPDATE "+Global.USERS_TABLE+" "+
		"SET "+Global.USER_TYPE+"=? "+
		"WHERE "+Global.USER_NAME+"=?";

	public UserManDAO() {
	}

	/**
	 * Authenticate a login and password for a user.
	 * @param userName the user name.
	 * @param password the password to validate
	 * @return true if the password validation succeeds, false otherwise.
	 */
	public static boolean checkPassword(String userName, String password)
	{
		if(userName.equals("") || password.equals("") || userName==null || password==null)
			return false;
		try {
			User user = getUserData(userName);
			if(user!=null)
				if(user.getAuthType().equals("LDAP"))
					return authLDAP(userName, password);
				else
				{
					return user.getPassword().equals(encryptPassword(password));				
				}
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return false;

	}

	/**
	 * Create a new user
	 * 
	 * @param userName The username for the new user.
	 * @param password The password for the new user.
	 * @param type The type for the new user. ("admin" or "worker")
	 * @return true if the user is added successfully, false otherwise
	 */
	public static boolean createUser(String userName, String password, String type, String authType)
	{
		if (MetaDbHelper.userExists(userName))  //duplicate user
			return false;

		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) {
			try 
			{
				PreparedStatement createUser = conn.prepareStatement(CREATE_USER);

				createUser.setString(1, userName);
				createUser.setString(2, encryptPassword(password));
				createUser.setString(3, type);
				createUser.setString(4, authType);
				createUser.setLong(5, 0);

				createUser.executeUpdate(); 

				createUser.close();
				conn.close();
				return true;

			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return false;
	}

	/**
	 * Delete a user
	 * 
	 * @param userName The username of the user to delete.
	 * @return true if the user was deleted successfully, false otherwise
	 */
	public static boolean deleteUser(String userName)
	{
		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) {
			try 
			{	
				PreparedStatement deleteUser = conn.prepareStatement(DELETE_USER);

				deleteUser.setString(1, userName); // Set parameters
				deleteUser.executeUpdate();

				deleteUser.close();
				conn.close(); // Close statement and connection

				return true;
			}

			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);
			}
		} 
		return false;
	}

	/**
	 * Returns an encrypted password given an unencrypted String.
	 * @param password the plain text string to encrypt.
	 * @return an encrypted String given the unencrypted String.
	 */
	private static String encryptPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(password.getBytes());
			byte raw[] = md.digest();
			StringBuffer sb = new StringBuffer(raw.length * 2);
			for (int i = 0; i < raw.length; i++){
				int v = raw[i] & 0xff;
				if(v < 16)
					sb.append('0');
				sb.append(Integer.toHexString(v));
			}
			String hash = sb.toString();
			return hash;
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return "";

	}

	/**
	 * Get a user's data.
	 * 
	 * @param userName The username of the desired user.	 
	 * @return a User object with username, password and type for the requested user.
	 * 
	 */
	public static User getUserData(String userName)
	{
		User requestedUser = null; // initialize return object
		
		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) {
			try 
			{
				if (!MetaDbHelper.userExists(userName)) // If user doesn't exist
					return new User("", "", "", "", 0, ""); //Return a dummy user
				PreparedStatement getUserQuery = conn.prepareStatement(GET_USER_DATA);

				getUserQuery.setString(1, userName); // set parameter
				ResultSet userData = getUserQuery.executeQuery();

				if (userData != null) // The query result was not null
				{
					userData.next();
					// Get parameters
					String password = userData.getString(Global.USER_PASSWORD);
					String type = userData.getString(Global.USER_TYPE);
					String authType = userData.getString(Global.USER_AUTH_TYPE);
					long last_login = userData.getLong(Global.USER_LAST_LOGIN);
					String lastProj=userData.getString(Global.USER_LAST_ACCESS);
					requestedUser = new User(userName, password, type,
							authType, last_login, lastProj);

				} 

				getUserQuery.close();
				userData.close();

				conn.close();
			}
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);
			}
		} 
		return requestedUser;
	}

	/**
	 * Update the password of a user.
	 * 
	 * @param userName The username of the user to update.
	 * @param newPassword The new password for the user to update.
	 * @return true if the user's password is updated successfully, false otherwise
	 */
	public static boolean updatePassword(String userName, String newPassword)
	{
		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) 
		{
			try 
			{
				PreparedStatement updateUser = conn.prepareStatement(UPDATE_USER_PASSWORD);
				newPassword = encryptPassword(newPassword);

				updateUser.setString(1, newPassword);
				updateUser.setString(2, userName);
				updateUser.executeUpdate();

				updateUser.close();
				conn.close(); // Close statement and connection

				return true;

			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);

			}
		} 
		return false;

	}
	
	/**
	 * Update the user type of a user.
	 * 
	 * @param userName The username of the user to update.
	 * @param newUserType The new user type for the user to update.
	 * @return true if the user's user type is updated successfully, false otherwise
	 */
	public static boolean updateUserType(String userName, String newUserType)
	{
		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) 
		{
			try 
			{
				PreparedStatement updateUser = conn.prepareStatement(UPDATE_USER_TYPE);
				
				updateUser.setString(1, newUserType);
				updateUser.setString(2, userName);
				updateUser.executeUpdate();

				updateUser.close();
				conn.close(); // Close statement and connection

				return true;

			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);

			}
		} 
		return false;
	}
	

	/**
	 * Update the authentication type of a user.
	 * 
	 * @param userName The username of the user to update.
	 * @param newAuthType The new authentication type for the user to update.
	 * @return true if the user's authentication type is updated successfully, false otherwise
	 */
	public static boolean updateAuthType(String userName, String newAuthType)
	{
		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) 
		{
			try 
			{
				PreparedStatement updateUser = conn.prepareStatement(UPDATE_AUTH_TYPE);
				
				updateUser.setString(1, newAuthType);
				updateUser.setString(2, userName);
				updateUser.executeUpdate();

				updateUser.close();
				conn.close(); // Close statement and connection

				return true;

			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);

			}
		} 
		return false;
	}
	
	/**
	 * Update the login time of a user.
	 * @param userName the user name to update.
	 * @param value the value of the login time, as a long integer.
	 * @return true if the user's login time was updated successfully, false otherwise.
	 */
	public static boolean updateLoginTime(String userName, long value)
	{
		if (!MetaDbHelper.userExists(userName))
			return false;

		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) 
		{
			try 
			{
				PreparedStatement updateUser = conn.prepareStatement(UPDATE_LAST_LOGIN);

				updateUser.setLong(1, value);
				updateUser.setString(2, userName);
				updateUser.executeUpdate();

				updateUser.close();
				conn.close(); // Close statement and connection

				return true;

			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);

			}
		} 
		return false;

	}

	/**
	 * Update the "last accessed" information of a user.
	 * @param userName the user name to update.
	 * @param accessData semicolon-delimited string of values.
	 * @return true if the last access information for the user was updated successfully, false otherwise.
	 */
	public static boolean updateLastProject(String userName, String accessData)
	{
		if (!MetaDbHelper.userExists(userName))
			return false;

		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null)
		{
			try 
			{	
				PreparedStatement updateUser = conn.prepareStatement(UPDATE_LAST_PROJECT);

				updateUser.setString(1, accessData);
				updateUser.setString(2, userName);
				updateUser.executeUpdate();

				updateUser.close();
				conn.close(); // Close statement and connection

				return true;

			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);

			}
		} 
		return false;

	}
	/**
	 * Get the last login time of a user.
	 * @param username the username to get the last login time of.
	 * @return the last login time of the user, as a long integer.
	 */
	public static long getLastLogin(String username) {
		try 
		{
			User user = getUserData(username);
			return user.getLast_login();
		} 
		catch (Exception e) 
		{
			MetaDbHelper.logEvent(e);
			return 0;
		}
	}

	public static String getLastProj(String username)
	{
		try
		{
			User user=getUserData(username);
			String lastProj = user.getLastProj(); 
			return lastProj == null ? "" : lastProj;
		}
		catch (Exception e)
		{
			MetaDbHelper.logEvent(e);
			return "";
		}
	}


	/**
	 * Get a list of all the users in the system.
	 * @return an Arraylist of Users representing all the users in the system.
	 */
	public static ArrayList<User> getUserList()
	{
		ArrayList<User> list = new ArrayList<User>();

		Connection conn = Conn.initialize(); // Establish connection
		if (conn != null) 
		{
			try 
			{
				PreparedStatement getUserList = conn.prepareStatement(GET_USER_LIST);
				ResultSet userData = getUserList.executeQuery();

				while (userData.next()) {
					String username = userData.getString(Global.USER_NAME);
					list.add(getUserData(username));
				}
				userData.close();

				getUserList.close();
				conn.close();
			} 
			catch (Exception e) 
			{
				MetaDbHelper.logEvent(e);
			}
		} 
		return list;
	}

	/**
	 * Perform LDAP authentication for a user.
	 * @param userName The username to authenticate.
	 * @param password The password to authenticate.
	 * @return true if the user was successfully authenticated by LDAP, false otherwise.
	 */
	private static boolean authLDAP(String userName, String password)
	{
		String searchUser = Global.LDAP_BROWSE_USERNAME;
		String searchPassword = Global.LDAP_BROWSE_PASSWORD;
		String dn = null;

		if(searchUser.equals("") || searchPassword.equals("") || searchUser ==null || searchPassword==null)
			dn = Global.LDAP_ID+"="+userName+","+Global.LDAP_CONTEXT;
		else
		{
			dn = getDN(searchUser, searchPassword, userName);
			// Check a DN was found
			if ((dn == null) || (dn.trim().equals("")))
			{
				//MetaDbHelper.note("Hierarchical LDAP Authentication Error: No DN found for user "+userName);
				return false;
			}
		}

		boolean success=false;
		// Set up environment for creating initial context
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
		"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(javax.naming.Context.PROVIDER_URL, Global.LDAP_URL);

		// Authenticate
		env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "Simple");
		env.put(javax.naming.Context.SECURITY_PRINCIPAL, dn);
		env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
		env.put(javax.naming.Context.AUTHORITATIVE, "true");
		env.put(javax.naming.Context.REFERRAL, "follow");

		DirContext ctx = null;
		try {
			// Try to bind
			ctx = new InitialDirContext(env);
			success = true;
			//MetaDbHelper.note("User "+userName+" logged in with LDAP");
			//MetaDbHelper.note("Context name: "+ctx.getNameInNamespace());
			//MetaDbHelper.note(ctx.toString());
		} 

		catch (NamingException e) {
			MetaDbHelper.note("Error: Failed to authenticate "+userName+".\n"+e);
			success = false;
		}

		finally {
			// Close the context when we're done
			try {
				if (ctx != null)
					ctx.close();
				//MetaDbHelper.note("LDAP connection closed");
			} 
			catch (NamingException e) {
				MetaDbHelper.logEvent(e);
				//MetaDbHelper.note("Error: Failed to establish context.");
			}
		}
		return success;
	}

	/**
	 * Get the LDAP DN for a user.
	 * @param searchUser
	 * @param searchPassword
	 * @param userName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String getDN(String searchUser, String searchPassword, String userName)
	{
		// The resultant DN
		String result;

		// Set up environment for creating initial context
		Hashtable env = new Hashtable(11);
		env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(javax.naming.Context.PROVIDER_URL, Global.LDAP_URL);

		// Use admin credencials for search// Authenticate
		env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "Simple");
		env.put(javax.naming.Context.SECURITY_PRINCIPAL, searchUser);
		env.put(javax.naming.Context.SECURITY_CREDENTIALS, searchPassword);

		DirContext ctx = null;
		try
		{
			// Create initial context
			ctx = new InitialDirContext(env);
			//MetaDbHelper.note("Created LDAP context");

			Attributes matchAttrs = new BasicAttributes(true);
			matchAttrs.put(new BasicAttribute(Global.LDAP_ID, userName));
			//MetaDbHelper.note("Created attributes");

			// look up attributes
			try
			{
				//MetaDbHelper.note("Setting up query");

				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(Global.LDAP_SCOPE);

				NamingEnumeration<SearchResult> answer = ctx.search(
						Global.LDAP_URL + Global.LDAP_CONTEXT,
						"(&({0}={1}))", new Object[] { Global.LDAP_ID,
								userName }, ctrls);

				//MetaDbHelper.note("NamingEnumeration retrieved");

				while (answer.hasMoreElements()) {
					SearchResult sr = answer.next();
					if (StringUtils.isEmpty(Global.LDAP_CONTEXT)) {
						result = sr.getName();
					} else {
						result = (sr.getName() + "," + Global.LDAP_CONTEXT);
					}

					//MetaDbHelper.note("Got DN: "+result);

					return result;
				}
			}
			catch (NamingException e)
			{
				MetaDbHelper.logEvent(e);
				//MetaDbHelper.note("LDAP Error: Failed Search");
			}
		}
		catch (NamingException e)
		{
			MetaDbHelper.logEvent(e);
			//MetaDbHelper.note("LDAP Error: Failed authentication");
		}
		finally
		{
			// Close the context when we're done
			try
			{
				if (ctx != null)
					ctx.close();
			}
			catch (NamingException e)
			{
			}
		}
		// No DN match found
		return null;
	}

}
