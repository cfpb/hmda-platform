package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.{ ApplicantIncomeEnum, EthnicityBorrowerCharacteristic, EthnicityCharacteristic, EthnicityEnum }
import hmda.model.publication.reports.EthnicityEnum._
import hmda.publication.reports.util.DispositionType.DispositionType
import hmda.publication.reports.util.ReportUtil.calculateDispositions
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

object EthnicityUtil {

  def filterEthnicity(larSource: Source[LoanApplicationRegisterQuery, NotUsed], ethnicity: EthnicityEnum): Source[LoanApplicationRegisterQuery, NotUsed] = {
    ethnicity match {
      case HispanicOrLatino => larSource.filter { lar =>
        lar.ethnicity == 1 &&
          (lar.coEthnicity == 1 || coapplicantEthnicityNotProvided(lar))
      }
      case NotHispanicOrLatino => larSource.filter { lar =>
        lar.ethnicity == 2 &&
          (lar.coEthnicity == 2 || coapplicantEthnicityNotProvided(lar))
      }
      case Joint => larSource.filter { lar =>
        (lar.ethnicity == 1 && lar.coEthnicity == 2) ||
          (lar.ethnicity == 2 && lar.coEthnicity == 1)
      }
      case NotAvailable => larSource.filter { lar =>
        lar.ethnicity == 3 || lar.ethnicity == 4
      }
    }
  }

  def coapplicantEthnicityNotProvided(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.coEthnicity == 3 ||
      lar.coEthnicity == 4 ||
      lar.coEthnicity == 5
  }

  def ethnicityBorrowerCharacteristic[as: AS, mat: MAT, ec: EC](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    dispositions: List[DispositionType]
  ): Future[EthnicityBorrowerCharacteristic] = {

    val larsHispanic = filterEthnicity(larSource, HispanicOrLatino)
    val larsNotHispanic = filterEthnicity(larSource, NotHispanicOrLatino)
    val larsNotAvailable = filterEthnicity(larSource, NotAvailable)
    val larsJoint = filterEthnicity(larSource, Joint)

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
          EthnicityCharacteristic(Joint, jointDispositions)
        )
      )

    }
  }

}
