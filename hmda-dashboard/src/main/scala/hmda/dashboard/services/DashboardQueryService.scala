package hmda.dashboard.services

import hmda.dashboard.models._
import hmda.dashboard.repositories.PostgresRepository
import monix.eval.Task

class DashboardQueryService (repo: PostgresRepository) extends QueryService{
    override def fetchTotalFilers(year: Int): Task[Seq[TotalFilers]] =
    repo.fetchTotalFilers(year)

  override def fetchTotalLars(year: Int): Task[Seq[TotalLars]] =
    repo.fetchTotalLars(year)

  override def fetchSingleAttempts(year: Int): Task[Seq[SingleAttempts]] =
    repo.fetchSingleAttempts(year)

  override def fetchMultipleAttempts(year: Int): Task[Seq[MultipleAttempts]] =
    repo.fetchMultipleAttempts(year)

  override def fetchTopFilers(count: Int, year: Int): Task[Seq[TopFilers]] =
    repo.fetchTopFilers(count,year)

  override def fetchSignsForLastDays(days: Int, year: Int): Task[Seq[SignsForLastDays]] =
    repo.fetchSignsForLastDays(days,year)

  override def fetchFilerAttempts(count: Int, year: Int): Task[Seq[FilerAttempts]] =
    repo.fetchFilerAttempts(count,year)

  override def fetchTSRecordCount(year: Int): Task[Seq[TSRecordCount]] =
    repo.fetchTSRecordCount(year)

  override def fetchFilersByAgency(year: Int): Task[Seq[FilersByAgency]] =
    repo.fetchFilersByAgency(year)

  override def fetchLARByAgency(year: Int): Task[Seq[LarByAgency]] =
    repo.fetchLARByAgency(year)

  override def fetchTopCountiesLar(year: Int, count: Int): Task[Seq[TopCountiesLar]] =
    repo.fetchTopCountiesLar(year, count)

  override def fetchLarCountByPropertyType(year: Int): Task[Seq[LarCountByPropertyType]] =
    repo.fetchLarCountByPropertyType(year)

  override def fetchFilersUsingExemptionsByAgency(year: Int): Task[Seq[FilersUsingExemptionByAgency]] =
    repo.fetchFilersUsingExemptionByAgency(year)

  override def fetchDenialReasonCountsByAgency(year: Int): Task[Seq[DenialReasonCountsByAgency]] =
    repo.fetchDenialReasonCountsByAgency(year)

  override def fetchLarCountUsingExemptionByAgency(year: Int): Task[Seq[LarCountUsingExemptionByAgency]] =
    repo.fetchLarCountUsingExemptionByAgency(year)

  override def fetchOpenEndCreditByAgency(year: Int): Task[Seq[OpenEndCreditByAgency]] =
    repo.fetchOpenEndCreditByAgency(year)
}
