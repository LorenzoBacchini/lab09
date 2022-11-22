package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix{

    private final int nThread;

    public MultiThreadedSumMatrix(final int nThread){
        this.nThread = nThread;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int xPos;
        private int yPos; 
        private final int nelem;
        private long res;

        /**
         * Build a new worker.
         * 
         * @param matrix
         *            the matrix to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.xPos = this.startpos/matrix[0].length;
            this.yPos = this.startpos%matrix[0].length;
            this.nelem = nelem;
        }

        @Override
        public void run() {
            int summedElem = 0;
            System.out.println("Working from position x: " + xPos + " y: " + yPos
             + " to position x: " + (startpos + nelem - 1)/matrix[0].length
             + " y: " + (startpos + nelem - 1)%matrix[0].length);
             
            for (int i = xPos; i < matrix.length && summedElem < nelem; i++ ) {
                for (int j = yPos; j < matrix[0].length && summedElem < nelem; j++) {
                    this.res += this.matrix[i][j];
                    summedElem++;
                }
                this.yPos = 0;
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public double getResult() {
            return this.res;
        }
    }

    @Override
    public double sum(final double[][] matrix) {
        final int row = matrix.length;
        final int col = matrix[0].length;
        final int matrixSize = row*col;
        final int size = (matrixSize) % nThread + (matrixSize) / nThread;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(nThread);
        for (int start = 0; start < matrixSize; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }
}
