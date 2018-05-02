package hmda.persistence.serialization.filing

import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler, FindHmdaFilers }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }
import hmda.persistence.model.serialization.HmdaFiler.HmdaFilerMessage
import hmda.persistence.model.serialization.HmdaFilerCommands.{ CreateHmdaFilerMessage, DeleteHmdaFilerMessage, FindHmdaFilerMessage, FindHmdaFilersMessage }
import hmda.persistence.model.serialization.HmdaFilerEvents.{ HmdaFilerCreatedMessage, HmdaFilerDeletedMessage }

object HmdaFilerProtobufConverter {

  def hmdaFilerToProtobuf(obj: HmdaFiler): HmdaFilerMessage = {
    HmdaFilerMessage(
      institutionId = obj.institutionId,
      respondentId = obj.respondentId,
      period = obj.period,
      name = obj.name
    )
  }

  def hmdaFilerFromProtobuf(msg: HmdaFilerMessage): HmdaFiler = {
    HmdaFiler(
      institutionId = msg.institutionId,
      respondentId = msg.respondentId,
      period = msg.period,
      name = msg.name
    )
  }

  def findHmdaFilerToProtobuf(obj: FindHmdaFiler): FindHmdaFilerMessage = {
    FindHmdaFilerMessage(
      institutionId = obj.institutionId
    )
  }

  def findHmdaFilerFromProtobuf(msg: FindHmdaFilerMessage): FindHmdaFiler = {
    FindHmdaFiler(
      institutionId = msg.institutionId
    )
  }

  def findHmdaFilersToProtobuf(obj: FindHmdaFilers): FindHmdaFilersMessage = {
    FindHmdaFilersMessage(
      period = obj.period
    )
  }

  def findHmdaFilersFromProtobuf(msg: FindHmdaFilersMessage): FindHmdaFilers = {
    FindHmdaFilers(
      period = msg.period
    )
  }

  def createHmdaFilerToProtobuf(obj: CreateHmdaFiler): CreateHmdaFilerMessage = {
    CreateHmdaFilerMessage(
      hmdaFiler = Some(hmdaFilerToProtobuf(obj.hmdaFiler))
    )
  }

  def createHmdaFilerFromProtobuf(msg: CreateHmdaFilerMessage): CreateHmdaFiler = {
    CreateHmdaFiler(
      hmdaFiler = hmdaFilerFromProtobuf(msg.hmdaFiler.getOrElse(HmdaFilerMessage()))
    )
  }

  def deleteHmdaFilerToProtobuf(obj: DeleteHmdaFiler): DeleteHmdaFilerMessage = {
    DeleteHmdaFilerMessage(
      hmdaFiler = Some(hmdaFilerToProtobuf(obj.hmdaFiler))
    )
  }

  def deleteHmdaFilerFromProtobuf(msg: DeleteHmdaFilerMessage): DeleteHmdaFiler = {
    DeleteHmdaFiler(
      hmdaFiler = hmdaFilerFromProtobuf(msg.hmdaFiler.getOrElse(HmdaFilerMessage()))
    )
  }

  def hmdaFilerCreatedToProtobuf(obj: HmdaFilerCreated): HmdaFilerCreatedMessage = {
    HmdaFilerCreatedMessage(
      hmdaFiler = Some(hmdaFilerToProtobuf(obj.hmdFiler))
    )
  }

  def hmdaFilerCreatedFromProtobuf(msg: HmdaFilerCreatedMessage): HmdaFilerCreated = {
    HmdaFilerCreated(
      hmdFiler = hmdaFilerFromProtobuf(msg.hmdaFiler.getOrElse(HmdaFilerMessage()))
    )
  }

  def hmdaFilerDeletedToProtobuf(obj: HmdaFilerDeleted): HmdaFilerDeletedMessage = {
    HmdaFilerDeletedMessage(
      hmdaFiler = Some(hmdaFilerToProtobuf(obj.hmdaFiler))
    )
  }

  def hmdaFilerDeletedFromProtobuf(msg: HmdaFilerDeletedMessage): HmdaFilerDeleted = {
    HmdaFilerDeleted(
      hmdaFiler = hmdaFilerFromProtobuf(msg.hmdaFiler.getOrElse(HmdaFilerMessage()))
    )
  }

}
