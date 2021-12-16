package hmda.query.ts

import hmda.parser.filing.ts.TsCsvParser.dateFromString
import hmda.util.PsvParsingCompanion
import hmda.util.conversion.ColumnDataFormatter
import io.chrisdavenport.cormorant.CSV
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.implicits._

case class TransmittalSheetEntity(
                                   lei: String = "",
                                   id: Int = 0,
                                   institutionName: String = "",
                                   year: Int = 0,
                                   quarter: Int = 0,
                                   name: String = "",
                                   phone: String = "",
                                   email: String = "",
                                   street: String = "",
                                   city: String = "",
                                   state: String = "",
                                   zipCode: String = "",
                                   agency: Int = 0,
                                   totalLines: Int = 0,
                                   taxId: String = "",
                                   submissionId: Option[String] = Some(""),
                                   createdAt: Option[java.sql.Timestamp] = Some(new java.sql.Timestamp(System.currentTimeMillis())),
                                   isQuarterly: Option[Boolean] = Some(false),
                                   signDate: Option[Long] = Some(0L)
                                 ) extends ColumnDataFormatter {
  def isEmpty: Boolean = lei == ""

  def toRegulatorPSV: String =
    s"$id|$institutionName|$year|" +
      s"$quarter|$name|$phone|" +
      s"$email|$street|$city|" +
      s"$state|$zipCode|$agency|" +
      s"$totalLines|$taxId|$lei|${dateToString(signDate)}"

  def toPublicPSV: String =
    s"$year|$quarter|$lei|$taxId|$agency|" +
      s"$institutionName|$state|$city|$zipCode|$totalLines"

  def toPublicCSV: String =
    s"$year,$quarter,$lei,$taxId,$agency," +
      s"${escapeCommas(institutionName)},$state,${escapeCommas(city)},$zipCode,$totalLines"

}

object TransmittalSheetEntity {

  object PublicParser extends PsvParsingCompanion[TransmittalSheetEntity] {
    override val psvReader: cormorant.Read[TransmittalSheetEntity] = { (a: CSV.Row) =>
      for {
        (rest, year)            <- enforcePartialRead(readNext[Int], a)
        (rest, quarter)         <- enforcePartialRead(readNext[Int], rest)
        (rest, lei)             <- enforcePartialRead(readNext[String], rest)
        (rest, taxId)           <- enforcePartialRead(readNext[String], rest)
        (rest, agency)          <- enforcePartialRead(readNext[Int], rest)
        (rest, institutionName) <- enforcePartialRead(readNext[String], rest)
        (rest, state)           <- enforcePartialRead(readNext[String], rest)
        (rest, city)            <- enforcePartialRead(readNext[String], rest)
        (rest, zipCode)         <- enforcePartialRead(readNext[String], rest)
        totalLinesOrMore        <- readNext[Int].readPartial(rest)
      } yield {
        def create(totalLines: Int) = TransmittalSheetEntity(
          lei = lei,
          institutionName = institutionName,
          year = year,
          quarter = quarter,
          city = city,
          state = state,
          zipCode = zipCode,
          agency = agency,
          totalLines = totalLines,
          taxId = taxId
        )
        totalLinesOrMore match {
          case Left((more, totalLines)) => Left(more -> create(totalLines))
          case Right(totalLines)        => Right(create(totalLines))
        }
      }
    }
  }

  /**
   *     s"$id|$institutionName|$year|" +
      s"$quarter|$name|$phone|" +
      s"$email|$street|$city|" +
      s"$state|$zipCode|$agency|" +
      s"$totalLines|$taxId|$lei|${dateToString(signDate)}"
   */
  object RegulatorParser extends PsvParsingCompanion[TransmittalSheetEntity] {
    override val psvReader: cormorant.Read[TransmittalSheetEntity] = { (a: CSV.Row) =>
      for {
        (rest, id)              <- enforcePartialRead(readNext[Int], a)
        (rest, institutionName) <- enforcePartialRead(readNext[String], rest)
        (rest, year)            <- enforcePartialRead(readNext[Int], rest)
        (rest, quarter)         <- enforcePartialRead(readNext[Int], rest)
        (rest, name)            <- enforcePartialRead(readNext[String], rest)
        (rest, phone)           <- enforcePartialRead(readNext[String], rest)
        (rest, email)           <- enforcePartialRead(readNext[String], rest)
        (rest, street)          <- enforcePartialRead(readNext[String], rest)
        (rest, city)            <- enforcePartialRead(readNext[String], rest)
        (rest, state)           <- enforcePartialRead(readNext[String], rest)
        (rest, zipCode)         <- enforcePartialRead(readNext[String], rest)
        (rest, agency)          <- enforcePartialRead(readNext[Int], rest)
        (rest, totalLines)      <- enforcePartialRead(readNext[Int], rest)
        (rest, taxId)           <- enforcePartialRead(readNext[String], rest)
        (rest, lei)             <- enforcePartialRead(readNext[String], rest)
        signDateOrMore          <- readNext[String].readPartial(rest)
      } yield {
        def create(signDate: String) = TransmittalSheetEntity(
          lei = lei,
          id = id,
          institutionName = institutionName,
          year = year,
          quarter = quarter,
          name = name,
          phone = phone,
          email = email,
          street = street,
          city = city,
          state = state,
          zipCode = zipCode,
          agency = agency,
          totalLines = totalLines,
          taxId = taxId,
          signDate = dateFromString(signDate)
        )

        signDateOrMore match {
          case Left((more, signDate)) => Left(more -> create(signDate))
          case Right(signDate)        => Right(create(signDate))
        }
      }
    }

  }

}