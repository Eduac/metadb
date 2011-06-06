#!/bin/sh

#
# MetaDB: A Distributed Metadata Collection Tool
# Copyright 2011, Lafayette College, Eric Luhrs, Haruki Yamaguchi, Long Ho.
#
#    This file is part of MetaDB.
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

echo -e "\nThis script populates serialized item numbers across a given field.\n"

read -p "Name of collection [required]: " collection

read -p "Name of element [required]: " element

read -p "Name of label [blank for none]: " label

read -p "Enter beginning string [blank for none]: " string1

read -p "Number of records [required]: " records

read -p "Enter ending string [blank for none]: " string2

echo -e "\nAttempt to set $records $element.$label fields in $collection to "$string1"000n"$string2?""

read -p "[any key to proceed, ^C to quit] " yn

count=0

while [ $count -ne $records ]; do

count=`expr $count + 1 `

item=`printf "%04d" "$count" `

echo "Processing $collection-$item";

psql -d metadb << EOF
UPDATE projects_adminmd_descmd SET data = '$string1$item$string2' WHERE
project_name = '$collection' AND element = '$element' AND label
= '$label' AND item_number = '$count';
\q
EOF

done

