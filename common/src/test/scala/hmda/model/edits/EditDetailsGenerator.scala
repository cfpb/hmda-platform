package hmda.model.edits

import org.scalacheck.Gen

object EditDetailsGenerator {

  implicit def fieldDetailsGen: Gen[FieldDetails] = {
    for {
      name <- Gen.alphaStr
      value <- Gen.alphaStr
    } yield FieldDetails(name, value)
  }

  implicit def editDetailsRowGen: Gen[EditDetailsRow] = {
    for {
      id <- Gen.alphaStr
      fields <- Gen.listOf(fieldDetailsGen)
    } yield EditDetailsRow(id, fields)
  }

  implicit def editDetailsGen: Gen[EditDetails] = {
    for {
      edit <- Gen.alphaStr
      rows <- Gen.listOf(editDetailsRowGen)
    } yield EditDetails(edit, rows)
  }
}
