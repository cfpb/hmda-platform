package hmda.publisher.query.component

import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.query.lar._
import hmda.publisher.query.panel.InstitutionEntity
import hmda.publisher.validation.{ LarData, PanelData, TsData }
import hmda.query.DbConfiguration._
import hmda.query.ts.TransmittalSheetEntity
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

// $COVERAGE-OFF$
class PublisherComponent(year: Int) extends PGTableNameLoader {

  import dbConfig.profile.api._
  import suffixKeys._

//  private val panelTableName = String.format("%s%d", panelTableBase, year)
  private val panelTableName = s"$panelTableBase$year"
  val institutionsTable = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, panelTableName))

  def institutionTableQuery(p: YearPeriod) = institutionsTable

  val (tsAnnualSuf, tsQ1Suf, tsQ2Suf, tsQ3Suf) = getSuffixes(year, TS)
  def transmittalSheetTableQuery(p: YearPeriod): TableQuery[TransmittalSheetTable] = {
    val tableName = p match {
      case YearPeriod.Whole => s"$tsAnnualTableBase$year$tsAnnualSuf"
      case YearPeriod.Q1 => s"$tsQuarterTableBase${year}_q1$tsQ1Suf"
      case YearPeriod.Q2 => s"$tsQuarterTableBase${year}_q2$tsQ2Suf"
      case YearPeriod.Q3 => s"$tsQuarterTableBase${year}_q3$tsQ3Suf"
    }
    TableQuery(tag => new TransmittalSheetTable(tag, tableName))
  }

  def createTransmittalSheetRepository(config: DatabaseConfig[JdbcProfile], p: YearPeriod) =
    new TsRepository(config, transmittalSheetTableQuery(p))

  val (larAnnualSuf, larQ1Suf, larQ2Suf, larQ3Suf) = getSuffixes(year, LAR)
  def larTableQuery(p: YearPeriod) = {
    val tableName = p match {
      case YearPeriod.Whole => s"$larAnnualTableBase$year$larAnnualSuf"
      case YearPeriod.Q1 => s"$larQuarterTableBase${year}_q1$larQ1Suf"
      case YearPeriod.Q2 => s"$larQuarterTableBase${year}_q2$larQ2Suf"
      case YearPeriod.Q3 => s"$larQuarterTableBase${year}_q3$larQ3Suf"
    }
    TableQuery(tag => new LarTable(tag, tableName))
  }

  def createLarRepository(config: DatabaseConfig[JdbcProfile], p: YearPeriod) =
    new LarRepository(config, larTableQuery(p))


  def validationLarData(p: YearPeriod): LarData = LarData[LarEntityImpl, LarTable](larTableQuery(p))(_.lei)

  def validationTSData(p: YearPeriod): TsData =
    TsData[TransmittalSheetEntity, TransmittalSheetTable](transmittalSheetTableQuery(p))(_.lei, _.totalLines, _.submissionId)

  def validationPanelData(p: YearPeriod): PanelData =
    PanelData[InstitutionEntity, InstitutionsTable](institutionTableQuery(p))(_.lei, _.hmdaFiler)

  val (mlar, _, _, _) = getSuffixes(year, MLAR)
  val mlarTableName = s"$mLarTableBase$year$mlar"

  val mlarTable = TableQuery[ModifiedLarTable]((tag: Tag) => new ModifiedLarTable(tag, mlarTableName))

  val validationMLarData: LarData =
    LarData[ModifiedLarEntityImpl, ModifiedLarTable](mlarTable)(_.lei)

}
// $COVERAGE-ON$