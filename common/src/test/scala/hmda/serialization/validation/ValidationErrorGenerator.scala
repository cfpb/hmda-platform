package hmda.serialization.validation

import hmda.model.validation._
import org.scalacheck.Gen

object ValidationErrorGenerator {

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] =
    Gen.oneOf(Seq(Syntactical, Validity, Quality, Macro))

  implicit def validationErrorEntityGen: Gen[ValidationErrorEntity] =
    Gen.oneOf(Seq(TsValidationError, LarValidationError))

  implicit def validationErrorGen: Gen[ValidationError] =
    for {
      uli <- Gen.alphaStr
      editName <- Gen.alphaStr
      validationErrorType <- validationErrorTypeGen
      valiationErrorEntity <- validationErrorEntityGen
    } yield {
      validationErrorType match {
        case Syntactical =>
          SyntacticalValidationError(uli, editName, valiationErrorEntity)
        case Validity =>
          ValidityValidationError(uli, editName, valiationErrorEntity)
        case Quality => QualityValidationError(uli, editName)
        case Macro   => MacroValidationError(editName)
      }
    }

}
