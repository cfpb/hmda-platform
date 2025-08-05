package hmda.parser.filing.lar

import cats.implicits._
import hmda.model.filing.lar.enums.LarCodeEnum
import hmda.parser.LarParserValidationResult
import hmda.parser.ParserErrorModel.ParserValidationError

import java.time.format.{DateTimeFormatter, ResolverStyle}
import scala.util.{Failure, Success, Try}

trait LarParser {

  def validateIntFieldReturnString(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    Try(value.toInt) match {
      case Success(_) => value.validNel
      case Failure(_) => parserValidationError.invalidNel
    }

  def validateIntFieldReturnInt(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[Int] =
    Try(value.toInt) match {
      case Success(i) => i.validNel
      case Failure(_) => parserValidationError.invalidNel
    }

  def validateDoubleFieldReturnString(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    Try(value.toDouble) match {
      case Success(_) => if ("[-\\d.]+".r.matches(value)) value.validNel else parserValidationError.invalidNel
      case Failure(_) => parserValidationError.invalidNel
    }

  def validateNAOrExemptOrStringValue(str: String): Boolean = {
    val naCode: String = "NA"
    val exemptCode: String = "Exempt"
    str match {
      case value if (value.equalsIgnoreCase( naCode) || value.equalsIgnoreCase( exemptCode )) =>{
        if (value.equalsIgnoreCase(naCode) || value.equalsIgnoreCase(exemptCode) ) {
          true
        } else {
          false
        }
      }
      case _ => true
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
      validateIntFieldReturnString(value, parserValidationError)
    }

  def validateIntStrOrNAOrExemptField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA" || value == "Exempt") {
      value.validNel
    } else {
      validateIntFieldReturnString(value, parserValidationError)
    }

  def validateDoubleStrOrNAField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA") {
      value.validNel
    } else {
      validateDoubleFieldReturnString(value, parserValidationError)
    }

  def validateDoubleStrOrNAOrExemptField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "") {
      parserValidationError.invalidNel
    } else if (value == "NA" || value == "Exempt") {
      value.validNel
    } else {
      validateDoubleFieldReturnString(value, parserValidationError)
    }

  def validateDoubleStrOrNAOrExemptOrEmptyField(value: String,
                                                parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "NA" || value == "Exempt" || value == "") {
      value.validNel
    } else {
      validateDoubleFieldReturnString(value, parserValidationError)
    }

  def validateDoubleStrOrEmptyOrNaField(value: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (value == "" || value == "NA") {
      value.validNel
    } else {
      validateDoubleFieldReturnString(value, parserValidationError)
    }


  def validateStr(str: String): LarParserValidationResult[String] =
      str.validNel

  def validateStrOrNAOrExemptField(str: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
    if (validateNAOrExemptOrStringValue(str))
      str.validNel
    else
      parserValidationError.invalidNel
  
  def validateDateField(str: String, parserValidationError: ParserValidationError): LarParserValidationResult[Int] = {
    val dateFormatter = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT)
    Try(dateFormatter.parse(str))  match {
      case Success(i) => str.toInt.validNel
      case Failure(_) => parserValidationError.invalidNel
    }
  }

  def validateDateOrNaField(str: String, parserValidationError: ParserValidationError): LarParserValidationResult[String] =
   if (str == "NA") {
      str.validNel
    } else {
      validateDateField(str, parserValidationError).map(x => x.toString)
    }

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
    } else if (value == 0) {
      parserValidationError.invalidNel
    } else {
      validateLarCode(larCodeEnum, value, parserValidationError)
    }

}
