package hmda.dataBrowser.models

import enumeratum._

import scala.collection.immutable

sealed abstract class HealthCheckStatus(override val entryName: String)
    extends EnumEntry

object HealthCheckStatus
    extends Enum[HealthCheckStatus]
    with CirceEnum[HealthCheckStatus] {
  val values: immutable.IndexedSeq[HealthCheckStatus] = findValues

  case object Up extends HealthCheckStatus("up")
  case object Down extends HealthCheckStatus("down")
}
