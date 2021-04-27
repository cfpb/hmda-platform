package hmda.publisher.helper

import java.time.{Clock, Instant, LocalDate, ZoneId, ZoneOffset}

import hmda.publisher.validation.PublishingGuard.Period
import org.scalatest.FreeSpec

class QuarterTimeBarrierTest extends FreeSpec {

  val  quarters2020 =List(Period.y2020Q1,Period.y2020Q2,Period.y2020Q3)
  val  quarters2021 =List(Period.y2021Q1,Period.y2021Q2,Period.y2021Q3)

  "protect all quarters correctly" in {
    testQuarter(Period.y2020Q1)
    testQuarter(Period.y2020Q2)
    testQuarter(Period.y2020Q3)
    testQuarter(Period.y2021Q1)
    testQuarter(Period.y2021Q2)
    testQuarter(Period.y2021Q3)
  }

  def testQuarter(quarter: Period.Quarter) = {
    if(Period.y2020Q1 == quarter ||
      Period.y2020Q2 == quarter ||
      Period.y2020Q3 == quarter) {
      val endDate = QuarterTimeBarrier.getEndDateForQuarter(Period.y2021Q1)
      test(endDate.minusDays(100), quarter, shouldRun = false)
      test(endDate, quarter, shouldRun = false)
      test(endDate.plusDays(6), quarter, shouldRun = false)
      test(endDate.plusDays(7), quarter, shouldRun = false)
      test(endDate.plusDays(8), quarter, shouldRun = false)
      test(endDate.plusDays(100), quarter, shouldRun = false)
    } else {
      val endDate = QuarterTimeBarrier.getEndDateForQuarter(quarter)
      test(endDate.minusDays(100), quarter, shouldRun = false)
      test(endDate, quarter, shouldRun = true)
      test(endDate.plusDays(6), quarter, shouldRun = true)
      test(endDate.plusDays(7), quarter, shouldRun = true)
      test(endDate.plusDays(8), quarter, shouldRun = false)
      test(endDate.plusDays(100), quarter, shouldRun = false)
    }
  }

  def test(now: LocalDate, quarter: Period.Quarter, shouldRun: Boolean) = {
    val zoneId = ZoneId.systemDefault()
    val clock = Clock.fixed(now.atTime(12, 0).toInstant(zoneId.getRules.getOffset(Instant.now())), zoneId)
    val timeBarrier = new QuarterTimeBarrier(clock)
    val hasRun = timeBarrier.runIfStillRelevant(quarter)(()).isDefined
    if(hasRun && !shouldRun){
      fail(s"Protected code shouldn't run but it did. Date: ${now}, period: ${quarter}")
    } else if (!hasRun && shouldRun) {
      fail(s"Protected code should run but it didnt. Date: ${now}, period: ${quarter}")
    }
  }



}