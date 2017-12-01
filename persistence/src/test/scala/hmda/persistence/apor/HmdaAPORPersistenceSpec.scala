package hmda.persistence.apor

import java.time.LocalDate

import akka.actor.Props
import akka.testkit.TestProbe
import hmda.persistence.model.ActorSpec
import hmda.persistence.apor.HmdaAPORPersistence._
import hmda.model.apor.APORGenerator._
import hmda.model.apor.{ FixedRate, VariableRate }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor }
import hmda.persistence.messages.events.apor.APOREvents.AporCreated

class HmdaAPORPersistenceSpec extends ActorSpec {

  val aporPersistence = system.actorOf(Props(new HmdaAPORPersistence))
  val probe = TestProbe()

  val apor1 = APORGen.sample.get.copy(loanTerm = LocalDate.of(2017, 11, 20))
  val apor2 = APORGen.sample.get.copy(loanTerm = LocalDate.of(2017, 11, 27))
  val apor3 = APORGen.sample.get.copy(loanTerm = LocalDate.of(2017, 12, 4))
  val apors = List(apor1, apor2, apor3)
  val state = HmdaAPORState(List(apor2, apor1), List(apor3))

  "APOR Persistence" must {
    "create fixed and variable rate APOR" in {
      probe.send(aporPersistence, CreateApor(apor1, FixedRate))
      probe.expectMsg(AporCreated(apor1, FixedRate))
      probe.send(aporPersistence, CreateApor(apor2, FixedRate))
      probe.expectMsg(AporCreated(apor2, FixedRate))
      probe.send(aporPersistence, CreateApor(apor3, VariableRate))
      probe.expectMsg(AporCreated(apor3, VariableRate))
    }
    "Retrieve current state" in {
      probe.send(aporPersistence, GetState)
      probe.expectMsg(state)
    }
    "Calculate rate spread" in {
      val apr = 5.12
      val amortizationType = 30
      val expectedRateSpread = apr - apor2.values(amortizationType)
      val dec1 = LocalDate.of(2017, 12, 1)
      val message = CalculateRateSpread(1, amortizationType, FixedRate, apr, dec1, 2)
      probe.send(aporPersistence, message)
      probe.expectMsg(Some(expectedRateSpread))
      val failActionType = CalculateRateSpread(3, amortizationType, FixedRate, apr, dec1, 2)
      probe.send(aporPersistence, failActionType)
      probe.expectMsg(None)
      val failReverseMortgage = CalculateRateSpread(1, amortizationType, FixedRate, apr, dec1, 1)
      probe.send(aporPersistence, failReverseMortgage)
      probe.expectMsg(None)
      val failAmortizationType = CalculateRateSpread(1, 100, FixedRate, apr, dec1, 2)
      probe.send(aporPersistence, failAmortizationType)
      probe.expectMsg(None)
    }
    "Recover state after actor is killed" in {
      probe.send(aporPersistence, Shutdown)
      val aporPersistence2 = system.actorOf(Props(new HmdaAPORPersistence))
      probe.send(aporPersistence2, GetState)
      probe.expectMsg(state)
    }
  }
}
