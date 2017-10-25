package microservices.sample.kafka

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

object config {
  final case class ScalaIOConfig(
    name: String,
    timeout: Timeout,
    passivation: Duration,
    kafka: KafkaCfg,
    server: ServerCfg,
    cluster: ClusterCfg,
    persistence: PersistenceCfg
  )

  object ScalaIOConfig {
    def apply(config: Config): ScalaIOConfig =
      ScalaIOConfig(
        name = config.getString("scalaio.name"),
        passivation = FiniteDuration(config.getDuration("scalaio.passivation").toNanos, TimeUnit.NANOSECONDS),
        timeout = Timeout(config.getDuration("scalaio.timeout").getSeconds, TimeUnit.SECONDS),
        kafka = KafkaCfg(config.getConfig("scalaio.kafka")),
        server = ServerCfg(config.getConfig("scalaio.server")),
        cluster = ClusterCfg(config.getConfig("akka.cluster")),
        persistence = PersistenceCfg(config.getConfig("scalaio.persistence"))
      )
  }

  final case class PersistenceCfg(eventsForSnapshots: Int, snapshotsToKeep: Int)

  object PersistenceCfg {
    def apply(config: Config): PersistenceCfg =
      PersistenceCfg(
        eventsForSnapshots = config.getInt("eventsForSnapshots"),
        snapshotsToKeep = config.getInt("snapshotsToKeep")
      )
  }

  final case class KafkaCfg(consumer: ConsumerCfg, producer: ProducerCfg)

  object KafkaCfg {
    def apply(config: Config): KafkaCfg =
      KafkaCfg(
        producer = ProducerCfg(config.getConfig("producer")),
        consumer = ConsumerCfg(config.getConfig("consumer")),
      )
  }

  final case class ClusterCfg(nodes: List[String])

  object ClusterCfg {
    def apply(config: Config): ClusterCfg =
      ClusterCfg(
        nodes = config.getStringList("seed-nodes").asScala.toList)
  }

  final case class ServerCfg(host: String, port: Int)

  object ServerCfg {
    def apply(config: Config): ServerCfg =
      ServerCfg(
        host = config.getString("host"),
        port = config.getInt("port"))
  }

  final case class ProducerCfg(brokers: String, topic: String)

  object ProducerCfg {
    def apply(config: Config): ProducerCfg = {
      ProducerCfg(
        brokers = config.getString("brokers"),
        topic = config.getString("topic")
      )
    }
  }

  final case class ConsumerCfg(brokers: String, topic: String, groupId: String)

  object ConsumerCfg {
    def apply(config: Config): ConsumerCfg = {
      ConsumerCfg(
        brokers = config.getString("brokers"),
        topic = config.getString("topic"),
        groupId = config.getString("groupId")
      )
    }
  }

}