package hmda.parser.filing.lar

import cats.data.ValidatedNel
import hmda.model.filing.lar.enums.LarCodeEnum
import hmda.parser.ParserErrorModel.ParserValidationError
import cats.implicits._
import scala.util.{Failure, Success, Try}

trait LarParser {

  type LarParserValidationResult[A] = ValidatedNel[ParserValidationError, A]

  def validateIntField(value: String,
                       parserValidationError: ParserValidationError)
    : LarParserValidationResult[Int] = {
    Try(value.toInt) match {
      case Success(i) => i.validNel
      case Failure(_) => parserValidationError.invalidNel
    }
  }

  def validateDoubleField(value: String,
                          parserValidationError: ParserValidationError)
    : LarParserValidationResult[Double] = {
    Try(value.toDouble) match {
      case Success(i) => i.validNel
      case Failure(_) => parserValidationError.invalidNel
    }
  }

  def validateStrOrNAField(value: String,
                           parserValidationError: ParserValidationError)
    : LarParserValidationResult[String] = {
    if (value == "") {
      println("found empty value")
      parserValidationError.invalidNel
    } else if (value == "NA") {
      println("Found NA")
      value.validNel
    } else {
      println("Value: " + value)
      validateIntField(value, parserValidationError).map(x => x.toString)
    }
  }

  def validateStr(str: String): LarParserValidationResult[String] = {
    str.validNel
  }

  def validateLarCode[A](larCodeEnum: LarCodeEnum[A],
                         value: String,
                         parserValidationError: ParserValidationError)
    : LarParserValidationResult[A] = {
    Try(larCodeEnum.valueOf(value.toInt)) match {
      case Success(enum) => enum.validNel
      case Failure(_)    => parserValidationError.invalidNel
    }
  }

  def validateMaybeLarCode[A](larCodeEnum: LarCodeEnum[A],
                              value: String,
                              parserValidationError: ParserValidationError)
    : LarParserValidationResult[A] = {
    val checkValue = if (value == "") "-1" else value
    validateLarCode(larCodeEnum, checkValue, parserValidationError)
  }

}
