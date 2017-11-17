package hmda.publication.reports.util

import hmda.model.publication.reports.EthnicityEnum._
import hmda.model.publication.reports.{ EthnicityBorrowerCharacteristic, EthnicityCharacteristic }
import hmda.publication.reports.util.DispositionType.{ LoansOriginated, ApplicationReceived }
import hmda.publication.reports.util.EthnicityUtil._
import hmda.util.SourceUtils
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class EthnicityUtilSpec extends AsyncWordSpec with MustMatchers with SourceUtils with ApplicantSpecUtil {

  "'Hispanic or Latino' ethnicity filter" must {
    "include applications that meet 'Hispanic or Latino' criteria" in {
      def coAppEthnicity = Gen.oneOf(1, 3, 4, 5).sample.get
      val lars = larCollectionWithApplicant(_.copy(ethnicity = 1, coEthnicity = coAppEthnicity))
      val latinoLars = filterEthnicity(source(lars), HispanicOrLatino)
      count(latinoLars).map(_ mustBe 100)
    }
    "exclude applications where applicant does not meet criteria" in {
      val larsExcludedByApplicant = larCollectionWithApplicant(_.copy(ethnicity = 2, coEthnicity = 3))
      val nonLatinoLars1 = filterEthnicity(source(larsExcludedByApplicant), HispanicOrLatino)
      count(nonLatinoLars1).map(_ mustBe 0)
    }
    "exclude applications where coApplicant does not meet criteria" in {
      val larsExcludedByCoApplicant = larCollectionWithApplicant(_.copy(ethnicity = 1, coEthnicity = 2))
      val nonLatinoLars2 = filterEthnicity(source(larsExcludedByCoApplicant), HispanicOrLatino)
      count(nonLatinoLars2).map(_ mustBe 0)
    }
  }

  "'Not Hispanic or Latino' ethnicity filter" must {
    "include applications that meet 'Not Hispanic/Latino' criteria" in {
      def coAppEthnicity = Gen.oneOf(2, 3, 4, 5).sample.get
      val lars = larCollectionWithApplicant(_.copy(ethnicity = 2, coEthnicity = coAppEthnicity))
      val nonLatinoLars = filterEthnicity(source(lars), NotHispanicOrLatino)
      count(nonLatinoLars).map(_ mustBe 100)
    }
    "exclude applications where applicant does not meet criteria" in {
      val larsExcludedByApplicant = larCollectionWithApplicant(_.copy(ethnicity = 1, coEthnicity = 3))
      val latinoLars1 = filterEthnicity(source(larsExcludedByApplicant), NotHispanicOrLatino)
      count(latinoLars1).map(_ mustBe 0)
    }
    "exclude applications where coApplicant does not meet criteria" in {
      val larsExcludedByCoApplicant = larCollectionWithApplicant(_.copy(ethnicity = 2, coEthnicity = 1))
      val latinoLars2 = filterEthnicity(source(larsExcludedByCoApplicant), NotHispanicOrLatino)
      count(latinoLars2).map(_ mustBe 0)
    }
  }

  "'Not Available' ethnicity filter" must {
    "include applications that meet 'Not Available' criteria" in {
      def appEthnicity = Gen.oneOf(3, 4).sample.get
      val lars = larCollectionWithApplicant(_.copy(ethnicity = appEthnicity))

      val notAvailableLars = filterEthnicity(source(lars), NotAvailable)
      count(notAvailableLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Not Available' criteria" in {
      def appEthnicity = Gen.oneOf(1, 2).sample.get
      val larsExcludedByApplicant = larCollectionWithApplicant(_.copy(ethnicity = appEthnicity))
      val lars = filterEthnicity(source(larsExcludedByApplicant), NotAvailable)
      count(lars).map(_ mustBe 0)
    }
  }

  "'Joint' ethnicity filter" must {
    "include applications with hispanic applicant and non-hispanic coApplicant" in {
      val lars1 = larCollectionWithApplicant(_.copy(ethnicity = 1, coEthnicity = 2))
      val jointLars1 = filterEthnicity(source(lars1), JointEthnicity)
      count(jointLars1).map(_ mustBe 100)
    }
    "include applications with non-hispanic applicant and hispanic coApplicant" in {
      val lars2 = larCollectionWithApplicant(_.copy(ethnicity = 2, coEthnicity = 1))
      val jointLars2 = filterEthnicity(source(lars2), JointEthnicity)
      count(jointLars2).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Joint' criteria" in {
      def ethnicity = Gen.oneOf(1, 2).sample.get
      val larsWithSameEthnicity = larCollectionWithApplicant { app =>
        val eth = ethnicity
        app.copy(ethnicity = eth, coEthnicity = eth)
      }
      val lars = filterEthnicity(source(larsWithSameEthnicity), JointEthnicity)
      count(lars).map(_ mustBe 0)
    }
  }

  "ethnicityBorrowerCharacteristic" must {
    "generate a EthnicityBorrowCharacteristic with all 4 ethnicity categories and the specified dispositions" in {
      val lars = lar100ListGen.sample.get
      val dispositions = List(ApplicationReceived, LoansOriginated)

      val resultF = ethnicityBorrowerCharacteristic(source(lars), dispositions)

      resultF.map { result =>
        result mustBe a[EthnicityBorrowerCharacteristic]

        result.ethnicities.size mustBe 4

        val firstEthCharacteristic = result.ethnicities.head
        firstEthCharacteristic mustBe a[EthnicityCharacteristic]
        firstEthCharacteristic.ethnicity mustBe HispanicOrLatino
        firstEthCharacteristic.dispositions.map(_.dispositionName) mustBe
          List(ApplicationReceived.value, LoansOriginated.value)
      }
    }
  }

}
