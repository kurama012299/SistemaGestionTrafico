package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.Conductor

import java.sql.{PreparedStatement, ResultSet}

object ConsultaConductor {

  private def resultSetParaConductor(rs: ResultSet): Conductor = {
    Conductor.conductorSinId(
      nombre = rs.getString("Nombre"),
      apellido = rs.getString("Apellidos"),
      licencia = rs.getString("Correo"),
      telefono = rs.getString("Telefono")
    )
  }

  def obtenerConductorPorId(id: Long): Option[Conductor] = {
    ConectorBaseDato.conConexion{ conn =>
      val consulta = "SELECT * FROM \"Persona\" WHERE \"Id\" = ?"
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
}


