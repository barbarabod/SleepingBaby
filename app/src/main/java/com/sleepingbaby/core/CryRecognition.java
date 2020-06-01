package com.sleepingbaby.core;

import java.util.LinkedList;

public class CryRecognition
{
    private LinkedList<Boolean> detectionList;
    private double thresholdAmplitude;
    private int thresholdQuantity;

    private boolean detected;

    public CryRecognition(int maxSize, int thresholdQuantity, double thresholdAmplitude)
    {
        detectionList = new LinkedList<>();
        for(int i = 0; i < maxSize; i++)
            detectionList.add(false);

        this.thresholdAmplitude = thresholdAmplitude;
        this.thresholdQuantity = thresholdQuantity;
    }

    /**
     * Updates crying detection
     * @param amplitude new amplitude
     * @return true if crying detected
     */
    public boolean update(double amplitude)
    {
        //updating list of detected ticks
        detectionList.add(amplitude >= thresholdAmplitude);
        detectionList.remove();

        //sum of detected ticks
        long sum = 0;
        for(boolean b : detectionList)
            if(b) sum += 1;

        return sum >= thresholdQuantity;
    }
}
