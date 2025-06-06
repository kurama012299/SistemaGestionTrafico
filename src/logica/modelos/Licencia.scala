package logica.modelos

import java.util.Date

case class Licencia(id:Option[Long],moto:Boolean,automovil:Boolean,camion:Boolean,
                    omnibus:Boolean,puntos:Integer,fechaEmision:Date,fechaVencimiento:Date)

object Licencia
{
  def licenciaSinId(
                     moto:Boolean,
                     automovil:Boolean,
                     camion:Boolean,
                     omnibus:Boolean,
                     puntos:Integer,
                     fechaEmision:Date,
                     fechaVencimiento:Date
                   ):Licencia = Licencia(None,moto, automovil, camion, omnibus, puntos,
                                          fechaEmision, fechaVencimiento)

  def licenciaConId(
                     id:Option[Long],
                     moto: Boolean,
                     automovil: Boolean,
                     camion: Boolean,
                     omnibus: Boolean,
                     puntos: Integer,
                     fechaEmision: Date,
                     fechaVencimiento: Date
                   ): Licencia = Licencia(id, moto, automovil, camion, omnibus, puntos,
    fechaEmision, fechaVencimiento)
}

