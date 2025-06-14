package interfaz_usuario

import com.formdev.flatlaf.FlatDarkLaf
import com.toedter.calendar.{JCalendar, JDateChooser}
import logica.consultas.{ConsultaConductor, ConsultaInfraccion, ConsultaLicencia}
import logica.modelos.*

import java.awt.*
import java.awt.event.*
import java.text.SimpleDateFormat
import java.time.{LocalDate, ZoneId}
import java.util.{Date, Locale}
import javax.swing.*
import javax.swing.table.DefaultTableModel
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

      // Menú principal "Opciones"
      val menuOpciones = new JMenu("Opciones")
      val menuReportes = new JMenu("Reportes")

      // Elementos del menú desplegable
      val menuItemConductor = new JMenuItem("Conductor")
      val menuItemLicencia = new JMenuItem("Licencia")
      val menuItemInfraccion = new JMenuItem("Infracción")

      val menuItemReporteInfracciones = new JMenuItem("Infracciones emitidas")
      val menuItemReporteLicencias = new JMenuItem("Licencias emitidas")

      // Panel principal (card layout para cambiar vistas)
      val cardPanel = new JPanel(new CardLayout())

      // Panel inicial vacío
      val initialPanel = new JPanel()
      val initialLabel = new JLabel("Seleccione una opción del menú", SwingConstants.CENTER)
      initialLabel.setFont(new Font("Arial", Font.PLAIN, 24))
      initialPanel.add(initialLabel)
      val tableConductores = new JTable()
      val tableLicencias = new JTable()
      val tableInfracciones = new JTable()

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
              editarConductor(modelo, filaSeleccionada)
              JOptionPane.showMessageDialog(frame, s"Editando registro ID: ${tableConductores.getValueAt(tableConductores.getSelectedRow(), 0)} en $title")
            }
          })

          eliminarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableConductores.getSelectedRow
            val modelo = tableConductores.getModel.asInstanceOf[DefaultTableModel]
            if (tableConductores.getSelectedRow() == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para eliminar en $title")
            } else {
              try{
                val carnetIdentidad: Long=modelo.getValueAt(tableConductores.getSelectedRow,0).toString.toLong
                val idLicencia:Long=modelo.getValueAt(tableConductores.getSelectedRow,3).toString.toLong
                ConsultaConductor.eliminarConductor(carnetIdentidad,idLicencia)
                if (mostrarMensajeConfirmacionEliminar()) {
                  modelo.removeRow(filaSeleccionada)
                }
              }catch{
                case e:Exception=>
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
              editarInfraccion(modelo, filaSeleccionada)
              JOptionPane.showMessageDialog(frame, s"Editando registro ID: ${tableInfracciones.getValueAt(tableInfracciones.getSelectedRow(), 0)} en $title")
            }
          })

          eliminarBtn.addActionListener((e: ActionEvent) => {
            val filaSeleccionada = tableInfracciones.getSelectedRow
            val modelo = tableInfracciones.getModel.asInstanceOf[DefaultTableModel]
            if (tableInfracciones.getSelectedRow() == -1) {
              JOptionPane.showMessageDialog(frame, s"Seleccione un registro para eliminar en $title")
            } else {
              try{
                val idInfraccion=modelo.getValueAt(tableInfracciones.getSelectedRow,0).toString.toLong
                ConsultaInfraccion.eliminarInfraccion(idInfraccion)
                if (mostrarMensajeConfirmacionEliminar()) {
                  modelo.removeRow(filaSeleccionada)
                }
              }catch{
                case e:Exception=>
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
              editarLicencia(modelo, filaSeleccionada)
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
      })

      menuItemLicencia.addActionListener((e: ActionEvent) => {
        val panel = cargarLicencias()
        cardPanel.add(panel, "licencias")
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "licencias")
      })

      menuItemInfraccion.addActionListener((e: ActionEvent) => {
        val panel = cargarInfracciones()
        cardPanel.add(panel, "infracciones")
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "infracciones")
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
      var idLicencia=Long.MinValue
      ConsultaLicencia.obtenerUltimaLicencia() match{
        case Right(id) => idLicencia=id
        case Left(error)=> printf("No se pudo obtener la ultima licencia \n" + error)
      }
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
        String.valueOf(contadorFilas + 1),
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

    } else {
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
            printf("No se pudo crear la infraccion \n" + e.getMessage)
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

  def editarConductor(modeloConductor: DefaultTableModel, fila: Int): Unit = {
    // Obtener datos actuales de la fila
    val ci=modeloConductor.getValueAt(fila,0).toString
    val nombre = modeloConductor.getValueAt(fila, 1).toString
    val apellido = modeloConductor.getValueAt(fila, 2).toString
    val telefono = modeloConductor.getValueAt(fila, 4).toString

    // Crear campos con los valores actuales
    val campoCi=new JTextField(ci)
    val campoNombre = new JTextField(nombre)
    val campoApellido = new JTextField(apellido)
    val campoTelefono = new JTextField(telefono)

    val campos = Array(
      "Nombre:", campoNombre,
      "Apellido:", campoApellido,
      "Ci:",campoCi,
      "Teléfono:", campoTelefono
    )

    if (JOptionPane.showConfirmDialog(null, campos, "Editar Conductor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      // Actualizar la fila

      modeloConductor.setValueAt(campoCi.getText,fila,0)
      modeloConductor.setValueAt(campoNombre.getText, fila, 1)
      modeloConductor.setValueAt(campoApellido.getText, fila, 2)
      modeloConductor.setValueAt(campoTelefono.getText, fila, 4)

      val conductor =Conductor (
        Some(campoCi.getText.toLong),
        campoNombre.getText,
        campoApellido.getText,
        None,
        campoTelefono.getText
      )
      try{
        ConsultaConductor.editarConductorConsulta(conductor, ci.toLong)
      }catch
        case e:Exception =>
          printf("No se pudo editar el conductor \n"+e.getMessage)
    }
  }


  def editarInfraccion(modeloInfraccion: DefaultTableModel, fila: Int): Unit = {
    // Obtener datos actuales de la fila

    val opcionesPuntos = Array[AnyRef](" ", Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(12))
    val modeloOpcionesPuntos = new DefaultComboBoxModel[AnyRef](opcionesPuntos)

    val opcionesGravedad = Array[String](" ", "Leve", "Grave", "Muy grave")
    val modeloOpcionesGravedad = new DefaultComboBoxModel[String](opcionesGravedad)

    val idInfraccion=modeloInfraccion.getValueAt(fila,0).toString
    val idLicencia = modeloInfraccion.getValueAt(fila, 1).toString
    val puntosDeducidos = modeloInfraccion.getValueAt(fila, 2)
    val gravedad = modeloInfraccion.getValueAt(fila, 3).toString
    val fecha = modeloInfraccion.getValueAt(fila, 4).toString


    // Crear campos con los valores actuales
    val campoIdLicencia = new JLabel(idLicencia)
    val campoPuntosDeducidos = new JComboBox[AnyRef](modeloOpcionesPuntos)
    campoPuntosDeducidos.setSelectedItem(puntosDeducidos)
    val campoGravedad = new JComboBox[String](modeloOpcionesGravedad)
    campoGravedad.setSelectedItem(gravedad)
    val campoFecha = new JDateChooser()
    println(convertirFecha(fecha.toString))
    convertirFecha(fecha.toString) match
      case Some(fecha) =>
        campoFecha.setDate(fecha)
      case None =>
        println("Formato de fecha incorrecto")

    val formato = new SimpleDateFormat("yyyy-MM-dd")
    val fechaFormateada = formato.format(campoFecha.getDate)

    val campos = Array(
      "Id Licencia:", campoIdLicencia,
      "Puntos deducidos:", campoPuntosDeducidos,
      "Gravedad:", campoGravedad,
      "Fecha:", campoFecha
    )

    if (JOptionPane.showConfirmDialog(null, campos, "Editar Infraccion", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      // Actualizar la fila
      val fechaFormateadaEditada = formato.format(campoFecha.getDate)
      modeloInfraccion.setValueAt(campoPuntosDeducidos.getSelectedItem, fila, 2)
      modeloInfraccion.setValueAt(campoGravedad.getSelectedItem, fila, 3)
      modeloInfraccion.setValueAt(fechaFormateadaEditada, fila, 4)

      val infraccion=Infraccion(
        Some(idInfraccion.toLong),
        idLicencia.toLong,
        Integer.valueOf(campoPuntosDeducidos.getSelectedItem.toString),
        campoGravedad.getSelectedItem.toString,
        campoFecha.getDate
      )
      try {
        ConsultaInfraccion.editarInfraccionConsulta(infraccion,Integer.valueOf(puntosDeducidos.toString))
      } catch
        case e: Exception =>
          printf("No se pudo editar la infraccion \n" + e.getMessage)
    }
    }

  def editarLicencia(modeloLicencia: DefaultTableModel, fila: Int): Unit = {

    val id=modeloLicencia.getValueAt(fila,0).toString
    val moto = modeloLicencia.getValueAt(fila, 1)
    val carro = modeloLicencia.getValueAt(fila, 2)
    val camion = modeloLicencia.getValueAt(fila, 3)
    val omnibus = modeloLicencia.getValueAt(fila, 4)

    val campoMoto = new JRadioButton()
    val campoCarro = new JRadioButton()
    val campoCamion = new JRadioButton()
    val campoOmnibus = new JRadioButton()

    campoMoto.setSelected(convertirStringABoolean(moto.toString))
    campoCarro.setSelected(convertirStringABoolean(carro.toString))
    campoCamion.setSelected(convertirStringABoolean(camion.toString))
    campoOmnibus.setSelected(convertirStringABoolean(omnibus.toString))


    val campos = Array(
      "Categoria Moto:", campoMoto,
      "Categoria Carro:", campoCarro,
      "Categoria Camion:", campoCamion,
      "Categoria Omnibus:", campoOmnibus
    )

    if (JOptionPane.showConfirmDialog(null, campos, "Editar Licencia", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      // Actualizar la fila
      modeloLicencia.setValueAt(convertirBoolean(campoMoto.isSelected), fila, 1)
      modeloLicencia.setValueAt(convertirBoolean(campoCarro.isSelected), fila, 2)
      modeloLicencia.setValueAt(convertirBoolean(campoCamion.isSelected), fila, 3)
      modeloLicencia.setValueAt(convertirBoolean(campoOmnibus.isSelected), fila, 4)

      val licencia =Licencia(
        Some(id.toLong),
        convertirStringABoolean(convertirBoolean(campoMoto.isSelected)),
        convertirStringABoolean(convertirBoolean(campoCarro.isSelected)),
        convertirStringABoolean(convertirBoolean(campoCamion.isSelected)),
        convertirStringABoolean(convertirBoolean(campoOmnibus.isSelected)),
        0,
        null,
        null
      )
      try {
        ConsultaLicencia.editarLicenciaConsulta(licencia)
      } catch
        case e: Exception =>
          printf("No se pudo editar la licencia \n" + e.getMessage)
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
}



