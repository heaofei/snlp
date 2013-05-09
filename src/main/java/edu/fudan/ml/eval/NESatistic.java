package edu.fudan.ml.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 统计 实验结果的Precision,recall 和 FB1值
 * @author fxx
 */
public class NESatistic {
	private static Set<String> entityType;			//存放实体类型
	private String[] typeP = new String[1];			//记录实体类型
	private String[] typeC = new String[1];			//记录实体类型
	
	private	StringBuffer EntBufC = new StringBuffer();		//用来存放正确答案的实体字符
	private	StringBuffer EntBufP = new StringBuffer();		//用来存放估计的实体字符
	
	private Queue<Entity> entityCs = new LinkedList<Entity>();			//存放正确的实体的容器
	private Queue<Entity> entityPs = new LinkedList<Entity>();			//存放估计的实体的容器
	private Queue<Entity> entityCinPs = new LinkedList<Entity>();		//存放估计中正确实体的容器
     
	private Map<Integer,Integer> mpc = new HashMap<Integer,Integer>();		//估计的中正确的，key是字符串长度，value是这种长度的个数
	private Map<Integer,Integer> mp = new HashMap<Integer,Integer>();		//估计的，key是字符串长度，value是这种长度的个数
	private Map<Integer,Integer> mc = new HashMap<Integer,Integer>();		//正确的，key是字符串长度，value是这种长度的个数
	
	private boolean bl_entityP = false;			//记录的是否是估计实体开始的标志
	private boolean bl_entityC = false;			//记录的是否是正确实体开始的标志
	
	/**
	 * 读取评测结果文件，并输出到outputPath
	 * @param filePath			待评测文件路径
	 * @param outputPath		评测结果的输出路径
	 */
	public void NeEvl(String filePath,String outputPath){
		if(filePath != null){
			File file = new File(filePath);
			BufferedReader reader = null;
	        try {
	        	entityType = new HashSet<String>();
	        	
	        	//按行读取文件内容，一次读一整行
	            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
	            
	            //从文件中提取实体并存入队列中
	            extractEntity(reader);
	            
	            //从entityPs和entityCs提取正确估计的实体，存到entityCinPs，并更新mpc中的统计信息
	            extractCInPEntity();
	            
	            //将实体类型转成
	            Iterator it = entityType.iterator();
	            ArrayList<String> typeList = new ArrayList<String>(); 
	            while(it.hasNext()){
	            	typeList.add((String) it.next());
	            }
	            
	            ArrayList<ArrayList<Entity>> list = new ArrayList<ArrayList<Entity>>();		//存放各种实体类的容器的容器
	            for(int i=0; i< typeList.size();i++){
	            	ArrayList<Entity> entityListP = new ArrayList<Entity>();
	            	ArrayList<Entity> entityListC = new ArrayList<Entity>();
	            	ArrayList<Entity> entityListPinC = new ArrayList<Entity>();
	            	list.add(entityListP);
	            	list.add(entityListC);
	            	list.add(entityListPinC);
	            }
	            
	            for(String str:typeList){
	            	System.out.println(str);
	            }
	            
	            //按各种标签统计实体
	            for(Entity entityp:entityPs){
	            	for(int i=0; i< typeList.size();i++){
	            		if(entityp.getType().equals(typeList.get(i))){
	            			list.get(i*3).add(entityp);
	            		}
	            	}
	            }
	            
	            for(Entity entityc:entityCs){
	            	for(int i=0; i< typeList.size();i++){
	            		if(entityc.getType().equals(typeList.get(i))){
	            			list.get(i*3+1).add(entityc);
	            		}
	            	}
	            }

	            for(Entity entityCinP:entityCinPs){
	            	for(int i=0; i< typeList.size();i++){
	            		if(entityCinP.getType().equals(typeList.get(i))){
	            			list.get(i*3+2).add(entityCinP);
	            		}
	            	}
	            }
	            
	            //输出统计数据
	            DecimalFormat df = new DecimalFormat("0.00");
	            StringBuffer strOutBuf = new StringBuffer();
			    String strInfo = "length"+"\t"+"Precision"+ "\t" +
			    		"Recall" + "\t" + "\t" + "FB1" + "\t" + "\t" 
			    			+ "PCount"  + "\t" + "\t" + "CCount";
			    strOutBuf.append(strInfo + "\n");
			    for(Integer length:mc.keySet()){
			    	if(mpc.containsKey(length) && mp.containsKey(length)){
			    		double pre = (double)mpc.get(length)/(double)mp.get(length);
				    	double recall = (double)mpc.get(length)/(double)mc.get(length);
				    	double FB1 = (pre*recall*2)/(recall+pre);
				    	String str = length +":" + "\t"  + df.format(pre*100)+"%"
				    			+ "\t" + "\t" + df.format(recall*100) + 
				    				"%" + "\t" + "\t" + df.format(FB1*100) +"%" 
				    					+ "\t" + "\t" + mp.get(length)
				    						+ "\t" + "\t" + mc.get(length);
				    	strOutBuf.append(str + "\n"	);
			    	}else{
			    		String str = length + ":" + "\t" + 0 + "" +
			    				"%\t" + "\t" +0+"%\t" + "\t" + 0 +"%"
			    					+  "\t" + "\t" + 0
			    						+ "\t" + "\t" + mc.get(length);;
			    		strOutBuf.append(str + "\n");
			    	}
			    }
			    for(int i=0; i< typeList.size();i++){
	            	double precision = 0.0;
	            	double recall = 0.0;
	            	double FB1 = 0.0;
	            	if(list.get(i*3+1).size() !=0 && list.get(i*3).size()!=0){
	            		precision = (double)list.get(i*3+2).size()/(double)list.get(i*3).size();
	            		recall = (double)list.get(i*3+2).size()/(double)list.get(i*3+1).size();
	            		if((recall+precision) != 0){
	            			FB1 = (precision*recall*2)/(recall+precision);
	            		}else{
	            			FB1 = 0;
	            		}
	            	}
	            	String str = typeList.get(i) +":" + "\t"
	            			+ df.format(precision*100) + "%" + "\t" + "\t"
	            				+ df.format(recall*100) + "%" + "\t" + "\t"
	            					+ df.format(FB1*100) + "%" + "\t" + "\t"
	            						+ list.get(i*3).size() + "\t" + "\t"
	            							+ list.get(i*3 + 1).size() + "\n";
	            	strOutBuf.append(str);
	            }
			    System.out.println(strOutBuf.toString());
			    
			    reader.close();
	            if(outputPath != null ){
					File outFile =new File(outputPath);
					Writer out=new OutputStreamWriter(new FileOutputStream(outFile));
	            	out.write(strOutBuf.toString());
	                out.close();
	            }
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	        System.exit(0);
		}
	}
	
    /**
     * 提取实体的字符长度，并更新map的统计信息，
     * @param entity	实体
     * @param map		key是实体的字符长度，value是这种长度的实体个数
     */
    public static void entityToMap(Entity entity,Map<Integer,Integer> map){
    	int len = entity.getEndIndex() - entity.getStartIndex()+1;
    	if(map.containsKey(len)){
    		map.put(len, map.get(len)+1);
    	}else{
    		map.put(len, 1);
    	}
    }

    
    /**
     * 从reader中提取实体，存到相应的队列中，并统计固定长度实体的个数，存到相应的map中
     * @param reader		结果文件的流
     */
    public void extractEntity(BufferedReader reader){
        int index = 1;				//tokens在文件中的绝对位置
        int entityStartIndexC = 1;				//正确的实体的起始位置
        int entityEndIndexC = 1;
        int entityStartIndexP = 1;				//估计的实体的起始位置
        int entityEndIndexP = 1;
        
    	String line;
    	try {
			while ((line = reader.readLine()) != null) {
				if(line.equals("")){
					if(bl_entityP){
						//生成实体并保存到相应的容器
						entityEndIndexP = index-1; 
						Entity entity = new Entity(entityStartIndexP,entityEndIndexP, EntBufP.toString().trim());
						entity.setType(typeP[0]);
						entityPs.offer(entity);
						EntBufP.delete(0, EntBufP.length());
						bl_entityP = false;
						entityToMap(entity, mp);
						typeP[0] = null;
					} 
					if(bl_entityC){
						entityEndIndexC = index-1; 
						Entity entity = new Entity(entityStartIndexC,entityEndIndexC, EntBufC.toString().trim());
						entity.setType(typeC[0]);
						entityCs.offer(entity);
						EntBufC.delete(0, EntBufC.length());
						bl_entityC = false;
						entityToMap(entity, mp);
						typeP[0] = null;
					}
				}else{
					//判断实体,实体开始的边界为B-***或者S-***，结束的边界为E-***或N（O）或空白字符或B-***
					//predict
					String[] toks = line.split("\\s");
					if(toks[2].matches("B|B-\\w*") || toks[2].matches("S|S-\\w*")){
						entityType.add(toks[2]);
						if(bl_entityP){
							entityEndIndexP = index-1; 
							Entity entity = new Entity(entityStartIndexP,entityEndIndexP, EntBufP.toString().trim());
							entity.setType(typeP[0]);
							entityPs.offer(entity);
							EntBufP.delete(0, EntBufP.length());
							entityToMap(entity, mp);
							typeP[0] = null;
						}
						typeP[0] = toks[2];
						bl_entityP = true;
						entityStartIndexP = index;
						EntBufP.append(toks[0]);
					}else if(toks[2].matches("I|I-\\w*") || toks[2].matches("M|M-\\w*")){
						EntBufP.append(toks[0]);
					}else if(toks[2].matches("E|E-\\w*")){
						if(bl_entityP){
							entityEndIndexP = index-1; 
							Entity entity = new Entity(entityStartIndexP,entityEndIndexP, EntBufP.toString().trim());
							entity.setType(typeP[0]);
							entityPs.offer(entity);
							EntBufP.delete(0, EntBufP.length());
							entityToMap(entity, mp);
							bl_entityP = false;
							typeP[0] = null;
						}
					}else{
						if(bl_entityP){
							entityEndIndexP = index-1; 
							Entity entity = new Entity(entityStartIndexP,entityEndIndexP, EntBufP.toString().trim());
							entity.setType(typeP[0]);
							entityPs.offer(entity);
							EntBufP.delete(0, EntBufP.length());
							bl_entityP = false;
							entityToMap(entity, mp);
							typeP[0] = null;
						}
					}
					
					//correct
					if(toks[1].matches("B|B-\\w*") || toks[1].matches("S|S-\\w*")){
						entityType.add(toks[1]);
						if(bl_entityC){
							entityEndIndexC = index-1; 
							Entity entity = new Entity(entityStartIndexC,entityEndIndexC, EntBufC.toString().trim());
							entity.setType(typeC[0]);
							entityCs.offer(entity);
							EntBufC.delete(0, EntBufC.length());
							entityToMap(entity, mc);
							typeC[0] = null;
						}
						typeC[0] = toks[1];
						bl_entityC = true;
						entityStartIndexC = index;
						EntBufC.append(toks[0]);
					}else if(toks[1].matches("I|I-\\w*") || toks[1].matches("M|M-\\w*")){
						EntBufC.append(toks[0]);
					}else if(toks[1].matches("E|E-\\w*")){
						if(bl_entityC){
							entityEndIndexC = index-1; 
							Entity entity = new Entity(entityStartIndexC,entityEndIndexC, EntBufC.toString().trim());
							entity.setType(typeC[0]);
							entityCs.offer(entity);
							EntBufC.delete(0, EntBufC.length());
							entityToMap(entity, mc);
							bl_entityC = false;
							typeC[0] = null;
						}
					}else{
						if(bl_entityC){
							entityEndIndexC = index-1; 
							Entity entity = new Entity(entityStartIndexC,entityEndIndexC, EntBufC.toString().trim());
							entity.setType(typeC[0]);
							entityCs.offer(entity);
							EntBufC.delete(0, EntBufC.length());
							bl_entityC = false;
							entityToMap(entity, mc);
							typeC[0] = null;
						}
					}
				}
				++index;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /*
    public void createEntity(int index,int entityStartIndex,StringBuffer EntBuf,
    		Queue<Entity> entities,Map<Integer,Integer> map,String[] type){
		int entityEndIndex = index-1; 
		Entity entity = new Entity(entityStartIndex,entityEndIndex, EntBuf.toString().trim());
		entity.setType(type[0]);
		entities.offer(entity);
		EntBuf.delete(0, EntBuf.length());
		entityToMap(entity, map);
		type[0] = null;
	}
    */
    
    /**
     * 提取在估计中正确的实体，存到entityCinPs中，并将长度个数统计信息存到mpc中
     */
    public void extractCInPEntity(){
    		Queue<Entity> entityCstmp = new LinkedList<Entity>();;
    		//得到在predict中正确的Pc
			for(Entity entityp:entityPs){
				while(!entityCs.isEmpty()){
					Entity entityc = entityCs.peek();
					if(entityp.getStartIndex() == entityc.getStartIndex()){
						if(entityp.getEndIndex() == entityc.getEndIndex()){
							entityCinPs.add(entityp);
							entityToMap(entityp, mpc);
						}
						entityCstmp.offer(entityCs.poll());
						break;
					}else if(entityp.getStartIndex() > entityc.getStartIndex()){
						entityCstmp.offer(entityCs.poll());
					}else{
						break;
					}
				}
			}
           
           /*
			for(Entity entityp:entityPs){
				while(!entityCs.isEmpty() && 
						entityp.getStartIndex() > entityCs.peek().getStartIndex()){
					entityCs.poll();
				}
				Entity entityc = entityCs.peek();
				if(entityp.getStartIndex() == entityc.getStartIndex()){
					if(entityp.getEndIndex() == entityc.getEndIndex()){
						entityCinPs.add(entityp);
						entityToMap(entityp, mpc);
					}
					entityCstmp.offer(entityCs.poll());
				}
			}
			*/
           for(Entity entityp:entityCstmp){
           	entityCs.offer(entityp);
           }
    }
    
    public static void main(String[] args){
    	
		String filePath = null;
		String outputPath = null;
		if(args.length >0){
			if(args[0].equals("-h")){
				System.out.println("NeSatistic.jar 要评测的文件	[输出到文件]");
			}else{
				filePath = args[0];
			}
		}
		
		if(args.length == 2){
			outputPath = args[1];
		}
		
//		filePath ="C:\\Users\\fxx\\Desktop\\实验\\result_test\\resultMsra_test";
		
		//读取评测结果文件，并输出到outputPath
		NESatistic ne1 = new NESatistic();
		ne1.NeEvl(filePath,outputPath);
	}
}
