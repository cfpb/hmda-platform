package hmda.parser.filing.ts

import cats.data.ValidatedNel
import cats.implicits._
import com.typesafe.config.ConfigFactory
import hmda.model.filing.ts.{Address, Contact, TransmittalSheet}
import hmda.model.institution.Agency
import hmda.parser.ParserErrorModel.{IncorrectNumberOfFields, ParserValidationError}
import hmda.parser.filing.ts.TsParserErrorModel._

import scala.util.{Failure, Success, Try}

sealed trait TsFormatValidator {

  val config = ConfigFactory.load()

  val numberOfFields = config.getInt("hmda.filing.ts.length")

  type TsParserValidationResult[A] = ValidatedNel[ParserValidationError, A]

  def validateTs(
      values: Seq[String]): TsParserValidationResult[TransmittalSheet] = {
    if (values.lengthCompare(numberOfFields) != 0) {
      IncorrectNumberOfFields(values.length, numberOfFields).invalidNel
    } else {
      val id = values.headOption.getOrElse("")
      val institutionName = values(1)
      val year = values(2)
      val quarter = values(3)
      val name = values(4)
      val phone = values(5)
      val email = values(6)
      val street = values(7)
      val city = values(8)
      val state = values(9)
      val zipCode = values(10)
      val agencyCode = values(11)
      val totalLines = values(12)
      val taxId = values(13)
      val lei = values(14)
      validateTsValues(
        id,
        institutionName,
        year,
        quarter,
        name,
        phone,
        email,
        street,
        city,
        state,
        zipCode,
        agencyCode,
        totalLines,
        taxId,
        lei
      )
    }

  }

  def validateTsValues(
      id: String,
      institutionName: String,
      year: String,
      quarter: String,
      name: String,
      phone: String,
      email: String,
      street: String,
      city: String,
      state: String,
      zipCode: String,
      agencyCode: String,
      totalLines: String,
      taxId: String,
      lei: String
  ): TsParserValidationResult[TransmittalSheet] = {

    val address = Address(street, city, state, zipCode)
    val contact = Contact(name, phone, email, address)

    (
      validateIdField(id),
      validateStr(institutionName),
      validateYear(year),
      validateQuarter(quarter),
      validateContact(contact),
      validateAgencyCode(agencyCode),
      validateTotalLines(totalLines),
      validateStr(taxId),
      validateStr(lei)
    ).mapN(TransmittalSheet)

  }

  private def validateStr(str: String): TsParserValidationResult[String] = {
    str.validNel
  }

  private def validateIdField(value: String): TsParserValidationResult[Int] = {
    validateIntField(value, InvalidId)
  }

  private def validateYear(value: String): TsParserValidationResult[Int] = {
    validateIntField(value, InvalidYear)
  }

  private def validateQuarter(value: String): TsParserValidationResult[Int] = {
    validateIntField(value, InvalidQuarter)
  }

  private def validateTotalLines(
      value: String): TsParserValidationResult[Int] = {
    validateIntField(value, InvalidTotalLines)
  }

  private def validateContact(
      contact: Contact): TsParserValidationResult[Contact] = {
    contact.validNel
  }

  private def validateAgencyCode(
      code: String): TsParserValidationResult[Agency] = {
    Try(Agency.valueOf(code.toInt)) match {
      case Success(c) => c.validNel
      case Failure(_) => InvalidAgencyCode.invalidNel
    }
  }

  private def validateIntField(value: String,
                               parserValidation: ParserValidationError)
    : TsParserValidationResult[Int] = {
    Try(value.toInt) match {
      case Success(i) => i.validNel
      case Failure(_) => parserValidation.invalidNel
    }
  }

}

object TsFormatValidator extends TsFormatValidator
