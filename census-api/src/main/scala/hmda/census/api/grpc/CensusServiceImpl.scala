package hmda.census.api.grpc

import akka.stream.Materializer
import hmda.census.validation.CensusValidation._
import hmda.grpc.services._
import hmda.model.census.Census

import scala.concurrent.Future

class CensusServiceImpl(materializer: Materializer,
                        indexedTract: Map[String, Census],
                        indexedCounty: Map[String, Census],
                        indexedSmallCounty: Map[String, Census])
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

  override def validatePopulation(
      in: ValidPopulationRequest): Future[ValidPopulationResponse] = {
    val county = in.county
    val isValid = isCountySmall(county, indexedSmallCounty)
    Future.successful(ValidPopulationResponse(isValid))
  }

}
