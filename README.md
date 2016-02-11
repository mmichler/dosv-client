DoSV Client
===========

*English:*

Experimental webservice client for [Hochschulstart.de](http://www.hochschulstart.de/index.php?id=3291&L=1).
Uses JAX-WS, has no dependencies or generated code.

*German:*

Minimaler experimenteller Client für das Dialogorientierte Serviceverfahren von
[Hochschulstart.de](http://www.hochschulstart.de/?id=3291).
Nutzt JAX-WS und hat keine Dependencies sowie keinen generierten Code.
Weniger als die Hälfte aller Schnittstellen-Anwendungsfälle sind momentan implementiert.


Dependencies
------------
 * Java >= 1.7
 * Maven >= 2.2
 
Usage
-----
-----

Building the DoSV Client
------------------------
    mvn package

Configuration
-------------

Webservice configuration is passed in the constructor via a Properties object. 

Development
-----------
-----------

Testing
------

To run all tests:

    mvn test

To set up the test environment, copy `test.default.properties` to `test.properties` and customize it
to your needs.
In order to run the test cases, you need to create an account on the test portal of Hochschulstart
and set BID and BAN in `test.properties`.

Debug messages are logged on level FINE. To display them set the `debug` property to `true`.
