package hmda.model.fi.lar

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class Geography(
  msa: String,
  state: String,
  county: String,
  tract: String
)
