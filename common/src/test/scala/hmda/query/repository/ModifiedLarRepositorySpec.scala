package hmda.query.repository

import org.scalatest.{MustMatchers, WordSpec}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class ModifiedLarRepositorySpec extends WordSpec with MustMatchers {

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val modifiedLarRepository = new ModifiedLarRepository(databaseConfig)

  "ModifiedLarRepository" should {
    "correctly calculate income categorization" when {
      "income is NA or empty" in {
        val resultNA = modifiedLarRepository.incomeCategorization("NA", 100000)
        assert(resultNA == "NA")
        val resultEmpty = modifiedLarRepository.incomeCategorization("", 100000)
        assert(resultEmpty == "NA")
      }
      "income is less than 50%" in {
        val result = modifiedLarRepository.incomeCategorization("49", 100000)
        assert(result == "<50%")
      }
      "income is between 50-79%" in {
        val result50 = modifiedLarRepository.incomeCategorization("50", 100000)
        assert(result50 == "50-79%")
        val result79 = modifiedLarRepository.incomeCategorization("79", 100000)
        assert(result79 == "50-79%")
      }
      "income is between 80-99%" in {
        val result80 = modifiedLarRepository.incomeCategorization("80", 100000)
        assert(result80 == "80-99%")
        val result99 = modifiedLarRepository.incomeCategorization("99", 100000)
        assert(result99 == "80-99%")
      }
      "income is between 100-119%" in {
        val result100 = modifiedLarRepository.incomeCategorization("100", 100000)
        assert(result100 == "100-119%")
        val result119 = modifiedLarRepository.incomeCategorization("119", 100000)
        assert(result119 == "100-119%")
      }
      "income is 120% or more" in {
        val result = modifiedLarRepository.incomeCategorization("120", 100000)
        assert(result == ">120%")
      }
    }

    "get correct median age calculated" when {
      "age is negative one" in {
        val result = modifiedLarRepository.medianAgeCalculated(2023, -1)
        assert( result == "Age Unknown")
      }
      "age is 54 or more" in {
        val result = modifiedLarRepository.medianAgeCalculated(2023, 54)
        assert(result == "1969 or Earlier")
      }
      "age is between 53 and 44" in {
        val result53 = modifiedLarRepository.medianAgeCalculated(2023, 53)
        assert(result53 == "1970 - 1979")

        val result44 = modifiedLarRepository.medianAgeCalculated(2023, 44)
        assert(result44 == "1970 - 1979")
      }
      "age is between 43 and 34" in {
        val result43 = modifiedLarRepository.medianAgeCalculated(2023, 43)
        assert(result43 == "1980 - 1989")

        val result34 = modifiedLarRepository.medianAgeCalculated(2023, 34)
        assert(result34 == "1980 - 1989")
      }
      "age is between 33 and 24" in {
        val result33 = modifiedLarRepository.medianAgeCalculated(2023, 33)
        assert(result33 == "1990 - 1999")

        val result24 = modifiedLarRepository.medianAgeCalculated(2023, 24)
        assert(result24 == "1990 - 1999")
      }
      "age is between 23 and 13" in {
        val result23 = modifiedLarRepository.medianAgeCalculated(2023, 23)
        assert(result23 == "2000 - 2010")

        val result13 = modifiedLarRepository.medianAgeCalculated(2023, 13)
        assert(result13 == "2000 - 2010")
      }
      "age is less than 13" in {
        val result = modifiedLarRepository.medianAgeCalculated(2023, 12)
        assert(result == "2011 - Present")
      }
    }
  }
}
