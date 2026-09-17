package logica;

import java.util.*;

/**
 * Resuelve el puzzle deslizante 4x4 (15-puzzle) usando IDA*
 * (Iterative Deepening A*) con la distancia de Manhattan como heurística.
 *
 * IDA* es una variante de A* pensada específicamente para problemas como
 * el 15-puzzle: en vez de mantener en memoria un conjunto abierto y un
 * conjunto de visitados que pueden crecer sin límite (el problema del A*
 * clásico en tableros difíciles, donde puede necesitarse explorar
 * millones de estados), IDA* hace una búsqueda en profundidad con un
 * umbral de f = g + h que se incrementa progresivamente. Esto la hace
 * completa (siempre encuentra una solución si existe), óptima (encuentra
 * la solución más corta) y con un consumo de memoria proporcional solo a
 * la profundidad de la solución, no al número de estados visitados.
 */
public class HeuristicaIA {
    private static final int[] DFILA = {-1, 1, 0, 0};
    private static final int[] DCOL = {0, 0, -1, 1};

    // El número de Dios del 15-puzzle es 80: ninguna configuración
    // solucionable necesita más de 80 movimientos óptimos. Se usa como
    // cota de seguridad para no iterar umbrales indefinidamente ante un
    // error de estado.
    private static final int PROFUNDIDAD_MAXIMA_TEORICA = 80;
    // Cota de seguridad frente a un desbordamiento patológico; en la
    // práctica el límite de tiempo (comprobado periódicamente, no en cada
    // nodo, para no penalizar el rendimiento) es el que decide cuándo
    // abandonar la búsqueda.
    private static final long LIMITE_NODOS = 2_000_000_000L;
    private static final long LIMITE_TIEMPO_MS = 30_000L;
    private static final long INTERVALO_CHEQUEO_TIEMPO = 100_000L;

    private Tablero tablero;
    private List<int[][]> solucion;

    // Estado de trabajo de la búsqueda IDA* en curso.
    private List<int[]> movimientos;
    private long nodosExplorados;
    private long tiempoInicioBusqueda;
    private boolean limiteExcedido;

    public HeuristicaIA(Tablero tablero) {
        this.tablero = tablero;
        this.solucion = new ArrayList<>();
    }

    /**
     * Calcula la solución completa (lista de estados sucesivos del
     * tablero, empezando por el estado actual) para resolver el puzzle.
     * Devuelve una lista con un único elemento si el puzzle ya está
     * resuelto, y una lista vacía si no fue posible encontrar solución
     * (tablero no solucionable, o presupuesto de búsqueda agotado).
     */
    public List<int[][]> resolverPuzzle() {
        solucion.clear();

        if (tablero.estaResuelto()) {
            return solucion;
        }

        int[][] estadoInicial = tablero.getMatriz();

        // Todo tablero producido por Tablero#mezclarTablero() es
        // solucionable por construcción (se genera con una caminata
        // aleatoria de movimientos válidos desde el estado resuelto), así
        // que esta comprobación nunca debería fallar en el flujo normal
        // de la aplicación. Se deja como red de seguridad explícita para
        // cualquier estado que llegue por otra vía (p.ej. cargado desde
        // fuera con Tablero#setMatriz).
        if (!esSolucionable(estadoInicial)) {
            System.out.println("El tablero actual no es solucionable (paridad de inversiones inválida).");
            return solucion;
        }

        System.out.println("Resolviendo con IDA* (distancia de Manhattan)...");
        if (resolverConIDAEstrella(estadoInicial)) {
            System.out.println("Solución encontrada: " + (solucion.size() - 1) + " movimientos, "
                    + nodosExplorados + " nodos explorados");
        } else {
            System.out.println("No se encontró solución dentro del presupuesto de búsqueda ("
                    + nodosExplorados + " nodos, límite " + LIMITE_NODOS + ").");
        }

        return solucion;
    }

    // ---------------------------------------------------------------
    // IDA*
    // ---------------------------------------------------------------

    private boolean resolverConIDAEstrella(int[][] estadoInicial) {
        int[] plano = aPlano(estadoInicial);
        int blanco = indiceDeCero(plano);

        int umbral = calcularHeuristicaPlano(plano);
        movimientos = new ArrayList<>();
        nodosExplorados = 0;
        limiteExcedido = false;
        tiempoInicioBusqueda = System.currentTimeMillis();

        while (umbral <= PROFUNDIDAD_MAXIMA_TEORICA) {
            int resultado = busquedaProfundidadLimitada(plano, blanco, 0, umbral, -1);

            if (resultado == ENCONTRADO) {
                construirSolucionDesdeMovimientos(estadoInicial);
                return true;
            }
            if (limiteExcedido) {
                return false;
            }
            if (resultado == Integer.MAX_VALUE) {
                // No debería ocurrir para un tablero solucionable: significa
                // que se agotó el espacio de búsqueda sin hallar el objetivo.
                return false;
            }
            umbral = resultado;
        }

        return false;
    }

    private static final int ENCONTRADO = -1;

    /**
     * Búsqueda en profundidad acotada por f = g + h &lt;= umbral.
     * Devuelve ENCONTRADO si se llegó al estado objetivo, o el menor valor
     * de f que superó el umbral entre todos los nodos explorados (para
     * usarlo como próximo umbral), o Integer.MAX_VALUE si no queda nada
     * por explorar.
     */
    private int busquedaProfundidadLimitada(int[] plano, int blanco, int g, int umbral, int direccionPrevia) {
        nodosExplorados++;
        if (nodosExplorados > LIMITE_NODOS) {
            limiteExcedido = true;
            return Integer.MAX_VALUE;
        }
        // System.currentTimeMillis() tiene coste no despreciable si se
        // llama en cada nodo dado el volumen de nodos que IDA* explora;
        // se comprueba solo cada INTERVALO_CHEQUEO_TIEMPO nodos.
        if (nodosExplorados % INTERVALO_CHEQUEO_TIEMPO == 0
                && System.currentTimeMillis() - tiempoInicioBusqueda > LIMITE_TIEMPO_MS) {
            limiteExcedido = true;
            return Integer.MAX_VALUE;
        }

        int h = calcularHeuristicaPlano(plano);
        int f = g + h;
        if (f > umbral) {
            return f;
        }
        if (h == 0) {
            // Manhattan == 0 para las 15 piezas numeradas implica que la
            // única celda restante (la del espacio vacío) también está en
            // su posición objetivo: el tablero está resuelto.
            return ENCONTRADO;
        }

        int minimoSiguiente = Integer.MAX_VALUE;
        int filaBlanco = blanco / 4;
        int colBlanco = blanco % 4;

        for (int dir = 0; dir < 4; dir++) {
            // Evita deshacer inmediatamente el movimiento anterior (ciclo
            // trivial de longitud 2), una poda estándar de IDA* que reduce
            // drásticamente el factor de ramificación sin afectar la
            // completitud ni la optimalidad del algoritmo.
            if (dir == (direccionPrevia ^ 1)) {
                continue;
            }

            int nuevaFila = filaBlanco + DFILA[dir];
            int nuevaCol = colBlanco + DCOL[dir];
            if (nuevaFila < 0 || nuevaFila >= 4 || nuevaCol < 0 || nuevaCol >= 4) {
                continue;
            }

            int nuevoIndice = nuevaFila * 4 + nuevaCol;

            plano[blanco] = plano[nuevoIndice];
            plano[nuevoIndice] = 0;
            movimientos.add(new int[]{nuevaFila, nuevaCol});

            int resultado = busquedaProfundidadLimitada(plano, nuevoIndice, g + 1, umbral, dir);

            if (resultado == ENCONTRADO) {
                return ENCONTRADO;
            }
            if (limiteExcedido) {
                return Integer.MAX_VALUE;
            }
            if (resultado < minimoSiguiente) {
                minimoSiguiente = resultado;
            }

            // Deshacer el movimiento para continuar explorando otras ramas.
            plano[nuevoIndice] = plano[blanco];
            plano[blanco] = 0;
            movimientos.remove(movimientos.size() - 1);
        }

        return minimoSiguiente;
    }

    private void construirSolucionDesdeMovimientos(int[][] estadoInicial) {
        int[][] estado = copiarMatriz(estadoInicial);
        solucion.add(copiarMatriz(estado));

        int[] posVacio = encontrarEspacioVacio(estado);
        for (int[] mov : movimientos) {
            int nr = mov[0];
            int nc = mov[1];
            estado[posVacio[0]][posVacio[1]] = estado[nr][nc];
            estado[nr][nc] = 0;
            posVacio[0] = nr;
            posVacio[1] = nc;
            solucion.add(copiarMatriz(estado));
        }
    }

    // ---------------------------------------------------------------
    // Heurística y comprobación de solucionabilidad
    // ---------------------------------------------------------------

    /**
     * Distancia de Manhattan: suma, para cada pieza numerada (excluyendo
     * el espacio vacío), de la distancia en filas más la distancia en
     * columnas hasta su posición objetivo. Es una heurística admisible
     * (nunca sobreestima el número real de movimientos) porque cada
     * movimiento del puzzle desplaza como máximo una pieza una casilla.
     */
    int calcularHeuristica(int[][] estado) {
        int manhattan = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int valor = estado[i][j];
                if (valor != 0) {
                    int filaObjetivo = (valor - 1) / 4;
                    int colObjetivo = (valor - 1) % 4;
                    manhattan += Math.abs(i - filaObjetivo) + Math.abs(j - colObjetivo);
                }
            }
        }
        return manhattan;
    }

    /**
     * Heurística usada por la búsqueda: distancia de Manhattan más
     * "conflicto lineal" (Hansson, Mayer &amp; Yung, 1992). Dos piezas
     * están en conflicto lineal cuando ambas pertenecen a la misma fila
     * (o columna) objetivo, ambas están ya en esa fila/columna, pero en
     * el orden contrario al que deberían tener: para llegar a su posición
     * una de ellas tendrá que salir de la fila/columna y volver a entrar,
     * lo que cuesta 2 movimientos adicionales por cada pieza que haya que
     * apartar. El número mínimo de piezas a apartar en una línea con k
     * piezas candidatas es k menos la subsecuencia creciente más larga
     * (LIS) de sus columnas/filas objetivo — el resultado clásico de
     * "mínimo de eliminaciones para dejar una secuencia ordenada". Sumar
     * 2 * (piezas a apartar) a la distancia de Manhattan sigue siendo
     * admisible: nunca sobreestima el número real de movimientos, pero
     * poda drásticamente el árbol de búsqueda de IDA* frente a usar solo
     * Manhattan.
     */
    private int calcularHeuristicaPlano(int[] plano) {
        int manhattan = 0;
        for (int idx = 0; idx < 16; idx++) {
            int valor = plano[idx];
            if (valor != 0) {
                int filaActual = idx / 4;
                int colActual = idx % 4;
                int filaObjetivo = (valor - 1) / 4;
                int colObjetivo = (valor - 1) % 4;
                manhattan += Math.abs(filaActual - filaObjetivo) + Math.abs(colActual - colObjetivo);
            }
        }
        return manhattan + calcularConflictoLinealPlano(plano);
    }

    // Buffers reutilizados para no asignar memoria en el punto más caliente
    // de la búsqueda (se invoca en cada nodo explorado, potencialmente
    // decenas de millones de veces por resolución).
    private final int[] bufferLinea = new int[4];
    private final int[] bufferLis = new int[4];

    private int calcularConflictoLinealPlano(int[] plano) {
        int conflicto = 0;

        // Filas: para cada fila, las piezas que pertenecen a esa fila
        // objetivo, en el orden en que aparecen actualmente.
        for (int fila = 0; fila < 4; fila++) {
            int n = 0;
            for (int col = 0; col < 4; col++) {
                int valor = plano[fila * 4 + col];
                if (valor != 0 && (valor - 1) / 4 == fila) {
                    bufferLinea[n++] = (valor - 1) % 4;
                }
            }
            conflicto += n - longitudSubsecuenciaCreciente(bufferLinea, n);
        }

        // Columnas: análogo, recorriendo cada columna de arriba a abajo.
        for (int col = 0; col < 4; col++) {
            int n = 0;
            for (int fila = 0; fila < 4; fila++) {
                int valor = plano[fila * 4 + col];
                if (valor != 0 && (valor - 1) % 4 == col) {
                    bufferLinea[n++] = (valor - 1) / 4;
                }
            }
            conflicto += n - longitudSubsecuenciaCreciente(bufferLinea, n);
        }

        return conflicto * 2;
    }

    private int longitudSubsecuenciaCreciente(int[] secuencia, int n) {
        int max = 0;
        for (int i = 0; i < n; i++) {
            bufferLis[i] = 1;
            for (int j = 0; j < i; j++) {
                if (secuencia[j] < secuencia[i] && bufferLis[j] + 1 > bufferLis[i]) {
                    bufferLis[i] = bufferLis[j] + 1;
                }
            }
            if (bufferLis[i] > max) {
                max = bufferLis[i];
            }
        }
        return max;
    }

    /**
     * Comprueba si una configuración del 15-puzzle es solucionable
     * mediante la regla de paridad de inversiones: en un tablero de ancho
     * par, es solucionable si y solo si
     * (fila del vacío contada desde abajo, 1-indexada) + (número de
     * inversiones) es par.
     */
    boolean esSolucionable(int[][] matriz) {
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

    // ---------------------------------------------------------------
    // Utilidades de estado
    // ---------------------------------------------------------------

    private int[] aPlano(int[][] matriz) {
        int[] plano = new int[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                plano[i * 4 + j] = matriz[i][j];
            }
        }
        return plano;
    }

    private int indiceDeCero(int[] plano) {
        for (int i = 0; i < 16; i++) {
            if (plano[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    boolean estaResuelto(int[][] estado) {
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

    private int[][] copiarMatriz(int[][] matriz) {
        int[][] copia = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(matriz[i], 0, copia[i], 0, 4);
        }
        return copia;
    }
}
