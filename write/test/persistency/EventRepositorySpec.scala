package persistency

import java.sql.SQLSyntaxErrorException
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import model.Entities.{Event, EventElement}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAll
import play.api.Logger
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Success

class EventRepositorySpec extends Specification with EventRepository with HSQLDbComponent with BeforeAll{


  "EventRepository trait" should{

    implicit val system: ActorSystem = ActorSystem("test")

    "Insert new event" in{
      skipped("db is not running")
      val expectedEvent = Event("event2","docId1","event1","My Event1.")
      val eventElement = EventElement(TestProbe().ref,Success(expectedEvent))
      val result: Future[EventElement] = persistInSQL(eventElement)
      val actualResult: EventElement = Await.result(result,3.seconds)
      expectedEvent shouldEqual actualResult.element.get
    }

    "Insert new Event and select this event" in{
      skipped("db is not running")
      val expectedEvent = Event("event1","docId1","event1","My Event1.")
      val eventElement = EventElement(TestProbe().ref,Success(expectedEvent))
      val result: Future[EventElement] = persistInSQL(eventElement)
      Await.result(result,3.seconds)
      import profile.api._

      val query = events.filter(_.eventId === expectedEvent.eventId).result.head
      val actualResult = Await.result(db.run(query),3.seconds)
      expectedEvent shouldEqual actualResult

    }

    "Insert new Event and Delete it" in {
      skipped("db is not running")
      val expectedEvent = Event("event3","docId1","event1","My Event1.")
      val eventElement = EventElement(TestProbe().ref,Success(expectedEvent))
      val result: Future[EventElement] = persistInSQL(eventElement)
      Await.result(result,3.seconds)

      import profile.api._
      try {
        Await.result(deleteEvent(eventElement), 3.seconds)

      }catch{
        case ex:Exception => Logger.error("Expected exception", ex)
        case ex:Throwable => Logger.error("Other Exception occurs",ex)
      }

      val query = events.filter(_.eventId === expectedEvent.eventId).result.headOption
      val actualResult: Option[Event] = Await.result(db.run(query), 3.seconds)
      actualResult shouldEqual None
    }

  }

  override def beforeAll(): Unit = {
    import profile.api._
    try {
      Logger.info("Creating Schema!!!!")
      val setup = DBIO.seq(
        sqlu"""CREATE TABLE Events(EVENTID varchar(64) PRIMARY KEY,DOCID varchar(200),NAME varchar(200),TEXT varchar(200));"""
      )
      Await.result(db.run(setup)
        , Duration.Inf)
    }catch {
      case _:SQLSyntaxErrorException => Logger.error(s"Table ${events.baseTableRow.name} already exists")
      case ex :Throwable => Logger.error("Error Occured!",ex)
    }
  }

}
