package hmda.dashboard.services

import hmda.dashboard.models._
import monix.eval.Task

trait QueryService {
  def fetchTotalFilers(period: String) : Task[Seq[TotalFilers]]
  def fetchTotalLars(period: String) : Task[Seq[TotalLars]]
  def fetchSingleAttempts(period: String) : Task[Seq[SingleAttempts]]
  def fetchMultipleAttempts(period: String) : Task[Seq[MultipleAttempts]]
  def fetchTopFilers(count: Int, period: String) : Task[Seq[TopFilers]]
  def fetchSignsForLastDays(days: Int, period: String) : Task[Seq[SignsForLastDays]]
  def fetchFilerAttempts(count: Int, period: String) : Task[Seq[FilerAttempts]]
  def fetchTSRecordCount(period: String) : Task[Seq[TSRecordCount]]
  def fetchFilersByAgency(period: String) : Task[Seq[FilersByAgency]]
  def fetchLARByAgency(period: String) : Task[Seq[LarByAgency]]
  def fetchTopCountiesLar(period: String, count: Int) : Task[Seq[TopCountiesLar]]
  def fetchLarCountByPropertyType(period: String) : Task[Seq[LarCountByPropertyType]]
  def fetchFilersUsingExemptionsByAgency(period: String) : Task[Seq[FilersUsingExemptionByAgency]]
  def fetchDenialReasonCountsByAgency(period: String) : Task[Seq[DenialReasonCountsByAgency]]
  def fetchLarCountUsingExemptionByAgency(period: String) : Task[Seq[LarCountUsingExemptionByAgency]]
  def fetchOpenEndCreditFilersByAgency(period: String) : Task[Seq[OpenEndCreditByAgency]]
  def fetchOpenEndCreditLarCountByAgency(period: String) : Task[Seq[OpenEndCreditLarCountByAgency]]
  def fetchFilersWithOnlyOpenEndCreditTransactions(period: String) : Task[Seq[FilersWithOnlyOpenEndCreditTransactions]]
  def fetchFilersWithOnlyClosedEndCreditTransactions(period: String) : Task[Seq[FilersWithOnlyClosedEndCreditTransactions]]
  def fetchFilersListWithOnlyOpenEndCreditTransactions(period: String) : Task[Seq[FilersListWithOnlyOpenEndCredit]]
  def fetchFilersClaimingExemption(period: String) : Task[Seq[FilersClaimingExemption]]
  def fetchListQuarterlyFilers(period: String) : Task[Seq[ListQuarterlyFilers]]
  def fetchFilersByWeekByAgency(period: String, week: Int) : Task[Seq[FilersByWeekByAgency]]
  def fetchLarByWeekByAgency(period: String, week: Int) : Task[Seq[LarByWeekByAgency]]
  def fetchListFilersWithOnlyClosedEndCreditTransactions(period: String) : Task[Seq[ListFilersWithOnlyClosedEndCreditTransactions]]
  def fetchFilersCountClosedEndOriginationsByAgency(period: String, x: Int) : Task[Seq[FilersCountClosedEndOriginationsByAgency]]
  def fetchFilersCountClosedEndOriginationsByAgencyGraterOrEqual(period: String, x: Int) : Task[Seq[FilersCountClosedEndOriginationsByAgencyGraterOrEqual]]
  def fetchFilersCountOpenEndOriginationsByAgency(period: String, x: Int) : Task[Seq[FilersCountOpenEndOriginationsByAgency]]
  def fetchFilersCountOpenEndOriginationsByAgencyGraterOrEqual(period: String, x: Int) : Task[Seq[FilersCountOpenEndOriginationsByAgencyGraterOrEqual]]
  def fetchTopInstitutionsCountOpenEndCredit(period: String, x: Int) : Task[Seq[TopInstitutionsCountOpenEndCredit]]
}
