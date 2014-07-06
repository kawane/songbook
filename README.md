songbook
========

Songbook is a extremely simple server that allows to store and edit songs chords and lyrics on our own server. 
It helps setting you free of web site full of distracting animations to focus on the song. 

It aims to be easily installed on a server, including for not technical users. Songbook can be easily installed
 
* on OpenShift with the songbook cartridge (TODO cartridge+doc),
* Google App Engine (TODO archive+doc)
* with Docker on a Linux server (kawane/songbook) (TODO post image+doc),
* on any server with Java 8 (TODO doc),

OpenShift is a really simple solution to get started, check this doc/Install_On_OpenShift.md#head.

Not supported:

* AMS Amazon (TODO Check if Java 8 is supported).
* Heroku (TODO Check if Java 8 is supported).
* Cloudbees (TODO Check if Java 8 is supported).




OpenShift and Google App Engine provides some free solutions 


Features
--------




Dependencies
============

Songbook is a Java 8 application. 

Songbook uses these third party libraries:

* [Vert.x](http://vertx.io/) as HTTP server.
* [Apache Lucene](http://lucene.apache.org/) for indexes and searches.


![IntelliJIDEA](https://github.com/llgcode/songbook/raw/master/data/intellij-banner.png)<br>
[IntelliJ IDEA](http://www.jetbrains.com/idea/)<br>

thanks to Elena.

License
=======

Songbook is under Apache license check the LICENSE file for more information.

Apache lucene is under Apache licence.

Vert.x is under the Apache licence.

Links
=====

It is a re-write of https://bitbucket.org/llg/songbook in Java

