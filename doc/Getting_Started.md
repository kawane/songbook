Getting Started with Songbook
=============================

This tutorial will help you to get started with Songbook.

Get your server
---------------

First you must get your own server working.
Check one of this methods to start your server:

* [OpenShift](Install_On_OpenShift.md)
* [RunAbove](Install_On_RunAbove.md)
* [Docker](Install_With_Docker.md)
* [Do It Yourself](Install_DIY.md)
* ...


Write access
------------

Now that your server is up, this section presents you how to add and edit songs.

Reading is public. Writing is for the administrator, who signs in one of two
ways (environment variables of the server):

* **A shared login in front of songbook** (`USERS_URL`, for instance
  `https://users.llgmusic.net`). A reverse proxy (Caddy `forward_auth`) asks the
  login service before the admin routes (`/edit/*`, `/new`, `/delete/*`,
  `/admin*` and the POST, PUT and DELETE methods of `/songs/*`) and passes the
  identity in the `Remote-User` and `Remote-Groups` headers: `Remote-Groups`
  must contain `admin`. songbook trusts these headers only when `USERS_URL` is
  set, so the proxy must remove them from every client request and the port of
  songbook must not be reachable without the proxy. The edit pages load the
  session keeper of the login service, and *Sign out* ends the session there.
* **The administrator key** (`ADMIN_KEY_LOGIN=true`), a fallback for a server
  without the shared login or while it is down. The key is created on the first
  start in `administrator.key` in the data directory; it is never printed nor
  shown. Sign in with *Sign In* (`/signin`): the key is sent in a form, never in
  an address, and the session cookie holds a signature, not the key. A new key
  (delete the file and restart) ends every session.

With neither, nobody can write. Older versions took the key as a `?key=` URL
parameter and stored it in a cookie: both are no longer read.

Signing in as administrator adds to the navigation several buttons:

* A pen to Edit a song.
* A plus to add a new song.
* A minus to remove a song.

Edition
-------

The edition is under-construction, check the issue #5

