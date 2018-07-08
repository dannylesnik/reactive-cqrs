package controllers

import actors.FlowActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import javax.inject._
import model.Entities._
import persistency.SchemaChecker
import play.api.Logger
import play.api.libs.json.{Json, _}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


@Singleton
class HomeController @Inject()(cc: ControllerComponents,system:ActorSystem, schemaChecker: SchemaChecker) extends AbstractController(cc) {


  val flowActor: ActorRef = system.actorOf(Props[FlowActor],"flow-actor")
  implicit val timeout: Timeout = Timeout(5.seconds)


  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def runEvent(event:Event):Future[FlowResult]={
    val result = flowActor ? event
    result.mapTo[FlowResult]
  }


  def saveEvent: Action[JsValue] = Action.async(parse.json) { request =>
    val event: JsResult[Event] = request.body.validate[Event]
    event.fold(
      errors => {
        Future{BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))}
      },
      event => runEvent(event).map{
        case FlowSuccess => Ok(Json.obj("status" -> "OK"))
        case Error(ex) => Logger.error("was not able to write Event ",ex)
          InternalServerError(Json.obj("status" -> "KO"))
        case Busy => BadGateway(Json.obj("status" -> "KO"))
      }
    )
  }


}


