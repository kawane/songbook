Songbook
========

Visit [Songbook](http://www.minibilles.org/songbook/index.html) website. 

Songbook is an extremely simple server that allows you to store and edit songs with chords and lyrics on your own server.
It helps setting you free of web site full of distracting animations to focus on the song.

It's fast and simple. There is no user management, no administration, only a minimal access control. 

It aims to be easily installed on a server, including for not technical users. Songbook can be easily installed
 
* on OpenShift with the songbook cartridge (TODO cartridge+doc),
* Google App Engine (TODO archive+doc)
* with Docker on a Linux server (kawane/songbook) (TODO post image+doc),
* on any server with Java 8 (TODO doc),

OpenShift is a really simple solution to get started, check [this](doc/Install_On_OpenShift.md).

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
  - [x] Client side editor.
- [x] Handle two access types: viewer and editor:
  - [ ] One time access administration page on install.
  - [x] Parametrized key for accesses.
- [x] Full text search on all songs.
- [x] Supports for computers, smart phones and tablets.
- [ ] Auto scroll for song.

Here are some feature for the future (partial):
- [ ] Appearance customization.
- [ ] Collections of songs (stored locally in the navigator with exchange capabilities).
- [ ] Present guitar tabs and piano chords.
- [ ] ...


### TODO

- [ ] Remove edition for non-admins.
- [ ] Add action to add a song.
- [ ] Work on song edition, content editable isn't sufficient, needs chord identification.
- [ ] Save index on hardrive.

### Bugs

- [ ] Index isn't updated after edit
- [ ] Uncaught TypeError: Cannot read property 'insertBefore' of undefined (songbook.ts:79)
- [ ] 'key=null' appears

Dependencies
------------

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

