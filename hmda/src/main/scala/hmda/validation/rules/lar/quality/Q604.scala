package hmda.validation.rules.lar.quality

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.{AsyncEditCheck, AsyncRequest}
import hmda.validation.{AS, EC, MAT}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.validation.model.AsyncModel.CountyValidate
import io.circe.generic.auto._

import scala.concurrent.Future

object Q604 extends AsyncEditCheck[LoanApplicationRegister] with AsyncRequest {

  override def name: String = "Q604"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.http.host")
  val port = config.getInt("hmda.census.http.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val county = lar.geography.county
    val state = lar.geography.state

    if (county.toLowerCase != "na" && state.toLowerCase != "na") {
      countyIsValid(county).map {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    } else {
      Future.successful(ValidationSuccess)
    }
  }

  def countyIsValid[as: AS, mat: MAT, ec: EC](
      county: String): Future[Boolean] = {

    val countyValidate = CountyValidate(county)
    for {
      messageRequest <- sendMessageRequestCounty(countyValidate,
                                                 host,
                                                 port,
                                                 "/census/validate/county")
      response <- executeRequest(messageRequest)
      messageOrErrorResponse <- unmarshallResponse(response, "county")
    } yield messageOrErrorResponse.isValid
  }

}
