package hmda.persistence.processing

import akka.actor.Props
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.persistence.messages.CommonMessages.{ GetState, Shutdown }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFiling._

class HmdaFilingSpec extends ActorSpec with LarGenerators {

  val period = "2017"

  val lars10 = get10SampleLars
  val lars20 = get10SampleLars ++ get10SampleLars

  val probe = TestProbe()

  val config = ConfigFactory.load()
  val multipleLarNumber = config.getInt("hmda.journal.snapshot.counter")

  "HMDA Filing" must {
    val hmdaFiling = createHmdaFiling(system, period)
    "Store 10 lars" in {
      for (lar <- lars10) {
        probe.send(hmdaFiling, LarValidated(lar, SubmissionId("12345", period, 0)))
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(Map(s"12345-$period-0" -> 10)))
    }
    "Store additional 20 lars" in {
      for (lar <- lars20) {
        probe.send(hmdaFiling, LarValidated(lar, SubmissionId("9999", period, 1)))
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(Map(s"12345-$period-0" -> 10, s"9999-$period-1" -> 20)))
    }
    "Store 1000 more lars, and recover state from snapshot" in {
      for (lar <- get1000SampleLars) {
        probe.send(hmdaFiling, LarValidated(lar, SubmissionId("9999", period, 1)))
      }
      val finalState = HmdaFilingState(Map(s"12345-$period-0" -> 10, s"9999-$period-1" -> 1020))
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(finalState)
      probe.send(hmdaFiling, Shutdown)
      val hmdaFiling2 = system.actorOf(Props(new HmdaFiling(period)))
      probe.send(hmdaFiling2, GetState)
      probe.expectMsg(finalState)
    }
  }

  private def get10SampleLars = larListGen.sample.getOrElse(List[LoanApplicationRegister]())

  private def get1000SampleLars = larNGen(multipleLarNumber).sample.getOrElse(List[LoanApplicationRegister]())
}
