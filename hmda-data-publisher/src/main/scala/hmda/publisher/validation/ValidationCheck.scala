package hmda.publisher.validation

import scala.concurrent.Future

trait ValidationCheck {
  def check(): Future[Either[String, Unit]]
}