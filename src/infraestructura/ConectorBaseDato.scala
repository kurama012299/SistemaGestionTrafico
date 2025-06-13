package infraestructura

import java.sql.{Connection,DriverManager,ResultSet}
import com.zaxxer.hikari.HikariDataSource

object ConectorBaseDato {

  private val dataSource = {
    val ds = new HikariDataSource()
    ds.setJdbcUrl("jdbc:postgresql://localhost:5432/SGT_scala")
    ds.setUsername("postgres")
    ds.setPassword("012299")
    ds.setMaximumPoolSize(10) // Número máximo de conexiones
    ds
  }

  Class.forName("org.postgresql.Driver")


  @throws[Exception]
  def getConexion(): Connection = {
    println("Conectando a la base de datos...") // Mensaje antes de intentar conectar
    val connection = dataSource.getConnection
    println("¡Conexión exitosa a la base de datos!") // Mensaje después de conectar
    connection
  }

  def conConexion[T](block: Connection => T): T = {
    val conn = getConexion()
    try {
      conn.setAutoCommit(false)
      val resultado = block(conn) // Ejecuta el bloque y captura el resultado
      conn.commit() // Confirma si no hay excepciones
      resultado
    } catch {
      case e: Throwable =>
        conn.rollback() // Revierte en caso de error
        throw e // Propaga la excepción
    } finally {
      conn.setAutoCommit(true)
      conn.close()
    }
  }
}
