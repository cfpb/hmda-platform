package hmda.dataBrowser.models

sealed trait Delimiter
case object Commas extends Delimiter
case object Pipes extends Delimiter

object Delimiter {
  def fileEnding(delimiter: Delimiter): String =
    delimiter match {
      case Commas => s".csv"
      case Pipes  => s".psv"
    }
}
