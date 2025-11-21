import java.util.Scanner;

public class Bonoloto {

    private int[] numerosSistema = new int[5];
    private int[] numerosUsuario = new int[5];
    Scanner scanner = new Scanner(System.in);
    public void iniciarJuego() {
        // al iniciar el juego pasan 2 cosas
        // 1. sale de la maquina 5 nums que seran los numeros del sistema. Numeros del 0-20

        for (int i = 0; i < 5; i++) {
            numerosSistema[i] = (int) (Math.random() * 21);


        }

        // 2 se le piden al usuario 5 numeros
        System.out.println("Procedemos a sellar tu boleto");
        for (int i = 0; i < 5; i++) {
            int numeroIntroducido = 0;
            do {
                System.out.println("introduce el numero");
                if (numeroIntroducido<0 || numeroIntroducido>20)
                    System.out.println("incorrecto");
            }while(numeroIntroducido<0 || numeroIntroducido>20);
            numerosUsuario[i]=numeroIntroducido;
        }
    }

    public void listarNumerosSistema(){

        for (int item: numerosSistema ){
            System.out.println(item);

        }

        }

    public void listarNumerosUsuario(){
        for (int item : numerosUsuario){
            System.out.println(item);
        }

    }




    }

