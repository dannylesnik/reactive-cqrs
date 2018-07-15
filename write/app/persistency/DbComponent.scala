package persistency

import slick.jdbc.JdbcProfile


trait DbComponent {

  val profile: JdbcProfile

  def db: profile.backend.DatabaseDef





}
