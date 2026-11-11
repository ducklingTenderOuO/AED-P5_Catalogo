public class ListaCircularDoble<T> {
    private NodoDoble<T> inicio;
    private NodoDoble<T> fin;

    public ListaCircularDoble() {
        this.inicio = null;
        this.fin = null;
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

    public void insertaInicio(T dato) {
        NodoDoble<T> n = new NodoDoble<>(dato);
        if (inicio == null) {
            inicio = n;
            fin = n;
            n.setSig(n);
            n.setAnt(n);
        } else {
            n.setSig(inicio);
            n.setAnt(fin);
            inicio.setAnt(n);
            fin.setSig(n);
            inicio = n;
        }
    }

    public void insertaFin(T dato) {
        NodoDoble<T> n = new NodoDoble<>(dato);
        if (inicio == null) {
            inicio = n;
            fin = n;
            n.setSig(n);
            n.setAnt(n);
        } else {
            n.setSig(inicio);
            n.setAnt(fin);
            fin.setSig(n);
            inicio.setAnt(n);
            fin = n;
        }
    }

    public T eliminaInicio() {
        if (inicio == null) {
            return null;
        }
        T datoEliminado = inicio.getInfo();
        if (inicio == fin) {
            inicio = null;
            fin = null;
        } else {
            inicio = inicio.getSig();
            inicio.setAnt(fin);
            fin.setSig(inicio);
        }
        return datoEliminado;
    }

    public T eliminaFin() {
        if (inicio == null) {
            return null;
        }
        T datoEliminado = fin.getInfo();
        if (inicio == fin) {
            inicio = null;
            fin = null;
        } else {
            fin = fin.getAnt();
            fin.setSig(inicio);
            inicio.setAnt(fin);
        }
        return datoEliminado;
    }
}
