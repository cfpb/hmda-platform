package hmda.validation.engine.lar

//See https://www.consumerfinance.gov/eregulations/1003-C/2015-26607_20180101#1003-C-1

object ULI {

  val conversionTable = Map(
    "a" -> 10,
    "b" -> 11,
    "c" -> 12,
    "d" -> 13,
    "e" -> 14,
    "f" -> 15,
    "g" -> 16,
    "h" -> 17,
    "i" -> 18,
    "j" -> 19,
    "k" -> 20,
    "l" -> 21,
    "m" -> 22,
    "n" -> 23,
    "o" -> 24,
    "p" -> 25,
    "q" -> 26,
    "r" -> 27,
    "s" -> 28,
    "t" -> 29,
    "u" -> 30,
    "v" -> 31,
    "w" -> 32,
    "x" -> 33,
    "y" -> 34,
    "z" -> 35
  )

  def convert(loanId: String): String = loanId
    .map(_.toLower)
    .map { c =>
      if (!c.isDigit)
        conversionTable(c.toString).toString
      else
        c
    }.mkString("")

  def calculateMod(i: BigInt): BigInt = {
    i % 97
  }

  def calculateCheckDigit(i: BigInt) = {
    98 - i
  }

  def checkDigit(uli: String): BigInt = {
    calculateCheckDigit(calculateMod(BigInt(convert(uli) ++ "00")))
  }

  def generateULI(uli: String): String = {
    uli + checkDigit(uli).toString()
  }

  def validate(uli: String): Boolean = {
    calculateMod(BigInt(convert(uli))) == 1
  }

}
