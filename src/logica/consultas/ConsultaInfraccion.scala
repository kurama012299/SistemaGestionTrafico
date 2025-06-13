package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.Infraccion

import java.sql.{PreparedStatement, ResultSet, SQLException}
import scala.collection.mutable.ListBuffer

object ConsultaInfraccion {

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
  
  def crearInfraccionConsulta(infraccion: Infraccion): Unit = {
   val conn = ConectorBaseDato.conConexion { conn =>
      try {
        conn.setAutoCommit(false)

        val consultaInfraccion =
          """INSERT INTO "infraccion" ("id_licencia", "puntos_deducidos",
           "gravedad", "fecha")
           VALUES (?, ?, ?, ?)"""

        val stmt = conn.prepareStatement(consultaInfraccion)
        stmt.setLong(1, infraccion.id_licencia)
        stmt.setInt(2, infraccion.puntos)
        stmt.setString(3, infraccion.gravedad)
        stmt.setDate(4, new java.sql.Date(infraccion.fecha.getTime)) // Conversión explícita a java.sql.Date
        stmt.executeUpdate()
        stmt.close()

        val updateLicencia =
          """
             UPDATE "licencia"
             SET puntos = puntos + ?
             WHERE "id" = ? AND puntos <= 36
           """
        val stmtLicencia = conn.prepareStatement(updateLicencia)
        stmtLicencia.setInt(1, infraccion.puntos)
        stmtLicencia.setLong(2, infraccion.id_licencia)

        val filasActualizadas = stmtLicencia.executeUpdate()
        stmtLicencia.close()

        // Verificación
        if (filasActualizadas != 1) {
          throw new SQLException("Licencia no encontrada o puntos insuficientes")
        }

        conn.commit() // Confirmar transacción
        println("Infracción registrada y puntos actualizados")

      } catch {
        case e: SQLException =>
          conn.rollback()
          println(s"Error transaccional: ${e.getMessage}")
          throw e
      } finally {
        conn.setAutoCommit(true)
        conn.close()
      }
    }
  }

  def editarInfraccionConsulta(infraccion: Infraccion,puntosAnteriores:Int): Unit = {
    infraccion.id match
      case None => Left("No se pudo encontrar la infraccion")
      case Some(id)=>
        ConectorBaseDato.conConexion { conn =>
          try {
            val puntosCambiaron=infraccion.puntos != puntosAnteriores
            val consulta =
              """
              UPDATE infraccion
              SET puntos_deducidos = ?, gravedad = ?, fecha = ?
              WHERE id = ?
            """
            val stmtInfraccion = conn.prepareStatement(consulta)
            try{
              stmtInfraccion.setInt(1, infraccion.puntos)
              stmtInfraccion.setString(2, infraccion.gravedad)
              stmtInfraccion.setDate(3, new java.sql.Date(infraccion.fecha.getTime))
              stmtInfraccion.setLong(4, id)
              if (stmtInfraccion.executeUpdate() != 1)
                return Left("Infracción no encontrada")
            }finally {
              stmtInfraccion.close()
            }

            if(puntosCambiaron){
              val licencia=ConsultaLicencia.obtenerLicenciaPorId(infraccion.id_licencia)
              licencia.get.id match{
                case None => Left("No se pudo encontrar la licencia")
                case Some(idLicencia)=>
                  val diferencia=(puntosAnteriores-infraccion.puntos)
                  val consultaLicencia =
                    """
                       UPDATE "licencia"
                       SET puntos = puntos + ?
                       WHERE id = ? AND puntos <=36
                     """
                  val stmLicencia = conn.prepareStatement(consultaLicencia)
                  try{
                    stmLicencia.setInt(1, -diferencia)
                    stmLicencia.setLong(2, idLicencia)
                    if (stmLicencia.executeUpdate() != 1 )
                      return Left("no se encontro Licencia")
                  }finally{
                    stmLicencia.close()
                  }

              }


            }
            Right(())
          } catch {
            case e: SQLException => Left(s"Error de BD: ${e.getMessage}")
          }
        }
  }

}

