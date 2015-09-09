package org.nsidc.parallel;

import java.util.concurrent.*;

public class BogusTask extends RecursiveAction {

    protected static int sThreshold = 100;
    
    protected static final long serialVersionUID = 1;
    
    protected static int idTotal = 0;
    protected int myID = 0;

	private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination;
  
    // Processing window size; should be odd.
    private int mBlurWidth = 15;
  
    public BogusTask(int[] src, int start, int length, int[] dst) {
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
    }

    protected void computeDirectly() {
    	
    	idTotal++;
    	myID = idTotal;

    	int q = 0;
    	
        System.out.println("  process number = " + myID );
        
        int sidePixels = (mBlurWidth - 1) / 2;
        for (int index = mStart; index < mStart + mLength; index++) {
            // Calculate average.
            float rt = 0, gt = 0, bt = 0;
            for (int mi = -sidePixels; mi <= sidePixels; mi++) {
                int mindex = Math.min(Math.max(mi + index, 0),
                                    mSource.length - 1);
                int pixel = mSource[mindex];
                rt += (float)((pixel & 0x00ff0000) >> 16)
                      / mBlurWidth;
                gt += (float)((pixel & 0x0000ff00) >>  8)
                      / mBlurWidth;
                bt += (float)((pixel & 0x000000ff) >>  0)
                      / mBlurWidth;
                
                q = 0;
                for (int j = 0; j < 100000000; j++) {
                	q = q + j;
                	q = q - j;
                }
            }
          
            // Reassemble destination pixel.
            int dpixel = (0xff000000     ) |
                   (((int)rt) << 16) |
                   (((int)gt) <<  8) |
                   (((int)bt) <<  0);
            mDestination[index] = dpixel;
            
        }
        
    }

    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return;
        }
        
        int split = mLength / 2;
        
        invokeAll(new BogusTask(mSource, mStart, split, mDestination),
                  new BogusTask(mSource, mStart + split, mLength - split,
                               mDestination));
    }
}
