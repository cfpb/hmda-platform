package hmda.utils

import hmda.persistence.util.CassandraUtil
import org.scalatest.{ BeforeAndAfterAll, Suite }

trait EmbeddedCassandra extends BeforeAndAfterAll {
  self: Suite =>
  override def beforeAll(): Unit = {
    CassandraUtil.startEmbeddedCassandra()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    CassandraUtil.shutdown()
    super.afterAll()
  }
}