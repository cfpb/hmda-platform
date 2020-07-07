package hmda.publisher.helper

import com.typesafe.config.ConfigFactory

object SnapshotCheck {

  val pgTableConfig    = ConfigFactory.load("application.conf").getConfig("pg-tables")
  val snapshotActive: Boolean = pgTableConfig.getBoolean("activate")

  def pathSelector(s3Path: String,fileName:String): String = {
    if(snapshotActive){
      val snapshotFile=fileName.replace(".txt","_snapshot.txt")
      "dev/snapshot-temp/"+snapshotFile
    }else{
      s3Path+fileName
    }
  }
}
