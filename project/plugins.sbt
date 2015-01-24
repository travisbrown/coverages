resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "jgit-repo" at "http://download.eclipse.org/jgit/maven"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.8.1")
addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.6")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

