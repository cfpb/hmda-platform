package hmda.publisher.helper

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{FreeSpec}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class RetryUtilsTest extends FreeSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "retry" in {
    var runNumber = 0
    val ftr = () => Future({
      runNumber +=1
      throw new RuntimeException
    })
    assertThrows[RuntimeException] {
      RetryUtils.retry(3, 5.millis)(ftr).futureValue
    }
    assert(runNumber == 4)

  }

}