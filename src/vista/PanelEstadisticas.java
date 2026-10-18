package vista;

import javax.swing.*;
import java.awt.*;

public class PanelEstadisticas extends JPanel {
    private final JLabel lblTotal = new JLabel("Total de contactos: 0");
    private final JLabel lblConEmail = new JLabel("Contactos con correo: 0");
    private final JLabel lblSinEmail = new JLabel("Contactos sin correo: 0");
    private final JLabel lblUltimo = new JLabel("Último contacto agregado: Ninguno");

    public PanelEstadisticas() {
        setLayout(new GridLayout(0, 1, 8, 8));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        Font fuente = new Font("SansSerif", Font.PLAIN, 16);
        for (JLabel etiqueta : new JLabel[]{lblTotal, lblConEmail, lblSinEmail, lblUltimo}) {
            etiqueta.setFont(fuente);
            add(etiqueta);
        }
    }

    public void actualizar(int total, int conEmail, int sinEmail, String ultimo) {
        lblTotal.setText("Total de contactos: " + total);
        lblConEmail.setText("Contactos con correo: " + conEmail);
        lblSinEmail.setText("Contactos sin correo: " + sinEmail);
        lblUltimo.setText("Último contacto agregado: " + (ultimo == null ? "Ninguno" : ultimo));
    }
}
