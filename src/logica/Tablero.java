package logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tablero {
    private int[][] matriz;
    private final int TAMAÑO = 4;
    private int espacioVacioX = 3;
    private int espacioVacioY = 3;
    
    public Tablero() {
        this.matriz = new int[TAMAÑO][TAMAÑO];
        inicializarTablero();
    }
    
    private void inicializarTablero() {
        // Crear tablero ordenado primero
        ordenarTablero();
        // Luego mezclarlo de manera válida
        mezclarTableroValidamente();
    }
    
    public void mezclarTablero() {
        mezclarTableroValidamente();
    }
    
    private void mezclarTableroValidamente() {
        // Comenzar desde estado resuelto y hacer movimientos aleatorios válidos
        // Esto garantiza que el puzzle siempre sea solucionable
        Random random = new Random();
        int numeroMovimientos = 100 + random.nextInt(200); // Entre 100 y 300 movimientos
        
        for (int i = 0; i < numeroMovimientos; i++) {
            List<int[]> movimientosPosibles = getPosiblesMovimientos();
            if (!movimientosPosibles.isEmpty()) {
                int[] movimiento = movimientosPosibles.get(random.nextInt(movimientosPosibles.size()));
                moverPieza(movimiento[0], movimiento[1]);
            }
        }
    }
    
    public boolean moverPieza(int x, int y) {
        if (esMovimientoValido(x, y)) {
            matriz[espacioVacioX][espacioVacioY] = matriz[x][y];
            matriz[x][y] = 0;
            espacioVacioX = x;
            espacioVacioY = y;
            return true;
        }
        return false;
    }
    
    private boolean esMovimientoValido(int x, int y) {
        if (x < 0 || x >= TAMAÑO || y < 0 || y >= TAMAÑO) {
            return false;
        }
        int difX = Math.abs(x - espacioVacioX);
        int difY = Math.abs(y - espacioVacioY);
        return (difX == 1 && difY == 0) || (difX == 0 && difY == 1);
    }
    
    public boolean estaResuelto() {
        int contador = 1;
        for (int i = 0; i < TAMAÑO; i++) {
            for (int j = 0; j < TAMAÑO; j++) {
                if (i == 3 && j == 3) {
                    return matriz[i][j] == 0;
                }
                if (matriz[i][j] != contador++) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void ordenarTablero() {
        int contador = 1;
        for (int i = 0; i < TAMAÑO; i++) {
            for (int j = 0; j < TAMAÑO; j++) {
                if (i == 3 && j == 3) {
                    matriz[i][j] = 0;
                    espacioVacioX = i;
                    espacioVacioY = j;
                } else {
                    matriz[i][j] = contador++;
                }
            }
        }
    }
    
    public int getValor(int x, int y) {
        if (x >= 0 && x < TAMAÑO && y >= 0 && y < TAMAÑO) {
            return matriz[x][y];
        }
        return -1;
    }
    
    public int getTamaño() {
        return TAMAÑO;
    }
    
    public int getEspacioVacioX() {
        return espacioVacioX;
    }
    
    public int getEspacioVacioY() {
        return espacioVacioY;
    }
    
    public int[][] getMatriz() {
        int[][] copia = new int[TAMAÑO][TAMAÑO];
        for (int i = 0; i < TAMAÑO; i++) {
            System.arraycopy(matriz[i], 0, copia[i], 0, TAMAÑO);
        }
        return copia;
    }
    
    public void setMatriz(int[][] nuevaMatriz) {
        for (int i = 0; i < TAMAÑO; i++) {
            for (int j = 0; j < TAMAÑO; j++) {
                this.matriz[i][j] = nuevaMatriz[i][j];
                if (nuevaMatriz[i][j] == 0) {
                    espacioVacioX = i;
                    espacioVacioY = j;
                }
            }
        }
    }
    
    public List<int[]> getPosiblesMovimientos() {
        List<int[]> movimientos = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        for (int i = 0; i < 4; i++) {
            int nuevoX = espacioVacioX + dx[i];
            int nuevoY = espacioVacioY + dy[i];
            
            if (nuevoX >= 0 && nuevoX < TAMAÑO && nuevoY >= 0 && nuevoY < TAMAÑO) {
                movimientos.add(new int[]{nuevoX, nuevoY});
            }
        }
        
        return movimientos;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TAMAÑO; i++) {
            for (int j = 0; j < TAMAÑO; j++) {
                if (matriz[i][j] == 0) {
                    sb.append("[  ]");
                } else {
                    sb.append(String.format("[%2d]", matriz[i][j]));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
