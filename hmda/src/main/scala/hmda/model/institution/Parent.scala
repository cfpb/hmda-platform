package hmda.model.institution

object Parent {
  def empty: Parent = Parent(None, None)
}

case class Parent(
    idRssd: Option[Int],
    name: Option[String]
)
