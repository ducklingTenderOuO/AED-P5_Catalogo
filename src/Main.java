public class Main {
    public static void main(String[] args) {
        GestorCartas gestor = new GestorCartas();
        gestor.cargarDesdeCSV("cartas_yugioh_completo.csv");
        System.out.println("Total de cartas cargadas del CSV: " + gestor.getTodasLasCartas().getTamano());
        NodoDoble<Carta> inicio = gestor.getTodasLasCartas().getInicio();
        if (inicio != null) {
            System.out.println("Primera carta (inicio): " + inicio.getInfo().getNombre());
            System.out.println("Ultima carta (fin / inicio.ant): " + inicio.getAnt().getInfo().getNombre());
        }
    }
}
