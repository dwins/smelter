Smelter! 

It inspects a jstools-style config file, and hosts auto-generated loader scripts for the libraries described.

It uses Maven to build:

$ mvn compile

You can also use Maven to run it.  This is the preferred mechanism until we start providing binary distributions:

$ mvn exec:java -Dexec.args="serve path/to/build.cfg"

Use ctrl-C to stop the server.

More info coming soon!

Distribution
------------
For handy, Maven-free execution, you can build a binary distribution with the smelter libraries, dependencies, and batch scripts to run the utility.  To build the distribution:

$ mvn package

This produces a ZIP archive in target/smelter-<version>-cli.zip that contains everything you need to run smelter, including Windows and Unix-style batch scripts to run smelter.  Use 'bin/smelter' with no arguments for usage information.
