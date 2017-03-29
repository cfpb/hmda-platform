package hmda.api.model

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json.{ JsObject, JsString }

class EditResultsSpec extends PropSpec with PropertyChecks with MustMatchers {
  val fields = JsObject(("name", JsString("value"))) // CSV format does not show field data

  val e1Rows = Seq(EditResultRow(RowId("Transmittal Sheet"), fields), EditResultRow(RowId("abc"), fields), EditResultRow(RowId("def"), fields))
  val e1 = EditResult("V001", "", e1Rows)
  val e2 = EditResult("S001", "", Seq(EditResultRow(RowId("ghi"), fields), EditResultRow(RowId("jkl"), fields)))
  val m1 = MacroResult("Q001")

  val sum = SummaryEditResults(EditResults(Seq(e2)), EditResults(Seq(e1)), QualityEditResults(false, Seq()), MacroResults(false, Seq(m1)))

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
