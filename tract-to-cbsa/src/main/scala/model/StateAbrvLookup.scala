package model

import hmda.model.ResourceUtils

object StateAbrvLookup extends ResourceUtils with CbsaResourceUtils {
  val values: Seq[StateAbrv] = {
    val lines = resourceLinesIso("/state.csv")

    lines.drop(1).map { line =>
      val values = line.split('|').map(_.trim)
      val stateFips = values(0)
      val stateAbrv = values(1)
      val stateName = values(2)
      val stereEns = values(3)

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
