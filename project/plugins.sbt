// Comment to get more information during initialization
logLevel := Level.Warn

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

<<<<<<< HEAD
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")
=======
//addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.9.0")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")
//resolvers += Classpaths.typesafeResolver

//resolvers += "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"

//addSbtPlugin("reaktor" % "sbt-scct" % "0.2-SNAPSHOT")

//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")
>>>>>>> 818a5a74ca1e70fbcd582e1372a48004ab23712b
