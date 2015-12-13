#!/bin/sh
# Script will take a combination of Twitter @Usernames and a target endpoint.
# The app is reasonably smart about arguments, so order does not matter.
# Some examples would be:
#	
#	./sling.sh http://localhost:8080/_ah/api @PartyTurkey 
# 
# 	./sling.sh @MeanStreetsOMA @Truffles4Mimi @thefirstbrat12 \ 
#		@FireRescueOmaha https://shouldibeworrying.appspot.com/_ah/api \
#		@PartyTurkey @DCNE911 >> log &
#
java -jar  target/client-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./service.p12 $@ 

