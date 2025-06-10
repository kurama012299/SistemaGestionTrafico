package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.Infraccion

import java.sql.{PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer

class ConsultaInfraccion {

  private def resultSetParaInfraccion(rs: ResultSet): Infraccion = {
    Infraccion(
      id = Some(rs.getLong("id")),
      id_licencia = rs.getLong("id_licencia"),
      puntos = rs.getInt("puntos_deducidos"),
      gravedad = rs.getString("gravedad"),
      fecha = rs.getDate("fecha")
    )
  }


  def obtenerTodasLasInfracciones(): List[Infraccion] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM infraccion"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      val rs: ResultSet = stmt.executeQuery()

      var infracciones: List[Infraccion] = List.empty

      while (rs.next()) {
        infracciones = infracciones :+ resultSetParaInfraccion(rs)
      }

      infracciones
    }
  }

  def obtenerInfraccionPorId(id: Long): Option[Infraccion] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM infraccion WHERE id = ?"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      stmt.setLong(1, id)
      val rs: ResultSet = stmt.executeQuery()

      if (rs.next()) {
        Some(resultSetParaInfraccion(rs))
      }
      else {
        None
      }
    }
  }
  
  def crearInfraccionConsulta(infraccion: Infraccion): Either[Throwable, Unit] = {
    ConectorBaseDato.conConexion { conn =>
      var stmt: java.sql.PreparedStatement = null
      try {
        conn.setAutoCommit(false)

        val consultaInfraccion =
          """INSERT INTO "Infraccion" ("id_licencia", "puntos", 
           "gravedad", "fecha")
           VALUES (?, ?, ?, ?)"""

        stmt = conn.prepareStatement(consultaInfraccion)
        stmt.setLong(1,infraccion.id_licencia)
        stmt.setInt(2, infraccion.puntos)
        stmt.setString(3, infraccion.gravedad)
        stmt.setDate(4, new java.sql.Date(infraccion.fecha.getTime)) // Conversión explícita a java.sql.Date

        val filasAfectadas = stmt.executeUpdate()

        if (filasAfectadas == 1) {
          conn.commit()
          Right(())
        } else {
          conn.rollback()
          Left(new RuntimeException("No se pudo insertar la infracción")) // Mejor mensaje de error
        }
      } catch {
        case e: Throwable =>
          if (conn != null) conn.rollback()
          Left(e)
      } finally {
        if (stmt != null) stmt.close()
        if (conn != null) conn.setAutoCommit(true)
      }
    }
  }
}

