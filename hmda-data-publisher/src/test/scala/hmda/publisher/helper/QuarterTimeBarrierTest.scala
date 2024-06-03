package hmda.publisher.helper

import hmda.publisher.helper.QuarterTimeBarrier.{getStartDateForQuarter, rulesConfig}
import hmda.publisher.query.component.YearPeriod

import java.time.{Clock, Instant, LocalDate, ZoneId}
import hmda.publisher.validation.PublishingGuard.Period
import org.scalatest._

class QuarterTimeBarrierTest extends FreeSpec {

  val  quarters2020 =List(Period.y2020Q1,Period.y2020Q2,Period.y2020Q3)
  val  quarters2021 =List(Period.y2021Q1,Period.y2021Q2,Period.y2021Q3)


  "protect all quarters correctly" in {
    testQuarter(Period.y2020Q1)
    testQuarter(Period.y2020Q2)
    testQuarter(Period.y2020Q3)
    testQuarter(Period.y2021Q1, 2021)
    testQuarter(Period.y2021Q2, 2021)
    testQuarter(Period.y2021Q3, 2021)
    testQuarter(Period.y2022Q1, 2022)
    testQuarter(Period.y2022Q2, 2022)
    testQuarter(Period.y2022Q3, 2022)
    testQuarter(Period.y2023Q1, 2023)
    testQuarter(Period.y2023Q2, 2023)
    testQuarter(Period.y2023Q3, 2023)
  }

  "has correct start date" in {
    testGetStartDateForQuarter(2023, YearPeriod.Q1, LocalDate.ofYearDay(2023, 91))
    testGetStartDateForQuarter(2023, YearPeriod.Q2, LocalDate.ofYearDay(2023, 91 + 91))
    testGetStartDateForQuarter(2023, YearPeriod.Q3, LocalDate.ofYearDay(2023, 92 + 91 + 91))
    testGetStartDateForQuarter(2022, YearPeriod.Q1, LocalDate.ofYearDay(2022, 91))
    testGetStartDateForQuarter(2022, YearPeriod.Q2, LocalDate.ofYearDay(2022, 91 + 91))
    testGetStartDateForQuarter(2022, YearPeriod.Q3, LocalDate.ofYearDay(2022, 92 + 91 + 91))
    testGetStartDateForQuarter(2021, YearPeriod.Q1, LocalDate.ofYearDay(2021, 91))
    testGetStartDateForQuarter(2021, YearPeriod.Q2, LocalDate.ofYearDay(2021, 91 + 91))
    testGetStartDateForQuarter(2021, YearPeriod.Q3, LocalDate.ofYearDay(2021, 92 + 91 + 91))
  }

  "has correct end date" in {
    testGetEndDateForQuarter(2023, YearPeriod.Q1, LocalDate.ofYearDay(2023, 181))
    testGetEndDateForQuarter(2023, YearPeriod.Q2, LocalDate.ofYearDay(2023, 182 + 91))
    testGetEndDateForQuarter(2022, YearPeriod.Q1, LocalDate.ofYearDay(2022, 181))
    testGetEndDateForQuarter(2022, YearPeriod.Q2, LocalDate.ofYearDay(2022, 182 + 91))
    testGetEndDateForQuarter(2021, YearPeriod.Q1, LocalDate.ofYearDay(2021, 181))
    testGetEndDateForQuarter(2021, YearPeriod.Q2, LocalDate.ofYearDay(2021, 182 + 91))
  }

  def testGetStartDateForQuarter(year: Int, quarter: YearPeriod, correctDay: LocalDate): Assertion  = {
      val startDate = QuarterTimeBarrier.getStartDateForQuarter(year, quarter)
      assert(startDate == correctDay)
  }

  def testGetEndDateForQuarter(year: Int, quarter: YearPeriod, correctDay: LocalDate): Assertion  = {
    val endDate = QuarterTimeBarrier.getEndDateForQuarter(year, quarter)
    assert(endDate == correctDay)
  }

  def testQuarter(quarter: Period.Quarter, actualYear: Int = 0) = {
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
      QuarterTimeBarrier.setRulesConfigWithActualYear(actualYear)
      val endDate = QuarterTimeBarrier.getEndDateForQuarter(quarter)
      test(endDate.minusDays(100), quarter, shouldRun = false, actualYear)
      test(endDate, quarter, shouldRun = true, actualYear)
      test(endDate.plusDays(6), quarter, shouldRun = true, actualYear)
      test(endDate.plusDays(7), quarter, shouldRun = true, actualYear)
      test(endDate.plusDays(8), quarter, shouldRun = false, actualYear)
      test(endDate.plusDays(100), quarter, shouldRun = false, actualYear)
    }
  }

  def test(now: LocalDate, quarter: Period.Quarter, shouldRun: Boolean, actualYear: Int = 0) = {
    val zoneId = ZoneId.systemDefault()
    val clock = Clock.fixed(now.atTime(12, 0).toInstant(zoneId.getRules.getOffset(Instant.now())), zoneId)
    val timeBarrier = new QuarterTimeBarrier(clock)
    timeBarrier.setRulesConfigWithActualYear(actualYear)
    val hasRun = timeBarrier.runIfStillRelevant(quarter)(()).isDefined
    if(hasRun && !shouldRun){
      fail(s"Protected code shouldn't run but it did. Date: ${now}, period: ${quarter}")
    } else if (!hasRun && shouldRun) {
      fail(s"Protected code should run but it didnt. Date: ${now}, period: ${quarter}")
    }
  }


}