package hmda.query

import hmda.model.census.Census
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Domain transfer objects used to communicate with Census API
  * These case classes are transferred over JSON via Circe
  */
package object dtos {
  case class TractCheck(tract: String)
  case class CountyCheck(county: String)
  case class TractValidated(isValid: Boolean)
  case class IndexedCensusEntry(index: String, data: Census, entryType: String)
  object IndexedCensusEntry {
    import DefaultJsonProtocol._
    implicit val jsonFormatIndexedCensusEntry
      : RootJsonFormat[IndexedCensusEntry] = jsonFormat3(
      IndexedCensusEntry.apply)
  }
}
