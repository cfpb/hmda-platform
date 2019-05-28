package hmda.data.browser.models

case class BrowserField(name: String = "empty",
                        value: Seq[String] = Seq("*"),
                        dbName: String = "empty",
                        redisName: String = "empty")
