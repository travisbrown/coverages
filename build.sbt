import com.typesafe.sbt.SbtSite.SiteKeys._

val build = jacoco.settings ++ Seq(
  scalaVersion := "2.11.5",
  crossScalaVersions := Seq("2.10.4", "2.11.5")
)

val scalacheck = Seq(
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
)

lazy val root = project.in(file("."))
  .settings(site.settings: _*)
  .settings(
    siteMappings := {
      // This is a horrible mess; I don't care.
      (baseDirectory.value * "ex*").get.flatMap { path =>
        val id = path.getName.drop(2)
        val jacoco = Path.allSubpaths(
          path / "target" / "scala-2.11" / "jacoco" / "html"
        ).toSeq.map {
          case (f, p) => (f, s"examples/$id/jacoco/" + p)
        }
        val scoverage = Path.allSubpaths(
          path / "target" / "scala-2.11" / "scoverage-report"
        ).toSeq.map {
          case (f, p) => (f, s"examples/$id/scoverage/" + p)
        }
        jacoco ++ scoverage
      }
    }
  )
  .settings(ghpages.settings: _*)
  .settings(
    git.remoteRepo := "git@github.com:travisbrown/coverages.git"
  ).aggregate(
    ex001
  )

lazy val ex001 = project.settings(build: _*).settings(scalacheck: _*)

lazy val readme = taskKey[Unit]("Copy code snippets to README")

readme := {
  import scala.io.Source

  val in = Source.fromFile("README.markdown")

  sealed trait State {
    def lines: Seq[String]
    def addLine(line: String): State
  }

  case class OutsideExample(lines: Seq[String]) extends State {
    def addLine(line: String) = copy(lines = lines :+ line)
  }

  case class InExample(id: Int, lines: Seq[String]) extends State {
    def addLine(line: String) = copy(lines = lines :+ line)
  }

  case class InCodeBlock(id: Int, lines: Seq[String]) extends State {
    def addLine(line: String) = this
  }

  val ExampleHeader = "^## Example (\\d+).*$".r
  val CodeBlock = "^<!-- code:(\\S+) -->$".r
  val TestBlock = "^<!-- test:(\\S+) -->$".r
  val FenceStart = "^``` scala$".r
  val FenceEnd = "^```$".r

  val readmeLines = in.getLines.foldLeft(OutsideExample(Nil): State) {
    case (state, line @ ExampleHeader(id)) => InExample(id.toInt, state.lines :+ line)
    case (InExample(id, lines), line @ CodeBlock(name)) =>
      val source = Source.fromFile(f"ex$id%03d/src/main/scala/sandbox/$name.scala")
      val sourceLines = source.getLines.drop(2).toList
      source.close()
      InCodeBlock(id, lines ++ (line +: "``` scala" +: sourceLines :+ "```"))
    case (InExample(id, lines), line @ TestBlock(name)) =>
      val source = Source.fromFile(f"ex$id%03d/src/test/scala/sandbox/$name.scala")
      val sourceLines = source.getLines.drop(2).toList
      source.close()
      InCodeBlock(id, lines ++ (line +: "``` scala" +: sourceLines :+ "```"))
    case (InCodeBlock(id, lines), FenceEnd()) => InExample(id, lines)
    case (state @ InCodeBlock(_, _), _) => state
    case (state, line) => state.addLine(line)
  }

  in.close()

  val writer = new java.io.PrintWriter("README.markdown")
  readmeLines.lines.foreach(writer.println)
  writer.close()
}

