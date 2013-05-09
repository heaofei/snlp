package edu.fudan.nlp.similarity;

import gnu.trove.iterator.TIntIterator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Brown 词聚类算法，多线程版
 * 
 * @author xpqiu
 * @since FudanNLP 1.5
 */
public class WordClusterM extends WordCluster {

    private static final long serialVersionUID = 58160232476872689L;
    transient int numThread = 4;
    transient private ExecutorService pool;
    transient float maxL;
    transient int maxc1;
    transient int maxc2;
    transient AtomicInteger count = new AtomicInteger();

    public WordClusterM(int threads) {
        this.numThread = threads;
        pool = Executors.newFixedThreadPool(numThread);
    }

    public synchronized void getmax(float f, int i, int j) {
        if (f > maxL) {
            maxL = f;
            maxc1 = i;
            maxc2 = j;
        }
    }

    class Multiplesolve implements Runnable {

        int c1, c2;

        public Multiplesolve(int c1, int c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        @Override
        public void run() {
            float l = calcL(c1, c2);
            getmax(l, c1, c2);
            count.decrementAndGet();
        }

    }

    /**
     * merge clusters
     */
    public void mergeCluster() {
        maxc1 = -1;
        maxc2 = -1;
        maxL = Float.NEGATIVE_INFINITY;
        TIntIterator it1 = slots.iterator();

        while (it1.hasNext()) {
            int i = it1.next();
            TIntIterator it2 = slots.iterator();
            // System.out.print(i+": ");
            while (it2.hasNext()) {
                int j = it2.next();

                if (i >= j)
                    continue;
                // System.out.print(j+" ");
                Multiplesolve c = new Multiplesolve(i, j);
                count.incrementAndGet();
                pool.execute(c);
            }
            // System.out.println();
        }

        while (count.get() != 0) {// 等待所有子线程执行完
            try {
                Thread.sleep(slotsize * slotsize / 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        merge(maxc1, maxc2);
    }

}
