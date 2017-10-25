package microservices.sample.kafka

import java.net.URLDecoder

import akka.actor.{ActorLogging, ActorRef, PoisonPill, Props, ReceiveTimeout}
import akka.persistence._

import scala.io.Codec
import InternalModels.{Ack, AttendeeState}
import akka.cluster.sharding.ShardRegion.Passivate
import microservices.sample.kafka.config.ScalaIOConfig
import microservices.sample.write.Models._

class AttendeeEntityActor(
  behaviour: AttendeeBizLogic,
  output: ActorRef,
  config: ScalaIOConfig) extends PersistentActor with ActorLogging {

  context.setReceiveTimeout(config.passivation)

  var state: AttendeeState = AttendeeState(0)

  override def persistenceId: String =
    URLDecoder.decode(self.path.name, Codec.UTF8.name)

  override def receiveRecover: Receive = {
    case event: Event =>
      log.debug(s"Recovering $event...")
      applyEvent(event)
    case SnapshotOffer(_, snapshot: AttendeeState) =>
      log.debug(s"Recovering snapshot $snapshot...")
      state = snapshot
    case RecoveryCompleted =>
      log.debug("Recovery completed!")
  }

  override def receiveCommand: Receive = {
    case command: AttendConferenceCommand =>

      log.debug("New command received: {}", command)
      val event = handleCommand(command)

      persist(event) { persistedEvent =>
        applyEvent(persistedEvent)

        output ! persistedEvent
      }

      sender ! Ack

    case SaveSnapshotSuccess(metadata) =>
      log.debug("Snapshot saved")
      deleteSnapshots(SnapshotSelectionCriteria.create(
        metadata.sequenceNr - config.persistence.snapshotsToKeep,
        metadata.timestamp))
    case SaveSnapshotFailure(_, cause) => log.error(cause, "Snapshot failed")
    case DeleteSnapshotsSuccess(_) => log.debug("Snapshots cleaned.")
    case DeleteSnapshotsFailure(_, cause) => log.error(cause, "Snapshots clean failed.")
    case ReceiveTimeout =>
      log.debug("ReceiveTimeout: passivating. ")
      context.parent ! Passivate(stopMessage = PoisonPill)
  }

  override def persist[B](event: B)(handler: B ⇒ Unit): Unit = {
    super.persist(event)(handler)
    if (super.lastSequenceNr % config.persistence.eventsForSnapshots == 0) {
      log.debug("Saving snapshot! (lastSequenceNr: {})", super.lastSequenceNr)
      saveSnapshot(state)
    } else {
      log.debug("Skipping snapshot")
    }
  }

  private[this] def handleCommand(command: Command): Event = command match {
    case c: AttendConferenceCommand => ConferenceAttendedEvent(c.attendeeId, c.conferenceName)
  }

  private[this] def applyEvent(event: Event): Unit = event match {
    case event: ConferenceAttendedEvent =>
      state = behaviour.attendConference(state, event.conference)
  }
}

object AttendeeEntityActor {

  def props(behaviour: AttendeeBizLogic, output: ActorRef, config: ScalaIOConfig): Props = Props(new AttendeeEntityActor(behaviour, output, config))
}
