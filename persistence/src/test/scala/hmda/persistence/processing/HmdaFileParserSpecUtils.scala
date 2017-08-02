package hmda.persistence.processing

import akka.actor.ActorRef
import akka.testkit.TestProbe
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.messages.events.processing.HmdaFileParserEvents.{ LarParsed, LarParsedErrors }
import hmda.persistence.processing.ProcessingMessages.Persisted

trait HmdaFileParserSpecUtils {

  def parseLars(actorRef: ActorRef, probe: TestProbe, xs: Array[String]): Unit = {
    val lars = xs.drop(1).map(line => LarCsvParser(line))
    lars.foreach {
      case Right(l) =>
        probe.send(actorRef, LarParsed(l))
        probe.expectMsg(Persisted)
      case Left(errors) =>
        probe.send(actorRef, LarParsedErrors(errors))
        probe.expectMsg(Persisted)
    }
  }

}
