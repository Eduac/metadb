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

cat << EOF
Welcome to the administrative shell script

This script will either add a new admin user or remove an existing user
a - Adds a new user with administrative rights
r - Removes an existing user
EOF

read -p "Please enter the action to perform (a/r): " CHOICE

if [ "$CHOICE" = a ]; then
	user=
	until [ ${#user} -ge 3 ]
	do
		echo -n "Enter the desired username of the new user: "
		read user
		if [ ${#user} -lt 3 ]; then
			echo "Error: Username must be greater than or equal to 3 characters."
		fi
	done
	
	password=not
	checkPassword=same
	
	until [ "$password" = "$checkPassword" ] && [ ${#password} -ge 6 ]
	do
		echo -n "Enter the admin's password: " 
		read -s password
		echo
		echo -n "Confirm the admin's password: "
		read -s checkPassword
		echo
		
		
		if [ "$password" != "$checkPassword" ]; then
			echo "Error: Passwords do not match! Please try again."
		else
			if [ ${#password} -lt 6 ]; then 
				echo "Error: Password has to be greater than or equal to 6 characters"
			fi
		fi
	done
	
	encrypt=`echo -n "$password" | openssl sha1 | sed "s/.* //g"`
	echo "$encrypt"
	echo "Will now attempt to connect to database..."
	echo "Will now print the output from the database"
	psql -d metadb -c "INSERT INTO users (user_name, password, user_type, auth_type, last_login, last_project) VALUES('$user', '$encrypt', 'admin', 'Local', 0, '')"
	status=$?
	echo "End of output"
	if [ $status != 0 ]; then
		echo "Error: Insert Failed!"
		exit 2
	else
		echo "Insert Successful"
	fi
	
	exit 0
elif [ "$CHOICE" = r ]; then
	echo -n "Enter the username to remove: "
	read user
	echo -n "Are you sure you want to delete user '$user'? (y/n): "
	read ans
	if [ "$ans" = y ]; then
		echo "Will now attempt to connect to database..."
		echo "Will now print the output from the database"
		psql -d metadb -c "delete from users where user_name='$user';"
		status=$?
		echo "End of output"
		if [ $status != 0 ]; then
			echo "Error: Remove Failed!"
			exit 2
		else
			echo "Remove Successful"
		fi
	else exit 0
	fi
	exit 0
else echo "Bad Command"
	exit 1
fi
