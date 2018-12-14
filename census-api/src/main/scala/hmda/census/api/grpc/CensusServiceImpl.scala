package hmda.census.api.grpc

import akka.stream.Materializer
import hmda.grpc.services._
import hmda.census.validation.CensusValidation._
import hmda.model.census.Census

import scala.concurrent.Future

class CensusServiceImpl(materializer: Materializer,
                        indexedTract: Map[String, Census],
                        indexedCounty: Map[String, Census],
                        indexedLargeCounty: Map[String, Census])
    extends CensusService {

  private implicit val mat: Materializer = materializer

  override def validateTract(
      in: ValidTractRequest): Future[ValidTractResponse] = {
    val tract = in.tract
    val isValid = isTractValid(tract, indexedTract)
    Future.successful(ValidTractResponse(isValid))
  }

  override def validateCounty(
      in: ValidCountyRequest): Future[ValidCountyResponse] = {
    val county = in.county
    val isValid = isCountyValid(county, indexedCounty)
    Future.successful(ValidCountyResponse(isValid))
  }

  override def validate30k(in: Valid30kRequest): Future[Valid30kResponse] = {
    val county = in.county
    val isValid = isPopulationGt30k(county, indexedLargeCounty)
    Future.successful(Valid30kResponse(isValid))
  }

}
