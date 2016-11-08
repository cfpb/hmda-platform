package hmda.model

import scala.io.{ BufferedSource, Source }

trait ResourceUtils {
  // For opening and reading files in this project's /resources directory.

  def resource(filename: String, encoding: String): BufferedSource = {
    val file = getClass.getResourceAsStream(filename)
    Source.fromInputStream(file, encoding)
  }

  def resourceLines(filename: String, encoding: String = "UTF-8"): Iterator[String] = {
    resource(filename, encoding).getLines()
  }
}
