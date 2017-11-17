package hmda.publication.reports.national

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.census.model.CbsaLookup
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.ApplicantIncomeEnum.LessThan50PercentOfMSAMedian
import hmda.model.publication.reports.{ EthnicityBorrowerCharacteristic, MinorityStatusBorrowerCharacteristic, RaceBorrowerCharacteristic }
import hmda.publication.reports.util.DispositionType._
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

class N52Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = CbsaLookup.values.map(_.cbsa).filterNot(f => f.isEmpty || f == "99999")
  val fipsGen = Gen.oneOf(fips)
  def propType = Gen.oneOf(1, 2).sample.get

  val lars = larListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fipsGen.sample.get)
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val expectedDispositionNames =
    List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted, ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)
      .map(_.value)

  "Generate an National Aggregate 5-2 report" in {
    N52.generate(source).map { result =>

      result.table mustBe "5-2"
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
