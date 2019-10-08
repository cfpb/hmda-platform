package hmda.uli.api.grpc

import akka.stream.Materializer
import hmda.grpc.services.{ CheckDigitService, ValidUliRequest, ValidUliResponse }
import hmda.uli.validation.ULI._
import scala.concurrent.Future

class CheckDigitServiceImpl(materializer: Materializer) extends CheckDigitService {

  override def validateUli(in: ValidUliRequest): Future[ValidUliResponse] = {
    val uli     = in.uli
    val isValid = validateULI(uli)
    Future.successful(ValidUliResponse(isValid))
  }

}
