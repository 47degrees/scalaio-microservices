package microservices.sample.kafka

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}

import InternalModels.EntityEnvelope
import microservices.sample.kafka.config.ScalaIOConfig

object AttendeeShardingRegion {

  def apply(behaviour: AttendeeBizLogic, output: ActorRef, config: ScalaIOConfig)(implicit system: ActorSystem): ActorRef = {

    val numberOfShards = config.cluster.nodes.length

    val extractEntityId: ShardRegion.ExtractEntityId = {
      case EntityEnvelope(id, payload) ⇒ (id, payload)
    }

    val extractShardId: ShardRegion.ExtractShardId = {
      case EntityEnvelope(id, _) ⇒ (id.hashCode % numberOfShards).toString
    }

    ClusterSharding(system).start(
      typeName = "AttendeesShardingRegion",
      entityProps = AttendeeEntityActor.props(behaviour, output, config),
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId)
  }
}