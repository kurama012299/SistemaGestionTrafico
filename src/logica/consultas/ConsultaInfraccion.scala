package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.Infraccion

import java.sql.{PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer


object ConsultaInfraccion {
  private def resultSetParaInfraccion(rs: ResultSet): Infraccion = {
    Infraccion.infraccion(
      id = Some(rs.getLong("id")),
      id_licencia = rs.getLong("id_licencia"),
      ptos_deducidos = rs.getInt("puntos_deducidos"),
      gravedad = rs.getString("gravedad"),
      fecha = rs.getDate("fecha")
    )
  }

  def obtenerInfraccionesPorId(id: Long): Option[Infraccion] = {
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

  def obtenerTodasLasInfracciones(): List[Infraccion] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM infraccion"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      val rs: ResultSet = stmt.executeQuery()

      val infracciones = ListBuffer.empty[Infraccion]

      while (rs.next()) {
        infracciones += resultSetParaInfraccion(rs)
      }

      infracciones.toList
    }
  }
}




