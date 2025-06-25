package logica.Reportes

import com.itextpdf.*

import java.io.{File, FileOutputStream, IOException}
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.awt.Desktop
import com.itextpdf.text.*
import com.itextpdf.text.pdf.{PdfPCell, PdfPTable, PdfWriter}
import logica.consultas.{ConsultaConductor, ConsultaInfraccion, ConsultaLicencia}
import logica.modelos.{Conductor, Infraccion, Licencia}

import scala.collection.JavaConverters.*
import scala.collection.immutable.List

object ReportGenerator {

  // Tipos de reporte disponibles
  sealed trait ReportType
  case object LicenciasEmitidas extends ReportType
  case object InfraccionesEmitidas extends ReportType

  // Método principal para generar reportes
  def generarReporte(tipo: ReportType): Unit = {
    try {
      val titulo = tipo match {
        case LicenciasEmitidas => "Reporte completo de licencias emitidas"
        case InfraccionesEmitidas => "Reporte completo de infracciones emitidas"
      }

      tipo match {
        case LicenciasEmitidas =>
          val licencias  = ConsultaLicencia.obtenerTodasLasLicencias()
          generarReporteLicencias(licencias, titulo)

        case InfraccionesEmitidas =>
          val infracciones = ConsultaInfraccion.obtenerTodasLasInfracciones()
          generarReporteInfracciones(infracciones, titulo)
      }

      println("Reporte generado correctamente")

    } catch {
      case e: Exception =>
        println(s"Error al generar reporte: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  // Implementación para reporte de licencias
  def generarReporteLicencias(licencias: List[Licencia], tituloReporte: String): Unit = {
    try {
      val pdfFile = File.createTempFile("reporte_licencias_completo_", ".pdf")
      pdfFile.deleteOnExit()

      val document = new Document(PageSize.A4.rotate()) // Horizontal para más columnas
      PdfWriter.getInstance(document, new FileOutputStream(pdfFile))
      document.open()

      val estilos = crearEstilosFuentes()
      agregarEncabezado(document, tituloReporte, "Listado completo de todas las licencias emitidas", estilos)
      agregarTablaLicencias(document, licencias, estilos)
      agregarPiePagina(document, estilos("footer"))

      document.close()
      abrirDocumento(pdfFile)

    } catch {
      case e: Exception => manejarErrorGeneracionReporte(e)
    }
  }

  // Implementación para reporte de infracciones
  def generarReporteInfracciones(infracciones: List[Infraccion], tituloReporte: String): Unit = {
    try {
      val pdfFile = File.createTempFile("reporte_infracciones_completo_", ".pdf")
      pdfFile.deleteOnExit()

      val document = new Document(PageSize.A4.rotate()) // Horizontal para más columnas
      PdfWriter.getInstance(document, new FileOutputStream(pdfFile))
      document.open()

      val estilos = crearEstilosFuentes()
      agregarEncabezado(document, tituloReporte, "Listado completo de todas las infracciones registradas", estilos)
      agregarTablaInfracciones(document, infracciones, estilos)
      agregarPiePagina(document, estilos("footer"))

      document.close()
      abrirDocumento(pdfFile)

    } catch {
      case e: Exception => manejarErrorGeneracionReporte(e)
    }
  }

  // Métodos de utilidad para generación de PDF
  private def agregarTablaLicencias(document: Document, licencias: List[Licencia], estilos: Map[String, Font]): Unit = {
    val tabla = new PdfPTable(9)
    tabla.setWidthPercentage(100)
    tabla.setSplitLate(false)

    val anchosColumnas = Array(1.5f, 1.5f, 1.5f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f)
    tabla.setWidths(anchosColumnas)

    tabla.setExtendLastRow(false)
    tabla.setHeaderRows(1)

    // Cabecera
    agregarCeldaCabecera(tabla, "Número Licencia", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Fecha Emisión", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Fecha Vencimiento", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Carnet Identidad", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Categoria Moto", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Categoria Automovil", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Categoria Camion", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Categoria Omnibus", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Puntos", estilos("cabecera"))

    // Datos
    var fondoGris = false
    licencias.foreach { licencia =>
      val colorFondo = if (fondoGris) BaseColor.LIGHT_GRAY else BaseColor.WHITE

      agregarCeldaDatosAjustable(tabla, licencia.id.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.fechaEmision.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.fechaVencimiento.toString, estilos("datos"), colorFondo)
      val conductor = licencia.id match {
        case Some(id) => ConsultaConductor.obtenerConductorPorLicencia(id)
        case None => "Sin conductor"
      }
      val idConductor = conductor match {
        case Some(conductor) => conductor.id
        case None => "no existe"
      }
      agregarCeldaDatosAjustable(tabla, idConductor.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.moto.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.automovil.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.camion.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.omnibus.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, licencia.puntos.toString, estilos("datos"), colorFondo)
    }
    fondoGris = !fondoGris

    document.add(tabla)
  }
  }

  private def agregarTablaInfracciones(document: Document, infracciones: List[Infraccion], estilos: Map[String, Font]): Unit = {
    val tabla = new PdfPTable(5)
    tabla.setWidthPercentage(100)
    tabla.setSplitLate(false)

    val anchosColumnas = Array(1.5f, 2f, 1.5f, 1.5f, 1.5f)
    tabla.setWidths(anchosColumnas)

    tabla.setExtendLastRow(false)
    tabla.setHeaderRows(1)

    // Cabecera
    agregarCeldaCabecera(tabla, "Número Infracción", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Fecha", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Gravedad", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Puntos Descontados", estilos("cabecera"))
    agregarCeldaCabecera(tabla, "Carnet Identidad", estilos("cabecera"))

    // Datos
    var fondoGris = false
    infracciones.foreach { infraccion =>
      val colorFondo = if (fondoGris) BaseColor.LIGHT_GRAY else BaseColor.WHITE

      agregarCeldaDatosAjustable(tabla, infraccion.id.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, infraccion.fecha.toString, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, infraccion.gravedad, estilos("datos"), colorFondo)
      agregarCeldaDatosAjustable(tabla, infraccion.puntos.toString, estilos("datos"), colorFondo)

      val conductor = ConsultaConductor.obtenerConductorPorLicencia(infraccion.id_licencia)

      val idConductor = conductor match {
        case Some(conductor) => conductor.id
        case None => "no existe"
      }
      agregarCeldaDatosAjustable(tabla, idConductor.toString, estilos("datos"), colorFondo)

      fondoGris = !fondoGris
    }

    document.add(tabla)
  }

  private def agregarCeldaDatosAjustable(tabla: PdfPTable, texto: String, font: Font, bgColor: BaseColor): Unit = {
    val celda = new PdfPCell(new Phrase(if (texto != null) texto else "", font))
    celda.setBackgroundColor(bgColor)
    celda.setPadding(5)
    celda.setHorizontalAlignment(Element.ALIGN_CENTER)
    celda.setVerticalAlignment(Element.ALIGN_TOP)
    celda.setNoWrap(false)
    celda.setFixedHeight(0)
    tabla.addCell(celda)
  }

  private def agregarCeldaCabecera(tabla: PdfPTable, texto: String, font: Font): Unit = {
    val celda = new PdfPCell(new Phrase(texto, font))
    celda.setBackgroundColor(BaseColor.GRAY)
    celda.setPadding(5)
    celda.setHorizontalAlignment(Element.ALIGN_CENTER)
    celda.setVerticalAlignment(Element.ALIGN_MIDDLE)
    celda.setNoWrap(false)
    tabla.addCell(celda)
  }

  private def agregarPiePagina(document: Document, fontFooter: Font): Unit = {
    val footer = new Paragraph(
      s"\n\nGenerado automáticamente el ${LocalDateTime.now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
      fontFooter
    )
    footer.setAlignment(Element.ALIGN_RIGHT)
    document.add(footer)
  }

  private def abrirDocumento(pdfFile: File): Unit = {
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.open(pdfFile)
    }
  }

  private def manejarErrorGeneracionReporte(e: Exception): Unit = {
    println(s"ERROR: ${e.getMessage}")
    e.printStackTrace()
  }

  private def crearEstilosFuentes(): Map[String, Font] = Map(
    "titulo" -> new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE),
    "subtitulo" -> new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.DARK_GRAY),
    "cabecera" -> new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE),
    "datos" -> new Font(Font.FontFamily.HELVETICA, 10),
    "footer" -> new Font(Font.FontFamily.HELVETICA, 8)
  )

  private def agregarEncabezado(document: Document, titulo: String, subtitulo: String, estilos: Map[String, Font]): Unit = {
    val tituloPdf = new Paragraph(titulo, estilos("titulo"))
    tituloPdf.setAlignment(Element.ALIGN_CENTER)
    tituloPdf.setSpacingAfter(20f)
    document.add(tituloPdf)

    val subtituloPdf = new Paragraph(subtitulo, estilos("subtitulo"))
    subtituloPdf.setAlignment(Element.ALIGN_CENTER)
    subtituloPdf.setSpacingAfter(15f)
    document.add(subtituloPdf)
  }