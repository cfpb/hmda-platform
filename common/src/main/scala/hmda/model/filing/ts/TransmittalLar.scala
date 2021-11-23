package hmda.model.filing.ts

case class TransmittalLar(ts: TransmittalSheet,
                          uli: String = "",
                          larsCount: Int = 0,
                          larsDistinctCount: Long = 0L,
                          uniqueLarsSpecificFields: Long = 0L,
                          distinctUliCount: Long = 0L,
                          distinctActionTakenUliCount: Long = 0L,
                          duplicateUliToLineNumbers: Map[String, List[Int]] = Map.empty,
                          duplicateUliToLineNumbersUliActionType: Map[String, List[Int]] = Map.empty,
                          actionTakenDatesWithinRange: Long = 0L,
                          actionTakenDatesGreaterThanRange: Long = 0L)
