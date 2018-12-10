package hmda

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.ValidatedNel
import hmda.model.validation.ValidationError

import scala.concurrent.ExecutionContext

package object validation {
  type Seq[+A] = collection.immutable.Seq[A]
  type HmdaValidation[+B] = ValidatedNel[ValidationError, B]
  type HmdaValidated[+B] = Either[List[ValidationError], B]

  type AS[_] = ActorSystem
  type MAT[_] = ActorMaterializer
  type EC[_] = ExecutionContext

}
