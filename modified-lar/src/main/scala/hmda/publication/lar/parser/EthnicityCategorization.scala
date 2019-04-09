package hmda.publication

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._

object EthnicityCategorization {

  def assignEthnicityCategorization(lar: LoanApplicationRegister): String = {
    val ethnicity = lar.applicant.ethnicity
    val coEthnicity = lar.coApplicant.ethnicity
    val ethnicityFields = Array(ethnicity.ethnicity1,
                                ethnicity.ethnicity2,
                                ethnicity.ethnicity3,
                                ethnicity.ethnicity4,
                                ethnicity.ethnicity5)
    val coethnicityFields = Array(ethnicity.ethnicity1,
                                  ethnicity.ethnicity2,
                                  ethnicity.ethnicity3,
                                  ethnicity.ethnicity4,
                                  ethnicity.ethnicity5)
    val hispanicEnums = Array(HispanicOrLatino,
                              Mexican,
                              PuertoRican,
                              Cuban,
                              OtherHispanicOrLatino)
    val coapplicantNotHispanic = (!hispanicEnums.contains(
      coEthnicity.ethnicity1) && !hispanicEnums.contains(coEthnicity.ethnicity2) && !hispanicEnums
      .contains(coEthnicity.ethnicity3) && !hispanicEnums.contains(
      coEthnicity.ethnicity4) && !hispanicEnums.contains(
      coEthnicity.ethnicity5))
    if (ethnicity.otherHispanicOrLatino != "" && ethnicity.ethnicity1 == EmptyEthnicityValue)
      "Free Form Text Only"
    else if (ethnicity.ethnicity1 == InformationNotProvided || ethnicity.ethnicity1 == EthnicityNotApplicable)
      "Ethnicity Not Available"
    else if (ethnicity.ethnicity1 == NotHispanicOrLatino && ethnicity.ethnicity2 == EmptyEthnicityValue && ethnicity.ethnicity3 == EmptyEthnicityValue && ethnicity.ethnicity4 == EmptyEthnicityValue && ethnicity.ethnicity5 == EmptyEthnicityValue && coapplicantNotHispanic)
      "Not Hispanic or Latino"
    else if (ethnicityFields
               .map(hispanicEnums.contains(_))
               .reduce(_ || _) && (coethnicityFields
               .map(_ == NotHispanicOrLatino)
               .reduce(_ && _)))
      "Joint"
    else if (coethnicityFields
               .map(hispanicEnums.contains(_))
               .reduce(_ || _) && (ethnicityFields
               .map(_ == NotHispanicOrLatino)
               .reduce(_ && _)))
      "Joint"
    else if (ethnicityFields.contains(NotHispanicOrLatino) && ethnicityFields
               .map(hispanicEnums.contains(_))
               .reduce(_ || _))
      "Joint"
    else if (coethnicityFields.contains(NotHispanicOrLatino) && coethnicityFields
               .map(hispanicEnums.contains(_))
               .reduce(_ || _))
      "Joint"
    else
      "Not Determined"
  }
}
