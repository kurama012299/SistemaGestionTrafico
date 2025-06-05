package logica.modelos

case class Conductor(id:Option[Long], nombre: String, apellido: String, licencia: String, telefono: String)

object Conductor {
  def conductorSinId(
                    nombre:String,
                    apellido:String,
                    licencia:String,
                    telefono:String
                    ): Conductor = Conductor(None,nombre,apellido,licencia, telefono)
}

