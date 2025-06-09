package logica.modelos

import java.util.Date

case class Infraccion(id:Option[Long],id_licencia:Long,ptos_deducidos:Integer,gravedad:String,fecha:Date)

object Infraccion {

  def infraccion(
                  id: Option[Long],
                  id_licencia: Long,
                  ptos_deducidos:Integer,
                  gravedad: String,
                  fecha: Date
                ): Infraccion = {
    Infraccion(id, id_licencia,ptos_deducidos, gravedad, fecha)
  }

  def infraccionSinId(
                       id_licencia: Long,
                       ptos_deducidos:Integer,
                       gravedad: String,
                       fecha: Date
                     ): Infraccion = {
    Infraccion( None,id_licencia,ptos_deducidos, gravedad, fecha)
  }
}


