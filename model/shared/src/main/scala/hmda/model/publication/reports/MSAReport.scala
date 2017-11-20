package hmda.model.publication.reports

case class MSAReport(
    id: String,
    name: String,
    state: String,
    stateName: String
) {
  def toJsonFormat: String = {
    s"""{
       "id": "$id",
       "name": "$name",
       "state": "$state",
       "stateName": "$stateName"
     }"""
  }
}
