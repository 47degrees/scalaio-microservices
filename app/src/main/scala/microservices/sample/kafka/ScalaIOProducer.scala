package microservices.sample.kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import microservices.sample.kafka.config.ScalaIOConfig

object ScalaIOProducer {
  private[this] def producerSettings(config: ScalaIOConfig)(
    implicit as: ActorSystem) =
    ProducerSettings(
        system = as,
        keySerializer = new ByteArraySerializer,
        valueSerializer = new StringSerializer)
      .withBootstrapServers(config.kafka.producer.brokers)

  def create(config: ScalaIOConfig)(
    implicit as: ActorSystem): KafkaProducer[Array[Byte], String] =
    producerSettings(config)
      .createKafkaProducer()
}
