import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public class VentanaPrincipal extends JFrame {
    private GestorCartas gestor;
    private ReproductorAudio reproductor;

    private ListaCircularDoble<Carta> listaActiva;
    private NodoDoble<Carta> nodoActual;
    private String criterioActivo = "ALL";

    private JButton btnShowAll;
    private JButton btnShowByLevel;
    private JComboBox<String> cmbLevel;
    private JButton btnShowByAttribute;
    private JComboBox<String> cmbAttribute;
    private JButton btnShowByAttack;
    private JCheckBox chkAttackLowToHigh;
    private JButton btnShowByDefense;
    private JCheckBox chkDefenseLowToHigh;
    private JButton btnSpeaker;

    private JPanel panelCartas;
    private JLabel lblModoActivo;
    private JLabel lblDatoRelevante;
    private JLabel lblContador;
    private JLabel lblImagenAnterior;
    private JLabel lblImagenActual;
    private JLabel lblImagenSiguiente;
    private JLabel lblNombreAnterior;
    private JLabel lblNombreSiguiente;
    private JButton btnAnterior;
    private JButton btnSiguiente;

    private JLabel lblNombreCarta;
    private JLabel lblAtributoCarta;
    private JLabel lblNivelCarta;
    private JLabel lblTipoCarta;
    private JLabel lblRarezaCarta;
    private JLabel lblAtkCarta;
    private JLabel lblDefCarta;
    private JTextArea txtDescripcionCarta;

    private static final int ANCHO_MINI = 148;
    private static final int ALTO_MINI = 216;
    private static final int ANCHO_ACTUAL = 310;
    private static final int ALTO_ACTUAL = 452;

    public VentanaPrincipal() {
        gestor = new GestorCartas();
        gestor.cargarDesdeCSV("cartas_yugioh_completo.csv");
        reproductor = new ReproductorAudio("yugitext meloboom.mp3");
        reproductor.reproducir();

        setTitle("Yu-Gi-Oh! 25th Anniversary Tin: Dueling Heroes Mega Pack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reproductor.detener();
            }
        });

        inicializarComponentes();
        configurarEventosNavegacion();
        configurarEventosFiltros();
        configurarAtajosTeclado();

        mostrarTodasLasCartas();
    }

    private void configurarAtajosTeclado() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "siguiente");
        am.put("siguiente", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avanzarCarta();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "anterior");
        am.put("anterior", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                retrocederCarta();
            }
        });
    }

    private void configurarEventosNavegacion() {
        btnAnterior.addActionListener(e -> retrocederCarta());
        lblImagenAnterior.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                retrocederCarta();
            }
        });

        btnSiguiente.addActionListener(e -> avanzarCarta());
        lblImagenSiguiente.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                avanzarCarta();
            }
        });

        lblImagenActual.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarZoomCartaActual();
            }
        });
    }

    private void mostrarZoomCartaActual() {
        if (nodoActual == null) {
            return;
        }
        Carta c = nodoActual.getInfo();
        JDialog dialogo = new JDialog(this, c.getNombre(), true);
        dialogo.setLayout(new BorderLayout(16, 16));
        dialogo.getContentPane().setBackground(new Color(18, 20, 32));

        JLabel lblZoom = new JLabel(CargadorImagenes.escalarImagen(CargadorImagenes.obtenerPlaceholder(), 421, 614), SwingConstants.CENTER);
        lblZoom.setBorder(new EmptyBorder(16, 20, 10, 20));
        CargadorImagenes.obtenerImagen(c, img -> lblZoom.setIcon(CargadorImagenes.escalarImagen(img, 421, 614)));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCerrar.setBackground(new Color(240, 201, 84));
        btnCerrar.setForeground(new Color(18, 20, 32));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(ev -> dialogo.dispose());

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSur.setOpaque(false);
        panelSur.setBorder(new EmptyBorder(0, 0, 12, 0));
        panelSur.add(btnCerrar);

        dialogo.add(lblZoom, BorderLayout.CENTER);
        dialogo.add(panelSur, BorderLayout.SOUTH);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void configurarEventosFiltros() {
        btnShowAll.addActionListener(e -> mostrarTodasLasCartas());
        btnShowByLevel.addActionListener(e -> mostrarPorNivel());
        btnShowByAttribute.addActionListener(e -> mostrarPorAtributo());
        btnShowByAttack.addActionListener(e -> mostrarPorAtaque());
        btnShowByDefense.addActionListener(e -> mostrarPorDefensa());

        cmbLevel.addActionListener(e -> {
            if ("LEVEL".equals(criterioActivo)) {
                mostrarPorNivel();
            }
        });

        cmbAttribute.addActionListener(e -> {
            if ("ATTRIBUTE".equals(criterioActivo)) {
                mostrarPorAtributo();
            }
        });

        chkAttackLowToHigh.addActionListener(e -> {
            if ("ATK".equals(criterioActivo)) {
                mostrarPorAtaque();
            }
        });

        chkDefenseLowToHigh.addActionListener(e -> {
            if ("DEF".equals(criterioActivo)) {
                mostrarPorDefensa();
            }
        });

        btnSpeaker.addActionListener(e -> {
            boolean activo = reproductor.alternarAudio();
            btnSpeaker.setIcon(crearIconoBocina(activo));
        });
    }

    private void mostrarTodasLasCartas() {
        criterioActivo = "ALL";
        listaActiva = gestor.getTodasLasCartas();
        nodoActual = listaActiva.getInicio();
        lblModoActivo.setText("Todas las cartas");
        resaltarBotonActivo(btnShowAll);
        actualizarVista();
    }

    private void mostrarPorNivel() {
        Object sel = cmbLevel.getSelectedItem();
        if (sel == null) {
            return;
        }
        String nivel = sel.toString();
        criterioActivo = "LEVEL";
        listaActiva = gestor.obtenerListaPorNivel(nivel);
        nodoActual = listaActiva.getInicio();
        lblModoActivo.setText("Cartas por Nivel: " + nivel);
        resaltarBotonActivo(btnShowByLevel);
        actualizarVista();
    }

    private void mostrarPorAtributo() {
        Object sel = cmbAttribute.getSelectedItem();
        if (sel == null) {
            return;
        }
        String atributo = sel.toString();
        criterioActivo = "ATTRIBUTE";
        listaActiva = gestor.obtenerListaPorAtributo(atributo);
        nodoActual = listaActiva.getInicio();
        lblModoActivo.setText("Cartas por Atributo: " + atributo);
        resaltarBotonActivo(btnShowByAttribute);
        actualizarVista();
    }

    private void mostrarPorAtaque() {
        boolean lowToHigh = chkAttackLowToHigh.isSelected();
        criterioActivo = "ATK";
        listaActiva = gestor.obtenerListaPorAtaque(lowToHigh);
        nodoActual = listaActiva.getInicio();
        lblModoActivo.setText("Cartas por Ataque (" + (lowToHigh ? "Menor a mayor" : "Mayor a menor") + ")");
        resaltarBotonActivo(btnShowByAttack);
        actualizarVista();
    }

    private void mostrarPorDefensa() {
        boolean lowToHigh = chkDefenseLowToHigh.isSelected();
        criterioActivo = "DEF";
        listaActiva = gestor.obtenerListaPorDefensa(lowToHigh);
        nodoActual = listaActiva.getInicio();
        lblModoActivo.setText("Cartas por Defensa (" + (lowToHigh ? "Menor a mayor" : "Mayor a menor") + ")");
        resaltarBotonActivo(btnShowByDefense);
        actualizarVista();
    }

    private void resaltarBotonActivo(JButton activo) {
        JButton[] botones = {btnShowAll, btnShowByLevel, btnShowByAttribute, btnShowByAttack, btnShowByDefense};
        for (JButton b : botones) {
            if (b == activo) {
                b.setBackground(new Color(240, 201, 84));
            } else {
                b.setBackground(new Color(247, 248, 251));
            }
        }
    }

    private void avanzarCarta() {
        if (nodoActual != null) {
            nodoActual = nodoActual.getSig();
            actualizarVista();
        }
    }

    private void retrocederCarta() {
        if (nodoActual != null) {
            nodoActual = nodoActual.getAnt();
            actualizarVista();
        }
    }

    private void inicializarComponentes() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(18, 20, 32));

        panelCartas = crearPanelVisualizador();
        contenedor.add(panelCartas, BorderLayout.CENTER);

        JPanel panelControles = crearPanelControles();
        contenedor.add(panelControles, BorderLayout.SOUTH);

        setContentPane(contenedor);
    }

    private JPanel crearPanelVisualizador() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setBackground(new Color(18, 20, 32));
        contenedor.setBorder(new EmptyBorder(10, 24, 8, 24));

        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setOpaque(false);

        lblModoActivo = new JLabel("Todas las cartas");
        lblModoActivo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblModoActivo.setForeground(new Color(240, 201, 84));

        lblContador = new JLabel("(0/0)", SwingConstants.CENTER);
        lblContador.setOpaque(true);
        lblContador.setBackground(new Color(37, 41, 66));
        lblContador.setForeground(Color.WHITE);
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblContador.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55), 2, true),
                new EmptyBorder(4, 16, 4, 16)
        ));

        barraSuperior.add(lblModoActivo, BorderLayout.WEST);
        barraSuperior.add(lblContador, BorderLayout.EAST);

        JPanel areaCentral = new JPanel(new GridBagLayout());
        areaCentral.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 16, 0, 16);
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        areaCentral.add(crearControlLateral(true), gbc);

        gbc.gridx = 1;
        areaCentral.add(crearControlCentral(), gbc);

        gbc.gridx = 2;
        areaCentral.add(crearControlLateral(false), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 24, 0, 0);
        gbc.fill = GridBagConstraints.VERTICAL;
        areaCentral.add(crearPanelFichaCarta(), gbc);

        contenedor.add(barraSuperior, BorderLayout.NORTH);
        contenedor.add(areaCentral, BorderLayout.CENTER);

        return contenedor;
    }

    private JPanel crearControlLateral(boolean esAnterior) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(175, 340));

        JLabel titulo = new JLabel(esAnterior ? "Carta anterior" : "Carta siguiente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(new Color(176, 182, 204));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblImagen = new JLabel(CargadorImagenes.escalarImagen(CargadorImagenes.obtenerPlaceholder(), ANCHO_MINI, ALTO_MINI));
        lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(90, 96, 125), 2));

        JLabel lblNombre = new JLabel("-", SwingConstants.CENTER);
        lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNombre.setForeground(new Color(216, 220, 232));
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setPreferredSize(new Dimension(165, 34));
        lblNombre.setMaximumSize(new Dimension(165, 34));

        JButton btnNav = new JButton(esAnterior ? "Anterior" : "Siguiente");
        btnNav.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension dimBtn = new Dimension(140, 38);
        btnNav.setPreferredSize(dimBtn);
        btnNav.setMaximumSize(dimBtn);
        btnNav.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnNav.setForeground(new Color(240, 201, 84));
        btnNav.setBackground(new Color(41, 46, 76));
        btnNav.setFocusPainted(false);
        btnNav.setBorder(BorderFactory.createLineBorder(new Color(240, 201, 84), 1, true));
        btnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (esAnterior) {
            lblImagenAnterior = lblImagen;
            lblNombreAnterior = lblNombre;
            btnAnterior = btnNav;
        } else {
            lblImagenSiguiente = lblImagen;
            lblNombreSiguiente = lblNombre;
            btnSiguiente = btnNav;
        }

        box.add(titulo);
        box.add(Box.createVerticalStrut(8));
        box.add(lblImagen);
        box.add(Box.createVerticalStrut(8));
        box.add(lblNombre);
        box.add(Box.createVerticalStrut(8));
        box.add(btnNav);

        return box;
    }

    private JPanel crearControlCentral() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        lblDatoRelevante = new JLabel("Carta actual", SwingConstants.CENTER);
        lblDatoRelevante.setOpaque(true);
        lblDatoRelevante.setBackground(new Color(37, 41, 66));
        lblDatoRelevante.setForeground(new Color(240, 201, 84));
        lblDatoRelevante.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDatoRelevante.setBorder(new EmptyBorder(4, 14, 4, 14));
        lblDatoRelevante.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblImagenActual = new JLabel(CargadorImagenes.escalarImagen(CargadorImagenes.obtenerPlaceholder(), ANCHO_ACTUAL, ALTO_ACTUAL));
        lblImagenActual.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImagenActual.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblImagenActual.setToolTipText("Hacer clic para ver en tamano completo");
        lblImagenActual.setBorder(BorderFactory.createLineBorder(new Color(240, 201, 84), 3));

        box.add(lblDatoRelevante);
        box.add(Box.createVerticalStrut(6));
        box.add(lblImagenActual);
        return box;
    }

    private JPanel crearPanelFichaCarta() {
        JPanel ficha = new JPanel();
        ficha.setLayout(new BoxLayout(ficha, BoxLayout.Y_AXIS));
        ficha.setPreferredSize(new Dimension(470, 460));
        ficha.setBackground(new Color(248, 249, 252));
        ficha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 204, 216), 2, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel filaNombre = new JPanel(new BorderLayout(0, 4));
        filaNombre.setOpaque(false);

        lblNombreCarta = new JLabel("-");
        lblNombreCarta.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblNombreCarta.setForeground(new Color(18, 20, 29));

        lblRarezaCarta = new JLabel("-");
        lblRarezaCarta.setOpaque(true);
        lblRarezaCarta.setBackground(new Color(37, 41, 66));
        lblRarezaCarta.setForeground(new Color(240, 201, 84));
        lblRarezaCarta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRarezaCarta.setBorder(new EmptyBorder(3, 8, 3, 8));

        filaNombre.add(lblNombreCarta, BorderLayout.NORTH);
        filaNombre.add(lblRarezaCarta, BorderLayout.SOUTH);

        JPanel filaAtribNivel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filaAtribNivel.setOpaque(false);

        lblAtributoCarta = new JLabel("Atributo: -");
        lblAtributoCarta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAtributoCarta.setForeground(new Color(31, 78, 95));

        lblNivelCarta = new JLabel("Nivel: -");
        lblNivelCarta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNivelCarta.setForeground(new Color(184, 74, 20));

        filaAtribNivel.add(lblAtributoCarta);
        filaAtribNivel.add(lblNivelCarta);

        JPanel filaTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filaTipo.setOpaque(false);

        lblTipoCarta = new JLabel("[ - ]");
        lblTipoCarta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTipoCarta.setForeground(new Color(35, 39, 51));
        filaTipo.add(lblTipoCarta);

        JPanel filaAtkDef = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filaAtkDef.setOpaque(false);

        lblAtkCarta = new JLabel("ATK -");
        lblAtkCarta.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAtkCarta.setForeground(new Color(143, 29, 29));

        lblDefCarta = new JLabel("DEF -");
        lblDefCarta.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDefCarta.setForeground(new Color(27, 73, 128));

        filaAtkDef.add(lblAtkCarta);
        filaAtkDef.add(lblDefCarta);

        txtDescripcionCarta = new JTextArea(8, 30);
        txtDescripcionCarta.setEditable(false);
        txtDescripcionCarta.setLineWrap(true);
        txtDescripcionCarta.setWrapStyleWord(true);
        txtDescripcionCarta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescripcionCarta.setForeground(new Color(36, 40, 52));
        txtDescripcionCarta.setBackground(new Color(248, 249, 252));

        JScrollPane scrollDesc = new JScrollPane(txtDescripcionCarta);
        scrollDesc.setBorder(BorderFactory.createEmptyBorder());

        ficha.add(filaNombre);
        ficha.add(Box.createVerticalStrut(8));
        ficha.add(new JSeparator());
        ficha.add(Box.createVerticalStrut(8));
        ficha.add(filaAtribNivel);
        ficha.add(Box.createVerticalStrut(6));
        ficha.add(filaTipo);
        ficha.add(Box.createVerticalStrut(6));
        ficha.add(filaAtkDef);
        ficha.add(Box.createVerticalStrut(8));
        ficha.add(new JSeparator());
        ficha.add(Box.createVerticalStrut(8));
        ficha.add(scrollDesc);

        return ficha;
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(new Color(236, 238, 244));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(24, 26, 36)),
                new EmptyBorder(10, 24, 10, 24)
        ));

        JPanel gridOpciones = new JPanel(new GridBagLayout());
        gridOpciones.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.anchor = GridBagConstraints.WEST;

        btnShowAll = crearBotonMenu("Mostrar todas las cartas");

        btnShowByLevel = crearBotonMenu("Mostrar por nivel");
        cmbLevel = new JComboBox<>();
        cmbLevel.setPreferredSize(new Dimension(150, 36));
        cmbLevel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cmbLevel.setBackground(Color.WHITE);
        llenarComboDesdeLista(cmbLevel, gestor.getNivelesDisponibles());
        JPanel cajaComboNivel = crearCajaCombo("Elegir nivel", cmbLevel);

        btnShowByAttribute = crearBotonMenu("Mostrar por atributo");
        cmbAttribute = new JComboBox<>();
        cmbAttribute.setPreferredSize(new Dimension(150, 36));
        cmbAttribute.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cmbAttribute.setBackground(Color.WHITE);
        llenarComboDesdeLista(cmbAttribute, gestor.getAtributosDisponibles());
        JPanel cajaComboAtributo = crearCajaCombo("Elegir atributo", cmbAttribute);

        btnShowByAttack = crearBotonMenu("Mostrar por puntos de ataque");
        chkAttackLowToHigh = crearCheckBox("Menor a mayor");

        btnShowByDefense = crearBotonMenu("Mostrar por puntos de defensa");
        chkDefenseLowToHigh = crearCheckBox("Menor a mayor");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gridOpciones.add(btnShowAll, gbc);

        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 1;
        gbc.gridy = 0;
        gridOpciones.add(btnShowByLevel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gridOpciones.add(cajaComboNivel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gridOpciones.add(btnShowByAttribute, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gridOpciones.add(cajaComboAtributo, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gridOpciones.add(btnShowByAttack, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gridOpciones.add(chkAttackLowToHigh, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gridOpciones.add(btnShowByDefense, gbc);

        gbc.gridx = 4;
        gbc.gridy = 1;
        gridOpciones.add(chkDefenseLowToHigh, gbc);

        btnSpeaker = new JButton(crearIconoBocina(true));
        btnSpeaker.setPreferredSize(new Dimension(52, 52));
        btnSpeaker.setBackground(new Color(221, 225, 236));
        btnSpeaker.setBorder(BorderFactory.createLineBorder(new Color(24, 26, 36), 2, true));
        btnSpeaker.setFocusPainted(false);
        btnSpeaker.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel panelSpeaker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 20));
        panelSpeaker.setOpaque(false);
        panelSpeaker.add(btnSpeaker);

        panel.add(gridOpciones, BorderLayout.CENTER);
        panel.add(panelSpeaker, BorderLayout.EAST);

        return panel;
    }

    private void actualizarVista() {
        if (listaActiva == null || listaActiva.estaVacia() || nodoActual == null) {
            lblContador.setText("(0/0)");
            lblDatoRelevante.setText("Sin cartas");
            lblImagenActual.setIcon(CargadorImagenes.escalarImagen(CargadorImagenes.obtenerPlaceholder(), ANCHO_ACTUAL, ALTO_ACTUAL));
            lblImagenAnterior.setIcon(CargadorImagenes.escalarImagen(CargadorImagenes.obtenerPlaceholder(), ANCHO_MINI, ALTO_MINI));
            lblImagenSiguiente.setIcon(CargadorImagenes.escalarImagen(CargadorImagenes.obtenerPlaceholder(), ANCHO_MINI, ALTO_MINI));
            lblNombreAnterior.setText("-");
            lblNombreSiguiente.setText("-");
            lblNombreCarta.setText("Sin cartas encontradas");
            lblRarezaCarta.setText("-");
            lblAtributoCarta.setText("-");
            lblNivelCarta.setText("-");
            lblTipoCarta.setText("-");
            lblAtkCarta.setText("ATK -");
            lblDefCarta.setText("DEF -");
            txtDescripcionCarta.setText("");
            return;
        }

        int total = listaActiva.getTamano();
        int indice = listaActiva.getIndiceDeNodo(nodoActual);
        lblContador.setText("(" + indice + "/" + total + ")");

        Carta cartaActual = nodoActual.getInfo();
        Carta cartaAnt = nodoActual.getAnt().getInfo();
        Carta cartaSig = nodoActual.getSig().getInfo();

        lblDatoRelevante.setText(construirTextoDestacado(cartaActual));

        lblNombreAnterior.setText("<html><center>" + cartaAnt.getNombre() + "</center></html>");
        lblNombreSiguiente.setText("<html><center>" + cartaSig.getNombre() + "</center></html>");

        lblNombreCarta.setText(cartaActual.getNombre());
        lblRarezaCarta.setText(cartaActual.getRareza() + " | ID: " + cartaActual.getId());
        lblAtributoCarta.setText("Atributo: " + cartaActual.getCategoriaAtributo());
        lblNivelCarta.setText(cartaActual.tieneNivel() ? "Nivel " + cartaActual.getNivel() : "Nivel N/A");
        lblTipoCarta.setText(cartaActual.getTipo());
        lblAtkCarta.setText("ATK " + cartaActual.getAtk());
        lblDefCarta.setText("DEF " + cartaActual.getDef());
        txtDescripcionCarta.setText(cartaActual.getDescripcion());
        txtDescripcionCarta.setCaretPosition(0);

        final String idActualEsperado = cartaActual.getId();
        CargadorImagenes.obtenerImagen(cartaActual, img -> {
            if (nodoActual != null && nodoActual.getInfo().getId().equals(idActualEsperado)) {
                lblImagenActual.setIcon(CargadorImagenes.escalarImagen(img, ANCHO_ACTUAL, ALTO_ACTUAL));
            }
        });

        final String idAntEsperado = cartaAnt.getId();
        CargadorImagenes.obtenerImagen(cartaAnt, img -> {
            if (nodoActual != null && nodoActual.getAnt().getInfo().getId().equals(idAntEsperado)) {
                lblImagenAnterior.setIcon(CargadorImagenes.escalarImagen(img, ANCHO_MINI, ALTO_MINI));
            }
        });

        final String idSigEsperado = cartaSig.getId();
        CargadorImagenes.obtenerImagen(cartaSig, img -> {
            if (nodoActual != null && nodoActual.getSig().getInfo().getId().equals(idSigEsperado)) {
                lblImagenSiguiente.setIcon(CargadorImagenes.escalarImagen(img, ANCHO_MINI, ALTO_MINI));
            }
        });
    }

    private String construirTextoDestacado(Carta c) {
        return switch (criterioActivo) {
            case "LEVEL" -> c.getNombre() + "  -  Nivel " + c.getNivel();
            case "ATTRIBUTE" -> c.getNombre() + "  -  Atributo: " + c.getCategoriaAtributo();
            case "ATK" -> c.getNombre() + "  -  ATK " + c.getAtk();
            case "DEF" -> c.getNombre() + "  -  DEF " + c.getDef();
            default -> c.getNombre() + "  -  " + c.getCategoriaAtributo() + (c.tieneNivel() ? " | Nivel " + c.getNivel() : "");
        };
    }

    private ImageIcon crearIconoBocina(boolean activa) {
        int s = 28;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(activa ? new Color(24, 26, 36) : new Color(168, 32, 32));

        Path2D bocina = new Path2D.Double();
        bocina.moveTo(4, 10);
        bocina.lineTo(9, 10);
        bocina.lineTo(15, 5);
        bocina.lineTo(15, 23);
        bocina.lineTo(9, 18);
        bocina.lineTo(4, 18);
        bocina.closePath();
        g2.fill(bocina);

        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (activa) {
            g2.drawArc(13, 8, 7, 12, -60, 120);
            g2.drawArc(15, 5, 10, 18, -60, 120);
        } else {
            g2.drawLine(18, 10, 24, 18);
            g2.drawLine(24, 10, 18, 18);
        }
        g2.dispose();
        return new ImageIcon(img);
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        Dimension dim = new Dimension(330, 52);
        btn.setPreferredSize(dim);
        btn.setMinimumSize(dim);
        btn.setMaximumSize(dim);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setForeground(new Color(18, 19, 26));
        btn.setBackground(new Color(247, 248, 251));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(18, 19, 26), 2, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel crearCajaCombo(String etiqueta, JComboBox<String> combo) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(44, 48, 64));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        combo.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lbl);
        box.add(Box.createVerticalStrut(2));
        box.add(combo);
        return box;
    }

    private JCheckBox crearCheckBox(String texto) {
        JCheckBox chk = new JCheckBox(texto);
        chk.setOpaque(false);
        chk.setFont(new Font("Segoe UI", Font.BOLD, 20));
        chk.setForeground(new Color(29, 32, 43));
        chk.setFocusPainted(false);
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return chk;
    }

    private void llenarComboDesdeLista(JComboBox<String> combo, ListaCircularDoble<String> lista) {
        if (lista == null || lista.estaVacia()) {
            return;
        }
        NodoDoble<String> r = lista.getInicio();
        do {
            combo.addItem(r.getInfo());
            r = r.getSig();
        } while (r != lista.getInicio());
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }
}
