package hmda.persistence.apor

import java.time.temporal.IsoFields
import java.time.{ LocalDate, ZoneId }

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.apor.{ APOR, FixedRate, RateType, VariableRate }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor }
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import hmda.persistence.model.HmdaPersistentActor

object HmdaAPORPersistence {
  val name = "hmda-apor-persistence"

  def props(): Props = Props(new HmdaAPORPersistence)
  def createAPORPersistence(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaAPORPersistence.props(), name)
  }

  case class HmdaAPORState(fixedRate: List[APOR] = Nil, variableRate: List[APOR] = Nil) {
    def update(event: Event): HmdaAPORState = event match {
      case AporCreated(apor, rateType) => rateType match {
        case FixedRate => HmdaAPORState(apor :: fixedRate, variableRate)
        case VariableRate => HmdaAPORState(fixedRate, apor :: variableRate)
      }
    }
  }
}

class HmdaAPORPersistence extends HmdaPersistentActor {
  import HmdaAPORPersistence._

  var state = HmdaAPORState()

  override def persistenceId: String = s"$name"

  override def updateState(event: Event): Unit =
    state = state.update(event)

  override def receiveCommand: Receive = {
    case CreateApor(apor, rateType) =>
      if (state.fixedRate.contains(apor) || state.variableRate.contains(apor)) {
        sender() ! AporCreated(apor, rateType)
      } else {
        persist(AporCreated(apor, rateType)) { e =>
          log.debug(s"APOR Persisted: $e")
          updateState(e)
          sender() ! e
        }
      }

    case CalculateRateSpread(actionTakenType, amortizationType, rateType, apr, lockinDate, reverseMortgage) =>
      val amortizationTypes = (1 to 50).toList
      val firstDate = LocalDate.of(2000, 1, 3)
      val apor = if (List(1, 2, 8).contains(actionTakenType) &&
        amortizationTypes.contains(amortizationType) &&
        reverseMortgage == 2 &&
        lockinDate.isAfter(firstDate)) {
        Some(findApor(amortizationType, rateType, apr, lockinDate))
      } else {
        None
      }
      sender() ! apor

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

  private def findApor(amortizationType: Int, rateType: RateType, apr: Double, lockinDate: LocalDate): Double = {
    rateType match {
      case FixedRate =>
        calculateRateSpread(amortizationType, apr, lockinDate, state.fixedRate)
      case VariableRate =>
        calculateRateSpread(amortizationType, apr, lockinDate, state.variableRate)
    }
  }

  private def calculateRateSpread(amortizationType: Int, apr: Double, lockinDate: LocalDate, aporList: List[APOR]): Double = {
    val zoneId = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_OF_WEEK_BASED_YEAR
    val dateTime = lockinDate.atStartOfDay(zoneId)
    val week = dateTime.get(weekField)
    val aporObj = aporList.find(apor => apor.loanTerm.get(weekField) == week).getOrElse(APOR())
    val apor = aporObj.values(amortizationType)
    apr - apor
  }

}
