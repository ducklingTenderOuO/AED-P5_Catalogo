import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class GestorCartas {
    private ListaCircularDoble<Carta> todasLasCartas;
    private ListaCircularDoble<String> nivelesDisponibles;
    private ListaCircularDoble<String> atributosDisponibles;

    public GestorCartas() {
        this.todasLasCartas = new ListaCircularDoble<>();
        this.nivelesDisponibles = new ListaCircularDoble<>();
        this.atributosDisponibles = new ListaCircularDoble<>();
    }

    public void cargarDesdeCSV(String rutaArchivo) {
        todasLasCartas = new ListaCircularDoble<>();
        nivelesDisponibles = new ListaCircularDoble<>();
        atributosDisponibles = new ListaCircularDoble<>();
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
                    registrarNivel(carta);
                    registrarAtributo(carta);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registrarNivel(Carta carta) {
        if (!carta.tieneNivel()) {
            return;
        }
        String niv = carta.getNivel().trim();
        if (!contieneTexto(nivelesDisponibles, niv)) {
            nivelesDisponibles.insertaPorCriterio(niv, (a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
        }
    }

    private void registrarAtributo(Carta carta) {
        String atrib = carta.getCategoriaAtributo();
        if (!atrib.equalsIgnoreCase("N/A") && !contieneTexto(atributosDisponibles, atrib)) {
            atributosDisponibles.insertaPorCriterio(atrib, String::compareToIgnoreCase);
        }
    }

    private boolean contieneTexto(ListaCircularDoble<String> lista, String valor) {
        if (lista.estaVacia()) {
            return false;
        }
        NodoDoble<String> r = lista.getInicio();
        do {
            if (r.getInfo().equalsIgnoreCase(valor)) {
                return true;
            }
            r = r.getSig();
        } while (r != lista.getInicio());
        return false;
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
                partes[0],
                partes[1],
                partes[2],
                partes[3],
                partes[4],
                partes[5],
                partes[6],
                descripcion,
                id
        );
    }

    public ListaCircularDoble<Carta> getTodasLasCartas() {
        return todasLasCartas;
    }

    public ListaCircularDoble<String> getNivelesDisponibles() {
        return nivelesDisponibles;
    }

    public ListaCircularDoble<String> getAtributosDisponibles() {
        return atributosDisponibles;
    }

    public ListaCircularDoble<Carta> obtenerListaPorNivel(String nivelBuscado) {
        ListaCircularDoble<Carta> resultado = new ListaCircularDoble<>();
        if (todasLasCartas.estaVacia() || nivelBuscado == null) {
            return resultado;
        }
        NodoDoble<Carta> r = todasLasCartas.getInicio();
        do {
            Carta c = r.getInfo();
            if (c.getNivel().equalsIgnoreCase(nivelBuscado.trim())) {
                resultado.insertaFin(c);
            }
            r = r.getSig();
        } while (r != todasLasCartas.getInicio());
        return resultado;
    }

    public ListaCircularDoble<Carta> obtenerListaPorAtributo(String atributoBuscado) {
        ListaCircularDoble<Carta> resultado = new ListaCircularDoble<>();
        if (todasLasCartas.estaVacia() || atributoBuscado == null) {
            return resultado;
        }
        NodoDoble<Carta> r = todasLasCartas.getInicio();
        do {
            Carta c = r.getInfo();
            if (c.getCategoriaAtributo().equalsIgnoreCase(atributoBuscado.trim())) {
                resultado.insertaFin(c);
            }
            r = r.getSig();
        } while (r != todasLasCartas.getInicio());
        return resultado;
    }

    public ListaCircularDoble<Carta> obtenerListaPorAtaque(boolean menorAMayor) {
        ListaCircularDoble<Carta> resultado = new ListaCircularDoble<>();
        if (todasLasCartas.estaVacia()) {
            return resultado;
        }
        Comparator<Carta> comparador = (c1, c2) -> {
            int cmp = Integer.compare(c1.getAtkValor(), c2.getAtkValor());
            if (!menorAMayor) {
                cmp = -cmp;
            }
            if (cmp == 0) {
                return c1.getNombre().compareToIgnoreCase(c2.getNombre());
            }
            return cmp;
        };
        NodoDoble<Carta> r = todasLasCartas.getInicio();
        do {
            Carta c = r.getInfo();
            if (c.tieneAtk()) {
                resultado.insertaPorCriterio(c, comparador);
            }
            r = r.getSig();
        } while (r != todasLasCartas.getInicio());
        return resultado;
    }

    public ListaCircularDoble<Carta> obtenerListaPorDefensa(boolean menorAMayor) {
        ListaCircularDoble<Carta> resultado = new ListaCircularDoble<>();
        if (todasLasCartas.estaVacia()) {
            return resultado;
        }
        Comparator<Carta> comparador = (c1, c2) -> {
            int cmp = Integer.compare(c1.getDefValor(), c2.getDefValor());
            if (!menorAMayor) {
                cmp = -cmp;
            }
            if (cmp == 0) {
                return c1.getNombre().compareToIgnoreCase(c2.getNombre());
            }
            return cmp;
        };
        NodoDoble<Carta> r = todasLasCartas.getInicio();
        do {
            Carta c = r.getInfo();
            if (c.tieneDef()) {
                resultado.insertaPorCriterio(c, comparador);
            }
            r = r.getSig();
        } while (r != todasLasCartas.getInicio());
        return resultado;
    }
}
