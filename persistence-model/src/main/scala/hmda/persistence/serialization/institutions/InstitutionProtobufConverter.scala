package hmda.persistence.serialization.institutions

import hmda.model.institution.ExternalIdType._
import hmda.model.institution._
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.serialization.InstitutionEvents._
import hmda.persistence.messages.commands.institutions.InstitutionCommands._
import hmda.persistence.model.serialization.InstitutionCommands._

object InstitutionProtobufConverter {

  def createInstitutionToProtobuf(cmd: CreateInstitution): CreateInstitutionMessage = {
    CreateInstitutionMessage(
      institution = Some(institutionToProtobuf(cmd.i))
    )
  }

  def createInstitutionFromProtobuf(msg: CreateInstitutionMessage): CreateInstitution = {
    CreateInstitution(
      i = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )
  }

  def modifyInstitutionToProtobuf(cmd: ModifyInstitution): ModifyInstitutionMessage = {
    ModifyInstitutionMessage(
      institution = Some(institutionToProtobuf(cmd.i))
    )
  }

  def modifyInstitutionFromProtobuf(msg: ModifyInstitutionMessage): ModifyInstitution = {
    ModifyInstitution(
      i = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )
  }

  def getInstitutionByRespondentIdToProtobuf(cmd: GetInstitutionByRespondentId): GetInstitutionByRespondentIdMessage = {
    GetInstitutionByRespondentIdMessage(id = cmd.id)
  }

  def getInstitutionByRespondentIdFromProtobuf(msg: GetInstitutionByRespondentIdMessage): GetInstitutionByRespondentId = {
    GetInstitutionByRespondentId(id = msg.id)
  }

  def getInstitutionByIdToProtobuf(cmd: GetInstitutionById): GetInstitutionByIdMessage = {
    GetInstitutionByIdMessage(institutionId = cmd.institutionId)
  }

  def getInstitutionByIdFromProtobuf(msg: GetInstitutionByIdMessage): GetInstitutionById = {
    GetInstitutionById(institutionId = msg.institutionId)
  }

  def getInstitutionsByIdToProtobuf(cmd: GetInstitutionsById): GetInstitutionsByIdMessage = {
    GetInstitutionsByIdMessage(ids = cmd.ids)
  }

  def getInstitutionsByIdFromProtobuf(msg: GetInstitutionsByIdMessage): GetInstitutionsById = {
    GetInstitutionsById(msg.ids.toList)
  }

  def findInstitutionByDomainToProtobuf(cmd: FindInstitutionByDomain): FindInstitutionByDomainMessage = {
    FindInstitutionByDomainMessage(domain = cmd.domain)
  }

  def findInstitutionByDomainFromProtobuf(msg: FindInstitutionByDomainMessage): FindInstitutionByDomain = {
    FindInstitutionByDomain(domain = msg.domain)
  }

  def institutionCreatedToProtobuf(evt: InstitutionCreated): InstitutionCreatedMessage = {
    InstitutionCreatedMessage(
      institution = Some(institutionToProtobuf(evt.institution))
    )
  }

  def institutionCreatedFromProtobuf(msg: InstitutionCreatedMessage): InstitutionCreated = {
    InstitutionCreated(
      institution = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )
  }

  def institutionModifiedToProtobuf(evt: InstitutionModified): InstitutionModifiedMessage = {
    InstitutionModifiedMessage(
      institution = Some(institutionToProtobuf(evt.institution))
    )
  }

  def institutionModifiedFromProtobuf(msg: InstitutionModifiedMessage): InstitutionModified = {
    InstitutionModified(
      institution = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )
  }

  def institutionToProtobuf(obj: Institution): InstitutionMessage = {
    InstitutionMessage(
      id = obj.id,
      agency = agencyToProtobuf(obj.agency),
      activityYear = obj.activityYear,
      institutionType = institutionTypeToProtobuf(obj.institutionType),
      cra = obj.cra,
      externalIds = obj.externalIds.map(externalId => externalIdToProtobuf(externalId)).toSeq,
      emailDomains = obj.emailDomains.toSeq,
      respondent = Some(respondentToProtobuf(obj.respondent)),
      hmdaFilerFlag = obj.hmdaFilerFlag,
      parent = Some(parentToProtobuf(obj.parent)),
      assets = obj.assets,
      otherLenderCode = obj.otherLenderCode,
      topHolder = Some(topHolderToProtobuf(obj.topHolder))
    )
  }

  def institutionFromProtobuf(msg: InstitutionMessage): Institution = {
    Institution(
      id = msg.id,
      agency = agencyFromProtobuf(msg.agency),
      activityYear = msg.activityYear,
      institutionType = institutionTypeFromProtobuf(msg.institutionType),
      cra = msg.cra,
      externalIds = msg.externalIds.map(externalIdMessage => externalIdFromProtobuf(externalIdMessage)).toSet[ExternalId],
      emailDomains = msg.emailDomains.toSet[String],
      respondent = respondentFromProtobuf(msg.respondent.getOrElse(RespondentMessage())),
      hmdaFilerFlag = msg.hmdaFilerFlag,
      parent = parentFromProtobuf(msg.parent.getOrElse(ParentMessage())),
      assets = msg.assets,
      otherLenderCode = msg.otherLenderCode,
      topHolder = topHolderFromProtobuf(msg.topHolder.getOrElse(TopHolderMessage()))
    )
  }

  def agencyToProtobuf(obj: Agency): AgencyMessage = {
    obj.value match {
      case 9 => AgencyMessage.CFPB
      case 3 => AgencyMessage.FDIC
      case 2 => AgencyMessage.FRS
      case 7 => AgencyMessage.HUD
      case 5 => AgencyMessage.NCUA
      case 1 => AgencyMessage.OCC
      case -1 => AgencyMessage.UNDETERMINED
    }
  }

  def agencyFromProtobuf(msg: AgencyMessage): Agency = {
    msg.value match {
      case 0 => Agency.CFPB
      case 1 => Agency.FDIC
      case 2 => Agency.FRS
      case 3 => Agency.HUD
      case 4 => Agency.NCUA
      case 5 => Agency.OCC
      case 6 => Agency.UndeterminedAgency
    }
  }

  def institutionTypeToProtobuf(obj: InstitutionType): InstitutionTypeMessage = {
    obj.entryName match {
      case "bank" => InstitutionTypeMessage.BANK
      case "credit-union" => InstitutionTypeMessage.CREDIT_UNION
      case "savings-and-loan" => InstitutionTypeMessage.SAVINGS_AND_LOAN
      case "independent-mortgage-company" => InstitutionTypeMessage.INDEPENDENT_MORTGAGE_COMPANY
      case "mortgage-banking-subsidiary" => InstitutionTypeMessage.MBS
      case "affiliate" => InstitutionTypeMessage.AFFILIATE
      case "undetermined-institution-type" => InstitutionTypeMessage.UNDETERMINED_INSTITUTION_TYPE
    }
  }

  def institutionTypeFromProtobuf(msg: InstitutionTypeMessage): InstitutionType = {
    msg.value match {
      case 0 => InstitutionType.Bank
      case 1 => InstitutionType.CreditUnion
      case 2 => InstitutionType.SavingsAndLoan
      case 3 => InstitutionType.IndependentMortgageCompany
      case 4 => InstitutionType.MBS
      case 5 => InstitutionType.Affiliate
      case 6 => InstitutionType.UndeterminedInstitutionType
    }
  }

  def externalIdToProtobuf(obj: ExternalId): ExternalIdMessage = {
    ExternalIdMessage(
      value = obj.value,
      externalIdType = externalIdTypeToProtobuf(obj.externalIdType)
    )
  }

  def externalIdFromProtobuf(msg: ExternalIdMessage): ExternalId = {
    ExternalId(
      value = msg.value,
      externalIdType = externalIdTypeFromProtobuf(msg.externalIdType)
    )
  }

  def externalIdTypeToProtobuf(obj: ExternalIdType): ExternalIdTypeMessage = {
    obj.entryName match {
      case "fdic-certificate-number" => ExternalIdTypeMessage.FDIC_CERTO_NO
      case "federal-tax-id" => ExternalIdTypeMessage.FEDERAL_TAX_ID
      case "ncua-charter-id" => ExternalIdTypeMessage.NCUA_CHARTER_ID
      case "occ-charter-id" => ExternalIdTypeMessage.OCC_CHARTER_ID
      case "rssd-id" => ExternalIdTypeMessage.RSSD_ID
      case "undetermined-external-id" => ExternalIdTypeMessage.UNDERTERMINED_EXTERNAL_ID
    }
  }
  def externalIdTypeFromProtobuf(msg: ExternalIdTypeMessage): ExternalIdType = {
    msg.value match {
      case 0 => FdicCertNo
      case 1 => FederalTaxId
      case 2 => NcuaCharterId
      case 3 => OccCharterId
      case 4 => RssdId
      case 5 => UndeterminedExternalId
    }
  }

  def respondentToProtobuf(obj: Respondent): RespondentMessage = {
    RespondentMessage(
      externalId = Some(externalIdToProtobuf(obj.externalId)),
      name = obj.name,
      state = obj.state,
      city = obj.city,
      fipsStateNumber = obj.fipsStateNumber
    )
  }
  def respondentFromProtobuf(msg: RespondentMessage): Respondent = {
    Respondent(
      externalId = externalIdFromProtobuf(msg.externalId.getOrElse(ExternalIdMessage())),
      name = msg.name,
      state = msg.state,
      city = msg.city,
      fipsStateNumber = msg.fipsStateNumber
    )
  }

  def parentToProtobuf(obj: Parent): ParentMessage = {
    ParentMessage(
      respondentId = obj.respondentId,
      idRssd = obj.idRssd,
      name = obj.name,
      city = obj.city,
      state = obj.state
    )
  }
  def parentFromProtobuf(msg: ParentMessage): Parent = {
    Parent(
      respondentId = msg.respondentId,
      idRssd = msg.idRssd,
      name = msg.name,
      city = msg.city,
      state = msg.state
    )
  }

  def topHolderToProtobuf(obj: TopHolder): TopHolderMessage = {
    TopHolderMessage(
      idRssd = obj.idRssd,
      name = obj.name,
      city = obj.city,
      state = obj.state,
      country = obj.country
    )
  }
  def topHolderFromProtobuf(msg: TopHolderMessage): TopHolder = {
    TopHolder(
      idRssd = msg.idRssd,
      name = msg.name,
      city = msg.city,
      state = msg.state,
      country = msg.country
    )
  }

}
