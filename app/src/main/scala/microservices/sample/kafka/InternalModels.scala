package microservices.sample.kafka

import microservices.sample.write.Models.Payload

object InternalModels {
  final case class EntityEnvelope(id: String, payload: Payload)

  final case class AttendeeState(attendedConferences: Int)

  final case object Ack
}
