package infraestructura

import java.sql.{Connection,DriverManager,ResultSet}

object ConectorBaseDato {
  val url = "jdbc:postgresql://localhost:5432/SGL"
  val user = "postgres"
  val password = "012299"

  Class.forName("org.postgresql.Driver")


  @throws[Exception]
  def getConexion(): Connection = {
    DriverManager.getConnection(url, user, password)
  }
  def conConexion[T](block: Connection => T): T = {
    val connection = getConexion()
    try {
      block(connection)
    } finally {
      if (connection != null && !connection.isClosed) {
        connection.close()
      }
    }
  }
}
