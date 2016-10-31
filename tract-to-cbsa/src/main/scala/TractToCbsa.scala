import model._
import java.io._

object TractToCbsa extends App {

  val prPop = PrPopLookup.values
  val statesPop = StatesPopLookup.values
  val pops = prPop ++ statesPop
  val tracts = TractLookup.values
  val cbsas = CbsaLookup.values
  val stateAbrvs = StateAbrvLookup.values

  val output = tracts.map { tract =>
    val state = stateAbrvs.find(state => state.state == tract.state).getOrElse(StateAbrv())
    val cbsa = cbsas.find(cbsa => cbsa.key == tract.key).getOrElse(Cbsa())
    val pop = pops.find(pop => pop.key == tract.key).getOrElse(StatesPopulation())
    List(
      cbsa.cbsaTitle,
      cbsa.metroDivTitle,
      tract.state,
      tract.key,
      tract.county,
      tract.tract,
      cbsa.cbsa,
      cbsa.metroDiv,
      pop.smallCounty,
      state.stateAbrv,
      tract.tractDec
    ).mkString("|")
  }.mkString("\r\n")
  println("Output")

  val file = new File("tract_to_cbsa.txt")
  val bw = new BufferedWriter(new FileWriter(file))
  bw.write(output)
  bw.close()
}
