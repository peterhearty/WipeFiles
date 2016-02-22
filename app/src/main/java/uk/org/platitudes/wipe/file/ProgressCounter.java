/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.wipe.file;

/**
 * Used to monitor progress of an operation. ProgressCounters can exist in a hierarchy where
 * children represent partial components of an operation. For example, in a file wipe
 * operation, the top level counter holds the total byte count of all the files to be wiped.
 * A child counter holds the counters for the file currently being wiped.
 */
public class ProgressCounter {

    /**
     * The largest value that this counter is allowed to hold. It is either set when the
     * ProgressCounter is created or by calls to addToMax().
     */
    private long            maxValue;

    /**
     * This is always in the range zero to maxValue, inclusive. It starts at zero and gets
     * added to by the add() method. When it reaches maxValue this ProgressCounter is marked
     * as "finished".
     */
    private long            currentValue;

    /**
     * If this ProgressCounter monitors a part of a larger operation then parentCounter monitors
     * the larger operation. The idea is that total progress can be measured by adding the
     * progress in this ProgressCounter to its parent and so on to obtain the total progress.
     * When this smaller operation is finished, its maxValue gets added to the parent's
     * currentValue.
     */
    private ProgressCounter parentCounter;

    /**
     * Set true when currentValue reaches maxValue. The currentValue can no longer be added to
     * and the parentCounter gets updated to reflect the completion of this smaller subtask.
     */
    private boolean         finished;

    public ProgressCounter (long max) {
        maxValue = max;
    }

    public ProgressCounter copy () {
        ProgressCounter result = new ProgressCounter(maxValue);
        result.parentCounter = parentCounter;
        return result;
    }

    public void setParentCounter (ProgressCounter parent) {parentCounter = parent;}
    public void addToMax (long a) {maxValue += a;}

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
            result = maxValue;
            if (parentCounter != null) {
                result = parentCounter.getTotalProgress();
            }
        } else {
            result = currentValue;
            if (parentCounter != null)
                result += parentCounter.getTotalProgress();
        }
        return result;
    }

    private long getTotalMax () {
        long result = maxValue;
        if (parentCounter != null)
            result = parentCounter.getTotalMax();
        if (result == 0) {
            // wiping zero size files can make this zero and cause a divide by zero in getProgressPercent
            result = 1;
        }
        return result;
    }

    public int getProgressPercent () {
        int result;
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
    public void setMaxValue(long maxValue) {this.maxValue = maxValue;}

}
