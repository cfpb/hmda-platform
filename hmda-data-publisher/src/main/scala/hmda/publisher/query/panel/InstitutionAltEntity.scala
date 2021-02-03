package hmda.publisher.query.panel

import hmda.util.PsvParsingCompanion
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.CSV
import io.chrisdavenport.cormorant.implicits._

case class InstitutionAltEntity(
                                 lei: String = "",
                                 activityYear: Int = 0,
                                 agency: Int = 9,
                                 institutionType: Int = -1,
                                 id2017: String = "",
                                 taxId: String = "",
                                 rssd: Int = -1,
                                 respondentName: String = "",
                                 respondentState: String = "",
                                 respondentCity: String = "",
                                 parentIdRssd: Int = -1,
                                 parentName: String = "",
                                 assets: Long = 0,
                                 otherLenderCode: Int = -1,
                                 topHolderIdRssd: Int = -1,
                                 topHolderName: String = "",
                                 hmdaFiler: Boolean = false,
                                 emailDomains: String = ""
                               ) {
  def isEmpty: Boolean = lei == ""

  def toPSV: String =
    s"$lei|$activityYear|$agency|" +
      s"$institutionType|$id2017|$taxId|" +
      s"$rssd|$emailDomains|$respondentName|$respondentState|" +
      s"$respondentCity|$parentIdRssd|$parentName|" +
      s"$assets|$otherLenderCode|$topHolderIdRssd|$topHolderName|$hmdaFiler"

  // This is not used
  // $COVERAGE-OFF$
  def toPublicPSV: String =
    s"$lei|$activityYear|$agency|" +
      s"$institutionType|$id2017|$taxId|" +
      s"$rssd|$respondentName|$respondentState|" +
      s"$respondentCity|$parentIdRssd|$parentName|" +
      s"$assets|$otherLenderCode|$topHolderIdRssd|$topHolderName"
  // $COVERAGE-ON$
}

object InstitutionAltEntity extends PsvParsingCompanion[InstitutionAltEntity] {
  override val psvReader: cormorant.Read[InstitutionAltEntity] = { (a: CSV.Row) =>
    for {
      (rest, lei)             <- enforcePartialRead(readNext[String], a)
      (rest, activityYear)    <- enforcePartialRead(readNext[Int], rest)
      (rest, agency)          <- enforcePartialRead(readNext[Int], rest)
      (rest, institutionType) <- enforcePartialRead(readNext[Int], rest)
      (rest, id2017)          <- enforcePartialRead(readNext[String], rest)
      (rest, taxId)           <- enforcePartialRead(readNext[String], rest)
      (rest, rssd)            <- enforcePartialRead(readNext[Int], rest)
      (rest, emailDomains)    <- enforcePartialRead(readNext[String], rest)
      (rest, respondentName)  <- enforcePartialRead(readNext[String], rest)
      (rest, respondentState) <- enforcePartialRead(readNext[String], rest)
      (rest, respondentCity)  <- enforcePartialRead(readNext[String], rest)
      (rest, parentIdRssd)    <- enforcePartialRead(readNext[Int], rest)
      (rest, parentName)      <- enforcePartialRead(readNext[String], rest)
      (rest, assets)          <- enforcePartialRead(readNext[Long], rest)
      (rest, otherLenderCode) <- enforcePartialRead(readNext[Int], rest)
      (rest, topHolderIdRssd) <- enforcePartialRead(readNext[Int], rest)
      (rest, topHolderName)   <- enforcePartialRead(readNext[String], rest)
      hmdaFilerOrMore         <- readNext[Boolean].readPartial(rest)
    } yield {
      def create(hmdaFiler: Boolean) = InstitutionAltEntity(
        lei = lei,
        activityYear = activityYear,
        agency = agency,
        institutionType = institutionType,
        id2017 = id2017,
        taxId = taxId,
        rssd = rssd,
        emailDomains = emailDomains,
        respondentName = respondentName,
        respondentState = respondentState,
        respondentCity = respondentCity,
        parentIdRssd = parentIdRssd,
        parentName = parentName,
        assets = assets,
        otherLenderCode = otherLenderCode,
        topHolderIdRssd = topHolderIdRssd,
        topHolderName = topHolderName,
        hmdaFiler = hmdaFiler
      )

      hmdaFilerOrMore match {
        case Left((more, hmdaFiler)) => Left(more -> create(hmdaFiler))
        case Right(hmdaFiler)        => Right(create(hmdaFiler))
      }
    }
  }

}