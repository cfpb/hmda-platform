package hmda.dashboard.services

import hmda.dashboard.models._
import monix.eval.Task

trait QueryService {
  def fetchTotalFilers(year: Int) : Task[Seq[TotalFilers]]
  def fetchTotalLars(year: Int) : Task[Seq[TotalLars]]
  def fetchSingleAttempts(year: Int) : Task[Seq[SingleAttempts]]
  def fetchMultipleAttempts(year: Int) : Task[Seq[MultipleAttempts]]
  def fetchTopFilers(count: Int, year: Int) : Task[Seq[TopFilers]]
  def fetchSignsForLastDays(days: Int, year: Int) : Task[Seq[SignsForLastDays]]
  def fetchFilerAttempts(count: Int, year: Int) : Task[Seq[FilerAttempts]]
  def fetchTSRecordCount(year: Int) : Task[Seq[TSRecordCount]]
  def fetchFilersByAgency(year: Int) : Task[Seq[FilersByAgency]]
  def fetchLARByAgency(year: Int) : Task[Seq[LarByAgency]]
  def fetchTopCountiesLar(count: Int, year: Int) : Task[Seq[TopCountiesLar]]
  def fetchLarCountByPropertyType(year: Int) : Task[Seq[LarCountByPropertyType]]
  def fetchFilersUsingExemptionsByAgency(year: Int) : Task[Seq[FilersUsingExemptionByAgency]]
  def fetchDenialReasonCountsByAgency(year: Int) : Task[Seq[DenialReasonCountsByAgency]]
  def fetchLarCountUsingExemptionByAgency(year: Int) : Task[Seq[LarCountUsingExemptionByAgency]]
  def fetchOpenEndCreditFilersByAgency(year: Int) : Task[Seq[OpenEndCreditByAgency]]
}
