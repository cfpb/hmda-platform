package hmda.api.http.codec.filing.submission

import SubmissionStatusCodec._
import hmda.model.filing.submission.{Created, SubmissionStatus}
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.submission.SubmissionGenerator._
import io.circe.syntax._

class SubmissionStatusCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Submission Status must encode/decode to/from JSON") {
    forAll(submissionGen) { submission =>
      val status = submission.status
      val json = status.asJson
      json.as[SubmissionStatus].getOrElse(Created) mustBe status
    }
  }
}
