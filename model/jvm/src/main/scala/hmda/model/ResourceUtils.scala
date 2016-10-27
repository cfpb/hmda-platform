package hmda.model

import scala.io.{ BufferedSource, Source }

trait ResourceUtils {
  // For opening and reading files in this project's /resources directory.

  def resource(filename: String): BufferedSource = {
    val file = getClass.getResourceAsStream(filename)
    Source.fromInputStream(file)
  }

  def resourceLines(filename: String): Iterator[String] = {
    resource(filename).getLines()
  }
}
