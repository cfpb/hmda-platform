package hmda.persistence.serialization.institution

import hmda.model.institution._

object InstitutionProtobufConverter {

  def respondentToProtobuf(respondent: Respondent): RespondentMessage = {
    val name = respondent.name.getOrElse("")
    val state = respondent.state.getOrElse("")
    val city = respondent.city.getOrElse("")
    RespondentMessage(name, state, city)
  }

  def respondentFromProtobuf(msg: RespondentMessage): Respondent = {
    val name = if (msg.name != "") Some(msg.name) else None
    val state = if (msg.state != "") Some(msg.state) else None
    val city = if (msg.city != "") Some(msg.city) else None
    Respondent(name, state, city)

  }

  def parentToProtobuf(parent: Parent): ParentMessage = {
    val idRssd = parent.idRssd.getOrElse(0)
    val name = parent.name.getOrElse("")
    ParentMessage(idRssd, name)
  }

  def parentFromProtobuf(msg: ParentMessage): Parent = {
    val idRssd = if (msg.idRssd != 0) Some(msg.idRssd) else None
    val name = if (msg.name != "") Some(msg.name) else None
    Parent(idRssd, name)
  }

  def topHolderToProtobuf(topHolder: TopHolder): TopHolderMessage = {
    val idRssd = topHolder.idRssd.getOrElse(0)
    val name = topHolder.name.getOrElse("")
    TopHolderMessage(idRssd, name)
  }

  def topHolderFromProtobuf(msg: TopHolderMessage): TopHolder = {
    val idRssd = if (msg.idRssd != 0) Some(msg.idRssd) else None
    val name = if (msg.name != "") Some(msg.name) else None
    TopHolder(idRssd, name)
  }

  def institutionToProtobuf(i: Institution): InstitutionMessage = {
    InstitutionMessage(
      activityYear = i.activityYear,
      lei = i.LEI.getOrElse(""),
      agency = i.agency.map(a => a.code).getOrElse(-1),
      institutionType = i.institutionType.map(x => x.code).getOrElse(-1),
      id2017 = i.institutionId_2017.getOrElse(""),
      taxId = i.taxId.getOrElse(""),
      rssd = i.rssd.getOrElse(""),
      emailDomains = i.emailDomains.getOrElse(Nil),
      respondent = Some(respondentToProtobuf(i.respondent)),
      parent = Some(parentToProtobuf(i.parent)),
      assets = i.assets.getOrElse(0),
      otherLenderCode = i.otherLenderCode.getOrElse(0),
      topHolder = Some(topHolderToProtobuf(i.topHolder)),
      hmdaFilter = i.hmdaFiler
    )
  }

  def institutionFromProtobuf(msg: InstitutionMessage): Institution = {
    Institution.empty.copy(
      activityYear = msg.activityYear,
      LEI = if (msg.lei == "") None else Some(msg.lei),
      agency = Some(Agency.valueOf(msg.agency)),
      institutionType = Some(InstitutionType.valueOf(msg.institutionType)),
      institutionId_2017 = if (msg.id2017 == "") None else Some(msg.id2017),
      taxId = if (msg.taxId == "") None else Some(msg.taxId),
      rssd = if (msg.rssd == "") None else Some(msg.rssd),
      emailDomains =
        if (msg.emailDomains.isEmpty) None else Some(msg.emailDomains.toList),
      respondent =
        respondentFromProtobuf(msg.respondent.getOrElse(RespondentMessage())),
      parent = parentFromProtobuf(msg.parent.getOrElse(ParentMessage())),
      assets = if (msg.assets == 0) None else Some(msg.assets),
      otherLenderCode =
        if (msg.otherLenderCode == 0) None else Some(msg.otherLenderCode),
      topHolder =
        topHolderFromProtobuf(msg.topHolder.getOrElse(TopHolderMessage())),
      hmdaFiler = msg.hmdaFilter
    )
  }

}
