package hmda.dataBrowser.models

import enumeratum._

import scala.collection.immutable

sealed trait State extends EnumEntry

object State extends Enum[State] {
  val values: immutable.IndexedSeq[State] = findValues

  case object AL extends State
  case object AK extends State
  case object AZ extends State
  case object AR extends State
  case object CA extends State
  case object CO extends State
  case object CT extends State
  case object DE extends State
  case object DC extends State
  case object FL extends State
  case object GA extends State
  case object HI extends State
  case object ID extends State
  case object IL extends State
  case object IN extends State
  case object IA extends State
  case object KS extends State
  case object KY extends State
  case object LA extends State
  case object ME extends State
  case object MD extends State
  case object MA extends State
  case object MI extends State
  case object MN extends State
  case object MS extends State
  case object MO extends State
  case object MT extends State
  case object NE extends State
  case object NV extends State
  case object NH extends State
  case object NJ extends State
  case object NM extends State
  case object NY extends State
  case object NC extends State
  case object ND extends State
  case object OH extends State
  case object OK extends State
  case object OR extends State
  case object PA extends State
  case object RI extends State
  case object SC extends State
  case object SD extends State
  case object TN extends State
  case object TX extends State
  case object UT extends State
  case object VT extends State
  case object VA extends State
  case object WA extends State
  case object WV extends State
  case object WI extends State
  case object WY extends State
  case object AS extends State
  case object GU extends State
  case object MP extends State
  case object PR extends State
  case object VI extends State
  case object UM extends State
  case object FM extends State
  case object MH extends State
  case object PW extends State

  def validateStates(states: Seq[String]): Either[Seq[String], Seq[State]] = {
    val potentialStates: Seq[(String, Option[State])] =
      states.map(state => (state, State.withNameInsensitiveOption(state)))
    val isValidState = potentialStates.map(_._2).forall(_.isDefined)
    if (isValidState) Right(potentialStates.flatMap(_._2))
    else
      Left(potentialStates.collect {
        case (input, None) => input
      })
  }
}
