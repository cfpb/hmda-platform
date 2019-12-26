package hmda.dashboard.models

import hmda.dashboard.models.{HealthCheckStatus => Status}

case class HealthCheckResponse(db: Status)
