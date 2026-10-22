Songbook with Docker
=====================

This tutorial presents how to get a songbook server using [Docker](https://www.docker.com) in a few minutes.

[Docker](https://www.docker.com) is a technology that helps installing software on Linux including all dependencies in one command.
Kawane proposes a Songbook distribution with Docker ([kawane/songbook](https://registry.hub.docker.com/u/kawane/songbook/)).

To use it:

* Install docker on your server (documentation [here](https://docs.docker.com/installation/)).
* Creates a folder where to store data for instance ```/data_on_host```.
* Run ```docker run -p 80:8000 -v /data_on_host:/data -e ADMIN_KEY_LOGIN=true kawane/songbook```.
  `ADMIN_KEY_LOGIN=true` creates the administrator key in `/data_on_host/administrator.key`; behind a
  shared login, set `USERS_URL` instead (see [Getting Started](Getting_Started.md#write-access)).

Now your server is up and running, check the [Getting Started](Getting_Started.md) to add your songs.
