package microservices.sample.kafka

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.pattern.ask
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import microservices.sample.kafka.InternalModels.EntityEnvelope
import microservices.sample.kafka.config.ScalaIOConfig
import microservices.sample.write.Models.AttendConferenceCommand
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.concurrent.Future


object ScalaIOConsumer {
  def consumerSettings(config: ScalaIOConfig)(implicit as: ActorSystem) = ConsumerSettings(as, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(config.kafka.consumer.brokers)
    .withGroupId(config.kafka.consumer.groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  import purecsv.safe._

  def start(
     handlerActor: ActorRef,
     config: ScalaIOConfig)(
     implicit
     as: ActorSystem,
     t: Timeout, ec: ExecutionContext,
     m: Materializer): Future[Done] =
    Consumer.committableSource(
      consumerSettings(config),
      Subscriptions.topics(config.kafka.consumer.topic))
      .mapAsync(1) { msg =>
        CSVReader[AttendConferenceCommand]
          .readCSVFromString(msg.record.value()) match {
          case List(Success(cmd)) =>
            (handlerActor ? EntityEnvelope(cmd.attendeeId, cmd))
              .map(_ => msg.committableOffset)
          case _ =>
            as.log.warning("Invalid empty input [{}]", msg.record.value())
            Future.successful(msg.committableOffset)
        }
      }
      // Commit in batches of 20
      .batch(max = 20, first =>
        CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
          batch.updated(elem)
      }
      // Parallelize commits
      .mapAsync(3)(_.commitScaladsl())
      .runWith(Sink.ignore)
}
