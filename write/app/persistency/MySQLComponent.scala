package persistency


import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api._


trait MySQLComponent extends DbComponent{

  override val profile = MySQLProfile

  override val db: profile.backend.Database = Database.forConfig("mysql")

}
