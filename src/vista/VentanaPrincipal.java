package vista;

import modelo.Contacto;
import modelo.ContactoDAO;
import util.ExportadorCSV;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class VentanaPrincipal extends JFrame {
    private final ContactoDAO dao = new ContactoDAO();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Nombre", "Teléfono", "Email"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);

    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtTelefono = new JTextField(18);
    private final JTextField txtEmail = new JTextField(18);
    private final JTextField txtBuscar = new JTextField(22);
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel lblEstado = new JLabel("Listo.");
    private final PanelEstadisticas panelEstadisticas = new PanelEstadisticas();

    // Controles del formulario y estado de edición.
    private JButton btnAgregar;
    private JButton btnModificar;
    private JButton btnCancelarEdicion;
    private int filaModeloEditando = -1;

    public VentanaPrincipal() {
        super("Gestión de Contactos - MVC");
        configurarVentana();
        construirInterfaz();
        actualizarEstadisticas();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Contactos", construirPanelContactos());
        tabs.addTab("Estadísticas", panelEstadisticas);
        add(tabs, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(10, 5));
        barra.setStringPainted(true);
        barra.setValue(0);
        pie.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        pie.add(lblEstado, BorderLayout.WEST);
        pie.add(barra, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    private JPanel construirPanelContactos() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del contacto"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(formulario, gbc, 0, "Nombre:", txtNombre);
        agregarCampo(formulario, gbc, 1, "Teléfono:", txtTelefono);
        agregarCampo(formulario, gbc, 2, "Email:", txtEmail);

        btnAgregar = new JButton("Agregar (Ctrl+G)");
        btnModificar = new JButton("Modificar seleccionado");
        btnCancelarEdicion = new JButton("Cancelar edición");
        btnCancelarEdicion.setVisible(false);
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnExportar = new JButton("Exportar CSV");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnCancelarEdicion);
        botones.add(btnLimpiar);
        botones.add(btnExportar);

        JPanel parteSuperior = new JPanel(new BorderLayout());
        parteSuperior.add(formulario, BorderLayout.CENTER);
        parteSuperior.add(botones, BorderLayout.SOUTH);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.add(new JLabel("Buscar:"));
        busqueda.add(txtBuscar);
        busqueda.add(new JLabel("Puedes ordenar haciendo clic en los encabezados"));

        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        principal.add(parteSuperior, BorderLayout.NORTH);
        principal.add(new JScrollPane(tabla), BorderLayout.CENTER);
        principal.add(busqueda, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> {
            if (filaModeloEditando >= 0) {
                guardarCambiosContacto();
            } else {
                agregarContacto();
            }
        });
        btnModificar.addActionListener(e -> iniciarModificacion());
        btnCancelarEdicion.addActionListener(e -> cancelarModificacion());
        btnLimpiar.addActionListener(e -> {
            if (filaModeloEditando >= 0) {
                cancelarModificacion();
            } else {
                limpiarFormulario();
            }
        });
        btnExportar.addActionListener(e -> exportarCSV());

        // Atajo global de ventana: Ctrl+G.
        principal.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("control G"), "agregarContacto");
        principal.getActionMap().put("agregarContacto", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { agregarContacto(); }
        });

        // Menú contextual al hacer clic derecho en una fila.
        JPopupMenu menu = new JPopupMenu();
        JMenuItem modificar = new JMenuItem("Modificar contacto");
        JMenuItem eliminar = new JMenuItem("Eliminar contacto");
        menu.add(modificar);
        menu.add(eliminar);
        modificar.addActionListener(e -> iniciarModificacion());
        eliminar.addActionListener(e -> eliminarSeleccionado());

        tabla.addMouseListener(new MouseAdapter() {
            private void mostrarMenu(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                int filaVista = tabla.rowAtPoint(e.getPoint());
                if (filaVista >= 0) {
                    tabla.setRowSelectionInterval(filaVista, filaVista);
                    menu.show(tabla, e.getX(), e.getY());
                }
            }
            @Override public void mousePressed(MouseEvent e) { mostrarMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { mostrarMenu(e); }
        });

        // Filtrado seguro: trata el texto de búsqueda como texto literal.
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = txtBuscar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
            }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        return principal;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila,
                              String etiqueta, JTextField campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void agregarContacto() {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocusInWindow();
            return;
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(this, "Ingresa un correo electrónico válido.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocusInWindow();
            return;
        }

        Contacto contacto = new Contacto(nombre, telefono, email);
        dao.agregar(contacto);
        modeloTabla.addRow(new Object[]{contacto.getNombre(), contacto.getTelefono(), contacto.getEmail()});
        limpiarFormulario();
        actualizarEstadisticas();
        lblEstado.setText("Contacto agregado correctamente.");
    }

    private void iniciarModificacion() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un contacto de la tabla para modificarlo.",
                    "Contacto no seleccionado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        filaModeloEditando = filaModelo;
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 0)));
        txtTelefono.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 1)));
        txtEmail.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 2)));

        btnAgregar.setText("Guardar cambios");
        btnModificar.setEnabled(false);
        btnCancelarEdicion.setVisible(true);
        lblEstado.setText("Editando contacto. Modifica los datos y guarda los cambios.");
        txtNombre.requestFocusInWindow();
    }

    private void guardarCambiosContacto() {
        if (filaModeloEditando < 0 || filaModeloEditando >= modeloTabla.getRowCount()) {
            JOptionPane.showMessageDialog(this,
                    "No hay un contacto válido en edición. Selecciona uno nuevamente.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            cancelarModificacion();
            return;
        }

        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocusInWindow();
            return;
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(this, "Ingresa un correo electrónico válido.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocusInWindow();
            return;
        }

        Contacto actualizado = new Contacto(nombre, telefono, email);
        if (!dao.modificar(filaModeloEditando, actualizado)) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el contacto.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            cancelarModificacion();
            return;
        }

        modeloTabla.setValueAt(actualizado.getNombre(), filaModeloEditando, 0);
        modeloTabla.setValueAt(actualizado.getTelefono(), filaModeloEditando, 1);
        modeloTabla.setValueAt(actualizado.getEmail(), filaModeloEditando, 2);
        cancelarModificacion();
        actualizarEstadisticas();
        lblEstado.setText("Contacto modificado correctamente.");
        JOptionPane.showMessageDialog(this, "Los datos del contacto se actualizaron correctamente.",
                "Modificación completada", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelarModificacion() {
        filaModeloEditando = -1;
        btnAgregar.setText("Agregar (Ctrl+G)");
        btnModificar.setEnabled(true);
        btnCancelarEdicion.setVisible(false);
        limpiarFormulario();
        lblEstado.setText("Edición cancelada. Listo.");
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtNombre.requestFocusInWindow();
    }

    private void eliminarSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un contacto para eliminar.");
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        String nombre = String.valueOf(modeloTabla.getValueAt(filaModelo, 0));
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Deseas eliminar el contacto \"" + nombre + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) return;

        dao.eliminar(filaModelo);
        modeloTabla.removeRow(filaModelo);
        if (filaModeloEditando >= 0) {
            cancelarModificacion();
        }
        actualizarEstadisticas();
        lblEstado.setText("Contacto eliminado.");
    }

    private void exportarCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar contactos como CSV");
        chooser.setSelectedFile(new java.io.File("contactos.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new java.io.File(archivo.getParentFile(), archivo.getName() + ".csv");
        }

        barra.setIndeterminate(true);
        lblEstado.setText("Exportando...");
        try {
            ExportadorCSV.exportar(dao.listar(), Path.of(archivo.getAbsolutePath()));
            barra.setIndeterminate(false);
            barra.setValue(100);
            lblEstado.setText("Exportación completada.");
            JOptionPane.showMessageDialog(this, "Archivo exportado correctamente:\n" + archivo.getAbsolutePath());
        } catch (IOException ex) {
            barra.setIndeterminate(false);
            barra.setValue(0);
            lblEstado.setText("Error al exportar.");
            JOptionPane.showMessageDialog(this, "No se pudo exportar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstadisticas() {
        List<Contacto> contactos = dao.listar();
        int conEmail = 0;
        String ultimo = null;
        for (Contacto c : contactos) {
            if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) conEmail++;
            ultimo = c.getNombre();
        }
        panelEstadisticas.actualizar(contactos.size(), conEmail, contactos.size() - conEmail, ultimo);
    }
}
