package hmda.api.model

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class EditResultsSpec extends PropSpec with PropertyChecks with MustMatchers {
  val e1 = EditResult("V001", ts = true, Seq(LarEditResult(LarId("abc")), LarEditResult(LarId("def"))))
  val e2 = EditResult("S001", ts = false, Seq(LarEditResult(LarId("ghi")), LarEditResult(LarId("jkl"))))
  val m1 = MacroResult("Q001", Seq.empty)

  val sum = SummaryEditResults(EditResults(Seq(e2)), EditResults(Seq(e1)), EditResults.empty, MacroResults(Seq(m1)))

  property("Edit results and summary edit results must convert to CSV") {
    EditResults(Seq(e1)).toCsv("validity") mustBe
      "validity, V001, Transmittal Sheet\n" +
      "validity, V001, abc\n" +
      "validity, V001, def\n"

    sum.toCsv mustBe
      "editType, editId, loanId\n" +
      "syntactical, S001, ghi\n" +
      "syntactical, S001, jkl\n" +
      "validity, V001, Transmittal Sheet\n" +
      "validity, V001, abc\n" +
      "validity, V001, def\n" +
      "macro, Q001\n"
  }
}
