public class Semaforo {

    public void semaforo (){

        String colorActual = "amarillo";

        switch (colorActual) {
            case "rojo":
                System.out.println("DETENTE");
                break; // ¡NECESARIO! Sale del switch
            case "verde":
                System.out.println("ADELANTE");
                break;
            case "amarillo":
                System.out.println("PRECAUCION");
                break;
            default:
                System.out.println("Introduce valores validos");
        }

    }
}
