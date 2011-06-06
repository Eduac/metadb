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
# Simple script to download and uncompress required jar files. Update as needed.
# Uses standard curl, unzip, rm, and cp commands, which are included with most linux distributions.
#

# download files, and unzip tgz files
curl http://archive.apache.org/dist/commons/fileupload/binaries/commons-fileupload-1.2.1-bin.tar.gz |tar xvz
curl http://archive.apache.org/dist/commons/lang/binaries/commons-lang-2.4-bin.tar.gz |tar xvz
curl http://archive.apache.org/dist/commons/io/binaries/commons-io-1.4-bin.tar.gz |tar xvz
curl http://archive.apache.org/dist/commons/sanselan/binaries/apache-sanselan-incubating-0.97-bin.tar.gz |tar xvz
curl http://download.java.net/media/jai/builds/release/1_1_3/jai-1_1_3-lib-linux-i586.tar.gz |tar xvz
curl http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-linux-i586.tar.gz |tar xvz
curl -O http://voxel.dl.sourceforge.net/project/javacsv/JavaCsv/JavaCsv%202.1/javacsv2.1.zip

# unzip one zip file
unzip javacsv2.1.zip -d javacsv2.1
rm javacsv2.1.zip

# copy jar files
cp commons-fileupload-1.2.1/lib/commons-fileupload-1.2.1.jar .
cp commons-io-1.4/commons-io-1.4.jar .
cp sanselan-0.97-incubator/sanselan-0.97-incubator.jar .
cp commons-lang-2.4/commons-lang-2.4.jar .
cp jai-1_1_3/lib/jai*.jar .
cp jai_imageio-1_1/lib/jai_imageio.jar .
cp javacsv2.1/javacsv.jar .

# delete source dirs
rm -r commons-fileupload-1.2.1/
rm -r commons-io-1.4/
rm -r sanselan-0.97-incubator/
rm -r commons-lang-2.4/
rm -r jai-1_1_3/
rm -r jai_imageio-1_1/
rm -r javacsv2.1/
