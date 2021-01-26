package hmda.publisher.qa

import hmda.query.DbConfiguration.dbConfig
import slick.lifted.Rep

import dbConfig.profile.api._

trait QATableBase[Entity] extends Table[QAEntity[Entity]] {
  def fileName: Rep[String] = column("file_name")
  def timeStamp: Rep[Long] = column("time_stamp")

}