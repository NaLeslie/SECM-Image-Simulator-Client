package deconvolutionutilities;

/**
 * Collection of methods for attempts to use modified Richardson-Lucy (R-L)
 * deconvolution with scanning electrochemical microscopy (SECM) images.
 * @author Nathaniel Leslie
 */
public class DeconvUtilMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
    /**
     * Converts a logk for a spot at the surface to a scaled current that would 
     * be observed by a microelectrode above the surface.This function is inverted
     * by {@link #currentToLogk(double)}.
     * @param logk The log rate constant
     * @return 
     */
    static double logkToCurrent(double logk){
        return 1;
    }
    
    /**
     * Converts a scaled current signal to the approximate logk value that may have generated it.
     * This serves as an inverse for {@link #logkToCurrent(double)}.
     * Newton's method is used to approximate this logk value.
     * @param scaled_current The scaled current as a value between 0 and 1.
     * @return the logk that will result in current when used as an argument for {@link #logkToCurrent(double)}.
     */
    static double currentToLogk(double scaled_current){
        double perturbation_logk = 0.01; //the perturbation to use when approximating the derivative for #logkToCurrent(double).
        double threshold = 0.005; //the threshold for determining convergence (differences in candidate logk that fall below this value will be considered converged).
        int iterations = 8; //number of iterations of Newton's method
        
        double candidate_logk = 0.5*(LOGK_HIGH - LOGK_LOW); //initial guess for logk (should be located in the sloped portion of the k-i curve).
        double candidate_i;
        double derivative;
        double old_candidate;
        
        do{
            old_candidate = candidate_logk;
            candidate_i = logkToCurrent(candidate_logk);
            derivative = (logkToCurrent(candidate_logk + perturbation_logk) - candidate_i)/ perturbation_logk;
            
            candidate_logk -= (candidate_i - scaled_current)/derivative;
            
            iterations --;
        }while(iterations > 0 && Math.abs(old_candidate - candidate_logk) > threshold);
        
        return candidate_logk;
    }
    
    /**
     * The logk value below which no change in current is expected
     */
    static final double LOGK_LOW = -6;
    
    /**
     * The logk value above which no change in current is expected
     */
    static final double LOGK_HIGH = -2;
}
