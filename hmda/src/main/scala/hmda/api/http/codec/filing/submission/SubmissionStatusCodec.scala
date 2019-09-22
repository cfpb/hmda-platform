package hmda.api.http.codec.filing.submission

import hmda.model.filing.submission.SubmissionStatus
import io.circe.{Decoder, Encoder, HCursor, Json}

object SubmissionStatusCodec {

  implicit val submissionStatusEncoder: Encoder[SubmissionStatus] =
    (a: SubmissionStatus) =>
      Json.obj(
        ("code", Json.fromInt(a.code)),
        ("message", Json.fromString(a.message)),
        ("description", Json.fromString(a.description))
      )

  implicit val submissionStatusDecoder: Decoder[SubmissionStatus] =
    (c: HCursor) => c.downField("code").as[Int].map(SubmissionStatus.valueOf)
}