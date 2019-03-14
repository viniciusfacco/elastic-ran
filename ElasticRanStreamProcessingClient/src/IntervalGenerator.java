
import java.util.Random;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author vinicius.rodrigues
 */
public class IntervalGenerator {

    private final Random gerador;
    
    public IntervalGenerator(){
        gerador = new Random();
    }
    
    //crescente
    public int generateAscending(int position, double y, int z){
        int resultado = (int) (position * y * z);
        return resultado;
    }
    
    //decrescente
    public int generateDescending(int position, float w, double y, int z){
        int resultado = (int) (w - (position * y * z));
        return resultado;
    }
    
    //onda
    public int generateWave(int position, double v, float w, double y, int z){
        int resultado = (int) ((v * z * Math.sin(y * position)) + (v * z + w));
        return resultado;
    }
    
    //exponenciais
    public int generateExponential(int position, double v, float w, double y, int z, String tipo){
        int resultado = 0;
        if (tipo.equalsIgnoreCase("ex+")){
            resultado = (int) (Math.exp((position / w) * y) + z);
        } else if (tipo.equalsIgnoreCase("ex-")){
            resultado = (int) (Math.exp(((v - position) / w) * y) + z);
        }
        return resultado;
    }
    
    public int generateRandomAll(){
        return gerador.nextInt(999900) + 100;
    }
    
    public int generateRandomAndConstant(int pos, int total, int fatias){
        //de todos os jobs, fatias é a quantidade vezes que o tipo do load vai trocar de ramdom pra constante
        int tamfatia = total / fatias; //quantidade de jobs em cada faixa
        int resultado = pos/tamfatia; //apenas para auxiliar, dividimos a posição atual pelo tamanho de cada faixa pra saber em que faixa estamos
        if (((resultado + 1) % 2) == 0){//se a faixa for par eu devolvo random, caso contratio devolvo constante
            resultado = gerador.nextInt(999900) + 100;
        } else {
            resultado = 500000;
        }
        return resultado;
    }
    
}
