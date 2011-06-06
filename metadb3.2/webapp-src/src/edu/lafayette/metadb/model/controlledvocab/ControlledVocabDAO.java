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
package edu.lafayette.metadb.model.controlledvocab;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import edu.lafayette.metadb.model.commonops.Conn;
import edu.lafayette.metadb.model.commonops.Global;
import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.syslog.SysLogDAO;
import edu.lafayette.metadb.model.userman.UserManDAO;


/**
 * This class serves as the access object for controlled vocabulary
 * lists in MetaDB. It provides various functions such as adding, 
 * removing, updating, and retrieving controlled vocabulary lists. 
 *	
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0
 */
public class ControlledVocabDAO 
{
	private static final String ADD_CONTROLLED_VOCAB=
		
		"INSERT INTO "+Global.CONTROLLED_VOCAB_TABLE+" "+
		"("+Global.CONTROLLED_VOCAB_NAME+","+Global.CONTROLLED_VOCAB_VALUES+")"+
		"VALUES (?, ?)";
	
	private static final String REMOVE_CONTROLLED_VOCAB=
		
		"DELETE FROM "+Global.CONTROLLED_VOCAB_TABLE+" "+
		"WHERE "+Global.CONTROLLED_VOCAB_NAME+"=?";
	
	private static final String UPDATE_CONTROLLED_VOCAB=
		
		"UPDATE "+Global.CONTROLLED_VOCAB_TABLE+" "+
		"SET "+Global.CONTROLLED_VOCAB_VALUES+"=?"+" "+
		"WHERE "+Global.CONTROLLED_VOCAB_NAME+"=?";
	
	private static final String GET_CONTROLLED_VOCAB=
		
		"SELECT "+Global.CONTROLLED_VOCAB_VALUES+" "+
		"FROM "+Global.CONTROLLED_VOCAB_TABLE+" "+
		"WHERE "+Global.CONTROLLED_VOCAB_NAME+"=?";
	
	private static final String GET_CONTROLLED_VOCAB_NAMES=
		
		"SELECT "+Global.CONTROLLED_VOCAB_NAME+" "+
		"FROM "+Global.CONTROLLED_VOCAB_TABLE;
	
	private static final String GET_NAMES_BY_USER = 
		"SELECT DISTINCT "+Global.CONTROLLED_VOCAB_NAME+" "+
		"FROM "+Global.CONTROLLED_VOCAB_TABLE+" "+
		"WHERE "+Global.CONTROLLED_VOCAB_NAME+" "+
		"IN "+
		"   (SELECT DISTINCT "+Global.CONTROLLED_VOCAB_NAME+" "+
			"FROM "+Global.ADMIN_DESC_TABLE+" "+
			"WHERE "+Global.PROJECT_NAME+" "+
			"IN "+
			"(SELECT "+Global.PROJECT_NAME+" "+
			"FROM "+ Global.PERMISSIONS_TABLE+" "+
			"WHERE "+Global.USER_NAME+"=? )"+" "+
			"ORDER BY "+ Global.CONTROLLED_VOCAB_NAME+") "+" "+
			"AND "+Global.CONTROLLED_VOCAB_NAME+" <> ''";

	public ControlledVocabDAO()
	{		
	}
	
	/**
	 * Adds a new controlled vocabulary list to the system.
	 * @param vocabName The name of the new vocabulary to add.
	 * @param input Input stream for the controlled vocabulary. 
	 * @return true if the vocabulary was added successfully, false otherwise.
	 */
	public static boolean addControlledVocab(String vocabName, InputStream input)
	{
		//MetaDbHelper.note("Adding controlled vocab \'"+vocabName+"\'");
		if(vocabName == null || vocabNameExists(vocabName))
			return false;

		Set<String> vocabSet=new TreeSet<String>();		
		try
		{
			Scanner fileSc=new Scanner(input);
			while(fileSc.hasNextLine())
			{
				String vocabEntry=fileSc.nextLine();
				vocabSet.add(vocabEntry.trim());
			}
			//MetaDbHelper.note("Controlled vocab \'"+vocabName+"\' added.");
			return addControlledVocab(vocabName, vocabSet);
		}
		
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
			
		}
		return false;		
	}
	
	/**
	 * Updates an existing controlled vocabulary list. 
	 * @param vocabName The name of the vocabulary list to be updated.
	 * @param vocab the new vocabulary list. 
	 * @return true if the controlled vocabulary is updated successfully, false otherwise. 
	 */
	public static boolean updateControlledVocab(String vocabName, String vocab)
	{
		if(vocabName == null)
			return false;
		
		else if(!vocabNameExists(vocabName)) //vocab list doesn't exist.
			return false;
			//MetaDbHelper.note("Vocab \'"+vocabName+"\' doesn't exist...aborting update.");

		try
		{			
			Set<String> newVocabList=ControlledVocabDAO.toVocabSet(vocab);			
				return updateControlledVocab(vocabName, newVocabList);
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}
	
	/**
	 * Adds new controlled vocabulary to the system.
	 * @param vocabName The name of the vocabulary to be added.
	 * @param vocabSet A Set containing all the words in the vocabulary. 
	 * @return true if the vocabulary was added successfully, false otherwise.
	 */
	public static boolean addControlledVocab(String vocabName, Set<String> vocabSet)
	{
		if(vocabName == null)
			return false;
		
		else if(vocabNameExists(vocabName))
		{
			//MetaDbHelper.note("Vocab \'"+vocabName+"\' already existent. Aborting add...");
			return false;
		}
		Connection conn = Conn.initialize();		
		if(conn!=null)
		{
			try
			{
				PreparedStatement addControlledVocab=conn.prepareStatement(ADD_CONTROLLED_VOCAB);
				String vocabString=toVocabString(vocabSet);
				
				addControlledVocab.setString(1, vocabName);
				addControlledVocab.setString(2, vocabString); 		
				addControlledVocab.executeUpdate();				
				addControlledVocab.close();
				conn.close(); 
				
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PROJECT, "New controlled vocab "+vocabName+" has " +
						"been created.");
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
	 * Removes a controlled vocabulary list from the system. 
	 * @param vocabName The name of the vocabulary list to be removed. 
	 * @return true if the vocabulary list was successfully removed, false otherwise.
	 */
	public static boolean removeControlledVocab(String vocabName)
	{
		if(vocabName == null)
			return false;
		
		else if(!vocabNameExists(vocabName)) //Vocab doesn't exist
			return false;
		//MetaDbHelper.note("Vocab \'"+vocabName+"\' does not exist. Aborting deletion..");

		Connection conn = Conn.initialize();		
		if(conn!=null)
		{
			try
			{
				PreparedStatement deleteControlledVocab=conn.prepareStatement(REMOVE_CONTROLLED_VOCAB);

				deleteControlledVocab.setString(1, vocabName);
				deleteControlledVocab.executeUpdate();
				deleteControlledVocab.close();
				conn.close(); 
				SysLogDAO.log(Global.METADB_USER, Global.SYSLOG_PROJECT, "Controlled vocab "+vocabName+" has " +
				"been deleted.");
				return true;				
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Updates a controlled vocabulary list with a new vocabulary list. 
	 * @param vocabName The name of the vocabulary list to be updated. 
	 * @param newVocab A Set of Strings to be used as the new vocabulary. 
	 * @return true if the vocabulary list was updated successfully, false otherwise.
	 */
	public static boolean updateControlledVocab(String vocabName, Set<String> newVocab)
	{
		if(vocabName==null || !vocabNameExists(vocabName))
			return false;
		
		//else if(!vocabNameExists(vocabName)) //Vocab doesn't exist
			//return false;
		//MetaDbHelper.note("Vocab \'"+vocabName+"\' doesn't exist. aborting update...");

		Connection conn = Conn.initialize(); //Establish connection
		
		if(conn!=null)
		{
			try
			{
				PreparedStatement updateControlledVocab=conn.prepareStatement(UPDATE_CONTROLLED_VOCAB);

				String vocabString=toVocabString(newVocab);
				updateControlledVocab.setString(1, vocabString); //Set parameters
				updateControlledVocab.setString(2, vocabName);
				
				updateControlledVocab.executeUpdate();
				updateControlledVocab.close();
				conn.close(); //Close statement/connections
				
				//MetaDbHelper.note("Controlled vocab \'"+vocabName+"\' successfully updated.");
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
	 * Get a list of the names of the controlled vocabs available.
	 * @return a Set of the names of the controlled vocabs available.
	 */
	public static Set<String> getControlledVocabs()
	{
		Set<String> vocabNames=new TreeSet<String>();
		Connection conn = Conn.initialize(); //Establish connection
		
		if(conn!=null)
		{
			try
			{
				PreparedStatement getControlledVocabNames=conn.prepareStatement(GET_CONTROLLED_VOCAB_NAMES);
				ResultSet vocabs=getControlledVocabNames.executeQuery();
				
				while(vocabs.next())
					vocabNames.add(vocabs.getString(Global.CONTROLLED_VOCAB_NAME));
				
				getControlledVocabNames.close();
				conn.close(); //Close statement/connections
								
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return vocabNames;
	}
	
	/**
	 * Get a list of the names of the controlled vocabs in the system, which 
	 * are tied to the projects a particular user has permissions to.
	 * @return a Set of the names of the controlled vocabs in the system, which 
	 * are tied to the projects a particular user has permissions to.
	 * @param userName the user to get the controlled vocab list for.
	 */
	public static Set<String> getControlledVocabs(String userName)
	{
		if(userName==null || userName.trim().equals(""))
			return getControlledVocabs();
		if(UserManDAO.getUserData(userName).getType().equals("admin"))
			return getControlledVocabs();
		
		Set<String> vocabNames=new TreeSet<String>();
		Connection conn = Conn.initialize();
		
		if(conn!=null)
		{
			try
			{
				PreparedStatement getControlledVocabNames=conn.prepareStatement(GET_NAMES_BY_USER);
				getControlledVocabNames.setString(1, userName);
				ResultSet vocabs=getControlledVocabNames.executeQuery();
				
				while(vocabs.next())
					vocabNames.add(vocabs.getString(Global.CONTROLLED_VOCAB_NAME));

				getControlledVocabNames.close();
				conn.close();
								
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return vocabNames;
	}
	
	/**
	 * Gets a list of controlled vocabulary based on the name of the vocabulary.
	 * @param vocabName The name of the vocabulary list to retrieve.
	 * @return A Set of vocabulary associated with this vocabName.
	 */
	public static Set<String> getControlledVocab(String vocabName)
	{
		//MetaDbHelper.note("Trying to get vocab: "+vocabName);
		Set<String> controlledVocab = new TreeSet<String>();
		
		Connection conn = Conn.initialize(); //Establish connection
		
		if(conn!=null)
		{
			try
			{
				PreparedStatement addControlledVocab=conn.prepareStatement(GET_CONTROLLED_VOCAB);
				addControlledVocab.setString(1, vocabName);
				
				ResultSet vocabValues=addControlledVocab.executeQuery();
				if(vocabValues.next())
					controlledVocab=toVocabSet(vocabValues.getString(Global.CONTROLLED_VOCAB_VALUES));
				
				//MetaDbHelper.note("Vocab terms of "+vocabName+ "retrieved: "+controlledVocab.size());
				addControlledVocab.close();
				conn.close();
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
		}
		return controlledVocab;
	}
	
	
	/**
	 * Checks whether a given vocabulary exists in the system. 
	 * @param vocabName The name to be searched. 
	 * @return true if a vocabulary list is found with name vocabName, false otherwise.
	 */
	public static boolean vocabNameExists(String vocabName)
	{
		return vocabName != null && !vocabName.equals("") && getControlledVocabs().contains(vocabName);
	}

	/**
	 * Converts a Set of Strings into the internal
	 * representation used by the system.
	 * @param vocabSet A Set of strings to be converted. 
	 * @return the system representation of the string list.
	 */
	private static String toVocabString(Set<String> vocabSet)
	{
		StringBuilder vocabString=new StringBuilder();
		int i = 0;
		for(String vocab: vocabSet)
		{
			vocabString.append(vocab.trim());
			if (i != vocabSet.size() - 1)
				vocabString.append(";");
			i++;
		}
		return vocabString.toString();
	}
	
	/**
	 * Converts the internal representation of a vocabulary list 
	 * to a Set of Strings. 
	 * @param vocabString the String to be parsed. 
	 * @return a List containing the parsed strings.
	 */
	private static Set<String> toVocabSet(String vocabString)
	{
		Set<String> vocabSet=new TreeSet<String>();
		Scanner vocabParser=new Scanner(vocabString).useDelimiter(";");
		while(vocabParser.hasNext())
		{
			vocabSet.add(vocabParser.next());
		}
		return vocabSet;
	}
}
