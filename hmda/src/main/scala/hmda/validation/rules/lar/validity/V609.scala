package hmda.validation.rules.lar.validity

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.{AS, EC, MAT}
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.{AsyncEditCheck, AsyncRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.validation.model.AsyncModel.{ULIValidate, ULIValidated}
import io.circe.generic.auto._

import scala.concurrent.Future

object V609 extends AsyncEditCheck[LoanApplicationRegister] with AsyncRequest {

  override def name: String = "V609"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.uli.http.host")
  val port = config.getInt("hmda.uli.http.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val uli = lar.loan.ULI

    if (uli.length <= 22) {
      Future.successful(ValidationSuccess)
    } else {
      uliIsValid(uli).map {
        case true =>
          ValidationSuccess
        case false =>
          ValidationFailure
      }
    }
  }

  def uliIsValid[as: AS, mat: MAT, ec: EC](uli: String): Future[Boolean] = {

    val uliValidate = ULIValidate(uli)
    for {
      messageRequest <- sendMessageRequest(uliValidate, host, port)
      response <- executeRequest(messageRequest)
      messageOrErrorResponse <- unmarshallResponse(response)
    } yield messageOrErrorResponse.isValid

  }

}
