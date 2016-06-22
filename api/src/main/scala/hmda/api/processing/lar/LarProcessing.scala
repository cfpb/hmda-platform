package hmda.api.processing.lar

import akka.NotUsed
import akka.stream.scaladsl.{ FileIO, Flow, Keep, Sink }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser

import scala.util.{ Failure, Success, Try }

object LarProcessing {

  def parseLars: Flow[String, LoanApplicationRegister, NotUsed] = {
    Flow[String]
      .map(parseLar)
      .map {
        case Success(l) => l
        case Failure(e) => throw new Exception(s"Parsing error: ${e.getLocalizedMessage}")
      }
  }

  val larsSink = Flow[LoanApplicationRegister]
    .toMat(Sink.fold(Seq.empty[LoanApplicationRegister])(_ :+ _))(Keep.right)

  private def parseLar(str: String): Try[LoanApplicationRegister] = {
    Try(LarCsvParser(str))
  }

}
