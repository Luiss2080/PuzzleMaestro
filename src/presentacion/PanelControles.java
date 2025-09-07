package presentacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PanelControles extends JPanel {
    private JButton btnResolver;
    private JButton btnReiniciar;
    private JButton btnSalir;
    private JProgressBar barraProgreso;
    private JLabel lblProgreso;
    private JLabel lblEstadisticas;
    
    public PanelControles() {
        inicializarPanel();
        crearComponentes();
        organizarComponentes();
    }
    
    private void inicializarPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setPreferredSize(new Dimension(350, 120)); // Aumentar altura para la barra de progreso
    }
    
    private void crearComponentes() {
        // Crear botones
        btnResolver = crearBoton("Resolver IA", new Color(46, 204, 113));
        btnReiniciar = crearBoton("Nuevo Puzzle", new Color(52, 152, 219));
        btnSalir = crearBoton("Salir", new Color(231, 76, 60));
        
        // Crear barra de progreso
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo para resolver");
        barraProgreso.setForeground(new Color(46, 204, 113));
        barraProgreso.setBackground(new Color(189, 195, 199));
        barraProgreso.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        barraProgreso.setFont(new Font("Arial", Font.BOLD, 10));
        
        // Crear etiquetas
        lblProgreso = new JLabel("Estado: Esperando", SwingConstants.CENTER);
        lblProgreso.setFont(new Font("Arial", Font.BOLD, 11));
        lblProgreso.setForeground(new Color(44, 62, 80));
        
        lblEstadisticas = new JLabel("", SwingConstants.CENTER);
        lblEstadisticas.setFont(new Font("Arial", Font.ITALIC, 10));
        lblEstadisticas.setForeground(new Color(127, 140, 141));
    }
    
    private JButton crearBoton(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(colorFondo);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        boton.setPreferredSize(new Dimension(90, 35));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        final Color originalColor = colorFondo;
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (boton.isEnabled()) {
                    boton.setBackground(colorFondo.darker());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (boton.isEnabled()) {
                    boton.setBackground(originalColor);
                }
            }
        });
        
        return boton;
    }
    
    private void organizarComponentes() {
        // Panel superior con botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBackground(new Color(236, 240, 241));
        panelBotones.add(btnResolver);
        panelBotones.add(btnReiniciar);
        panelBotones.add(btnSalir);
        
        // Panel inferior con progreso
        JPanel panelProgreso = new JPanel(new BorderLayout(5, 5));
        panelProgreso.setBackground(new Color(236, 240, 241));
        panelProgreso.add(lblProgreso, BorderLayout.NORTH);
        panelProgreso.add(barraProgreso, BorderLayout.CENTER);
        panelProgreso.add(lblEstadisticas, BorderLayout.SOUTH);
        
        // Agregar a layout principal
        add(panelBotones, BorderLayout.NORTH);
        add(panelProgreso, BorderLayout.SOUTH);
    }
    
    public void actualizarProgreso(int pasoActual, int totalPasos) {
        if (totalPasos > 0) {
            int porcentaje = (pasoActual * 100) / totalPasos;
            barraProgreso.setValue(porcentaje);
            barraProgreso.setString("Resolviendo... " + pasoActual + "/" + totalPasos);
            lblProgreso.setText("Estado: Ejecutando movimiento " + pasoActual);
            
            // Cambiar color según progreso
            if (porcentaje < 30) {
                barraProgreso.setForeground(new Color(231, 76, 60)); // Rojo
            } else if (porcentaje < 70) {
                barraProgreso.setForeground(new Color(255, 199, 95)); // Amarillo
            } else {
                barraProgreso.setForeground(new Color(46, 204, 113)); // Verde
            }
        }
    }
    
    public void resetearProgreso() {
        barraProgreso.setValue(0);
        barraProgreso.setString("Listo para resolver");
        barraProgreso.setForeground(new Color(46, 204, 113));
        lblProgreso.setText("Estado: Completado");
        lblEstadisticas.setText("¡Puzzle resuelto exitosamente!");
        
        // Resetear después de un momento
        Timer timer = new Timer(3000, e -> {
            lblProgreso.setText("Estado: Esperando");
            lblEstadisticas.setText("");
            ((Timer)e.getSource()).stop();
        });
        timer.start();
    }
    
    public void setResolverListener(ActionListener listener) {
        btnResolver.addActionListener(listener);
    }
    
    public void setReiniciarListener(ActionListener listener) {
        btnReiniciar.addActionListener(listener);
    }
    
    public void setSalirListener(ActionListener listener) {
        btnSalir.addActionListener(listener);
    }
    
    public void habilitarBotones(boolean habilitar) {
        btnResolver.setEnabled(habilitar);
        btnReiniciar.setEnabled(habilitar);
        
        if (habilitar) {
            btnResolver.setBackground(new Color(46, 204, 113));
            btnReiniciar.setBackground(new Color(52, 152, 219));
            lblProgreso.setText("Estado: Esperando");
        } else {
            btnResolver.setBackground(new Color(127, 140, 141));
            btnReiniciar.setBackground(new Color(127, 140, 141));
            lblProgreso.setText("Estado: Resolviendo...");
            barraProgreso.setValue(0);
            barraProgreso.setString("Iniciando resolución...");
            lblEstadisticas.setText("Calculando movimientos óptimos...");
        }
    }
}
