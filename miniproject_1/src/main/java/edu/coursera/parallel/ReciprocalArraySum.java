package edu.coursera.parallel;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public final class ReciprocalArraySum {

    private ReciprocalArraySum() {
    }

    protected static double seqArraySum(final double[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }
        return sum;
    }

    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    private static int getChunkStartInclusive(final int chunk, final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    private static int getChunkEndExclusive(final int chunk, final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    private static class ReciprocalArraySumTask extends RecursiveAction {

        private final int startIndexInclusive;
        private final int endIndexExclusive;
        private final double[] input;
        private double value;

        ReciprocalArraySumTask(final int setStartIndexInclusive,
                               final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            if(endIndexExclusive - startIndexInclusive <= 10000){
                for(int i=startIndexInclusive;i<endIndexExclusive;i++){
                    value += 1/input[i];
                }
            } else {
                int midPoint = (endIndexExclusive+startIndexInclusive)/2 + 1;
                ReciprocalArraySumTask reciprocalArraySumTaskLeft = new ReciprocalArraySumTask(startIndexInclusive,midPoint,input);
                ReciprocalArraySumTask reciprocalArraySumTaskRight = new ReciprocalArraySumTask(midPoint,endIndexExclusive,input);
                invokeAll(reciprocalArraySumTaskLeft,reciprocalArraySumTaskRight);
                value = reciprocalArraySumTaskLeft.getValue() + reciprocalArraySumTaskRight.getValue();
            }
        }
    }


    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;
        int start = getChunkStartInclusive(0,1,input.length);
        int end = getChunkEndExclusive(0,1,input.length);
        ReciprocalArraySumTask reciprocalArraySumTask = new ReciprocalArraySumTask(start,end,input);
        System.setProperty("java.util.concurrent.ForkJoin.common.parallelism","4");
        ForkJoinPool.commonPool().invoke(reciprocalArraySumTask);
        return reciprocalArraySumTask.getValue();
    }


    protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
        double sum = 0;
        ArrayList<ReciprocalArraySumTask> reciprocalArraySumTaskList = new ArrayList<>();
        for(int i=0;i<numTasks;i++){
            int start = getChunkStartInclusive(i,numTasks,input.length);
            int end = getChunkEndExclusive(i,numTasks,input.length);
            reciprocalArraySumTaskList.add(new ReciprocalArraySumTask(start,end,input));
        }
        ForkJoinTask.invokeAll(reciprocalArraySumTaskList);
        for(ReciprocalArraySumTask rast : reciprocalArraySumTaskList){
            sum += rast.getValue();
        }
        return sum;
    }
}