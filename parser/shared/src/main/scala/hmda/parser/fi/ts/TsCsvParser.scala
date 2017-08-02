package hmda.parser.fi.ts

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }

import scalaz._
import scalaz.Scalaz._
import scala.util.{ Failure, Success, Try }

object TsCsvParser {
  def apply(s: String): Either[List[String], TransmittalSheet] = {
    val values = (s + " ").split('|').map(_.trim).toList
    if (values.length != 21) {
      return Left(List(s"An incorrect number of data fields were reported: ${values.length} data fields were found, when 21 data fields were expected."))
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

    val maybeTS: ValidationNel[String, TransmittalSheet] = (
      id
      |@| code
      |@| timestamp
      |@| activityYear
      |@| taxId.success
      |@| totalLines
      |@| respondent.success
      |@| parent.success
      |@| contact.success
    ) { TransmittalSheet }

    maybeTS.leftMap(_.toList).toEither
  }

  def toIntOrFail(value: String, fieldName: String): ValidationNel[String, Int] = {
    convert(value.toInt, s"$fieldName is not an integer")
  }

  def toLongOrFail(value: String, fieldName: String): ValidationNel[String, Long] = {
    convert(value.toLong, s"$fieldName is not an integer")
  }

  private def convert[T](x: => T, message: String): ValidationNel[String, T] = {
    Try(x) match {
      case Failure(result) => message.failure.toValidationNel
      case Success(result) => result.success
    }
  }

}
