package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.Conductor

import java.sql.{PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer

object ConsultaConductor {

  private def resultSetParaConductor(rs: ResultSet): Conductor = {
    Conductor.conductor(
      id =Some(rs.getLong("carnet_identidad")),
      nombre = rs.getString("nombre"),
      apellido = rs.getString("apellido"),
      licencia = ConsultaLicencia.obtenerLicenciaPorId(rs.getLong("id_licencia")),
      telefono = rs.getString("Telefono")
    )
  }

  def obtenerConductorPorCI(id: Long): Option[Conductor] = {
    ConectorBaseDato.conConexion{ conn =>
      val consulta = "SELECT * FROM conductor WHERE carnet_identidad = ?"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      stmt.setLong(1, id)
      val rs:ResultSet=stmt.executeQuery()

      if(rs.next()){
        Some(resultSetParaConductor(rs))
      }
      else{
        None
      }
    }

  }

  def obtenerConductorPorLicencia(id_Licencia:Long): Option[Conductor] ={
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM conductor WHERE id_licencia = ?"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      stmt.setLong(1, id_Licencia)
      val rs: ResultSet = stmt.executeQuery()

      if (rs.next()) {
        Some(resultSetParaConductor(rs))
      }
      else {
        None
      }
    }
  }

  def obtenerTodosLosConductores(): List[Conductor] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM conductor"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      val rs: ResultSet = stmt.executeQuery()

      val conductores = ListBuffer.empty[Conductor]

      while (rs.next()) {
        conductores += resultSetParaConductor(rs)
      }

      conductores.toList
    }
  }

}


