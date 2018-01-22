package hmda.persistence.processing

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.FlowShape
import akka.stream.scaladsl.{ Balance, Flow, GraphDSL, Merge }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.LarEngine

object HmdaFileWorker extends LarEngine {

  def validate(ctx: ValidationContext, replyTo: ActorRef) = {
    Flow[LoanApplicationRegister]
      .map { lar =>
        replyTo ! lar
        validateLar(lar, ctx).toEither
      }
  }

  def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int): Flow[In, Out, NotUsed] = {
    import GraphDSL.Implicits._

    Flow.fromGraph(GraphDSL.create() { implicit b =>
      val balancer = b.add(Balance[In](workerCount, waitForAllDownstreams = true))
      val merge = b.add(Merge[Out](workerCount))

      for (_ <- 1 to workerCount) {
        balancer ~> worker.async ~> merge
      }

      FlowShape(balancer.in, merge.out)
    })
  }

}
