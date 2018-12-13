package hmda.validation.rules.lar.validity

import akka.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory
import hmda.grpc.services.{
  CensusServiceClient,
  ValidTractRequest,
  ValidCountyRequest
}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.{AS, EC, MAT}
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.AsyncEditCheck

import scala.concurrent.Future

object V626 extends AsyncEditCheck[LoanApplicationRegister] {
  override def name: String = "V626"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.grpc.host")
  val port = config.getInt("hmda.census.grpc.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val county = lar.geography.county

    if (county.toLowerCase == "na") {
      Future.successful(ValidationSuccess)
    } else if (county.isEmpty) {
      Future.successful(ValidationFailure)
    } else {
      countyIsValid(county).map {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    }
  }

  def countyIsValid[as: AS, mat: MAT, ec: EC](
      county: String): Future[Boolean] = {
    println("This is the host: " + host)
    println("this is the port: " + port)
    val client = CensusServiceClient(
      GrpcClientSettings.connectToServiceAt(host, port).withTls(false)
    )
    client
      .validateCounty(ValidCountyRequest(county))
      .map(response => response.isValid)
  }

}
