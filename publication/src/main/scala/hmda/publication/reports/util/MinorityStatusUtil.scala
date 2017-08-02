package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports._
import hmda.model.publication.reports.MinorityStatusEnum.{ OtherIncludingHispanic, WhiteNonHispanic }
import hmda.publication.reports.util.DispositionType.DispositionType
import hmda.publication.reports.util.ReportUtil.calculateDispositions
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

object MinorityStatusUtil {

  def filterMinorityStatus(larSource: Source[LoanApplicationRegisterQuery, NotUsed], minorityStatus: MinorityStatusEnum): Source[LoanApplicationRegisterQuery, NotUsed] = {
    val relevantLars = larSource.filter { lar =>
      (lar.ethnicity == 2 && applicantRace1NotProvided(lar)) ||
        (applicantEthnicityNotProvided(lar) && lar.race1 == 5) ||
        (applicantEthnicityNotProvided(lar) && applicantRace1NotProvided(lar))
    }

    minorityStatus match {
      case WhiteNonHispanic => relevantLars.filter { lar =>
        lar.ethnicity == 2 && lar.race1 == 1
      }
      case OtherIncludingHispanic => relevantLars.filter { lar =>
        lar.ethnicity == 1 && applicantRacesAllNonWhite(lar)
      }
    }
  }

  private def applicantEthnicityNotProvided(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.ethnicity == 3 || lar.ethnicity == 4
  }

  private def applicantRace1NotProvided(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.race1 == 6 || lar.race1 == 7
  }

  private def applicantRacesAllNonWhite(lar: LoanApplicationRegisterQuery): Boolean = {
    val race1NonWhite = lar.race1 == 1 || lar.race1 == 2 || lar.race1 == 3 || lar.race1 == 4

    race1NonWhite &&
      lar.race2 != "5" &&
      lar.race3 != "5" &&
      lar.race4 != "5" &&
      lar.race5 != "5"
  }

  def minorityStatusBorrowerCharacteristic[as: AS, mat: MAT, ec: EC](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    applicantIncomeEnum: ApplicantIncomeEnum,
    dispositions: List[DispositionType]
  ): Future[MinorityStatusBorrowerCharacteristic] = {

    val larsWhite = filterMinorityStatus(larSource, WhiteNonHispanic)
    val larsOther = filterMinorityStatus(larSource, OtherIncludingHispanic)

    val dispWhiteF = calculateDispositions(larsWhite, dispositions)
    val dispOtherF = calculateDispositions(larsOther, dispositions)

    for {
      whiteDispositions <- dispWhiteF
      otherDispositions <- dispOtherF
    } yield {

      MinorityStatusBorrowerCharacteristic(
        List(
          MinorityStatusCharacteristic(WhiteNonHispanic, whiteDispositions),
          MinorityStatusCharacteristic(OtherIncludingHispanic, otherDispositions)
        )
      )

    }
  }

}
