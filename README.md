# Hi OJUG!
Slide deck is on prezi at http://prezi.com/0oyzn-ralb-q/?utm_campaign=share&utm_medium=copy

# Should You Be Worried?

A mashup for neurotic residents of Omaha, Nebraska, as seen through 
twitter and a bunch of geographic APIs and regular expressions.

Live demo at https://shouldyou.be/worried/

![screenshot](https://raw.githubusercontent.com/tmcoma/shouldyou.be/master/docs/images/00-screenshot.png)

If you've ever wondered why there are lights and sirens screaming
through the neighborhood or why the "ghetto copter" is hovering above,
then you'll understand the motivation behind this mashup.  Thanks to
some folks who live tweet the police and fire dispatch radios during
the hours when mayhem typically ensues in my local area, you can quickly-ish
and easily-ish get a sense for where the action is.

This app takes advantage of the twitter streaming API, uses sausage-fingered
algorithm to extract guesses at the addresses being referenced, looks up
the appropriate map coordinates, and pushes the map coordinates to your
browser, which will then pan the map to the last event to occur.  Even on
mobile devices, it typically takes less than a second for this to happen
from the time the tweet is published.  In real terms, this obviously depends
on when events get tweeted, but it has not been uncommon for the map
to be updated long before the authorities arrive.

I've intentionally avoided capturing any end-user information, and the record
of which incidents you've clicked is tracked only in the browser's Web Storage.

Demonstrates usage of the following APIs, platforms, etc.
* Java 1.7
* Google App Engine
* Google Cloud Endpoints
* Google Cloud Datastore (w/Objectify) 
* Google Maps Javascript API
* Google Maps Geocoding API
* Twitter Streaming API
* Twitter UI Widget Thingie
* OAuth2

And, somewhat less interestingly

* JUnit
* Maven
* Regular expressions that will make you want to self-immolate
* Google Service API Keys
* Nifty Javascript animations
* Web Storage
* (coming soon: some documentation on how to front this kind of GAE project 
  with cheap domain names and SSL certs)

Sadly, due to the number of integrations here, this is not the type of project
that one can simply check out, build, and run.  The number of third party APIs
with their respective keys and URLs makes this virtually impossible.  I have
tried to be as didactic as possible so the project can be a resource to others
who may wish to do similar integrations.


## Setup Overview 


1. [A Google App Engine Project](https://console.developers.google.com)
    - Google Maps Javascript API Key
    - Google Maps Geocoding API Key
    - Google Maps Geolocation API API 
1. A registered Twitter Application (w/API Key) https://apps.twitter.com
1. JDK > 1.7
1. Maven > 3.2

Optionally:

1. Eclipse
1. A domain name (with SSL Certificates)

### Creating Your Project
In the [Google Developer's Console](https://console.developers.google.com), create a new project.


Take note of the project ID (later referenced as "app id" in some of the documentation - I will use the terms interchangeably.


On your local machine, create a new project, using this Maven Archetype

```
mvn archetype:generate -Dappengine-version=1.9.28 -Dapplication-id=your-app-id -Dfilter=com.google.appengine.archetypes:
```

You will be prompted for some naming things like groupId, as well as which archetype to use. Choose the "endpoints-skeleton-archetype" option.
```
Choose archetype:
1: remote -> com.google.appengine.archetypes:appengine-skeleton-archetype (A skeleton application with Google App Engine)
2: remote -> com.google.appengine.archetypes:endpoints-skeleton-archetype (A skeleton project using Cloud Endpoints with Google App Engine Java)
3: remote -> com.google.appengine.archetypes:guestbook-archetype (A guestbook application with Google App Engine)
4: remote -> com.google.appengine.archetypes:hello-endpoints-archetype (A simple starter application using Cloud Endpoints with Google App Engine Java)
5: remote -> com.google.appengine.archetypes:skeleton-archetype (-)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 2
```

Our project will have a server component (the part hosted on GAE) and a client component.  By convention, I choose to name my server component with a "-war" suffix, e.g. "worried-war."  The client, then, will be "worried-jar," and will be an executable jar.

*At this point I made series of changes to the pom.xml.   These aren't really functional changes, they are just to take advantage of newer Maven features, which are a bit less verbose (using the [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-source-and-target.html) to specify versions has always bugged me, and specifying the `maven.compiler.source` and `maven.compiler.target` seems to work fine).*

####  Configure API Keys
Follow the instructions in keys.properties for obtaining API keys.



