package actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import kafka.KafkaConnector
import model.Entities._
import persistency.{EventRepository, MySQLComponent}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class FlowActor extends Actor with ActorLogging with EventRepository with KafkaConnector with MySQLComponent{

  implicit val sys: ActorSystem = context.system
  implicit val mat: ActorMaterializer = ActorMaterializer()

  private val topic  = "test"


  val (actorSource, done) = Source.actorRef[EventElement](100, OverflowStrategy.fail).
    conflate { (prior, overflown) =>
      Logger.error(s"Dropping overflow ${overflown.element.get.eventId}")
      overflown.responseHandler ! Busy
      prior
  }.buffer(15, OverflowStrategy.backpressure)
    .mapAsync[EventElement](10) (eventElement => persistInSQL(eventElement))
    .mapAsync[EventElement](10){eventElement =>sentToKafka(eventElement)}
    .toMat(Sink.foreach{
      case EventElement(handler, Success(_)) =>
        handler ! FlowSuccess
      case EventElement(handler, Failure(ex)) =>
        handler ! Error(ex)
    })(Keep.both)
    .run()

  override def receive: Receive = {

    case event:Event => val senderRef = sender()
      val eventElement = EventElement(senderRef,Success(event))
      actorSource ! eventElement
    case e@_ => unhandled(s"Unhandled message $e")
  }





  def sentToKafka(evenElement: EventElement):Future[EventElement]={

    val result: Future[EventElement] = evenElement.element match{
      case Success(event) =>
        Logger.info(s"""Persisting Event ${event.eventId} to Kafka!!""")
        deliverToKafka(event)(topic).map(_ => evenElement).recoverWith {
          case ex: Throwable =>
            Logger.error("Failed to persist event to Kafka!",ex)
            deleteEvent(evenElement)
        }
      case Failure(_) =>
        Logger.info("Failed event will not be persisted to kafka")
       Future{evenElement}
    }

    result
  }


}
