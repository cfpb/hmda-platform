package hmda.validation.rules.lar.validity

import akka.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory
import hmda.grpc.services.{CheckDigitServiceClient, ValidUliRequest}
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
object V609 extends AsyncEditCheck[LoanApplicationRegister] {
  override def name: String = "V609"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.uli.grpc.host")
  val port = config.getInt("hmda.uli.grpc.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val uli = lar.loan.ULI

    if (uli.length <= 22) {
      Future.successful(ValidationSuccess)
    } else {
      uliIsValid(uli).map {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    }

  }

  def uliIsValid[as: AS, mat: MAT, ec: EC](uli: String): Future[Boolean] = {
    val client = CheckDigitServiceClient(
      GrpcClientSettings.connectToServiceAt(host, port).withDeadline(10.seconds).withTls(false)
    )
    for {
      response <- client
        .validateUli(ValidUliRequest(uli))
        .map(response => response.isValid)
      _ <- client.close()
      closed <- client.closed()
    } yield (response, closed)._1
  }

}
