package hmda.dataBrowser.models

import slick.jdbc.GetResult

case class ModifiedLarEntity2017(
    id: Int,
    respondentId: String,
    agency: Int,
    loanType: Int,
    propertyType: Int,
    loanPurpose: Int,
    occupancy: Int,
    loanAmount: String,
    preapproval: Int,
    actionTakenType: Int,
    msaMd: String,
    state: String,
    county: String,
    tract: String,
    ethnicityApplicant: Int,
    ethnicityCoApplicant: Int,
    raceApplicant1: Int,
    raceApplicant2: String,
    raceApplicant3: String,
    raceApplicant4: String,
    raceApplicant5: String,
    coRaceApplicant1: Int,
    coRaceApplicant2: String,
    coRaceApplicant3: String,
    coRaceApplicant4: String,
    coRaceApplicant5: String,
    sexApplicant: Int,
    sexCoApplicant: Int,
    income: String,
    purchaserType: Int,
    denialReason1: String,
    denialReason2: String,
    denialReason3: String,
    rateSpread: String,
    hoepaStatus: Int,
    lienStatus: Int,
    population: String,
    minorityPopulationPercent: String,
    ffiecMedFamIncome: String,
    tractToMsaIncomePct: String,
    ownerOccupiedUnits: String,
    oneToFourFamUnits: String,
    filingYear: String
)

object ModifiedLarEntity2017 {
  // See http://slick.lightbend.com/doc/3.2.0/sql.html?highlight=getresult#result-sets
  // we use shortcut << to get type inference instead of explicitly specifying nextInt or nextString based on the type
  implicit val getResult: GetResult[ModifiedLarEntity2017] =
    GetResult(
      r =>
        ModifiedLarEntity2017(
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<,
          r.<<
      ))

  def header = ""
    
  def headerPipe = ""
    
  implicit class CsvOps2017(modifiedLarEntity: ModifiedLarEntity2017) {
    import modifiedLarEntity._
    def toCsv =
      s"""$id,$respondentId,$agency,$loanType,$propertyType,$loanPurpose,$occupancy,$loanAmount,$preapproval,$actionTakenType,$msaMd,$state,$county,$tract,$ethnicityApplicant,$ethnicityCoApplicant,$raceApplicant1,$raceApplicant2,$raceApplicant3,$raceApplicant4,$raceApplicant5,$coRaceApplicant1,$coRaceApplicant2,$coRaceApplicant3,$coRaceApplicant4,$coRaceApplicant5,$sexApplicant,$sexCoApplicant,$income,$purchaserType,$denialReason1,$denialReason2,$denialReason3,$rateSpread,$hoepaStatus,$lienStatus,$population,$minorityPopulationPercent,$ffiecMedFamIncome,$tractToMsaIncomePct,$ownerOccupiedUnits,$oneToFourFamUnits"""
  }

  implicit class PipeOps2017(modifiedLarEntity: ModifiedLarEntity2017) {
    import modifiedLarEntity._
    def toPipe =
      s"$id|$respondentId|$agency|$loanType|$propertyType|$loanPurpose|$occupancy|$loanAmount|$preapproval|$actionTakenType|$msaMd|$state|$county|$tract|$ethnicityApplicant|$ethnicityCoApplicant|$raceApplicant1|$raceApplicant2|$raceApplicant3|$raceApplicant4|$raceApplicant5|$coRaceApplicant1|$coRaceApplicant2|$coRaceApplicant3|$coRaceApplicant4|$coRaceApplicant5|$sexApplicant|$sexCoApplicant|$income|$purchaserType|$denialReason1|$denialReason2|$denialReason3|$rateSpread|$hoepaStatus|$lienStatus|$population|$minorityPopulationPercent|$ffiecMedFamIncome|$tractToMsaIncomePct|$ownerOccupiedUnits|$oneToFourFamUnits"
  }
}
