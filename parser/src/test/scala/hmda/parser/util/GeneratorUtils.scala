package hmda.parser.util

trait GeneratorUtils {
  def padIntWithZeros(intArg: Int, numZeroes: Int): String = {
    intArg.toString.reverse.padTo(numZeroes, "0").reverse.mkString
  }
}
