Songbook
========

[![Build Status](https://drone.io/github.com/kawane/songbook/status.png)](https://drone.io/github.com/kawane/songbook/latest)

Songbook is an extremely simple server that allows you to store and edit songs with chords and lyrics on your own server.
It helps setting you free of web site full of distracting animations to focus on the song.

It's fast and simple. There is no user management, no administration, only a minimal access control. 

Get documentation with the [Getting Started](doc/Getting_Started.md).

It aims to be easily installed on a server, including for not technical users. Songbook can be easily installed
 
* on OpenShift with the songbook cartridge [Here](doc/Install_On_OpenShift.md),
* on RunAbove [Here](doc/Install_On_RunAbove.md),
* with Docker on a Linux server (kawane/songbook) [Here](doc/Install_With_Docker.md) ,
* on any server with Java 8 [Here](doc/Install_DIY.md),
* Google App Engine (TODO archive+doc)

Not supported:

- AMS Amazon (TODO Check if Java 8 is supported).
- Heroku (TODO Check if Java 8 is supported).
- Cloudbees (TODO Check if Java 8 is supported).


OpenShift and Google App Engine provides some free solutions to test, they are sufficient for Songbook needs. 


Features
--------

The first Songbook version will include this features:

- [x] Store songs with lyrics, chords and meta information.
- [x] Add and edit song:
  - [x] Server side add and update.
  - [x] Client side editor with formatting.
  - [x] Client side add song.
  - [x] Client side remove song.
- [x] Handle two access types: viewer and editor:
  - [x] One time access administration page on install.
  - [x] Parametrized key for accesses.
- [x] Full text search on all songs.
- [x] Supports for computers, smart phones and tablets.
- [ ] Administration
  - [ ] Import/Export all songs in one archive
  - [x] Clear and rebuild index 
- [ ] Auto scroll for song.

Here are some feature for the future (partial):
- [ ] Appearance customization.
- [ ] Collections of songs (stored locally in the navigator with exchange capabilities).
- [ ] Present guitar tabs and piano chords.
- [ ] ...

Dependencies
------------

Songbook is a Java 8 application. 

Songbook uses these third party libraries:

* [Undertow](http://undertow.io) as HTTP server.
* [Apache Lucene](http://lucene.apache.org/) for indexes and searches.


![IntelliJIDEA](https://github.com/kawane/songbook/raw/master/doc/img/intellij-banner.png)<br>
[IntelliJ IDEA](http://www.jetbrains.com/idea/)<br>

thanks to Elena.

License
=======

Songbook is under Apache license check the LICENSE file for more information.

Apache lucene is under Apache licence.

Undertow is under the Apache licence.

Links
=====

It is a re-write of https://bitbucket.org/llg/songbook in Java

