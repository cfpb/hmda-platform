package hmda.api.http.codec.filing.submission

import hmda.model.filing.submission.SubmissionStatus
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object SubmissionStatusCodec {

  implicit val submissionStatusEncoder: Encoder[SubmissionStatus] =
    new Encoder[SubmissionStatus] {
      override def apply(a: SubmissionStatus): Json = Json.obj(
        ("code", Json.fromInt(a.code)),
        ("message", Json.fromString(a.message)),
        ("description", Json.fromString(a.description))
      )
    }

  implicit val submissionStatusDecoder: Decoder[SubmissionStatus] =
    new Decoder[SubmissionStatus] {
      override def apply(c: HCursor): Result[SubmissionStatus] =
        for {
          code <- c.downField("code").as[Int]
        } yield {
          SubmissionStatus.valueOf(code)
        }
    }
}
