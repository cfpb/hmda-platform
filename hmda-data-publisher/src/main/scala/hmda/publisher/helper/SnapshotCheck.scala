package hmda.publisher.helper
// $COVERAGE-OFF$
import com.typesafe.config.ConfigFactory

object SnapshotCheck {

  val snapshotConfig    = ConfigFactory.load("application.conf").getConfig("snapshot")
  val snapshotActive: Boolean = snapshotConfig.getBoolean("snapshot_activate")
  val snapshotBucket: String = snapshotConfig.getString("snapshot_bucket")
  val snapshotPath: String = snapshotConfig.getString("snapshot_path")


  def pathSelector(s3Path: String,fileName:String): String = {
      snapshotPath+fileName
  }
}
// $COVERAGE-ON$