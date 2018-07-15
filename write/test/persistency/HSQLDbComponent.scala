package persistency

import org.slf4j.{Logger, LoggerFactory}

import scala.util.Random

trait HSQLDbComponent extends DbComponent{

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override val profile = slick.jdbc.H2Profile

  import profile.api._

  val randomDB: String = "jdbc:h2:mem:test;"

  val h2Url: String = randomDB + "MODE=MySql;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;"

  override val db: profile.backend.Database = {
    println("Creating test connection ..................................")
    Database.forURL(url = h2Url, driver = "org.h2.Driver")
  }
}

