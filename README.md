songbook
========

Song book web site in Java. 

License
=======

Song book is under Apache license check the LICENSE file for more information.

Mysql Database
==============

(MACOSX) If you have installed the Startup Item, use this command:

     shell> sudo /Library/StartupItems/MySQLCOM/MySQLCOM start

If you don't use the Startup Item, enter the following command sequence:

     shell> cd /usr/local/mysql
     shell> sudo ./bin/mysqld_safe
     (ENTER YOUR PASSWORD, IF NECESSARY)
     (PRESS CONTROL-Z)
     shell> bg

create database songbook;


Notes
=====
When downloading latest version of Jetty for running songbook download also the latest version of **asm 5.0.2** so that
it uses latest version byte code analyser.

Replace the asm 4.0.x by this one in libs/annotation jetty folder

Links
=====
It is a re-write of https://bitbucket.org/llg/songbook in Java


