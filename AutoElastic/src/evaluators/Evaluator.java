/*
 * 02/07/2015
 * Evaluator
 * Author: viniciusfacco
 * - Interface used to define the methods to implement evaluators
 */
package evaluators;

/**
 *
 * @author viniciusfacco
 */
public interface Evaluator {
    
    public boolean evaluate(float upper_cpu_threshold, float lower_cpu_threshold, float upper_mem_threshold, float lower_mem_threshold, float upper_network_threshold, float lower_network_threshold, Boolean usarElasticidadeMultinivel);
    
    public float computeLoad(float CpuLoad, float memLoad, float networkLoad);
    
    public float getDecisionCpuLoad();
    
    public float getDecisionMemLoad();
    
    public float getDecisionNetworkLoad();
    
    public float getLastDecisionCpuLoad();
    
    public float getLastDecisionMemLoad();
    
    public float getLastDecisionNetworkLoad();
    
    public boolean isHighCpuAction();
    
    public boolean isLowCpuAction();
    
    public boolean isHighMemAction();
    
    public boolean isLowMemAction();
    
    public boolean isHighNetworkAction();
    
    public boolean isLowNetworkAction();
    
    public byte whichAction();
    
    public void resetFlags();
    
    public void reset();
    
}
