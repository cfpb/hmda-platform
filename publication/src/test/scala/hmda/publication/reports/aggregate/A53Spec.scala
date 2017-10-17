package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.ActionTakenTypeEnum._
import hmda.model.publication.reports.ApplicantIncomeEnum.LessThan50PercentOfMSAMedian
import hmda.model.publication.reports.{ EthnicityBorrowerCharacteristic, MSAReport, MinorityStatusBorrowerCharacteristic, RaceBorrowerCharacteristic }
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.LarConverter._
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

class A53Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val fips = 18700 //Corvallis, OR
  def propType = Gen.oneOf(1, 2).sample.get

  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(propertyType = propType, purpose = 3)
    lar.copy(geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegisterQuery, NotUsed] = Source
    .fromIterator(() => lars.toIterator)
    .map(lar => toLoanApplicationRegisterQuery(lar))

  val expectedDispositions = List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted, ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

  "Generate an Aggregate 5-3 report" in {
    A53.generate(source, fips).map { result =>

      result.msa mustBe MSAReport("18700", "Corvallis, OR", "OR", "Oregon")
      result.table mustBe "5-3"
      result.applicantIncomes.size mustBe 5

      val lowestIncome = result.applicantIncomes.head
      lowestIncome.applicantIncome mustBe LessThan50PercentOfMSAMedian

      val races = lowestIncome.borrowerCharacteristics.head.asInstanceOf[RaceBorrowerCharacteristic].races
      races.size mustBe 8

      val ethnicities = lowestIncome.borrowerCharacteristics(1).asInstanceOf[EthnicityBorrowerCharacteristic].ethnicities
      ethnicities.size mustBe 4

      val minorityStatuses = lowestIncome.borrowerCharacteristics(2).asInstanceOf[MinorityStatusBorrowerCharacteristic].minoritystatus
      minorityStatuses.size mustBe 2

      races.head.dispositions.map(_.disposition) mustBe expectedDispositions
    }
  }

}
