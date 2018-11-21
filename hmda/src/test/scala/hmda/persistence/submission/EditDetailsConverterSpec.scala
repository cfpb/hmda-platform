package hmda.persistence.submission

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.validation.{
  LarValidationError,
  SyntacticalValidationError,
  ValidityValidationError
}
import org.scalatest.{MustMatchers, WordSpec}
import EditDetailsConverter._

class EditDetailsConverterSpec extends WordSpec with MustMatchers {

  "Edit Details Converter" must {
    "convert validated rows into list of edit details" in {

      val uli1 = "12345"
      val uli2 = "6789"

      val validationErrors1 =
        List(
          SyntacticalValidationError(
            uli1,
            "S300",
            LarValidationError
          ),
          SyntacticalValidationError(
            uli1,
            "S301",
            LarValidationError
          ),
          ValidityValidationError(
            uli1,
            "V709",
            LarValidationError
          )
        )

      val validationErrors2 =
        List(
          SyntacticalValidationError(
            uli2,
            "S300",
            LarValidationError
          ),
          SyntacticalValidationError(
            uli2,
            "S301",
            LarValidationError
          )
        )

      val hmdaRowValidatedError1 =
        HmdaRowValidatedError(2, validationErrors1)

      val hmdaRowValidationError2 =
        HmdaRowValidatedError(3, validationErrors2)

      val hmdaRowValidatedErrors =
        Seq(hmdaRowValidatedError1, hmdaRowValidationError2)

      val convertEditDetails =
        hmdaRowValidatedErrors.flatMap(e => validatedRowToEditDetails(e))

      convertEditDetails.count(_.edit == "S300") mustBe 2
      convertEditDetails.count(_.edit == "S301") mustBe 2
      convertEditDetails.count(_.edit == "V709") mustBe 1

    }
  }

}
