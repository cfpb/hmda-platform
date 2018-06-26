package hmda.persistence.serialization.institution

import hmda.model.institution._

object InstitutionProtobufConverter {

  def respondentToProtobuf(
      respondent: Respondent): Option[RespondentMessage] = {
    if (respondent.isEmpty) {
      None
    } else {
      val name = respondent.name.getOrElse("")
      val state = respondent.state.getOrElse("")
      val city = respondent.city.getOrElse("")
      Some(RespondentMessage(name, state, city))
    }
  }

  def respondentFromProtobuf(
      msg: Option[RespondentMessage]): Option[Respondent] = {
    if (msg.isEmpty) {
      None
    } else {
      val name = msg.map(r => r.name)
      val state = msg.map(r => r.state)
      val city = msg.map(r => r.city)
      Some(
        Respondent(name, state, city)
      )
    }
  }

  def parentToProtobuf(parent: Parent): Option[ParentMessage] = {
    if (parent.isEmpty) {
      None
    } else {
      val idRssd = parent.idRssd.getOrElse(0)
      val name = parent.name.getOrElse("")
      Some(ParentMessage(idRssd, name))
    }
  }

  def parentFromProtobuf(msg: ParentMessage): Parent = ???

  def topHolderToProtobuf(topHolder: TopHolder): Option[TopHolderMessage] = {
    if (topHolder.isEmpty) {
      None
    } else {
      val idRssd = topHolder.idRssd.getOrElse(0)
      val name = topHolder.name.getOrElse("")
      Some(TopHolderMessage(idRssd, name))
    }
  }

  def topHolderFromProtobuf(msg: TopHolderMessage): TopHolder = ???

  def institutionToProtobuf(i: Institution): InstitutionMessage = {
    InstitutionMessage(
      activityYear = i.activityYear,
      lei = i.LEI.getOrElse(""),
      agency = i.agency.getOrElse(Agency()).code,
      institutionType = i.institutionType.getOrElse(InstitutionType()).code,
      id2017 = i.institutionId_2017.getOrElse(""),
      taxId = i.taxId.getOrElse(""),
      rssd = i.rssd.getOrElse(""),
      emailDomains = i.emailDomains.getOrElse(Nil),
      respondent = respondentToProtobuf(i.respondent),
      parent = parentToProtobuf(i.parent),
      assets = i.assets.getOrElse(0),
      otherLenderCode = i.otherLenderCode.getOrElse(0),
      topHolder = topHolderToProtobuf(i.topHolder),
      hmdaFilter = i.hmdaFiler
    )
  }

  def institutionFromProtobuf(
      institution: Option[InstitutionMessage]): Institution = ???

}
