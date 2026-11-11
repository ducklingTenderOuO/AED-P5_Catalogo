public class Carta {
    private String nombre;
    private String atributo;
    private String nivel;
    private String tipo;
    private String atk;
    private String def;
    private String rareza;
    private String descripcion;
    private String id;

    public Carta(String nombre, String atributo, String nivel, String tipo, String atk, String def, String rareza, String descripcion, String id) {
        this.nombre = nombre;
        this.atributo = atributo;
        this.nivel = nivel;
        this.tipo = tipo;
        this.atk = atk;
        this.def = def;
        this.rareza = rareza;
        this.descripcion = descripcion;
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAtributo() {
        return atributo;
    }

    public String getNivel() {
        return nivel;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAtk() {
        return atk;
    }

    public String getDef() {
        return def;
    }

    public String getRareza() {
        return rareza;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getId() {
        return id;
    }

    public boolean tieneNivel() {
        return nivel != null && !nivel.equalsIgnoreCase("N/A") && !nivel.isEmpty();
    }

    public boolean tieneAtk() {
        return atk != null && !atk.equalsIgnoreCase("N/A") && !atk.isEmpty();
    }

    public int getAtkValor() {
        try {
            return Integer.parseInt(atk.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean tieneDef() {
        return def != null && !def.equalsIgnoreCase("N/A") && !def.isEmpty();
    }

    public int getDefValor() {
        try {
            return Integer.parseInt(def.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public String getCategoriaAtributo() {
        if (atributo != null && !atributo.equalsIgnoreCase("N/A") && !atributo.isEmpty()) {
            return atributo.trim();
        }
        if (tipo != null) {
            String t = tipo.toUpperCase();
            if (t.contains("SPELL")) {
                return "SPELL";
            }
            if (t.contains("TRAP")) {
                return "TRAP";
            }
        }
        return "N/A";
    }

    @Override
    public String toString() {
        return nombre + " | " + getCategoriaAtributo() + " | Level " + nivel + " | " + tipo + " | ATK " + atk + " / DEF " + def;
    }
}
