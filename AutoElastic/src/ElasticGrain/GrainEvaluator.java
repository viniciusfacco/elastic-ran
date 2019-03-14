/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticGrain;

import middlewares.OneManager;
import thresholds.Thresholds;
import java.lang.Math;
import slas.WSAgreementSLA;


/**
 *
 * @author leandro.andrioli
 */
public class GrainEvaluator {
    private OneManager oneManager;
    private GrainFunctionEnum grainFunctionEnum;
    private boolean usarGraoElastico;
    private double percentualVariacaoGraoElasticoLinear;
    private float percentualVariacaoExponencial;
    private int quantidadeHostsCadastrados;
    private float newUsageAfterIncreases;
    private WSAgreementSLA sla;
    private int increase_grain_vms_size;
    private int decrease_grain_vms_size;
    private String reportGrain = "";
    
    private int iterationForExponentialFunction;
    
    public GrainEvaluator(OneManager om, GrainFunctionEnum gfe, boolean pusarGraoElastico, double ppercentualVariacaoGraoElasticoLinear, int pquantidadeHostsCadastrados, WSAgreementSLA psla){
        oneManager = om;
        grainFunctionEnum = gfe;
        usarGraoElastico = pusarGraoElastico;
        percentualVariacaoGraoElasticoLinear = ppercentualVariacaoGraoElasticoLinear;
        iterationForExponentialFunction = 1;
        quantidadeHostsCadastrados = pquantidadeHostsCadastrados;
        newUsageAfterIncreases = 0;
        percentualVariacaoExponencial = 0.06f;
        sla = psla;
        increase_grain_vms_size = 1;
        decrease_grain_vms_size = 1;
    } 
    
    public void computeElasticGrain(float lastDecisionCpuLoad, float lastDecisionMemLoad, float lastDecisionNetworkLoad,
            float currentDecisionCpuLoad, float currentDecisionMemLoad, float currentDecisionNetworkLoad, boolean increase)
    {
        if(usarGraoElastico && newUsageAfterIncreases > 0){
            String operacao = "";
            if(increase) operacao = "INCREASE";
            else operacao = "DECREASE";
            
            System.out.println("Vai calcular o grão elastico devido a um " + operacao + " | Atualmente esta em increase_grain_vms_size: " +  increase_grain_vms_size + " | decrease_grain_vms_size: " + decrease_grain_vms_size);
            System.out.println("Percentual de aumento ocorrido: " + String.valueOf(lastDecisionCpuLoad - currentDecisionCpuLoad) + "Percentual definido SLA: " + percentualVariacaoGraoElasticoLinear);
            
            //float percentualVariacao = Math.abs(newUsageAfterIncreases - currentDecisionCpuLoad);
            //if (percentualVariacao < 0.001f)
             //   percentualVariacao = Math.abs(lastDecisionCpuLoad - currentDecisionCpuLoad);
             float percentualVariacao = Math.abs(lastDecisionCpuLoad - currentDecisionCpuLoad);
            //Se o percentual de variação for maior que o linear (menor deles) ja quer dizer que o grão vai aumentar de tamanho
            if(percentualVariacao > percentualVariacaoGraoElasticoLinear){
                CalculateGrainSize(true, percentualVariacao, increase);
                //se for para adicionar recursos
                if(increase){
                    int totalDeVmsDisponiveis = ((quantidadeHostsCadastrados * oneManager.quatidade_cores_host) - oneManager.getTotalActiveResources());
                    if(totalDeVmsDisponiveis >= increase_grain_vms_size)                    
                        oneManager.vms_per_operation = increase_grain_vms_size;
                    else{
                        oneManager.vms_per_operation = totalDeVmsDisponiveis; //maximo possivel dado que o grão é muito grande
                    }
                }else{//se for para remover recursos
                    if (oneManager.getTotalActiveResources() - sla.getMinResources(false) >= decrease_grain_vms_size)            
                        oneManager.vms_per_operation = decrease_grain_vms_size;
                    else{
                        oneManager.vms_per_operation = oneManager.getTotalActiveResources() - sla.getMinResources(false);
                    }
                }                
            }      

            //Se o percentual de variação for menor que o linear (menor deles) ja quer dizer que o grão vai diminuir de tamanho
            if(percentualVariacao < percentualVariacaoGraoElasticoLinear){
                CalculateGrainSize(false, percentualVariacao, increase);
                if(increase){
                    int totalDeVmsDisponiveis = ((quantidadeHostsCadastrados * oneManager.quatidade_cores_host) - oneManager.getTotalActiveResources());
                    if(totalDeVmsDisponiveis >= increase_grain_vms_size)                    
                        oneManager.vms_per_operation = increase_grain_vms_size;
                    else{
                        oneManager.vms_per_operation = totalDeVmsDisponiveis; //maximo possivel dado que o grão é muito grande
                    }
                }
                else{
                    if (oneManager.getTotalActiveResources() - sla.getMinResources(false) >= decrease_grain_vms_size)            
                        oneManager.vms_per_operation = decrease_grain_vms_size;
                    else{
                        oneManager.vms_per_operation = oneManager.getTotalActiveResources() - sla.getMinResources(false);
                    }
                }
            }
            reportGrain = "aumento ocorrido: " + percentualVariacao + "|Percentual definido SLA: " + percentualVariacaoGraoElasticoLinear + " | "+ "Grao apos calculo: increase_grain_vms_size: " + increase_grain_vms_size + " | decrease_grain_vms_size: " + decrease_grain_vms_size + " | vmsPerOperation: " + oneManager.vms_per_operation + " e hostOperation: " + oneManager.hosts_per_operation;
            System.out.println(reportGrain);             
        }
    }
    
    private void CalculateGrainSize(boolean ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao, float percentualDeAumento, boolean operacaoIncrementoDeRecurso){
        //se for incremento de recursos deve atualizar a variavel do grao d eaumentar recursos
        if(operacaoIncrementoDeRecurso){
            if(ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao){
                if(percentualDeAumento > percentualVariacaoGraoElasticoLinear && percentualDeAumento < percentualVariacaoExponencial){
                    increase_grain_vms_size = (int) increase_grain_vms_size + 1;
                }else if(percentualDeAumento >= percentualVariacaoExponencial){
                    increase_grain_vms_size = increase_grain_vms_size + (int) Math.floor(Math.pow(2, 2));
                }
            }
            else{
                if(percentualDeAumento < percentualVariacaoGraoElasticoLinear){
                    increase_grain_vms_size = (int) increase_grain_vms_size - 1;
                }else if(percentualDeAumento >= percentualVariacaoExponencial){
                    increase_grain_vms_size = increase_grain_vms_size - (int) Math.floor(Math.pow(2, 2));
                }

                if(increase_grain_vms_size <= 0) increase_grain_vms_size = 1;
            }
        }
        else{//se for decremento de recursos deve atualizar a variavel do grao d eaumentar recursos
            if(ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao){
                if(percentualDeAumento > percentualVariacaoGraoElasticoLinear && percentualDeAumento < percentualVariacaoExponencial){
                    decrease_grain_vms_size = (int) decrease_grain_vms_size + 1;
                }else if(percentualDeAumento >= percentualVariacaoExponencial){
                    decrease_grain_vms_size = decrease_grain_vms_size + (int) Math.floor(Math.pow(2, 2));
                }
            }
            else{
                if(percentualDeAumento < percentualVariacaoGraoElasticoLinear){
                    decrease_grain_vms_size = (int) decrease_grain_vms_size - 1;
                }else if(percentualDeAumento >= percentualVariacaoExponencial){
                    decrease_grain_vms_size = decrease_grain_vms_size - (int) Math.floor(Math.pow(2, 2));
                }

                if(decrease_grain_vms_size <= 0) decrease_grain_vms_size = 1;
            }
        }    
        
    }
    
    public String GetReportGrain(){
        return reportGrain;
    }
    
    public void UpdateNewUsageAfterIncreases(float cpuUsage){
        newUsageAfterIncreases = cpuUsage;
    }
}
