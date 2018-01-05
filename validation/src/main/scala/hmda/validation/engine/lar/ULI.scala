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

  def convert(loanId: String): BigInt = {
    val digits = loanId
      .map(_.toLower)
      .map { c =>
        if (!c.isDigit) conversionTable(c.toString).toString
        else c
      }.mkString("")
    BigInt(digits)
  }

  def calculateMod(i: BigInt): BigInt = {
    i % 97
  }

  def calculateCheckDigit(i: BigInt): BigInt = {
    98 - i
  }

  def checkDigit(loanId: String): String = {
    if (!isAlphanumeric(loanId)) {
      throw new Exception("Loan ID is not alphanumeric")
    } else {
      stringLengthTwo(calculateCheckDigit(calculateMod(convert(loanId) * 100)))
    }
  }

  def stringLengthTwo(n: BigInt): String = {
    if (n <= 9 && n >= 0) s"0$n"
    else n.toString
  }

  def generateULI(loanId: String): String = {
    loanId + checkDigit(loanId).toString
  }

  def validateULI(uli: String): Boolean = {
    if (!isAlphanumeric(uli)) {
      throw new Exception("ULI is not alphanumeric")
    } else if (!uliIsValidLength(uli)) {
      throw new Exception(s"$uli is not between 23 and 45 characters long")
    } else {
      calculateMod(convert(uli)) == 1
    }
  }

  val alphanumeric = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toSet

  private def isAlphanumeric(str: String): Boolean = {
    str.forall(alphanumeric.contains(_))
  }

  def uliIsValidLength(uli: String): Boolean = {
    val count = uli.count(_.toString != "")
    count >= 23 && count <= 45
  }

}
