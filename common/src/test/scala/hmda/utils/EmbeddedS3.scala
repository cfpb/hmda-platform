package hmda.utils

import java.util.concurrent.atomic.AtomicReference

import com.adobe.testing.s3mock.S3MockApplication
import org.scalatest.{ BeforeAndAfterAll, Suite }

import scala.collection.mutable
import scala.collection.JavaConverters._

trait EmbeddedS3 extends BeforeAndAfterAll { self: Suite =>
  private val s3: AtomicReference[S3MockApplication] = new AtomicReference(null)

  private val properties: mutable.Map[String, Object] =
    mutable // S3 Mock mutates the map so we cannot use an immutable map :(
      .Map(
        S3MockApplication.PROP_HTTPS_PORT      -> S3MockApplication.DEFAULT_HTTPS_PORT,
        S3MockApplication.PROP_HTTP_PORT       -> S3MockApplication.DEFAULT_HTTP_PORT,
        S3MockApplication.PROP_SILENT          -> true,
        S3MockApplication.PROP_INITIAL_BUCKETS -> "cfpb-hmda-public-dev"
      )
      .map { case (k, v) => (k, v.asInstanceOf[Object]) }

  override def beforeAll(): Unit = {
    s3.set(S3MockApplication.start(properties.asJava))
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    Option(s3.get()).foreach(_.stop())
    super.afterAll()
  }
}