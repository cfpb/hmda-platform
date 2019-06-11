package hmda.data.browser.models

case class BrowserField(name: String = "empty",
                        values: Seq[String] = Seq("*"),
                        dbName: String = "empty",
                        redisName: String = "empty")
