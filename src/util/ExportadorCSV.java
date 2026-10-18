package util;

import modelo.Contacto;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExportadorCSV {
    private ExportadorCSV() {}

    public static void exportar(List<Contacto> contactos, Path destino) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            writer.write("Nombre,Telefono,Email");
            writer.newLine();
            for (Contacto c : contactos) {
                writer.write(campoCSV(c.getNombre()) + ","
                        + campoCSV(c.getTelefono()) + ","
                        + campoCSV(c.getEmail()));
                writer.newLine();
            }
        }
    }

    private static String campoCSV(String valor) {
        if (valor == null) return "\"\"";
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
