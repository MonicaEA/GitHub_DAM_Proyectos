
import java.util.Scanner;

public class LogicaTransiccion {

    // CORRECCIÓN 1: Todo el código ejecutable debe ir dentro de main
    public void banco() {
        // CORRECCIÓN 2: Declaración e inicialización del Scanner
        Scanner scanner = new Scanner(System.in);

        System.out.println("Introduce el saldo inicial (ej: 500.00):");
        double saldoInicial = scanner.nextDouble();

        System.out.println("Introduce el monto del depósito (ej: 250.00):");
        double deposito = scanner.nextDouble();

        System.out.println("Introduce el monto del retiro (ej: 100.00):");
        double retiro = scanner.nextDouble();

        double saldoTotal = (saldoInicial + deposito) - retiro;

        System.out.println("Saldo Final: " + saldoTotal);

        // LÓGICA CORRECTA: if-else con operador &&
        if (saldoTotal > 600 && deposito >= retiro) {
            System.out.println("Transacción Exitosa y Saldo Elevado.");
        } else { // CORRECCIÓN 3: else no lleva paréntesis
            System.out.println("Revisión de Saldo o Transacción.");
        }

        scanner.close(); // Buena práctica
    }
}