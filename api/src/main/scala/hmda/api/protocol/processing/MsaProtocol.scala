package hmda.api.protocol.processing

import hmda.query.model.filing.{ Irs, Msa, MsaSummary, MsaWithName }
import spray.json.DefaultJsonProtocol

trait MsaProtocol extends DefaultJsonProtocol {
  implicit val msaProtocol = jsonFormat13(Msa.apply)
  implicit val msaWithNameProtocol = jsonFormat14(MsaWithName.apply)
  implicit val msaSummaryProtocol = jsonFormat12(MsaSummary.apply)
  implicit val irsProtocol = jsonFormat2(Irs.apply)
}
