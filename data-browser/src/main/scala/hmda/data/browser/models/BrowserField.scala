package hmda.data.browser.models

case class BrowserField(name: String,
                        value: Seq[String],
                        dbName: String,
                        redisName: String)
