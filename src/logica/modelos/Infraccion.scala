package logica.modelos

import java.util.Date

case class Infraccion(id:Option[Long],id_licencia:Long,puntos:Integer,gravedad:String,fecha:Date)

object Infraccion{

  def InfraccionConId(
                       id:Option[Long],
                       id_licencia:Long,
                       puntos:Integer,
                       gravedad:String,
                       fecha:Date
                   ): Infraccion = Infraccion(id, id_licencia, puntos, gravedad,fecha)

  def infraccionSinId(
                       id_licencia: Long,
                       puntos: Integer,
                       gravedad: String,
                       fecha: Date
                     ): Infraccion = {
    Infraccion(None, id_licencia, puntos, gravedad, fecha)
  }
}




