package hmda.model

import java.io.Closeable

import scala.io.{BufferedSource, Source}
import hmda._

object ResourceUtils {

  def using[A <: Closeable, B](a: A)(f: A => B): B  = {
    try {
      f(a)
    } finally {
      a.close()
    }
  }

  def resource(fileName: String, encoding: String): BufferedSource = {
    val file = getClass.getResourceAsStream(fileName)
    Source.fromInputStream(file, encoding)
  }

  def fileLines(fileName: String, encoding: String = "UTF-8"): Iterable[String] = {
    using(resource(fileName, encoding)) { source =>
      source.getLines().toList
    }
  }

}
