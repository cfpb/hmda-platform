package hmda.validation.engine.lar.`macro`

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.validation.engine.lar.`macro`.MacroAggregationActor.MacroData
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.validation.engine.lar.`macro`.MacroAggregationActor._

class MacroAggregationActorSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem()

  override def afterAll(): Unit = {
    system.terminate()
  }

  val macroAggregations = system.actorOf(MacroAggregationActor.props)

  val probe = TestProbe()

  val id1 = SubmissionId("0", "2017", 1)
  val id2 = SubmissionId("0", "2017", 2)

  "Macro Aggregator" must {
    "Insert, update and read summary macro data" in {
      val d1 = MacroData(200, 2000000)
      probe.send(macroAggregations, AddMacroAggregation(id1, d1))
      probe.send(macroAggregations, GetMacroData(id1))
      probe.expectMsg(d1)

      val d2 = MacroData(400, 10000000000L)
      probe.send(macroAggregations, AddMacroAggregation(id2, d2))
      probe.send(macroAggregations, GetMacroData(id2))
      probe.expectMsg(d2)

      val d2Updated = MacroData(100, 0)
      probe.send(macroAggregations, AddMacroAggregation(id2, d2Updated))
      probe.send(macroAggregations, GetMacroData(id2))
      probe.expectMsg(d2Updated)

    }
  }

}
