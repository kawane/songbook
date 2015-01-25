Songbook with Docker
=====================

This tutorial presents how to get a songbook server using [Docker](https://www.docker.com) in a few minutes.

[Docker](https://www.docker.com) is a technology that helps installing software on Linux including all dependencies in one command.
Kawane proposes a Songbook distribution with Docker ([kawane/songbook](https://registry.hub.docker.com/u/kawane/songbook/)).

To use it:

* Install docker on your server (documentation [here](https://docs.docker.com/installation/)).
* Run ```docker run -p 80:80 kawane/songbook```.

Now your server is up and running, check the [Getting Started](Getting_Started.md) to add your songs.
