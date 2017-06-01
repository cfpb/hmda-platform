package hmda.model.validation

import org.scalacheck.Gen

object ValidationErrorGenerators {

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] = {
    Gen.oneOf(
      List(Syntactical, Validity, Quality, Macro)
    )
  }
  def validationErrorGen: Gen[ValidationError] =
    Gen.oneOf(
      syntacticalValidationErrorGen,
      validityValidationErrorGen,
      qualityValidationErrorGen,
      macroValidationErrorGen
    )

  implicit def syntacticalValidationErrorGen: Gen[SyntacticalValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      ts <- Gen.oneOf(true, false)
    } yield SyntacticalValidationError(id, name, ts)
  }
  implicit def validityValidationErrorGen: Gen[ValidityValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      ts <- Gen.oneOf(true, false)
    } yield ValidityValidationError(id, name, ts)
  }
  implicit def qualityValidationErrorGen: Gen[QualityValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      ts <- Gen.oneOf(true, false)
    } yield QualityValidationError(id, name, ts)
  }
  implicit def macroValidationErrorGen: Gen[MacroValidationError] = {
    for {
      id <- Gen.alphaStr
    } yield MacroValidationError(id)
  }

}
