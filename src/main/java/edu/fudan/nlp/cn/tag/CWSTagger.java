package edu.fudan.nlp.cn.tag;

import java.util.ArrayList;

import edu.fudan.ml.classifier.struct.inf.ConstraintViterbi;
import edu.fudan.ml.classifier.struct.inf.LinearViterbi;
import edu.fudan.ml.types.Dictionary;
import edu.fudan.ml.types.Instance;
import edu.fudan.nlp.cn.Sentenizer;
import edu.fudan.nlp.cn.tag.format.FormatCWS;
import edu.fudan.nlp.pipe.Pipe;
import edu.fudan.nlp.pipe.SeriesPipes;
import edu.fudan.nlp.pipe.seq.DictLabel;
import edu.fudan.nlp.pipe.seq.DictMultiLabel;
import edu.fudan.nlp.pipe.seq.String2Sequence;
import edu.fudan.util.MyCollection;
import edu.fudan.util.exception.LoadModelException;
import gnu.trove.set.hash.THashSet;

/**
 * 中文分词器
 * 
 * @author xpqiu
 * @version 1.0
 * @since FudanNLP 1.0
 */
public class CWSTagger extends AbstractTagger {
    // 考虑不同CWStagger可能使用不同dict，所以不使用静态
    private DictLabel dictPipe = null;
    private Pipe oldfeaturePipe = null;
    /**
     * 是否对英文单词进行预处理
     */
    private boolean isEnFilter = true;

    /**
     * 是否对英文单词进行预处理，将连续的英文字母看成一个单词
     * 
     * @param b
     */
    public void setEnFilter(boolean b) {
        isEnFilter = b;
        prePipe = new String2Sequence(isEnFilter);
    }

    /**
     * 构造函数，使用LinearViterbi解码
     * 
     * @param str
     *            模型文件名
     * @throws LoadModelException
     */
    public CWSTagger(String str, boolean classpath) throws LoadModelException {
        super(str, classpath);
        prePipe = new String2Sequence(isEnFilter);

        // DynamicViterbi dv = new DynamicViterbi(
        // (LinearViterbi) cl.getInferencer(),
        // cl.getAlphabetFactory().buildLabelAlphabet("labels"),
        // cl.getAlphabetFactory().buildFeatureAlphabet("features"),
        // false);
        // dv.setDynamicTemplets(DynamicTagger.getDynamicTemplet("example-data/structure/template_dynamic"));
        // cl.setInferencer(dv);
    }

    public CWSTagger(String file) throws LoadModelException {
        this(file, false);
    }

    private void initDict(Dictionary dict) {
        // prePipe = new String2SimpleSequence(false);

        if (dict.isAmbiguity()) {
            dictPipe = new DictMultiLabel(dict, labels);
        } else {
            dictPipe = new DictLabel(dict, labels);
        }

        oldfeaturePipe = featurePipe;
        featurePipe = new SeriesPipes(new Pipe[] { dictPipe, featurePipe });

        LinearViterbi dv = new ConstraintViterbi(
                (LinearViterbi) cl.getInferencer());
        cl.setInferencer(dv);
    }

    /**
     * 构造函数，使用ConstraintViterbi解码
     * 
     * @param str
     *            模型文件名
     * @param dict
     *            外部词典资源
     * @throws Exception
     */
    public CWSTagger(String str, Dictionary dict) throws Exception {
        this(str);
        initDict(dict);
    }

    /**
     * 设置词典
     * 
     * @param dict
     *            词典
     */
    public void setDictionary(Dictionary dict) {
        if (dictPipe == null) {
            initDict(dict);
        } else {
            dictPipe.setDict(dict);
        }
    }

    /**
     * 设置词典
     * 
     * @param newset
     */
    public void setDictionary(THashSet<String> newset) {
        if (newset.size() == 0)
            return;
        ArrayList<String> al = new ArrayList<String>();
        MyCollection.TSet2List(newset, al);
        Dictionary dict = new Dictionary();
        dict.addSegDict(al);
        setDictionary(dict);

    }

    /**
     * 移除词典
     */
    public void removeDictionary() {
        if (oldfeaturePipe != null) {
            featurePipe = oldfeaturePipe;
        }
        LinearViterbi dv = new LinearViterbi((LinearViterbi) cl.getInferencer());
        cl.setInferencer(dv);

        dictPipe = null;
        oldfeaturePipe = null;
    }

    @Override
    public String tag(String src) {
        if (src == null || src.length() == 0)
            return src;
        String[] sents = Sentenizer.split(src);
        String tag = "";
        try {
            for (int i = 0; i < sents.length; i++) {
                Instance inst = new Instance(sents[i]);
                String[] preds = _tag(inst);
                String s = FormatCWS.toString(inst, preds, delim);
                tag += s;
                if (i < sents.length - 1)
                    tag += delim;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tag;
    }

    /**
     * 先进行断句，得到每句的分词结果，返回List[]数组
     * 
     * @param src
     *            字符串
     * @return String[][] 多个句子数组
     */
    public String[][] tag2DoubleArray(String src) {
        if (src == null || src.length() == 0)
            return null;
        String[] sents = Sentenizer.split(src);
        String[][] words = new String[sents.length][];
        for (int i = 0; i < sents.length; i++) {
            words[i] = tag2Array(sents[i]);
        }
        return words;
    }

    /**
     * 得到分词结果 List，不进行断句
     * 
     * @param src
     *            字符串
     * @return ArrayList<String> 词数组，每个元素为一个词
     */
    public ArrayList<String> tag2List(String src) {
        if (src == null || src.length() == 0)
            return null;
        ArrayList<String> res = null;
        try {
            Instance inst = new Instance(src);
            String[] preds = _tag(inst);
            res = FormatCWS.toList(inst, preds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 得到分词结果 String[]，不进行断句
     * 
     * @param src
     *            字符串
     * @return String[] 词数组，每个元素为一个词
     */
    public String[] tag2Array(String src) {
        ArrayList<String> words = tag2List(src);
        return (String[]) words.toArray(new String[words.size()]);
    }

}
