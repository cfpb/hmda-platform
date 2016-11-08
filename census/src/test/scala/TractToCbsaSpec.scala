import hmda.census.model._
import hmda.census.TractToCbsa._
import org.scalatest.{ MustMatchers, WordSpec }

class TractToCbsaSpec extends WordSpec with MustMatchers {

  "Tract to Cbsa" must {
    "make proper lines" in {
      val prPop = PrPopLookup.values
      val statesPop = StatesPopLookup.values
      val pops = prPop ++ statesPop
      val cbsas = CbsaLookup.values
      val stateAbrvs = StateAbrvLookup.values
      val dallasLine = "Dallas-Fort Worth-Arlington, TX|Dallas-Plano-Irving, TX|48|48113|113|012209|19100|19124|0|TX|0122.09"
      val noMsaLine = "||26|26011|011|990100|||1|MI|9901.00"
      val jamestownLine = "Jamestown-Dunkirk-Fredonia, NY||36|36013|013|036901|27460||0|NY|0369.01"

      makeLine(Tract("48", "113", "012209", "0122.09", "48113"), stateAbrvs, cbsas, pops) mustBe dallasLine
      makeLine(Tract("26", "011", "990100", "9901.00", "26011"), stateAbrvs, cbsas, pops) mustBe noMsaLine
      makeLine(Tract("36", "013", "036901", "0369.01", "36013"), stateAbrvs, cbsas, pops) mustBe jamestownLine

    }
  }

}
