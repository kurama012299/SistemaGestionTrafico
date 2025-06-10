package interfaz_usuario

import com.formdev.flatlaf.FlatDarkLaf
import logica.consultas.ConsultaConductor
import logica.modelos.*

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
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

      val menuItemReporteInfracciones= new JMenuItem("Infracciones emitidas")
      val menuItemReporteLicencias= new JMenuItem("Licencias emitidas")

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
      def createTablePanel(title: String, columns: Array[Object], data: Array[Array[Object]]): JPanel =
        val panel = new JPanel(new BorderLayout())
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20))

        // Crear tabla
        val tableModel = new DefaultTableModel(data, columns) {
          override def isCellEditable(row: Int, column: Int): Boolean = false
        }
        tableConductores.getTableHeader.setReorderingAllowed(false)
        tableConductores.getTableHeader.setResizingAllowed(false)
        tableConductores.setModel(tableModel)
        tableConductores.setFillsViewportHeight(true)


        // Crear botones
        val buttonPanel = new JPanel()
        val agregarBtn = new JButton("Agregar")
        val editarBtn = new JButton("Editar")
        val eliminarBtn = new JButton("Eliminar")

        // Configurar acciones de los botones
        agregarBtn.addActionListener((e: ActionEvent) => {
          val modelo = tableConductores.getModel.asInstanceOf[DefaultTableModel]
          agregarConductor(modelo)
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
            modelo.removeRow(filaSeleccionada)
            JOptionPane.showMessageDialog(frame, s"Acción eliminar para registro ID: ${tableConductores.getValueAt(tableConductores.getSelectedRow(), 0)} en $title")
            // Nota: No eliminamos realmente el registro como se solicitó
          }
        })

        buttonPanel.add(agregarBtn)
        buttonPanel.add(editarBtn)
        buttonPanel.add(eliminarBtn)

        // Configurar panel
        panel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH)
        panel.add(new JScrollPane(tableConductores), BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)
        panel

      // Datos para las tablas
      val conductorColumns = Array[Object]("ID", "Nombre", "Apellido", "Licencia", "Teléfono")
      val conductorData: Array[Array[Object]] = {
        val conductores = ConsultaConductor.obtenerTodosLosConductores()

        conductores.map { conductor =>
          Array[Object](
            conductor.id.getOrElse("").toString,
            conductor.nombre,
            conductor.apellido,
            conductor.licencia.get.id.getOrElse("").toString,
            conductor.telefono
          )
        }.toArray
      }

      val licenciaColumns = Array[Object]("ID", "Número", "Tipo", "Emisión", "Vencimiento")
      val licenciaData = Array[Array[Object]](
        Array[Object]("1", "ABC123", "A", "01/01/2020", "01/01/2030"),
        Array[Object]("2", "DEF456", "B", "15/03/2021", "15/03/2031"),
        Array[Object]("3", "GHI789", "C", "20/05/2022", "20/05/2032")
      )

      val infraccionColumns = Array[Object]("ID", "Código", "Descripción", "Fecha", "Gravedad")
      val infraccionData = Array[Array[Object]](
        Array[Object]("1", "ART-45", "Exceso de velocidad", "10/06/2023", "Leve"),
        Array[Object]("2", "ART-78", "No usar cinturón", "15/07/2023", "Grave"),
        Array[Object]("3", "ART-92", "Teléfono al conducir", "20/08/2023", "Muy grave")
      )

      // Crear paneles para cada vista
      val conductorPanel = createTablePanel("Conductores", conductorColumns, conductorData)
      //val licenciaPanel = createTablePanel("Licencias", licenciaColumns, licenciaData)
      //val infraccionPanel = createTablePanel("Infracciones", infraccionColumns, infraccionData)

      // Agregar paneles al card layout
      cardPanel.add(initialPanel, "inicio")
      cardPanel.add(conductorPanel, "conductores")
      //cardPanel.add(licenciaPanel, "licencias")
      //cardPanel.add(infraccionPanel, "infracciones")

      // Configurar acciones del menú
      menuItemConductor.addActionListener((e: ActionEvent) => {
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "conductores")
      })

      menuItemLicencia.addActionListener((e: ActionEvent) => {
        cardPanel.getLayout.asInstanceOf[CardLayout].show(cardPanel, "licencias")
      })

      menuItemInfraccion.addActionListener((e: ActionEvent) => {
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


  def agregarConductor(modelo: DefaultTableModel): Unit = {

    val campoNombre = new JTextField()
    val campoApellido = new JTextField()
    val campoLicencia = new JTextField()
    val campoTelefono = new JTextField()

    // Creamos un array con los campos y sus etiquetas
    val campos = Array(
      "Nombre:", campoNombre,
      "Apellido:", campoApellido,
      "Licencia:", campoLicencia,
      "Teléfono:", campoTelefono
    )

    val resultado = JOptionPane.showConfirmDialog(
      null,
      campos,
      "Registro de Conductor",
      JOptionPane.OK_CANCEL_OPTION
    )

    // Si el usuario hace clic en OK, procesamos los datos
    if (resultado == JOptionPane.OK_OPTION) {
      val contadorFilas = modelo.getRowCount
      val conductor = Conductor(
        (Some(contadorFilas + 1)),
        campoNombre.getText,
        campoApellido.getText,
        null,
        campoTelefono.getText
      )

      // Mostramos los datos capturados (puedes guardarlos en una lista, BD, etc.)
      JOptionPane.showMessageDialog(
        null,
        s"""
           |Conductor registrado:
           |Nombre: ${conductor.nombre}
           |Apellido: ${conductor.apellido}
           |Licencia: ${conductor.licencia}
           |Teléfono: ${conductor.telefono}
           |""".stripMargin,
        "Éxito",
        JOptionPane.INFORMATION_MESSAGE
      )
      modelo.addRow(Array[AnyRef](
        String.valueOf(contadorFilas + 1),
        campoNombre.getText,
        campoApellido.getText,
        campoLicencia.getText,
        campoTelefono.getText)
      )
    } else {
      JOptionPane.showMessageDialog(
        null,
        "Registro cancelado",
        "Aviso",
        JOptionPane.WARNING_MESSAGE
      )
    }

  }

  def editarConductor(modelo: DefaultTableModel, fila: Int): Unit = {
    // Obtener datos actuales de la fila
    val nombre = modelo.getValueAt(fila, 1).toString
    val apellido = modelo.getValueAt(fila, 2).toString
    val licencia = modelo.getValueAt(fila, 3).toString
    val telefono = modelo.getValueAt(fila, 4).toString

    // Crear campos con los valores actuales
    val campoNombre = new JTextField(nombre)
    val campoApellido = new JTextField(apellido)
    val campoLicencia = new JTextField(licencia)
    val campoTelefono = new JTextField(telefono)

    val campos = Array(
      "Nombre:", campoNombre,
      "Apellido:", campoApellido,
      "Licencia:", campoLicencia,
      "Teléfono:", campoTelefono
    )

    if (JOptionPane.showConfirmDialog(null, campos, "Editar Conductor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      // Actualizar la fila
      modelo.setValueAt(campoNombre.getText, fila, 1)
      modelo.setValueAt(campoApellido.getText, fila, 2)
      modelo.setValueAt(campoLicencia.getText, fila, 3)
      modelo.setValueAt(campoTelefono.getText, fila, 4)
    }
  }
}
