package hmda.model.institution

import hmda.generators.CommonGenerators.{activityYearGen, emailListGen, leiGen, stateGen}
import hmda.util.CSVConsolidator.listDeDupeToList
import org.scalacheck.Gen

object InstitutionGenerators {

  implicit def institutionGen: Gen[Institution] =
    for {
      activityYear    <- activityYearGen
      lei             <- leiGen
      agency          <- agencyGen
      institutionType <- institutionTypeGen
      id2017          <- Gen.alphaStr
      taxId           <- Gen.alphaStr
      rssd            <- Gen.choose(0, Int.MaxValue)
      email           <- emailListGen
      respondent      <- institutionRespondentGen
      parent          <- institutionParentGen
      assets          <- Gen.choose(Long.MinValue, Long.MaxValue)
      otherLenderCode <- Gen.choose(Int.MinValue, Int.MaxValue)
      topHolder       <- topHolderGen
      hmdaFiler       <- Gen.oneOf(true, false)
      quarterlyFiler  <- Gen.oneOf(true, false)
      quarterlyFilerHasFiledQ1  <- Gen.oneOf(true, false)
      quarterlyFilerHasFiledQ2  <- Gen.oneOf(true, false)
      quarterlyFilerHasFiledQ3  <- Gen.oneOf(true, false)
      notes                     <- Gen.asciiPrintableStr

    } yield {
      Institution(
        activityYear: Int,
        lei,
        agency,
        institutionType,
        if (id2017 == "") None else Some(id2017),
        if (taxId == "") None else Some(taxId),
        rssd,
        listDeDupeToList(email),
        respondent,
        parent,
        assets,
        otherLenderCode,
        topHolder,
        hmdaFiler,
        quarterlyFiler,
        quarterlyFilerHasFiledQ1,
        quarterlyFilerHasFiledQ2,
        quarterlyFilerHasFiledQ3,
        notes
      )
    }

  implicit def agencyCodeGen: Gen[Int] =
    Gen.oneOf(Agency.values)

  implicit def agencyGen: Gen[Agency] =
    for {
      agencyCode <- agencyCodeGen
      agency     = Agency.valueOf(agencyCode)
    } yield agency

  implicit def institutionTypeCodeGen: Gen[Int] =
    Gen.oneOf(InstitutionType.values.filter(x => x != -1))

  implicit def institutionTypeGen: Gen[InstitutionType] =
    for {
      institutionTypeCode <- institutionTypeCodeGen
      institutionType     = InstitutionType.valueOf(institutionTypeCode)
    } yield institutionType

  implicit def institutionRespondentGen: Gen[Respondent] =
    for {
      name  <- Gen.option(Gen.alphaStr.suchThat(!_.isEmpty))
      state <- Gen.option(stateGen)
      city  <- Gen.option(Gen.alphaStr.suchThat(!_.isEmpty))
    } yield Respondent(name, state, city)

  implicit def institutionParentGen: Gen[Parent] =
    for {
      idRssd <- Gen.choose(Int.MinValue, Int.MaxValue)
      name   <- Gen.option(Gen.alphaStr.suchThat(!_.isEmpty))
    } yield Parent(idRssd, name)

  implicit def topHolderGen: Gen[TopHolder] =
    for {
      idRssd <- Gen.choose(Int.MinValue, Int.MaxValue)
      name   <- Gen.option(Gen.alphaStr.suchThat(!_.isEmpty))
    } yield TopHolder(idRssd, name)

}
