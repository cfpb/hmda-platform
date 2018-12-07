package hmda.validation.filing

import akka.stream.scaladsl.Source
import org.scalatest.{AsyncWordSpec, MustMatchers}
import hmda.util.SourceUtils._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.filing.lar.LarCsvParser

class MacroValidationFlowSpec extends AsyncWordSpec with MustMatchers {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val fileSource = scala.io.Source.fromURL(
    getClass.getResource("/clean_file_1000_rows_Bank0_syntax_validity.txt"))

  val lars = fileSource.getLines().drop(1)
  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars)
    .map(x => LarCsvParser(x).getOrElse(LoanApplicationRegister()))

  "Macro Validation" must {
    "count total number of LARs" in {
      count(source).map(total => total mustBe lars.size)
    }
  }

}
