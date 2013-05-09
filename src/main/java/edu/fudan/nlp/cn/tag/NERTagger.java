package edu.fudan.nlp.cn.tag;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.fudan.nlp.cn.Tags;
import edu.fudan.util.MyCollection;

/**
 * 实体名标注器 通过词性标注实现。
 * 
 * @author 邱锡鹏
 * 
 */
public class NERTagger {

    private static POSTagger pos;

    public NERTagger(CWSTagger cws, String str) throws Exception {
        pos = new POSTagger(cws, str);
    }

    public NERTagger(String segmodel, String posmodel) throws Exception {
        pos = new POSTagger(segmodel, posmodel);
    }

    public NERTagger(POSTagger posmodel) {
        pos = posmodel;
    }

    public HashMap<String, String> tag(String src) {
        HashMap<String, String> map = new HashMap<String, String>();
        tag(src, map);
        return map;
    }

    public void tag(String src, HashMap<String, String> map) {

        String[] sents = src.split("\\n+");
        try {
            for (int i = 0; i < sents.length; i++) {
                String[][] res = pos.tag2Array(sents[i]);
                if (res != null) {
                    for (int j = 0; j < res[0].length; j++) {
                        if (Tags.isEntiry(res[1][j])) {
                            map.put(res[0][j], res[1][j]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> tagFile(String input) {
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    input), "utf-8");
            BufferedReader lbin = new BufferedReader(read);
            String str = lbin.readLine();
            HashMap<String, String> map = new HashMap<String, String>();
            while (str != null) {
                tag(str, map);
                str = lbin.readLine();
            }
            lbin.close();
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void tagFile(String input, String output) {
        HashMap<String, String> map = tagFile(input);
        MyCollection.write(map.keySet(), output);
    }

}
