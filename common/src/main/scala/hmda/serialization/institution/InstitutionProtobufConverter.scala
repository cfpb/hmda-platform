package hmda.serialization.institution

import hmda.model.institution._
import hmda.persistence.serialization.institution.{InstitutionMessage, ParentMessage, RespondentMessage, TopHolderMessage}
import hmda.util.CSVConsolidator.listDeDupeToList

object InstitutionProtobufConverter {

  def respondentToProtobuf(respondent: Respondent): RespondentMessage = {
    val name  = respondent.name.getOrElse("")
    val state = respondent.state.getOrElse("")
    val city  = respondent.city.getOrElse("")
    RespondentMessage(name, state, city)
  }

  def respondentFromProtobuf(msg: RespondentMessage): Respondent = {
    val name  = if (msg.name != "") Some(msg.name) else None
    val state = if (msg.state != "") Some(msg.state) else None
    val city  = if (msg.city != "") Some(msg.city) else None
    Respondent(name, state, city)

  }

  def parentToProtobuf(parent: Parent): ParentMessage = {
    val idRssd = parent.idRssd
    val name   = parent.name.getOrElse("")
    ParentMessage(idRssd, name)
  }

  def parentFromProtobuf(msg: ParentMessage): Parent = {
    val idRssd = msg.idRssd
    val name   = if (msg.name != "") Some(msg.name) else None
    Parent(idRssd, name)
  }

  def topHolderToProtobuf(topHolder: TopHolder): TopHolderMessage = {
    val idRssd = topHolder.idRssd
    val name   = topHolder.name.getOrElse("")
    TopHolderMessage(idRssd, name)
  }

  def topHolderFromProtobuf(msg: TopHolderMessage): TopHolder = {
    val idRssd = msg.idRssd
    val name   = if (msg.name != "") Some(msg.name) else None
    TopHolder(idRssd, name)
  }

  def institutionToProtobuf(i: Institution): InstitutionMessage =
    InstitutionMessage(
      activityYear = i.activityYear,
      lei = i.LEI,
      agency = i.agency.code,
      institutionType = i.institutionType.code,
      id2017 = i.institutionId_2017.getOrElse(""),
      taxId = i.taxId.getOrElse(""),
      rssd = i.rssd,
      emailDomains = listDeDupeToList(i.emailDomains),
      respondent = Some(respondentToProtobuf(i.respondent)),
      parent = Some(parentToProtobuf(i.parent)),
      assets = i.assets,
      otherLenderCode = i.otherLenderCode,
      topHolder = Some(topHolderToProtobuf(i.topHolder)),
      hmdaFilter = i.hmdaFiler,
      quarterlyFiler = i.quarterlyFiler,
      quarterlyFilerHasFiledQ1 = i.quarterlyFilerHasFiledQ1,
      quarterlyFilerHasFiledQ2 = i.quarterlyFilerHasFiledQ2,
      quarterlyFilerHasFiledQ3 = i.quarterlyFilerHasFiledQ3,
      notes = i.notes
    )

  def institutionFromProtobuf(msg: InstitutionMessage): Institution =
    Institution.empty.copy(
      activityYear = msg.activityYear,
      LEI = msg.lei,
      agency = Agency.valueOf(msg.agency),
      institutionType = InstitutionType.valueOf(msg.institutionType),
      institutionId_2017 = if (msg.id2017 == "") None else Some(msg.id2017),
      taxId = if (msg.taxId == "") None else Some(msg.taxId),
      rssd = msg.rssd,
      emailDomains = msg.emailDomains,
      respondent = respondentFromProtobuf(msg.respondent.getOrElse(RespondentMessage())),
      parent = parentFromProtobuf(msg.parent.getOrElse(ParentMessage())),
      assets = msg.assets,
      otherLenderCode = msg.otherLenderCode,
      topHolder = topHolderFromProtobuf(msg.topHolder.getOrElse(TopHolderMessage())),
      hmdaFiler = msg.hmdaFilter,
      quarterlyFiler = msg.quarterlyFiler,
      quarterlyFilerHasFiledQ1 = msg.quarterlyFilerHasFiledQ1,
      quarterlyFilerHasFiledQ2 = msg.quarterlyFilerHasFiledQ2,
      quarterlyFilerHasFiledQ3 = msg.quarterlyFilerHasFiledQ3,
      notes = msg.notes
    )

}