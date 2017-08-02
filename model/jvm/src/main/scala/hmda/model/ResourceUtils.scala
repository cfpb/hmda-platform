package hmda.model

import scala.language.reflectiveCalls
import scala.io.{ BufferedSource, Source }

trait ResourceUtils {

  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B = {
    try {
      f(resource)
    } finally {
      resource.close()
    }
  }

  def resource(filename: String, encoding: String): BufferedSource = {
    val file = getClass.getResourceAsStream(filename)
    Source.fromInputStream(file, encoding)
  }

  def resourceLines(filename: String, encoding: String = "UTF-8"): Iterable[String] = {
    using(resource(filename, encoding)) { source =>
      source.getLines().toList
    }
  }
}
