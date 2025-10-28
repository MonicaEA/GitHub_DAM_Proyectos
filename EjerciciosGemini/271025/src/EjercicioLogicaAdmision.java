import java.util.Scanner;


public class EjercicioLogicaAdmision {

    public void logicaAdmision(){
        int edadEstudiante = 16;
        int puntuacionExam = 85;

        if (edadEstudiante >= 18 && puntuacionExam >=75){
            System.out.println("Felicidades alumno admitido");
        }
        else
            System.out.println("No cumple requisitos");


    }

}
