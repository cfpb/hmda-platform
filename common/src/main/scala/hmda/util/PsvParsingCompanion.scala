package hmda.util

import cats.data.NonEmptyList
import cats.syntax.all._
import com.typesafe.config.ConfigFactory
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.parser.CSVLikeParser
import io.chrisdavenport.cormorant.{CSV, Error, Get}

private object PsvParsingCompanion {
  private final val config = ConfigFactory.load()
  private final val QUOTE = "\""
  private final val ESCAPED_QUOTE = "\"\""
  private final val FIELD_REGEX = config.getString("psv.fieldRegex")
  private final val FIELD_QUOTED_REPLACEMENT = config.getString("psv.quotedReplacement")
}
trait PsvParsingCompanion[T] {
  import PsvParsingCompanion._
  val psvReader: cormorant.Read[T]
  def parseFromPSV(str: String): Either[cormorant.Error, T] = {
    val parser: CSVLikeParser = new CSVLikeParser('|') {}
    cormorant.parser.parseRow(quoteFieldsInPSV(str), parser).flatMap(psvReader.read)
  }
  def parseFromPSVUnsafe(str: String): T = parseFromPSV(str) match {
    case Left(value)  => throw value
    case Right(value) => value
  }

  // helper method to ensure read subset of columns and ensure there are some colums left
  protected def enforcePartialRead[T](reader: cormorant.Read[T], row: CSV.Row): Either[Error.DecodeFailure, (CSV.Row, T)] =
    reader
      .readPartial(row)
      .flatMap({
        case Left(value)  => Right(value)
        case Right(value) => Left(Error.DecodeFailure.single(s"CSV row read fully when partial read was expected. Output: ${value}"))
      })

  // helper to read next field from row
  protected def readNext[T: Get]: cormorant.Read[T] = (a: CSV.Row) => Get[T]
    .get(a.l.head)
    .map(t =>
      NonEmptyList.fromList(a.l.tail) match {
        case Some(nel) => (CSV.Row(nel), t).asLeft
        case None => t.asRight
      }
    )

  private def quoteFieldsInPSV(psvLine: String): String =
    if (psvLine.nonEmpty && psvLine.contains(QUOTE)) {
      psvLine
        .replaceAll(QUOTE, ESCAPED_QUOTE)
        .replaceAll(FIELD_REGEX, FIELD_QUOTED_REPLACEMENT)
    } else {
      psvLine
    }
}