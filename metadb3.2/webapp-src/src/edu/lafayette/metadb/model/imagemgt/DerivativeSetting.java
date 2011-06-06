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
package edu.lafayette.metadb.model.imagemgt;

/**
 * Class representing a single derivative setting for a single project. 
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class DerivativeSetting 
{
	private String projectName;
	private String settingName;
	private int maxWidth;
	private int maxHeight;
	private String brand;
	private String bgColor;
	private String fgColor;
	private int annotationMode;
	private boolean enabled;
	
	public DerivativeSetting(String projectName, String settingName, int maxWidth, int maxHeight, 
			String brand, String bgColor, String fgColor, int annotationMode, boolean enabled)
	{
		this.projectName=projectName;
		this.settingName=settingName;
		this.maxWidth=maxWidth;
		this.maxHeight=maxHeight;
		this.brand=brand;
		this.bgColor=bgColor;
		this.fgColor=fgColor;
		this.annotationMode=annotationMode;
		this.enabled=enabled;

	}

	public String getProjectName() {
		return projectName;
	}


	public String getSettingName() {
		return settingName;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public String getBrand() {
		return brand;
	}

	public int getAnnotationMode() {
		return annotationMode;
	}

	public String getBgColor() {
		return bgColor;
	}

	public String getFgColor() {
		return fgColor;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
}
