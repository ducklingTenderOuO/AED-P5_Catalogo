import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class GestorCartas {
    private ListaCircularDoble<Carta> todasLasCartas;

    public GestorCartas() {
        this.todasLasCartas = new ListaCircularDoble<>();
    }

    public void cargarDesdeCSV(String rutaArchivo) {
        todasLasCartas = new ListaCircularDoble<>();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea = br.readLine();
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                Carta carta = parsearLinea(linea);
                if (carta != null) {
                    todasLasCartas.insertaFin(carta);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Carta parsearLinea(String linea) {
        String[] partes = new String[7];
        int pos = 0;
        for (int i = 0; i < 7; i++) {
            int coma = linea.indexOf(',', pos);
            if (coma == -1) {
                return null;
            }
            partes[i] = linea.substring(pos, coma).trim();
            pos = coma + 1;
        }
        int ultimaComa = linea.lastIndexOf(',');
        if (ultimaComa < pos) {
            return null;
        }
        String descripcion = linea.substring(pos, ultimaComa).trim();
        String id = linea.substring(ultimaComa + 1).trim();
        return new Carta(
                partes[0], partes[1], partes[2], partes[3], partes[4], partes[5], partes[6],
                descripcion, id
        );
    }

    public ListaCircularDoble<Carta> getTodasLasCartas() {
        return todasLasCartas;
    }
}
