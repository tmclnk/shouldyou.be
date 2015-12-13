Use sling.sh as directed in the script.

To test basic connectivity locally, if you have maven available you can do

```
mvn exec:java
```

This will connect to the twitter sample feed and use random addresses,
but should be sufficient to verify that API keys are correct.  Saves
some of the hassle of having to go manually feed a twitter account.
