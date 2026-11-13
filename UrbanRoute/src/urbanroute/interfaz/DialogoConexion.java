/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package urbanroute.interfaz;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import urbanroute.modelo.Estacion;
/**
 * Diálogo para elegir dos estaciones y escribir el tiempo de la conexión
 * nueva entre ellas.
 * <p>
 * Las listas desplegables muestran el texto de cada estación. Para saber
 * qué estación eligió el usuario se usa la posición seleccionada en el
 * arreglo {@link #estaciones}, que está en el mismo orden que la lista.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class DialogoConexion extends javax.swing.JDialog {

    /** Estaciones que se muestran, en el mismo orden que las listas. */
    private final Estacion[] estaciones;

    /** {@code true} si el usuario cerró el diálogo con Aceptar. */
    private boolean aceptado = false;

    /** Tiempo escrito por el usuario, ya convertido a número. */
    private double tiempo = 0;

    /**
     * Crea el diálogo modal con las estaciones de la red.
     *
     * @param parent ventana principal
     * @param estaciones estaciones para elegir (al menos 2)
     */
    public DialogoConexion(java.awt.Frame parent, Estacion[] estaciones) {
        super(parent, true);
        initComponents();
        this.estaciones = estaciones;
        String[] textos = new String[estaciones.length];
        for (int i = 0; i < estaciones.length; i++) {
            textos[i] = estaciones[i].toString();
        }
        cmbOrigen.setModel(new DefaultComboBoxModel<>(textos));
        cmbDestino.setModel(new DefaultComboBoxModel<>(textos));
        cmbDestino.setSelectedIndex(1);
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
     * Devuelve el ID de la estación de origen elegida.
     *
     * @return el ID
     */
    public String getIdOrigen() {
        return estaciones[cmbOrigen.getSelectedIndex()].getId();
    }

    /**
     * Devuelve el ID de la estación de destino elegida.
     *
     * @return el ID
     */
    public String getIdDestino() {
        return estaciones[cmbDestino.getSelectedIndex()].getId();
    }

    /**
     * Devuelve el tiempo escrito.
     *
     * @return minutos de traslado
     */
    public double getTiempo() {
        return tiempo;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCampos = new javax.swing.JPanel();
        lblOrigen = new javax.swing.JLabel();
        cmbOrigen = new javax.swing.JComboBox<>();
        lblDestino = new javax.swing.JLabel();
        cmbDestino = new javax.swing.JComboBox<>();
        lblTiempo = new javax.swing.JLabel();
        txtTiempo = new javax.swing.JTextField();
        panelBotones = new javax.swing.JPanel();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Agregar conexión");
        setResizable(false);

        panelCampos.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelCampos.setLayout(new java.awt.GridLayout(0, 2, 8, 8));

        lblOrigen.setText("Origen:");
        panelCampos.add(lblOrigen);

        cmbOrigen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelCampos.add(cmbOrigen);

        lblDestino.setText("Destino:");
        panelCampos.add(lblDestino);

        cmbDestino.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelCampos.add(cmbDestino);

        lblTiempo.setText("Tiempo (minutos):");
        panelCampos.add(lblTiempo);
        panelCampos.add(txtTiempo);

        getContentPane().add(panelCampos, java.awt.BorderLayout.CENTER);

        btnAceptar.setText("Aceptar");
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });
        panelBotones.add(btnAceptar);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        panelBotones.add(btnCancelar);

        getContentPane().add(panelBotones, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        if (cmbOrigen.getSelectedIndex() == cmbDestino.getSelectedIndex()) {
            JOptionPane.showMessageDialog(this,
                    "El origen y el destino deben ser estaciones distintas.",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Se acepta la coma decimal: "2,5" se lee como 2.5
            tiempo = Double.parseDouble(
                    txtTiempo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            tiempo = 0;
        }
        if (tiempo <= 0) {
            JOptionPane.showMessageDialog(this,
                    "El tiempo debe ser un número mayor que 0, por ejemplo 2 o 2.5.",
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
    private javax.swing.JComboBox<String> cmbDestino;
    private javax.swing.JComboBox<String> cmbOrigen;
    private javax.swing.JLabel lblDestino;
    private javax.swing.JLabel lblOrigen;
    private javax.swing.JLabel lblTiempo;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JTextField txtTiempo;
    // End of variables declaration//GEN-END:variables
}
