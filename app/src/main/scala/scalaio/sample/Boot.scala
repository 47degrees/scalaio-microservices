package scalaio.sample

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scalaio.sample.Models.{AttendConferenceCommand, EntityEnvelope}

object Boot extends App {

  implicit val system = ActorSystem("scalaio-microservices")

  implicit val executionContext = system.dispatcher

  implicit val timeout = Timeout(15, TimeUnit.SECONDS)
  implicit val materialiser: Materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val kafkaProducer: KafkaProducer[Array[Byte], String] = producerSettings.createKafkaProducer()

  val output = system.actorOf(Props(new PublisherActor(kafkaProducer)))
  val attendeesRegion = AttendeeShardingRegion(output)

    val done =
    Consumer.committableSource(consumerSettings, Subscriptions.topics("input"))
      .mapAsync(1) { msg =>
        val (attendeeName, conferenceName) = Try {
          val attendeeMsg = msg.record.value().split(";")
          (Option(attendeeMsg(0)), Option(attendeeMsg(1)))
        } match {
          case Success(values) => values
          case Failure(e) =>
            println("Invalid input")
            (None, None)
        }

        (attendeeName, conferenceName) match {
          case (Some(a), Some(c)) =>
            val attendConferenceCommand = AttendConferenceCommand(a, c)
            (attendeesRegion ? EntityEnvelope(a, attendConferenceCommand))
              .map(_ => msg.committableOffset)
          case _ => Future.successful(msg.committableOffset)
        }
      }
      .batch(max = 20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
        batch.updated(elem)
      }
      .mapAsync(3)(_.commitScaladsl())
      .runWith(Sink.ignore)
}
