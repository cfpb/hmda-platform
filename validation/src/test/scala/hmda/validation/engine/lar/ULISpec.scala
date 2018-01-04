package hmda.validation.engine.lar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

class ULISpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val loan1 = "10Bx939c5543TqA1144M999143X"
  val loan2 = "10Cx939c5543TqA1144M999143X"
  val validULI1 = "10Bx939c5543TqA1144M999143X38"
  val validULI2 = "10Cx939c5543TqA1144M999143X10"
  val invalidULI = "10Bx939c5543TqA1144M999133X38"

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "A ULI Validation" must {
    "Produce valid check digit" in {
      ULI.checkDigit(loan1) mustBe "38"
      ULI.checkDigit(loan2) mustBe "10"
      ULI.generateULI(loan1) mustBe validULI1
      ULI.generateULI(loan2) mustBe validULI2
    }
    "include a leading 0 for check digits < 10" in {
      val loanId = "5493001YS08XHF42M0372005203"
      ULI.checkDigit(loanId) mustBe "07"
      ULI.generateULI(loanId) mustBe s"${loanId}07"
      val lId2 = "asdgfhkjasdgfhkasd000"
      ULI.checkDigit(lId2) mustBe "03"
      ULI.generateULI(lId2) mustBe s"${lId2}03"
    }
    "Validate ULI" in {
      ULI.validateULI(validULI1) mustBe true
      ULI.validateULI(validULI2) mustBe true
      ULI.validateULI(invalidULI) mustBe false
    }
  }
}
