// Comment to get more information during initialization
logLevel := Level.Warn

//scalaVersion := "2.9.2"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// The Typesafe repository
//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
 
//resolvers += "sonatype-public" at "https://oss.sonatype.org/​content/repositories/public"

// Use the Play sbt plugin for Play projects
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

//addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.9.0")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")
//resolvers += Classpaths.typesafeResolver

//resolvers += "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"

//addSbtPlugin("reaktor" % "sbt-scct" % "0.2-SNAPSHOT")

//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")
