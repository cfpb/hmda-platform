package hmda.parser.fi.ts

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import scala.collection.immutable.ListMap
import scalaz._
import scalaz.Scalaz._
import scala.util.{ Failure, Success, Try }

object TsCsvParser {
  def apply(s: String): Either[List[String], TransmittalSheet] = {
    val values = s.split('|').map(_.trim).toList
    val parsedInts = checkTs(values)
    parsedInts match {
      case scalaz.Success(parsedValues) => {

        val id = parsedValues(0).asInstanceOf[Int]
        val respId = values(1)
        val code = parsedValues(1).asInstanceOf[Int]
        val timestamp = parsedValues(4).asInstanceOf[Long]
        val activityYear = parsedValues(2).asInstanceOf[Int]
        val taxId = values(5)
        val totalLines = parsedValues(3).asInstanceOf[Int]
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
        Right(
          TransmittalSheet(
            id,
            code,
            timestamp,
            activityYear,
            taxId,
            totalLines,
            respondent,
            parent,
            contact
          )
        )
      }
      case scalaz.Failure(errors) => {
        Left(
          errors.list.toList
        )
      }
    }
  }

  def checkTs(fields: List[String]): ValidationNel[String, List[AnyVal]] = {

    if (fields.length != 21) {
      ("Incorrect number of fields. found: " + fields.length + ", expected: 21").failure.toValidationNel
    } else {
      val numericFields = ListMap(
        "Record Identifier" -> fields(0),
        "Agency Code" -> fields(2),
        "Activity Year" -> fields(4),
        "Total Lines Entries" -> fields(6)
      )

      val validationListInt = numericFields.map { case (key, value) => toIntorFail(value, key) }
      val validationListLong = toLongorFail(fields(3), "Timestamp")
      val potato = validationListInt.reduce(_ +++ _)
      potato +++ validationListLong
    }
  }

  def toIntorFail(value: String, fieldName: String): ValidationNel[String, List[AnyVal]] = {
    Try(value.toInt) match {
      case Failure(result) => s"$fieldName is not an Integer".failure.toValidationNel
      case Success(result) => List(result).success
    }
  }

  def toLongorFail(value: String, fieldName: String): ValidationNel[String, List[AnyVal]] = {
    Try(value.toLong) match {
      case Failure(result) => s"$fieldName is not a Long".failure.toValidationNel
      case Success(result) => List(result).success
    }
  }

}
