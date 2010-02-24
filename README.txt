Smelter! 

It inspects a jstools-style config file, and hosts auto-generated loader scripts for the libraries described.

It uses Maven to build:

$ mvn compile

You can also use Maven to run it.  This is the preferred mechanism until we start providing binary distributions:

$ mvn exec:java -Dexec.args="path/to/build.cfg"

Use ctrl-C to stop the server.

More info coming soon!
