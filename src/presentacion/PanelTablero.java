package presentacion;

import logica.Tablero;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelTablero extends JPanel {
    private Tablero tablero;
    private VentanaPrincipal ventanaPrincipal;
    private JButton[][] botones;
    private final int TAMAÑO_CELDA = 80;
    
    // Colores para animaciones
    private final Color COLOR_NUMERO = new Color(74, 144, 226);
    private final Color COLOR_NUMERO_CORRECTO = new Color(46, 204, 113);
    private final Color COLOR_VACIO = new Color(52, 73, 94);
    private final Color COLOR_HOVER = new Color(155, 89, 182);
    private final Color COLOR_PRESIONADO = new Color(231, 76, 60);
    private final Color COLOR_FONDO = new Color(44, 62, 80);
    
    // Colores para animaciones de movimiento
    private final Color COLOR_MOVIMIENTO_1 = new Color(255, 107, 107); // Rojo suave
    private final Color COLOR_MOVIMIENTO_2 = new Color(78, 205, 196);  // Turquesa
    private final Color COLOR_MOVIMIENTO_3 = new Color(255, 199, 95);  // Amarillo
    private final Color COLOR_MOVIMIENTO_4 = new Color(162, 155, 254); // Púrpura suave
    private final Color COLOR_MOVIMIENTO_5 = new Color(255, 159, 243); // Rosa
    private final Color COLOR_CELEBRACION = new Color(255, 215, 0);    // Dorado
    
    private final Font FUENTE_NUMEROS = new Font("Arial", Font.BOLD, 24);
    
    // Variables para animaciones
    private Timer animacionTimer;
    private int[] piezaMoviendose = null; // [fila, columna] de la pieza que se mueve
    private Color colorAnimacion = COLOR_MOVIMIENTO_1;
    private boolean mostrandoCelebracion = false;
    private Timer celebracionTimer;
    private int contadorCelebracion = 0;
    
    public PanelTablero(Tablero tablero, VentanaPrincipal ventanaPrincipal) {
        this.tablero = tablero;
        this.ventanaPrincipal = ventanaPrincipal;
        inicializarPanel();
        crearBotones();
        actualizarTablero();
    }
    
    private void inicializarPanel() {
        setLayout(new GridLayout(4, 4, 3, 3));
        setBackground(COLOR_FONDO);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setPreferredSize(new Dimension(350, 350));
    }
    
    private void crearBotones() {
        botones = new JButton[4][4];
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final int fila = i;
                final int columna = j;
                
                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(TAMAÑO_CELDA, TAMAÑO_CELDA));
                boton.setFont(FUENTE_NUMEROS);
                boton.setFocusPainted(false);
                boton.setBorderPainted(true);
                boton.setBorder(BorderFactory.createRaisedBevelBorder());
                
                boton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ventanaPrincipal.onPiezaClicada(fila, columna);
                    }
                });
                
                boton.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        if (tablero.getValor(fila, columna) != 0) {
                            boton.setBackground(COLOR_HOVER);
                            boton.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createRaisedBevelBorder(),
                                BorderFactory.createEmptyBorder(2, 2, 2, 2)
                            ));
                        }
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        actualizarBoton(fila, columna);
                    }
                    
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        if (tablero.getValor(fila, columna) != 0) {
                            boton.setBackground(COLOR_PRESIONADO);
                        }
                    }
                    
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        actualizarBoton(fila, columna);
                    }
                });
                
                botones[i][j] = boton;
                add(boton);
            }
        }
    }
    
    public void actualizarTablero() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                actualizarBoton(i, j);
            }
        }
        repaint();
    }
    
    private void actualizarBoton(int i, int j) {
        JButton boton = botones[i][j];
        int valor = tablero.getValor(i, j);
        
        if (valor == 0) {
            boton.setText("");
            boton.setBackground(COLOR_VACIO);
            boton.setEnabled(false);
            boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        } else {
            boton.setText(String.valueOf(valor));
            boton.setForeground(Color.WHITE);
            boton.setEnabled(true);
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (estaEnPosicionCorrecta(i, j, valor)) {
                boton.setBackground(COLOR_NUMERO_CORRECTO);
                boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createLineBorder(Color.GREEN, 2)
                ));
            } else {
                boton.setBackground(COLOR_NUMERO);
                boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)
                ));
            }
        }
    }
    
    private boolean estaEnPosicionCorrecta(int fila, int columna, int valor) {
        int valorEsperado = fila * 4 + columna + 1;
        return valor == valorEsperado;
    }
    
    public void mostrarAnimacionMovimiento(int[][] estadoAnterior, int[][] estadoNuevo, int pasoNumero) {
        // Encontrar qué pieza se movió
        int[] piezaMovida = encontrarPiezaMovida(estadoAnterior, estadoNuevo);
        
        if (piezaMovida != null) {
            piezaMoviendose = piezaMovida;
            
            // Alternar colores según el paso
            Color[] coloresMovimiento = {
                COLOR_MOVIMIENTO_1, COLOR_MOVIMIENTO_2, COLOR_MOVIMIENTO_3,
                COLOR_MOVIMIENTO_4, COLOR_MOVIMIENTO_5
            };
            colorAnimacion = coloresMovimiento[pasoNumero % coloresMovimiento.length];
            
            // Animar la pieza por un corto tiempo
            animacionTimer = new Timer(200, new ActionListener() {
                int pulsos = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pulsos < 3) {
                        actualizarBotonConAnimacion(piezaMoviendose[0], piezaMoviendose[1], 
                                                   pulsos % 2 == 0 ? colorAnimacion : COLOR_NUMERO);
                        pulsos++;
                    } else {
                        ((Timer)e.getSource()).stop();
                        piezaMoviendose = null;
                    }
                }
            });
            animacionTimer.start();
        }
    }
    
    public void mostrarCelebracion() {
        mostrandoCelebracion = true;
        contadorCelebracion = 0;
        
        celebracionTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contadorCelebracion < 10) {
                    // Alternar todos los botones con color dorado
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            if (tablero.getValor(i, j) != 0) {
                                Color color = contadorCelebracion % 2 == 0 ? 
                                            COLOR_CELEBRACION : COLOR_NUMERO_CORRECTO;
                                actualizarBotonConAnimacion(i, j, color);
                            }
                        }
                    }
                    contadorCelebracion++;
                } else {
                    ((Timer)e.getSource()).stop();
                    mostrandoCelebracion = false;
                    actualizarTablero(); // Restaurar colores normales
                }
            }
        });
        celebracionTimer.start();
    }
    
    private int[] encontrarPiezaMovida(int[][] estadoAnterior, int[][] estadoNuevo) {
        // Encontrar dónde estaba el espacio vacío antes
        int[] espacioAnterior = encontrarEspacio(estadoAnterior);
        int[] espacioNuevo = encontrarEspacio(estadoNuevo);
        
        if (espacioAnterior != null && espacioNuevo != null) {
            // La pieza que se movió ahora está donde estaba el espacio anterior
            return espacioAnterior;
        }
        
        return null;
    }
    
    private int[] encontrarEspacio(int[][] estado) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (estado[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
    
    private void actualizarBotonConAnimacion(int i, int j, Color color) {
        JButton boton = botones[i][j];
        int valor = tablero.getValor(i, j);
        
        if (valor != 0) {
            boton.setBackground(color);
            boton.setText(String.valueOf(valor));
            boton.setForeground(Color.WHITE);
            boton.setEnabled(true);
            boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
            ));
        }
    }
    
    public void mostrarRetroalimentacionMovimiento(int x, int y, boolean esValido) {
        JButton boton = botones[x][y];
        Color colorOriginal = boton.getBackground();
        Color colorFeedback = esValido ? 
            new Color(46, 204, 113) : // Verde para válido
            new Color(231, 76, 60);   // Rojo para inválido
        
        boton.setBackground(colorFeedback);
        
        Timer timer = new Timer(200, e -> {
            boton.setBackground(colorOriginal);
            ((Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(44, 62, 80),
            getWidth(), getHeight(), new Color(52, 73, 94)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 20, 20);
        
        g2d.setColor(new Color(149, 165, 166, 50));
        g2d.drawRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 20, 20);
        
        g2d.dispose();
    }
}