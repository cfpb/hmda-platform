package hmda.serialization.validation

import hmda.model.validation._
import org.scalacheck.Gen

object ValidationErrorGenerator {

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] =
    Gen.oneOf(Seq(Syntactical, Validity, Quality, Macro))

  implicit def validationErrorEntityGen: Gen[ValidationErrorEntity] =
    Gen.oneOf(Seq(TsValidationError, LarValidationError))

  implicit def validationErrorFieldsGen: Gen[Map[String, String]] = {
    val keyGen = Gen.listOf(Gen.alphaStr)
    for {
      keys <- keyGen
      values <- Gen.containerOfN[List, String](keys.length, Gen.alphaStr)
    } yield (keys zip values).toMap
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
