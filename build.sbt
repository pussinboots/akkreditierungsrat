import com.typesafe.sbt.SbtStartScript

name := "akkreditierung_fetcher"

version in ThisBuild := "0.2"

organization := "org.akkreditierung"

scalaVersion in ThisBuild := "2.10.2"

mainClass in Compile := Some("org.akkreditierung.ui.Start")

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(ScctPlugin.instrumentSettings : _*)

parallelExecution in Test := false

//ui dependencies
libraryDependencies ++= Seq(
    "org.apache.wicket" % "wicket-core" % "6.6.0",
    "org.apache.wicket" % "wicket-extensions" % "6.6.0",
    "org.apache.wicket" % "wicket-datetime" % "6.6.0",
    "org.avaje" % "ebean" % "2.7.7",
    "javax.servlet" % "servlet-api" % "2.5"
)

//embedded jetty dependencies
libraryDependencies ++= Seq(
    "org.eclipse.jetty.orbit" % "javax.servlet" % "2.5.0.v201103041518" artifacts Artifact("javax.servlet", "jar", "jar"),
    "org.eclipse.jetty.orbit" % "javax.security.auth.message" % "1.0.0.v201108011116" artifacts Artifact("javax.security.auth.message", "jar", "jar"),
    "org.eclipse.jetty.orbit" % "javax.mail.glassfish" % "1.4.1.v201005082020" artifacts Artifact("javax.mail.glassfish", "jar", "jar"),
    "org.eclipse.jetty.orbit" % "javax.activation" % "1.1.0.v201105071233" artifacts Artifact("javax.activation", "jar", "jar"),
    "org.eclipse.jetty.orbit" % "javax.annotation" % "1.1.0.v201108011116" artifacts Artifact("javax.annotation", "jar", "jar"),
    "org.eclipse.jetty.aggregate" % "jetty-all-server" % "7.6.3.v20120416"
)

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"

// fetcher dependencies
libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
    "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.2",
    "org.apache.commons" % "commons-lang3" % "3.1",
    "play" %% "anorm" % "2.1.4",
    "com.github.seratch" %% "scalikejdbc" % "1.5.3",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "org.hsqldb" %  "hsqldb" % "[2,)"
)

resolvers += "enhancedwickettester" at "http://enhancedwickettester.googlecode.com/svn/repo"

// test dependencies
libraryDependencies ++= Seq(
    "co.freeside" % "betamax" % "1.1.2" % "test",
    "org.codehaus.groovy" % "groovy-all" % "1.8.8" % "test",
    "org.specs2" %% "specs2" % "2.2" % "test"
)
