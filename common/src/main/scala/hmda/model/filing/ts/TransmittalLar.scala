package hmda.model.filing.ts

case class TransmittalLar(ts: TransmittalSheet,
                          larsCount: Int = 0,
                          larsDistinctCount: Long = 0L,
                          distinctUliCount: Long = 0L)
