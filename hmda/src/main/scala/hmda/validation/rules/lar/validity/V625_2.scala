package hmda.validation.rules.lar.validity

import akka.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory
import hmda.grpc.services.{CensusServiceClient, ValidTractRequest}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.{AS, EC, MAT}
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.AsyncEditCheck

import scala.concurrent.Future
import scala.concurrent.duration._

object V625_2 extends AsyncEditCheck[LoanApplicationRegister] {
  override def name: String = "V625-2"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.grpc.host")
  val port = config.getInt("hmda.census.grpc.port")

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
    val client = CensusServiceClient(
      GrpcClientSettings.connectToServiceAt(host, port)
        .withDeadline(10.seconds)
        .withTls(false)
    )
    for {
      response <- client
        .validateTract(ValidTractRequest(tract))
        .map(response => response.isValid)
      _ <- client.close()
      closed <- client.closed()
    } yield (response, closed)._1
  }

}
