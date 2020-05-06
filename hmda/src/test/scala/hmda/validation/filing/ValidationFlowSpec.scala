package hmda.validation.filing

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation._
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.validation.Seq
import hmda.validation.context.ValidationContext
import hmda.validation.filing.ValidationFlow._
import org.scalatest.{ MustMatchers, WordSpec }

import scala.collection.immutable._

class ValidationFlowSpec extends WordSpec with MustMatchers {

  implicit val system       = ActorSystem()
  implicit val materializer = Materializer(system)

  val cleanTsTxt = List(
    "1|Bank0|2018|4|Mr. Smug Pockets|555-555-5555|pockets@ficus.com|1234 Hocus Potato Way|Tatertown|UT|84096|9|2|01-0123456|B90YWS6AFX2LGWOXJ1LD\n"
  )
  val cleanLarTxt = List(
    "2|95GVQQ61RS6CWQF0SZD9|95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92|20180914|1|1|2|1|1|570420|1|20180916|1234 Hocus Potato Way|Tatertown|RI|29801|36085|36085011402|3||||||11||14|12|13||2|2|6||||||||8||||||||3|4|2|2|1|1|85|104|NA|4|1|3|1|746|8888|7||9||10|||||NA|8636|8597|2362|9120|18.18|29|NA|65|329|34|2|1|2|1|5909430|3|5|24|18|1|2|12345678|6||||||17||||||2|2|2\n",
    "2|B90YWS6AFX2LGWOXJ1LD|B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89|NA|1|32|2|1|2|356709|6|20180908|1234 Hocus Potato Way|Tatertown|DC|63114|22051|22051025001|4|||||ZUDIP0BD1KDWWIF5E96QGIRIQLRBMLT1CEU42OY6R21HA7LDCI080|4|||||I1N7USFJJ99NVF41XPM1VTGDWOP8ZLEICH4P89VJ2YICHHHMDGDD90SBTSVPYMO3X3NAX96H6DEBPYC9NTKAE651CDW5ZHP|3|3|7|||||MLRGW12RN1406P0XP2KA6TKLDISDRBB9EBN2ZM2S7PLQZELIM0N619TC87JDWA5NQXR94N|UM|10EOG6ICA8M18J1NHCKNT7HAGBQRI5UZQTB4EAAA27UJT2FPWQS28YSDR5CCB06H34EL5RTOBRDUPTP4YU9VWXBN6U7KBUD5|4|||||7I0YNQBHNJC0K12XVX4FNJRK0EB0FNZEIIHAP23EIY3BHHE7JNEWQMF4BVUJW8SLIUSGT8LBN4B1B412EB5RF6Z4TQ9G|K2G17CZW08C|FDWBDIYCGUILGNHGCY1Z6R1RAG3BOTAVT|3|1|4|6|3|2|8888|24|NA|1|NA|3|1|8888|8888|9||9||10|||||NA|NA|7964|1525|8054|11.11|NA|NA|NA|159|19|1111|1111|1111|1111|555271|3|5|24|17|3|3|12345678|6||||||17||||||1111|1111|2\n"
  )

  val errorTsTxt = List(
    "3|Bank0|2018|4|Mr. Smug Pockets|555-555-5555||1234 Hocus Potato Way|Tatertown|UT|84096|9|10|01-0123456|B90YWS6AFX2LGWOXJ1LD\n"
  )

  val errorLarTxt = List(
    "3|95GVQQ61RS6CWQF0SZD9|95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92|20180914|1|20|2|1|1|570420|1|20180916|1234 Hocus Potato Way|Tatertown|RI|29801|36085|36085011402|3||||||11||14|12|13||2|2|6||||||||8||||||||3|4|2|2|1|1|85|104|NA|4|1|3|1|746|8888|7||9||10|||||NA|8636|8597|2362|9120|18.18|29|NA|65|329|34|2|1|2|1|5909430|3|5|24|18|1|2|12345678|6||||||17||||||2|2|2\n",
    "2|B90YWS6AFX2LGWOXJ1LD|B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89|NA|1|32|2|1|2|356709|6|20180908|1234 Hocus Potato Way|Tatertown|DC|63114|22051|22051025001|4|||||ZUDIP0BD1KDWWIF5E96QGIRIQLRBMLT1CEU42OY6R21HA7LDCI080|4|||||I1N7USFJJ99NVF41XPM1VTGDWOP8ZLEICH4P89VJ2YICHHHMDGDD90SBTSVPYMO3X3NAX96H6DEBPYC9NTKAE651CDW5ZHP|25|3|7|||||MLRGW12RN1406P0XP2KA6TKLDISDRBB9EBN2ZM2S7PLQZELIM0N619TC87JDWA5NQXR94N|UM|10EOG6ICA8M18J1NHCKNT7HAGBQRI5UZQTB4EAAA27UJT2FPWQS28YSDR5CCB06H34EL5RTOBRDUPTP4YU9VWXBN6U7KBUD5|4|||||7I0YNQBHNJC0K12XVX4FNJRK0EB0FNZEIIHAP23EIY3BHHE7JNEWQMF4BVUJW8SLIUSGT8LBN4B1B412EB5RF6Z4TQ9G|K2G17CZW08C|FDWBDIYCGUILGNHGCY1Z6R1RAG3BOTAVT|3|1|4|6|3|2|8888|24|NA|1|NA|3|1|8888|8888|9||9||10|||||NA|NA|7964|1525|8054|11.11|NA|NA|NA|159|19|1111|1111|1111|1111|555271|3|5|24|17|3|3|12345678|6||||||17||||||1111|1111|2\n"
  )

  val mapRecordID = ListMap("Record Identifier" -> "3")
  val mapV601 = ListMap(
    "Financial Institution Name"             -> "Bank0",
    "Contact Person's Office Street Address" -> "1234 Hocus Potato Way",
    "Contact Person's E-mail Address"        -> "",
    "Contact Person's Office City"           -> "Tatertown",
    "Contact Person's Name"                  -> "Mr. Smug Pockets"
  )
  val mapV629_1 = ListMap(
    "Ethnicity of Applicant or Borrower: 1"                                                      -> "4",
    "Ethnicity of Applicant or Borrower: 2"                                                      -> "0",
    "Ethnicity of Applicant or Borrower: 3"                                                      -> "0",
    "Ethnicity of Applicant or Borrower: 4"                                                      -> "0",
    "Ethnicity of Applicant or Borrower: 5"                                                      -> "0",
    "Ethnicity of Applicant or Borrower: Free Form Text Field for Other Hispanic or Latino"      -> "ZUDIP0BD1KDWWIF5E96QGIRIQLRBMLT1CEU42OY6R21HA7LDCI080",
    "Ethnicity of Applicant or Borrower Collected on the Basis of Visual Observation or Surname" -> "25"
  )
  val mapV630 = ListMap(
    "Ethnicity of Applicant or Borrower: 1"                                                      -> "4",
    "Ethnicity of Applicant or Borrower Collected on the Basis of Visual Observation or Surname" -> "25"
  )

  val cleanRows: Seq[String] = cleanTsTxt ++ cleanLarTxt
  val errorRows: Seq[String] = errorTsTxt ++ errorLarTxt

  "Validation Flow" must {
    val cleanHmdaFileSource = Source.fromIterator(() => cleanRows.toIterator)
    val cleanTs             = TsCsvParser(cleanRows.head).getOrElse(TransmittalSheet())
    val cleanLars           = cleanRows.tail.map(s => LarCsvParser(s).getOrElse(LoanApplicationRegister()))
    val errorHmdaFileSource = Source.fromIterator(() => errorRows.toIterator)

    "validate Transmittal Sheet" in {
      cleanHmdaFileSource
        .take(1)
        .map(ByteString(_))
        .via(validateTsFlow("all", ValidationContext(None)))
        .map(_.getOrElse(TransmittalSheet()))
        .runWith(TestSink.probe[TransmittalSheet])
        .request(1)
        .expectNext(cleanTs)
        .request(1)
        .expectComplete()
    }

    "validate list of Loan Application Register" in {
      cleanHmdaFileSource
        .drop(1)
        .map(ByteString(_))
        .via(validateLarFlow("all", ValidationContext(None)))
        .map(_.getOrElse(LoanApplicationRegister()))
        .runWith(TestSink.probe[LoanApplicationRegister])
        .request(cleanLars.size)
        .expectNextN(cleanLars)
        .request(1)
        .expectComplete()
    }

    "validate a full HMDA File" in {
      cleanHmdaFileSource
        .map(ByteString(_))
        .via(validateHmdaFile("all", ValidationContext(None)))
        .collect {
          case Right(p) => p
        }
        .runWith(TestSink.probe[PipeDelimited])
        .request(1)
        .expectNext(cleanTs)
        .request(cleanRows.size - 1)
        .expectNextN(cleanLars)
        .request(1)
        .expectComplete()
    }

    "collect errors in Transmittal Sheet" in {
      errorHmdaFileSource
        .take(1)
        .map(ByteString(_))
        .via(validateTsFlow("all", ValidationContext(None)))
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ValidationError]])
        .request(1)
        .expectNext(
          List(
            SyntacticalValidationError("B90YWS6AFX2LGWOXJ1LD", "S300", TsValidationError, mapRecordID),
            ValidityValidationError("B90YWS6AFX2LGWOXJ1LD", "V601", TsValidationError, mapV601)
          )
        )
        .request(1)
        .expectComplete()
    }

    "filter syntactical errors in Transmittal Sheet" in {
      errorHmdaFileSource
        .take(1)
        .map(ByteString(_))
        .via(validateTsFlow("syntactical", ValidationContext(None)))
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ValidationError]])
        .request(1)
        .expectNext(List(SyntacticalValidationError("B90YWS6AFX2LGWOXJ1LD", "S300", TsValidationError, mapRecordID)))
        .request(1)
        .expectComplete()
    }

    "filter validity errors in Transmittal Sheet" in {
      errorHmdaFileSource
        .take(1)
        .map(ByteString(_))
        .via(validateTsFlow("validity", ValidationContext(None)))
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ValidationError]])
        .request(1)
        .expectNext(List(ValidityValidationError("B90YWS6AFX2LGWOXJ1LD", "V601", TsValidationError, mapV601)))
        .request(1)
        .expectComplete()
    }

    "collect errors in Loan Application Register" in {
      errorHmdaFileSource
        .map(ByteString(_))
        .via(validateLarFlow("all", ValidationContext(None)))
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ValidationError]])
        .request(1)
        .expectNext(
          List(
            SyntacticalValidationError("95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92", "S300", LarValidationError, mapRecordID),
            ValidityValidationError(
              "95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92",
              "V612-1",
              LarValidationError,
              ListMap("Loan Purpose" -> "20", "Preapproval" -> "2")
            )
          )
        )
        .request(1)
        .expectNext(
          List(
            ValidityValidationError("B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89", "V629-1", LarValidationError, mapV629_1),
            ValidityValidationError("B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89", "V630", LarValidationError, mapV630)
          )
        )
        .request(1)
        .expectComplete()
    }

    "filter syntactical errors in Loan Application Register" in {
      errorHmdaFileSource
        .map(ByteString(_))
        .via(validateLarFlow("syntactical", ValidationContext(None)))
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ValidationError]])
        .request(1)
        .expectNext(
          List(SyntacticalValidationError("95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92", "S300", LarValidationError, mapRecordID))
        )
        .request(1)
        .expectComplete()
    }

    "filter validity errors in Loan Application Register" in {
      errorHmdaFileSource
        .map(ByteString(_))
        .via(validateLarFlow("validity", ValidationContext(None)))
        .collect {
          case Left(errors) => errors
        }
        .runWith(TestSink.probe[List[ValidationError]])
        .request(1)
        .expectNext(
          List(
            ValidityValidationError(
              "95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92",
              "V612-1",
              LarValidationError,
              ListMap("Loan Purpose" -> "20", "Preapproval" -> "2")
            )
          )
        )
        .request(1)
        .expectNext(
          List(
            ValidityValidationError("B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89", "V629-1", LarValidationError, mapV629_1),
            ValidityValidationError("B90YWS6AFX2LGWOXJ1LDNIXOQ6OO7BRA5SLR6FSJJ5R89", "V630", LarValidationError, mapV630)
          )
        )
        .request(1)
        .expectComplete()
    }

  }

}