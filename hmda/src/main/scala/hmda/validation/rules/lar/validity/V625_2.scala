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
import hmda.validation.model.AsyncModel.TractValidate
import io.circe.generic.auto._

import scala.concurrent.Future

object V625_2
    extends AsyncEditCheck[LoanApplicationRegister]
    with AsyncRequest {

  override def name: String = "V625-2"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.http.host")
  val port = config.getInt("hmda.census.http.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val tract = lar.geography.tract

    if (tract.toLowerCase != "na") {
      tractIsValid(tract).map {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    } else {
      Future.successful(ValidationSuccess)
    }
  }

  def tractIsValid[as: AS, mat: MAT, ec: EC](tract: String): Future[Boolean] = {
    val tractValidate = TractValidate(tract)
    for {
      messageRequest <- sendMessageRequest(tractValidate, host, port)
      response <- executeRequest(messageRequest)
      messageOrErrorResponse <- unmarshallResponse(response)
    } yield messageOrErrorResponse.isValid
  }

}
