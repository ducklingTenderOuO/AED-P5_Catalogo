public class Main {
    public static void main(String[] args) {
        ListaCircularDoble<Carta> lista = new ListaCircularDoble<>();
        Carta c1 = new Carta("Dark Magician", "Normal Monster", "DARK", "7", "Spellcaster", "2500", "2100", "The ultimate wizard in terms of attack and defense.", "46986414");
        Carta c2 = new Carta("Blue-Eyes White Dragon", "Normal Monster", "LIGHT", "8", "Dragon", "3000", "2500", "This legendary dragon is a powerful engine of destruction.", "89631139");
        lista.insertaFin(c1);
        lista.insertaFin(c2);
        System.out.println("Lista inicial de cartas creada. Total: " + lista.getTamano());
        NodoDoble<Carta> r = lista.getInicio();
        System.out.println("Carta actual: " + r.getInfo().getNombre());
        System.out.println("Siguiente: " + r.getSig().getInfo().getNombre());
        System.out.println("Anterior: " + r.getAnt().getInfo().getNombre());
    }
}
