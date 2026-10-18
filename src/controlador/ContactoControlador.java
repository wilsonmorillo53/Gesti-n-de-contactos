package controlador;

/**
 * Punto de entrada del controlador para iniciar la aplicación.
 * Los eventos de la interfaz se conectan en VentanaPrincipal para mantener
 * este ejemplo sencillo y ejecutable sin dependencias externas.
 */
import javax.swing.SwingUtilities;
import vista.VentanaPrincipal;

public class ContactoControlador {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManagerCompat.configurar();
            } catch (Exception ignored) {
                // Se conserva el estilo predeterminado si no está disponible.
            }
            new VentanaPrincipal().setVisible(true);
        });
    }

    private static class UIManagerCompat {
        static void configurar() throws Exception {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        }
    }
}
