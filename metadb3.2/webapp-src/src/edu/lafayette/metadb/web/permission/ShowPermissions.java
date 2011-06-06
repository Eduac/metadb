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
package edu.lafayette.metadb.web.permission;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.permission.Permission;
import edu.lafayette.metadb.model.permission.PermissionManDAO;
import edu.lafayette.metadb.model.userman.User;
import edu.lafayette.metadb.model.userman.UserManDAO;

/**
 * Servlet implementation class ShowPermissions
 */
public class ShowPermissions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		JSONObject data = new JSONObject();
		String username = request.getParameter("username");
		StringBuilder list = new StringBuilder();
		ArrayList<Permission> permissionList;
		try {
			User user = UserManDAO.getUserData(username);
			JSONObject info =  new JSONObject();
			data.put("info", info);

			info.put("auth_type", user.getAuthType());
			info.put("user_type", user.getType());

			permissionList = PermissionManDAO.getAllProjectPermission(username);
		
			if (permissionList!=null && !permissionList.isEmpty()) {
				for (Permission p:permissionList) {
					
					String projname = p.getProjectName();
					list.append( "<tr class='permission-cell' id='"+projname+"-permission'>\n");
					list.append( "<td class='border'>");
			        list.append( projname+"</td>\n");
			       
			        String md = p.getDesc_md();
			        list.append( "<td class='cell'>\n");
			        if (md.equals("read")) {
			        	list.append( "<input type='radio' name='desc_md-"+projname+"' value='read' checked/>read only<br/>");
			        	list.append( "<input type='radio' name='desc_md-"+projname+"' value='read_write' />read/write");
			        }
			        else if (md.equals("read_write")) {
			        	list.append( "<input type='radio' name='desc_md-"+projname+"' value='read' />read only<br/>");
			        	list.append( "<input type='radio' name='desc_md-"+projname+"' value='read_write' checked/>read/write");
			        }
			        list.append( "</td>\n");
			        
			        
			        md = p.getAdmin_md();
			        list.append( "<td class='cell'>\n");
			        if (md.equals("read")) {
			        	list.append( "<input type='radio' name='admin_md-"+projname+"' value='read' checked/>read only<br/>");
			        	list.append( "<input type='radio' name='admin_md-"+projname+"' value='read_write' />read/write");
			        }
			        else if (md.equals("read_write")) {
			        	list.append( "<input type='radio' name='admin_md-"+projname+"' value='read' />read only<br/>");
			        	list.append( "<input type='radio' name='admin_md-"+projname+"' value='read_write' checked/>read/write");
			        }
			        
			        String table_edit = p.getTable_edit();
			        list.append( "<td class='cell'>\n");
			        if (table_edit.equals("deny")) {
			        	list.append( "<input type='radio' name='table_edit-"+projname+"' value='deny' checked/>deny access<br/>");
			        	list.append( "<input type='radio' name='table_edit-"+projname+"' value='allow' />allow access");
			        }
			        else if (table_edit.equals("allow")) {
			        	list.append( "<input type='radio' name='table_edit-"+projname+"' value='deny' />deny access<br/>");
			        	list.append( "<input type='radio' name='table_edit-"+projname+"' value='allow' checked/>allow access");
			        }
			        
			        String controlled_vocab = p.getControlled_vocab();
			        list.append( "<td class='cell'>\n");
			        if (controlled_vocab.equals("deny")) {
			        	list.append( "<input type='radio' name='controlled_vocab-"+projname+"' value='deny' checked/>deny access<br/>");
			        	list.append( "<input type='radio' name='controlled_vocab-"+projname+"' value='allow' />allow access");
			        }
			        else if (controlled_vocab.equals("allow")) {
			        	list.append( "<input type='radio' name='controlled_vocab-"+projname+"' value='deny' />deny access<br/>");
			        	list.append( "<input type='radio' name='controlled_vocab-"+projname+"' value='allow' checked/>allow access");
			        }
			        list.append( "</td>\n");
			        			        
			        String dataPerm = p.getData(); 
			        list.append( "<td class='cell'>\n");
			        if (dataPerm.equals("none")) {
			        	list.append( "<input type='radio' name='data-"+projname+"' value='none' checked />no access<br/>");
			        	list.append( "<input type='radio' name='data-"+projname+"' value='export' />export only<br/>");
			        	list.append( "<input type='radio' name='data-"+projname+"' value='import_export' />import/export");
			        }
			        else if (dataPerm.equals("export")) {
			        	list.append( "<input type='radio' name='data-"+projname+"' value='none' />no access<br/>");
			        	list.append( "<input type='radio' name='data-"+projname+"' value='export' checked/>export only<br/>");
			        	list.append( "<input type='radio' name='data-"+projname+"' value='import_export' />import/export");
			        }
			        else if (dataPerm.equals("import_export")) {
			        	list.append( "<input type='radio' name='data-"+projname+"' value='none' />no access<br/>");
			        	list.append( "<input type='radio' name='data-"+projname+"' value='export' />export only<br/>");
			        	list.append( "<input type='radio' name='data-"+projname+"' value='import_export' checked/>import/export");
			        }
			        list.append( "</td>\n");
			        
			        
			        
			        list.append( "<td class='border'><input type='button' class='ui-state-default' style='width:60px' name='revoke' value='Revoke'></td>\n");
			        list.append( "</tr>\n");
		        }
			}
			data.put("data", list.toString());
			out.print(data);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}		
		response.setContentType("application/x-json");
		out.flush();
	}

}
