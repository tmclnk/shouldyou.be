# shouldyou.be
A mashup for neurotic Omahans.

## Setup Overview 
This section should give you some idea of how to get started with a similar project on your own.  I can only speak from my own experience, this is not meant to be an authoritative guide.

What you will need:

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

[create new project](docs/images/01)


Take note of the project ID (later referenced as "app id" in some of the documentation - I will use the terms interchangeably.


[create new project](docs/images/02)

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

#### Client

