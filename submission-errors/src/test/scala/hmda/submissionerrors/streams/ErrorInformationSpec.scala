package hmda.submissionerrors.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.testkit.TestKit
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.validation.{ LarValidationError, MacroValidationError, SyntacticalValidationError }
import hmda.submissionerrors.streams.ErrorInformation._
import org.scalatest.FlatSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers

class ErrorInformationSpec extends TestKit(ActorSystem("error-information-spec")) with FlatSpecLike with ScalaFutures with Matchers {
  "collectErrors" should "index HmdaRowValidatedError into a Map of Line Numbers and Edit Names" in {
    val exampleSource: Source[HmdaRowValidatedError, NotUsed] =
      Source(
        List(
          HmdaRowValidatedError(
            rowNumber = 1,
            List(
              SyntacticalValidationError(uli = "EXAMPLE-ULI-1", "ABC", LarValidationError),
              SyntacticalValidationError(uli = "EXAMPLE-ULI-1", "DEF", LarValidationError)
            )
          ),
          HmdaRowValidatedError(
            rowNumber = 2,
            List(
              SyntacticalValidationError(uli = "EXAMPLE-ULI-2", "ABC", LarValidationError),
              SyntacticalValidationError(uli = "EXAMPLE-ULI-2", "GHI", LarValidationError)
            )
          ),
          HmdaRowValidatedError(
            rowNumber = 2,
            List(MacroValidationError("XYZ"))
          )
        )
      )

    whenReady(exampleSource.runWith(collectErrors)) { errorMap =>
      errorMap shouldBe Map(
        1 -> Set("ABC", "DEF"),
        2 -> Set("ABC", "GHI", "XYZ")
      )
    }
  }
}