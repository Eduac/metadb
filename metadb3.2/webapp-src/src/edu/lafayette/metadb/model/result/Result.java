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
package edu.lafayette.metadb.model.result;

/**
 * Class used to store a "result" of some action. 
 * Currently only used by the data import code. 
 * 
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0
 * 
 */
public class Result implements ResultInterface {

	private boolean result;
	private Object data;
	private String message;
	
	public Result(boolean result, String message, Object data) {
		this.result = result;
		this.message = message;
		this.data = data;
	}
	
	public Result(boolean result) {
		this.result = result;
	}
	
	public Object getData() {
		return data;
	}

	
	public String getMessage() {
		return message;
	}

	
	public boolean isSuccess() {
		return result;
	}
	
	public void setResult(boolean res) {
		this.result = res;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public void setMessage(String mess) {
		this.message = mess;
	}

}
