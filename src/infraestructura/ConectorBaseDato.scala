package infraestructura

import java.sql.{Connection,DriverManager,ResultSet}

object ConectorBaseDato {
  val url = "jdbc:postgresql://localhost:5432/sistema_gestion_licencia_scala"
  val user = "postgres"
  val password = "4622"

  Class.forName("org.postgresql.Driver")


  @throws[Exception]
  def getConexion(): Connection = {
    println("Conectando a la base de datos...") // Mensaje antes de intentar conectar
    val connection = DriverManager.getConnection(url, user, password)
    println("¡Conexión exitosa a la base de datos!") // Mensaje después de conectar
    connection
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
