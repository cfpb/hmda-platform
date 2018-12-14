package hmda.validation.rules.lar.quality

import akka.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory
import hmda.grpc.services._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.{AS, EC, MAT}
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.AsyncEditCheck

import scala.concurrent.Future

object Q603 extends AsyncEditCheck[LoanApplicationRegister] {
  override def name: String = "Q603"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.grpc.host")
  val port = config.getInt("hmda.census.grpc.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val county = lar.geography.county
    val tract = lar.geography.tract

    if (tract.toLowerCase == "na" && county.toLowerCase != "na") {
      isPopulationGt30k(county).map {
        case true  => ValidationFailure
        case false => ValidationSuccess
      }
    } else {
      Future.successful(ValidationSuccess)
    }
  }

  def isPopulationGt30k[as: AS, mat: MAT, ec: EC](
      county: String): Future[Boolean] = {
    val client = CensusServiceClient(
      GrpcClientSettings.connectToServiceAt(host, port).withTls(false)
    )
    client
      .validatePopulation(ValidPopulationRequest(county))
      .map(response => response.isValid)
  }

}
