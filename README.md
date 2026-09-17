# 🧩 PuzzleMaestro

> Un 15-puzzle (tablero deslizante 4x4) con resolución automática por IDA*
> y heurística de Manhattan + conflicto lineal: encuentra siempre la
> solución óptima, incluso en las configuraciones más difíciles, con una
> interfaz de escritorio en Java Swing. Pensado para quien quiera ver un
> algoritmo de búsqueda clásico de IA funcionando de verdad, no solo en
> teoría.

## Características

- **Tablero 4x4 siempre solucionable**: cada partida nueva se genera con
  una caminata aleatoria de movimientos válidos desde el estado resuelto
  (no una permutación al azar), así que nunca le toca al jugador una
  configuración imposible.
- **Resolución automática con IDA\*** (Iterative Deepening A*): usa la
  distancia de Manhattan más el conflicto lineal como heurística
  admisible, por lo que la solución encontrada es siempre la más corta
  posible (óptima), no una aproximación.
- **Verificación de solucionabilidad** por paridad de inversiones, como
  red de seguridad adicional antes de intentar resolver.
- **Juego manual**: mueve las piezas con el mouse; los movimientos
  inválidos dan retroalimentación visual inmediata.
- **Animación paso a paso** de la solución encontrada, con barra de
  progreso y contador de movimientos.
- **Interfaz sin bloqueos**: la búsqueda corre en un hilo de fondo
  (`SwingWorker`) mientras se muestra un diálogo de progreso.

## Cómo usar

1. Se abre un tablero 4x4 ya mezclado (siempre solucionable).
2. Juega manualmente haciendo clic en una pieza adyacente al espacio
   vacío, o pulsa **Resolver IA** para ver la solución óptima calculada
   y animada paso a paso.
3. Pulsa **Nuevo Puzzle** para mezclar de nuevo.

## Instalación y uso local

Requiere JDK 17 o superior.

**Con Maven** (recomendado — compila, corre las pruebas y empaqueta):

```bash
mvn package
java -jar target/puzzlemaestro-1.0.0.jar
```

**Sin Maven, con javac directamente** (Windows, doble clic en `run.bat`,
o manualmente en cualquier sistema):

```bash
javac -d bin src/logica/*.java src/main/*.java src/presentacion/*.java
java -cp bin main.Main
```

## Tecnologías

- **Java 17+**, interfaz gráfica con **Swing** (sin dependencias de UI
  externas).
- **Maven** para la gestión de dependencias, pruebas y empaquetado.
- **JUnit 5** para las pruebas automatizadas.
- **GitHub Actions** para integración continua.

## Algoritmo

- **Heurística**: distancia de Manhattan (suma, por cada pieza, de la
  distancia en filas más columnas hasta su casilla objetivo, excluyendo
  el espacio vacío) más conflicto lineal (Hansson, Mayer & Yung, 1992):
  dos piezas que ya están en su fila/columna objetivo pero en el orden
  contrario suman 2 movimientos extra, porque una de ellas tendrá que
  salir y volver a entrar. Ambos términos combinados siguen siendo
  admisibles (nunca sobreestiman el costo real), así que IDA* garantiza
  la solución más corta.
- **Búsqueda**: IDA* explora en profundidad con un umbral de f = g + h
  que se incrementa progresivamente, en vez de mantener en memoria un
  conjunto abierto que puede crecer sin límite (la limitación clásica de
  A* en tableros difíciles). El consumo de memoria es proporcional a la
  profundidad de la solución, no a la cantidad de estados visitados.
- **Límites de seguridad**: cota teórica de 80 movimientos (el "número de
  Dios" del 15-puzzle), más un presupuesto de tiempo, para no bloquear la
  interfaz ante un estado inesperado.

## Tests

101 pruebas con JUnit 5: la distancia de Manhattan (valor en el objetivo,
exclusión del espacio vacío, casos de una y varias piezas desplazadas),
el detector de solucionabilidad (tablero resuelto, a un movimiento,
un caso construido a mano que debe reportarse como no solucionable, y
tableros reales generados por el mezclador de la app), y el
solucionador de principio a fin (tablero ya resuelto, a un movimiento
del objetivo, un tablero no solucionable que no debe producir una
solución falsa, y 15 repeticiones resolviendo tableros mezclados al
azar verificando que el estado final quede realmente resuelto).

```bash
mvn test
```

## Licencia

MIT — ver [LICENSE](LICENSE).
