package hmda.persistence.apor

import java.time.LocalDate

import akka.actor.Props
import akka.testkit.TestProbe
import hmda.persistence.model.ActorSpec
import hmda.persistence.apor.HmdaAPORPersistence._
import hmda.model.apor.APORGenerator._
import hmda.model.apor.{ FixedRate, VariableRate }
import hmda.model.rateSpread.{ RateSpreadError, RateSpreadResponse }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor, FindApor, ModifyApor }
import hmda.persistence.messages.events.apor.APOREvents.{ AporCreated, AporModified }

class HmdaAPORPersistenceSpec extends ActorSpec {

  val aporPersistence = system.actorOf(Props(new HmdaAPORPersistence))
  val probe = TestProbe()

  val apor1 = APORGen.sample.get.copy(rateDate = LocalDate.of(2017, 11, 20))
  val apor2 = APORGen.sample.get.copy(rateDate = LocalDate.of(2017, 11, 27))
  val apor3 = APORGen.sample.get.copy(rateDate = LocalDate.of(2017, 12, 4))
  val apors = List(apor1, apor2, apor3)
  val state = HmdaAPORState(List(apor2, apor1), List(apor3))

  val rateSpreadRequest = CalculateRateSpread(1, 30, FixedRate, 5.12, LocalDate.of(2017, 12, 1), 2)

  "APOR Persistence" must {
    "create fixed and variable rate APOR" in {
      probe.send(aporPersistence, CreateApor(apor1, FixedRate))
      probe.expectMsg(AporCreated(apor1, FixedRate))
      probe.send(aporPersistence, CreateApor(apor2, FixedRate))
      probe.expectMsg(AporCreated(apor2, FixedRate))
      probe.send(aporPersistence, CreateApor(apor3, VariableRate))
      probe.expectMsg(AporCreated(apor3, VariableRate))
      probe.send(aporPersistence, CreateApor(apor3, VariableRate))
      probe.expectMsg(AporCreated(apor3, VariableRate))
    }
    "find APOR by date" in {
      probe.send(aporPersistence, FindApor(VariableRate, LocalDate.of(2017, 12, 4)))
      probe.expectMsg(Some(apor3))
      probe.send(aporPersistence, FindApor(FixedRate, LocalDate.of(2000, 1, 1)))
      probe.expectMsg(None)
    }
    "Retrieve current state" in {
      probe.send(aporPersistence, GetState)
      probe.expectMsg(state)
    }
    "Return Right(RateSpread(value)) for valid rate spread request" in {
      val value = 5.12 - apor2.values(30 - 1)
      val expectedRateSpread = BigDecimal(value).setScale(3, BigDecimal.RoundingMode.HALF_UP)
      probe.send(aporPersistence, rateSpreadRequest)
      probe.expectMsg(Right(RateSpreadResponse(expectedRateSpread.toString)))
    }
    "Return Right(RateSpread(value)) for valid rate spread request with loan term 1" in {
      val value = 5.12 - apor2.values(0)
      val expectedRateSpread = BigDecimal(value).setScale(3, BigDecimal.RoundingMode.HALF_UP)
      val request = rateSpreadRequest.copy(loanTerm = 1)
      probe.send(aporPersistence, request)
      probe.expectMsg(Right(RateSpreadResponse(expectedRateSpread.toString)))
    }
    "Return Right(RateSpread(value)) for valid rate spread request with loan term 50" in {
      val value = 5.12 - apor2.values(50 - 1)
      val expectedRateSpread = BigDecimal(value).setScale(3, BigDecimal.RoundingMode.HALF_UP)
      val request = rateSpreadRequest.copy(loanTerm = 50)
      probe.send(aporPersistence, request)
      probe.expectMsg(Right(RateSpreadResponse(expectedRateSpread.toString)))
    }
    "Return Right(RateSpread('NA')) for request with reverseMortgage=1" in {
      val request = rateSpreadRequest.copy(reverseMortgage = 1)
      probe.send(aporPersistence, request)
      probe.expectMsg(Right(RateSpreadResponse("NA")))
    }
    "Return Right(RateSpread('NA')) for request with actionTakenType!=1,2,8" in {
      val request = rateSpreadRequest.copy(actionTakenType = 5)
      probe.send(aporPersistence, request)
      probe.expectMsg(Right(RateSpreadResponse("NA")))
    }
    "return Left(RateSpreadError()) with message for loan term outside of 1-50" in {
      val request = rateSpreadRequest.copy(loanTerm = 51)
      probe.send(aporPersistence, request)
      probe.expectMsg(Left(RateSpreadError(400, "Loan term must be 1-50")))

      val request2 = rateSpreadRequest.copy(loanTerm = 0)
      probe.send(aporPersistence, request2)
      probe.expectMsg(Left(RateSpreadError(400, "Loan term must be 1-50")))
    }
    "return Left(RateSpreadError()) with message when lock in date is not covered by APOR data" in {
      val request = rateSpreadRequest.copy(lockInDate = LocalDate.of(2010, 11, 20))
      val expectedMessage = "Cannot calculate rate spread; APOR value not found for lock-in date 2010-11-20"
      probe.send(aporPersistence, request)
      probe.expectMsg(Left(RateSpreadError(404, expectedMessage)))
    }
    "modify fixed and variable rate APOR" in {
      val fixedDate = LocalDate.of(2017, 11, 20)
      val newFixedRateApor = APORGen.sample.get.copy(rateDate = fixedDate)
      probe.send(aporPersistence, ModifyApor(newFixedRateApor, FixedRate))
      probe.expectMsg(Some(AporModified(newFixedRateApor, FixedRate)))
      probe.send(aporPersistence, GetState)
      probe.expectMsg(HmdaAPORState(List(newFixedRateApor, apor2), List(apor3)))

      val variableDate = LocalDate.of(2017, 12, 4)
      val newVariableRateApor = APORGen.sample.get.copy(rateDate = variableDate)
      probe.send(aporPersistence, ModifyApor(newVariableRateApor, VariableRate))
      probe.expectMsg(Some(AporModified(newVariableRateApor, VariableRate)))
      probe.send(aporPersistence, GetState)
      probe.expectMsg(HmdaAPORState(List(newFixedRateApor, apor2), List(newVariableRateApor)))
    }
  }
}
