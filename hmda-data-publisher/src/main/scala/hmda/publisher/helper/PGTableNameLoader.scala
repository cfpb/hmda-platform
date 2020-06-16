package hmda.publisher.helper

import com.typesafe.config.ConfigFactory

trait PGTableNameLoader {

  val pgTableConfig    = ConfigFactory.load("application.conf").getConfig("pg-tables")

  val snapshotActive: Boolean = pgTableConfig.getBoolean("activate")

  //2018 table names
  val lar2018TableName: String = SnapshotCheck.check(pgTableConfig.getString("lar2018TableName"),snapshotActive)
  val mlar2018TableName: String = SnapshotCheck.check(pgTableConfig.getString("mlar2018TableName"),snapshotActive)
  val panel2018TableName: String = SnapshotCheck.check(pgTableConfig.getString("panel2018TableName"),snapshotActive)
  val ts2018TableName: String = SnapshotCheck.check(pgTableConfig.getString("ts2018TableName"),snapshotActive)

  //2019 table names
  val lar2019TableName: String = SnapshotCheck.check(pgTableConfig.getString("lar2019TableName"),snapshotActive)
  val mlar2019TableName: String = SnapshotCheck.check(pgTableConfig.getString("mlar2019TableName"),snapshotActive)
  val panel2019TableName: String = SnapshotCheck.check(pgTableConfig.getString("panel2019TableName"),snapshotActive)
  val ts2019TableName: String = SnapshotCheck.check(pgTableConfig.getString("ts2019TableName"),snapshotActive)

  //2020 table names
  val lar2020TableName: String = SnapshotCheck.check(pgTableConfig.getString("lar2020TableName"),snapshotActive)
  val mlar2020TableName: String = SnapshotCheck.check(pgTableConfig.getString("mlar2020TableName"),snapshotActive)
  val panel2020TableName: String = SnapshotCheck.check(pgTableConfig.getString("panel2020TableName"),snapshotActive)
  val ts2020TableName: String = SnapshotCheck.check(pgTableConfig.getString("ts2020TableName"),snapshotActive)

  //common table names
  val emailTableName: String = SnapshotCheck.check(pgTableConfig.getString("emailTableName"),snapshotActive)

  println( "TABLE NAME: ",lar2019TableName)

}
