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
package edu.lafayette.metadb.model.commonops;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import edu.lafayette.metadb.model.syslog.SysLogDAO;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Helper class that does various system-wide operations, such as debug logging and 
 * operations above the project level.
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0
 *
 */
public class MetaDbHelper 
{
	private static final String PROJECT_EXISTS=

		"SELECT "+Global.PROJECT_NAME+" "+
		"FROM "+Global.PROJECTS_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String USER_EXISTS=
		"SELECT "+Global.USER_NAME+" "+
		"FROM "+Global.USERS_TABLE+" "+
		"WHERE "+Global.USER_NAME+"=?";

	private static final String GET_ITEM_NUMBERS=

		"SELECT "+Global.ITEM_NUMBER+" "+
		"FROM "+Global.ITEM_TABLE+" "+
		"WHERE "+Global.PROJECT_NAME+"=?";

	private static final String COUNT_ITEMS=
		"SELECT COUNT("+Global.ITEM_NUMBER+") "+" "+
		"FROM "+Global.ITEM_TABLE;

	/*
	 * Left these three hardcoded for now. 
	 * If there is a need for more toggle-variables, 
	 * will consider creating a new package to handle those.
	 */
	private static final String PARSER_ON=
		"UPDATE "+Global.CTRL_TABLE+" "+
		"SET "+Global.CTRL_STATUS+"='t'"+" "+
		"WHERE "+Global.CTRL_NAME+"=?";

	private static final String PARSER_OFF=
		"UPDATE "+Global.CTRL_TABLE+" "+
		"SET "+Global.CTRL_STATUS+"='f'"+" "+
		"WHERE "+Global.CTRL_NAME+"=?";

	private static final String GET_PARSER_STATUS=
		"SELECT "+Global.CTRL_STATUS+" "+
		"FROM "+Global.CTRL_TABLE+" "+
		"WHERE "+Global.CTRL_NAME+"=?";
	public MetaDbHelper()
	{
	}

	/**
	 * Checks if a metadata type is valid.
	 * @param lookForMdType The metadata type to be checked.
	 * @return true if lookForMdType is a valid metadata type, false otherwise. 
	 */
	public static boolean mdTypeExists(String lookForMdType)
	{
		return true;
	}

	/**
	 * Checks if a project exists in MetaDB.
	 * 
	 * @param projectName The project name to be checked for existence
	 * @return true if a project with name projectName is found, false otherwise.
	 */
	public static boolean projectExists(String projectName)
	{
		Connection conn=Conn.initialize(); //Set up connection
		boolean exists=false;
		if(conn!=null)
		{
			try
			{
				PreparedStatement projectExistsQuery=conn.prepareStatement(PROJECT_EXISTS);
				projectExistsQuery.setString(1,  projectName); //set parameters
				ResultSet projectExistsQueryResult=projectExistsQuery.executeQuery();	
				if(projectExistsQueryResult.next())
					exists=true;

				projectExistsQueryResult.close();
				projectExistsQuery.close();

				conn.close();
			}
			catch(Exception e)
			{
				logEvent(e);
				return false;
			}
		}
		return exists;
	}


	/**
	 * Checks if a user exists in MetaDB.
	 * 
	 * @param userName The user name to be checked for existence
	 * @return true if the user exists, false otherwise. 
	 */
	public static boolean userExists(String userName) 
	{
		Connection conn = Conn.initialize(); //Set up connection
		boolean userExists=false;
		if(conn!=null)
		{
			try
			{
				PreparedStatement userExistsQuery=conn.prepareStatement(USER_EXISTS);
				userExistsQuery.setString(1,  userName); //set parameters
				System.out.println("userExists: Parameter set.");
				ResultSet userExistsQueryResult=userExistsQuery.executeQuery();
				
				if(userExistsQueryResult.next())
					userExists=true; // Query found a row that matched condition

				userExistsQueryResult.close();
				userExistsQuery.close();
				conn.close();
				
			}
			catch(Exception e)
			{
				logEvent(e);
				return false;
			}
		}
		return userExists;
	}


	/**
	 * Logs debugging information to the log table. 
	 * 
	 * @param className The class which called the method to be logged. 
	 * @param methodName The method that was called. 
	 * @param eventText The event that occurred in the class. 
	 */
	public static void  logEvent(String className, String methodName, String eventText)
	{
		SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_DEBUG, className+"."+methodName+": "+eventText);
	}

	/**
	 * Logs debugging information to the log table. 
	 * 
	 * @param className The class which called the method to be logged. 
	 * @param e The exception passed from the caller. 
	 */
	public static void  logEvent(String className, Exception e)
	{
		SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_DEBUG, getStackTrace(e));
	}

	/**
	 * Logs debugging information to the log table. 
	 * 
	 * @param e The exception passed from the caller.
	 **/
	public static void  logEvent(Exception e)
	{
		SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_DEBUG, getStackTrace(e));	
	}

	/**
	 * Adds a debugging note to the log table. 
	 * @param note The note contents.
	 */
	public static void note(String note)
	{
		SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_DEBUG, note);
	}

	/**
	 * Toggles the parser to on.
	 * @return true if togggled on, false otherwise.
	 */
	public static boolean parserOn(String projectName, String setting)
	{
		SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PARSER, "Parser initiated for "+projectName+"; " +
				"Setting: "+setting);
		Connection conn=Conn.initialize();
		if (conn!=null)
		{
			try
			{
				PreparedStatement parserOn=conn.prepareStatement(PARSER_ON);
				parserOn.setString(1, "parser");
				parserOn.executeUpdate();
				parserOn.close();
				conn.close();
				return true;
			}
			catch(Exception e)
			{
				logEvent(e);
				return false;
			}
		}
		return false;
	}

	/**
	 * Toggle the parser to off.
	 * @return true if toggled off, false otherwise
	 */
	public static boolean parserOff(String projectName, String setting, String timeElapsed)
	{
		SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PARSER, "Parser finished for "+projectName+"; " +
				"Setting: "+setting+", running time: "+timeElapsed);
		Connection conn=Conn.initialize();
		if (conn!=null)
		{
			try
			{
				PreparedStatement parserOff=conn.prepareStatement(PARSER_OFF);
				parserOff.setString(1, "parser");
				parserOff.executeUpdate();
				parserOff.close();
				conn.close();
				return true;
			}
			catch(Exception e)
			{
				logEvent(e);
				return false;
			}
		}
		return false;
	}

	/**
	 * Get the status of the image parser.
	 * @return true if the parser flag is true, false otherwise.
	 */
	public static boolean getParserStatus()
	{
		boolean status=false;
		Connection conn=Conn.initialize();
		if(conn!=null)
		{
			try
			{
				PreparedStatement getParserStatus=conn.prepareStatement(GET_PARSER_STATUS);
				getParserStatus.setString(1, Global.CTRL_PARSER);
				ResultSet rs=getParserStatus.executeQuery();
				if(rs.next())
				{
					status=rs.getBoolean(Global.CTRL_STATUS);
					rs.close();
					getParserStatus.close();
				}
				conn.close();
				return status;
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Returns a string representation of the stack trace 
	 * for a Throwable object. 
	 * @param t The Throwable object of which the stack trace is desired. 
	 * @return A String representation of the stack trace for t.
	 */
	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		String trace=sw.toString();
		int traceLength=trace.length();

		if(traceLength>=1200)
			return trace.substring(0,1199);
		else 
			return trace.substring(0, traceLength-1);

	}

	/**
	 * Returns the item numbers for a project.
	 * @param projectName
	 * @return an ArrayList of integers representing the item number sequence in the project.
	 */
	public static ArrayList<Integer> getItemNumbers(String projectName)
	{
		ArrayList<Integer> results=new ArrayList<Integer>();
		Connection conn=Conn.initialize();
		if(conn!=null)
		{

			try
			{
				PreparedStatement getItemNumbers=conn.prepareStatement(GET_ITEM_NUMBERS);
				getItemNumbers.setString(1, projectName);
				ResultSet itemNumbers=getItemNumbers.executeQuery();

				while(itemNumbers.next())
				{
					results.add(itemNumbers.getInt(Global.ITEM_NUMBER));
				}
				getItemNumbers.close();
				conn.close();
				return results;
			}
			catch(Exception e)
			{
				logEvent(e);
			}
		}
		return results;
	}

	/**
	 * Get the number of items in MetaDB.
	 * @return an int representing the total number of scanned items in MetaDB.
	 */
	public static int getItemCount()
	{
		int count=0;
		Connection conn=Conn.initialize();
		if (conn!=null)
		{
			try
			{
				PreparedStatement countItems=conn.prepareStatement(COUNT_ITEMS);
				ResultSet cnt=countItems.executeQuery();
				if (cnt.next())
					count=cnt.getInt(1);
				
				cnt.close();
				countItems.close();
				conn.close();
			}
			catch(Exception e)
			{
				logEvent(e);
				return 0;
			}
		}
		return count;
	}

	/**
	 * Get scaled dimensions of an image for the UI.
	 * @param width the given width of the image.
	 * @param height the given height of the image.
	 * @param maxWidth the maximum width of the image.
	 * @param maxHeight the maximum height of the image.
	 * @return an array of ints containing the width and height for the correctly 
	 * scaled image, according to the given width/height and maximum width/height, 
	 * in the 0th and 1st index respectively.
	 */
	public static int[] getScaledDimensions(int width, int height, int maxWidth, int maxHeight) {
		int[] dimensions = new int[2];
		if (width <= maxWidth || height <= maxHeight) {
			dimensions[0] = width;
			dimensions[1] = height;
		}
		else {
			int scaledWidth = width;
			int scaledHeight = height;
			try {
				if (maxWidth > 0 && maxHeight > 0) {
					double hRatio = height/(maxHeight*1.0);
					double wRatio = width/(maxWidth*1.0);
					double ratio = hRatio <= wRatio ? wRatio : hRatio;
					scaledWidth = (int) (width/ratio) >= maxWidth ? maxWidth: (int) (width/ratio);
					scaledHeight = (int) (height/ratio) >= maxHeight ? maxHeight : (int) (height/ratio);
				}
			} catch(Exception e) {
				logEvent(e);
			}
			dimensions[0] = scaledWidth;
			dimensions[1] = scaledHeight;
		}
		return dimensions;
	}

	/**
	 * Returns a list of Strings representing the terms in the search string.
	 * The words must be separated by whitespace and quotes signify an exact match.
	 * @param query The search query string.
	 * @return an ArrayList of Strings, containing the original query separated to individual search terms 
	 */
	public static ArrayList<String> getSQLTokens(String query)
	{
		//Parse query into quoted/non-quoted words
		String[] tokens=query.split("#(?:\"([^\"]+)\")|([^ ]+)#");
		String noteTokens = "";
		for (String token : tokens)
			noteTokens += "\"" + token + "\" ";
		//MetaDbHelper.note("Search tokens: "+noteTokens);
		ArrayList<String> sqlTokens=new ArrayList<String>();
		int numTokens=tokens.length;
		for(int i=0; i<numTokens; i++)
		{
			String quotesRemovedString=tokens[i].replaceAll("\"", "");
			sqlTokens.add("%"+quotesRemovedString.trim().toLowerCase()+"%");
		}
		return sqlTokens;		
	}

	/**
	 * Returns a list of Strings representing the terms in the search string.
	 * The words must be separated by whitespace and quotes signify an exact match.
	 * @param query The search query string.
	 * @return an ArrayList of Strings, containing the original query separated to individual search terms.
	 */
	public static ArrayList<String> getStringTokens(String query)
	{
		//Parse query into quoted/non-quoted words
		String[] tokens=query.split("#(?:\"([^\"]+)\")|([^ ]+)#");
		String noteTokens = "";
		for (String token : tokens)
			noteTokens += "\"" + token + "\" ";
		//MetaDbHelper.note("Search tokens: "+noteTokens);
		ArrayList<String> sqlTokens=new ArrayList<String>();
		int numTokens=tokens.length;
		for(int i=0; i<numTokens; i++)
		{
			String quotesRemovedString=tokens[i].replaceAll("\"", "");
			sqlTokens.add(quotesRemovedString.trim().toLowerCase());
		}
		return sqlTokens;		
	}

	/**
	 * Parses a string containing a range of integers into a set of integers.
	 * @param source The source string, to parse for integers.
	 * @return A Set containing the integers specified by the source string.
	 */
	public static Set<Integer> filterRecords(String source) {
		Set<Integer> records = new TreeSet<Integer>();
		String[] processedEntries = source.split(",");
		try {
			for (String entry : processedEntries) {
				if (entry != "") {
					if (entry.indexOf("-") != -1) {
						int min = Integer.parseInt(entry.split("-")[0]);
						int max = Integer.parseInt(entry.split("-")[1]);
						for (int i = min; i <= max; i++) 
							records.add(i);
					}
					else
						records.add(Integer.parseInt(entry));
				}
			}
		}
		catch (Exception e) {
			logEvent(e);
			records.clear();
		}
		return records;
	}
	
	/**
	 * Lexically sort a list of controlled vocab words. 
	 * @param vocabs The String containing the vocab words.
	 * @return A String containing the same vocab terms, sorted in lexical order.
	 */
	public static String sortVocab(String vocabs) {
		if(vocabs.trim().equals(""))
			return vocabs;
		Set<String> sortedData = new TreeSet<String>(Arrays.asList(vocabs.split(";")));
		StringBuilder finalData = new StringBuilder();
		for (String vocab : sortedData) {
			finalData.append(vocab);
			finalData.append(";");
		}
		return finalData.toString();
	}
	
}
