package hmda.publication.reports

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.ActionTakenTypeEnum.{ ApplicationReceived, LoansOriginated }
import hmda.model.publication.reports.{ MinorityStatusBorrowerCharacteristic, MinorityStatusCharacteristic }
import hmda.model.publication.reports.MinorityStatusEnum._
import hmda.publication.reports.util.DispositionType.{ OriginatedDisp, ReceivedDisp }
import hmda.publication.reports.util.MinorityStatusUtil._
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.LarConverter._
import hmda.util.SourceUtils
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class MinorityStatusUtilSpec extends AsyncWordSpec with MustMatchers with LarGenerators with SourceUtils {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def larCollection(transformation: (LoanApplicationRegister => LoanApplicationRegister)): List[LoanApplicationRegister] = {
    lar100ListGen.sample.get.map(transformation)
  }

  def source(lars: List[LoanApplicationRegister]): Source[LoanApplicationRegisterQuery, NotUsed] = Source
    .fromIterator(() => lars.toIterator)
    .map(lar => toLoanApplicationRegisterQuery(lar))

  "'White Non-Hispanic' minority status filter" must {
    "include applications that meet 'White Non-Hispanic' criteria" in {
      val lars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, race1 = 5)
        lar.copy(applicant = applicant)
      }

      val nonHispanicLars = filterMinorityStatus(source(lars), WhiteNonHispanic)
      count(nonHispanicLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'White Non-Hispanic' criteria" in {
      def excludedRace = Gen.oneOf(1, 2, 3, 4, 6, 7).sample.get
      def excludedEthnicity = Gen.oneOf(1, 3, 4).sample.get
      val excludedLars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = excludedEthnicity, race1 = excludedRace)
        lar.copy(applicant = applicant)
      }
      val excluded = filterMinorityStatus(source(excludedLars), WhiteNonHispanic)
      count(excluded).map(_ mustBe 0)
    }
  }

  "'Other, Including Hispanic' ethnicity filter" must {
    "include applications that meet 'Other, Including Hispanic' criteria" in {
      def appRace1 = Gen.oneOf(1, 2, 3, 4).sample.get
      def appRace2to5 = Gen.oneOf("1", "2", "3", "4", "").sample.get
      val lars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, race1 = appRace1,
          race2 = appRace2to5, race3 = appRace2to5, race4 = appRace2to5, race5 = appRace2to5)
        lar.copy(applicant = applicant)
      }

      val hispanicLars = filterMinorityStatus(source(lars), OtherIncludingHispanic)
      count(hispanicLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Other, Including Hispanic' criteria" in {
      def appEthnicity = Gen.oneOf(2, 3, 4).sample.get
      val excludedLars = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = appEthnicity, race1 = 5)
        lar.copy(applicant = applicant)
      }
      val lars = filterMinorityStatus(source(excludedLars), OtherIncludingHispanic)
      count(lars).map(_ mustBe 0)
    }
  }

  "ethnicityBorrowerCharacteristic" must {
    "generate a MinorityStatusBorrowCharacteristic with both MinorityStatus categories and the specified dispositions" in {
      val lars = lar100ListGen.sample.get
      val dispositions = List(ReceivedDisp, OriginatedDisp)

      val resultF = minorityStatusBorrowerCharacteristic(source(lars), dispositions)

      resultF.map { result =>
        result mustBe a[MinorityStatusBorrowerCharacteristic]

        result.minoritystatus.size mustBe 2

        val firstCharacteristic = result.minoritystatus.head
        firstCharacteristic mustBe a[MinorityStatusCharacteristic]
        firstCharacteristic.minorityStatus mustBe WhiteNonHispanic
        firstCharacteristic.dispositions.map(_.disposition) mustBe List(ApplicationReceived, LoansOriginated)
      }
    }
  }

}
