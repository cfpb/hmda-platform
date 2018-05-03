package hmda.model.filing

trait HmdaFileRow {
  def valueOf(field: String): Any
}
