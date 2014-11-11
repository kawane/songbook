# Songbook on OpenShift

[OpenShift](https://openshift.redhat.com) is a service that allows to install application on their cloud platform.
It's an open platform supported by [RedHat](https://www.redhat.com/en).

This tutorial presents how to get a SongBook server on [OpenShift](https://openshift.redhat.com) in a few minutes.
With this tutorial, you will create an application containing a SongBook server on OpenShift servers for using a free account.

The [demo](http://songbookdemo.kawane.ovh) is running on OpenShift.

## Create an OpenShift account

In order to have access to application creation with OpenShift, you must create an account.
Go to [OpenShift Account Creation](https://openshift.redhat.com/app/account/new?then=%2Fapp%2Faccount&web_user) and create one.
Then you'll have access to a free account allowing you to create 3 small gears. A small gear is more than enough for SongBook.

![AccountCreation](img/openshift/AccountCreation.png)


## Create an application

You now have to create an application.

![AccountInformation](img/openshift/AccountInformation.png)

On your account information click on *Add Application...*.

![ChooseApplication1](img/openshift/ChooseApplication1.png)

On the *Choose the type of application* page, go at the bottom of the page.

![ChooseApplication2](img/openshift/ChooseApplication2.png)

Copy the SongBook cartridge URL: `https://github.com/kawane/songbook-cartridge/raw/master/metadata/manifest.yml` in the *Code Anything* section and click *Next*.

![ConfigureApplication](img/openshift/ConfigureApplication.png)

On the *Configure application* page, set a name (here `songbook`) and click on 'Create Application'.

![NextSteps](img/openshift/NextSteps.png)

To access to your server, go on the *Applications* page.
Now your server is up and running, check the [Getting Started](Getting_Started.md) to add your songs.


## For advanced users

If you already use OpenShift, you can use the `rhc` command:


```shell
rhc create-app https://github.com/kawane/songbook-cartridge/raw/master/metadata/manifest.yml -a mysongbook
```


