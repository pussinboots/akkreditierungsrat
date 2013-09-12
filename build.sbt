name := "akkreditierung_fetcher"
 
version := "1.0-SNAPSHOT"
 
organization := "org.akkreditierung"

scalaVersion := "2.10.2"
 
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.10.0"

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.2"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "play" %% "anorm" % "2.1.4"

libraryDependencies += "com.github.seratch" %% "scalikejdbc" % "1.5.3"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.18"

libraryDependencies += "org.hsqldb" %  "hsqldb" % "[2,)"

libraryDependencies += "co.freeside" % "betamax" % "1.1.2" % "test"

libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "1.8.8" % "test"

libraryDependencies += "org.specs2" %% "specs2" % "2.2" % "test"

