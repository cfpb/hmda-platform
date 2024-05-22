package hmda.api.http.codec.filing.submission

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import EditDetailsSummaryGenerator._
import hmda.api.http.model.filing.submissions.EditDetailsSummary
import io.circe.syntax._

class EditDetailsSummaryCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("EditDetailsSummary must encode/decode to/from JSON") {
    forAll(editDetailsSummaryGen) { editDetailsSummary =>
      whenever(!editDetailsSummary.isEmpty) {
        val json = editDetailsSummary.asJson
        val encoded = json
          .as[EditDetailsSummary]
          .getOrElse(EditDetailsSummary())
        encoded.editName mustBe editDetailsSummary.editName
        encoded.rows mustBe editDetailsSummary.rows
        encoded.total mustBe editDetailsSummary.total
      }
    }
  }

  property("Empty EditDetailsSummary must correctly return true for is empty"){
    val editDetailsSummary = EditDetailsSummary()
     assert(editDetailsSummary.isEmpty)
  }
}
