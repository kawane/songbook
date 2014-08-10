Songbook on OpenShift
=====================

This tutorial presents how to get a songbook server on OpenShift in 5 minutes

**Note** While Songbook uses Java 8 and Openshift doesn't native support for it yet. We use an home made cartridge to
  deploy Songbook. To create an app with an home made cartridge the tool *rhc* is required.

TODO:
* Add link to doc to create an OpenShift account
* Add link to doc to install rhc commands



```shell
rhc create-app https://github.com/kawane/songbook-cartridge/raw/master/metadata/manifest.yml -a mysongbook
```

Now your server is up and running, check the [Getting Started](Getting_Started.md) to add your songs.
