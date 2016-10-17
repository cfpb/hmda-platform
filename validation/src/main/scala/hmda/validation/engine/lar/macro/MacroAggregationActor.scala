package hmda.validation.engine.lar.`macro`

import akka.actor.{ Actor, ActorLogging, Props }
import hmda.model.fi.SubmissionId
import hmda.validation.engine.lar.`macro`.MacroAggregationActor._

import scala.util.{ Failure, Success, Try }

object MacroAggregationActor {

  case class MacroData(numOfLars: Int, loanAmountSum: Long)
  case object EmptyMacroData
  case class AddMacroAggregation(submissionId: SubmissionId, value: MacroData)
  case class GetMacroData(submissionId: SubmissionId)

  def props(): Props = Props(new MacroAggregationActor)
}

class MacroAggregationActor extends Actor with ActorLogging {

  var macroAggregations = Map.empty[String, MacroData]

  override def receive: Receive = {

    case AddMacroAggregation(submissionId, macroData) =>
      macroAggregations = macroAggregations.updated(submissionId.toString, macroData)

    case GetMacroData(submissionId) =>
      val d = Try(macroAggregations(submissionId.toString))
      d match {
        case Success(data) => sender() ! data
        case Failure(_) => sender() ! EmptyMacroData
      }
  }

}
