package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.{Conductor, Licencia}

import java.sql.{Date, PreparedStatement, ResultSet, SQLException}
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
      
      var licencias: List[Licencia] = List.empty

      while (rs.next()) {
        licencias = licencias :+ resultSetParaLicencia(rs)
      }
      licencias
    }
  }

  def obtenerUltimaLicencia(): Either[Unit,Long]={
    ConectorBaseDato.conConexion { conn =>
      try {
        val query =
          """
           SELECT id 
           FROM licencia 
           ORDER BY id DESC 
           LIMIT 1
         """

        val stmt = conn.prepareStatement(query)
        val rs = stmt.executeQuery()

        if (rs.next()) {
          Right(rs.getLong("id"))
        } else {
          Left("No se encontraron licencias registradas")
        }
      } catch {
        case e: SQLException => Left(s"Error de BD: ${e.getMessage}")
      }
    }
  }

  def editarLicenciaConsulta(licencia: Licencia): Unit = {
    licencia.id match {
      case None => Left("No se encuentra el id_licencia")
      case Some(id)=>
        ConectorBaseDato.conConexion { conn =>
          try {
            val consulta =
              """
          UPDATE licencia
          SET moto = ?, automovil = ?, camion = ?, omnibus = ?
          WHERE id = ?
        """
            val stmt = conn.prepareStatement(consulta)
            stmt.setBoolean(1,licencia.moto)
            stmt.setBoolean(2,licencia.automovil)
            stmt.setBoolean(3,licencia.camion)
            stmt.setBoolean(4,licencia.omnibus)
            stmt.setLong(5,id)

            if (stmt.executeUpdate() == 1) Right(())
            else Left("Licencia no encontrada")
          } catch {
            case e: SQLException => Left(s"Error de BD: ${e.getMessage}")
          }
        }
    }

  }

}
