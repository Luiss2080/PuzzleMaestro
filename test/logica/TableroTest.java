package logica;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableroTest {

    @Test
    void unTableroNuevoOrdenadoEstaResuelto() {
        Tablero tablero = new Tablero();
        tablero.ordenarTablero();
        assertTrue(tablero.estaResuelto());
    }

    @Test
    void moverPiezaFueraDeRangoNoAlteraElTablero() {
        Tablero tablero = new Tablero();
        tablero.ordenarTablero();
        int[][] antes = tablero.getMatriz();

        assertFalse(tablero.moverPieza(-1, 0));
        assertFalse(tablero.moverPieza(0, 99));

        assertArrayEquals(antes, tablero.getMatriz());
    }

    @Test
    void moverPiezaNoAdyacenteAlVacioEsInvalido() {
        Tablero tablero = new Tablero();
        tablero.ordenarTablero(); // vacío en (3,3)
        // (0,0) no es adyacente a (3,3)
        assertFalse(tablero.moverPieza(0, 0));
    }

    @Test
    void moverPiezaAdyacenteAlVacioLaIntercambiaConElVacio() {
        Tablero tablero = new Tablero();
        tablero.ordenarTablero(); // vacío en (3,3), pieza 15 en (3,2)

        assertTrue(tablero.moverPieza(3, 2));
        assertEquals(15, tablero.getValor(3, 3));
        assertEquals(0, tablero.getValor(3, 2));
        assertEquals(3, tablero.getEspacioVacioX());
        assertEquals(2, tablero.getEspacioVacioY());
    }

    @RepeatedTest(20)
    void mezclarTableroSiemprePreservaElConjuntoDePiezas() {
        // El mezclador debe seguir siendo una permutación válida de
        // 0..15: ninguna pieza se duplica ni desaparece.
        Tablero tablero = new Tablero();
        boolean[] vistos = new boolean[16];
        int[][] matriz = tablero.getMatriz();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int valor = matriz[i][j];
                assertFalse(vistos[valor], "valor duplicado tras mezclar: " + valor);
                vistos[valor] = true;
            }
        }
        for (boolean visto : vistos) {
            assertTrue(visto);
        }
    }

    @RepeatedTest(20)
    void mezclarTableroDejaElEspacioVacioConsistenteConLaMatriz() {
        Tablero tablero = new Tablero();
        assertEquals(0, tablero.getValor(tablero.getEspacioVacioX(), tablero.getEspacioVacioY()));
    }
}
