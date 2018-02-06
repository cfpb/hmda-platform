package hmda.model.filing.lar

sealed trait RaceObserved {
  val code: Int
  val description: String
}

object RaceObserved {
  val values = List()

  def valueOf(code:Int): RaceObserved = ???
}


