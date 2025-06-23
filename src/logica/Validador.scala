package logica

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}
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
  def validarString(texto: String): Unit = {
    try {
      // Realizar las validaciones necesarias
      if (texto == null || texto.trim.isEmpty) {
        throw new IllegalArgumentException("El texto no puede estar vacío")
      }


      if (!texto.matches(".*[a-zA-Z].*")) {
        throw new IllegalArgumentException("El texto debe contener al menos una letra")
      }

      // Si pasa todas las validaciones
      println("Texto válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Mostrar el panel de error con el mensaje de excepción
        JOptionPane.showMessageDialog(
          null,
          s"Error de validación: ${e.getMessage}",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        // Relanzar la excepción si es necesario
        throw e
    }
  }

  /**
   * Método que valida un string según ciertas reglas y muestra un panel de error si falla
   *
   * @param texto El string a validar
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación < 8 caracteres
   */
  def validarTamannoTelefono(texto: String): Unit = {
    try {


      if (texto.length < 8) {
        throw new IllegalArgumentException("El telefono debe contener al menos 8 digitos")
      }

      // Si pasa todas las validaciones
      println("Texto válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Mostrar el panel de error con el mensaje de excepción
        JOptionPane.showMessageDialog(
          null,
          s"Error de validación: ${e.getMessage}",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        // Relanzar la excepción si es necesario
        throw e
    }
  }

  /**
   * Método que valida un string que debe contener solo números y tener exactamente 11 caracteres
   *
   * @param texto El string a validar
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación
   */
  def validarStringNumerico(texto: String): Unit = {
    try {
      // Validación 1: No nulo y no vacío
      if (texto == null || texto.trim.isEmpty) {
        throw new IllegalArgumentException("El texto no puede estar vacío")
      }

      // Validación 2: Solo números (dígitos 0-9)
      if (!texto.matches("^\\d+$")) {
        throw new IllegalArgumentException("Solo se permiten números (0-9)")
      }

      // Si pasa todas las validaciones
      println("Texto válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Mostrar el panel de error
        JOptionPane.showMessageDialog(
          null,
          s"Error de validación: ${e.getMessage}",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        throw e
    }
  }

  /**
   * Método que valida un string que debe contener solo números y tener exactamente 11 caracteres
   *
   * @param texto El string a validar como carnet
   * @throws IllegalArgumentException cuando el texto no cumple las reglas de validación
   */
  def validarStringNumericoCarnet(texto: String): Unit = {
    // Validación 1: Exactamente 11 caracteres
    if (texto.length != 11) {
      throw new IllegalArgumentException("Debe tener exactamente 11 dígitos numéricos")
    }
    try {
      validarStringNumerico(texto)

      var temp = texto.substring(0, 6)
      try {
        var fecha = LocalDate.parse(temp, formatter)
        if (LocalDate.now().getYear - fecha.getYear < 18)
          throw new Exception()
      } catch {
        case e: Exception =>
          throw new IllegalArgumentException(s"Deben ser una fecha válida Formato:(DDMMAA) , mayor 18 años.")
      }

      // Si pasa todas las validaciones
      println("Texto válido: " + texto)

    } catch {
      case e: IllegalArgumentException =>
        // Mostrar el panel de error
        JOptionPane.showMessageDialog(
          null,
          s"Error de validación: ${e.getMessage}",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        throw e
    }
  }
}
