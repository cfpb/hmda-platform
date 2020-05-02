package hmda

import cats.data.ValidatedNel
import hmda.model.validation.ValidationError

package object validation {
  type Seq[+A]            = collection.immutable.Seq[A]
  type HmdaValidation[+B] = ValidatedNel[ValidationError, B]
  type HmdaValidated[+B]  = Either[List[ValidationError], B]
}