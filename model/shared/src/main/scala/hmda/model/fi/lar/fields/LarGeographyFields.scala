package hmda.model.fi.lar.fields

import hmda.model.fi.RecordField

object LarGeographyFields {
  val msa = RecordField("Metropolitan Statistical Area / Metropolitan Division", "")

  val state = RecordField("State Code", "")

  val county = RecordField("County Code", "")

  val tract = RecordField("Census Tract", "")
}
