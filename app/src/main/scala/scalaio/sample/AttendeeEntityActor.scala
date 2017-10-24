package scalaio.sample

import java.net.URLDecoder

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence._

import scala.io.Codec
import scalaio.sample.Models.{AttendConferenceCommand, AttendeeState, ConferenceAttendedEvent, Event}

class AttendeeEntityActor(output: ActorRef) extends PersistentActor with ActorLogging {

  var state: AttendeeState = AttendeeState(List.empty[String])

  override def persistenceId: String =
    URLDecoder.decode(self.path.name, Codec.UTF8.name)

  override def receiveRecover: Receive = {
    case event: Event =>
      log.info(s"Recovering $event")
      updateState(event)
    case SnapshotOffer(_, snapshot: AttendeeState) =>
      log.info(s"Recovering snapshot $snapshot")
      state = snapshot
    case RecoveryCompleted =>
      log.debug("Recovery completed")
  }

  override def receiveCommand: Receive = {
    case command: AttendConferenceCommand =>
      println("############################ AttendConferenceCommand")
      println(command)
      output ! ConferenceAttendedEvent(command.attendeeId, command.conferenceName)
      sender() ! "Ack command"
    case SaveSnapshotSuccess(_)        => log.info("Snapshot saved")
    case SaveSnapshotFailure(_, cause) => log.error(cause, "Snapshot failed")
    case any =>
      println(s"############ Debugging....$any")
      log.debug("############ Debugging....{}", any)
      sender() ! "Ack any"
  }

  override def persist[B](event: B)(handler: B ⇒ Unit): Unit = {
    super.persist(event)(handler)
    if (super.lastSequenceNr % 5 == 0) {
      log.info("Saving snapshot")
      saveSnapshot(state)
    } else {
      log.info("Skipping snapshot")
    }
  }

  private[this] def updateState(event: Event): Unit = event match {
    case event: ConferenceAttendedEvent =>
      println("########## ConferenceAttendedEvent ##########")

  }
}

object AttendeeEntityActor {

  def props(output: ActorRef): Props = Props(new AttendeeEntityActor(output))
}
