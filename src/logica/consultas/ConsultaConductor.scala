package logica.consultas

import infraestructura.ConectorBaseDato
import logica.modelos.{ActividadReciente, Conductor, Licencia}

import java.sql.{Connection, Date, PreparedStatement, ResultSet, SQLException}
import scala.collection.mutable.ListBuffer

object ConsultaConductor {

  private def resultSetParaConductor(rs: ResultSet): Conductor = {
    Conductor.conductor(
      id = Some(rs.getLong("carnet_identidad")),
      nombre = rs.getString("nombre"),
      apellido = rs.getString("apellido"),
      licencia = ConsultaLicencia.obtenerLicenciaPorId(rs.getLong("id_licencia")),
      telefono = rs.getString("Telefono")
    )
  }

  def obtenerConductorPorCI(id: Long): Option[Conductor] = {
    ConectorBaseDato.conConexion { conn =>
      val consulta = "SELECT * FROM conductor WHERE carnet_identidad = ?"
      val stmt: PreparedStatement = conn.prepareStatement(consulta)
      stmt.setLong(1, id)
      val rs: ResultSet = stmt.executeQuery()

      if (rs.next()) {
        Some(resultSetParaConductor(rs))
      }
      else {
        None
      }
    }

  }

  def obtenerConductorPorLicencia(id_Licencia: Long): Option[Conductor] = {
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

  def obtenerInfoLicencia(conductor: Conductor): String = conductor.licencia match {
    case Some(lic) => s"${lic.id}"
  }

  def crearConductorYLicencia(conductor: Conductor, licencia: Licencia): Unit = {
    val conn = ConectorBaseDato.getConexion() // Obtiene conexión directamente
    try {
      conn.setAutoCommit(false)

      val consultaLicencia =
        """INSERT INTO "licencia" ("moto", "automovil",
                 "camion", "omnibus", "puntos","fecha_emision","fecha_vencimiento")
                 VALUES (?, ?, ?, ?, ?, ?, ?)
                 RETURNING ("id")"""

      val fechaEmisionSQL: java.sql.Date = new Date(licencia.fechaEmision.getTime)
      val fechaVencimientoSQL: java.sql.Date = new Date(licencia.fechaVencimiento.getTime)
      val stmtLicencia = conn.prepareStatement(consultaLicencia)
      stmtLicencia.setBoolean(1, licencia.moto)
      stmtLicencia.setBoolean(2, licencia.automovil)
      stmtLicencia.setBoolean(3, licencia.camion)
      stmtLicencia.setBoolean(4, licencia.omnibus)
      stmtLicencia.setInt(5, licencia.puntos)
      stmtLicencia.setDate(6, fechaEmisionSQL)
      stmtLicencia.setDate(7, fechaVencimientoSQL)
      val rs = stmtLicencia.executeQuery()

      if (!rs.next()) throw new SQLException("Fallo al crear licencia")
      val idLicencia = rs.getLong(1)
      stmtLicencia.close()


      val insertConductor =
        """INSERT INTO "conductor" ("carnet_identidad", "nombre",
                 "apellido", "telefono", "id_licencia")
                 VALUES (?, ?, ?, ?, ?)"""

      var carnetIdentidad = Long.MinValue
      conductor.id match {
        case Some(conductor: Long) =>
          carnetIdentidad = conductor
        case None =>
          Left(new Exception("No se encontro el id del conductor"))
      }
      val stmtConductor = conn.prepareStatement(insertConductor)
      stmtConductor.setLong(1, carnetIdentidad)
      stmtConductor.setString(2, conductor.nombre)
      stmtConductor.setString(3, conductor.apellido)
      stmtConductor.setString(4, conductor.telefono)
      stmtConductor.setLong(5, idLicencia)

      if (stmtConductor.executeUpdate() != 1) {
        throw new SQLException("Fallo al crear conductor")
      }
      stmtConductor.close()

      conn.commit() // Confirma si todo va bien

    } catch {
      case e: Exception =>
        conn.rollback() // Revierte en caso de error
        throw e // Relanza la excepción
    } finally {
      conn.setAutoCommit(true)
      conn.close()
    }

    val categorias = new StringBuilder
    licencia.obtenerCategorias().foreach { categoria =>
      if (categorias.nonEmpty) categorias.append(", ")
      categorias.append(categoria)
    }
    ActividadReciente.registrarAgregarEliminar("AGREGAR", "Conductor", conductor.nombre + " " + conductor.apellido,conductor)
    ActividadReciente.registrarAgregarEliminar("AGREGAR", "Licencia", categorias.toString(),licencia)
  }

  def editarConductorConsulta(conductor: Conductor, ciViejo: Long): Unit = {
    conductor.id match {
      case None => Left("Id del conductor incorrecto")
      case Some(id) =>
        val conductorViejo=obtenerConductorPorCI(ciViejo.toLong)
        conductorViejo match
          case None =>
            println("No se encontro el conductor")
          case Some(conductorViejo)=>
            ActividadReciente.registrarEditar("MODIFICAR","Conductor",conductor.nombre+" "+conductor.apellido,conductor,conductorViejo)
        ConectorBaseDato.conConexion { conn =>
          try {
            val consulta =
              """
          UPDATE conductor
          SET nombre = ?, apellido = ?, telefono = ?,carnet_identidad = ?
          WHERE carnet_identidad = ?
        """
            val stmt = conn.prepareStatement(consulta)
            stmt.setString(1, conductor.nombre)
            stmt.setString(2, conductor.apellido)
            stmt.setString(3, conductor.telefono)
            stmt.setLong(4, id)
            stmt.setLong(5, ciViejo)

            if (stmt.executeUpdate() == 1) Right(())
            else Left("Conductor no encontrado")
          } catch {
            case e: SQLException => Left(s"Error de BD: ${e.getMessage}")
          }
        }

    }

  }

  def eliminarConductorCompleto(carnetIdentidad: Long, idLicencia: Long): Unit = {
    val resultado=ConectorBaseDato.conConexion { conn =>
      try {
        conn.setAutoCommit(false)
        val licencia = ConsultaLicencia.obtenerLicenciaPorId(idLicencia)
        val categorias = new StringBuilder
        licencia match
          case Some(licencia) =>
            licencia.obtenerCategorias().foreach { categoria =>
              if (categorias.nonEmpty) categorias.append(", ")
              categorias.append(categoria)
              }
          case None =>
            println("No hay licencia")

        val conductor = obtenerConductorPorCI(carnetIdentidad)
        var conductorNombre = " "
        conductor match
          case Some(conductor) =>
            conductorNombre = conductor.nombre + " " + conductor.apellido
            val licenciaEliminar = ConsultaLicencia.obtenerLicenciaPorId(idLicencia)
            licenciaEliminar match
              case None =>
                println("No se encuentra la licencia")
              case Some(licenciaEliminar) => {
                  ActividadReciente.registrarAgregarEliminar("ELIMINAR", "Licencia", categorias.toString() + "  " + conductorNombre, licenciaEliminar)
                  ActividadReciente.registrarAgregarEliminar("ELIMINAR", "Conductor", conductorNombre, conductor)
                }
          case None =>
            println("No hay conductor")


        val idLicenciaOption:Option[Long]=Some(idLicencia)
        idLicenciaOption.foreach(ConsultaInfraccion.eliminarInfraccionesDeLicencia(conn, _))
        eliminarConductor(conn, carnetIdentidad)
        idLicenciaOption.foreach(ConsultaLicencia.eliminarLicencia(conn, _))
        conn.commit()
        true
      } catch {
        case e: SQLException =>
          try {
            conn.setAutoCommit(true)
          } catch {
            case _: SQLException =>
          }
          Left(s"Error al eliminar conductor: ${e.getMessage}")
          false

      } finally {
        try {
        } catch {
          case _: SQLException =>
        }
      }
    }
    if(!resultado){
      println("ERROR al eliminar conductor")
    }
  }

  def eliminarConductor(conn: Connection, idConductor: Long): Unit = {
    var stmt: PreparedStatement = null
    try {
      val query = "DELETE FROM conductor WHERE carnet_identidad = ?"
      stmt = conn.prepareStatement(query)
      stmt.setLong(1, idConductor)
      stmt.executeUpdate()
    } finally {
      if (stmt != null) stmt.close()
      conn.setAutoCommit(false)
    }
  }
}




