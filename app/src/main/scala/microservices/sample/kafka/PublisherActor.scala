package microservices.sample.kafka

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import microservices.sample.kafka.config.ScalaIOConfig
import microservices.sample.write.Models.ConferenceAttendedEvent

import purecsv.safe._

class PublisherActor(
  config: ScalaIOConfig,
  publisher: KafkaProducer[Array[Byte], String])
  extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: ConferenceAttendedEvent =>
      log.debug("# Publishing output: {}", event)

      publisher.send(new ProducerRecord[Array[Byte], String](
        config.kafka.producer.topic,
        event.toCSV()))
  }
}

object PublisherActor {

  def props: Props = Props[PublisherActor]
}

