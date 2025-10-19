import java.util.Scanner;

public class Ejercicio2 {

    Scanner scanner = new Scanner(System.in);
   /* Escribe un programa que pida dos números al usuario y muestre el resultado de todas las comparaciones relacionales
    entre ellos (mayor que, menor que, igual, diferente, mayor o igual, menor o igual).
    Ejemplo de salida por consola:
    Introduce el primer número: 10
    Introduce el segundo número: 10
            ¿10 es mayor que 10?: false
            ¿10 es menor que 10?: false
            ¿10 es igual a 10?: true
            ¿10 es diferente de 10?: false
            ¿10 es mayor o igual que 10?: true
            ¿10 es menor o igual que 10?: true*/

    //Solicitamos al programa 2 números
    public void ejercicio2(){
        System.out.println("Introduce el primer número: ");
        int num1 = scanner.nextInt();
        System.out.println("Introduce el segundo número: ");
        int num2 = scanner.nextInt();

        //Ahora operamos con comparaciones relacionales

        System.out.println("¿"+num1+ " es mayor que " +num2 + "?: " +( num1>num2));
        System.out.println("¿"+num1+ " es menor que " +num2 + "?: " + ( num1<num2));
        System.out.println("¿"+num1+ " es igual que " +num2 + "?: " + ( num1==num2));
        System.out.println("¿"+num1+ " es diferente a " +num2 + "?: " +( num1!=num2));
        System.out.println("¿"+num1+ " es mayor o igual que " +num2 + "?: " +( num1>=num2));
        System.out.println("¿"+num1+ " es menor o igual que " +num2 + "?: " +( num1<=num2));





    }






}
