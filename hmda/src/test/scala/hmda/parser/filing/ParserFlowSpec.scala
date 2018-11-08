package hmda.parser.filing

import org.scalatest.{MustMatchers, WordSpec}
import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.lar.LarGenerators._
import ParserFlow._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.ParserErrorModel.{
  IncorrectNumberOfFields,
  ParserValidationError
}

class ParserFlowSpec extends WordSpec with MustMatchers {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val ts = tsGen.sample.getOrElse(TransmittalSheet())
  val tsCsv = ts.toCSV + "\n"
  val tsSource = Source.fromIterator(() => List(tsCsv).iterator)

  val badTsCsv = ts.toCSV + "|too|many|fields\n"
  val badTsSource = Source.fromIterator(() => List(badTsCsv).iterator)

  val larList = larNGen(10).suchThat(_.nonEmpty).sample.getOrElse(Nil)
  val larCsv = larList.map(lar => lar.toCSV + "\n")
  val larSource = Source.fromIterator(() => larCsv.iterator)

  val badLarList = larNGen(10).sample.getOrElse(Nil)
  val badLarCsv = List(badLarList.head.toCSV + "|too|many|fields\n") ++ badLarList.tail
    .map(lar => lar.toCSV + "\n")
  val badLarSource = Source.fromIterator(() => badLarCsv.iterator)

  val hmdaFile = List(ts) ++ larList
  val hmdaFileCsv = List(tsCsv) ++ larCsv
  val hmdaFileSource = Source.fromIterator(() => hmdaFileCsv.iterator)

  val badHmdaFileCsv = List(badTsCsv) ++ badLarCsv
  val badHmdaFileSource = Source.fromIterator(() => badHmdaFileCsv.iterator)

  "Parser Flow" must {
    "parse text into Transmittal Sheet" in {
      tsSource
        .map(ByteString(_))
        .via(parseTsFlow)
        .map(_.getOrElse(TransmittalSheet()))
        .runWith(TestSink.probe[PipeDelimited])
        .request(1)
        .expectNext(ts)
    }

    "find Transmittal Sheet parsing errors" in {
      badTsSource
        .map(ByteString(_))
        .via(parseTsFlow)
        .map(_.left.get)
        .runWith(TestSink.probe[List[ParserValidationError]])
        .request(1)
        .expectNext(List(IncorrectNumberOfFields(18, 15)))
    }

    "parse list of text into list of Loan Application Register" in {
      larSource
        .map(ByteString(_))
        .via(parseLarFlow)
        .map(_.getOrElse(LoanApplicationRegister()))
        .runWith(TestSink.probe[PipeDelimited])
        .request(larCsv.size)
        .expectNextN(larList)
    }

    "find Loan Application Register parsing errors" in {
      badLarSource
        .map(ByteString(_))
        .via(parseLarFlow)
        .map(_.left.get)
        .runWith(TestSink.probe[List[ParserValidationError]])
        .request(badLarList.size)
        .expectNextN(Seq(List(IncorrectNumberOfFields(113, 110)))
          .asInstanceOf[Seq[List[ParserValidationError]]])
    }

    "parse clean HMDA file" in {
      hmdaFileSource
        .map(ByteString(_))
        .via(parseHmdaFile)
        .map(_.right.get)
        .runWith(TestSink.probe[PipeDelimited])
        .request(hmdaFile.size)
        .expectNextN(hmdaFile)
    }

    "parse dirty HMDA file and collect errors" in {
      badHmdaFileSource
        .map(ByteString(_))
        .via(parseHmdaFile)
        .filter(x => x.isLeft)
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ParserValidationError]])
        .request(badHmdaFileCsv.size)
        .expectNext(List(IncorrectNumberOfFields(18, 15)))
        .expectNext(List(IncorrectNumberOfFields(113, 110)))
    }
  }

}
