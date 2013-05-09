package edu.fudan.nlp.similarity;

import java.io.Serializable;

import edu.fudan.ml.types.Instance;
import edu.fudan.nlp.parser.dep.DependencyTree;
/**
 * 计算两颗树的相似度
 *
 */
public class TreeKernel implements ISimilarity <DependencyTree>, Serializable{

	private static final long serialVersionUID = 6749406907457182885L;

	@Override
	public float calc(DependencyTree item1, DependencyTree item2) throws Exception {
		float score = getDepScore(item1, item2, 1);
		float base = getBase(item1) * getBase(item2);
		base = (float) Math.sqrt(base);
		return (score / base);
	}

	/**
	 * 计算Tree Kernel
	 * @param t1
	 * @param t2
	 * @param depth
	 * @return
	 */
	private float getDepScore(DependencyTree t1, DependencyTree t2, int depth){
		float score = 0.0f;
		float modify = getDepthModify(depth);
		if(modify == 0)
			return score;
		
		score += modify * getSScore(t1, t2);
		
		for(int i = 0; i < t1.leftChilds.size(); i++)
			for(int j = 0; j < t2.leftChilds.size(); j++)
				score += getDepScore(t1.leftChilds.get(i), t2.leftChilds.get(j), depth+1);
		
		for(int i = 0; i < t1.leftChilds.size(); i++)
			for(int j = 0; j < t2.rightChilds.size(); j++)
				score += getDepScore(t1.leftChilds.get(i), t2.rightChilds.get(j), depth+1);
		
		for(int i = 0; i < t1.rightChilds.size(); i++)
			for(int j = 0; j < t2.leftChilds.size(); j++)
				score += getDepScore(t1.rightChilds.get(i), t2.leftChilds.get(j), depth+1);
		
		for(int i = 0; i < t1.rightChilds.size(); i++)
			for(int j = 0; j < t2.rightChilds.size(); j++)
				score += getDepScore(t1.rightChilds.get(i), t2.rightChilds.get(j), depth+1);
		
		return score;
	}
	
	/**
	 * c函数
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean getCScore(DependencyTree t1, DependencyTree t2){
		if(t1.pos.equals(t2.pos))
			return true;
		else return false;
	}
	
	/**
	 * s函数
	 * @param t1
	 * @param t2
	 * @return
	 */
	private double getSScore(DependencyTree t1, DependencyTree t2){
		if(t1.word.equals(t2.word))
			return 1;
		else return 0;
	}
	
	/**
	 * 深度修正参数
	 * @param depth
	 * @return
	 */
	private float getDepthModify(int depth){
		if(depth == 1)
			return 1;
		else if(depth == 2)
			return 0.9f;
		else if(depth == 3)
			return 0.8f;
		else if(depth == 4)
			return 0.65f;
		else if(depth == 5)
			return 0.5f;
		else if(depth == 6)
			return 0.3f;
		else if(depth == 7)
			return 0.1f;
		else return 0;
	}

	/**
	 * 分数归一化
	 * @param t
	 * @return
	 */
	private float getBase(DependencyTree t){
		return getDepScore(t,t,1);
	}
}
