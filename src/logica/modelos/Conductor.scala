package logica.modelos

case class Conductor(id:Option[Long], nombre: String, apellido: String, licencia:Option[Licencia], telefono: String)

object Conductor {
  def conductor(
                      id: Option[Long],
                      nombre: String,
                      apellido: String,
                      licencia: Option[Licencia],
                      telefono: String
                    ): Conductor = {
    Conductor(id, nombre, apellido, licencia, telefono)
  }
  
  def conductorSinId(
                    nombre:String,
                    apellido:String,
                    licencia: Option[Licencia],
                    telefono:String
                    ): Conductor = Conductor(None,nombre,apellido,licencia, telefono)

  def conductorSinIdYSinLicencia(
                      nombre: String,
                      apellido: String,
                      telefono: String
                    ): Conductor = Conductor(None, nombre, apellido,None, telefono)

}

