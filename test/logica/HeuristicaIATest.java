package logica;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la lógica pura de HeuristicaIA: la heurística de distancia
 * de Manhattan, la comprobación de solucionabilidad por paridad de
 * inversiones, y el comportamiento de extremo a extremo del solucionador
 * IDA*.
 */
class HeuristicaIATest {

    private static int[][] tableroResuelto() {
        return new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };
    }

    private HeuristicaIA nuevaHeuristica() {
        return new HeuristicaIA(new Tablero());
    }

    // -----------------------------------------------------------------
    // Distancia de Manhattan
    // -----------------------------------------------------------------

    @Test
    void heuristicaEsCeroEnElEstadoObjetivo() {
        HeuristicaIA ia = nuevaHeuristica();
        assertEquals(0, ia.calcularHeuristica(tableroResuelto()));
    }

    @Test
    void heuristicaExcluyeElEspacioVacioDelCalculo() {
        // Estado objetivo salvo que el espacio vacío está en (0,0) y la
        // pieza 1 se movió a (3,3): si el cálculo incluyera por error el
        // 0 como "pieza", el resultado cambiaría.
        int[][] estado = tableroResuelto();
        estado[0][0] = 0;
        estado[3][3] = 1;

        HeuristicaIA ia = nuevaHeuristica();
        // Solo la pieza 1 está fuera de lugar: de (0,0) a (3,3) son
        // |0-3| + |0-3| = 6.
        assertEquals(6, ia.calcularHeuristica(estado));
    }

    @Test
    void heuristicaSumaDistanciasDeVariasPiezasDesplazadas() {
        // Intercambiar 1 y 2 (fila 0): cada una a 1 casilla de su destino.
        int[][] estado = tableroResuelto();
        estado[0][0] = 2;
        estado[0][1] = 1;

        HeuristicaIA ia = nuevaHeuristica();
        assertEquals(2, ia.calcularHeuristica(estado));
    }

    @Test
    void heuristicaDeUnMovimientoValidoEsUno() {
        int[][] estado = tableroResuelto();
        // Intercambiar el espacio vacío (3,3) con la pieza 15 (3,2):
        // un movimiento legal de distancia 1.
        estado[3][3] = 15;
        estado[3][2] = 0;

        HeuristicaIA ia = nuevaHeuristica();
        assertEquals(1, ia.calcularHeuristica(estado));
    }

    // -----------------------------------------------------------------
    // Solucionabilidad (paridad de inversiones)
    // -----------------------------------------------------------------

    @Test
    void tableroResueltoEsSolucionable() {
        HeuristicaIA ia = nuevaHeuristica();
        assertTrue(ia.esSolucionable(tableroResuelto()));
    }

    @Test
    void unSoloMovimientoLegalSigueSiendoSolucionable() {
        int[][] estado = tableroResuelto();
        estado[3][3] = 15;
        estado[3][2] = 0;

        HeuristicaIA ia = nuevaHeuristica();
        assertTrue(ia.esSolucionable(estado));
    }

    @Test
    void intercambiarDosPiezasSinMoverElVacioEsNoSolucionable() {
        // Caso clásico: intercambiar las piezas 14 y 15 dejando el resto
        // (incluido el espacio vacío) igual. Esto cambia la paridad de
        // inversiones en 1 sin mover el vacío, produciendo una
        // configuración con la misma clase de posición para el vacío pero
        // paridad de inversiones opuesta: no solucionable.
        int[][] estado = tableroResuelto();
        estado[3][0] = 14;
        estado[3][1] = 13;

        HeuristicaIA ia = nuevaHeuristica();
        assertFalse(ia.esSolucionable(estado));
    }

    @RepeatedTest(30)
    void todoTableroGeneradoPorLaAppEsSolucionableSinImportarLaFilaDelVacio() {
        // Tablero#mezclarTablero() siempre parte del estado resuelto y
        // aplica únicamente movimientos legales, así que el resultado debe
        // ser solucionable sin importar en qué fila termine el espacio
        // vacío. Se repite para cubrir las 4 filas posibles.
        Tablero tablero = new Tablero();
        HeuristicaIA ia = new HeuristicaIA(tablero);
        assertTrue(ia.esSolucionable(tablero.getMatriz()));
    }

    // -----------------------------------------------------------------
    // estaResuelto
    // -----------------------------------------------------------------

    @Test
    void estaResueltoReconoceElEstadoObjetivo() {
        HeuristicaIA ia = nuevaHeuristica();
        assertTrue(ia.estaResuelto(tableroResuelto()));
    }

    @Test
    void estaResueltoRechazaUnaSolaPiezaFueraDeLugar() {
        int[][] estado = tableroResuelto();
        estado[0][0] = 2;
        estado[0][1] = 1;

        HeuristicaIA ia = nuevaHeuristica();
        assertFalse(ia.estaResuelto(estado));
    }

    // -----------------------------------------------------------------
    // resolverPuzzle: comportamiento de extremo a extremo
    // -----------------------------------------------------------------

    @Test
    void tableroYaResueltoDevuelveListaVacia() {
        Tablero tablero = new Tablero();
        tablero.ordenarTablero();

        HeuristicaIA ia = new HeuristicaIA(tablero);
        List<int[][]> solucion = ia.resolverPuzzle();

        assertEquals(0, solucion.size());
    }

    @Test
    void tableroAUnMovimientoSeResuelveEnUnPaso() {
        Tablero tablero = new Tablero();
        tablero.ordenarTablero();
        tablero.moverPieza(3, 2); // deja el tablero a 1 movimiento del objetivo

        HeuristicaIA ia = new HeuristicaIA(tablero);
        List<int[][]> solucion = ia.resolverPuzzle();

        assertEquals(2, solucion.size(), "estado inicial + 1 movimiento");
        assertTrue(ia.estaResuelto(solucion.get(solucion.size() - 1)));
    }

    @Test
    void tableroNoSolucionableNoProduceUnaFalsaSolucion() {
        // Construye directamente (sin pasar por el generador válido) una
        // configuración con paridad de inversiones incorrecta.
        int[][] estado = tableroResuelto();
        estado[3][0] = 14;
        estado[3][1] = 13; // ver intercambiarDosPiezasSinMoverElVacioEsNoSolucionable

        Tablero tablero = new Tablero();
        tablero.setMatriz(estado);

        HeuristicaIA ia = new HeuristicaIA(tablero);
        List<int[][]> solucion = ia.resolverPuzzle();

        // Debe reconocer que no es solucionable y no inventar una
        // "solución" sobre un tablero que nunca podrá quedar resuelto.
        assertEquals(0, solucion.size());
    }

    @RepeatedTest(15)
    void tablerosMezcladosAleatoriamenteSeResuelvenCorrectamente() {
        // Regresión del bug principal: la implementación original producía
        // un tablero final NO resuelto en el 92% de los casos porque
        // agotaba el presupuesto de A* y caía a un solucionador de
        // respaldo roto. IDA* debe resolver siempre un tablero generado
        // por el mezclador real de la aplicación.
        Tablero tablero = new Tablero(); // ya mezclado de forma válida por el constructor
        HeuristicaIA ia = new HeuristicaIA(tablero);

        List<int[][]> solucion = ia.resolverPuzzle();

        assertFalse(solucion.isEmpty(), "todo tablero generado por la app es solucionable");
        int[][] estadoFinal = solucion.get(solucion.size() - 1);
        assertTrue(ia.estaResuelto(estadoFinal),
                "el último estado de la solución debe ser el tablero resuelto");
    }
}
