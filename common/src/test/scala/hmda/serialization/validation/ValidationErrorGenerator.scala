package hmda.serialization.validation

import hmda.model.validation._
import org.scalacheck.Gen

import scala.collection.immutable._

object ValidationErrorGenerator {

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] =
    Gen.oneOf(Seq(Syntactical, Validity, Quality, Macro))

  implicit def validationErrorEntityGen: Gen[ValidationErrorEntity] =
    Gen.oneOf(Seq(TsValidationError, LarValidationError))

  implicit def validationErrorFieldsGen: Gen[ListMap[String, String]] = {
    val keyGen = Gen.listOf(Gen.alphaStr)
    for {
      keys <- keyGen
      values <- Gen.containerOfN[List, String](keys.length, Gen.alphaStr)
    } yield ListMap((keys zip values): _*)
  }

  implicit def validationErrorGen: Gen[ValidationError] =
    for {
      uli <- Gen.alphaStr
      editName <- Gen.alphaStr
      validationErrorType <- validationErrorTypeGen
      validationErrorEntity <- validationErrorEntityGen
      validationErrorFields <- validationErrorFieldsGen
    } yield {
      validationErrorType match {
        case Syntactical =>
          SyntacticalValidationError(uli,
                                     editName,
                                     validationErrorEntity,
                                     validationErrorFields)
        case Validity =>
          ValidityValidationError(uli,
                                  editName,
                                  validationErrorEntity,
                                  validationErrorFields)
        case Quality =>
          QualityValidationError(uli, editName, validationErrorFields)
        case Macro => MacroValidationError(editName)
      }
    }

}
