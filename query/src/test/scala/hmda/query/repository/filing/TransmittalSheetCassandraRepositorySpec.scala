package hmda.query.repository.filing

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.ts.TsGenerators
import hmda.model.institution.Agency
import hmda.query.model.filing.TransmittalSheetWithTimestamp
import hmda.query.repository.CassandraRepositorySpec

class TransmittalSheetCassandraRepositorySpec extends CassandraRepositorySpec[TransmittalSheetWithTimestamp] with TransmittalSheetCassandraRepository with TsGenerators {
  override def beforeAll(): Unit = {
    createKeyspace()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "TS in Cassandra" must {
    "Drop the table if it exists, create it again and populate it with some data that can be read back" in {
      dropTable()
      createTable()

      val tsList = ts100ListGen.sample.getOrElse(Nil)
        .map(x => x.copy(agencyCode = 9))
        .map(ts => TransmittalSheetWithTimestamp(ts, LocalDateTime.now().toString))
      val source = Source.fromIterator(() => tsList.toIterator)
      insertData(source)

      val readF = readData(100).runWith(Sink.seq)
      readF.map { t =>
        t.map(t => t.ts.agencyCode mustBe Agency.CFPB.value)
        t.size mustBe 100
      }
    }
  }

  override implicit def system: ActorSystem = ActorSystem()

  override implicit def materializer: ActorMaterializer = ActorMaterializer()
}
