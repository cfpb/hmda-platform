package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.rules.lar.SummaryEditCheckSpec
import hmda.validation.rules.lar.`macro`.MacroEditTypes._
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.ExecutionContext

abstract class MacroSpec extends SummaryEditCheckSpec with BeforeAndAfterAll with LarGenerators {

  implicit val system: ActorSystem = ActorSystem("macro-edits-test")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val lars = MacroTestData.lars

  val larSource: LoanApplicationRegisterSource = {
    Source.fromIterator(() => lars.toIterator)
  }

  override def afterAll(): Unit = {
    system.terminate()
  }

  protected def newActionTakenTypeSource(lars: List[LoanApplicationRegister], numOfGoodLars: Int, goodActionTakenType: Int, badActionTakenType: Int) = {
    val goodLars = lars.map(lar => lar.copy(actionTakenType = goodActionTakenType)).take(numOfGoodLars)
    val badLars = lars.map(lar => lar.copy(actionTakenType = badActionTakenType)).drop(numOfGoodLars)
    val newLars = goodLars ::: badLars
    Source.fromIterator(() => newLars.toIterator)
  }

}
