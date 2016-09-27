package hmda.persistence.processing

object TestConfigOverride {

  def config: String =
    """
      | akka.loggers = ["akka.testkit.TestEventListener"]
      | akka.loglevel = DEBUG
      | akka.stdout-loglevel = "OFF"
      | akka.persistence.journal.plugin = "inmemory-journal"
      | akka.persistence.query.journal.id = "inmemory-read-journal"
      | akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
      | akka.actor {
      |  serializers {
      |      hmdaRawFile = "hmda.persistence.processing.serialization.HmdaRawFileProtobufSerializer"
      |      hmdaParserFile = "hmda.persistence.processing.serialization.HmdaFileParserProtobufSerializer"
      |    }
      |
      |    serialization-bindings {
      |      "hmda.persistence.processing.HmdaRawFile$LineAdded" = hmdaRawFile
      |      "hmda.persistence.processing.HmdaFileParser$TsParsed" = hmdaParserFile
      |      "hmda.persistence.processing.HmdaFileParser$TsParsedErrors" = hmdaParserFile
      |      "hmda.persistence.processing.HmdaFileParser$LarParsed" = hmdaParserFile
      |      "hmda.persistence.processing.HmdaFileParser$LarParsedErrors" = hmdaParserFile
      |    }
      |}
    """.stripMargin

}
