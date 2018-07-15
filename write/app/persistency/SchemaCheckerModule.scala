package persistency

import java.sql.SQLSyntaxErrorException

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Inject}
import javax.inject.Singleton
import play.api.Logger
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class SchemaCheckerModule extends AbstractModule{

  override def configure (): Unit = {
    bind(classOf[SchemaChecker]).asEagerSingleton()
  }
}

@Singleton
class SchemaChecker  @Inject()(system: ActorSystem) extends EventRepository with MySQLComponent {

  Logger.info("Updating Schema!!!")

  try {
    Await.result(db.run(DBIO.seq(events.schema.create))
    , Duration.Inf)
  }catch {
    case _:SQLSyntaxErrorException => Logger.error(s"Table ${events.baseTableRow.name} already exists")
    case ex :Throwable => Logger.error("Error Occured!",ex )
  }



}