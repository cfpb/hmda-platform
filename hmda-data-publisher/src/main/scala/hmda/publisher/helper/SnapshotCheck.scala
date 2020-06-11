package hmda.publisher.helper

object SnapshotCheck {

  def check(value: String,activate: Boolean): String = {
    if (activate) {
      value +"_snapshot"
    } else {
      value
    }
  }
}
