package sandbox

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

import Ordering.Implicits._

object FooSpecification extends Properties("Foo") {
  property("ordering Bars") = forAll { (a: String, b: String) =>
    (Bar(a): Foo) < (Bar(b): Foo) == a < b
  }
}
