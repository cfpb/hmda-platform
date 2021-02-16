package hmda.api.http.model

import io.circe.generic.JsonCodec

@JsonCodec
case class SignedLeiCountResponse(count: Int)

@JsonCodec
case class SignedLeiListResponse(data: Seq[SignedLeiListResponse.Elem])

object SignedLeiListResponse {
  @JsonCodec
  case class Elem(lei: String, submissionId: String)
}


@JsonCodec
case class TotalLeiCountResponse(count: Int)

@JsonCodec
case class TotalLeiListResponse(data: Seq[String])