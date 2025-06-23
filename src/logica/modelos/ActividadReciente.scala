package logica.modelos

case class ActividadReciente(
                            fecha: java.util.Date,tipo:String,entidad:String,detalles:String,objetoEntidad:AnyRef,objetoEntidadModificada:AnyRef
                            )

object ActividadReciente {
  
  def actividadReciente(
                         fecha: java.util.Date,
                         tipo:String,
                         entidad:String,
                         detalles:String,
                         objetoEntidad:AnyRef,
                         objetoEntidadModificada:AnyRef
                       ): ActividadReciente ={
    ActividadReciente(fecha,tipo, entidad, detalles, objetoEntidad, objetoEntidadModificada)
  }
  private val listeners =scala.collection.mutable.ListBuffer[()=> Unit]()
  private val actividades = scala.collection.mutable.ListBuffer[ActividadReciente]()
  private val MAX_ACTIVIDADES = 10 // Límite de actividades a mostrar

  def registrarAgregarEliminar(tipo: String, entidad: String, detalles: String, objetoEntidad:AnyRef): Unit = {
    val nuevaActividad = ActividadReciente(new java.util.Date(), tipo, entidad, detalles,objetoEntidad,null)
    actividades.prepend(nuevaActividad)

    // Mantener solo las últimas actividades
    if (actividades.size > MAX_ACTIVIDADES) {
      actividades.remove(MAX_ACTIVIDADES, actividades.size - MAX_ACTIVIDADES)
    }
    listeners.foreach(_.apply())
  }

  def registrarEditar(tipo: String, entidad: String, detalles: String, objetoEntidad: AnyRef,objetoEntidadModificada:AnyRef): Unit = {
    val nuevaActividad = ActividadReciente(new java.util.Date(), tipo, entidad, detalles, objetoEntidad, objetoEntidadModificada)
    actividades.prepend(nuevaActividad)

    // Mantener solo las últimas actividades
    if (actividades.size > MAX_ACTIVIDADES) {
      actividades.remove(MAX_ACTIVIDADES, actividades.size - MAX_ACTIVIDADES)
    }
    listeners.foreach(_.apply())
  }

  def addListener(listener: => Unit):Unit={
    listeners += (()=>listener)
  }
  def obtenerRecientes(): List[ActividadReciente] = actividades.toList
}

