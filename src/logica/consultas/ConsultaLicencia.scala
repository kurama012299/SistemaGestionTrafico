package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.{Conductor, Licencia}

import java.sql.{PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer

object ConsultaLicencia
{

  private def resultSetParaLicencia(rs: ResultSet): Licencia = {
    Licencia.licenciaConId(
      id = Some(rs.getLong("id")),
      moto = rs.getBoolean("moto"),
      automovil = rs.getBoolean("automovil"),
      camion = rs.getBoolean("camion"),
      omnibus = rs.getBoolean("omnibus"),
      puntos = rs.getInt("puntos"),
      fechaEmision = rs.getDate("fecha_emision"),
      fechaVencimiento = rs.getDate("fecha_vencimiento")
    )
  }

  def obtenerLicenciaPorId(id: Long): Option[Licencia] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM licencia WHERE id = ?"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      stmt.setLong(1, id)
      val rs: ResultSet = stmt.executeQuery()

      if (rs.next()) {
        Some(resultSetParaLicencia(rs))
      }
      else {
        None
      }
    }

  }

  def obtenerTodasLasLicencias(): List[Licencia] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM licencia"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      val rs: ResultSet = stmt.executeQuery()

      val licencias = ListBuffer.empty[Licencia]

      while (rs.next()) {
        licencias += resultSetParaLicencia(rs)
      }

      licencias.toList
    }
  }

}
