package hmda.publication.reports

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ Applicant, LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.ActionTakenTypeEnum.{ ApplicationReceived, LoansOriginated }
import hmda.model.publication.reports.{ RaceBorrowerCharacteristic, RaceCharacteristic }
import hmda.model.publication.reports.RaceEnum._
import hmda.publication.reports.util.DispositionType.{ OriginatedDisp, ReceivedDisp }
import hmda.publication.reports.util.RaceUtil._
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.LarConverter._
import hmda.util.SourceUtils
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class RaceUtilSpec extends AsyncWordSpec with MustMatchers with LarGenerators with SourceUtils {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def larCollection(transformation: (LoanApplicationRegister => LoanApplicationRegister)): List[LoanApplicationRegister] = {
    lar100ListGen.sample.get.map(transformation)
  }

  def source(lars: List[LoanApplicationRegister]): Source[LoanApplicationRegisterQuery, NotUsed] = Source
    .fromIterator(() => lars.toIterator)
    .map(lar => toLoanApplicationRegisterQuery(lar))

  def nonWhiteRaceGen = Gen.oneOf("1", "2", "3", "4").sample.get
  def coApplicantNotWhite(app: Applicant) = {
    app.copy(coRace1 = nonWhiteRaceGen.toInt, coRace2 = nonWhiteRaceGen, coRace3 = nonWhiteRaceGen,
      coRace4 = nonWhiteRaceGen, coRace5 = nonWhiteRaceGen)
  }
  def whiteOrBlank = Gen.oneOf("5", "").sample.get
  def applicantRace3to5Blank(app: Applicant) = app.copy(race3 = "", race4 = "", race5 = "")

  "'American Indian or Alaska Native' race filter" must {
    "include applications that meet 'American Indian or Alaska Native' criteria" in {
      val lars = larCollection { lar =>
        val app = lar.applicant
        val withQualifyingRace = applicantRace3to5Blank(app.copy(race1 = 1, race2 = whiteOrBlank))
        val withCoApplicant = coApplicantNotWhite(withQualifyingRace)
        lar.copy(applicant = withCoApplicant)
      }
      val nativeLars = filterRace(source(lars), AmericanIndianOrAlaskaNative)
      count(nativeLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'American Indian or Alaska Native' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(race1 = 1, race2 = "2")
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(race1 = 1, coRace1 = 5)
        lar.copy(applicant = applicant)
      }
      val nonNativeLars1 = filterRace(source(larsExcludedByApplicant), AmericanIndianOrAlaskaNative)
      val nonNativeLars2 = filterRace(source(larsExcludedByCoApplicant), AmericanIndianOrAlaskaNative)
      count(nonNativeLars1).map(_ mustBe 0)
      count(nonNativeLars2).map(_ mustBe 0)
    }
  }

  "'Asian' race filter" must {
    "include applications that meet 'Asian' criteria" in {
      val lars = larCollection { lar =>
        val app = lar.applicant
        val withQualifyingRace = applicantRace3to5Blank(app.copy(race1 = 2, race2 = whiteOrBlank))
        val withCoApplicant = coApplicantNotWhite(withQualifyingRace)
        lar.copy(applicant = withCoApplicant)
      }
      val asianLars = filterRace(source(lars), Asian)
      count(asianLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Asian' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(race1 = 2, race2 = "3")
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(race1 = 2, coRace1 = 5)
        lar.copy(applicant = applicant)
      }
      val nonAsianLars1 = filterRace(source(larsExcludedByApplicant), Asian)
      val nonAsianLars2 = filterRace(source(larsExcludedByCoApplicant), Asian)
      count(nonAsianLars1).map(_ mustBe 0)
      count(nonAsianLars2).map(_ mustBe 0)
    }
  }
  /*
  "'Black or African American' race filter" must {
    "include applications that meet 'Black or African American' criteria" in {
      val lars = larCollection { lar =>
      }
      val nativeLars = filterRace(source(lars), BlackOrAfricanAmerican)
      count(nativeLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Black or African American' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val nonBlackLars = filterRace(source(lars), BlackOrAfricanAmerican)
      count(nonBlackLars).map(_ mustBe 0)
    }
  }
  "'Hawaiian or Pacific Islander' race filter" must {
    "include applications that meet 'Hawaiian or Pacific Islander' criteria" in {
      val lars = larCollection { lar =>
      }
      val hawaiianLars = filterRace(source(lars), HawaiianOrPacific)
      count(hawaiianLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Hawaiian or Pacific Islander' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val hawaiianLars = filterRace(source(lars), HawaiianOrPacific)
      count(hawaiianLars).map(_ mustBe 0)
    }
  }
  "'White' race filter" must {
    "include applications that meet 'White' criteria" in {
      val lars = larCollection { lar =>
      }
      val whiteLars = filterRace(source(lars), White)
      count(whiteLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'White' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val nonWhiteLars = filterRace(source(lars), White)
      count(nonWhiteLars).map(_ mustBe 0)
    }
  }
  "'Not Provided' race filter" must {
    "include applications that meet 'Not Provided' criteria" in {
      val lars = larCollection { lar =>
      }
      val notProvidedLars = filterRace(source(lars), NotProvided)
      count(notProvidedLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Not Provided' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val otherLars = filterRace(source(lars), NotProvided)
      count(otherLars).map(_ mustBe 0)
    }
  }
  "'Two Or More Minority' race filter" must {
    "include applications that meet 'Two Or More Minority' criteria" in {
      val lars = larCollection { lar =>
      }
      val multiMinorityLars = filterRace(source(lars), TwoOrMoreMinority)
      count(multiMinorityLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Two Or More Minority' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val nonMultiMinority = filterRace(source(lars), TwoOrMoreMinority)
      count(nonMultiMinority).map(_ mustBe 0)
    }
  }
  "'Joint' race filter" must {
    "include applications that meet 'Joint' criteria" in {
      val lars = larCollection { lar =>
      }
      val jointLars = filterRace(source(lars), Joint)
      count(jointLars).map(_ mustBe 100)
    }
    "exclude applications that do not meet 'Joint' criteria" in {
      val larsExcludedByApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 2, coEthnicity = 3)
        lar.copy(applicant = applicant)
      }
      val larsExcludedByCoApplicant = larCollection { lar =>
        val applicant = lar.applicant.copy(ethnicity = 1, coEthnicity = 2)
        lar.copy(applicant = applicant)
      }
      val nonJointLars = filterRace(source(lars), Joint)
      count(nonJointLars).map(_ mustBe 0)
    }
  }

*/

  /*
  "ethnicityBorrowerCharacteristic" must {
    "generate a EthnicityBorrowCharacteristic with all 4 ethnicity categories and the specified dispositions" in {
      val lars = lar100ListGen.sample.get
      val dispositions = List(ReceivedDisp, OriginatedDisp)

      val resultF = ethnicityBorrowerCharacteristic(source(lars), dispositions)

      resultF.map { result =>
        result mustBe a[EthnicityBorrowerCharacteristic]

        result.ethnicities.size mustBe 4

        val firstEthCharacteristic = result.ethnicities.head
        firstEthCharacteristic mustBe a[EthnicityCharacteristic]
        firstEthCharacteristic.ethnicity mustBe HispanicOrLatino
        firstEthCharacteristic.dispositions.map(_.disposition) mustBe List(ApplicationReceived, LoansOriginated)
      }
    }
  }
  */

}
