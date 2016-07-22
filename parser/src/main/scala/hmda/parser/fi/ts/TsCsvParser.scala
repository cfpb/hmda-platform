package hmda.parser.fi.ts

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }

import scalaz._
import scalaz.Scalaz._
import scala.util.{ Failure, Success, Try }

object TsCsvParser {
  def apply(s: String): Either[List[String], TransmittalSheet] = {
    val values = s.split('|').map(_.trim).toList
    if (values.length != 21) {
      return Left(List("Incorrect number of fields. found: " + values.length + ", expected: 21"))
    }

    val id = toIntOrFail(values(0), "Record Identifier")
    val respId = values(1)
    val code = toIntOrFail(values(2), "Agency Code")
    val timestamp = toLongOrFail(values(3), "Timestamp")
    val activityYear = toIntOrFail(values(4), "Activity Year")
    val taxId = values(5)
    val totalLines = toIntOrFail(values(6), "Total Lines")
    val respName = values(7)
    val respAddress = values(8)
    val respCity = values(9)
    val respState = values(10)
    val respZip = values(11)
    val parentName = values(12)
    val parentAddress = values(13)
    val parentCity = values(14)
    val parentState = values(15)
    val parentZip = values(16)
    val contactPerson = values(17)
    val contactPhone = values(18)
    val contactFax = values(19)
    val contactEmail = values(20)

    val respondent = Respondent(respId, respName, respAddress, respCity, respState, respZip)
    val parent = Parent(parentName, parentAddress, parentCity, parentState, parentZip)
    val contact = Contact(contactPerson, contactPhone, contactFax, contactEmail)

    val maybeTS = (
      id
      |@| code
      |@| timestamp
      |@| activityYear
      |@| totalLines
    ) {
        TransmittalSheet(
          _,
          _,
          _,
          _,
          taxId,
          _,
          respondent,
          parent,
          contact
        )
      }

    maybeTS.disjunction.toEither.bimap(_.toList, identity(_))
  }

  def toIntOrFail(value: String, fieldName: String): ValidationNel[String, Int] = {
    Try(value.toInt) match {
      case Failure(result) => s"$fieldName is not an Integer".failure.toValidationNel
      case Success(result) => result.success
    }
  }

  def toLongOrFail(value: String, fieldName: String): ValidationNel[String, Long] = {
    Try(value.toLong) match {
      case Failure(result) => s"$fieldName is not a Long".failure.toValidationNel
      case Success(result) => result.success
    }
  }

}
