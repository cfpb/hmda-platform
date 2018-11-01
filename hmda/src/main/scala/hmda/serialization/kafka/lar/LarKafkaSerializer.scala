package hmda.serialization.kafka.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.serialization.kafka.KafkaSerializer
import hmda.serialization.filing.lar.LoanApplicationRegisterProtobufConverter._

class LarKafkaSerializer extends KafkaSerializer[LoanApplicationRegister] {
  override def serialize(topic: String,
                         data: LoanApplicationRegister): Array[Byte] = {
    loanApplicationRegisterToProtobuf(data).toByteArray
  }
}
