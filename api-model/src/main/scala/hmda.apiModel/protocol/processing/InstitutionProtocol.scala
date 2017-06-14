package hmda.apiModel.protocol.processing

import hmda.apiModel.model.{ InstitutionDetail, InstitutionWrapper, Institutions }
import spray.json.DefaultJsonProtocol

trait InstitutionProtocol extends DefaultJsonProtocol with FilingProtocol {

  implicit val institutionWrapperFormat = jsonFormat2(InstitutionWrapper.apply)
  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
  implicit val institutionDetail = jsonFormat2(InstitutionDetail.apply)

}
