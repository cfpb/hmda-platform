package hmda.analytics

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import hmda.model.filing.submission.SubmissionId
import hmda.parser.filing.lar.{LarCsvParser, SignedSubmissionCsvParser}
import hmda.parser.filing.ts.TsCsvParser
import hmda.query.HmdaQuery.{readRawData, readSubmission}
import hmda.util.streams.FlowUtils.framing
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object TestExample extends App {

  implicit val clientSystem = ActorSystem("testexmple")
  implicit val materializer = ActorMaterializer()
  implicit val ec = clientSystem.dispatcher

  val submissionId = SubmissionId(lei = "B90YWS6AFX2LGWOXJ1LD",
                                  period = "2018",
                                  sequenceNumber = 579)
//  readRawData(submissionId)
//    .map(l => l.data)
//    .map(ByteString(_))
//    .via(framing("\n"))
//    .map(_.utf8String)
//    .map(_.trim)
//    .drop(1)
//    .take(1)
//    .map(s => LarCsvParser(s))
//    .runWith(Sink.foreach(println))
//
//  readSubmission(submissionId)
//    .drop(9)
//    .take(1)
//    .map(l => l.submission.end)
////
////
//    .runWith(Sink.foreach(println))

  def signDate: Future[Done] =
    readSubmission(submissionId)
      .drop(9)
      .take(1)
      .map(l => l.submission.end)
      .runWith(Sink.ignore)

  def result =
    for {
      res <- signDate
    } yield res

  Await.result(result, 10.seconds)

  Thread.sleep(100)
  println("This is the result: " + result)

}
