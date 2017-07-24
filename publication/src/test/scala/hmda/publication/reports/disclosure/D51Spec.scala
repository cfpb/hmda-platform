package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.publication.reports.MSAReport
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.LarConverter._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

class D51Spec extends WordSpec with MustMatchers with LarGenerators {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val respId = "98765"
  val fips = 18700 //Corvallis, OR
  val loanType = Gen.oneOf(2, 3, 4).sample.get
  val propType = Gen.oneOf(1, 2).sample.get

  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = loanType, propertyType = propType, purpose = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegisterQuery, NotUsed] = Source
    .fromIterator(() => lars.toIterator)
    .map(lar => toLoanApplicationRegisterQuery(lar))

  "Generate a Disclosure 5-1 report" in {
    val result = Await.result(D51.generate(source, fips, respId), 5.seconds)

    result.msa mustBe MSAReport("18700", "Corvallis, OR", "OR", "Oregon")
    result.table mustBe "5-1"
    result.respondentId mustBe "98765"
  }

}
