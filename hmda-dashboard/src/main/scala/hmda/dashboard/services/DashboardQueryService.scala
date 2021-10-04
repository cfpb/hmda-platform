package hmda.dashboard.services

import hmda.dashboard.models._
import hmda.dashboard.repositories.PostgresRepository
import monix.eval.Task

class DashboardQueryService (repo: PostgresRepository) extends QueryService{
    override def fetchTotalFilers(year: String): Task[Seq[TotalFilers]] =
    repo.fetchTotalFilers(year)

  override def fetchTotalLars(year: String): Task[Seq[TotalLars]] =
    repo.fetchTotalLars(year)

  override def fetchSingleAttempts(year: String): Task[Seq[SingleAttempts]] =
    repo.fetchSingleAttempts(year)

  override def fetchMultipleAttempts(year: String): Task[Seq[MultipleAttempts]] =
    repo.fetchMultipleAttempts(year)

  override def fetchTopFilers(count: Int, year: String): Task[Seq[TopFilers]] =
    repo.fetchTopFilers(count,year)

  override def fetchFilerAllPeriods(lei: String): Task[Seq[FilerAllPeriods]] =
    repo.fetchFilerAllPeriods(lei)

  override def fetchFilersByLar(year: String, min_lar: Int, max_lar: Int): Task[Seq[FilersByLar]] =
    repo.fetchFilersByLar(year, min_lar, max_lar)

  override def fetchFilersCountByLar(year: String, min_lar: Int, max_lar: Int): Task[Seq[FilersCountByLar]] =
    repo.fetchFilersCountByLar(year, min_lar, max_lar)

  override def fetchSignsForLastDays(days: Int, year: String): Task[Seq[SignsForLastDays]] =
    repo.fetchSignsForLastDays(days,year)

  override def fetchFilerAttempts(count: Int, year: String): Task[Seq[FilerAttempts]] =
    repo.fetchFilerAttempts(count,year)

  override def fetchTSRecordCount(year: String): Task[Seq[TSRecordCount]] =
    repo.fetchTSRecordCount(year)

  override def fetchFilersByAgency(year: String): Task[Seq[FilersByAgency]] =
    repo.fetchFilersByAgency(year)

  override def fetchLARByAgency(year: String): Task[Seq[LarByAgency]] =
    repo.fetchLARByAgency(year)

  override def fetchTopCountiesLar(year: String, count: Int): Task[Seq[TopCountiesLar]] =
    repo.fetchTopCountiesLar(year, count)

  override def fetchLarCountByPropertyType(year: String): Task[Seq[LarCountByPropertyType]] =
    repo.fetchLarCountByPropertyType(year)

  override def fetchFilersUsingExemptionsByAgency(year: String): Task[Seq[FilersUsingExemptionByAgency]] =
    repo.fetchFilersUsingExemptionByAgency(year)

  override def fetchDenialReasonCountsByAgency(year: String): Task[Seq[DenialReasonCountsByAgency]] =
    repo.fetchDenialReasonCountsByAgency(year)

  override def fetchLarCountUsingExemptionByAgency(year: String): Task[Seq[LarCountUsingExemptionByAgency]] =
    repo.fetchLarCountUsingExemptionByAgency(year)

  override def fetchOpenEndCreditFilersByAgency(year: String): Task[Seq[OpenEndCreditByAgency]] =
    repo.fetchOpenEndCreditFilersByAgency(year)

  override def fetchOpenEndCreditLarCountByAgency(year: String): Task[Seq[OpenEndCreditLarCountByAgency]] =
    repo.fetchOpenEndCreditLarCountByAgency(year)

  override def fetchFilersWithOnlyOpenEndCreditTransactions(year: String): Task[Seq[FilersWithOnlyOpenEndCreditTransactions]] =
    repo.fetchFilersWithOnlyOpenEndCreditTransactions(year)

  override def fetchFilersWithOnlyClosedEndCreditTransactions(year: String): Task[Seq[FilersWithOnlyClosedEndCreditTransactions]] =
    repo.fetchFilersWithOnlyClosedEndCreditTransactions((year))

  override def fetchFilersListWithOnlyOpenEndCreditTransactions(year: String): Task[Seq[FilersListWithOnlyOpenEndCredit]] =
    repo.fetchFilersListWithOnlyOpenEndCreditTransactions(year)

  override def fetchFilersClaimingExemption(year: String) : Task[Seq[FilersClaimingExemption]] =
    repo.fetchFilersClaimingExemption(year)

  override def fetchListQuarterlyFilers(year: String): Task[Seq[ListQuarterlyFilers]] =
    repo.fetchListQuarterlyFilers(year)

  override def fetchQuarterlyInfo(period: String): Task[Seq[QuarterDetails]] =
    repo.fecthQuarterlyInfo(period)

  override def fetchFilersByWeekByAgency(year: String, week: Int): Task[Seq[FilersByWeekByAgency]] =
    repo.fetchFilersByWeekByAgency(year, week)

  override def fetchLarByWeekByAgency(year: String, week: Int): Task[Seq[LarByWeekByAgency]] =
    repo.fetchLarByWeekByAgency(year, week)

  override def fetchListFilersWithOnlyClosedEndCreditTransactions(year: String): Task[Seq[ListFilersWithOnlyClosedEndCreditTransactions]] =
    repo.fetchListFilersWithOnlyClosedEndCreditTransactions(year)

  override def fetchFilersCountClosedEndOriginationsByAgency(year: String, x: Int): Task[Seq[FilersCountClosedEndOriginationsByAgency]] =
    repo.fetchFilersCountClosedEndOriginationsByAgency(year, x)

  override def fetchFilersCountClosedEndOriginationsByAgencyGraterOrEqual(year: String, x: Int): Task[Seq[FilersCountClosedEndOriginationsByAgencyGraterOrEqual]] =
    repo.fetchFilersCountClosedEndOriginationsByAgencyGraterOrEqual(year, x)

  override def fetchFilersCountOpenEndOriginationsByAgency(year: String, x: Int): Task[Seq[FilersCountOpenEndOriginationsByAgency]] =
    repo.fetchFilersCountOpenEndOriginationsByAgency(year, x)

  override def fetchFilersCountOpenEndOriginationsByAgencyGraterOrEqual(year: String, x: Int): Task[Seq[FilersCountOpenEndOriginationsByAgencyGraterOrEqual]] =
    repo.fetchFilersCountOpenEndOriginationsByAgencyGraterOrEqual(year, x)

  override def fetchTopInstitutionsCountOpenEndCredit(year: String, x: Int): Task[Seq[TopInstitutionsCountOpenEndCredit]] =
    repo.fetchTopInstitutionsCountOpenEndCredit(year, x)

  override def fetchLateFilers(period: String, late: String): Task[Seq[LateFilers]] =
    repo.fetchLateFilers(period, late)

  override def fetchLateFilersByQuarter(period: String, cutoff: String): Task[Seq[LateFilers]] =
    repo.fetchLateFilersByQuarter(period, cutoff)

  override def fetchVoluntaryFilers(period: String): Task[Seq[VoluntaryFilers]] =
    repo.fetchVoluntaryFilers(period)
}
