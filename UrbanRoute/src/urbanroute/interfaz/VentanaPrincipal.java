/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package urbanroute.interfaz;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import urbanroute.logica.ControladorRed;
/**
 * Ventana principal de UrbanRoute, diseñada con el GUI Builder de NetBeans.
 * <p>
 * No contiene lógica de la red: cada acción del usuario se le pide al
 * {@link ControladorRed}, y la ventana solo muestra los resultados y los
 * mensajes de error.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class VentanaPrincipal extends javax.swing.JFrame {

/** Coordinador al que la ventana le pide todo. */
    private final ControladorRed controlador;

    /**
     * Crea la ventana principal y carga la red maestra de data/.
     */
    public VentanaPrincipal() {
        initComponents();
        controlador = new ControladorRed();
        setSize(1100, 700);
        setLocationRelativeTo(null);
        cargarRedInicial();
    }

    /**
     * Carga los archivos maestros al iniciar. Si faltan, avisa y deja la
     * red vacía para que el usuario cargue otra.
     */
    private void cargarRedInicial() {
        try {
            String[] advertencias = controlador.cargarArchivosMaestros();
            txtResultados.setText("Red maestra cargada desde la carpeta data.");
            if (advertencias.length > 0) {
                mostrarAdvertencias(advertencias);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la red maestra:\n" + e.getMessage()
                    + "\n\nUse \"Cargar red\" para elegir otros archivos.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        actualizarEstado();
    }

    /**
     * Pide el dataset y el archivo de grafo con JFileChooser y carga la red.
     * Antes, si hay cambios sin guardar, pregunta si se desea guardarlos.
     */
    private void cargarRed() {
        if (!confirmarCambiosPendientes()) {
            return;
        }
        File dataset = elegirArchivo("Seleccione el dataset de estaciones");
        if (dataset == null) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "No eligió un dataset. ¿Desea cargar solo el archivo de "
                    + "grafo?\n(Las estaciones se crearán como provisionales.)",
                    "Cargar red", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        File grafo = elegirArchivo("Seleccione el archivo de grafo");
        if (grafo == null) {
            return;
        }
        try {
            String rutaDataset = (dataset == null) ? null : dataset.getPath();
            String[] advertencias = controlador.cargar(rutaDataset, grafo.getPath());
            txtResultados.setText("Red cargada desde:\n"
                    + (dataset == null ? "(sin dataset)" : dataset.getName())
                    + "\n" + grafo.getName());
            mostrarAdvertencias(advertencias);
        } catch (IOException e) {
            mostrarError("No se pudo cargar la red:\n" + e.getMessage()
                    + "\n\nSe conserva la red anterior.");
        }
        actualizarEstado();
    }

    /**
     * Guarda la red en los archivos de donde se cargó.
     *
     * @return true si se guardó; false si hubo un error
     */
    private boolean guardarRed() {
        try {
            controlador.guardar();
            actualizarEstado();
            JOptionPane.showMessageDialog(this, "La red se guardó.",
                    "Guardar", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            mostrarError("No se pudo guardar la red:\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Si hay cambios sin guardar, pregunta qué hacer con ellos.
     *
     * @return true si se puede continuar; false si el usuario canceló o
     * falló el guardado
     */
    private boolean confirmarCambiosPendientes() {
        if (!controlador.hayCambiosSinGuardar()) {
            return true;
        }
        Object[] opciones = {"Guardar", "No guardar", "Cancelar"};
        int respuesta = JOptionPane.showOptionDialog(this,
                "Hay cambios sin guardar en la red.\n"
                + "¿Desea guardarlos antes de continuar?",
                "Cambios sin guardar", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
        if (respuesta == 0) {
            return guardarRed();
        }
        return respuesta == 1;
    }

    /**
     * Abre un JFileChooser que muestra solo archivos .csv.
     *
     * @param titulo título de la ventana de selección
     * @return el archivo elegido, o null si el usuario canceló
     */
    private File elegirArchivo(String titulo) {
        JFileChooser selector = new JFileChooser(new File("data"));
        selector.setDialogTitle(titulo);
        selector.setFileFilter(
                new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return selector.getSelectedFile();
        }
        return null;
    }

    /**
     * Muestra el resumen de una carga y, si las hay, sus advertencias.
     *
     * @param advertencias advertencias de la carga
     */
    private void mostrarAdvertencias(String[] advertencias) {
        String resumen = controlador.getResumen();
        if (advertencias.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Red cargada: " + resumen + "\nSin advertencias.",
                    "Red cargada", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (String advertencia : advertencias) {
            texto.append(advertencia).append('\n');
        }
        JTextArea area = new JTextArea(texto.toString(), 12, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this,
                new Object[]{"Red cargada: " + resumen + "\n"
                    + advertencias.length + " advertencia(s):",
                    new JScrollPane(area)},
                "Red cargada", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Muestra un mensaje de error.
     *
     * @param mensaje texto del error
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Actualiza la barra de estado con el resumen de la red.
     */
    private void actualizarEstado() {
        lblEstado.setText(" " + controlador.getResumen());
    }

    /**
     * Cierra el programa, preguntando antes si hay cambios sin guardar.
     */
    private void salir() {
        if (confirmarCambiosPendientes()) {
            dispose();
            System.exit(0);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupoColor = new javax.swing.ButtonGroup();
        lblEstado = new javax.swing.JLabel();
        panelControles = new javax.swing.JPanel();
        lblArchivo = new javax.swing.JLabel();
        btnCargar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        lblModificar = new javax.swing.JLabel();
        btnAgregarEstacion = new javax.swing.JButton();
        btnEliminarEstacion = new javax.swing.JButton();
        btnAgregarConexion = new javax.swing.JButton();
        btnEliminarConexion = new javax.swing.JButton();
        lblAnalisis = new javax.swing.JLabel();
        btnRuta = new javax.swing.JButton();
        btnSubredes = new javax.swing.JButton();
        btnHubs = new javax.swing.JButton();
        lblColorear = new javax.swing.JLabel();
        rbLinea = new javax.swing.JRadioButton();
        rbSubred = new javax.swing.JRadioButton();
        lblBuscar = new javax.swing.JLabel();
        panelBuscar = new javax.swing.JPanel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        panelCentral = new javax.swing.JPanel();
        panelGrafo = new javax.swing.JPanel();
        lblGrafoPendiente = new javax.swing.JLabel();
        scrollResultados = new javax.swing.JScrollPane();
        txtResultados = new javax.swing.JTextArea();
        barraMenu = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        itemCargar = new javax.swing.JMenuItem();
        itemGuardar = new javax.swing.JMenuItem();
        itemSalir = new javax.swing.JMenuItem();
        menuRed = new javax.swing.JMenu();
        itemAgregarEstacion = new javax.swing.JMenuItem();
        itemAgregarConexion = new javax.swing.JMenuItem();
        itemEliminarEstacion = new javax.swing.JMenuItem();
        itemEliminarConexion = new javax.swing.JMenuItem();
        menuAnalisis = new javax.swing.JMenu();
        itemRuta = new javax.swing.JMenuItem();
        itemSubredes = new javax.swing.JMenuItem();
        itemHubs = new javax.swing.JMenuItem();
        menuAyuda = new javax.swing.JMenu();
        itemComoUsar = new javax.swing.JMenuItem();
        itemAcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("UrbanRoute - Red de Transporte Multimodal");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lblEstado.setText("Cargando...");
        lblEstado.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        getContentPane().add(lblEstado, java.awt.BorderLayout.SOUTH);

        panelControles.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panelControles.setLayout(new java.awt.GridLayout(0, 1, 0, 4));

        lblArchivo.setText("       ARCHIVO");
        panelControles.add(lblArchivo);

        btnCargar.setText("Cargar red");
        btnCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarActionPerformed(evt);
            }
        });
        panelControles.add(btnCargar);

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        panelControles.add(btnGuardar);

        lblModificar.setText(" MODIFICAR RED");
        panelControles.add(lblModificar);

        btnAgregarEstacion.setText("+ Estación");
        panelControles.add(btnAgregarEstacion);

        btnEliminarEstacion.setText("- Estación");
        panelControles.add(btnEliminarEstacion);

        btnAgregarConexion.setText("+ Conexion");
        panelControles.add(btnAgregarConexion);

        btnEliminarConexion.setText("- Conexión");
        panelControles.add(btnEliminarConexion);

        lblAnalisis.setText("       ANÁLISIS");
        panelControles.add(lblAnalisis);

        btnRuta.setText("Ruta óptima");
        panelControles.add(btnRuta);

        btnSubredes.setText("Subredes");
        panelControles.add(btnSubredes);

        btnHubs.setText("Hubs");
        panelControles.add(btnHubs);

        lblColorear.setText("    Colorear por:");
        panelControles.add(lblColorear);

        grupoColor.add(rbLinea);
        rbLinea.setSelected(true);
        rbLinea.setText("Linea");
        panelControles.add(rbLinea);

        grupoColor.add(rbSubred);
        rbSubred.setText("Subred");
        panelControles.add(rbSubred);

        lblBuscar.setText(" Buscar estación:");
        panelControles.add(lblBuscar);

        panelBuscar.setLayout(new java.awt.BorderLayout());
        panelBuscar.add(txtBuscar, java.awt.BorderLayout.CENTER);

        btnBuscar.setText("Buscar");
        panelBuscar.add(btnBuscar, java.awt.BorderLayout.EAST);

        panelControles.add(panelBuscar);

        getContentPane().add(panelControles, java.awt.BorderLayout.LINE_START);

        panelCentral.setLayout(new java.awt.BorderLayout());

        panelGrafo.setLayout(new java.awt.BorderLayout());

        lblGrafoPendiente.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGrafoPendiente.setText("Aquí se dibujará el grafo");
        panelGrafo.add(lblGrafoPendiente, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelGrafo, java.awt.BorderLayout.CENTER);

        txtResultados.setEditable(false);
        txtResultados.setColumns(20);
        txtResultados.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        txtResultados.setLineWrap(true);
        txtResultados.setRows(7);
        txtResultados.setWrapStyleWord(true);
        scrollResultados.setViewportView(txtResultados);

        panelCentral.add(scrollResultados, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panelCentral, java.awt.BorderLayout.CENTER);

        menuArchivo.setText("Archivo");

        itemCargar.setText("Cargar red...");
        itemCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCargarActionPerformed(evt);
            }
        });
        menuArchivo.add(itemCargar);

        itemGuardar.setText("Guardar");
        itemGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemGuardarActionPerformed(evt);
            }
        });
        menuArchivo.add(itemGuardar);

        itemSalir.setText("Salir");
        itemSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemSalirActionPerformed(evt);
            }
        });
        menuArchivo.add(itemSalir);

        barraMenu.add(menuArchivo);

        menuRed.setText("Red");

        itemAgregarEstacion.setText("Agregar estación...");
        menuRed.add(itemAgregarEstacion);

        itemAgregarConexion.setText("Agregar conexión...");
        menuRed.add(itemAgregarConexion);

        itemEliminarEstacion.setText("Eliminar estación...");
        menuRed.add(itemEliminarEstacion);

        itemEliminarConexion.setText("Eliminar conexión...");
        menuRed.add(itemEliminarConexion);

        barraMenu.add(menuRed);

        menuAnalisis.setText("Análisis");

        itemRuta.setText("Ruta óptima...");
        menuAnalisis.add(itemRuta);

        itemSubredes.setText("Subredes");
        menuAnalisis.add(itemSubredes);

        itemHubs.setText("Hubs");
        itemHubs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemHubsActionPerformed(evt);
            }
        });
        menuAnalisis.add(itemHubs);

        barraMenu.add(menuAnalisis);

        menuAyuda.setText("Ayuda");

        itemComoUsar.setText("Cómo usar");
        itemComoUsar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemComoUsarActionPerformed(evt);
            }
        });
        menuAyuda.add(itemComoUsar);

        itemAcercaDe.setText("Acerca de");
        itemAcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemAcercaDeActionPerformed(evt);
            }
        });
        menuAyuda.add(itemAcercaDe);

        barraMenu.add(menuAyuda);

        setJMenuBar(barraMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void itemCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCargarActionPerformed
        cargarRed();
    }//GEN-LAST:event_itemCargarActionPerformed

    private void itemHubsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemHubsActionPerformed
        
    }//GEN-LAST:event_itemHubsActionPerformed

    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarActionPerformed
        cargarRed();
    }//GEN-LAST:event_btnCargarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarRed();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void itemGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemGuardarActionPerformed
        guardarRed();
    }//GEN-LAST:event_itemGuardarActionPerformed

    private void itemSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSalirActionPerformed
        salir();
    }//GEN-LAST:event_itemSalirActionPerformed

    private void itemComoUsarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemComoUsarActionPerformed
        JOptionPane.showMessageDialog(this,
                "1. Al abrir, se carga la red de la carpeta data.\n"
                + "2. \"Cargar red\" pide el dataset y luego el archivo de grafo.\n"
                + "3. Use los botones de \"Modificar red\" para cambiarla.\n"
                + "4. \"Guardar\" escribe los cambios en los mismos archivos.\n"
                + "5. \"Análisis\" calcula rutas, subredes y hubs.",
                "Cómo usar UrbanRoute", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_itemComoUsarActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        salir();
    }//GEN-LAST:event_formWindowClosing

    private void itemAcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAcercaDeActionPerformed
        JOptionPane.showMessageDialog(this,
                "UrbanRoute\nEstructuras de Datos — Unimet\n\n"
                + "Gianmarco Del Giudice\nAlberto Kolster Garcia",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_itemAcercaDeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar barraMenu;
    private javax.swing.JButton btnAgregarConexion;
    private javax.swing.JButton btnAgregarEstacion;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnEliminarConexion;
    private javax.swing.JButton btnEliminarEstacion;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnHubs;
    private javax.swing.JButton btnRuta;
    private javax.swing.JButton btnSubredes;
    private javax.swing.ButtonGroup grupoColor;
    private javax.swing.JMenuItem itemAcercaDe;
    private javax.swing.JMenuItem itemAgregarConexion;
    private javax.swing.JMenuItem itemAgregarEstacion;
    private javax.swing.JMenuItem itemCargar;
    private javax.swing.JMenuItem itemComoUsar;
    private javax.swing.JMenuItem itemEliminarConexion;
    private javax.swing.JMenuItem itemEliminarEstacion;
    private javax.swing.JMenuItem itemGuardar;
    private javax.swing.JMenuItem itemHubs;
    private javax.swing.JMenuItem itemRuta;
    private javax.swing.JMenuItem itemSalir;
    private javax.swing.JMenuItem itemSubredes;
    private javax.swing.JLabel lblAnalisis;
    private javax.swing.JLabel lblArchivo;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblColorear;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblGrafoPendiente;
    private javax.swing.JLabel lblModificar;
    private javax.swing.JMenu menuAnalisis;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenu menuAyuda;
    private javax.swing.JMenu menuRed;
    private javax.swing.JPanel panelBuscar;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelControles;
    private javax.swing.JPanel panelGrafo;
    private javax.swing.JRadioButton rbLinea;
    private javax.swing.JRadioButton rbSubred;
    private javax.swing.JScrollPane scrollResultados;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextArea txtResultados;
    // End of variables declaration//GEN-END:variables
}
