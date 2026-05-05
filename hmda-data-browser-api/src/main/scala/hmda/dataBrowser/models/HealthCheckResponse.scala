package hmda.dataBrowser.models

import hmda.dataBrowser.models.{HealthCheckStatus => Status}

case class HealthCheckResponse(cache: Status, db: Status, s3: Status)
