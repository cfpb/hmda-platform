package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.{ EthnicityBorrowerCharacteristic, EthnicityCharacteristic, EthnicityEnum }
import hmda.model.publication.reports.EthnicityEnum._
import hmda.publication.reports.util.DispositionType.DispositionType
import hmda.publication.reports.util.ReportUtil.calculateDispositions
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

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

  def ethnicityBorrowerCharacteristic[as: AS, mat: MAT, ec: EC](
    larSource: Source[LoanApplicationRegister, NotUsed],
    dispositions: List[DispositionType]
  ): Future[EthnicityBorrowerCharacteristic] = {

    val larsHispanic = filterEthnicity(larSource, HispanicOrLatino)
    val larsNotHispanic = filterEthnicity(larSource, NotHispanicOrLatino)
    val larsNotAvailable = filterEthnicity(larSource, NotAvailable)
    val larsJoint = filterEthnicity(larSource, JointEthnicity)

    val dispHispanicF = calculateDispositions(larsHispanic, dispositions)
    val dispNotHispanicF = calculateDispositions(larsNotHispanic, dispositions)
    val dispNotAvailableF = calculateDispositions(larsNotAvailable, dispositions)
    val dispJointF = calculateDispositions(larsJoint, dispositions)

    for {
      hispanicDispositions <- dispHispanicF
      notHispanicDispositions <- dispNotHispanicF
      notAvailableDispositions <- dispNotAvailableF
      jointDispositions <- dispJointF
    } yield {

      EthnicityBorrowerCharacteristic(
        List(
          EthnicityCharacteristic(HispanicOrLatino, hispanicDispositions),
          EthnicityCharacteristic(NotHispanicOrLatino, notHispanicDispositions),
          EthnicityCharacteristic(NotAvailable, notAvailableDispositions),
          EthnicityCharacteristic(JointEthnicity, jointDispositions)
        )
      )

    }
  }

}
