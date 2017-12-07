package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.GenderEnum
import hmda.model.publication.reports.GenderEnum._

object GenderUtil {

  def filterGender(larSource: Source[LoanApplicationRegister, NotUsed], genderEnum: GenderEnum): Source[LoanApplicationRegister, NotUsed] = {
    genderEnum match {
      case Male => larSource.filter(isMale)
      case Female => larSource.filter(isFemale)
      case JointGender => larSource.filter(isJoint)
      case GenderNotAvailable => larSource.filter(isNotAvailable)
    }
  }

  private def isMale(lar: LoanApplicationRegister): Boolean =
    lar.applicant.sex == 1 && lar.applicant.coSex != 2

  private def isFemale(lar: LoanApplicationRegister): Boolean =
    lar.applicant.sex == 2 && lar.applicant.coSex != 1

  private def isJoint(lar: LoanApplicationRegister): Boolean =
    (lar.applicant.sex == 1 && lar.applicant.coSex == 2) ||
      (lar.applicant.sex == 2 && lar.applicant.coSex == 1)

  private def isNotAvailable(lar: LoanApplicationRegister): Boolean =
    lar.applicant.sex == 3

}
