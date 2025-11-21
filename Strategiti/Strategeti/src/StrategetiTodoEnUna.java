import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.*;

public class StrategetiTodoEnUna extends Application {

    private static final int SIZE = 4;
    private static final int CELL_SIZE = 165;    // 25% menos del doble
    private static final int PIEZA_SIZE = 128;
    private static final int RESERVA_SIZE = 90;

    private Tablero tablero;
    private GridPane grid;
    private VBox reservaIzq, reservaDer;
    private Label turnoLabel, contadorLabel;
    private Button pasarTurnoBtn;

    private int jugadorActivo = 1;
    private int origenDragFila = -1, origenDragCol = -1;
    private int gacelaSaltandoRow = -1, gacelaSaltandoCol = -1;

    // Contador de fichas eliminadas
    private int fichasEliminadasJ1 = 0, fichasEliminadasJ2 = 0;

    private Map<Integer, List<Animal>> reservas = new HashMap<>();

    private boolean juegoFinalizado = false;

    @Override
    public void start(Stage primaryStage) {
        tablero = new Tablero(SIZE);
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        reservaIzq = new VBox(15);
        reservaIzq.setAlignment(Pos.CENTER);
        reservaDer = new VBox(15);
        reservaDer.setAlignment(Pos.CENTER);

        turnoLabel = new Label();
        turnoLabel.setFont(Font.font("Arial", 38));
        turnoLabel.setTextFill(Color.rgb(60, 60, 180));
        turnoLabel.setPadding(new Insets(0,0,5,0));

        contadorLabel = new Label();
        contadorLabel.setFont(Font.font("Arial", 26));
        contadorLabel.setTextFill(Color.web("#444"));

        pasarTurnoBtn = new Button("PASAR TURNO");
        pasarTurnoBtn.setFont(Font.font("Arial", 26));
        pasarTurnoBtn.setOnAction(e -> {
            if (juegoFinalizado) return;
            gacelaSaltandoRow = -1;
            gacelaSaltandoCol = -1;
            cambiarTurno();
            refrescarTodo();
            comprobarVictoria();
        });
        pasarTurnoBtn.setPadding(new Insets(15));

        crearReservasIniciales();
        refrescarTodo();

        HBox tableroCentral = new HBox(32, reservaIzq, grid, reservaDer);
        tableroCentral.setAlignment(Pos.CENTER);
        VBox root = new VBox(12, turnoLabel, contadorLabel, tableroCentral, pasarTurnoBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(16));

        Scene scene = new Scene(root, SIZE * CELL_SIZE + 440, SIZE * CELL_SIZE + 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Strategeti");
        primaryStage.show();
    }

    private void refrescarTodo() {
        dibujarTablero();
        refrescarReserva();
        actualizarTurnoLabel();
        actualizarContador();
        pasarTurnoBtn.setDisable(juegoFinalizado);
    }

    private void crearReservasIniciales() {
        reservas.put(1, new ArrayList<>(Arrays.asList(
                new Elefante(1), new Elefante(1),
                new Leon(1), new Leon(1),
                new Cebra(1), new Cebra(1),
                new Gacela(1), new Gacela(1)
        )));
        reservas.put(2, new ArrayList<>(Arrays.asList(
                new Elefante(2), new Elefante(2),
                new Leon(2), new Leon(2),
                new Cebra(2), new Cebra(2),
                new Gacela(2), new Gacela(2)
        )));
    }

    private void dibujarTablero() {
        grid.getChildren().clear();
        for (int fila = 0; fila < SIZE; fila++) {
            for (int col = 0; col < SIZE; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle((fila + col) % 2 == 0 ?
                        "-fx-background-color: #fcf3ce; -fx-border-color: #886e25;" :
                        "-fx-background-color: #f2dea8; -fx-border-color: #886e25;");
                grid.add(cell, col, fila);

                if (esEsquina(fila, col)) {
                    Text x = new Text("X");
                    x.setFill(Color.RED);
                    x.setFont(Font.font("Arial Black", 55));
                    cell.getChildren().add(x);
                }

                final int f = fila, c = col;

                cell.setOnDragOver(event -> {
                    if (juegoFinalizado) return;
                    Dragboard db = event.getDragboard();
                    boolean aceptar = false;
                    if (db.hasString()) {
                        if (origenDragFila == -1) {
                            aceptar = !esEsquina(f, c) && tablero.getAnimal(f, c) == null;
                        } else {
                            Animal moving = tablero.getAnimal(origenDragFila, origenDragCol);
                            if (moving != null && moving.getJugador() == jugadorActivo
                                    && (gacelaSaltandoRow == -1 ||
                                    (moving instanceof Gacela && gacelaSaltandoRow == origenDragFila && gacelaSaltandoCol == origenDragCol))) {
                                if (moving instanceof Elefante) aceptar = movimientoElefanteValido(origenDragFila, origenDragCol, f, c);
                                if (moving instanceof Leon)     aceptar = movimientoLeonValido(origenDragFila, origenDragCol, f, c);
                                if (moving instanceof Cebra)    aceptar = movimientoCebraValido(origenDragFila, origenDragCol, f, c);
                                if (moving instanceof Gacela)   aceptar = movimientoGacelaValido(origenDragFila, origenDragCol, f, c);
                            }
                        }
                        if (aceptar) event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                cell.setOnDragEntered(event -> {
                    if (juegoFinalizado) return;
                    Dragboard db = event.getDragboard();
                    boolean aceptar = false;
                    if (db.hasString()) {
                        if (origenDragFila == -1) {
                            aceptar = !esEsquina(f, c) && tablero.getAnimal(f, c) == null;
                        } else {
                            Animal moving = tablero.getAnimal(origenDragFila, origenDragCol);
                            if (moving != null && moving.getJugador() == jugadorActivo
                                    && (gacelaSaltandoRow == -1 ||
                                    (moving instanceof Gacela && gacelaSaltandoRow == origenDragFila && gacelaSaltandoCol == origenDragCol))) {
                                if (moving instanceof Elefante) aceptar = movimientoElefanteValido(origenDragFila, origenDragCol, f, c);
                                if (moving instanceof Leon)     aceptar = movimientoLeonValido(origenDragFila, origenDragCol, f, c);
                                if (moving instanceof Cebra)    aceptar = movimientoCebraValido(origenDragFila, origenDragCol, f, c);
                                if (moving instanceof Gacela)   aceptar = movimientoGacelaValido(origenDragFila, origenDragCol, f, c);
                            }
                        }
                    }
                    if (aceptar)
                        cell.setStyle("-fx-background-color: #fff7c2; -fx-border-color: #8e793e;");
                    event.consume();
                });

                cell.setOnDragExited(event -> {
                    cell.setStyle((f + c) % 2 == 0 ?
                            "-fx-background-color: #fcf3ce; -fx-border-color: #886e25;" :
                            "-fx-background-color: #f2dea8; -fx-border-color: #886e25;");
                    event.consume();
                });

                cell.setOnDragDropped(event -> {
                    if (juegoFinalizado) return;
                    Dragboard db = event.getDragboard();
                    if (esEsquina(f, c) && origenDragFila == -1) {
                        mostrarAlerta("No puedes colocar una ficha en la esquina.");
                        event.setDropCompleted(false);
                        event.consume();
                        return;
                    }
                    if (db.hasString()) {
                        String dato = db.getString();
                        if (origenDragFila == -1) {
                            if (tablero.getAnimal(f, c) != null) {
                                mostrarAlerta("Ya hay una ficha en esa casilla.");
                                event.setDropCompleted(false);
                            } else {
                                String tipo = dato;
                                Animal fichanueva = crearAnimalSegunTipo(tipo, jugadorActivo);
                                tablero.colocarAnimal(f, c, fichanueva);
                                reservas.get(jugadorActivo).removeIf(a -> a.getClass().getSimpleName().equals(tipo));
                                cambiarTurno();
                                refrescarTodo();
                                comprobarVictoria();
                                event.setDropCompleted(true);
                            }
                        } else {
                            Animal moving = tablero.getAnimal(origenDragFila, origenDragCol);
                            boolean puedeMover = (gacelaSaltandoRow == -1 ||
                                    (moving instanceof Gacela &&
                                            gacelaSaltandoRow == origenDragFila && gacelaSaltandoCol == origenDragCol));

                            if (!puedeMover) {
                                mostrarAlerta("Solo puedes mover la gacela que está en salto encadenado.");
                                event.setDropCompleted(false);
                                return;
                            }

                            if (moving.getJugador() != jugadorActivo) {
                                mostrarAlerta("Solo puedes mover tus propias fichas.");
                                event.setDropCompleted(false);
                            } else if (moving instanceof Elefante) {
                                if (movimientoElefanteValido(origenDragFila, origenDragCol, f, c)) {
                                    boolean eliminado = gestionarEmpujeElefante(origenDragFila, origenDragCol, f, c);
                                    cambiarTurno();
                                    refrescarTodo();
                                    if (eliminado) actualizarContador();
                                    comprobarVictoria();
                                    event.setDropCompleted(true);
                                } else {
                                    mostrarAlerta("Movimiento de elefante no válido.");
                                    event.setDropCompleted(false);
                                }
                            } else if (moving instanceof Leon) {
                                if (movimientoLeonValido(origenDragFila, origenDragCol, f, c)) {
                                    gestionarCapturaLeon(origenDragFila, origenDragCol, f, c);
                                    cambiarTurno();
                                    refrescarTodo();
                                    comprobarVictoria();
                                    event.setDropCompleted(true);
                                } else {
                                    mostrarAlerta("Movimiento de león no válido.");
                                    event.setDropCompleted(false);
                                }
                            } else if (moving instanceof Cebra) {
                                if (movimientoCebraValido(origenDragFila, origenDragCol, f, c)) {
                                    tablero.colocarAnimal(f, c, moving);
                                    tablero.colocarAnimal(origenDragFila, origenDragCol, null);
                                    cambiarTurno();
                                    refrescarTodo();
                                    comprobarVictoria();
                                    event.setDropCompleted(true);
                                } else {
                                    mostrarAlerta("Movimiento de cebra no válido.");
                                    event.setDropCompleted(false);
                                }
                            } else if (moving instanceof Gacela) {
                                if (movimientoGacelaValido(origenDragFila, origenDragCol, f, c)) {
                                    tablero.colocarAnimal(f, c, moving);
                                    tablero.colocarAnimal(origenDragFila, origenDragCol, null);
                                    if (haySaltoGacelaDesde(f, c)) {
                                        gacelaSaltandoRow = f;
                                        gacelaSaltandoCol = c;
                                        refrescarTodo();
                                    } else {
                                        gacelaSaltandoRow = -1;
                                        gacelaSaltandoCol = -1;
                                        cambiarTurno();
                                        refrescarTodo();
                                        comprobarVictoria();
                                    }
                                    event.setDropCompleted(true);
                                } else {
                                    mostrarAlerta("Movimiento de gacela no válido.");
                                    event.setDropCompleted(false);
                                }
                            }
                        }
                        origenDragFila = -1;
                        origenDragCol = -1;
                    } else {
                        event.setDropCompleted(false);
                    }
                    event.consume();
                });

                Animal animal = tablero.getAnimal(fila, col);
                if (animal != null) {
                    agregarImagenMovible(cell, animal, fila, col);
                }
            }
        }
    }

    // Borde de color e indicador para identificar jugador
    private void agregarImagenMovible(Pane cell, Animal animal, int fila, int col) {
        ImageView im = new ImageView(new Image(animal.getRutaImagen()));
        im.setFitWidth(PIEZA_SIZE);
        im.setFitHeight(PIEZA_SIZE);
        Circle borde = new Circle(PIEZA_SIZE / 2 + 9);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(animal.getJugador() == 1 ? Color.DODGERBLUE : Color.CRIMSON);
        borde.setStrokeWidth(11);

        Circle indicador = new Circle(22, animal.getJugador() == 1 ? Color.DODGERBLUE : Color.CRIMSON);
        indicador.setStroke(Color.WHITE);
        indicador.setStrokeWidth(3);
        indicador.setTranslateX(PIEZA_SIZE/2 - 28);
        indicador.setTranslateY(PIEZA_SIZE/2 - 28);

        StackPane stk = new StackPane(borde, im, indicador);
        stk.setPrefSize(PIEZA_SIZE+18, PIEZA_SIZE+18);

        if (animal.getJugador() == jugadorActivo &&
                (gacelaSaltandoRow == -1 ||
                        (animal instanceof Gacela && fila == gacelaSaltandoRow && col == gacelaSaltandoCol))) {
            stk.setOnDragDetected(e -> {
                origenDragFila = fila;
                origenDragCol = col;
                Dragboard db = stk.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(animal.getClass().getSimpleName());
                db.setContent(content);
                e.consume();
            });
        }
        cell.getChildren().add(stk);
    }

    private void refrescarReserva() {
        reservaIzq.getChildren().clear();
        reservaDer.getChildren().clear();
        Map<String, Integer> cJ1 = cuentaReserva(1);
        Map<String, Integer> cJ2 = cuentaReserva(2);
        for (String tipo : Arrays.asList("Elefante", "Leon", "Cebra", "Gacela")) {
            int cantidad2 = cJ2.getOrDefault(tipo, 0);
            for (int i = 0; i < cantidad2; i++) {
                Animal muestra = crearAnimalSegunTipo(tipo, 2);
                reservaIzq.getChildren().add(crearFichaReserva(muestra));
            }
        }
        for (String tipo : Arrays.asList("Elefante", "Leon", "Cebra", "Gacela")) {
            int cantidad1 = cJ1.getOrDefault(tipo, 0);
            for (int i = 0; i < cantidad1; i++) {
                Animal muestra = crearAnimalSegunTipo(tipo, 1);
                reservaDer.getChildren().add(crearFichaReserva(muestra));
            }
        }
    }

    private StackPane crearFichaReserva(Animal muestra) {
        ImageView img = new ImageView(new Image(muestra.getRutaImagen()));
        img.setFitWidth(RESERVA_SIZE);
        img.setFitHeight(RESERVA_SIZE);
        Circle borde = new Circle(RESERVA_SIZE / 2 + 8);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(muestra.getJugador() == 1 ? Color.DODGERBLUE : Color.CRIMSON);
        borde.setStrokeWidth(7);
        StackPane stk = new StackPane(borde, img);
        stk.setPrefSize(RESERVA_SIZE+14, RESERVA_SIZE+14);

        if (muestra.getJugador() == jugadorActivo && gacelaSaltandoRow == -1) {
            stk.setOnDragDetected(e -> {
                origenDragFila = -1;
                origenDragCol = -1;
                Dragboard db = stk.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(muestra.getClass().getSimpleName());
                db.setContent(content);
                e.consume();
            });
        }
        return stk;
    }

    private Map<String, Integer> cuentaReserva(int jugador) {
        Map<String, Integer> cuenta = new LinkedHashMap<>();
        for (Animal a : reservas.get(jugador)) {
            String tipo = a.getClass().getSimpleName();
            cuenta.put(tipo, cuenta.getOrDefault(tipo, 0) + 1);
        }
        return cuenta;
    }

    private Animal crearAnimalSegunTipo(String tipo, int jugador) {
        return switch (tipo) {
            case "Elefante" -> new Elefante(jugador);
            case "Leon" -> new Leon(jugador);
            case "Cebra" -> new Cebra(jugador);
            case "Gacela" -> new Gacela(jugador);
            default -> null;
        };
    }

    private void cambiarTurno() {
        jugadorActivo = 3 - jugadorActivo;
        gacelaSaltandoRow = -1;
        gacelaSaltandoCol = -1;
    }

    private void actualizarTurnoLabel() {
        String color = jugadorActivo == 1 ? "#2376da" : "#db2228";
        turnoLabel.setText("Turno: Jugador " + jugadorActivo);
        turnoLabel.setTextFill(Color.web(color));
    }

    private void actualizarContador() {
        contadorLabel.setText("Fichas eliminadas:  J1 = " + fichasEliminadasJ1 + "    J2 = " + fichasEliminadasJ2);
    }

    // --- Condiciones de victoria ---
    private void comprobarVictoria() {
        if (juegoFinalizado) return;
        // 1) Eliminar 5 fichas rivales
        if (fichasEliminadasJ1 >= 5) {
            juegoFinalizado = true;
            mostrarVictoria(1,"ha eliminado 5 fichas");
            return;
        }
        if (fichasEliminadasJ2 >= 5) {
            juegoFinalizado = true;
            mostrarVictoria(2,"ha eliminado 5 fichas");
            return;
        }
        // 2) 4 en línea
        int ganadorLinea = comprobarCuatroEnLinea();
        if (ganadorLinea > 0) {
            juegoFinalizado = true;
            mostrarVictoria(ganadorLinea, "tiene 4 fichas en línea");
            return;
        }
        // 3) Jugador rival totalmente bloqueado
        if (estaBloqueado(3 - jugadorActivo)) {
            juegoFinalizado = true;
            mostrarVictoria(jugadorActivo, "ha dejado al rival bloqueado");
        }
    }

    private void mostrarVictoria(int ganador, String motivo) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText("¡FIN DE PARTIDA!");
        alerta.setContentText("Gana el jugador " + ganador + "\n(" + motivo + ")");
        alerta.showAndWait();
    }

    // Devuelve el jugador ganador, 0 si nadie
    private int comprobarCuatroEnLinea() {
        for (int j = 1; j <= 2; j++) {
            // Horizontal y vertical
            for (int i = 0; i < SIZE; i++) {
                boolean hor = true, ver = true;
                for (int k = 0; k < SIZE; k++) {
                    if (tablero.getAnimal(i, k) == null || tablero.getAnimal(i, k).getJugador() != j) hor = false;
                    if (tablero.getAnimal(k, i) == null || tablero.getAnimal(k, i).getJugador() != j) ver = false;
                }
                if (hor || ver) return j;
            }
            // Diagonal
            boolean diag1 = true, diag2 = true;
            for (int k = 0; k < SIZE; k++) {
                if (tablero.getAnimal(k, k) == null || tablero.getAnimal(k, k).getJugador() != j) diag1 = false;
                if (tablero.getAnimal(k, SIZE - 1 - k) == null || tablero.getAnimal(k, SIZE - 1 - k).getJugador() != j) diag2 = false;
            }
            if (diag1 || diag2) return j;
        }
        return 0;
    }

    // Si nadie puede mover ni colocar
    private boolean estaBloqueado(int jugador) {
        // ¿Puede colocar?
        if (!reservas.get(jugador).isEmpty()) {
            for (int fila = 0; fila < SIZE; fila++)
                for (int col = 0; col < SIZE; col++)
                    if (!esEsquina(fila, col) && tablero.getAnimal(fila, col) == null)
                        return false;
        }
        // ¿Puede mover algo?
        for (int fila = 0; fila < SIZE; fila++) {
            for (int col = 0; col < SIZE; col++) {
                Animal a = tablero.getAnimal(fila, col);
                if (a != null && a.getJugador() == jugador
                        && (puedeMoverAnimal(a, fila, col)))
                    return false;
            }
        }
        return true;
    }
    private boolean puedeMoverAnimal(Animal a, int fila, int col) {
        for (int df = -3; df <= 3; df++) {
            for (int dc = -3; dc <= 3; dc++) {
                int f2 = fila + df, c2 = col + dc;
                if (!enRango(f2, c2)) continue;
                if (a instanceof Elefante && movimientoElefanteValido(fila, col, f2, c2)) return true;
                if (a instanceof Leon     && movimientoLeonValido(fila, col, f2, c2)) return true;
                if (a instanceof Cebra    && movimientoCebraValido(fila, col, f2, c2)) return true;
                if (a instanceof Gacela   && movimientoGacelaValido(fila, col, f2, c2)) return true;
            }
        }
        return false;
    }

    private boolean esEsquina(int f, int c) {
        return (f == 0 || f == SIZE - 1) && (c == 0 || c == SIZE - 1);
    }
    private boolean enRango(int f, int c) {
        return f >= 0 && f < SIZE && c >= 0 && c < SIZE;
    }
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }

    // --- GESTIÓN FIEL DE CAPTURAS Y EMPUJES ---
    private boolean gestionarEmpujeElefante(int fromRow, int fromCol, int toRow, int toCol) {
        int dirRow = Integer.compare(toRow, fromRow);
        int dirCol = Integer.compare(toCol, fromCol);
        int r = fromRow + dirRow, c = fromCol + dirCol;
        while (enRango(r, c) && tablero.getAnimal(r, c) != null) {
            r += dirRow; c += dirCol;
        }
        boolean eliminado = false;
        if (!enRango(r, c)) {
            int lastRow = r - dirRow, lastCol = c - dirCol;
            Animal aElim = tablero.getAnimal(lastRow, lastCol);
            if (aElim != null) {
                if (aElim.getJugador() == 1) fichasEliminadasJ2++;
                else if (aElim.getJugador() == 2) fichasEliminadasJ1++;
                eliminado = true;
            }
            tablero.colocarAnimal(lastRow, lastCol, null);
        } else {
            for (int rr = r, cc = c; !(rr == fromRow + dirRow && cc == fromCol + dirCol); rr -= dirRow, cc -= dirCol) {
                tablero.colocarAnimal(rr, cc, tablero.getAnimal(rr - dirRow, cc - dirCol));
            }
        }
        tablero.colocarAnimal(toRow, toCol, tablero.getAnimal(fromRow, fromCol));
        tablero.colocarAnimal(fromRow, fromCol, null);
        return eliminado;
    }
    private void gestionarCapturaLeon(int fromRow, int fromCol, int toRow, int toCol) {
        Animal capturada = tablero.getAnimal(toRow, toCol);
        if (capturada != null) {
            if (capturada.getJugador() == 1) fichasEliminadasJ2++;
            else if (capturada.getJugador() == 2) fichasEliminadasJ1++;
        }
        tablero.colocarAnimal(toRow, toCol, tablero.getAnimal(fromRow, fromCol));
        tablero.colocarAnimal(fromRow, fromCol, null);
    }

    // --- RESTO DE LÓGICA DE ANIMALES ---
    private boolean movimientoElefanteValido(int fromRow, int fromCol, int toRow, int toCol) {
        int dr = toRow - fromRow;
        int dc = toCol - fromCol;
        if (!((Math.abs(dr) == 1 && dc == 0) || (Math.abs(dc) == 1 && dr == 0))) return false;
        int dirRow = Integer.compare(toRow, fromRow);
        int dirCol = Integer.compare(toCol, fromCol);
        int checkRow = fromRow + dirRow;
        int checkCol = fromCol + dirCol;
        if (!enRango(checkRow, checkCol)) return false;
        Animal empujada = tablero.getAnimal(checkRow, checkCol);
        if (empujada == null) return false;
        if (empujada instanceof Elefante) return false;
        int r = checkRow, c = checkCol;
        while (enRango(r, c) && tablero.getAnimal(r, c) != null) {
            if (tablero.getAnimal(r, c) instanceof Elefante) return false;
            r += dirRow;
            c += dirCol;
        }
        return true;
    }

    private boolean movimientoLeonValido(int fromRow, int fromCol, int toRow, int toCol) {
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        if (!((dr == 1 && dc == 0) || (dr == 0 && dc == 1))) return false;
        Animal destino = tablero.getAnimal(toRow, toCol);
        if (destino == null) return false;
        return (destino instanceof Cebra || destino instanceof Gacela);
    }

    private boolean movimientoCebraValido(int fromRow, int fromCol, int toRow, int toCol) {
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        if ((dr == 1 && dc == 0) || (dr == 0 && dc == 1)) {
            return tablero.getAnimal(toRow, toCol) == null;
        }
        return false;
    }

    private boolean movimientoGacelaValido(int fromRow, int fromCol, int toRow, int toCol) {
        Set<String> alcanzados = new HashSet<>();
        Queue<int[]> cola = new LinkedList<>();
        cola.add(new int[]{fromRow, fromCol});
        boolean[][] visitado = new boolean[SIZE][SIZE];
        visitado[fromRow][fromCol] = true;
        int[][] dirs = { {-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1} };
        while (!cola.isEmpty()) {
            int[] pos = cola.poll();
            for (int[] d : dirs) {
                int midR = pos[0] + d[0], midC = pos[1] + d[1];
                int destR = pos[0] + 2*d[0], destC = pos[1] + 2*d[1];
                if (enRango(midR,midC) && enRango(destR, destC)
                        && tablero.getAnimal(midR, midC) != null
                        && tablero.getAnimal(destR, destC) == null
                        && !visitado[destR][destC]) {
                    alcanzados.add(destR + "," + destC);
                    cola.add(new int[]{destR, destC});
                    visitado[destR][destC] = true;
                }
                int overR = pos[0] + 2*d[0], overC = pos[1] + 2*d[1];
                int destR2 = pos[0] + 3*d[0], destC2 = pos[1] + 3*d[1];
                if (enRango(midR,midC) && enRango(overR,overC) && enRango(destR2,destC2)
                        && tablero.getAnimal(midR, midC) != null
                        && tablero.getAnimal(overR, overC) != null
                        && tablero.getAnimal(destR2, destC2) == null
                        && !visitado[destR2][destC2]) {
                    alcanzados.add(destR2 + "," + destC2);
                    cola.add(new int[]{destR2, destC2});
                    visitado[destR2][destC2] = true;
                }
            }
        }
        return alcanzados.contains(toRow + "," + toCol);
    }

    private boolean haySaltoGacelaDesde(int fr, int fc) {
        int[][] dirs = { {-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1} };
        Animal gacela = tablero.getAnimal(fr, fc);
        if (!(gacela instanceof Gacela)) return false;
        for (int[] d : dirs) {
            int midR = fr + d[0], midC = fc + d[1];
            int destR = fr + 2*d[0], destC = fc + 2*d[1];
            if (enRango(midR,midC) && enRango(destR,destC)
                    && tablero.getAnimal(midR,midC)!=null
                    && tablero.getAnimal(destR,destC)==null) return true;
            int overR = fr + 2*d[0], overC = fc + 2*d[1];
            int destR2 = fr + 3*d[0], destC2 = fc + 3*d[1];
            if (enRango(midR,midC) && enRango(overR,overC) && enRango(destR2,destC2)
                    && tablero.getAnimal(midR,midC)!=null && tablero.getAnimal(overR,overC)!=null
                    && tablero.getAnimal(destR2,destC2)==null) return true;
        }
        return false;
    }

    // --- MODELO ---
    abstract class Animal {
        protected int jugador;
        public Animal(int jugador) { this.jugador = jugador; }
        public int getJugador() { return jugador; }
        public abstract String getRutaImagen();
    }
    class Elefante extends Animal {
        public Elefante(int jugador) { super(jugador); }
        public String getRutaImagen() { return "file:elefante.png"; }
    }
    class Leon extends Animal {
        public Leon(int jugador) { super(jugador);}
        public String getRutaImagen() { return "file:leon.png"; }
    }
    class Cebra extends Animal {
        public Cebra(int jugador) { super(jugador);}
        public String getRutaImagen() { return "file:cebra.png"; }
    }
    class Gacela extends Animal {
        public Gacela(int jugador) { super(jugador);}
        public String getRutaImagen() { return "file:gacela.png"; }
    }
    class Tablero {
        private Animal[][] matriz;
        public Tablero(int size) {
            matriz = new Animal[size][size];
        }
        public Animal getAnimal(int fila, int col) {
            return matriz[fila][col];
        }
        public void colocarAnimal(int fila, int col, Animal animal) {
            matriz[fila][col] = animal;
        }
    }
}

