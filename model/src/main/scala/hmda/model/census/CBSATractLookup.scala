package hmda.model.census

import java.io.File

import scala.io.Source

object CBSATractLookup {
  val values: Seq[CBSATract] = {
    val lines = Source.fromFile(new File("model/src/main/resources/tract_to_cbsa_2013.csv")).getLines
    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val name = values(0)
      val namelsad = values(1)
      val lsad = values(2)
      val memi = values(3)
      val mtfcc = values(4)
      val tract = values(5)
      val state = values(6)
      val county = values(7)
      val tracts = values(8)
      val geoidMsa = values(9)
      val geoidMetDiv = values(10)
      val metDivFp = values(11)
      val smallCounty = values(12)
      CBSATract(
        name,
        namelsad,
        lsad,
        memi,
        mtfcc,
        tract,
        state,
        county,
        tracts,
        geoidMsa,
        geoidMetDiv,
        metDivFp,
        smallCounty
      )
    }.toSeq
  }
}

case class CBSATract(
  name: String,
  namelsad: String,
  lsad: String,
  memi: String,
  mtfcc: String,
  tract: String,
  state: String,
  county: String,
  tracts: String,
  geoidMsa: String,
  geoidMetdiv: String,
  metdivfp: String,
  smallCounty: String
)
