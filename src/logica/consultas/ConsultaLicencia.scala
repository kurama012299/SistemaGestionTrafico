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
      
      var licencias: List[Licencia] = List.empty

      while (rs.next()) {
        licencias = licencias :+ resultSetParaLicencia(rs)
      }

      licencias
    }
  }

  def crearLicenciaConsulta(licencia: Licencia): Either[Throwable, Unit] = {
    ConectorBaseDato.conConexion { conn =>
      try {
        conn.setAutoCommit(false) 
        
        val consultaLicencia =
          """INSERT INTO "Licencia" ("moto", "automovil", 
           "camion", "omnibus", "puntos")
           VALUES (?, ?, ?, ?, ?)"""

        val stmt = conn.prepareStatement(consultaLicencia)
        stmt.setBoolean(1, licencia.moto)
        stmt.setBoolean(2, licencia.automovil)
        stmt.setBoolean(3, licencia.camion)
        stmt.setBoolean(4, licencia.omnibus)
        stmt.setInt(5, licencia.puntos)

        val filasAfectadas = stmt.executeUpdate()
        stmt.close()

        if (filasAfectadas == 1) {
          conn.commit()
          Right(())
        } else {
          conn.rollback()
          Left(new RuntimeException("No se pudo insertar la licencia"))
        }
      } catch {
        case e: Throwable =>
          conn.rollback()
          Left(e)
      } finally {
        conn.setAutoCommit(true)
      }
    }
  }
}
