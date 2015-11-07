Jetty Daemon Runner
===================

Ever wanted a servlet engine in a single runnabable JAR that could 
correctly daemonize and run WAR from a command line? 

Jetty  Daemon Runner improves over existing jetty-runner with JVM daemonization.
It's packaged as a single runnable jar you can use on Unix platforms
supported by Akuma/JNA.

[Latest release 0.5](https://github.com/vnesek/jetty-daemon-runner/releases/v0.5)

Usage
-----

* Start Jetty at port 8080 serving WAR at / as a daemon in background

```sh
	java -jar jetty-daemon.jar --start --pid some.pid some.war 
```

* Stop Jetty using pid file

```sh
	java -jar jetty-daemon.jar --stop --pid some.pid 
```

Features
--------
* Supports Java 8 on Unix
* Start/stop/restart service 
* Detaching from terminal (no need for nohup, java service wrapper...)
* All dependencies packaged as a single runnable JAR
* BSD style license

Command line options
--------------------

```
Addition daemon server opts:
 --start                             - detach from a terminal and run in background
 --stop                              - stop running server by pid
 --restart                           - restarts server by pid
 --pid file                          - PID file
 --chdir dir                         - change running directory

Usage: java [-Djetty.home=dir] -jar jetty-runner.jar [--help|--version] [ server opts] [[ context opts] context ...] 
Server opts:
 --version                           - display version and exit
 --log file                          - request log filename (with optional 'yyyy_mm_dd' wildcard
 --out file                          - info/warn/debug log filename (with optional 'yyyy_mm_dd' wildcard
 --host name|ip                      - interface to listen on (default is all interfaces)
 --port n                            - port to listen on (default 8080)
 --stop-port n                       - port to listen for stop command
 --stop-key n                        - security string for stop command (required if --stop-port is present)
 [--jar file]*n                      - each tuple specifies an extra jar to be added to the classloader
 [--lib dir]*n                       - each tuple specifies an extra directory of jars to be added to the classloader
 [--classes dir]*n                   - each tuple specifies an extra directory of classes to be added to the classloader
 --stats [unsecure|realm.properties] - enable stats gathering servlet context
 [--config file]*n                   - each tuple specifies the name of a jetty xml config file to apply (in the order defined)
Context opts:
 [[--path /path] context]*n          - WAR file, web app dir or context xml file, optionally with a context path
```


References
----------

* [Akuma](http://akuma.kohsuke.org/) for JVM daemonization
* [JNA](https://github.com/java-native-access/jna) for native code access 
* [Jetty Runner](http://www.eclipse.org/jetty/documentation/current/runner.html) favorite servlet engine

Author Contact and Support
--------------------------

For further information please contact
Vjekoslav Nesek (vnesek@nmote.com)
