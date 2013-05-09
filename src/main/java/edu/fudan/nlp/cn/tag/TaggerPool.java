package edu.fudan.nlp.cn.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 多线程序列标注
 * 
 * @author xpqiu
 * @version 1.0
 * @since FudanNLP 1.5
 */
public class TaggerPool {

    private ExecutorService pool;
    private int numThread;
    private AbstractTagger tagers;
    List<Future> f;

    public TaggerPool(int num) throws Exception {
        numThread = num;
        pool = Executors.newFixedThreadPool(numThread);

        f = new ArrayList<Future>();
    }

    public void tag(String c) throws Exception {

        ClassifyTask t = new ClassifyTask(c);
        f.add(pool.submit(t));

    }

    class ClassifyTask implements Callable {
        private String inst;

        public ClassifyTask(String inst) {
            this.inst = inst;
        }

        public String call() {
            // System.out.println("Thread: "+ idx);
            String type = (String) tagers.tag(inst);
            return type;

        }
    }

    public void loadPosTagger(String seg, String pos) throws Exception {
        tagers = new POSTagger(seg, pos);
    }

    public String getRes(int i) throws Exception {
        return (String) f.get(i).get();
    }

}
