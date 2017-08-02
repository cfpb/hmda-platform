import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext

package object hmda {
  type AS[_] = ActorSystem
  type MAT[_] = ActorMaterializer
  type EC[_] = ExecutionContext
}
