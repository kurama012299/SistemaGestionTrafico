package logica.modelos

import java.util.Date
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

case class Licencia(id:Option[Long],moto:Boolean,automovil:Boolean,camion:Boolean,
                    omnibus:Boolean,puntos:Integer,fechaEmision:Date,fechaVencimiento:Date) {

  def obtenerCategorias(): ArrayBuffer[String] = {
    val arreglo= ArrayBuffer[String]()
    val arregloString=ArrayBuffer[String]("Moto","Automovil","Camion","Omnibus")
    val arregloBoolean=ArrayBuffer[Boolean](moto,automovil,camion,omnibus)
    for(i <- arregloBoolean.indices){
      if(arregloBoolean(i)==true)
        arreglo+=arregloString(i).toString
    }
    arreglo
  }
}

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
                   ): Licencia ={
    Licencia(id, moto, automovil, camion, omnibus, puntos,
      fechaEmision, fechaVencimiento)
  }

}




