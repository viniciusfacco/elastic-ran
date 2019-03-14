/*
 * 02/07/2015
 * StaticThresholds
 * Author: viniciusfacco
 * - Implements static thresholds that not change
 */
package thresholds;

/**
 * Reviews
 * @author viniciusfacco
 * 28/09/2015 - viniciusfacco
 *            - removed methods recalculateLowerThreshold and recalculateUpperThreshold
 *            - added method resetThresholds
 */
public class StaticThresholds implements Thresholds{
    
    protected float upper_cpu_threshold;
    protected float lower_cpu_threshold;
    protected float upper_mem_threshold;
    protected float lower_mem_threshold;
    protected float upper_network_threshold;
    protected float lower_network_threshold;
    protected String objname;
    
    public StaticThresholds(float upperCpuT, float lowerCpuT, float upperMemT, float lowerMemT, float upperNetworkT, float lowerNetworkT){
        upper_cpu_threshold = upperCpuT;
        lower_cpu_threshold = lowerCpuT;
        upper_mem_threshold = upperMemT;
        lower_mem_threshold = lowerMemT;
        upper_network_threshold = upperNetworkT;
        lower_network_threshold = lowerNetworkT;
    }

    /**
     * With static threshold this method do nothing
     * @param load
     */
    @Override
    public void calculateThresholds(float load){}
    
    /**
     * StaticThresholds: do nothing.
     * 
     */
    @Override
    public void resetThresholds(){}
    
    @Override
    public void recalculateUpperThreshold(float x, float y, float z) {}

    @Override
    public void recalculateLowerThreshold(float x, float y, float z) {}
    
    /**
     * Return the current upper threshold
     * @return current current upper threshold
     */
    @Override
    public float getUpperCpuThreshold() {
        return upper_cpu_threshold;
    }

    /**
     * Return the current lower threshold
     * @return current lower threshold
     */
    @Override
    public float getLowerCpuThreshold() {
        return lower_cpu_threshold;
    }
        
    @Override
    public void setUpperCpuThreshold(float threshold) {
        upper_cpu_threshold = threshold;
    }

    @Override
    public void setLowerCpuThreshold(float threshold) {
        lower_cpu_threshold = threshold;
    }

    @Override
    public void reset(float uppert, float lowert) {
        upper_cpu_threshold = uppert;
        lower_cpu_threshold = lowert;
    }    

    @Override
    public float getUpperMemThreshold() {
        return upper_mem_threshold;
    }

    @Override
    public float getLowerMemThreshold() {
        return lower_mem_threshold;
    }

    @Override
    public void setUpperMemThreshold(float threshold) {
        upper_mem_threshold = threshold;
    }

    @Override
    public void setLowerMemThreshold(float threshold) {
        lower_mem_threshold = threshold;
    }

    @Override
    public float getUpperNetworkThreshold() {
        return upper_network_threshold;
    }

    @Override
    public float getLowerNetworkThreshold() {
        return lower_network_threshold;
    }

    @Override
    public void setUpperNetworkThreshold(float threshold) {
        upper_network_threshold = threshold;
    }

    @Override
    public void setLowerNetworkThreshold(float threshold) {
        lower_network_threshold = threshold;
    }
}
