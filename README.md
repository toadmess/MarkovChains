Code for creation and simple navigation of n-order markov chains for use on words of text. 

The resulting graph/chain object can be (de)serialized using either a standard sqlite database or a more space efficient raw format. 

It's geared up for storing and using the resulting chain/graph on fairly resource constrained devices like mobiles. However, the *compilation* of the chain/graph isn't quite so optimised, so that's more suitable for laptops/desktops when the original source text is large. 

## Contents and dependencies:

* `graphs/src/main/java` - The main code tree for the graph model objects. All code in this tree has no mandatory dependencies.
* `compiler/src/main/java` - The main code tree for the graph compiler. This source tree has some dependencies, as detailed below.
* `src/test/java` - JUnit 4 tests

### Some useful starting points..

* `compiler/src/main/java/org/abatons/markov/compiler/GraphCompiler.java` - This contains a main method that takes text file's filenames as arguments. It'll generate the first 5 order graphs, run a little test parody using each, then save each graph in both a raw format and as a sqlite database.
* `graphs/src/main/java/org/abatons/markov/graph` - This package, and subpackages, contain all the classes for representing, navigating, (de)serializing graphs, and for running a parody.

### Dependencies:

There need be no dependencies if you just want to read and navigate raw graph objects. 

For reading/compiling graph objects from/to sqlite, you'll need a sqlite JDBC driver.
For compiling either raw or sqlite based graph objects, you'll need the commons-io library.

* `commons-io-2.4.jar` - used during graph compilation for just a single utility method to read lines of the source text into an array. Can be easily removed.
* `sqlite-jdbc-3.7.2.jar` - Sqlite JDBC driver, if using sqlite for persistence.
* junit 4 - For the unit tests

## Limitations:

* The main one that springs to mind is that only 65536 unique words can be captured in the source text (where different capitalisations of the same word are considered different words). Mind you, running this code over the complete works of Shakespeare from Project Gutenburg only required about 54664 unique words to capture the complete graph.
* It currently only works on words, but this could be tweaked at a future date.
* Some abbreviations using periods, such as abbrv., may be considered as the end of a sentence. There's a list of some abbreviations in `org.abatons.markov.compiler.SentenceReader` that can easily be added to.

## Build:

Easy way is to use Maven if you have it installed already. Just run the command `mvn clean package --projects compiler assembly:single` at the top project level. 

This will create one library jar in `graphs/target/` that contains classes for reading and navigating the graph objects. It is free from any dependencies. 

Two jars will be created under the `compiler/target` directory, which both contain the classes for compiling the graph objects. One jar is without the dependencies and the other bundles all dependencies in it for running the compiler and using sqlite without any hassle.

## Usage examples

### Usage example: For compiling

`java -jar compiler/compiler-1.0-SNAPSHOT-jar-with-dependencies.jar jabberwocky.txt`
That'll generate order 1 through 4 graphs for the text in the `jabberwocky.txt` file and create both `.raw` and `.sqlite.db` files representing the same Graph object.

### Usage example: For reading and navigating compiled graphs

Use the `GraphPersistenceRaw` or `GraphPersistenceSqlite` classes to load and instantiate a Graph object. You can test the `Graph` object by using the cheap and cheerful `Parody` class.
