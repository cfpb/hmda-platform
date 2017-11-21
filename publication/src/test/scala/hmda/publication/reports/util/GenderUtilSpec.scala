package hmda.publication.reports.util

import hmda.model.publication.reports.GenderEnum._
import hmda.publication.reports.util.GenderUtil._
import hmda.util.SourceUtils
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class GenderUtilSpec extends AsyncWordSpec with MustMatchers with SourceUtils with ApplicantSpecUtil {

  def notMale = Gen.oneOf(2, 3, 4, 5).sample.get
  def notFemale = Gen.oneOf(1, 3, 4, 5).sample.get

  "'Male' gender filter" must {
    "include applications with a male applicant and no female coapplicant" in {
      val lars = larCollectionWithApplicant(_.copy(sex = 1, coSex = notFemale))
      val maleLars = filterGender(source(lars), Male)
      count(maleLars).map(_ mustBe 100)
    }
    "exclude lars where applicant not male" in {
      val excludedLars = larCollectionWithApplicant(_.copy(sex = notMale))
      val nonMaleLars = filterGender(source(excludedLars), Male)
      count(nonMaleLars).map(_ mustBe 0)
    }
    "exclude lars where applicant is female and coApplicant is female" in {
      val excludedLars = larCollectionWithApplicant(_.copy(sex = 1, coSex = 2))
      val nonMaleLars = filterGender(source(excludedLars), Male)
      count(nonMaleLars).map(_ mustBe 0)
    }
  }

  "'Female' gender filter" must {
    "include applications with a female applicant and no male coapplicant" in {
      val lars = larCollectionWithApplicant(_.copy(sex = 2, coSex = notMale))
      val maleLars = filterGender(source(lars), Female)
      count(maleLars).map(_ mustBe 100)
    }
    "exclude lars where applicant not female" in {
      val excludedLars = larCollectionWithApplicant(_.copy(sex = notFemale))
      val nonFemaleLars = filterGender(source(excludedLars), Female)
      count(nonFemaleLars).map(_ mustBe 0)
    }
    "exclude lars where applicant is female and coApplicant is male" in {
      val excludedLars = larCollectionWithApplicant(_.copy(sex = 2, coSex = 1))
      val nonFemaleLars = filterGender(source(excludedLars), Female)
      count(nonFemaleLars).map(_ mustBe 0)
    }
  }

  "'Joint' gender filter" must {
    "include applications with female applicant and male coapplicant" in {
      val lars = larCollectionWithApplicant(_.copy(sex = 2, coSex = 1))
      val jointLars = filterGender(source(lars), JointGender)
      count(jointLars).map(_ mustBe 100)
    }
    "include applications with male applicant and female coapplicant" in {
      val lars = larCollectionWithApplicant(_.copy(sex = 1, coSex = 2))
      val jointLars = filterGender(source(lars), JointGender)
      count(jointLars).map(_ mustBe 100)
    }
    "exclude lars where applicant is female and coapplicant not male" in {
      val excludedLars = larCollectionWithApplicant(_.copy(sex = 2, coSex = notMale))
      val nonJointLars = filterGender(source(excludedLars), JointGender)
      count(nonJointLars).map(_ mustBe 0)
    }
    "exclude lars where applicant is male and coapplicant not female" in {
      val excludedLars = larCollectionWithApplicant(_.copy(sex = 1, coSex = notFemale))
      val nonJointLars = filterGender(source(excludedLars), JointGender)
      count(nonJointLars).map(_ mustBe 0)
    }

  }

}
