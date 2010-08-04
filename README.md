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

and then run "sbt update" to download the required jars.

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

At this point, you might notice that running a method on a knowledge
base returns a function, rather than a result.  In this case, it's a
function which returns a Result with a number of marker traits:
SparqlXmlResponse, SparqlResponse, SparqlBooleanResponse and
SparqlJsonResponse.  In order to do something useful, we need to
decide what type of response we're interested in and apply some Scala
magic:

    scala> import com.clarkparsia.pelletserver.scala.results.SparqlResult._
    import ...

    scala> kbs(0).consistency asSparql
    INF: [console logger] dispatch: GET http://ps.clarkparsia.com/wine/consistency
    res1: Seq[Map[String,com.clarkparsia.pelletserver.scala.results.SparqlResult]] = List(Map((Consistent,Literal(true))))

or more usefully:

    scala> import com.clarkparsia.pelletserver.scala.results.SparqlBooleanResult._
    import ...

    scala> kbs(0).consistent asBoolean
    res2: Boolean = true

