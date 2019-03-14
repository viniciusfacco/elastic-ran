/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticGrain;

/**
 *
 * @author leandro.andrioli
 */
public enum GrainFunctionEnum {
     Linear(1), Quadratico(2), Exponencial(3);
     
     private final int valor;
     
    GrainFunctionEnum(int valorOpcao){
        valor = valorOpcao;
    }
    public int getValor(){
        return valor;
    }
}
