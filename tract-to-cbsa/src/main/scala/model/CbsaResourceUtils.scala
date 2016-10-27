package model

trait CbsaResourceUtils {

  def smallCountyChecker(population: Int) = {
    if (population < 3000) 1 else 0
  }

}
