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

sql=""

echo -e "\nThis script will performs a search and replace on specified MetaDB projects, fields, and/or qualifiers."
echo -e "To restore a created database file please use type in:\n ./findreplace.sh --restore [filename]\n"

if [ "$1" = --restore ]; then
	if [ "$#" = 2 ]; then
		echo "Will now attempt to restore the database with $2."
		read -p "[press any key to proceed, ^C to quit] " yn
		psql -d metadb -f ${2}
	else echo "File needed"
		exit 1
	fi
	exit 0
fi
	

project=
until [ ${#project} -gt 0 ]
do
	read -p "Name of project [required, or '*' for all]: " project
	project=${project//[^a-zA-Z0-9_-.*]/}
	project=${project//\'/\\\'}
done

if [ "${project}" != "*" ]; then
	sql=$sql" AND project_name='$project'"
	proj=" of the $project project"
else proj=" and ALL projects"
fi


name=
until [ ${#name} -gt 0 ]
do
	read -p "Name of element [required, or '*' for all]: " name
	name=${name//[^a-zA-Z0-9_-.*]/}
	name=${name//\'/\\\'}
done



if [ "${name}" != "*" ]; then
	sql=$sql" AND element='$name'"
	na=" the $name"
else na=" ALL"
fi

qualifier=
read -p "Name of qualifier [blank for none, or '*' for all]: " qualifier
qualifier=${qualifier//[^a-zA-Z0-9_.*]/}
qualifier=${qualifier//\'/\\\'}


find=
echo -e "\nThe find citeria is case-sensitive and matches exact string.\nThe user must include spaces before and after find term to match whole words.\n"
until [ ${#find} -gt 0 ]
do
	read -r -p "String to find: " find
	findoriginal=$find
	findregex=${find//\\/\\\\\\\\}
	find=${find//\\/\\\\}

	findregex=${findregex//\'/\\\'}
	find=${find//\'/\\\'}

	findregex=${findregex//\"/\\\"}
	find=${find//\"/\\\"}

	findregex=${findregex//\%/\\\\\%}
	find=${find//\%/\\\%}

	findregex=${findregex//\_/\\\\\_}
	find=${find//\_/\\\_}
done

echo
replace=
until [ ${#replace} -gt 0 ]
do
	read -r -p "String to replace: " replace
	replaceoriginal=$replace
	replace=${replace//\\/\\\\}
	replace=${replace//\'/\\\'}
	replace=${replace//\"/\\\"}
	replace=${replace//\%/\\\%}
	replace=${replace//\_/\\\_}
done


atdate=`date '+%Y%m%d%H%M%S'`
echo
read -p "Do you want to dump the database table to 'pg_dump_$atdate.sql'?: (y/n) " yn
if [ "$yn" = y ]; then
	echo "DROP TABLE projects_adminmd_descmd;" > "pg_dump_$atdate.sql"
	pg_dump -t "projects_adminmd_descmd" metadb >> "pg_dump_$atdate.sql"
	if [ $? = 0 ]; then
		echo "Dump Completed"
	else echo "Dump Failed"
	fi
else echo "Dump Skipped"
fi
echo

sql3head1="SELECT count(*) "
sql3head2="SELECT project_name, item_number, md_type, element, label "
sql3="FROM projects_adminmd_descmd WHERE data LIKE '%$findregex%'"$sql
sql="UPDATE projects_adminmd_descmd SET data=replace(data,'$find','$replace') WHERE data LIKE '%$findregex%'"$sql


if [ "${qualifier}" = "*" ]; then
	qual=".ALL fields"
elif [ ${#qualifier} = 0 ]; then
	sql=$sql" AND label='$qualifier'"
	sql3=$sql3" AND label='$qualifier'"
	qual=" field"
else  sql=$sql" AND label='$qualifier'"
	sql3=$sql3" AND label='$qualifier'"
	qual="."$qualifier" field"
fi


sql=$sql";"
sql3=$sql3";"

echo -n "Attempt to replace "
echo -n `psql -d metadb -P footer=off -P format="Unaligned" -t -c "$sql3head1$sql3"`
echo -n " occurances of the term '$findoriginal' with '$replaceoriginal'"
echo " in$na$qual$proj"
read -p "[press any key to proceed, or, ^C to quit] " yn
psql -d metadb -P footer=off -P format="Unaligned" -t -c "$sql"
