/*
 * 02/07/2015
 * Thresholds
 * Author: viniciusfacco
 * - Interface used to define the methods to implement thresholds
 */
package thresholds;

/**
 *
 * @author viniciusfacco
 * 28/09/2015 - viniciusfacco
 *            - removed methods recalculateLowerThreshold and recalculateUpperThreshold
 *            - added method resetThresholds
 */
public interface Thresholds {
    
    public void calculateThresholds(float load);  
    
    public void resetThresholds();
    
    public void recalculateUpperThreshold(float x, float y, float z);
    
    public void recalculateLowerThreshold(float x, float y, float z);
    
    public float getUpperCpuThreshold();
    
    public float getLowerCpuThreshold();
    
    public void setUpperCpuThreshold(float threshold);
    
    public void setLowerCpuThreshold(float threshold);
    
    public float getUpperMemThreshold();
    
    public float getLowerMemThreshold();
    
    public void setUpperMemThreshold(float threshold);
    
    public void setLowerMemThreshold(float threshold);
    
   public float getUpperNetworkThreshold();
    
    public float getLowerNetworkThreshold();
    
    public void setUpperNetworkThreshold(float threshold);
    
    public void setLowerNetworkThreshold(float threshold);

    public void reset(float uppert, float lowert);
    
}
