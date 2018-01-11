package hmda.http.common.model

final case class Status(status: String,
                        service: String,
                        time: String,
                        host: String)
