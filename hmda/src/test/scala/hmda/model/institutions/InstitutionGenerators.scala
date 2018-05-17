package hmda.model.institutions

import hmda.model.institution._
import org.scalacheck.Gen
import hmda.model.filing.FilingGenerators._

object InstitutionGenerators {

  implicit def institutionGen: Gen[Institution] = {
    for {
      activityYear <- activityYearGen
      lei <- Gen.option(leiGen)
      agency <- Gen.option(agencyGen)
      institutionType <- Gen.option(institutionTypeGen)
      id2017 <- Gen.option(Gen.alphaStr)
      taxId <- Gen.option(Gen.alphaStr)
      rssd <- Gen.option(Gen.alphaStr)
      email <- Gen.option(emailGen)
      respondent <- institutionRespondentGen
      parent <- institutionParentGen
      assets <- Gen.option(Gen.choose(Int.MinValue, Int.MaxValue))
      otherLenderCode <- Gen.option(Gen.choose(Int.MinValue, Int.MaxValue))
      topHolder <- topHolderGen
      hmdaFiler <- Gen.oneOf(true, false)
    } yield {
      Institution(
        activityYear: Int,
        lei,
        agency,
        institutionType,
        id2017,
        taxId: Option[String],
        rssd: Option[String],
        email,
        respondent,
        parent,
        assets,
        otherLenderCode: Option[Int],
        topHolder: TopHolder,
        hmdaFiler: Boolean
      )
    }
  }

  implicit def institutionTypeCodeGen: Gen[Int] = {
    Gen.oneOf(InstitutionType.values.filter(x => x != -1))
  }

  implicit def institutionTypeGen: Gen[InstitutionType] = {
    for {
      institutionTypeCode <- institutionTypeCodeGen
      institutionType = InstitutionType.valueOf(institutionTypeCode)
    } yield institutionType
  }

  implicit def institutionRespondentGen: Gen[Respondent] = {
    for {
      name <- Gen.option(Gen.alphaStr)
      state <- Gen.option(stateGen)
      city <- Gen.option(Gen.alphaStr)
    } yield Respondent(name, state, city)
  }

  implicit def institutionParentGen: Gen[Parent] = {
    for {
      idRssd <- Gen.option(Gen.choose(Int.MinValue, Int.MaxValue))
      name <- Gen.option(Gen.alphaStr)
    } yield Parent(idRssd, name)
  }

  implicit def topHolderGen: Gen[TopHolder] = {
    for {
      idRssd <- Gen.option(Gen.choose(Int.MinValue, Int.MaxValue))
      name <- Gen.option(Gen.alphaStr)
    } yield TopHolder(idRssd, name)
  }

}
