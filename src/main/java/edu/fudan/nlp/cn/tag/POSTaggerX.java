package edu.fudan.nlp.cn.tag;

import java.util.ArrayList;
import java.util.List;

import edu.fudan.ml.types.Instance;
import edu.fudan.nlp.cn.Sentenizer;
import edu.fudan.nlp.cn.tag.format.Seq2ArrayWithTag;
import edu.fudan.nlp.cn.tag.format.Seq2StrWithTag;

/**
 * 用交叉标签的词性标注器
 * 
 * @author xpqiu
 * @version 1.0
 * @since FudanNLP 1.0
 */
public class POSTaggerX extends AbstractTagger {

    public POSTaggerX(String str) throws Exception {
        super(str);

        // DynamicViterbi dv = new DynamicViterbi(
        // (LinearViterbi) cl.getInferencer(),
        // cl.getAlphabetFactory().buildLabelAlphabet("labels"),
        // cl.getAlphabetFactory().buildFeatureAlphabet("features"),
        // false);
        // dv.setDynamicTemplets(DynamicTagger.getDynamicTemplet("example-data/structure/template_dynamic"));
        // cl.setInferencer(dv);
    }

    public String[][] tag2Array(String src) {
        ArrayList words = new ArrayList<String>();
        ArrayList pos = new ArrayList<String>();
        String[] s = Sentenizer.split(src);
        try {
            for (int i = 0; i < s.length; i++) {
                Instance inst = new Instance(s[i]);
                doProcess(inst);
                int[] pred = (int[]) cl.classify(inst).getLabel(0);
                String[] target = labels.lookupString(pred);
                List[] res = Seq2ArrayWithTag.format(inst, target);
                words.addAll(res[0]);
                pos.addAll(res[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[][] tag = new String[2][];
        tag[0] = (String[]) words.toArray(new String[words.size()]);
        tag[1] = (String[]) pos.toArray(new String[pos.size()]);
        return tag;
    }

    @Override
    public String tag(String src) {
        String[] sents = Sentenizer.split(src);
        String tag = "";
        try {
            for (int i = 0; i < sents.length; i++) {
                Instance inst = new Instance(sents[i]);
                String[] preds = _tag(inst);
                String s = Seq2StrWithTag.format(inst, preds);
                tag += s;
                if (i < sents.length - 1)
                    tag += delim;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tag;
    }

}
