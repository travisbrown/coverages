# Scala code coverage sandbox

This repository contains some Scala projects with tests. Some of these tests
are bad. We want to see whether our code coverage tools can tell us anything
useful about how bad they are. We're currently only comparing the SBT plugins
for [Scoverage](https://github.com/scoverage/sbt-scoverage) and
[JaCoCo](https://github.com/sbt/jacoco4sbt).

The following command will run the coverage tools, update the README, and
prepare the reports for publication:

```
sbt clean coverage test jacoco:cover readme make-site
```

You can add `ghpages-push-site` if you're me and want to publish the reports
to this project's GitHub Pages site.

I'll aim to add examples as I find time and think of things to try. Pull
requests are also welcome.

## Example 1

We've got this algebraic data type with an ordering:

<!-- code:Foo -->
``` scala
sealed trait Foo
case class Bar(s: String) extends Foo
case object Baz extends Foo

object Foo {
  implicit val fooOrdering: Ordering[Foo] = Ordering.fromLessThan {
    case (Bar(_), Baz) => true
    case (Bar(a), Bar(b)) => a < b
    case (_, _) => false
  }
}
```

And we've got a [ScalaCheck](https://www.scalacheck.org/) specification that
confirms that instances of the `Bar` constructor are properly ordered.

<!-- test:FooSpecification -->
``` scala
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

import Ordering.Implicits._

object FooSpecification extends Properties("Foo") {
  property("ordering Bars") = forAll { (a: String, b: String) =>
    (Bar(a): Foo) < (Bar(b): Foo) == a < b
  }
}
```

This isn't very good—we should be checking that any `Bar` precedes `Baz` as
well. Here's what our code coverage tools have to say:

* [Scoverage](https://travisbrown.github.io/coverages/examples/001/scoverage/)
* [JaCoCo](https://travisbrown.github.io/coverages/examples/001/jacoco/)

Scoverage shows exactly which cases aren't being tested, but JaCoCo isn't very
helpful here.

