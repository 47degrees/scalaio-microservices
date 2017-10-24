package scalaio.sample

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}

import scalaio.sample.Models.EntityEnvelope

object AttendeeShardingRegion {

  def apply(output: ActorRef)(implicit system: ActorSystem): ActorRef = {

    val numberOfShards = 3

    val extractEntityId: ShardRegion.ExtractEntityId = {
      case EntityEnvelope(id, payload) ⇒ (id, payload)
    }

    val extractShardId: ShardRegion.ExtractShardId = {
      case EntityEnvelope(id, _) ⇒ (id.hashCode % numberOfShards).toString
    }

    ClusterSharding(system).start(
      typeName = "Attendees",
      entityProps = AttendeeEntityActor.props(output),
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId)
  }
}