import com.typesafe.sbt.SbtStartScript

name := "akkreditierung_fetcher"

version := "1.0-SNAPSHOT"

organization := "org.akkreditierung"

scalaVersion := "2.10.2"

mainClass in Compile := Some("org.akkreditierung.ui.Start")

seq(SbtStartScript.startScriptForClassesSettings: _*)

//seq(webSettings :_*)

//ui dependencies

libraryDependencies += "org.apache.wicket" % "wicket-core" % "6.6.0"

libraryDependencies += "org.apache.wicket" % "wicket-extensions" % "6.6.0"

libraryDependencies += "org.apache.wicket" % "wicket-datetime" % "6.6.0"

libraryDependencies += "org.avaje" % "ebean" % "2.7.7"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5"

resolvers += "apache snaphots" at "https://repository.apache.org/content/repositories/snapshots"

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.servlet" % "2.5.0.v201103041518" artifacts Artifact("javax.servlet", "jar", "jar")

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.security.auth.message" % "1.0.0.v201108011116" artifacts Artifact("javax.security.auth.message", "jar", "jar")

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.mail.glassfish" % "1.4.1.v201005082020" artifacts Artifact("javax.mail.glassfish", "jar", "jar")

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.activation" % "1.1.0.v201105071233" artifacts Artifact("javax.activation", "jar", "jar")

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.annotation" % "1.1.0.v201108011116" artifacts Artifact("javax.annotation", "jar", "jar")

libraryDependencies += "org.eclipse.jetty.aggregate" % "jetty-all-server" % "7.6.3.v20120416"

// fetcher dependencies
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.10.0"

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.2"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "play" %% "anorm" % "2.1.4"

libraryDependencies += "com.github.seratch" %% "scalikejdbc" % "1.5.3"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.18"

libraryDependencies += "org.hsqldb" %  "hsqldb" % "[2,)"

// test dependencies

libraryDependencies += "co.freeside" % "betamax" % "1.1.2" % "test"

libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "1.8.8" % "test"

libraryDependencies += "org.specs2" %% "specs2" % "2.2" % "test"

