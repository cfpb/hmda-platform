package hmda.parser.derivedFields

import hmda.model.filing.lar.{ Ethnicity, LoanApplicationRegister }
import hmda.model.filing.lar.enums._

object EthnicityCategorization {

  def assignEthnicityCategorization(lar: LoanApplicationRegister): String = {
    val ethnicity   = lar.applicant.ethnicity
    val coEthnicity = lar.coApplicant.ethnicity
    val ethnicityFields =
      Array(ethnicity.ethnicity1, ethnicity.ethnicity2, ethnicity.ethnicity3, ethnicity.ethnicity4, ethnicity.ethnicity5)

    val coethnicityFields =
      Array(coEthnicity.ethnicity1, coEthnicity.ethnicity2, coEthnicity.ethnicity3, coEthnicity.ethnicity4, coEthnicity.ethnicity5)

    val hispanicEnums = Seq(HispanicOrLatino, Mexican, PuertoRican, Cuban, OtherHispanicOrLatino)

    def checkEthnicityTwoToFiveEmpty(ethnicity: Ethnicity): Boolean =
      ethnicity.ethnicity2 == EmptyEthnicityValue &&
        ethnicity.ethnicity3 == EmptyEthnicityValue &&
        ethnicity.ethnicity4 == EmptyEthnicityValue &&
        ethnicity.ethnicity5 == EmptyEthnicityValue

    def cotainsNotHispanicOrLatinoFlag(ethnicity: Ethnicity): Boolean =
      ethnicity.ethnicity1 == NotHispanicOrLatino ||
        ethnicity.ethnicity2 == NotHispanicOrLatino ||
        ethnicity.ethnicity3 == EmptyEthnicityValue ||
        ethnicity.ethnicity4 == EmptyEthnicityValue ||
        ethnicity.ethnicity5 == EmptyEthnicityValue

    def OnlyHispanicFlag(ethnicityFields: Array[EthnicityEnum],
                         ethnicityEnum: Seq[EthnicityEnum with Product]): Boolean =
      ethnicityFields.exists(ethnicityEnum.contains) &&
        !ethnicityFields.contains(NotHispanicOrLatino)

    def NotHispanicFlagExist(ethnicityFields: Array[EthnicityEnum]): Boolean =
      ethnicityFields.contains(NotHispanicOrLatino)

    //Free form text only
    if (ethnicity.ethnicity1 == EmptyEthnicityValue)
      "Free Form Text Only"

    //Ethnicity Not Available
    else if (ethnicity.ethnicity1 == InformationNotProvided || ethnicity.ethnicity1 == EthnicityNotApplicable)
      "Ethnicity Not Available"
    //Hispanic or Latino
    else if (hispanicEnums.contains(ethnicity.ethnicity1) &&
             checkEthnicityTwoToFiveEmpty(ethnicity) &&
             !cotainsNotHispanicOrLatinoFlag(coEthnicity))
      "Hispanic or Latino"
    else if (OnlyHispanicFlag(ethnicityFields, hispanicEnums) &&
             !NotHispanicFlagExist(coethnicityFields))
      "Hispanic or Latino"

    //Not Hispanic or Latino
    else if (ethnicity.ethnicity1 == NotHispanicOrLatino &&
             checkEthnicityTwoToFiveEmpty(ethnicity) &&
             !coethnicityFields.exists(hispanicEnums.contains))
      "Not Hispanic or Latino"
    //Joint
    else if (ethnicityFields.exists(hispanicEnums.contains) &&
             cotainsNotHispanicOrLatinoFlag(coEthnicity))
      "Joint"
    else if (cotainsNotHispanicOrLatinoFlag(ethnicity) &&
             coethnicityFields.exists(hispanicEnums.contains))
      "Joint"
    else if (cotainsNotHispanicOrLatinoFlag(ethnicity) &&
             ethnicityFields.exists(hispanicEnums.contains))
      "Joint"
    else if (cotainsNotHispanicOrLatinoFlag(coEthnicity) &&
             coethnicityFields.exists(hispanicEnums.contains))
      "Joint"
//TBD
    else
      "Ethnicity Not Available"
  }
}
