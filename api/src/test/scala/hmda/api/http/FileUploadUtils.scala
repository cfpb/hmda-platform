package hmda.api.http

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, Multipart }

trait FileUploadUtils {
  def multiPartFile(contents: String, fileName: String) =
    Multipart.FormData(Multipart.FormData.BodyPart.Strict(
      "file",
      HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
      Map("filename" -> fileName)
    ))
}
