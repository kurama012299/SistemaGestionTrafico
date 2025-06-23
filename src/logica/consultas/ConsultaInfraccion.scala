package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.{ActividadReciente, Infraccion, Licencia}

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}
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

  def contarInfraccionesPorLicencia(idLicencia: Long): Option[Integer] = {
    val resultado:Either[_,Option[Integer]]=ConectorBaseDato.conConexion { conn =>
      try {
        val query = """
        SELECT COUNT(*) AS total
        FROM infraccion
        WHERE id_licencia = ?
      """

        val stmt = conn.prepareStatement(query)
        try {
          stmt.setLong(1, idLicencia)
          val rs = stmt.executeQuery()

          if (rs.next()) {
            Right(Some(rs.getInt("total")))
          } else {
            Right(None)
          }
        } finally {
          stmt.close()
        }
      } catch {
        case _: SQLException =>
          Right(None)
      }
    }
    resultado match
      case Right(option)=> option
      case Left(_)=> None
  }

  
  def crearInfraccionConsulta(infraccion: Infraccion): Unit = {
   ConectorBaseDato.conConexion { conn =>
      try {
        ActividadReciente.registrarAgregarEliminar("AGREGAR", "Infraccion", "Gravedad: " + infraccion.gravedad + " " + "Puntos deducidos: " + infraccion.puntos,infraccion)
        conn.setAutoCommit(false)
        val licenciaVieja=ConsultaLicencia.obtenerLicenciaPorId(infraccion.id_licencia)

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


        (ConsultaLicencia.obtenerLicenciaPorId(infraccion.id_licencia), licenciaVieja) match
          case (Some(licencianNueva), Some(licenciaVieja)) =>
            ActividadReciente.registrarEditar("MODIFICAR", "Licencia", "Puntos: " + licencianNueva.puntos, licencianNueva, licenciaVieja)
          case (None, _) =>
            println("No se encontro la licencia")
          case (_, None) =>
            println("No se encontro la licencia")

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
        val infraccionVieja=obtenerInfraccionPorId(id)
        infraccionVieja match
          case None=>
            Left("No se pudo encontrar la infraccion")
          case Some(infraccionVieja)=>{
            ActividadReciente.registrarEditar("MODIFICAR", "Infraccion", "Gravedad: " + infraccion.gravedad + " " + "Puntos deducidos: " + infraccion.puntos, infraccion, infraccionVieja)
          }

        val licenciaVieja=ConsultaLicencia.obtenerLicenciaPorId(infraccion.id_licencia)
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

        (ConsultaLicencia.obtenerLicenciaPorId(infraccion.id_licencia), licenciaVieja) match
          case (Some(licencianNueva), Some(licenciaVieja)) =>
            ActividadReciente.registrarEditar("MODIFICAR", "Licencia", "Puntos: " + licencianNueva.puntos, licencianNueva, licenciaVieja)
          case (None, _) =>
            println("No se encontro la licencia")
          case (_, None) =>
            println("No se encontro la licencia")
  }

  def eliminarInfraccion(idInfraccion: Long): Unit = {
    ConectorBaseDato.conConexion { conn =>
      try {
        val infraccion=obtenerInfraccionPorId(idInfraccion)
        var puntosInfraccion=0
        infraccion match
          case Some(infraccion)=>
            puntosInfraccion=infraccion.puntos
            ActividadReciente.registrarAgregarEliminar("ELIMINAR","Infraccion","Puntos eliminados: "+puntosInfraccion +"  "+ "Id licencia: "+infraccion.id_licencia.toString,infraccion)
          case None=>
            println("No se encuentra la infraccion")


        // 1. Obtener puntos e id_licencia antes de eliminar
        val selectStmt = conn.prepareStatement(
          "SELECT puntos_deducidos, id_licencia FROM infraccion WHERE id = ?"
        )
        selectStmt.setLong(1, idInfraccion)
        val rs = selectStmt.executeQuery()

        if (!rs.next()) {
          Left("Infracción no encontrada")
        }
        val puntos = rs.getInt("puntos_deducidos")
        val idLicencia = rs.getLong("id_licencia")
        selectStmt.close()

        // 2. Eliminar infracción
        val deleteStmt = conn.prepareStatement("DELETE FROM infraccion WHERE id = ?")
        deleteStmt.setLong(1, idInfraccion)

        if (deleteStmt.executeUpdate() != 1) {
          Left("Error al eliminar infracción")
        }
        deleteStmt.close()

        // 3. Restaurar puntos en licencia
        val updateStmt = conn.prepareStatement(
          "UPDATE licencia SET puntos = puntos - ? WHERE id = ?"
        )
        updateStmt.setInt(1, puntos)
        updateStmt.setLong(2, idLicencia)

        if (updateStmt.executeUpdate() != 1) {
          Left("Error al actualizar puntos de licencia")
        }
        updateStmt.close()

        Right(())
      } catch {
        case e: SQLException =>
          Left(s"Error de base de datos: ${e.getMessage}")
      }
    }
  }

  def eliminarInfraccionesDeLicencia(conn: Connection, idLicencia: Long): Unit = {
    var stmt: PreparedStatement = null
    try {
      val query = "DELETE FROM infraccion WHERE id_licencia = ?"
      stmt = conn.prepareStatement(query)
      stmt.setLong(1, idLicencia)
      stmt.executeUpdate()
    } finally {
      if (stmt != null) stmt.close()
    }
  }

}

