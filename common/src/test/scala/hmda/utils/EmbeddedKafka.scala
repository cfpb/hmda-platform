package hmda.utils

import java.util.concurrent.atomic.AtomicReference

import net.manub.embeddedkafka.{ EmbeddedK, EmbeddedKafka }
import org.scalatest.{ BeforeAndAfterAll, Suite }

trait EmbeddedKafka extends BeforeAndAfterAll {
  self: Suite =>

  private val kafka: AtomicReference[EmbeddedK] = new AtomicReference(null)

  override def beforeAll(): Unit = {
    kafka.set(EmbeddedKafka.start())
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    Option(kafka.get()).foreach(_.stop(clearLogs = true))
    super.afterAll()
  }
}