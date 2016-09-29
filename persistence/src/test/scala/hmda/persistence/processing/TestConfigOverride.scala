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
      |      hmdaFileParser = "hmda.persistence.processing.serialization.HmdaFileParserProtobufSerializer"
      |      hmdaFileValidator = "hmda.persistence.processing.serialization.HmdaFileValidatorProtobufSerializer"
      |  }
      |
      |  serialization-bindings {
      |    "hmda.persistence.processing.HmdaRawFile$LineAdded" = hmdaRawFile
      |    "hmda.persistence.processing.HmdaFileParser$TsParsed" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileParser$TsParsedErrors" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileParser$LarParsed" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileParser$LarParsedErrors" = hmdaFileParser
      |    "hmda.persistence.processing.HmdaFileValidator$TsValidated" = hmdaFileValidator
      |    "hmda.persistence.processing.HmdaFileValidator$LarValidated" = hmdaFileValidator
      |    "hmda.validation.engine.ValidationError" = hmdaFileValidator
      |  }
      |}
    """.stripMargin

}
