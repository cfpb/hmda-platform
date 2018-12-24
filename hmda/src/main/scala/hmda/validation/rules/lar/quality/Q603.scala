package hmda.validation.rules.lar.quality

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
import hmda.validation.model.AsyncModel.{CountyValidate, Countyvalidated}
import io.circe.generic.auto._

import scala.concurrent.Future

object Q603 extends AsyncEditCheck[LoanApplicationRegister] with AsyncRequest {

  override def name: String = "Q603"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.http.host")
  val port = config.getInt("hmda.census.http.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val county = lar.geography.county
    val tract = lar.geography.tract

    if (tract.toLowerCase == "na" && county.toLowerCase != "na") {
      isCountySmall(county).map {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    } else {
      Future.successful(ValidationSuccess)
    }
  }

  def isCountySmall[as: AS, mat: MAT, ec: EC](
      county: String): Future[Boolean] = {

    val countyValidate = CountyValidate(county)
    for {
      messageRequest <- sendMessageRequest(countyValidate, host, port)
      response <- executeRequest(messageRequest)
      messageOrErrorResponse <- unmarshallResponse(response)
    } yield messageOrErrorResponse.isValid

  }

}
