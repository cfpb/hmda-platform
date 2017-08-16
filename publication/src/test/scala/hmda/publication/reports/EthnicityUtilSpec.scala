package hmda.publication.reports

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.EthnicityEnum._
import hmda.publication.reports.util.EthnicityUtil._
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.LarConverter._
import hmda.util.SourceUtils
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class EthnicityUtilSpec extends AsyncWordSpec with MustMatchers with LarGenerators with SourceUtils {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def larCollection(transformation: (LoanApplicationRegister => LoanApplicationRegister)): List[LoanApplicationRegister] = {
    lar100ListGen.sample.get.map(transformation)
  }

  def source(lars: List[LoanApplicationRegister]): Source[LoanApplicationRegisterQuery, NotUsed] = Source
    .fromIterator(() => lars.toIterator)
    .map(lar => toLoanApplicationRegisterQuery(lar))

  "'Hispanic or Latino' ethnicity filter" must {
    "include applications that meet 'Hispanic or Latino' criteria" in {
      def coAppEthnicity = Gen.oneOf(1, 3, 4, 5).sample.get
      val lars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = coAppEthnicity)
        lar.copy(applicant = applicant)
      }
      val latinoLars = filterEthnicity(source(lars), HispanicOrLatino)
      count(latinoLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Hispanic or Latino' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val nonLatinoLars1 = filterEthnicity(source(larsExcludedByApplicant), HispanicOrLatino)
      val nonLatinoLars2 = filterEthnicity(source(larsExcludedByCoApplicant), HispanicOrLatino)
      count(nonLatinoLars1).map(_ mustBe 0)
      count(nonLatinoLars2).map(_ mustBe 0)
    }
  }

  "'Not Hispanic or Latino' ethnicity filter" must {
    "include applications that meet 'Not Hispanic/Latino' criteria" in {
      def coAppEthnicity = Gen.oneOf(2, 3, 4, 5).sample.get
      val lars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = coAppEthnicity)
        lar.copy(applicant = applicant)
      }

      val nonLatinoLars = filterEthnicity(source(lars), NotHispanicOrLatino)
      count(nonLatinoLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Not Hispanic/Latino' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 1)
        lar.copy(applicant = applicant)
      }
      val latinoLars1 = filterEthnicity(source(larsExcludedByApplicant), NotHispanicOrLatino)
      val latinoLars2 = filterEthnicity(source(larsExcludedByCoApplicant), NotHispanicOrLatino)
      count(latinoLars1).map(_ mustBe 0)
      count(latinoLars2).map(_ mustBe 0)
    }
  }

  "'Not Available' ethnicity filter" must {
    "include applications that meet 'Not Available' criteria" in {
      def appEthnicity = Gen.oneOf(3, 4).sample.get
      val lars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = appEthnicity)
        lar.copy(applicant = applicant)
      }

      val notAvailableLars = filterEthnicity(source(lars), NotAvailable)
      count(notAvailableLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Not Available' criteria" in {
      def appEthnicity = Gen.oneOf(1, 2).sample.get
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = appEthnicity)
        lar.copy(applicant = applicant)
      }
      val lars = filterEthnicity(source(larsExcludedByApplicant), NotAvailable)
      count(lars).map(_ mustBe 0)
    }
  }

  "'Joint' ethnicity filter" must {
    "include applications that meet 'Joint' criteria" in {
      val lars1 = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val lars2 = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 1)
        lar.copy(applicant = applicant)
      }

      val jointLars1 = filterEthnicity(source(lars1), Joint)
      val jointLars2 = filterEthnicity(source(lars2), Joint)
      count(jointLars1).map(_ mustBe 100)
      count(jointLars2).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Joint' criteria" in {
      def ethnicity = Gen.oneOf(1, 2).sample.get
      val larsWithSameEthnicity = larCollection { lar =>
        val eth = ethnicity
        val applicant = lar.applicant.copy(ethnicity = eth, coEthnicity = eth)
        lar.copy(applicant = applicant)
      }
      val lars = filterEthnicity(source(larsWithSameEthnicity), Joint)
      count(lars).map(_ mustBe 0)
    }
  }
}
