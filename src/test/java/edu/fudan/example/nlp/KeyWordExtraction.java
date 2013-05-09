package edu.fudan.example.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fnlp.app.keyword.Extractor;
import org.fnlp.app.keyword.WordExtract;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.corpus.StopWords;

/**
 * 关键词抽取使用示例
 * 
 * @author jyzhao,ltian
 * 
 */
public class KeyWordExtraction {

    public static void main(String[] args) throws Exception {

        String dir = "/com/dripower/models";
        List<String> stopWords = new ArrayList<String>();
        stopWords.add(dir + "/stopwords/ErrorWords.txt");
        stopWords.add(dir + "/stopwords/NoSenseWords.txt");
        stopWords.add(dir + "/stopwords/StopWords.txt");
        StopWords sw = new StopWords(stopWords);
        CWSTagger seg = new CWSTagger(dir + "/seg.m", true);
        Extractor key = new WordExtract(seg, sw);
        System.out
                .println(key
                        .extract(
                                "甬温线特别重大铁路交通事故车辆经过近24小时的清理工作，26日深夜已经全部移出事故现场，之前埋下的D301次动车车头被挖出运走",
                                10, true));

        // 处理已经分好词的句子
        sw = null;
        key = new WordExtract(seg, sw);
        Map<String, Integer> res1 = key
                .extract(
                        "甬温线 特别 重大 铁路交通事故车辆经过近24小时的清理工作，26日深夜已经全部移出事故现场，之前埋下的D301次动车车头被挖出运走",
                        10);
        System.out.println(res1);
        System.out.println(key.extract("赵嘉亿 是 好人 还是 坏人", 5));

        key = new WordExtract();
        System.out.println(key.extract("", 5));

    }
}
