/**
 * This source code is not owned by anybody. You can can do what you like with it.
 *
 * @author  Peter Hearty
 * @date    April 2015
 */
package uk.org.platitudes.wipe.file;

/**
 * Created by pete on 25/06/15.
 */
public class ProgressCounter {

    private long            maxValue;
    private long            currentValue;
    private ProgressCounter parentCounter;
    private boolean         finished;
    private int             compressFactor;


    public ProgressCounter (long max) {
        maxValue = max;
        compressFactor = 1;
    }

    public ProgressCounter copy () {
        ProgressCounter result = new ProgressCounter(maxValue);
        result.parentCounter = parentCounter;
        result.compressFactor = compressFactor;
        return result;
    }

    public void setParentCounter (ProgressCounter parent) {
        parentCounter = parent;
    }

    public void multiplyCompressFactor (int m) {
        compressFactor *= m;
    }

    public void addToMax (long a) {
        maxValue += a;
    }

    public void add (long a) {
        if (finished)
            return;
        currentValue += a;
        if (currentValue >= maxValue) {
            finish();
        }
    }

    public void finish () {
        if (finished)
            return;
        currentValue = maxValue;
        finished = true;
        updateParent();
    }

    private long getTotalProgress () {
        long result;
        if (finished) {
            result = maxValue / compressFactor;
            if (parentCounter != null) {
                result = parentCounter.getTotalProgress();
            }
        } else {
            result = currentValue / compressFactor;
            if (parentCounter != null)
                result += parentCounter.getTotalProgress();
        }
        return result;
    }

    private long getTotalMax () {
        long result = maxValue;
        if (parentCounter != null)
            result = parentCounter.getTotalMax();
        return result;
    }

    public int getProgressPercent () {
        int result = 0;
        result = (int)(getTotalProgress()*100/getTotalMax());
        return result;
    }

    public void updateParent () {
        if (parentCounter == null) return;
        parentCounter.currentValue += maxValue;
    }

    public boolean isFinished () {return finished;}
    public long getCurrentValue() {return currentValue;}
    public long getMaxValue() {return maxValue;}


}
