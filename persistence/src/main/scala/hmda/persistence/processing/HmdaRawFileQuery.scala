package hmda.persistence.processing

import akka.NotUsed
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.persistence.CommonMessages.{ Command, Event }
import hmda.persistence.processing.HmdaRawFileQuery.ReadHmdaRawData

object HmdaRawFileQuery {

  case class ReadHmdaRawData(submissionId: String) extends Command

  def props(): Props = Props(new HmdaRawFileQuery)

  def createHmdaRawFileQuery(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaRawFileQuery.props())
  }

}

class HmdaRawFileQuery extends Actor with ActorLogging {

  implicit val materializer = ActorMaterializer()

  val readJournal = PersistenceQuery(context.system)
    .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  override def receive: Receive = {

    case ReadHmdaRawData(id) =>
      val events: Source[Event, NotUsed] =
        readJournal.currentEventsByPersistenceId(id)
          .map(_.event.asInstanceOf[Event])

      val sink = Sink.foreach(println)

      events.runWith(sink)

    //events.runWith(sink)

    case _ => //ignore
  }

}
