Songbook Do It Yourself
=======================

This tutorial presents how to get a songbook server on your own server.

Java 8
------

Songbook is meant to be easy to install and it is, except for one thing: Java 8.
We really wanted to test out the new language features. 
They are ground breaking.
There are lots of tutorial online to install java 8 depending on your platform.
For Windows or MacOS X, just go to Oracle Java download [page](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) and install it.
For linux, it depends on your distribution. 
Here are some tutorials for some distributions:

* Ubuntu (here)[http://ubuntuhandbook.org/index.php/2013/07/install-oracle-java-6-7-8-on-ubuntu-13-10/] or (here)[http://www.devsniper.com/install-jdk-8-on-ubuntu/].
* Debian (here)[http://linuxg.net/how-to-install-the-oracle-java-8-on-debian-wheezy-and-debian-jessie-via-repository/] or (here)[http://tutorialforlinux.com/2014/03/26/how-to-install-oracle-jdk-8-on-debian-squeeze-6-32-64bit-easy-guide/].
* Fedora, CentOS and RHEL (here)[http://tecadmin.net/install-java-8-on-centos-rhel-and-fedora/] or (here)[http://tutorialforlinux.com/2014/03/16/how-to-install-oracle-jdk-8-on-fedora-16-17-18-19-20-21-3264bit-linux-easy-guide/].

 
Linux
-----

Create a directory where to install songbook

```Shell
mkdir songbook
cd songbook
```

Download and unzip Vert.x.

```Shell
wget http://dl.bintray.com/vertx/downloads/vert.x-2.1.2.tar.gz
tar xfz vert.x-2.1.2.tar.gz
rm -f vert.x-2.1.2.tar.gz
```

Download and unzip Songbook.

```Shell
wget https://github.com/llgcode/songbook/release/download/0.2.0/songbook.zip
unzip songbook.zip
rm -f songbook.zip
```

Run Songbook

```Shell
export HOST=localhost
export PORT=8080
export WEB_ROOT=$PWD/songbook/web
export DATA_ROOT=$PWD/songbook/data
export CLASSPATH=:$PWD/songbook/jar/songbook.jar
./vert.x-2.1.2/bin/vertx run songbook.server.Server
```

MacOS X
--------

Create a directory where to install songbook

```Shell
mkdir songbook
cd songbook
```

Download and unzip Vert.x.

```Shell
curl -O http://dl.bintray.com/vertx/downloads/vert.x-2.1.2.zip
unzip vert.x-2.1.2.zip
rm -f vert.x-2.1.2.zip
```

Download and unzip Songbook.

```Shell
curl -O https://github.com/llgcode/songbook/release/download/0.2.0/songbook.zip
unzip songbook.zip
rm -f songbook.zip
```

Run Songbook

```Shell
export HOST=localhost
export PORT=8080
export WEB_ROOT=$PWD/songbook/web
export DATA_ROOT=$PWD/songbook/data
export CLASSPATH=:$PWD/songbook/jar/songbook.jar
./vert.x-2.1.2/bin/vertx run songbook.server.Server
```

Windows
-------

TODO

Songbook
--------

Now your server is up and running, check the [Getting Started](Getting_Started.md) to add your songs.
