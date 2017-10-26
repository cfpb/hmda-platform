package hmda.publication.reports.util

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ Applicant, LarGenerators, LoanApplicationRegister }

trait ApplicantSpecUtil extends LarGenerators {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def larCollectionWithApplicant(transformation: (Applicant => Applicant)): List[LoanApplicationRegister] = {
    lar100ListGen.sample.get.map { lar =>
      val newApplicant = transformation(lar.applicant)
      lar.copy(applicant = newApplicant)
    }
  }

  def source(lars: List[LoanApplicationRegister]): Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

}
