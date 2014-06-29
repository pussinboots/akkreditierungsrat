// Comment to get more information during initialization
logLevel := Level.Warn

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")