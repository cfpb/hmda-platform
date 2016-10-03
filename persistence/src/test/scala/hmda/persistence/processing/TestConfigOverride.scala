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
      |      filing = "hmda.persistence.institutions.serialization.FilingPersistenceProtobufSerializer"
      |      hmdaRawFile = "hmda.persistence.processing.serialization.HmdaRawFileProtobufSerializer"
      |      hmdaFileParser = "hmda.persistence.processing.serialization.HmdaFileParserProtobufSerializer"
      |  }
      |
      |  serialization-bindings {
      |    "hmda.persistence.institutions.FilingPersistence$FilingCreated" = filing
      |    "hmda.persistence.institutions.FilingPersistence$FilingStatusUpdated" = filing
      |    "hmda.persistence.processing.HmdaRawFile$LineAdded" = hmdaRawFile
      |    "hmda.persistence.processing.HmdaFileParser$TsParsed" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileParser$TsParsedErrors" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileParser$LarParsed" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileParser$LarParsedErrors" = hmdaFileParser
      |  }
      |}
    """.stripMargin

}
