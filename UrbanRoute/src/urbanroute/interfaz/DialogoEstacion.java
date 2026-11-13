/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package urbanroute.interfaz;
import javax.swing.JOptionPane;
/**
 * Diálogo para escribir los datos de una estación nueva.
 * <p>
 * Solo recoge los datos y revisa que no falten los obligatorios. La
 * ventana principal se los pasa al {@link urbanroute.logica.ControladorRed},
 * que hace la validación completa (ID o nombre repetidos, etc.).
 * <p>
 * Es modal: mientras está abierto no se puede usar la ventana principal, y
 * {@code setVisible(true)} no termina hasta que el diálogo se cierra.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class DialogoEstacion extends javax.swing.JDialog {

    /**
     * Creates new form DialogoEstacion
     */
/** {@code true} si el usuario cerró el diálogo con Aceptar. */
    private boolean aceptado = false;

    /** Capacidad escrita por el usuario, ya convertida a número. */
    private int capacidad = 0;

    /**
     * Crea el diálogo centrado sobre la ventana principal.
     *
     * @param parent ventana principal
     * @param modal {@code true} para bloquear la ventana principal
     */
    public DialogoEstacion(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Indica si el usuario pulsó Aceptar con datos válidos.
     *
     * @return {@code true} si se aceptó; {@code false} si se canceló
     */
    public boolean fueAceptado() {
        return aceptado;
    }

    /**
     * Devuelve el ID escrito.
     *
     * @return el ID
     */
    public String getIdEstacion() {
        return txtId.getText();
    }

    /**
     * Devuelve el nombre escrito.
     *
     * @return el nombre
     */
    public String getNombre() {
        return txtNombre.getText();
    }

    /**
     * Devuelve la línea escrita.
     *
     * @return la línea (puede estar vacía)
     */
    public String getLinea() {
        return txtLinea.getText();
    }

    /**
     * Devuelve la zona escrita.
     *
     * @return la zona (puede estar vacía)
     */
    public String getZona() {
        return txtZona.getText();
    }

    /**
     * Devuelve la capacidad escrita.
     *
     * @return pasajeros por hora
     */
    public int getCapacidad() {
        return capacidad;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBotones = new javax.swing.JPanel();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        panelCampos = new javax.swing.JPanel();
        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblLinea = new javax.swing.JLabel();
        txtLinea = new javax.swing.JTextField();
        lblZona = new javax.swing.JLabel();
        txtZona = new javax.swing.JTextField();
        lblCapacidad = new javax.swing.JLabel();
        txtCapacidad = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Agregar estación");
        setResizable(false);

        btnAceptar.setText("Aceptar");
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addComponent(btnAceptar)
                .addGap(37, 37, 37)
                .addComponent(btnCancelar)
                .addContainerGap(85, Short.MAX_VALUE))
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotonesLayout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addGroup(panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptar)
                    .addComponent(btnCancelar))
                .addGap(33, 33, 33))
        );

        getContentPane().add(panelBotones, java.awt.BorderLayout.SOUTH);

        panelCampos.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelCampos.setLayout(new java.awt.GridLayout(0, 2, 8, 8));

        lblId.setText("ID:");
        panelCampos.add(lblId);
        panelCampos.add(txtId);

        lblNombre.setText("Nombre:");
        panelCampos.add(lblNombre);
        panelCampos.add(txtNombre);

        lblLinea.setText("Línea:");
        panelCampos.add(lblLinea);
        panelCampos.add(txtLinea);

        lblZona.setText("Zona:");
        panelCampos.add(lblZona);
        panelCampos.add(txtZona);

        lblCapacidad.setText("Capacidad (pasajeros/hora):");
        panelCampos.add(lblCapacidad);

        txtCapacidad.setText("0");
        panelCampos.add(txtCapacidad);

        getContentPane().add(panelCampos, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        if (txtId.getText().trim().isEmpty()
                || txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El ID y el nombre son obligatorios.",
                    "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            capacidad = Integer.parseInt(txtCapacidad.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "La capacidad debe ser un número entero, por ejemplo 12000.",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        aceptado = true;
        dispose();
    }//GEN-LAST:event_btnAceptarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JLabel lblCapacidad;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblLinea;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblZona;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JTextField txtCapacidad;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtLinea;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtZona;
    // End of variables declaration//GEN-END:variables
}
