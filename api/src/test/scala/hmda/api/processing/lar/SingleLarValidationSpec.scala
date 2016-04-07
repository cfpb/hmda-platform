package hmda.api.processing.lar

import java.io.File

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import hmda.api.processing.lar.SingleLarValidation.CheckLar
import hmda.parser.fi.lar.LarCsvParser
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

import scala.io.Source

class SingleLarValidationSpec(_system: ActorSystem)
    extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with MustMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("hmda-lar-validation-test"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val larValidation = system.actorOf(SingleLarValidation.props, "larValidation")

  val lines = Source.fromFile(new File("parser/src/test/resources/txt/THE_LYONS_NATIONAL_BANK.txt")).getLines()
  val lars = lines.drop(1).map(l => LarCsvParser(l))

  "LAR Validation" must {
    "validate all lars in sample files" in {
      lars.foreach { lar =>
        larValidation ! CheckLar(lar)
        expectMsg(Nil)
      }
    }
  }

}
