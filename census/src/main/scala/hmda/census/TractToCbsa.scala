package hmda.census

import model._
import java.io._

object TractToCbsa extends App with CbsaResourceUtils {

  val prPop = PrPopLookup.values
  val statesPop = StatesPopLookup.values
  val pops = prPop ++ statesPop
  val tracts = TractLookup.values
  val cbsas = CbsaLookup.values
  val stateAbrvs = StateAbrvLookup.values

  val output = tracts.map { tract =>
    makeLine(tract, stateAbrvs, cbsas, pops)
  }.mkString("\r\n")

  val file = new File("model/jvm/src/main/resources/tract_to_cbsa_2015.txt")
  val bw = new BufferedWriter(new FileWriter(file))
  try {
    bw.write(output)
  } catch {
    case _: Throwable => println("failed to write to file")
  } finally {
    bw.close()
  }

  def makeLine(tract: Tract, stateAbrvs: Seq[StateAbrv], cbsas: Seq[Cbsa], pops: Seq[Population]): String = {
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
  }

}
