package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports._
import hmda.model.publication.reports.MinorityStatusEnum.{ OtherIncludingHispanic, WhiteNonHispanic }

object MinorityStatusUtil {

  def filterMinorityStatus(larSource: Source[LoanApplicationRegister, NotUsed], minorityStatus: MinorityStatusEnum): Source[LoanApplicationRegister, NotUsed] = {
    minorityStatus match {
      case WhiteNonHispanic => larSource.filter { lar =>
        lar.applicant.ethnicity == 2 && lar.applicant.race1 == 5
      }
      case OtherIncludingHispanic => larSource.filter { lar =>
        lar.applicant.ethnicity == 1 && applicantRacesAllNonWhite(lar)
      }
    }
  }

  private def applicantRacesAllNonWhite(lar: LoanApplicationRegister): Boolean = {
    val race1NonWhite = lar.applicant.race1 == 1 || lar.applicant.race1 == 2 || lar.applicant.race1 == 3 || lar.applicant.race1 == 4

    race1NonWhite &&
      lar.applicant.race2 != "5" &&
      lar.applicant.race3 != "5" &&
      lar.applicant.race4 != "5" &&
      lar.applicant.race5 != "5"
  }

}
