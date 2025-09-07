package presentacion;

import logica.Tablero;
import logica.HeuristicaIA;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private Tablero tablero;
    private PanelTablero panelTablero;
    private PanelControles panelControles;
    private HeuristicaIA ia;
    private Timer timerResolucion;
    private List<int[][]> solucionPasos;
    private int pasoActual;
    
    public VentanaPrincipal() {
        inicializarComponentes();
        configurarVentana();
        configurarEventos();
    }
    
    private void inicializarComponentes() {
        tablero = new Tablero();
        ia = new HeuristicaIA(tablero);
        
        panelTablero = new PanelTablero(tablero, this);
        panelControles = new PanelControles();
        
        timerResolucion = new Timer(1000, new ActionListener() { // 1 segundo por movimiento
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pasoActual < solucionPasos.size()) {
                    // Mostrar animación de transición
                    panelTablero.mostrarAnimacionMovimiento(
                        tablero.getMatriz(), 
                        solucionPasos.get(pasoActual),
                        pasoActual
                    );
                    
                    // Actualizar el tablero
                    tablero.setMatriz(solucionPasos.get(pasoActual));
                    panelTablero.actualizarTablero();
                    pasoActual++;
                    
                    // Mostrar progreso
                    panelControles.actualizarProgreso(pasoActual, solucionPasos.size());
                    
                    if (pasoActual >= solucionPasos.size()) {
                        timerResolucion.stop();
                        panelControles.habilitarBotones(true);
                        panelControles.resetearProgreso();
                        
                        // Mostrar celebración
                        panelTablero.mostrarCelebracion();
                        
                        JOptionPane.showMessageDialog(VentanaPrincipal.this, 
                            "¡Puzzle resuelto automáticamente!\n" +
                            "Movimientos realizados: " + (solucionPasos.size() - 1), 
                            "¡Completado!", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    timerResolucion.stop();
                    panelControles.habilitarBotones(true);
                    panelControles.resetearProgreso();
                }
            }
        });
    }
    
    private void configurarVentana() {
        setTitle("🧩 Puzzle 4x4 - Solucionador Inteligente con IA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Agregar panel de información
        JPanel panelInfo = crearPanelInformacion();
        
        add(panelInfo, BorderLayout.NORTH);
        add(panelTablero, BorderLayout.CENTER);
        add(panelControles, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        
        // Configurar icono de ventana (opcional)
        try {
            setIconImage(createPuzzleIcon());
        } catch (Exception e) {
            // Ignorar si no se puede crear el icono
        }
    }
    
    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titulo = new JLabel("🧩 Solucionador Automático de Puzzle 4x4", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setForeground(Color.WHITE);
        
        JLabel instrucciones = new JLabel(
            "<html><center>• Haz clic en 'Resolver IA' para ver la solución automática<br>" +
            "• Cada movimiento se muestra con animaciones de colores<br>" +
            "• También puedes jugar manualmente haciendo clic en las piezas</center></html>",
            SwingConstants.CENTER
        );
        instrucciones.setFont(new Font("Arial", Font.PLAIN, 11));
        instrucciones.setForeground(new Color(149, 165, 166));
        
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(instrucciones, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Image createPuzzleIcon() {
        // Crear un icono simple para la ventana
        int size = 32;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo
        g2.setColor(new Color(52, 73, 94));
        g2.fillRect(0, 0, size, size);
        
        // Cuadrícula simple
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        for (int i = 1; i < 4; i++) {
            int pos = (size * i) / 4;
            g2.drawLine(pos, 4, pos, size - 4);
            g2.drawLine(4, pos, size - 4, pos);
        }
        
        g2.dispose();
        return image;
    }
    
    private void configurarEventos() {
        panelControles.setResolverListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resolverPuzzle();
            }
        });
        
        panelControles.setReiniciarListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciarJuego();
            }
        });
        
        panelControles.setSalirListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
    
    private void resolverPuzzle() {
        if (tablero.estaResuelto()) {
            JOptionPane.showMessageDialog(this, 
                "¡El puzzle ya está resuelto! \n\n" +
                "Haz clic en 'Nuevo Puzzle' para generar uno nuevo.", 
                "Puzzle Completado", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        panelControles.habilitarBotones(false);
        
        // Mostrar diálogo de progreso
        JDialog dialogoProgreso = new JDialog(this, "Resolviendo Puzzle", true);
        JLabel labelProgreso = new JLabel("Analizando puzzle y buscando solución...");
        JProgressBar barraProgreso = new JProgressBar();
        barraProgreso.setIndeterminate(true);
        
        JPanel panelProgreso = new JPanel(new BorderLayout());
        panelProgreso.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelProgreso.add(labelProgreso, BorderLayout.NORTH);
        panelProgreso.add(barraProgreso, BorderLayout.CENTER);
        
        dialogoProgreso.add(panelProgreso);
        dialogoProgreso.setSize(300, 100);
        dialogoProgreso.setLocationRelativeTo(this);
        
        SwingWorker<List<int[][]>, Void> worker = new SwingWorker<List<int[][]>, Void>() {
            @Override
            protected List<int[][]> doInBackground() throws Exception {
                return ia.resolverPuzzle();
            }
            
            @Override
            protected void done() {
                dialogoProgreso.dispose(); // Cerrar diálogo de progreso
                
                try {
                    solucionPasos = get();
                    if (solucionPasos != null && solucionPasos.size() > 1) {
                        pasoActual = 0;
                        
                        // Mostrar información de la solución
                        int movimientos = solucionPasos.size() - 1;
                        JOptionPane.showMessageDialog(VentanaPrincipal.this, 
                            "✅ ¡Solución encontrada!\n\n" +
                            "Movimientos necesarios: " + movimientos + "\n" +
                            "Tiempo estimado: " + movimientos + " segundos\n\n" +
                            "La resolución comenzará automáticamente...", 
                            "Solución Lista", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        timerResolucion.start();
                    } else {
                        JOptionPane.showMessageDialog(VentanaPrincipal.this, 
                            "❌ No se pudo encontrar una solución válida\n\n" +
                            "Esto puede deberse a que:\n" +
                            "• El puzzle no es solucionable\n" +
                            "• El algoritmo encontró un problema\n\n" +
                            "Intenta generar un nuevo puzzle.", 
                            "Error de Resolución", 
                            JOptionPane.WARNING_MESSAGE);
                        panelControles.habilitarBotones(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(VentanaPrincipal.this, 
                        "❌ Error inesperado al resolver el puzzle:\n\n" + 
                        "Detalles técnicos: " + e.getMessage() + 
                        "\n\nPor favor, intenta con un nuevo puzzle.", 
                        "Error del Algoritmo", 
                        JOptionPane.ERROR_MESSAGE);
                    panelControles.habilitarBotones(true);
                }
            }
        };
        
        worker.execute();
        
        // Mostrar el diálogo después de iniciar el worker
        Timer timer = new Timer(100, e -> dialogoProgreso.setVisible(true));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void reiniciarJuego() {
        if (timerResolucion.isRunning()) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                "La resolución está en progreso.\n\n¿Estás seguro de que quieres generar un nuevo puzzle?",
                "Confirmar Reinicio",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                timerResolucion.stop();
            } else {
                return;
            }
        }
        
        tablero.mezclarTablero();
        panelTablero.actualizarTablero();
        panelControles.habilitarBotones(true);
        panelControles.resetearProgreso();
        
        JOptionPane.showMessageDialog(this, 
            "🎲 ¡Nuevo puzzle generado!\n\n" +
            "El tablero ha sido mezclado aleatoriamente.\n" +
            "Puedes jugar manualmente o usar el solucionador IA.", 
            "Nuevo Puzzle", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void onPiezaClicada(int x, int y) {
        if (!timerResolucion.isRunning()) {
            if (tablero.moverPieza(x, y)) {
                panelTablero.mostrarRetroalimentacionMovimiento(x, y, true);
                panelTablero.actualizarTablero();
                
                if (tablero.estaResuelto()) {
                    panelTablero.mostrarCelebracion();
                    
                    Timer celebracionTimer = new Timer(2000, e -> {
                        JOptionPane.showMessageDialog(this, 
                            "🎉 ¡FELICITACIONES! 🎉\n\n" +
                            "Has resuelto el puzzle manualmente.\n" +
                            "¡Excelente trabajo de resolución de problemas!\n\n" +
                            "¿Quieres intentar con un nuevo puzzle?", 
                            "¡Puzzle Completado!", 
                            JOptionPane.INFORMATION_MESSAGE);
                        ((Timer)e.getSource()).stop();
                    });
                    celebracionTimer.setRepeats(false);
                    celebracionTimer.start();
                }
            } else {
                // Dar feedback visual cuando el movimiento no es válido
                panelTablero.mostrarRetroalimentacionMovimiento(x, y, false);
            }
        } else {
            // Si está resolviendo automáticamente, mostrar mensaje
            JOptionPane.showMessageDialog(this,
                "⏳ La IA está resolviendo el puzzle automáticamente.\n\n" +
                "Por favor, espera a que termine o reinicia para jugar manualmente.",
                "Resolución en Progreso",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public Tablero getTablero() {
        return tablero;
    }
}