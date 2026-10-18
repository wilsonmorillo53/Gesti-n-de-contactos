package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repositorio en memoria para mantener los contactos durante la ejecución.
 * No utiliza una base de datos externa.
 */
public class ContactoDAO {
    private final List<Contacto> contactos = new ArrayList<>();

    public void agregar(Contacto contacto) {
        contactos.add(contacto);
    }

    public boolean eliminar(Contacto contacto) {
        return contactos.remove(contacto);
    }

    /** Elimina el contacto ubicado en el índice indicado. */
    public boolean eliminar(int indice) {
        if (indice < 0 || indice >= contactos.size()) {
            return false;
        }
        contactos.remove(indice);
        return true;
    }

    /** Reemplaza el contacto ubicado en el índice indicado. */
    public boolean modificar(int indice, Contacto contactoActualizado) {
        if (indice < 0 || indice >= contactos.size() || contactoActualizado == null) {
            return false;
        }
        contactos.set(indice, contactoActualizado);
        return true;
    }

    public List<Contacto> listar() {
        return Collections.unmodifiableList(new ArrayList<>(contactos));
    }

    public int total() {
        return contactos.size();
    }
}
