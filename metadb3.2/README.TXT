README.TXT FOR METADB V3.2 
----------------------------------------------------------------

(NOTE: This is the first page from the User Guide PDF)

MetaDB Development and Support

     MetaDB was designed to facilitate Digital Initiatives 	
     projects at Lafayette College, and we feel that our
     digital collections have been strengthened by its use.  
     In the hope that other institutions may benefit from our 	
     work, we have decided to release the source code for
     MetaDB v3.2 under the General Public License (GPL).
     MetaDB carries no warranty whatsoever, is used completely
     at one's own risk, and comes with absolutely no guarantee
     of support.

     That said, we intend to answer general questions by email
     so far as time allows, and we welcome community support
     from others who want to improve MetaDB, either by pointing
     out problems, suggesting new features, or by
     contributing new or improved code.  Please direct all
     correspondence to metadb@lafayette.edu, or visit our project
     page at http://digital.lafayette.edu/projects/metadb.

General Overview

     MetaDB is a web application that provides tools to automate
     and distribute the creation of metadata for digital image
     collections.  Typically, an administrative user creates and
     configures projects and manages user access to the
     system.  Once a user account has been created, the holdercan
     login and edit metadata for projects to which they have been
     assigned.  After all metadata for a given project has been 
     entered, an admin user exports project data from MetaDB, and
     loads it into the organization's forward-facing image 
     repository.

     Most image repository platforms provide basic tools for 
     creating collections and adding metadata.  MetaDB makes it
     easier to create strong data records by providing remote 
     access to users, by allowing multiple collaborators to work 
     concurrently, by including tools to improve the speed and 
     quality of metadata entry, by adding the ability to quickly 
     sort, narrow, and search image sets, by centralizing the 
     management of project settings and authority lists, and by 
     automatically generating technical metadata and access 
     derivatives.

     MetaDB has been used extensively at Lafayette College over
     the past few years, so we hope that the User Interface is 
     mostly self explanatory. This document is meant to provide
     a high-level overview of how the system works, and will 
     explain some of MetaDB's main features.  For a general 
     overview of installing MetaDB, please see 
     MetaDB-v3.2-general-install.txt.  For detailed instructions 
     about installing MetaDB on RedHat Enterprise Linux 6, 
     including configuration of all required software and 
     services, please see MetaDB-v3.2-RHEL6-install.pdf.