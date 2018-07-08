package persistency

import model.Entities.{Event, EventElement}
import play.api.Logger
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

trait DBLayer {

  val db = Database.forConfig("mysql")

  val events = TableQuery[Events]

class Events(tag: Tag) extends Table[Event](tag, "Events") {

  def eventId = column[String]("EVENTID", O.PrimaryKey)
  def docId = column[String]("DOCID")
  def name = column[String]("NAME")
  def text = column[String]("TEXT")
  def * = (eventId,docId,name,text).mapTo[Event]
}


  def deleteEvent(eventElement: EventElement):Future[EventElement]={
    Logger.error("Deleting failed event from SQL!")
    val event: Event = eventElement.element.get
    val query = events.filter(_.eventId === event.eventId)
    val result: Future[Int] = db.run(query.delete)
    result.map(_=>eventElement.copy(element = Failure(new Exception("Failed to Persist to Kafka. " +
      "Therefore event deleted from SQL!")))).recoverWith{
      case ex:Throwable => Logger.error("Failed to Delete Event In SQL",ex)
        Future(eventElement.copy(element = Failure(ex)))
    }

  }


  def persistInSQL(evenElement:EventElement):Future[EventElement]={
    val event: Event = evenElement.element.get
    println(s"Persisting id ${event.eventId}")

    db.run(DBIO.seq(
      events += event
    )).map( _ => evenElement).recover{case ex:Throwable=>
      Logger.error(s"Was not able to Persist the Event with id ${event.eventId}",ex)
      evenElement.copy(element = Failure(ex))}
  }

}
