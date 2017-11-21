package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.ApplicantIncomeEnum.LessThan50PercentOfMSAMedian
import hmda.model.publication.reports.{ EthnicityBorrowerCharacteristic, MSAReport, MinorityStatusBorrowerCharacteristic, RaceBorrowerCharacteristic }
import hmda.publication.reports.util.DispositionType._
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.Future

class D51Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "98765"
  val fips = 18700 //Corvallis, OR
  def loanType = Gen.oneOf(2, 3, 4).sample.get
  def propType = Gen.oneOf(1, 2).sample.get

  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = loanType, propertyType = propType, purpose = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val expectedDispositionNames =
    List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted, ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)
      .map(_.value)

  "Generate a Disclosure 5-1 report" in {
    D51.generate(source, fips, respId, Future("Corvallis Test Bank")).map { result =>

      result.msa mustBe MSAReport("18700", "Corvallis, OR", "OR", "Oregon")
      result.table mustBe "5-1"
      result.respondentId mustBe "98765"
      result.institutionName mustBe "Corvallis Test Bank"
      result.applicantIncomes.size mustBe 5

      val lowestIncome = result.applicantIncomes.head
      lowestIncome.applicantIncome mustBe LessThan50PercentOfMSAMedian

      val races = lowestIncome.borrowerCharacteristics.head.asInstanceOf[RaceBorrowerCharacteristic].races
      races.size mustBe 8

      val ethnicities = lowestIncome.borrowerCharacteristics(1).asInstanceOf[EthnicityBorrowerCharacteristic].ethnicities
      ethnicities.size mustBe 4

      val minorityStatuses = lowestIncome.borrowerCharacteristics(2).asInstanceOf[MinorityStatusBorrowerCharacteristic].minoritystatus
      minorityStatuses.size mustBe 2

      races.head.dispositions.map(_.dispositionName) mustBe expectedDispositionNames
    }
  }

}
