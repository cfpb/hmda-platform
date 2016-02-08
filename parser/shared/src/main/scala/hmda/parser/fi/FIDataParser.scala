package hmda.parser.fi

import hmda.model.fi.FIData

trait FIDataParser[A] {
  def read(input: Iterable[A]): FIData

  def readAll(input: String): FIData
}

