package hmda.serialization.kafka.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.persistence.serialization.loanapplicationregister.LoanApplicationRegisterMessage
import hmda.serialization.kafka.KafkaDeserializer
import hmda.serialization.filing.lar.LoanApplicationRegisterProtobufConverter._

class LarKafkaDeserializer extends KafkaDeserializer[LoanApplicationRegister] {
  override def deserialize(topic: String,
                           data: Array[Byte]): LoanApplicationRegister = {
    loanApplicationRegisterFromProtobuf(
      LoanApplicationRegisterMessage.parseFrom(data))
  }
}
