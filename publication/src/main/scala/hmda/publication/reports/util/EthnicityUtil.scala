package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.EthnicityEnum
import hmda.model.publication.reports.EthnicityEnum._

object EthnicityUtil {

  def filterEthnicity(larSource: Source[LoanApplicationRegister, NotUsed], ethnicity: EthnicityEnum): Source[LoanApplicationRegister, NotUsed] = {
    ethnicity match {
      case HispanicOrLatino => larSource.filter { lar =>
        lar.applicant.ethnicity == 1 &&
          (lar.applicant.coEthnicity == 1 || coapplicantEthnicityNotProvided(lar))
      }
      case NotHispanicOrLatino => larSource.filter { lar =>
        lar.applicant.ethnicity == 2 &&
          (lar.applicant.coEthnicity == 2 || coapplicantEthnicityNotProvided(lar))
      }
      case JointEthnicity => larSource.filter { lar =>
        (lar.applicant.ethnicity == 1 && lar.applicant.coEthnicity == 2) ||
          (lar.applicant.ethnicity == 2 && lar.applicant.coEthnicity == 1)
      }
      case NotAvailable => larSource.filter { lar =>
        lar.applicant.ethnicity == 3 || lar.applicant.ethnicity == 4
      }
    }
  }

  def coapplicantEthnicityNotProvided(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.coEthnicity == 3 ||
      lar.applicant.coEthnicity == 4 ||
      lar.applicant.coEthnicity == 5
  }

}
