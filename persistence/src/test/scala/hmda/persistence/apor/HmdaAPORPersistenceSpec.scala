package hmda.persistence.apor

import akka.testkit.TestProbe
import hmda.persistence.model.ActorSpec
import hmda.persistence.apor.HmdaAPORPersistence._
import hmda.model.apor.APORGenerator._
import hmda.model.apor.{ FixedRate, VariableRate }
import hmda.persistence.messages.CommonMessages.GetState

class HmdaAPORPersistenceSpec extends ActorSpec {

  val aporPersistence = createAPORPersistence(system)
  val probe = TestProbe()

  val apor1 = APORGen.sample.get
  val apor2 = APORGen.sample.get
  val apor3 = APORGen.sample.get
  val apors = List(apor1, apor2, apor3)

  "APOR Persistence" must {
    "create fixed and variable rate APOR" in {
      probe.send(aporPersistence, CreateApor(apor1, FixedRate))
      probe.expectMsg(AporCreated(apor1, FixedRate))
      probe.send(aporPersistence, CreateApor(apor2, FixedRate))
      probe.expectMsg(AporCreated(apor2, FixedRate))
      probe.send(aporPersistence, CreateApor(apor3, VariableRate))
      probe.expectMsg(AporCreated(apor3, VariableRate))
    }
    "Retrieve current state" in {
      probe.send(aporPersistence, GetState)
      probe.expectMsg(HmdaAPORState(List(apor2, apor1), List(apor3)))
    }
  }
}
