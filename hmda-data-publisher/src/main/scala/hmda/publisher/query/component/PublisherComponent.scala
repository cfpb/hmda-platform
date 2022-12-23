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

//  private val panelTableName = String.format("%s%d", panelTableBase, year)
  private val panelTableName = s"$panelTableBase$year"
  val institutionsTable = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, panelTableName))

  def institutionTableQuery(p: YearPeriod) = institutionsTable

  def transmittalSheetTableQuery(p: YearPeriod): TableQuery[TransmittalSheetTable] = {
    val tableName = p match {
      case YearPeriod.Whole => s"$tsAnnualTableBase$year"
      case YearPeriod.Q1 => s"$tsQuarterTableBase${year}_q1"
      case YearPeriod.Q2 => s"$tsQuarterTableBase${year}_q2"
      case YearPeriod.Q3 => s"$tsQuarterTableBase${year}_q3"
    }
    TableQuery(tag => new TransmittalSheetTable(tag, tableName))
  }

  def createTransmittalSheetRepository(config: DatabaseConfig[JdbcProfile], p: YearPeriod) =
    new TsRepository(config, transmittalSheetTableQuery(p))

  def larTableQuery(p: YearPeriod) = {
    val tableName = p match {
      case YearPeriod.Whole => s"$larAnnualTableBase$year"
      case YearPeriod.Q1 => s"$larQuarterTableBase${year}_q1"
      case YearPeriod.Q2 => s"$larQuarterTableBase${year}_q2"
      case YearPeriod.Q3 => s"$larQuarterTableBase${year}_q3"
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

  val mlarTableName = s"$mLarTableBase$year"

  val mlarTable = TableQuery[ModifiedLarTable]((tag: Tag) => new ModifiedLarTable(tag, mlarTableName))

  val validationMLarData: LarData =
    LarData[ModifiedLarEntityImpl, ModifiedLarTable](mlarTable)(_.lei)

}
// $COVERAGE-ON$