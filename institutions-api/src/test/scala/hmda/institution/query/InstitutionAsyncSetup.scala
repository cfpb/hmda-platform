package hmda.institution.query

import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

abstract class InstitutionAsyncSetup
    extends AsyncWordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with InstitutionSetup {

  override def beforeAll() = {
    super.beforeAll()
    setup()
  }

  override def afterAll() = {
    super.afterAll()
    tearDown()
  }

}
