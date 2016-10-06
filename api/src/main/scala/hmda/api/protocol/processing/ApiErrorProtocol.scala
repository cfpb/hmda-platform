package hmda.api.protocol.processing

import akka.http.scaladsl.model.Uri.Path
import hmda.api.model.ErrorResponse
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ApiErrorProtocol extends DefaultJsonProtocol {
  implicit object PathJsonFormat extends RootJsonFormat[Path] {
    override def write(path: Path): JsValue = JsString(path.toString)
    override def read(path: JsValue): Path =
      path match {
        case JsString(s) => Path(s)
        case _ => throw new DeserializationException("Unable to deserialize")
      }
  }

  implicit val apiErrorFormat = jsonFormat3(ErrorResponse.apply)

}
