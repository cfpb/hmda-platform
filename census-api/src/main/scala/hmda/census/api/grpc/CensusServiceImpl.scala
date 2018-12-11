package hmda.census.api.grpc

import akka.stream.Materializer
import hmda.grpc.services._
import hmda.census.validation.Census._

import scala.concurrent.Future

class CensusServiceImpl(materializer: Materializer) extends CensusService {

  private implicit val mat: Materializer = materializer

  override def validateTract(
      in: ValidTractRequest): Future[ValidTractResponse] = {
    val uli = in.tract
    val isValid = testSomething(uli)
    Future.successful(ValidTractResponse(isValid))
  }

}
