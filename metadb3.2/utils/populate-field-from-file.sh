#!/bin/sh

#
# MetaDB: A Distributed Metadata Collection Tool
# Copyright 2011, Lafayette College, Eric Luhrs, Haruki Yamaguchi, Long Ho.
#
# This file is part of MetaDB.
#
#    MetaDB is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    MetaDB is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with MetaDB.  If not, see <http://www.gnu.org/licenses/>.
#

echo -e "\nThis script populates a field based on values from a file.\n"

read -p "Name of collection [required]: " collection

read -p "Name of element [required]: " element

read -p "Name of qualifier [blank for none]: " label

read -p "Enter filename with field values [required]: " filename

        index=0

        while read line ; do
                MYARRAY[$index]="$line"
                index=$(($index+1))
        done < $filename

echo -e "\nThere are ${index} lines in $filename.  Does this match number of records in $collection collection?\n"

read -p "[any key to proceed, ^C to quit] " yn

echo -e "\nAttempt to populate ${index} $element.$label fields with values from $filename?\n"

read -p "[any key to proceed, ^C to quit] " yn

count=0

while [ $count -ne ${index} ]; do

count=`expr $count + 1 `

item=`printf "%04d" "$count" `

value="${MYARRAY[$count - 1]}"

echo "Processing item $item from $collection collection";

psql -d metadb << EOF
UPDATE projects_adminmd_descmd SET data = '$value' WHERE project_name = '$collection' AND element = '$element' AND label = '$label' AND item_number = '$count';
\q
EOF

done

