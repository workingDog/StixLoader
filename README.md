## An App to load STIX-2.0 objects

**StixLoader** is a desktop application that loads [STIX-2.0](https://oasis-open.github.io/cti-documentation/) 
objects from various input storage systems to other output systems. The aim of **StixLoader** is to convert STIX-2
domain objects (SDO) and relationships (SRO) from and to; files, MongoDB, Neo4j and PostgreSQL. **StixLoader** provides a graphical user interface 
for choosing the STIX-2 data input and output systems.
    
### Installation and packaging

To compile from source and assemble the application and all its dependencies into a single fat jar file, use [SBT](http://www.scala-sbt.org/) and type:

    sbt assembly

This will produce a big jar file called *stixloader-1.0.jar* in the *./target/scala-2.12* directory. 
    
### Usage

Using [SBT](http://www.scala-sbt.org/) directly (without having to generate a jar file) type:

    sbt run
 
Using Java to launch **StixLoader**, type at a command prompt:
 
    java -jar stixloader-1.0.jar

This will display the main GUI of the application.

![Alt text](/stixloader.png?raw=true "StixLoader")

Select the data from the **From** list, then select a destination from the **To** list.
**StixLoader** will convert and load the data as per the selections when the *Convert* button is clicked. 
Some basic log information can be viewd in the *Log info* tab.

### Dependencies and requirements

Depends specifically on the [ScalaStix](https://github.com/workingDog/scalastix) and 
[StixToNeoLib](https://github.com/workingDog/StixToNeoLib).

See also the *build.sbt* file.

Java 8 is required.
                       
### References
 
1) [Neo4j](https://neo4j.com/)

2) [Java Neo4j API](https://neo4j.com/docs/java-reference/current/javadocs/)

3) [ScalaStix](https://github.com/workingDog/scalastix)

4) [StixToNeoLib](https://github.com/workingDog/StixToNeoLib)

5) [STIX-2](https://oasis-open.github.io/cti-documentation/)

6) [MongoDB](https://www.mongodb.com/)

7) [PostgreSQL](https://www.postgresql.org/)

### Status

Very early stage of work in progress.

Currently, only converts and loads STIX-2 from: file and MongoDB to: file, MongoDB and Neo4j.


