package interfaz_usuario

import com.formdev.flatlaf.{FlatDarkLaf, FlatLightLaf}
import com.toedter.calendar.{JCalendar, JDateChooser}
import logica.Validador
import logica.consultas.{ConsultaConductor, ConsultaInfraccion, ConsultaLicencia}
import logica.modelos.*

import java.awt.*
import java.awt.event.*
import java.text.SimpleDateFormat
import java.time.{LocalDate, ZoneId}
import java.util.{Date, Locale}
import javax.swing.*
import javax.swing.table.DefaultTableModel
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps


object main {
  def main(args: Array[String]): Unit = {


    FlatDarkLaf.setup()
    SwingUtilities.invokeLater(() => {

      val frame = new JFrame("Sistema de Tráfico")
      frame.setSize(750, 500)
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.setLocationRelativeTo(null)
      frame.setResizable(false)


      // Crear la barra de menú
      val menuBar = new JMenuBar()
      var seleccionoMenu: Boolean = false


      // Menú principal "Opciones"
      val menuOpciones = new JMenu("Opciones")
      val menuReportes = new JMenu("Reportes")


      // Elementos del menú desplegable
      val menuItemConductor = new JMenuItem("Conductor")
      val menuItemLicencia = new JMenuItem("Licencia")
      val menuItemInfraccion = new JMenuItem("Infracción")
      val menuItemAtras = new JMenuItem("Atras")

      val tableConductores = new JTable()
      val tableLicencias = new JTable()
      val tableInfracciones = new JTable()

      val menuItemReporteInfracciones = new JMenuItem("Infracciones emitidas")
      val menuItemReporteLicencias = new JMenuItem("Licencias emitidas")

      // Panel principal (card layout para cambiar vistas)
      val cardPanel = new JPanel(new CardLayout())

      // Panel inicial vacío
      val initialPanel = new JPanel(new BorderLayout())
      val topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING))
      val initialLabel = new JLabel("Seleccione una opcion del menú", SwingConstants.CENTER)
      initialLabel.setFont(new Font("Arial", Font.PLAIN, 24))
      val selectorModoClaro = new JToggleButton("Modo claro")
      selectorModoClaro.addActionListener(e => {
        try {
          if (selectorModoClaro.isSelected()) {
            FlatLightLaf.setup()
            SwingUtilities.updateComponentTreeUI(frame)
            updateTableColors(tableLicencias)
            updateTableColors(tableInfracciones)
            updateTableColors(tableConductores)
            println("si")
            selectorModoClaro.setText("Modo oscuro")
          } else {
            FlatDarkLaf.setup()
            SwingUtilities.updateComponentTreeUI(frame)
            updateTableColors(tableLicencias)
            updateTableColors(tableInfracciones)
            updateTableColors(tableConductores)
            selectorModoClaro.setText("Modo claro")
            println("No")
          }
        } catch
          case e: Exception =>
            println("No se pudo realizar el cambio de modo")

      })

      val panelCentral = new JPanel(new GridLayout(1, 2, 15, 15))
      panelCentral.add(createRecentActivityPanel())

      initialPanel.add(initialLabel, BorderLayout.NORTH)
      topPanel.add(selectorModoClaro)
      initialPanel.add(topPanel, BorderLayout.EAST)
      initialPanel.add(panelCentral, BorderLayout.CENTER)


      // Función para crear tablas con botones

      def limpiarTabla(table: JTable, nuevasColumnas: Array[Object] = null): DefaultTableModel = {
        val model = new DefaultTableModel(nuevasColumnas, 0)
        table.setModel(model)
        model
      }

      def agregarFila(model: DefaultTableModel, datos: Array[Any]): Unit = {
        val fila = datos.map(_.asInstanceOf[Object])
        model.addRow(fila)
      }

      def createTablePanel(title: String, columns: Array[Object], table: JTable): JPanel =
        val panel = new JPanel(new BorderLayout())
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20))

        // Crear tabla
        table.getTableHeader.setReorderingAllowed(false)
        table.getTableHeader.setResizingAllowed(false)
        table.setFillsViewportHeight(true)


        // Crear botones
        val buttonPanel = new JPanel()
        val agregarBtn = new JButton("Agregar")
        val editarBtn = new JButton("Editar")
        val eliminarBtn = new JButton("Eliminar")

        // Configurar acciones de los botones



        if (title.equalsIgnoreCase("Conductores")) {
          panel.add(new JScrollPane(tableConductores), BorderLayout.CENTER)
          agregarBtn.addActionListener((e: ActionEvent) => {
            val modelo = tableConductores.getModel.asInstanceOf[DefaultTableModel]
            val modeloLicencias = tableLicencias.getModel.asInstanceOf[DefaultTableModel]
            agregarConductor(modelo, modeloLicencias)
          })

          editarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableConductores.getSelectedRow
            val modelo = tableConductores.getModel.asInstanceOf[DefaultTableModel]
            if (filaSeleccionada == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para editar en $title")
            } else {
              var exito = editarConductor(modelo, filaSeleccionada)
              if (exito)
                JOptionPane.showMessageDialog(frame, s"Editando registro ID: ${tableConductores.getValueAt(tableConductores.getSelectedRow(), 0)} en $title")
            }
          })

          eliminarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableConductores.getSelectedRow
            val modelo = tableConductores.getModel.asInstanceOf[DefaultTableModel]
            if (tableConductores.getSelectedRow() == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para eliminar en $title")
            } else {
              try {
                val carnetIdentidad: Long = modelo.getValueAt(tableConductores.getSelectedRow, 0).toString.toLong
                val idLicencia: Long = modelo.getValueAt(tableConductores.getSelectedRow, 3).toString.toLong
                ConsultaConductor.eliminarConductorCompleto(carnetIdentidad, idLicencia)
                if (mostrarMensajeConfirmacionEliminar()) {
                  modelo.removeRow(filaSeleccionada)
                }
              } catch {
                case e: Exception =>
                  JOptionPane.showMessageDialog(frame, e.getMessage)
              }

            }
          })
          buttonPanel.add(agregarBtn)
          buttonPanel.add(editarBtn)
          buttonPanel.add(eliminarBtn)

        }
        else if (title.equalsIgnoreCase("Infracciones")) {
          panel.add(new JScrollPane(tableInfracciones), BorderLayout.CENTER)
          agregarBtn.addActionListener((e: ActionEvent) => {
            val modelo = tableInfracciones.getModel.asInstanceOf[DefaultTableModel]
            agregarInfraccion(modelo)
          })

          editarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableInfracciones.getSelectedRow
            val modelo = tableInfracciones.getModel.asInstanceOf[DefaultTableModel]
            if (filaSeleccionada == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para editar en $title")
            } else {
              var exito = editarInfraccion(modelo, filaSeleccionada)
              if (exito)
                JOptionPane.showMessageDialog(frame, s"Editando registro ID: ${tableInfracciones.getValueAt(tableInfracciones.getSelectedRow(), 0)} en $title")
            }
          })

          eliminarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableInfracciones.getSelectedRow
            val modelo = tableInfracciones.getModel.asInstanceOf[DefaultTableModel]
            if (tableInfracciones.getSelectedRow() == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para eliminar en $title")
            } else {
              try {
                val idInfraccion = modelo.getValueAt(tableInfracciones.getSelectedRow, 0).toString.toLong
                ConsultaInfraccion.eliminarInfraccion(idInfraccion)
                if (mostrarMensajeConfirmacionEliminar()) {
                  modelo.removeRow(filaSeleccionada)
                }
              } catch {
                case e: Exception =>
                  JOptionPane.showMessageDialog(frame, e.getMessage)
              }

            }
          })


          buttonPanel.add(agregarBtn)
          buttonPanel.add(editarBtn)
          buttonPanel.add(eliminarBtn)

        }
        else if (title.equalsIgnoreCase("Licencias")) {
          panel.add(new JScrollPane(tableLicencias), BorderLayout.CENTER)

          editarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableLicencias.getSelectedRow
            val modelo = tableLicencias.getModel.asInstanceOf[DefaultTableModel]
            if (filaSeleccionada == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para editar en $title")
            } else {
              var exito = editarLicencia(modelo, filaSeleccionada)
              if(exito)
                JOptionPane.showMessageDialog(frame, s"Editando registro ID: ${tableLicencias.getValueAt(tableLicencias.getSelectedRow(), 0)} en $title")
            }
          })
          buttonPanel.add(editarBtn)
        }
        // Configurar panel
        panel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH)
        panel.add(buttonPanel, BorderLayout.SOUTH)
        panel

      // Datos para las tablas
      def cargarConductores(): JPanel = {
        val conductorColumns = Array[Object]("ID", "Nombre", "Apellido", "Licencia", "Teléfono")
        val modelConductor = limpiarTabla(tableConductores, conductorColumns)
        val conductores = ConsultaConductor.obtenerTodosLosConductores()

        conductores.foreach { conductor =>
          val fila = Array[Any](
            conductor.id.getOrElse("N/A").toString,
            conductor.nombre,
            conductor.apellido,
            conductor.licencia.get.id.getOrElse("").toString,
            conductor.telefono
          )
          agregarFila(modelConductor, fila)
        }
        createTablePanel("Conductores", conductorColumns, tableConductores)
      }


      def cargarInfracciones(): JPanel = {
        val infraccionColumns = Array[Object]("ID", "ID Licencia", "Puntos deducidos", "Gravedad", "Fecha")
        val modelInfraccion = limpiarTabla(tableInfracciones, infraccionColumns)
        val infracciones = ConsultaInfraccion.obtenerTodasLasInfracciones()

        infracciones.foreach { infraccion =>
          val fila = Array[Any](
            infraccion.id.getOrElse("N/A").toString,
            infraccion.id_licencia.toString,
            infraccion.puntos,
            infraccion.gravedad,
            infraccion.fecha
          )
          agregarFila(modelInfraccion, fila)
        }

        createTablePanel("Infracciones", infraccionColumns, tableInfracciones)
      }

      def cargarLicencias(): JPanel = {
        val licenciaColumns = Array[Object]("ID", "Moto", "Auto", "Camion", "Omnibus", "Puntos", "Fecha emision", "Fecha vencido")
        val modelLicencia = limpiarTabla(tableLicencias, licenciaColumns)
        val licencias = ConsultaLicencia.obtenerTodasLasLicencias()

        licencias.foreach { licencia =>
          val fila = Array[Any](
            licencia.id.getOrElse("N/A").toString,
            convertirBoolean(licencia.moto),
            convertirBoolean(licencia.automovil),
            convertirBoolean(licencia.camion),
            convertirBoolean(licencia.omnibus),
            licencia.puntos,
            licencia.fechaEmision,
            licencia.fechaVencimiento
          )
          agregarFila(modelLicencia, fila)
        }

        createTablePanel("Licencias", licenciaColumns, tableLicencias)
      }






      // Agregar paneles al card layout
      cardPanel.add(initialPanel, "inicio")

      // Configurar acciones del menú
      menuItemConductor.addActionListener((e: ActionEvent) => {
        val panel = cargarConductores()
        cardPanel.add(panel, "conductores")
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "conductores")
        menuOpciones.add(menuItemAtras)
      })

      menuItemLicencia.addActionListener((e: ActionEvent) => {
        val panel = cargarLicencias()
        cardPanel.add(panel, "licencias")
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "licencias")
        menuOpciones.add(menuItemAtras)
      })

      menuItemInfraccion.addActionListener((e: ActionEvent) => {
        val panel = cargarInfracciones()
        cardPanel.add(panel, "infracciones")
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "infracciones")
        menuOpciones.add(menuItemAtras)
      })

      menuItemAtras.addActionListener((e: ActionEvent) => {
        cardPanel.add(initialPanel, "inicio")
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "inicio")
        menuOpciones.remove(menuItemAtras)
      })

      menuItemReporteLicencias.addActionListener((e: ActionEvent) => {
        ActividadReciente.registrarAgregarEliminar("REPORTE", "Licencias", "Exitosamente", null)
      })

      menuItemReporteInfracciones.addActionListener((e: ActionEvent) => {
        ActividadReciente.registrarAgregarEliminar("REPORTE", "Infracciones", "Exitosamente", null)
      })

      // Configurar menú
      menuOpciones.add(menuItemConductor)
      menuOpciones.add(menuItemLicencia)
      menuOpciones.add(menuItemInfraccion)
      menuReportes.add(menuItemReporteLicencias)
      menuReportes.add(menuItemReporteInfracciones)
      menuBar.add(menuOpciones)
      menuBar.add(menuReportes)


      frame.setJMenuBar(menuBar)

      // Mostrar ventana
      frame.add(cardPanel)
      frame.setVisible(true)
    }
    )
  }


  def agregarConductor(modeloConductor: DefaultTableModel, modeloLicencia: DefaultTableModel): Unit = {


    val campoNombre = new JTextField()
    val campoApellido = new JTextField()
    val campoCi = new JTextField()
    val campoTelefono = new JTextField()
    val categoriaCarro = new JRadioButton()
    val categoriaMoto = new JRadioButton()
    val categoriaOmnibus = new JRadioButton()
    val categoriaCamion = new JRadioButton()

    // Creamos un array con los campos y sus etiquetas
    val campos = Array(
      "Nombre:", campoNombre,
      "Apellido:", campoApellido,
      "CI:", campoCi,
      "Teléfono:", campoTelefono,
      "Categoria Carro", categoriaCarro,
      "Categoria Moto", categoriaMoto,
      "Categoria Camion", categoriaCamion,
      "Categoria Omnibus", categoriaOmnibus,
    )

    val resultado = JOptionPane.showConfirmDialog(
      null,
      campos,
      "Registro de Conductor",
      JOptionPane.OK_CANCEL_OPTION
    )

    val fechaActual = LocalDate.now()
    val fechaActualDate: Date = java.util.Date.from(fechaActual.atStartOfDay(ZoneId.systemDefault()).toInstant)

    val fechaAnnosDespues = fechaActual.plusYears(10)
    val fechaAnnosDespuesDate: Date = java.util.Date.from(fechaAnnosDespues.atStartOfDay(ZoneId.systemDefault()).toInstant)


    // Si el usuario hace clic en OK, procesamos los datos
    if (resultado == JOptionPane.OK_OPTION) {
      var idLicencia = Long.MinValue
      ConsultaLicencia.obtenerUltimaLicencia() match {
        case Right(id) => idLicencia = id
        case Left(error) => printf("No se pudo obtener la ultima licencia \n" + error)
      }
      if (!categoriaMoto.isSelected && !categoriaCarro.isSelected && !categoriaOmnibus.isSelected && !categoriaCamion.isSelected) {
        JOptionPane.showMessageDialog(
          null,
          s"Error de validación: Debe elegir una categoria",
          "Error",
          JOptionPane.ERROR_MESSAGE)
      }
      else {
        val licencia = Licencia(
          Some(idLicencia + 1),
          categoriaMoto.isSelected,
          categoriaCarro.isSelected,
          categoriaCamion.isSelected,
          categoriaOmnibus.isSelected,
          0,
          fechaActualDate,
          fechaAnnosDespuesDate
        )

        val contadorFilas = modeloConductor.getRowCount

        val validador = new Validador()
        try {
          validador.validarString(campoNombre.getText)
          validador.validarString(campoApellido.getText)
          validador.validarStringNumerico(campoTelefono.getText)
          validador.validarStringNumericoCarnet(campoCi.getText)
          validador.validarTamannoTelefono(campoTelefono.getText)


          val conductor = Conductor(
            (Some(campoCi.getText.toLong)),
            campoNombre.getText,
            campoApellido.getText,
            (Some(licencia)),
            campoTelefono.getText
          )


          // Mostramos los datos capturados (puedes guardarlos en una lista, BD, etc.)
          JOptionPane.showMessageDialog(
            null,
            s"""
               |Conductor registrado:
               |Nombre: ${conductor.nombre}
               |Apellido: ${conductor.apellido}
               |Licencia: ${licencia.id.getOrElse("N/A").toString}
               |Teléfono: ${conductor.telefono}
               |""".stripMargin,
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE
          )
          modeloConductor.addRow(Array[AnyRef](
            campoCi.getText,
            campoNombre.getText,
            campoApellido.getText,
            licencia.id.getOrElse("N/A").toString,
            campoTelefono.getText)
          )
          try {
            val crearConductor = ConsultaConductor.crearConductorYLicencia(conductor, licencia)
          } catch {
            case e: Exception =>
              printf("No se pudo crear el conductor \n" + e.getMessage)
          }
        } catch {
          case e: IllegalArgumentException =>
            // Solo registrar el error, ya mostramos el mensaje
            println("Error de validación: " + e.getMessage)
        }
      }
    }
    else {
      JOptionPane.showMessageDialog(
        null,
        "Registro cancelado",
        "Aviso",
        JOptionPane.WARNING_MESSAGE
      )
    }
  }

  def agregarInfraccion(modeloInfraccion: DefaultTableModel): Unit = {

    val opcionesPuntos = Array[AnyRef](" ", Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(12))
    val modeloOpcionesPuntos = new DefaultComboBoxModel[AnyRef](opcionesPuntos)

    val opcionesGravedad = Array[String](" ", "Leve", "Grave", "Muy grave")
    val modeloOpcionesGravedad = new DefaultComboBoxModel[String](opcionesGravedad)


    val campoIdLicencia = new JTextField()
    val campoPuntosDeducidos = new JComboBox[AnyRef](modeloOpcionesPuntos)
    val campoGravedad = new JComboBox[String](modeloOpcionesGravedad)
    val campoFecha = new JDateChooser()

    // Creamos un array con los campos y sus etiquetas
    val campos = Array(
      "Id Licencia:", campoIdLicencia,
      "Puntos deducidos:", campoPuntosDeducidos,
      "Gravedad:", campoGravedad,
      "Fecha:", campoFecha
    )

    val resultado = JOptionPane.showConfirmDialog(
      null,
      campos,
      "Registro de Infraccion",
      JOptionPane.OK_CANCEL_OPTION
    )

    // Si el usuario hace clic en OK, procesamos los datos
    if (resultado == JOptionPane.OK_OPTION) {
      val contadorFilas = modeloInfraccion.getRowCount
      val infraccion = Infraccion(
        (Some(contadorFilas + 1)),
        campoIdLicencia.getText.toLong,
        Integer.valueOf(campoPuntosDeducidos.getSelectedItem.toString),
        campoGravedad.getSelectedItem.toString,
        campoFecha.getDate
      )

      val formato = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"))
      val fechaFormateada = formato.format(infraccion.fecha)

      try {
        val fechaSeleccionada = campoFecha.getDate.toInstant.
          atZone(java.time.ZoneId.systemDefault()).toLocalDate()

        if (fechaSeleccionada.isAfter(LocalDate.now())) {
          JOptionPane.showMessageDialog(
            null,
            "La fecha no puede ser futura",
            "Error de fecha",
            JOptionPane.ERROR_MESSAGE
          )
          throw new Exception("Fecha futura no permitida")
        }
      } catch {
        case e: Exception =>
          return
      }


      val conductorInfractor = ConsultaConductor.obtenerConductorPorLicencia(infraccion.id_licencia)
      // Mostramos los datos capturados (puedes guardarlos en una lista, BD, etc.)
      var infractor = ""
      conductorInfractor match {
        case Some(conductor: Conductor) =>
          infractor = conductor.nombre
        case None =>
          println("No existe conductor")
      }


      if (!infractor.equalsIgnoreCase("")) {
        JOptionPane.showMessageDialog(
          null,
          s"""
             |Infraccion registrada:
             |Nombre conductor: $infractor
             |Puntos deducidos: ${infraccion.puntos}
             |Gravedad: ${infraccion.gravedad}
             |Fecha: $fechaFormateada
             |""".stripMargin,
          "Éxito",
          JOptionPane.INFORMATION_MESSAGE
        )
        modeloInfraccion.addRow(Array[AnyRef](
          String.valueOf(contadorFilas + 1),
          campoIdLicencia.getText,
          campoPuntosDeducidos.getSelectedItem.toString,
          campoGravedad.getSelectedItem.toString,
          fechaFormateada)
        )
        try {
          val crearInfraccion = ConsultaInfraccion.crearInfraccionConsulta(infraccion)
        } catch {
          case e: Exception =>
        }
      }
      else {
        JOptionPane.showMessageDialog(
          null,
          "No se encontro el conductor con la licencia dada",
          "Aviso",
          JOptionPane.WARNING_MESSAGE
        )
      }
    } else {
      JOptionPane.showMessageDialog(
        null,
        "Registro cancelado",
        "Aviso",
        JOptionPane.WARNING_MESSAGE
      )
    }

  }

  def editarConductor(modeloConductor: DefaultTableModel, fila: Int): Boolean = {
    // Obtener datos actuales de la fila
    val ciOriginal = modeloConductor.getValueAt(fila, 0).toString
    val nombreOriginal = modeloConductor.getValueAt(fila, 1).toString
    val apellidoOriginal = modeloConductor.getValueAt(fila, 2).toString
    val telefonoOriginal = modeloConductor.getValueAt(fila, 4).toString

    // Crear campos con los valores actuales
    val campoCi = new JTextField(ciOriginal)
    val campoNombre = new JTextField(nombreOriginal)
    val campoApellido = new JTextField(apellidoOriginal)
    val campoTelefono = new JTextField(telefonoOriginal)

    val campos = Array(
      "Nombre:", campoNombre,
      "Apellido:", campoApellido,
      "CI:", campoCi,
      "Teléfono:", campoTelefono
    )

    if (JOptionPane.showConfirmDialog(null, campos, "Editar Conductor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      try {
        val validador = new Validador()

        // Validar ANTES de modificar
        validador.validarString(campoNombre.getText)
        validador.validarString(campoApellido.getText)
        validador.validarStringNumerico(campoTelefono.getText)
        validador.validarStringNumericoCarnet(campoCi.getText)
        validador.validarTamannoTelefono(campoTelefono.getText)

        // Obtener licencia
        val licenciaNueva = ConsultaLicencia.obtenerLicenciaPorId(
          modeloConductor.getValueAt(fila, 3).toString.toLong
        ) match {
          case None =>
            JOptionPane.showMessageDialog(null, "No se encuentra la licencia", "Advertencia", JOptionPane.WARNING_MESSAGE)
            null
          case Some(lic) => lic
        }

        // Crear objeto conductor validado
        val conductor = Conductor(
          Some(campoCi.getText.toLong),
          campoNombre.getText,
          campoApellido.getText,
          Option(licenciaNueva),
          campoTelefono.getText
        )

        var resultado = false // Inicializamos como false por defecto

        try {
          // Intentar editar en base de datos
          ConsultaConductor.editarConductorConsulta(conductor, ciOriginal.toLong)
          resultado = true // Solo se establece a true si la operación tiene éxito

        } catch {
          case e: Exception =>
            resultado = false
        }

        if (resultado) {
          // Actualizar modelo solo si la edición fue exitosa
          modeloConductor.setValueAt(campoCi.getText, fila, 0)
          modeloConductor.setValueAt(campoNombre.getText, fila, 1)
          modeloConductor.setValueAt(campoApellido.getText, fila, 2)
          modeloConductor.setValueAt(campoTelefono.getText, fila, 4)
          true
        } else
          false

      } catch {
        case e: IllegalArgumentException =>
          JOptionPane.showMessageDialog(null, s"Error de validación: ${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
          false
        case e: Exception =>
          JOptionPane.showMessageDialog(null, s"Error al editar conductor: ${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
          false
      }
    } else {
      false // Usuario canceló la operación
    }
  }

  def editarInfraccion(modeloInfraccion: DefaultTableModel, fila: Int): Boolean = {
    try {
      // Obtener datos actuales de la fila
      val opcionesPuntos = Array[AnyRef](Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(12))
      val modeloOpcionesPuntos = new DefaultComboBoxModel[AnyRef](opcionesPuntos)

      val opcionesGravedad = Array[String]("Leve", "Grave", "Muy grave")
      val modeloOpcionesGravedad = new DefaultComboBoxModel[String](opcionesGravedad)

      val idInfraccion = modeloInfraccion.getValueAt(fila, 0).toString
      val idLicencia = modeloInfraccion.getValueAt(fila, 1).toString
      val puntosDeducidos = modeloInfraccion.getValueAt(fila, 2)
      val gravedad = modeloInfraccion.getValueAt(fila, 3).toString
      val fechaStr = modeloInfraccion.getValueAt(fila, 4).toString

      // Crear campos con los valores actuales
      val campoIdLicencia = new JLabel(idLicencia)
      val campoPuntosDeducidos = new JComboBox[AnyRef](modeloOpcionesPuntos)
      campoPuntosDeducidos.setSelectedItem(puntosDeducidos)

      val campoGravedad = new JComboBox[String](modeloOpcionesGravedad)
      campoGravedad.setSelectedItem(gravedad)

      val campoFecha = new JDateChooser()
      convertirFecha(fechaStr) match {
        case Some(fecha) => campoFecha.setDate(fecha)
        case None => campoFecha.setDate(new Date()) // Fecha actual por defecto
      }

      val campos = Array(
        "ID Licencia:", campoIdLicencia,
        "Puntos deducidos:", campoPuntosDeducidos,
        "Gravedad:", campoGravedad,
        "Fecha:", campoFecha
      )

      if (JOptionPane.showConfirmDialog(null, campos, "Editar Infracción", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        try {
          // Validación de fecha
          if (campoFecha.getDate == null) {
            throw new IllegalArgumentException("Fecha inválida: No se ha seleccionado ninguna fecha")
          } else if (campoFecha.getDate.after(new Date())) {
            throw new IllegalArgumentException("Fecha inválida: No puede ser una fecha futura")
          }

          // Crear objeto Infracción
          val infraccion = Infraccion(
            Some(idInfraccion.toLong),
            idLicencia.toLong,
            campoPuntosDeducidos.getSelectedItem.asInstanceOf[Int],
            campoGravedad.getSelectedItem.toString,
            campoFecha.getDate
          )

          // Editar en base de datos
          ConsultaInfraccion.editarInfraccionConsulta(infraccion, puntosDeducidos.toString.toInt)

          // Actualizar modelo
          val formato = new SimpleDateFormat("yyyy/MM/dd")
          modeloInfraccion.setValueAt(campoPuntosDeducidos.getSelectedItem, fila, 2)
          modeloInfraccion.setValueAt(campoGravedad.getSelectedItem, fila, 3)
          modeloInfraccion.setValueAt(formato.format(campoFecha.getDate), fila, 4)

          true // Operación exitosa

        } catch {
          case e: NumberFormatException =>
            JOptionPane.showMessageDialog(null, "Error en formato numérico: " + e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
            false

          case e: IllegalArgumentException =>
            JOptionPane.showMessageDialog(null, e.getMessage, "Error de validación", JOptionPane.ERROR_MESSAGE)
            false

          case e: Exception =>
            JOptionPane.showMessageDialog(null, "Error al editar infracción: " + e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
            false
        }
      } else {
        false // Usuario canceló la operación
      }
    } catch {
      case e: Exception =>
        JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
        false
    }
  }

  def editarLicencia(modeloLicencia: DefaultTableModel, fila: Int): Boolean = {
    try {
      // Obtener datos actuales de la fila
      val id = modeloLicencia.getValueAt(fila, 0).toString
      val moto = modeloLicencia.getValueAt(fila, 1)
      val carro = modeloLicencia.getValueAt(fila, 2)
      val camion = modeloLicencia.getValueAt(fila, 3)
      val omnibus = modeloLicencia.getValueAt(fila, 4)
      val puntos = modeloLicencia.getValueAt(fila, 5).toString.toInt

      // Crear componentes de interfaz
      val campoMoto = new JRadioButton()
      val campoCarro = new JRadioButton()
      val campoCamion = new JRadioButton()
      val campoOmnibus = new JRadioButton()

      // Configurar estado inicial
      campoMoto.setSelected(convertirStringABoolean(moto.toString))
      campoCarro.setSelected(convertirStringABoolean(carro.toString))
      campoCamion.setSelected(convertirStringABoolean(camion.toString))
      campoOmnibus.setSelected(convertirStringABoolean(omnibus.toString))

      val campos = Array(
        "Categoría Moto:", campoMoto,
        "Categoría Carro:", campoCarro,
        "Categoría Camión:", campoCamion,
        "Categoría Ómnibus:", campoOmnibus
      )

      if (JOptionPane.showConfirmDialog(null, campos, "Editar Licencia", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        try {
          // Validar que al menos una categoría esté seleccionada
          if (!campoMoto.isSelected && !campoCarro.isSelected &&
            !campoCamion.isSelected && !campoOmnibus.isSelected) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría")
          }

          // Crear objeto Licencia
          val licencia = Licencia(
            Some(id.toLong),
            campoMoto.isSelected,
            campoCarro.isSelected,
            campoCamion.isSelected,
            campoOmnibus.isSelected,
            puntos,
            null,
            null
          )

          // Actualizar base de datos
          ConsultaLicencia.editarLicenciaConsulta(licencia)

          // Actualizar modelo de tabla
          modeloLicencia.setValueAt(campoMoto.isSelected, fila, 1)
          modeloLicencia.setValueAt(campoCarro.isSelected, fila, 2)
          modeloLicencia.setValueAt(campoCamion.isSelected, fila, 3)
          modeloLicencia.setValueAt(campoOmnibus.isSelected, fila, 4)

          true // Operación exitosa

        } catch {
          case e: IllegalArgumentException =>
            JOptionPane.showMessageDialog(null, e.getMessage, "Error de validación", JOptionPane.ERROR_MESSAGE)
            false

          case e: Exception =>
            JOptionPane.showMessageDialog(null, s"Error al editar licencia: ${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
            false
        }
      } else {
        false // Usuario canceló la operación
      }
    } catch {
      case e: Exception =>
        JOptionPane.showMessageDialog(null, s"Error inesperado: ${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
        false
    }
  }

  def convertirFecha(fecha: String): Option[Date] = {
    try {
      val formato = new SimpleDateFormat("yyyy-MM-dd")
      formato.setLenient(false)
      Some(formato.parse(fecha))
    } catch
      case e: Exception => None
  }

  def convertirBoolean(esCategoria: Boolean): String = {
    if (esCategoria) "Si" else "No"
  }

  def convertirStringABoolean(categorias: String): Boolean = {
    if (categorias.equalsIgnoreCase("Si")) true else false
  }

  def mostrarMensajeConfirmacionEliminar(): Boolean = {
    val respuesta = JOptionPane.showConfirmDialog(null, "Desea eliminar este registro", "Eliminar", JOptionPane.YES_NO_OPTION)
    respuesta == JOptionPane.YES_OPTION
  }

  def updateTableColors(table: Component): Unit = {
    table match {
      case table: javax.swing.JTable =>
        table.setBackground(UIManager.getColor("Table.background"))
        table.setForeground(UIManager.getColor("Table.foreground"))
        table.setGridColor(UIManager.getColor("Table.gridColor"))
        table.setSelectionBackground(UIManager.getColor("Table.selectionBackground"))
        table.setSelectionForeground(UIManager.getColor("Table.selectionForeground"))

        // Actualizar el header de la tabla
        Option(table.getTableHeader).foreach { header =>
          header.setBackground(UIManager.getColor("TableHeader.background"))
          header.setForeground(UIManager.getColor("TableHeader.foreground"))
        }

        // Actualizar renderers existentes
        (0 until table.getColumnCount).foreach { i =>
          table.getColumnModel.getColumn(i).getCellRenderer match {
            case r: javax.swing.table.DefaultTableCellRenderer =>
              r.setBackground(UIManager.getColor("Table.background"))
              r.setForeground(UIManager.getColor("Table.foreground"))
            case _ => // No hacer nada para renderers personalizados
          }
        }
      case _ => // No es una tabla, no hacer nada
    }
  }

  def createRecentActivityPanel(): JPanel = {
    val panel = new JPanel(new BorderLayout())
    panel.setBorder(BorderFactory.createTitledBorder("Actividad Reciente"))

    // Cambiamos a DefaultListModel[ActividadReciente] para almacenar los objetos completos
    val model = new DefaultListModel[ActividadReciente]()
    val activityList = new JList[ActividadReciente](model)

    // Configuramos el listener para doble click
    activityList.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        if (e.getClickCount == 2) { // Doble click
          val indice = activityList.locationToIndex(e.getPoint)
          if (indice != -1) {
            val actividad = model.getElementAt(indice)
            mostrarDetallesActividad(actividad)
          }
        }
      }
    })

    // Función para mostrar detalles completos
    def mostrarDetallesActividad(actividad: ActividadReciente): Unit = {
      val dialog = new JDialog()
      dialog.setResizable(false)
      dialog.setModal(true)
      dialog.setTitle(s"Detalles de Actividad - ${actividad.tipo}")
      dialog.setLayout(new BorderLayout())

      val panelPrincipal = new JPanel()
      panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS))
      val scroolPane = new JScrollPane(panelPrincipal)
      dialog.add(scroolPane, BorderLayout.CENTER)

      // Panel con información básica
      val panelInfo = new JPanel(new GridLayout(0, 2, 5, 5))
      panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))

      val formatoFecha = new java.text.SimpleDateFormat("yyyy-MM-dd")

      panelInfo.add(new JLabel("Fecha:"))
      panelInfo.add(new JLabel(formatoFecha.format(actividad.fecha)))
      panelInfo.add(new JLabel("Tipo:"))
      actividad.tipo match
        case "MODIFICAR" =>
          panelInfo.add(new JLabel(actividad.tipo)).setForeground(new Color(0, 145, 255))
        case "AGREGAR" =>
          panelInfo.add(new JLabel(actividad.tipo)).setForeground(new Color(0, 200, 83))
        case "ELIMINAR" =>
          panelInfo.add(new JLabel(actividad.tipo)).setForeground(new Color(255, 61, 0))
        case _ =>
          panelInfo.add(new JLabel(actividad.tipo)).setForeground(Color.BLACK)

      panelInfo.add(new JLabel("Entidad:"))
      panelInfo.add(new JLabel(actividad.entidad))

      panelPrincipal.add(panelInfo)

      if (actividad.tipo.equalsIgnoreCase("Modificar")) {
        val panelCambio = new JPanel()
        panelCambio.setLayout(new BoxLayout(panelCambio, BoxLayout.Y_AXIS))
        panelCambio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))

        (actividad.objetoEntidad, actividad.objetoEntidadModificada) match {
          case (conductor: Conductor, conductorViejo: Conductor) => {
            panelCambio.add(compararValores("Nombre", conductorViejo.nombre, conductor.nombre))
            panelCambio.add(compararValores("Apellido", conductorViejo.apellido, conductor.apellido))
            conductorViejo.id match
              case None => {
                println("No se encontró el carnet de identidad del conductor modificado")
              }
              case Some(id) => {
                conductor.id match
                  case None =>
                    println("No se encontró el carnet de identidad del conductor")
                  case Some(idActual) =>
                    panelCambio.add(compararValores("CI", id.toString, idActual.toString))
              }
                panelCambio.add(compararValores("Teléfono", conductorViejo.telefono, conductor.telefono))
                panelPrincipal.add(panelCambio)
          }
          case (infraccion: Infraccion, infraccionVieja: Infraccion) =>
            panelCambio.add(compararValores("Puntos deducidos", infraccionVieja.puntos.toString, infraccion.puntos.toString))
            panelCambio.add(compararValores("Gravedad", infraccionVieja.gravedad, infraccion.gravedad))
            panelCambio.add(compararValores("Fecha", infraccionVieja.fecha, infraccion.fecha))
            panelPrincipal.add(panelCambio)
          case (licencia: Licencia, licenciaVieja: Licencia) =>
            panelCambio.add(compararValores("Carro", convertirBoolean(licenciaVieja.automovil), convertirBoolean(licencia.automovil)))
            panelCambio.add(compararValores("Moto", convertirBoolean(licenciaVieja.moto), convertirBoolean(licencia.moto)))
            panelCambio.add(compararValores("Camión", convertirBoolean(licenciaVieja.camion), convertirBoolean(licencia.camion)))
            panelCambio.add(compararValores("Omnibús", convertirBoolean(licenciaVieja.omnibus), convertirBoolean(licencia.omnibus)))
            panelCambio.add(compararValores("Puntos", licenciaVieja.puntos.toString, licencia.puntos.toString))
            panelPrincipal.add(panelCambio)
          case _: (AnyRef, AnyRef) =>
            println("No se encuentra el objeto")
        }

      }
      else if (actividad.tipo.equalsIgnoreCase("Eliminar") || actividad.tipo.equalsIgnoreCase("Agregar")) {
        val panelCambio = new JPanel()
        panelCambio.setLayout(new BoxLayout(panelCambio, BoxLayout.Y_AXIS))
        panelCambio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
        actividad.objetoEntidad match {
          case conductor: Conductor =>
            panelCambio.add(escribirValores("Nombre", conductor.nombre))
            panelCambio.add(escribirValores("Apellidos", conductor.apellido))
            conductor.id match
              case None =>
                println("No se encontró el carnet de identidad del conductor")
              case Some(idActual) =>
                panelCambio.add(escribirValores("CI", idActual))
            panelCambio.add(escribirValores("Teléfono", conductor.telefono))
            panelPrincipal.add(panelCambio)
          case licencia: Licencia =>
            panelCambio.add(escribirValores("Carro", convertirBoolean(licencia.automovil)))
            panelCambio.add(escribirValores("Moto", convertirBoolean(licencia.moto)))
            panelCambio.add(escribirValores("Camión", convertirBoolean(licencia.camion)))
            panelCambio.add(escribirValores("Omnibús", convertirBoolean(licencia.omnibus)))
            if (actividad.tipo.equalsIgnoreCase("eliminar")) {
              panelCambio.add(escribirValores("Puntos", licencia.puntos))
            }
            panelPrincipal.add(panelCambio)
          case infraccion: Infraccion =>
            panelCambio.add(escribirValores("Puntos deducidos", infraccion.puntos))
            panelCambio.add(escribirValores("Gravedad", infraccion.gravedad))
            panelCambio.add(escribirValores("Fecha", infraccion.fecha))
            panelPrincipal.add(panelCambio)
        }
      }
      else {
        val panelCambio = new JPanel()
        panelCambio.setLayout(new BoxLayout(panelCambio, BoxLayout.Y_AXIS))
        panelCambio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
        panelCambio.add(new JLabel(s"<html><div style='width:200px;'><b/>Reporte generado exitosamente de ${actividad.entidad} emitidas en un periodo de tiempo</b></div></html>"))
        panelPrincipal.add(panelCambio)
      }


      // Construir el diálogo
      dialog.pack()
      dialog.add(panelInfo, BorderLayout.NORTH)
      dialog.setSize(300, 200)
      dialog.setLocationRelativeTo(null)
      dialog.setVisible(true)
    }

    // Función para actualizar la lista
    def actualizarLista(): Unit = {
      model.clear()
      ActividadReciente.obtenerRecientes().foreach { actividad =>
        model.addElement(actividad) // Ahora agregamos el objeto completo
      }
    }

    // Configurar el renderizado de items
    activityList.setCellRenderer(new DefaultListCellRenderer() {
      override def getListCellRendererComponent(
                                                 list: JList[_],
                                                 value: Any,
                                                 index: Int,
                                                 isSelected: Boolean,
                                                 cellHasFocus: Boolean
                                               ): Component = {
        val c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        c.asInstanceOf[JLabel].setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

        value match {
          case actividad: ActividadReciente =>
            val formatoFecha = new java.text.SimpleDateFormat("HH:mm:ss")
            c.asInstanceOf[JLabel].setText(s"${formatoFecha.format(actividad.fecha)} [${actividad.tipo}] ${actividad.entidad}: ${actividad.detalles}")

            // Cambiar color según el tipo de actividad
            actividad.tipo match {
              case "AGREGAR" => c.setForeground(new Color(0, 200, 83)) // Verde
              case "MODIFICAR" => c.setForeground(new Color(0, 145, 255)) // Azul
              case "ELIMINAR" => c.setForeground(new Color(255, 61, 0)) // Rojo
              case _ => c.setForeground(Color.BLACK)
            }

          case _ => c.asInstanceOf[JLabel].setText(value.toString)
        }
        c
      }
    })

    // Actualizar inicialmente
    actualizarLista()

    panel.add(new JScrollPane(activityList), BorderLayout.CENTER)
    ActividadReciente.addListener(actualizarLista())

    panel
  }

  def compararValores(campo: String, viejo: Any, actual: Any): JLabel = {
    val formatoFecha = new SimpleDateFormat("yyyy-MM-dd")
    val (viejoValor, actualValor) = (viejo, actual) match {
      case (d1: java.util.Date, d2: java.util.Date) =>
        (formatoFecha.format(d1), formatoFecha.format(d2))
      case _ =>
        (viejo.toString, actual.toString)
    }

    var texto: String = " "
    if (viejoValor != actualValor) {
      texto = (s"<html><b>$campo modificado:</b> <font color='red'>$viejoValor</font>  ->  <font color ='green'>$actualValor</font><html>")
    }
    else {
      texto = (s"<html><b>$campo:</b> $actualValor (sin cambios)</html>")
    }
    new JLabel(texto)
  }

  def escribirValores(campo: String, actual: Any): JLabel = {
    val formatoFecha = new SimpleDateFormat("yyyy-MM-dd")
    val actualValor = actual match {
      case d1: java.util.Date =>
        formatoFecha.format(d1)
      case _ =>
        actual.toString
    }
    val texto = (s"<html><b>$campo:</b> $actualValor</html>")
    new JLabel(texto)
  }
}



