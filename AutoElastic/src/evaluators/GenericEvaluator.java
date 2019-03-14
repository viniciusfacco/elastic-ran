/*
 * 24/04/2014
 * GenericEvaluator
 * Author: viniciusfacco
 * - Classe destinada a fazer a avaliação genérica de carga de CPU;
 * - Avaliação consiste em examinar um histórico de leituras de CPU e concluir se há necessidade de elasticidade ou não
 */

package evaluators;

import static autoelastic.AutoElasticManager.gera_log;

/**
 * Reviews
 * @author viniciusfacco
 * 02/07/2015 - viniciusfacco
 *            - class now implements Evaluator
 *            - changed the constructor and parameters. Now the thresholds are not keep here
 *            - changed method "evaluate" to receive the thresholds as parameters
 */
public class GenericEvaluator implements Evaluator{

    protected static String objname = "evaluators.Generic";;//name of the class to be used in the logs
    protected int VIEW_SIZE;        //quantidade de dados históricos que serão avaliados
    //protected float threshold_high; //limite superior em %
    //protected float threshold_low;  //limite inferior em %
    protected byte counter;         //quantidade de vezes consecutivas que threshold foi violado
    protected boolean high_cpu_alert;   //flag para avisar se o limite superior de CPU foi atingido VIEW_SIZE vezes consecutivas
    protected boolean low_cpu_alert;    //flag para avisar se o limite inferior de COU foi atingido VIEW_SIZE vezes consecutivas
    protected boolean high_mem_alert;   //flag para avisar se o limite superior de CPU foi atingido VIEW_SIZE vezes consecutivas
    protected boolean low_mem_alert;    //flag para avisar se o limite inferior de COU foi atingido VIEW_SIZE vezes consecutivas
    protected boolean high_network_alert;   //flag para avisar se o limite superior de CPU foi atingido VIEW_SIZE vezes consecutivas
    protected boolean low_network_alert;    //flag para avisar se o limite inferior de COU foi atingido VIEW_SIZE vezes consecutivas
    protected float decision_cpu_load;  //value used to compare with thresholds CPU
    protected float decision_mem_load;  //value used to compare with thresholds CPU
    protected float decision_network_load;  //value used to compare with thresholds CPU
    protected float last_decision_cpu_load;  //value used to compare with thresholds CPU
    protected float last_decision_mem_load;  //value used to compare with thresholds CPU
    protected float last_decision_network_load;  //value used to compare with thresholds CPU
    //public GenericEvaluator(int viewsize, float th_max, float th_min){
        //threshold_high = th_max;
        //threshold_low = th_min;
    public GenericEvaluator(int viewsize){        
        counter = 0; 
        high_cpu_alert = false;
        low_cpu_alert = false;
        decision_cpu_load = 0;
        VIEW_SIZE = viewsize;
    }
    
    //24/04/2014: método de avaliação que devolve true se houve uma sequência de VIEW_SIZE quebras seguidas em algum dos thresholds
    @Override
    public boolean evaluate(float upper_cpu_threshold, float lower_cpu_threshold, float upper_mem_threshold, float lower_mem_threshold, float upper_network_threshold, float lower_network_threshold, Boolean usarElasticidadeMultinivel){
        //----------------------------------------------------------------------
        //para realizar a avaliação primeiramente comparo a cpu com os thresholds
        if (decision_cpu_load > upper_cpu_threshold){        //verifico se violamos o limite superior
            if (counter >= 0){          //se sim, então verifico se o contador de violações está zerado ou já possui alguma contagem consecutiva de violações superiores
                counter++;              //se sim então incremento o contador
            } else counter = 1;         //se não, isso quer dizer que violações inferiores tinham ocorrido, então realizo a primeira contagem consecutiva de violação superior
        } else if (decision_cpu_load < lower_cpu_threshold){ //se não, então verifico se violamos o limite inferior
            if (counter <= 0){          //se sim, então verifico se o contador de violações está zerado ou já possui alguma contagem consecutiva de violações inferiores
                counter--;              //se sim então incremento o contador
            } else counter = -1;        //se não, isso quer dizer que violações superiores tinham ocorrido, então realizo a primeira contagem consecutiva de violação inferior
        } else counter = 0;             //se não, quer dizer que não violamos nenhum limite e posso zerar o contador de violações
        //----------------------------------------------------------------------
        //agora vamos verificar se ocorreram VIEW_SIZE violações seguidas
        if ((counter >= VIEW_SIZE)){                    //se ocorreram VIEW_SIZE violações superiores
            counter = 0;                                    //zeramos o contador
            high_cpu_alert = true;                                      //setamos para true o alerta de violações superiores consecutivas
            low_cpu_alert = false;                                      //setamos para false o alerta de violações inferiores consecutivas
            return true;                                            //retornamos true para avisar que há necessidade de ações
        } else if ((counter <= (-1 * VIEW_SIZE))){      //se não, se ocorreram VIEW_SIZE violações inferiores
            counter = 0;                                    //zeramos o contador
            high_cpu_alert = false;                                     //setamos para false o alerta de violações superiores consecutivas
            low_cpu_alert = true;                                       //setamos para true o alerta de violações inferiores consecutivas
            return true;                                             //retornamos true para avisar que há necessidade de ações
        }
        //----------------------------------------------------------------------
        //alternativa ao código acima, porém faz sempre 3 comparações e não mantém o histórico da última violação na próxima avaliação
        //high_cpu_alert = (counter >= VIEW_SIZE);
        //low_cpu_alert = (counter <= (-1 * VIEW_SIZE));
        //if (low_cpu_alert || low_cpu_alert){
        //    counter = 0;
        //    return true;
        //}
        //----------------------------------------------------------------------
        return false;
    }
    
    @Override
    public float computeLoad(float load, float memLoad, float networkLoad) {
        decision_cpu_load = load;
        return decision_cpu_load;
    }
    
    @Override
    public float getDecisionCpuLoad() {
        return decision_cpu_load;
    }
    
    @Override
    public float getDecisionMemLoad() {
        return decision_mem_load;    
    }

    @Override
    public float getDecisionNetworkLoad() {
        return decision_network_load;    
    }
    
    //25/04/2014:método para retornar se há necessidade de realizar alguma ação para violação superior de CPU
    @Override
    public boolean isHighCpuAction(){
        return high_cpu_alert;
    }
    
    //25/04/2014:método para retornar se há necessidade de realizar alguma ação para violação inferior de CPU
    @Override
    public boolean isLowCpuAction(){
        return low_cpu_alert;
    }
    
        //25/04/2014:método para retornar se há necessidade de realizar alguma ação para violação superior de CPU
    @Override
    public boolean isHighMemAction(){
        return high_mem_alert;
    }
    
    //25/04/2014:método para retornar se há necessidade de realizar alguma ação para violação inferior de CPU
    @Override
    public boolean isLowMemAction(){
        return low_mem_alert;
    }
    
        //25/04/2014:método para retornar se há necessidade de realizar alguma ação para violação superior de CPU
    @Override
    public boolean isHighNetworkAction(){
        return high_network_alert;
    }
    
    //25/04/2014:método para retornar se há necessidade de realizar alguma ação para violação inferior de CPU
    @Override
    public boolean isLowNetworkAction(){
        return low_network_alert;
    }
    
    //25/04/2014:método para retornar qual ação deve ser realizada: 1-superior, -1-inferior e 0-nenhuma
    @Override
    public byte whichAction(){
        if (high_cpu_alert) 
            return 1;
        else if (low_cpu_alert) 
            return -1;
        else return 0;
    }
    
    /**
     * Set the alert flags to false
     */
    @Override
    public void resetFlags(){
        high_cpu_alert = false;
        low_cpu_alert = false;
    }
    
    //23/05/2014: reset the counters
    @Override
    public void reset(){
        counter = 0;
        high_cpu_alert = false;
        low_cpu_alert = false;
        decision_cpu_load = 0;
    }

    @Override
    public float getLastDecisionCpuLoad() {
        return last_decision_cpu_load;
    }

    @Override
    public float getLastDecisionMemLoad() {
       return last_decision_mem_load;
    }

    @Override
    public float getLastDecisionNetworkLoad() {
        return last_decision_network_load;
    }
    
}
