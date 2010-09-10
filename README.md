PelletServer Scala API
======================

PelletServer is a RESTful web application exposing the core OWL
reasoning services of Pellet in a web friendly style.  Offering these
services over REST means that:

* developers can use languages other than Java to do OWL reasoning;

* inference services can be used in a memory or compute constrained
  environment, e.g. on a mobile phone;

* it is much easier to develop rich client applications,
  e.g. JavaScript based web applications.

The PelletServer Scala API is a small suite of client side libraries
which take care of the low level protocol details and offer a type
safe, Scala friendly way to interact with remote PelletServer
instances.

Documentation
-------------

Maven generated documentation, including ScalaDocs are available at
http://clarkparsia.github.com/PelletServerScalaClient/.  The
PelletServer Scala API client library is currently split into three
modules:

1.  Stub generator.  Used when (re)building from source if the
    PelletServer API changes.

2.  Core.  Implements all the declared PelletServer knowledge base
    methods, and can parse all the result formats apart from RDF/XML.
    See ScalaDocs at
    http://clarkparsia.github.com/PelletServerScalaClient/scala-ps-api/scaladocs/index.html

3.  RDF/XML.  Additional module to parse RDF/XML results using the
    Jena libraries.

The core methods on a knowledge base are declared in the
com.clarkparsia.pelletserver.scala.api.KnowledgeBase class with each
method declaring a set of marker traits to determine how the results
can be parsed and processed.  See the "Choices" section below for details.

Example Scala usage
-------------------

Let's create a simple project using Scala's Simple Build Tool, sbt.
For sbt installation instructions, see
http://code.google.com/p/simple-build-tool/wiki/Setup

Create a directory for the example project:

    > mkdir PelletServer-example
    > cd PelletServer-example

Run sbt to create a skeleton project and answer some questions:

    > sbt
    Project does not exist, create new project? (y/N/s) y
    Name: PelletServer-example
    Organization: example.com
    Version [1.0]: 
    Scala version [2.7.7]: 2.8.0
    sbt version [0.7.4]:
    > quit

Now declare the dependency on scala-ps-api and its dependencies.
Create a directory project/build and a new file, PSExample.scala in
that directory:

    > mkdir project/build

Into the PSExample.scala, paste the following:

    import sbt._

    class PSProject(info: ProjectInfo) extends DefaultProject(info) {
      val git = "C&P git Maven repository" at "http://clarkparsia.github.com/PelletServerScalaClient/maven2"
      val psScalaApi = "com.clarkparsia" % "scala-ps-api" % "0.1-RC2-SNAPSHOT"
    }

and then run `sbt update` to download the required jars.

The following can be run from the console, which coupled with the new
tab-completion in Scala 2.8.0 makes testing things a bit easier.  Type
"sbt console" to bring up an interpreter with all required
dependencies already on the classpath and try the following:

    scala> import com.clarkparsia.pelletserver.scala.PelletServer
    import com...

    scala> val ps = PelletServer("http://ps.clarkparsia.com")
    INF: [console logger] dispatch: GET http://ps...

    scala> val kbs = ps.getKBs
    kbs: List[com.clarkparsia...] = List(KnowledgeBase(wine), ...)

    scala> kbs(0).consistency
    res1: (String) => com.clarkparsia...

At this point, you might notice that applying a method on a knowledge
base returns a function, rather than a result.  In this case, it's a
function which returns a `Result` with a number of marker traits:
`SparqlXmlResponse`, `SparqlResponse`, `SparqlBooleanResponse` and
`SparqlJsonResponse`.  In order to do something useful, we need to
decide what type of response we're interested in and apply some Scala
magic:

    scala> import com.clarkparsia.pelletserver.scala.results.SparqlXmlResult._
    import ...

    scala> kbs(0).consistency asSparqlXml
    INF: [console logger] dispatch: GET http://ps.clarkparsia.com/wine/consistency
    res1: scala.xml.Elem = 
    <sparql xmlns="http://www.w3.org/2005/sparql-results#">
      <head>
      </head>
      <boolean>true</boolean>
    </sparql>

or more usefully:

    scala> import com.clarkparsia.pelletserver.scala.results.SparqlBooleanResult._
    import ...

    scala> kbs(0).consistent asBoolean
    res2: Boolean = true

Choices
=======

For many of the methods on a knowledge base, PelletServer can respond
with different serializations, e.g. JSON, RDF/XML, SPARQL-XML,
SPARQL-JSON, etc.  In order to make the most of this, we provide
helper methods to parse these formats depending on how the results are
to be used.  The main intention is to be able to manage the number and
size of dependent libraries, so for instance if you need to parse and
manipulate RDF on the client side, you can add a dependency on
com.clarkparsia:scala-ps-rdf and the necessary Jena libraries will be
included for you.  On the other hand, Scala has rather good built in
support for XML, so in many cases this may be overkill and you might
get away with iterating over the results of an XML path expression.

### Plain XML results

For those methods marked as returning `RdfXmlResponse` or
`SparqlXmlResponse`, import `results.RdfXmlResult._` or
`results.SparqlXmlResult._` respectively in order to get back a
standard Scala XML object `scala.xml.Elem`, e.g.:

    scala> import com.clarkparsia.pelletserver.scala.results.RdfXmlResult._
    import ...

    scala> kbs(2).classify asRdfXml
    INF: [console logger] dispatch:...
    res3: scala.xml.Elem = 
    <rdf:RDF xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"> 
      <rdf:Description...

and

    scala> import com.clarkparsia.pelletserver.scala.results.SparqlXmlResult._
    import ...

    scala> kbs(2).query("SELECT DISTINCT ?c WHERE {[] a ?c}") asSparqlXml
    INF: ...
    res4: scala.xml.Elem = 
    <sparql xmlns="http://www.w3.org/2005/sparql-results#">
      <head>
        <variable name="c"></variable>...

### JSON responses

Methods marked with `PlainJsonResponse` can import
`results.PlainJsonResult._` which uses the databinder.dispatch (see
dispatch.databinder.ne) JSON library.  While the library is powerful,
allowing for iterating over and extracting JSON values, it does lack
some documentation, so here's a quick example and explanation:

    scala> import com.clarkparsia.pelletserver.scala.results.PlainJsonResult._
    import ...

    scala> import dispatch.json.Js._
    import ...

    scala> kbs(0).search("red") asJson
    res5: dispatch.json.JsValue = [{"hit" : {"value" :...

    scala> list(res5).map('hit ! obj andThen 'value ! str)
    res6: List[String] = List(http://www.w3.org/...

Importing `dispatch.json.Js._` brings in the functions `list`, `obj`
and `str` as well as some implicit conversions. The `list` function
does the obvious and converts a JSON array value to a Scala list if
possible.  More tricksy, `'hit ! obj` defines a function which takes a
JSON value, considers it as a dictionary/object and extracts the value
of "hit" as another JSON object; the use of the single quote in front
of "hit" is a shorthand in Scala for creating `Symbol` objects, so
for example if the JSON key contains a hyphen, you'd have to
explicitly write `Symbol("some-key")`.  Similarly, `'value ! str`
declares a function which extracts the value of the `value` key as a
string.  In the example above, these functions are composed using
`andThen` and applied to the list of search results to return a list
of strings, of URIs in this case.

### Parsing SPARQL respoonses

Methods marked with `SparqlResponse` and `SparqlBooleanResponse` can
import `results.SparqlResult._` and `results.SparqlBooleanResult._`
respectively in order to parse SPARQL XML results into either a
`List[Map[String,SparqlResult]]`, or a simple `Boolean` for ASK style
queries.

    scala> import com.clarkparsia.pelletserver.scala.results.SparqlResult._
    import ...

    scala> val q = """
         | PREFIX owl: <http://www.w3.org/2002/07/owl#> 
         | PREFIX sdl: <http://pellet.owldl.com/ns/sdle#>
         | 
         | SELECT DISTINCT ?c WHERE {
         |   ?c sdl:directSubClassOf owl:Thing
         | }"""
    q: java.lang.String = ...

    scala> kbs(1).query(q) asSparql
    INF: [console...
    res7: Seq[Map[String,com.clarkparsia...

    scala> res7.toList.flatMap(_.get("c") match {
         |   case Some(Resource(uri)) => Some(uri)
         |   case _ => None
         | })
    res8: List[java.net.URI] = List(http://www.co-ode...
