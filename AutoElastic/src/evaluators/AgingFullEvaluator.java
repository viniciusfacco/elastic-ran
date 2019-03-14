/*
 * 06/07/2015
 * AgingFullEvaluator
 * - class to apply the exponential moving average in the system load of all observations
 */
package evaluators;

import static autoelastic.AutoElasticManager.gera_log;

/**
 * Reviews
 * @author viniciusfacco
 * 
 */
public class AgingFullEvaluator extends GenericEvaluator{
    
    /**
     *
     * @param viewsize - define the minimum of observations necessary
     */
    public AgingFullEvaluator(int viewsize) {
        super(viewsize);
        objname = "evaluators.AgingFullEvaluator";    //rewrite the name
    }
    
    /**
    * Return if the aging system identified if the factor is out of range between the thresholds.
    * @param upper_threshold - the current upper threshold.
    * @param lower_threshold - the current lower threshold.
    * @return 
    */
    @Override
    public boolean evaluate(float upper_cpu_threshold, float lower_cpu_threshold, float upper_mem_threshold, float lower_mem_threshold, float upper_network_threshold, float lower_network_threshold, Boolean usarElasticidadeMultinivel){        
        //gera_log(objname, "Main|AginFullEvaluator|evaluate: Aging = " + decision_cpu_load);
        boolean alertHappen = false;
        if (counter >= VIEW_SIZE - 1){
            //test if the aging is out of the range between the thresholds
            if (usarElasticidadeMultinivel && decision_network_load > upper_network_threshold){ //test if we have a violation on the lower threshold after aply the aging
                high_network_alert = true;
                low_network_alert = false;
                alertHappen = true;
            }  
            else if (decision_cpu_load > upper_cpu_threshold) { //test if we have a violation on the higher threshold after aply the aging
                high_cpu_alert = true; 
                low_cpu_alert = false;
                alertHappen = true;
            } else if (decision_cpu_load < lower_cpu_threshold){ //test if we have a violation on the lower threshold after aply the aging
                high_cpu_alert = false;
                low_cpu_alert = true;
                alertHappen = true;
            } 
            if (decision_mem_load > upper_mem_threshold){ //test if we have a violation on the lower threshold after aply the aging
                high_mem_alert = true;
                low_mem_alert = false;
                alertHappen = true;
            } else if (decision_mem_load < lower_mem_threshold){ //test if we have a violation on the lower threshold after aply the aging
                high_mem_alert = false;
                low_mem_alert = true;
                alertHappen = true;
            } 
           // else if (usarElasticidadeMultinivel && decision_network_load < lower_network_threshold){ //test if we have a violation on the lower threshold after aply the aging
               // high_network_alert = false;
              //  low_network_alert = true;
              //  alertHappen = true;
            //}
            if (!alertHappen){
                high_cpu_alert = false;
                low_cpu_alert = false;
                high_mem_alert = false;
                low_mem_alert = false;
                high_network_alert = false;
                low_network_alert = false;
            }
        } else {
            counter++; //here, counter is used to define the observantions amount
        }
        return alertHappen;  
    }    
    
    @Override
    public float computeLoad(float cpuLoad, float memLoad, float networkLoad){
        //Store the last value for adaptative grain comparisons
        last_decision_cpu_load = decision_cpu_load;
        last_decision_mem_load = decision_mem_load;
        last_decision_network_load = decision_network_load;
        //Update the current values
        decision_cpu_load = (float) (decision_cpu_load * 0.5 + cpuLoad * 0.5);
        decision_mem_load = (float) (decision_mem_load * 0.5 + memLoad * 0.5);
        decision_network_load = (float) (decision_network_load * 0.5 + networkLoad * 0.5);
         return 0;
    }
}
