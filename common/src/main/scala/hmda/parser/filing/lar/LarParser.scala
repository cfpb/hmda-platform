package hmda.parser.filing.lar

import cats.implicits._
import hmda.model.filing.lar.enums.LarCodeEnum
import hmda.parser.LarParserValidationResult
import hmda.parser.ParserErrorModel.ParserValidationError

import scala.util.{ Failure, Success, Try }

trait LarParser {

  def validateIntField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[Int] =
    Try(value.toInt) match {
      case Success(i) => i.validNel
      case Failure(e) =>
        parserValidationError.invalidNel
    }

  def validateDoubleField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[Double] =
    Try(value.toDouble) match {
      case Success(i) => i.validNel
      case Failure(_) => parserValidationError.invalidNel
    }

  def validateNAOrExemptField(str: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] = {
    val naCode: String = "NA"
    val exemptCode: String = "Exempt"
    str match {
      case value if (value == naCode || value == exemptCode ) => value.validNel
      case _ => parserValidationError.invalidNel
    }
  }

  def validateBigDecimalField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[BigDecimal] =
    Try(BigDecimal(value)) match {
      case Success(i) => i.validNel
      case Failure(_) => parserValidationError.invalidNel
    }

  def validateStrNoSpace(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value.contains(" ") || value.contains("|") || value.contains(","))
      parserValidationError.invalidNel
    else
      value.validNel

  def validateIntStrOrNAField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA") {
      value.validNel
    } else {
      validateIntField(value, parserValidationError).map(x => x.toString)
    }

  def validateIntStrOrNAOrExemptField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA" || value == "Exempt") {
      value.validNel
    } else {
      validateIntField(value, parserValidationError).map(x => x.toString)
    }

  def validateDoubleStrOrNAField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA") {
      value.validNel
    } else {
      validateDoubleField(value, parserValidationError).map(x => x.toString)
    }

  def validateDoubleStrOrNAOrExemptField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA" || value == "Exempt") {
      value.validNel
    } else {
      validateDoubleField(value, parserValidationError).map(x => x.toString)
    }

  def validateDoubleStrOrNAOrExemptOrEmptyField(value: String,
                                                parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "NA" || value == "Exempt" || value == "") {
      value.validNel
    } else {
      validateDoubleField(value, parserValidationError).map(x => x.toString)
    }

  def validateDoubleStrOrEmptyOrNaField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "" || value == "NA") {
      value.validNel
    } else {
      validateDoubleField(value, parserValidationError).map(x => x.toString)
    }


  def validateStr(str: String): LarParserValidationResult[String] =
    str.validNel

  def validateStrOrNAOrExemptField(str: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
      str.validNel
      //TODO: Enforce logic to validate NA / Exempt case match

  def validateLarCode[A](larCodeEnum: LarCodeEnum[A],
                         value: String,
                         parserValidationError: ParserValidationError): LarParserValidationResult[A] =
    Try(larCodeEnum.valueOf(value.toInt)) match {
      case Success(enum) => enum.validNel
      case Failure(_)    => parserValidationError.invalidNel
    }

  def validateLarCodeOrEmptyField[A](larCodeEnum: LarCodeEnum[A],
                                     value: String,
                                     parserValidationError: ParserValidationError): LarParserValidationResult[A] =
    if (value == "") {
      larCodeEnum.valueOf(0).validNel
    } else {
      validateLarCode(larCodeEnum, value, parserValidationError)
    }

}
