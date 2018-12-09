package hmda.census.api.http.model

import hmda.model.census.Census

case class CensusResponse(censuses: Seq[Census])
