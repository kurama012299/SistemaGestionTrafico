package logica

import java.time.{LocalDate, Period}
import java.time.format.{DateTimeFormatter, DateTimeParseException, ResolverStyle}
import javax.swing.JOptionPane

class Validador {

  private val formatter = DateTimeFormatter.ofPattern("ddMMuu")
    .withResolverStyle(ResolverStyle.STRICT)

  /**
   * Método que valida un string según ciertas reglas y muestra un panel de error si falla
   *
   * @param texto El string a validar
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación
   */

  import javax.swing.JOptionPane

  def validarString(texto: String): Unit = {
    try {
      // Validación 1: No nulo y no vacío
      if (texto == null || texto.trim.isEmpty) {
        showErrorDialog("Error: El campo no puede estar vacío")
        throw new IllegalArgumentException("Texto vacío o nulo")
      }

      // Validación 2: Solo letras (incluye acentos y ñ/Ñ)
      if (!texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
        showErrorDialog("Error: Solo se permiten letras (no se admiten números ni caracteres especiales)")
        throw new IllegalArgumentException("Texto contiene caracteres no permitidos")
      }

      // Si pasa todas las validaciones
      println("Texto válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Relanzamos la excepción para detener el flujo
        throw e
    }
  }


  /**
   * Método que valida un string según ciertas reglas y muestra un panel de error si falla
   *
   * @param texto El string a validar
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación < 8 caracteres
   */

  import javax.swing.JOptionPane

  def validarTamannoTelefono(texto: String): Unit = {
    try {
      // Validación principal
      if (texto == null || texto.length != 8) {
        val mensaje = if (texto == null) "El teléfono no puede ser nulo"
        else "El teléfono debe contener al menos 8 dígitos"

        // Mostrar mensaje de error
        showErrorDialog(s"Error de validación: ${mensaje}")
        // Lanzar excepción para detener el flujo
        throw new IllegalArgumentException(mensaje)
      }

      // Si pasa la validación
      println("Teléfono válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Relanzar la excepción para que el código llamador la maneje
        throw e
    }
  }

  /**
   * Método que valida un string que debe contener solo números y tener exactamente 11 caracteres
   *
   * @param texto El string a validar
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación
   */

  import javax.swing.JOptionPane

  def validarStringNumerico(texto: String): Unit = {
    try {
      // Validación 1: No nulo
      if (texto == null) {
        showErrorDialog("El texto no puede ser nulo")
        throw new IllegalArgumentException("Texto nulo")
      }

      // Validación 2: No vacío
      if (texto.trim.isEmpty) {
        showErrorDialog("El texto no puede estar vacío")
        throw new IllegalArgumentException("Texto vacío")
      }

      // Validación 3: Solo números
      if (!texto.matches("^\\d+$")) {
        showErrorDialog("Solo se permiten números (0-9)")
        throw new IllegalArgumentException("Contiene caracteres no numéricos")
      }

      // Si pasa todas las validaciones
      println("Texto numérico válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Relanzamos la excepción para detener el flujo
        throw e
    }
  }


  /**
   * Método que valida un string que debe ser un carnet de identidad
   *
   * @param texto El string a validar como carnet
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación
   */
  def validarStringNumericoCarnet(texto: String): Unit = {
    try {
      // Validación 1: No nulo
      if (texto == null) {
        showErrorDialog("Error: El carnet no puede ser nulo")
        throw new IllegalArgumentException("Carnet nulo")
      }

      // Validación 2: Exactamente 11 caracteres numéricos
      if (texto.length != 11 || !texto.matches("\\d+")) {
        showErrorDialog("Error: El carnet debe tener exactamente 11 dígitos numéricos")
        throw new IllegalArgumentException("Formato de carnet inválido")
      }

      val fechaStr = texto.substring(0, 6)
      val formatter = DateTimeFormatter.ofPattern("yyMMdd")

      try {
        val fechaParsed = LocalDate.parse(fechaStr, formatter)

        // Determinar el siglo correcto
        val anoDosDigitos = fechaStr.substring(0, 2).toInt
        val siglo = if (anoDosDigitos <= 25) 2000 else 1900
        val fecha = fechaParsed.plusYears(siglo - 2000) // Ajustar el siglo

        println(s"Fecha interpretada: $fecha") // Para depuración

        // Validar que la fecha no sea futura
        if (fecha.isAfter(LocalDate.now())) {
          showErrorDialog("Error: La fecha de nacimiento no puede ser futura")
          throw new IllegalArgumentException("Fecha futura inválida")
        }

        val edad = Period.between(fecha, LocalDate.now()).getYears

        if (edad < 18) {
          showErrorDialog(s"Error: Edad insuficiente ($edad años). Debe ser mayor de 18 años")
          throw new IllegalArgumentException("Edad insuficiente")
        }

      } catch {
        case e: DateTimeParseException =>
          showErrorDialog("Error: Los primeros 6 dígitos deben ser una fecha válida (AAMMDD)")
          throw new IllegalArgumentException("Fecha inválida")
      }

      // Si pasa todas las validaciones
      println("Carnet válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        throw e
    }
  }


  private def showErrorDialog(message: String): Unit = {
    JOptionPane.showMessageDialog(
      null,
      s"Error de validación: $message",
      "Error",
      JOptionPane.ERROR_MESSAGE
    )
  }
}
