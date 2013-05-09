package edu.fudan.nlp.cn.tag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.fudan.ml.classifier.LabelParser.Type;
import edu.fudan.ml.classifier.TPredict;
import edu.fudan.ml.classifier.linear.Linear;
import edu.fudan.ml.types.Instance;
import edu.fudan.ml.types.alphabet.AlphabetFactory;
import edu.fudan.ml.types.alphabet.IFeatureAlphabet;
import edu.fudan.ml.types.alphabet.LabelAlphabet;
import edu.fudan.nlp.pipe.Pipe;
import edu.fudan.nlp.pipe.seq.Sequence2FeatureSequence;
import edu.fudan.nlp.pipe.seq.templet.TempletGroup;
import edu.fudan.util.exception.LoadModelException;

/**
 * 分词训练
 * 
 */

public abstract class AbstractTagger {

    protected Linear cl;
    protected Pipe prePipe = null;
    protected Pipe featurePipe;
    public AlphabetFactory factory;
    protected TempletGroup templets;
    protected LabelAlphabet labels;
    /**
     * 词之间间隔标记，缺省为空格。
     */
    protected String delim = " ";

    /**
     * 抽象标注器构造函数
     * 
     * @param file
     *            模型文件
     * @throws LoadModelException
     */
    public AbstractTagger(String file, boolean classpath)
            throws LoadModelException {
        if (classpath) {
            loadFromClasspath(file);
        } else {
            loadFrom(file);
        }
        if (cl == null) {
            throw new LoadModelException("模型为空");
        }

        factory = cl.getAlphabetFactory();
        labels = factory.DefaultLabelAlphabet();
        IFeatureAlphabet features = factory.DefaultFeatureAlphabet();
        featurePipe = new Sequence2FeatureSequence(templets, features, labels);
    }

    /**
     * 抽象标注器构造函数
     * 
     * @param file
     *            模型文件
     * @throws LoadModelException
     */
    public AbstractTagger(String file) throws LoadModelException {
        this(file, false);
    }

    public AbstractTagger() {
    }

    /**
     * 序列标注方法
     * 
     * @param src
     *            输入句子
     * @return
     */
    public abstract Object tag(String src);

    @SuppressWarnings("rawtypes")
    protected String[] _tag(Instance inst) {
        doProcess(inst);
        TPredict pred = cl.classify(inst, Type.SEQ);
        if (pred == null)
            return new String[0];
        return (String[]) pred.getLabel(0);
    }

    /**
     * 序列标注方法，输入输出为文件
     * 
     * @param input
     *            输入文件 UTF8编码
     * @param output
     *            输出文件 UTF8编码
     */
    public void tagFile(String input, String output, String sep) {
        String s = tagFile(input, "\n");
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(output), "utf-8");
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(s);
            bw.close();
        } catch (Exception e) {
            System.out.println("写输出文件错误");
            e.printStackTrace();
        }
    }

    /**
     * 序列标注方法，输入为文件
     * 
     * @param input
     *            输入文件 UTF8编码
     * @return 标注结果
     */
    public String tagFile(String input) {
        return tagFile(input, " ");
    }

    /**
     * 序列标注方法，输入为文件
     * 
     * @param input
     *            输入文件 UTF8编码
     * @return 标注结果
     */
    public String tagFile(String input, String sep) {
        StringBuilder res = new StringBuilder();
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    input), "utf-8");
            BufferedReader lbin = new BufferedReader(read);
            String str = lbin.readLine();
            while (str != null) {
                String s = (String) tag(str);
                res.append(s);
                res.append("\n");
                str = lbin.readLine();
            }
            lbin.close();
            return res.toString();
        } catch (IOException e) {
            System.out.println("读输入文件错误");
            e.printStackTrace();
        }
        return "";

    }

    /**
     * 数据处理方法，将数据从字符串的形式转化成向量形式
     * 
     * @param carrier
     *            样本实例
     */
    public void doProcess(Instance carrier) {
        try {
            if (prePipe != null)
                prePipe.addThruPipe(carrier);
            carrier.setSource(carrier.getData());
            featurePipe.addThruPipe(carrier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveTo(String modelfile) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new GZIPOutputStream(
                        new FileOutputStream(modelfile))));
        out.writeObject(templets);
        out.writeObject(cl);
        out.close();
    }

    public void loadFrom(String modelfile) throws LoadModelException {
        try {
            FileInputStream file = new FileInputStream(modelfile);
            loadFromStream(file);
        } catch (Exception e) {
            throw new LoadModelException(e, modelfile);
        }
    }

    public void loadFromStream(InputStream inputStream)
            throws LoadModelException {
        try {
            ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(new GZIPInputStream(inputStream)));
            templets = (TempletGroup) in.readObject();
            cl = (Linear) in.readObject();
        } catch (Exception e) {
            throw new LoadModelException(e, "cannot read");
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }

        }
    }

    public void loadFromClasspath(String path) throws LoadModelException {
        loadFromStream(this.getClass().getResourceAsStream(path));
    }
}
