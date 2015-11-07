Jetty Unix Daemon Runner
========================

Improves over existing jetty-runner with JVM daemonization.
Packaged as a single runnable jar you can use on Unix platforms
supported by JNA:

https://github.com/java-native-access/jna

For actual daemonization we use embedable daemonization library akuma:

http://akuma.kohsuke.org/


Features
--------
* Supports Java 8 on Unix
* Start/stop/restart service 
* Detaching from terminal (no need for nohup, java service wrapper...)
* All dependencies packaged as a single runnable JAR
* BSD style license


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


Author Contact and Support
--------------------------

For further information please contact
Vjekoslav Nesek (vnesek@nmote.com)
