package hmda.api.http.codec.filing.submission

import hmda.api.http.model.filing.submissions.{EditDetailsSummary}
import hmda.model.edits.{EditDetailsRow, FieldDetails}
import org.scalacheck.Gen

object EditDetailsSummaryGenerator {

  implicit def editDetailsSummaryGen: Gen[EditDetailsSummary] =
    for {
      editName <- Gen.alphaStr.suchThat(_.length > 0)
      rows <- Gen.listOf(editDetailsRowGen)
      path <- Gen.alphaStr
      currentPage <- Gen.choose(1, Int.MaxValue)
      total <- Gen.choose(1, Int.MaxValue)
    } yield EditDetailsSummary(editName, rows, path, currentPage, total)

  implicit def editDetailsRowGen: Gen[EditDetailsRow] =
    for {
      id <- Gen.alphaStr
      fields <- Gen.listOf(fieldDetailsGen)
    } yield EditDetailsRow(id, fields)

  implicit def fieldDetailsGen: Gen[FieldDetails] =
    for {
      name <- Gen.alphaStr
      value <- Gen.alphaStr
    } yield FieldDetails(name, value)

}
