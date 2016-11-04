package hmda.census.model

// This file contains a linking of state fips codes and state abbreviations
// site: https://www.census.gov/geo/reference/ansi_statetables.html
// file: http://www2.census.gov/geo/docs/reference/state.txt

object StateAbrvLookup extends CbsaResourceUtils {
  val values: Seq[StateAbrv] = {
    val lines = resourceLines("/state.csv", "ISO-8859-1")

    lines.drop(1).map { line =>
      val values = line.split('|').map(_.trim)
      val stateFips = values(0)
      val stateAbrv = values(1)
      val stateName = values(2)
      val stateEns = values(3)

      StateAbrv(
        stateFips,
        stateAbrv,
        stateName
      )
    }.toSeq
  }
}

case class StateAbrv(
  state: String = "",
  stateAbrv: String = "",
  stateName: String = ""
)
