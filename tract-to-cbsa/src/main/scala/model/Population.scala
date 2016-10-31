package model

abstract class Population {
  def key: String = ""
  def smallCounty: String = ""
}

case class PrPopulation(
  override val key: String,
  override val smallCounty: String,
  sex: String,
  ageGroup: String
) extends Population

case class StatesPopulation(
  override val key: String = "",
  override val smallCounty: String = ""
) extends Population
