package hmda.model.filing.ts

case class TransmittalLar(ts: TransmittalSheet, larsCount: Int = 0, larsDistinctCount: Int = 0, distinctUliCount: Int = 0)
//S304 => ts.totalLines == larsCount
//S305 => larsCount == larsDistinctCount
//Q600 => larsCount == distinctUliCount