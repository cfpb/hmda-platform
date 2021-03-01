package hmda.api.http.model

final case class HmdaServiceStatus(status: String, service: String, time: String, host: String, version: String)
