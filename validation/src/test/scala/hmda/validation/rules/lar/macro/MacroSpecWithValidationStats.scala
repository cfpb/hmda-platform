package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.InstitutionGenerators.sampleInstitution
import hmda.validation.stats.ValidationStats._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.duration._

trait MacroSpecWithValidationStats extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit override def executionContext = system.dispatcher
  val duration = 5.seconds
  implicit val timeout = Timeout(duration)
  val validationStats = createValidationStats(system)
  val configuration = ConfigFactory.load()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  def any: Int = Gen.choose(0, 100).sample.get

  def ctx(institutionId: String, currentYear: Int = 2017): ValidationContext = {
    ValidationContext(Some(sampleInstitution.copy(id = institutionId)), Some(currentYear))
  }

  def toSource(lars: List[LoanApplicationRegister]): LoanApplicationRegisterSource = {
    Source.fromIterator(() => lars.toIterator)
  }

  def listOfN(n: Int, transform: LoanApplicationRegister => LoanApplicationRegister): List[LoanApplicationRegister] = {
    larNGen(n).sample.getOrElse(List()).map(transform)
  }

}
