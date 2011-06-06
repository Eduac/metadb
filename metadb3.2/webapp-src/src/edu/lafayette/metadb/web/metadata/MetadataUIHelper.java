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
package edu.lafayette.metadb.web.metadata;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import edu.lafayette.metadb.model.items.ItemsDAO;

public class MetadataUIHelper {
	
	public static int getNextIndex(String projname, String raw_index, String current_item, String direction) {
		int last = ItemsDAO.nextItemNumber(projname);
		if (last == 1)
			return -1;
		try {
			if (direction != null && !direction.equals("")) {
				if (direction.equals("last"))
					return last - 1;
				else if (direction.equals("first"))
					return 1;
				else if (current_item != null && !current_item.equals("")) {
					int current = Integer.parseInt(current_item);
					if (direction.equals("next"))
						return current >= last -1 ? last - 1 : current + 1;
					else if (direction.equals("back"))
						return current <= 1 ? 1 : current - 1;
				}
			}
			else if (raw_index != null && !raw_index.equals("")) {
				int destIndex = Integer.parseInt(raw_index);
				if (destIndex <= last - 1 && destIndex >= 1)
					return destIndex;
				return 1;
			}
		} catch (NumberFormatException e) {
			//MetaDbHelper.logEvent(e);
		} catch (Exception e) {
			MetaDbHelper.logEvent(e);
		}
		return 1;
	}
}
