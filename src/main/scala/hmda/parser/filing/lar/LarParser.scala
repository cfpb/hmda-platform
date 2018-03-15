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
      case Failure(e) =>
        parserValidationError.invalidNel
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
      parserValidationError.invalidNel
    } else if (value == "NA") {
      value.validNel
    } else {
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

  def validateLarCodeOrEmptyField[A](
      larCodeEnum: LarCodeEnum[A],
      value: String,
      parserValidationError: ParserValidationError)
    : LarParserValidationResult[A] = {
    if (value == "") {
      larCodeEnum.valueOf(-1).validNel
    } else {
      validateLarCode(larCodeEnum, value, parserValidationError)
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
