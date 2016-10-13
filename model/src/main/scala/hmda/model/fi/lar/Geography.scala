package hmda.model.fi.lar

case class Geography(
  msa: String,
  state: String,
  county: String,
  tract: String
)

object Geography {
  def apply(): Geography = {
    Geography("", "", "", "")
  }
}
