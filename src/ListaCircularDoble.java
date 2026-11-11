import java.util.Comparator;

public class ListaCircularDoble<T> {
    private NodoDoble<T> inicio;
    private NodoDoble<T> fin;

    public ListaCircularDoble() {
        this.inicio = null;
        this.fin = null;
    }

    public void insertaInicio(T dato) {
        NodoDoble<T> n = new NodoDoble<>(dato);
        if (inicio == null) {
            inicio = fin = n;
            n.setSig(inicio);
            n.setAnt(inicio);
        } else {
            n.setSig(inicio);
            inicio.setAnt(n);
            inicio = n;
            fin.setSig(inicio);
            n.setAnt(fin);
        }
    }

    public void insertaFin(T dato) {
        NodoDoble<T> n = new NodoDoble<>(dato);
        if (inicio == null) {
            inicio = fin = n;
            n.setSig(inicio);
            n.setAnt(inicio);
        } else {
            n.setSig(inicio);
            inicio.setAnt(n);
            fin.setSig(n);
            n.setAnt(fin);
            fin = n;
        }
    }

    public T eliminaInicio() {
        T datoEliminado = null;
        if (inicio == null) {
            System.out.println("Lista vacia");
        } else {
            datoEliminado = inicio.getInfo();
            if (inicio == fin) {
                inicio = fin = null;
            } else {
                fin.setSig(inicio.getSig());
                inicio = inicio.getSig();
                inicio.setAnt(fin);
            }
        }
        return datoEliminado;
    }

    public T eliminaFin() {
        T datoEliminado = null;
        if (inicio == null) {
            System.out.println("Lista vacia");
        } else {
            datoEliminado = fin.getInfo();
            if (inicio == fin) {
                inicio = fin = null;
            } else {
                NodoDoble<T> r = fin.getAnt();
                r.setSig(inicio);
                inicio.setAnt(r);
                fin = r;
            }
        }
        return datoEliminado;
    }

    public boolean estaVacia() {
        return inicio == null;
    }

    public NodoDoble<T> getInicio() {
        return inicio;
    }

    public int getTamano() {
        if (inicio == null) {
            return 0;
        }
        int contador = 0;
        NodoDoble<T> r = inicio;
        do {
            contador++;
            r = r.getSig();
        } while (r != inicio);
        return contador;
    }

    public int getIndiceDeNodo(NodoDoble<T> objetivo) {
        if (inicio == null || objetivo == null) {
            return 0;
        }
        int indice = 1;
        NodoDoble<T> r = inicio;
        do {
            if (r == objetivo) {
                return indice;
            }
            indice++;
            r = r.getSig();
        } while (r != inicio);
        return 1;
    }

    public void insertaPorCriterio(T dato, Comparator<T> comparador) {
        if (inicio == null || comparador.compare(dato, inicio.getInfo()) < 0) {
            insertaInicio(dato);
        } else if (comparador.compare(dato, fin.getInfo()) >= 0) {
            insertaFin(dato);
        } else {
            NodoDoble<T> actual = inicio.getSig();
            while (actual != inicio && comparador.compare(dato, actual.getInfo()) >= 0) {
                actual = actual.getSig();
            }
            NodoDoble<T> n = new NodoDoble<>(dato);
            NodoDoble<T> anterior = actual.getAnt();
            anterior.setSig(n);
            n.setAnt(anterior);
            n.setSig(actual);
            actual.setAnt(n);
        }
    }
}
