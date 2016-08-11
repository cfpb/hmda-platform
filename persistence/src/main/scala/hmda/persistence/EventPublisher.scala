package hmda.persistence

import hmda.persistence.CommonMessages.Event

trait EventPublisher {
  def publishEvent(e: Event): Unit
}
