package hmda.persistence.model

import hmda.persistence.messages.CommonMessages.Event

trait EventPublisher {
  def publishEvent(e: Event): Unit
}
