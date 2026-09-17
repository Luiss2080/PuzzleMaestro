package logica;

import java.util.*;

public class HeuristicaIA {
    private Tablero tablero;
    private List<int[][]> solucion;
    
    public HeuristicaIA(Tablero tablero) {
        this.tablero = tablero;
        this.solucion = new ArrayList<>();
    }
    
    public List<int[][]> resolverPuzzle() {
        solucion.clear();
        
        if (tablero.estaResuelto()) {
            return solucion;
        }
        
        if (!esSolucionable(tablero.getMatriz())) {
            System.out.println("Puzzle no solucionable, generando solución directa...");
            generarSolucionDirecta();
            return solucion;
        }
        
        System.out.println("Intentando resolver con A*...");
        try {
            if (resolverConAStar()) {
                System.out.println("Solución encontrada con A*: " + solucion.size() + " pasos");
                return solucion;
            }
        } catch (Exception e) {
            System.out.println("Error en A*: " + e.getMessage());
        }
        
        System.out.println("A* falló, usando solución directa...");
        generarSolucionDirecta();
        return solucion;
    }
    
    private boolean resolverConAStar() {
        PriorityQueue<Nodo> abiertos = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<String> visitados = new HashSet<>();
        
        int[][] estadoInicial = copiarMatriz(tablero.getMatriz());
        Nodo inicial = new Nodo(estadoInicial, 0, calcularHeuristica(estadoInicial), null);
        abiertos.offer(inicial);
        
        int maxIteraciones = 10000; // Reducido para evitar trabas
        int iteraciones = 0;
        long tiempoInicio = System.currentTimeMillis();
        long maxTiempo = 5000; // 5 segundos máximo
        
        while (!abiertos.isEmpty() && iteraciones < maxIteraciones) {
            iteraciones++;
            
            // Verificar timeout
            if (System.currentTimeMillis() - tiempoInicio > maxTiempo) {
                System.out.println("Timeout en A* después de " + iteraciones + " iteraciones");
                return false;
            }
            
            Nodo actual = abiertos.poll();
            String clave = matrizToString(actual.estado);
            
            if (visitados.contains(clave)) {
                continue;
            }
            visitados.add(clave);
            
            if (estaResuelto(actual.estado)) {
                construirSolucion(actual);
                return true;
            }
            
            // Limitar profundidad para evitar soluciones muy largas
            if (actual.g > 50) {
                continue;
            }
            
            List<int[][]> sucesores = generarSucesores(actual.estado);
            for (int[][] sucesor : sucesores) {
                String claveSucesor = matrizToString(sucesor);
                if (!visitados.contains(claveSucesor)) {
                    int g = actual.g + 1;
                    int h = calcularHeuristica(sucesor);
                    Nodo nuevoNodo = new Nodo(sucesor, g, h, actual);
                    abiertos.offer(nuevoNodo);
                }
            }
            
            // Progreso cada 1000 iteraciones
            if (iteraciones % 1000 == 0) {
                System.out.println("A* - Iteración: " + iteraciones + ", Estados visitados: " + visitados.size());
            }
        }
        
        System.out.println("A* terminó sin solución después de " + iteraciones + " iteraciones");
        return false;
    }
    
    private void generarSolucionDirecta() {
        solucion.clear();
        
        int[][] estado = copiarMatriz(tablero.getMatriz());
        solucion.add(copiarMatriz(estado));
        
        System.out.println("Iniciando solución directa...");
        
        try {
            resolverCapaSuperiores(estado);
            resolverCapaInferiores(estado);

            int[] posVacioActual = encontrarEspacioVacio(estado);
            if (posVacioActual[0] != 3 || posVacioActual[1] != 3) {
                moverEspacioVacioA(estado, 3, 3);
            }

            if (!estaResuelto(estado)) {
                // Con el límite de sub-movimientos ahora estricto (ver
                // colocarPiezaEnPosicion), es posible en casos muy raros que
                // alguna pieza no llegue a su posición final dentro del
                // límite. Lo dejamos explícito en el log en vez de reportar
                // éxito silenciosamente sobre un tablero no resuelto.
                System.out.println("ADVERTENCIA: la solución directa terminó sin resolver completamente el tablero.");
            }

            System.out.println("Solución directa completada con " + solucion.size() + " pasos");
        } catch (Exception e) {
            System.out.println("Error en solución directa: " + e.getMessage());
            // Si falla todo, al menos devolver el estado inicial
            if (solucion.isEmpty()) {
                solucion.add(copiarMatriz(tablero.getMatriz()));
            }
        }
    }
    
    private void resolverCapaSuperiores(int[][] estado) {
        for (int fila = 0; fila < 2; fila++) {
            for (int col = 0; col < 4; col++) {
                int valorObjetivo = fila * 4 + col + 1;
                colocarPiezaEnPosicion(estado, valorObjetivo, fila, col);
            }
        }
    }
    
    private void resolverCapaInferiores(int[][] estado) {
        for (int col = 0; col < 2; col++) {
            int valor1 = 2 * 4 + col + 1;
            int valor2 = 3 * 4 + col + 1;
            
            colocarPiezaEnPosicion(estado, valor1, 2, col);
            colocarPiezaEnPosicion(estado, valor2, 3, col);
        }
        
        resolver2x2Final(estado);
    }
    
    private void resolver2x2Final(int[][] estado) {
        int maxIntentos = 50;
        int intentos = 0;
        
        while (!estaResuelto2x2Final(estado) && intentos < maxIntentos) {
            int[] posVacio = encontrarEspacioVacio(estado);
            
            if (posVacio[0] == 3 && posVacio[1] == 3) {
                if (estado[2][2] != 11) {
                    realizarMovimiento(estado, 2, 3);
                } else if (estado[2][3] != 12) {
                    realizarMovimiento(estado, 3, 2);
                } else if (estado[3][2] != 14) {
                    realizarMovimiento(estado, 2, 3);
                }
            } else {
                moverEspacioVacioA(estado, 3, 3);
            }
            
            intentos++;
        }
    }
    
    private boolean estaResuelto2x2Final(int[][] estado) {
        return estado[2][2] == 11 && estado[2][3] == 12 && 
               estado[3][2] == 14 && estado[3][3] == 0;
    }
    
    private void realizarMovimiento(int[][] estado, int fila, int col) {
        int[] posVacio = encontrarEspacioVacio(estado);
        
        estado[posVacio[0]][posVacio[1]] = estado[fila][col];
        estado[fila][col] = 0;
        solucion.add(copiarMatriz(estado));
    }
    
    private void colocarPiezaEnPosicion(int[][] estado, int pieza, int filaDestino, int colDestino) {
        if (estado[filaDestino][colDestino] == pieza) {
            return;
        }
        
        int[] posActual = encontrarPieza(estado, pieza);
        if (posActual == null) return;

        // IMPORTANTE: "intentos" cuenta el TOTAL de sub-movimientos realizados
        // para colocar esta pieza y NUNCA se reinicia. La versión anterior
        // hacía "intentos = 0" cada vez que un movimiento tenía éxito, lo que
        // anulaba por completo el límite: una pieza que oscila entre dos
        // casillas (moverse, ser desplazada, volver a moverse) generaba
        // movimientos "exitosos" indefinidamente, cada uno agregando una
        // copia del tablero a la lista de solución, hasta agotar la memoria
        // (OutOfMemoryError verificado empíricamente en tableros difíciles
        // donde A* agota su presupuesto de búsqueda y cae a este método).
        int maxIntentos = 60;
        int intentos = 0;

        while (intentos < maxIntentos) {
            posActual = encontrarPieza(estado, pieza);
            if (posActual == null) break;

            if (posActual[0] == filaDestino && posActual[1] == colDestino) {
                break;
            }

            if (!moverPiezaUnPaso(estado, posActual, filaDestino, colDestino)) {
                posicionarEspacioVacio(estado, posActual, filaDestino, colDestino);
            }

            intentos++;
        }
    }
    
    private boolean moverPiezaUnPaso(int[][] estado, int[] posActual, int filaDestino, int colDestino) {
        int[] posVacio = encontrarEspacioVacio(estado);
        
        int deltaFila = filaDestino - posActual[0];
        int deltaCol = colDestino - posActual[1];
        
        int proximaFila = posActual[0];
        int proximaCol = posActual[1];
        
        if (deltaFila > 0) proximaFila++;
        else if (deltaFila < 0) proximaFila--;
        else if (deltaCol > 0) proximaCol++;
        else if (deltaCol < 0) proximaCol--;
        
        if (posVacio[0] == proximaFila && posVacio[1] == proximaCol) {
            estado[posVacio[0]][posVacio[1]] = estado[posActual[0]][posActual[1]];
            estado[posActual[0]][posActual[1]] = 0;
            solucion.add(copiarMatriz(estado));
            return true;
        }
        
        return false;
    }
    
    private void posicionarEspacioVacio(int[][] estado, int[] piezaPos, int filaDestino, int colDestino) {
        int deltaFila = filaDestino - piezaPos[0];
        int deltaCol = colDestino - piezaPos[1];
        
        int espacioFilaObjetivo = piezaPos[0];
        int espacioColObjetivo = piezaPos[1];
        
        if (deltaFila > 0) espacioFilaObjetivo++;
        else if (deltaFila < 0) espacioFilaObjetivo--;
        else if (deltaCol > 0) espacioColObjetivo++;
        else if (deltaCol < 0) espacioColObjetivo--;
        
        moverEspacioVacioA(estado, espacioFilaObjetivo, espacioColObjetivo);
    }
    
    private boolean moverEspacioVacioA(int[][] estado, int filaObj, int colObj) {
        int[] posVacio = encontrarEspacioVacio(estado);
        
        if (posVacio[0] == filaObj && posVacio[1] == colObj) {
            return true;
        }
        
        int deltaFila = filaObj - posVacio[0];
        int deltaCol = colObj - posVacio[1];
        
        int nuevaFila = posVacio[0];
        int nuevaCol = posVacio[1];
        
        if (deltaFila != 0) {
            nuevaFila = posVacio[0] + (deltaFila > 0 ? 1 : -1);
        } else if (deltaCol != 0) {
            nuevaCol = posVacio[1] + (deltaCol > 0 ? 1 : -1);
        }
        
        if (nuevaFila >= 0 && nuevaFila < 4 && nuevaCol >= 0 && nuevaCol < 4) {
            estado[posVacio[0]][posVacio[1]] = estado[nuevaFila][nuevaCol];
            estado[nuevaFila][nuevaCol] = 0;
            solucion.add(copiarMatriz(estado));
            return true;
        }
        
        return false;
    }
    
    private int[] encontrarPieza(int[][] estado, int pieza) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (estado[i][j] == pieza) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
    
    private boolean esSolucionable(int[][] matriz) {
        int[] array = new int[15];
        int index = 0;
        int filaVacio = 0;
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (matriz[i][j] == 0) {
                    filaVacio = i;
                } else {
                    array[index++] = matriz[i][j];
                }
            }
        }
        
        int inversiones = 0;
        for (int i = 0; i < 15; i++) {
            for (int j = i + 1; j < 15; j++) {
                if (array[i] > array[j]) {
                    inversiones++;
                }
            }
        }
        
        int filaDesdeAbajo = 4 - filaVacio;
        
        if (filaDesdeAbajo % 2 == 1) {
            return inversiones % 2 == 0;
        } else {
            return inversiones % 2 == 1;
        }
    }
    
    private int calcularHeuristica(int[][] estado) {
        int manhattan = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (estado[i][j] != 0) {
                    int valor = estado[i][j];
                    int filaObjetivo = (valor - 1) / 4;
                    int colObjetivo = (valor - 1) % 4;
                    manhattan += Math.abs(i - filaObjetivo) + Math.abs(j - colObjetivo);
                }
            }
        }
        return manhattan;
    }
    
    private List<int[][]> generarSucesores(int[][] estado) {
        List<int[][]> sucesores = new ArrayList<>();
        int[] posVacio = encontrarEspacioVacio(estado);
        List<int[]> movimientos = obtenerMovimientosPosibles(posVacio[0], posVacio[1]);
        
        for (int[] mov : movimientos) {
            int[][] nuevoEstado = copiarMatriz(estado);
            nuevoEstado[posVacio[0]][posVacio[1]] = nuevoEstado[mov[0]][mov[1]];
            nuevoEstado[mov[0]][mov[1]] = 0;
            sucesores.add(nuevoEstado);
        }
        
        return sucesores;
    }
    
    private List<int[]> obtenerMovimientosPosibles(int x, int y) {
        List<int[]> movimientos = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        for (int i = 0; i < 4; i++) {
            int nuevoX = x + dx[i];
            int nuevoY = y + dy[i];
            
            if (nuevoX >= 0 && nuevoX < 4 && nuevoY >= 0 && nuevoY < 4) {
                movimientos.add(new int[]{nuevoX, nuevoY});
            }
        }
        
        return movimientos;
    }
    
    private int[] encontrarEspacioVacio(int[][] estado) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (estado[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{3, 3};
    }
    
    private boolean estaResuelto(int[][] estado) {
        int contador = 1;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 && j == 3) {
                    return estado[i][j] == 0;
                }
                if (estado[i][j] != contador++) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void construirSolucion(Nodo nodoFinal) {
        List<int[][]> pasos = new ArrayList<>();
        Nodo actual = nodoFinal;
        
        while (actual != null) {
            pasos.add(copiarMatriz(actual.estado));
            actual = actual.padre;
        }
        
        Collections.reverse(pasos);
        solucion.addAll(pasos);
    }
    
    private int[][] copiarMatriz(int[][] matriz) {
        int[][] copia = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(matriz[i], 0, copia[i], 0, 4);
        }
        return copia;
    }
    
    private String matrizToString(int[][] matriz) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(matriz[i][j]).append(",");
            }
        }
        return sb.toString();
    }
    
    private static class Nodo {
        int[][] estado;
        int g;
        int f;
        Nodo padre;
        
        public Nodo(int[][] estado, int g, int h, Nodo padre) {
            this.estado = copiarMatriz(estado);
            this.g = g;
            this.f = g + h;
            this.padre = padre;
        }
        
        private static int[][] copiarMatriz(int[][] matriz) {
            int[][] copia = new int[4][4];
            for (int i = 0; i < 4; i++) {
                System.arraycopy(matriz[i], 0, copia[i], 0, 4);
            }
            return copia;
        }
    }
}
