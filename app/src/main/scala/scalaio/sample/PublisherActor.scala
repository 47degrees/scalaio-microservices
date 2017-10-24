package scalaio.sample

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scalaio.sample.Models.{AttendeeState, ConferenceAttendedEvent}

class PublisherActor(publisher: KafkaProducer[Array[Byte], String]) extends Actor with ActorLogging {

  var state: AttendeeState = AttendeeState(List.empty[String])

  override def receive: Receive = {
    case event: ConferenceAttendedEvent =>
      publisher.send(new ProducerRecord[Array[Byte], String]("output", s"${event.attendee} attended ${event.conference}"))
  }
}

object PublisherActor {

  def props: Props = Props[PublisherActor]
}

