package model

import akka.actor.ActorRef
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Try

object Entities {


  case class Event(eventId:String,docId:String,name:String,text:String)

  case object Event extends ((String,String,String, String) => Event) {

    implicit val eventWrites:Writes[Event]{
    } = new Writes[Event] {
      override def writes(event: Event): JsValue = Json.obj(
        "eventId" -> event.eventId,
        "docId" -> event.docId,
        "text" -> event.text,
      "name" -> event.name
      )
    }

    implicit val eventReads: Reads[Event] = (
      (JsPath \ "eventId").read[String] and
        (JsPath \ "docId").read[String] and
        (JsPath \ "name").read[String]  and
        (JsPath \ "text").read[String]
      )(Event.apply _)

  }
  case class EventElement(responseHandler: ActorRef, element: Try[Event])


  sealed trait FlowResult

  case object FlowSuccess extends FlowResult
  case class Error(ex:Throwable) extends FlowResult
  case object Busy extends FlowResult




}
