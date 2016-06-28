/*******************************************************************************
 * This file is part of the Java SwingPrintPreview Library
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 René Bergelt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package de.renber.swing.controls.documentview.types.scaling;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import de.renber.swing.controls.documentview.threading.AutoResetEvent;
import de.renber.swing.controls.documentview.types.Page;

/**
 * Scales pages asynchronously (one at a time) and informs the calling component
 * when a page has been scaled successfully If a page is scheduled for rescale
 * with a distinct resolution and is rescheduled with a different target
 * resolution the old work item in the queue is replaced by the new one avoiding
 * unnecessary scaling
 *
 * @author berre
 */
public class PageScaler {

    // awaiting scaling jobs
    final List<ScaleJob> jobQueue = new ArrayList<ScaleJob>();
    Thread scaleThread = null;
    Scaler scaler = null;
    AutoResetEvent barrier;
    
    List<ScalingListener> scalingListeners = new ArrayList<ScalingListener>();

    public PageScaler() {
        barrier = new AutoResetEvent(false);                
    }
    
    /**
     * Starts the background thread
     */
    public void enable() {
        if (scaleThread == null) {
            scaler = new Scaler(barrier);
            scaleThread = new Thread(scaler);
            scaleThread.start();
        }
    }
    
    /**
     * Stops the background thread
     */
    public void disable() {
        if (scaleThread != null) {
            scaler.shutdown();
            scaler = null;
            try {
                scaleThread.join();
            } catch (InterruptedException ex) {
                // --
            }
            scaleThread = null;
        }
    }
    
    public void addScalingListener(ScalingListener listener) {
        if (!scalingListeners.contains(listener)) {
            scalingListeners.add(listener);
        }
    }
    
    public void removeScalingListener(ScalingListener listener) {
        if (scalingListeners.contains(listener)) {
            scalingListeners.remove(listener);
        }
    }
    
    protected void raiseScalingDoneEvent(ScaleJob job) {
        for(ScalingListener listener: scalingListeners) {
            listener.scalingDone(job.page, job.targetResolution);
        }
    }

    /**
     * Schedule a new scale job with will be processed in the future
     *
     * @param page
     * @param targetResolution
     */
    public void enqeue(Page page, Dimension targetResolution, boolean highPriority) {
        synchronized (jobQueue) {
            ScaleJob existJob = findJob(page);
            if (existJob == null) {
                // has not been scheduled yet
            	if (highPriority)            		
            		jobQueue.add(0, new ScaleJob(page, targetResolution));
            	else
            		// add it to the end of the queue
            		jobQueue.add(new ScaleJob(page, targetResolution));
            } else {
            	if (highPriority) {
            		// move job to top
            		jobQueue.remove(existJob);
            		jobQueue.add(0, existJob);            		
            	} else
            		// replace old job with new one
            		existJob.targetResolution = targetResolution;
            }

            barrier.set(); // inform thread
        }
    }

    /**
     * Find the job for the given page if one has been scheduled already
     *
     * @param page
     * @return the job or null
     */
    public ScaleJob findJob(Page page) {
        synchronized (jobQueue) {
            for (ScaleJob job : jobQueue) {
                if (job.page == page) {
                    return job;
                }
            }

            return null;
        }
    }

    /**
     * empties the list of waiting jobs
     */
    public void clear() {
        synchronized (jobQueue) {
            jobQueue.clear();
        }
    }

    class Scaler implements Runnable {

        AutoResetEvent barrier;
        boolean cancel = false;

        public Scaler(AutoResetEvent _barrier) {
            barrier = _barrier;
        }

        @Override
        public void run() {

            while (!cancel) {
                try {
                    // wait for queue activity
                    barrier.waitOne();
                } catch (InterruptedException ex) {
                    // --
                }

                //got unlocked because of cancellation request?
                if (cancel) {
                    return;
                }

                ScaleJob nextJob = null;
                boolean jobsRemaining = true;

                while (jobsRemaining) {
                    synchronized (jobQueue) {
                        if (jobQueue.size() > 0) {
                            nextJob = jobQueue.get(0);
                            jobQueue.remove(0);
                        }
                        
                        jobsRemaining = jobQueue.size() > 0;
                    }

                    // execute the job
                    if (nextJob != null) {
                        try
                        {
                        	nextJob.page.hiQualityScale(nextJob.targetResolution.width, nextJob.targetResolution.height);
                        	raiseScalingDoneEvent(nextJob);
                        }
                        catch (Exception exc) {
                            // scaling failed
                        }
                    }
                }
            }
        }

        /**
         * Cancels execution
         */
        public void shutdown() {
            cancel = true;
            barrier.set(); // re-enable the thread if it is not running atm            
        }
    }
}
