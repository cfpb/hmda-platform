package hmda.model.fi

case class HmdaRowError() extends HmdaFileRow {

  override def valueOf(field: String): Any = "error"

}
