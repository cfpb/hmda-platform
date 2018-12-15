package hmda.analytics.query

import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

abstract class TransmittalSheetAsyncSetup
    extends AsyncWordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with TransmittalSheetSetup {

  override def beforeAll(): Unit = {
    super.beforeAll()
    setup()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    tearDown()
  }

}
