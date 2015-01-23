package sandbox

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
