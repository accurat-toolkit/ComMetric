package leeds.cts.nlp;

import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


/** This is a demo of calling CRFClassifier programmatically.
 *  <p>
 *  Usage: <code> java -cp "stanford-ner.jar:." NERDemo [serializedClassifier [fileName]]</code>
 *  <p>
 *  If arguments aren't specified, they default to
 *  ner-eng-ie.crf-3-all2006.ser.gz and some hardcoded sample text.
 *  <p>
 *  To use CRFClassifier from the command line:
 *  java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier
 *      [classifier] -textFile [file]
 *  Or if the file is already tokenized and one word per line, perhaps in
 *  a tab-separated value format with extra columns for part-of-speech tag,
 *  etc., use the version below (note the 's' instead of the 'x'):
 *  java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier
 *      [classifier] -testFile [file]
 *
 *  @author Jenny Finkel
 *  @author Christopher Manning
 */
public class NER {

	 public static void main(String[] args) throws IOException {
		 NER a=new NER();
	//	 a.SimNER();
		 a.NERbyString("/home/fzsu/temp/en-zh/en.txt", "/home/fzsu/temp/en-zh/temp/CHINESE-translation.txt","/home/fzsu/temp/en-zh/temp");
	
		    }

	 


/*
 * read the English text string as input for NER.
 */
public void NERbyString(String SourcePath, String TargetPath, String result){
	 try{
	//String serializedClassifier = "/home/fzsu/stanford-ner-2011-06-19/classifiers/all.3class.distsim.crf.ser.gz"; // try two different training dataset
	//String serializedClassifier = "/home/fzsu/stanford-ner-2011-06-19/classifiers/conll.4class.distsim.crf.ser.gz";
		System.out.println("Start named entity extraction ...");
		File file = new File(".");  
		String CurrentPath = file.getCanonicalPath();
		String serializedClassifier=CurrentPath+File.separator+"conll.4class.distsim.crf.ser.gz"; 
		AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
       BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(result+File.separator+"ner.info")),"UTF-8"));
       GetNERbyStr(SourcePath,classifier,bw);
       GetNERbyStr(TargetPath,classifier,bw);
	    bw.flush();
	    bw.close();
	System.out.println("Named entity extraction is done");
	 }catch(Exception ex){
		 ex.printStackTrace();
	 }
}

public void GetNERbyStr(String path, AbstractSequenceClassifier classifier, BufferedWriter bw){
	try{ 
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
		String s="";
	    while (true){
		 s=br.readLine();
		 if (s==null){
		    		break;
		    	}else{
					 File f5=new File(s);
					 String fullname=f5.getName();
					 if (!s.contains("###")){
						 fullname="";
						 String names[]=s.replaceAll("\\\\","/").split("/");
						    if (names[0].contains(":")){
						    	names[0]=names[0].replace(":", "@@@");
						    }
							for (int k=0;k<names.length-1;k++){
								fullname=fullname+names[k]+"###";
							}
							fullname=fullname+names[names.length-1];
					 }
	        	 BufferedReader br1=new BufferedReader(new InputStreamReader(new FileInputStream(s), "UTF8"));	
	        	 ArrayList id=new ArrayList();
	    		 ArrayList idCount=new ArrayList();
		         while (true){
		        	 s=br1.readLine();
		        	 if (s==null){
		        		 break;
		        	 }else{
		        		 if (s.trim().length()>0){
		        			 String res=classifier.classifyToString(s);
		        		     String t[]=res.split("\\ ");
		        		     for (int i=0;i<t.length;i++){
		        		    	 if (t[i].contains("/")){
		        		    		 String str[]=t[i].split("\\/");
		        		    		  	if (str[1].contains("LOCATION") ||str[1].contains("PERSON")||str[1].contains("ORGANIZATION") ||str[1].contains("MISC") ){  
		      				        	  if (id.contains(str[0])){
		      				        		 int p=id.indexOf(str[0]);
		      				        	     int value=(Integer)idCount.get(p)+1;
		      				        	     idCount.set(p, value);
		      				              }else{
		      				        	     id.add(str[0]);
		      				        	     idCount.add(1);
		      				              }
		      				        	}
		        		    	 }
		        		     }
		        		 }
		        	 }
		         }
		         if (id.size()>0){
		        	 String ss=fullname;
		        	 for (int i=0;i<id.size();i++){
		        		 ss=ss+"\t"+id.get(i).toString()+" "+idCount.get(i).toString();
		        	 }
		        	 bw.write(ss);
		        	 bw.newLine();
		         }
		    	}
	        }
	     
	}catch(Exception ex){
		ex.printStackTrace();
	}
}

/*
 * compute the similarity from the named entities by cosine
 * with alignment file ready
 */


public void SimNER(String nerPath, String alignPath, String result){
	try{
	//	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("/home/fzsu/ICC-stem/SL-EN/sl-en.NER"), "UTF8"));
	//	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("/home/fzsu/ICC-translation/RO-DE/ro-de.NER"), "UTF8"));
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(nerPath), "UTF8"));
        String s="";
        ArrayList DocName=new ArrayList();
        ArrayList alist=new ArrayList();
        while (true){
        	s=br.readLine();
        	if (s==null){
        		break;
        	}else{
        		String t[]=s.split("\\t");
        		/*    String str[]=t[0].split("\\.");
				String name=str[0];
				// for de-en only
				if (str[0].contains("_de_en")){
					name="";
					String a[]=str[0].split("\\_");
					for (int i=0;i<a.length-3;i++){
						name=name+a[i]+"-";
					}
					name=name+"de-"+a[a.length-3];	
				} 
        		DocName.add(name); */
				DocName.add(t[0]);
        	    alist.add(s);
        		if (t.length<=2){
        			System.out.println(s);
        		}
        	}
        }
    //    BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/all/comparability-info.txt"));  
	//    BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-stem/SL-EN/all/sl-en-NER.cosine"));
   //     BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.label"));  
	//    BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/RO-DE/ro-de-NER.cosine"));
        BufferedReader br1=new BufferedReader(new FileReader(alignPath));  
        BufferedWriter bw=new BufferedWriter(new FileWriter(result));
        int count=0;
	    while (true){
		s=br1.readLine();
		if (s==null){
		    break;
		}else{
		    String t[]=s.split("\\ ");
		    /*  if (DocName.contains(t[1])&&DocName.contains(t[2])){
			int p=DocName.indexOf(t[1]);
			int q=DocName.indexOf(t[2]); */
		  /*  if (!t[0].contains(".txt")){
			    t[0]=t[0]+".txt";
			    }
		    if (!t[1].contains(".txt")){
			    t[1]=t[1]+".txt";
			    }  */
		/*    if (t[0].contains("/")){  // for ET-EN, lt-en datasets and LV-EN dataset
		    	String str[]=t[0].split("\\/");
		    	t[0]=str[2];
		    }
		    if (!t[0].contains(".txt")){
		    t[0]=t[0]+".txt";
		    }
		    if (t[1].contains("/")){
		    	String str[]=t[1].split("\\/");
		    	t[1]=str[2];
		    }
		    if (!t[1].contains(".txt")){
			    t[1]=t[1]+".txt";
			    }   */
		    if (DocName.contains(t[0])&&DocName.contains(t[1])){
		    String ss=t[0]+" "+t[1]+" "+t[2];
			int p=DocName.indexOf(t[0]);  
			int q=DocName.indexOf(t[1]); 
            String s1=alist.get(p).toString();
			String t1[]=s1.split("\\t");
			String s2=alist.get(q).toString();
			String t2[]=s2.split("\\t");
			if (t1.length>1 &&t2.length>1){
				ArrayList value1=new ArrayList();
				ArrayList id1=new ArrayList();
				for (int k=1;k<t1.length;k++){
				    String w[]=t1[k].split("\\ ");
				    id1.add(w[0]);
				    value1.add(w[1]);
				}
				ArrayList id2=new ArrayList();
				ArrayList value2=new ArrayList();
				for (int k=1;k<t2.length;k++){
				    String w[]=t2[k].split("\\ ");
				    id2.add(w[0]);
				    value2.add(w[1]);
				}
				double sim=GETcosine(id1,value1,id2,value2);
				ss=ss+" "+sim; // for ET-EN dataset and LV-EN dataset
			//	String ss=s+" "+sim;
				
			}
			else{
				ss=ss+" "+0;
			//	System.out.println(ss);
			}
			bw.write(ss);
			bw.newLine();
			count++;
		    }else{
		    	System.out.println("no in the file list:"+s);
		    }
		}
	    }
	    bw.flush();
	    bw.close();  
	    System.out.println(count+" all done!!!");  
	}catch(Exception ex){
	    ex.printStackTrace();
	}
    }


public void SimNERWithoutAlignment(String nerPath, String source, String target, String result){
	try{
	//	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("/home/fzsu/ICC-stem/SL-EN/sl-en.NER"), "UTF8"));
	//	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("/home/fzsu/ICC-translation/RO-DE/ro-de.NER"), "UTF8"));
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(nerPath), "UTF8"));
        String s="";
        ArrayList DocName=new ArrayList();
        ArrayList alist=new ArrayList();
        while (true){
        	s=br.readLine();
        	if (s==null){
        		break;
        	}else{
        		String t[]=s.split("\\t");
        		/*    String str[]=t[0].split("\\.");
				String name=str[0];
				// for de-en only
				if (str[0].contains("_de_en")){
					name="";
					String a[]=str[0].split("\\_");
					for (int i=0;i<a.length-3;i++){
						name=name+a[i]+"-";
					}
					name=name+"de-"+a[a.length-3];	
				} 
        		DocName.add(name); */
				DocName.add(t[0]);
        	    alist.add(s);
        		if (t.length<=2){
        			System.out.println(s);
        		}
        	}
        }
    //    BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/all/comparability-info.txt"));  
	//    BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-stem/SL-EN/all/sl-en-NER.cosine"));
   //     BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.label"));  
	//    BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/RO-DE/ro-de-NER.cosine"));
        BufferedReader br1=new BufferedReader(new FileReader(source));
         ArrayList sDoc=new ArrayList();
         while (true){
        	 s=br1.readLine();
        	 if (s==null){
        		 break;
        	 }else{
        		 String t[]=s.replaceAll("\\\\","/").split("/");
        		 sDoc.add(t[t.length-1]);
        	 }
         }
         BufferedReader br2=new BufferedReader(new FileReader(target));
         ArrayList tDoc=new ArrayList();
         while (true){
        	 s=br2.readLine();
        	 if (s==null){
        		 break;
        	 }else{
        		 String t[]=s.replaceAll("\\\\","/").split("/");
        		 tDoc.add(t[t.length-1]);
        	 }
         }
         BufferedWriter bw=new BufferedWriter(new FileWriter(result));
           for (int i=0;i<sDoc.size();i++){
        	   for (int j=0;j<tDoc.size();j++){
		  
		    if (DocName.contains(sDoc.get(i).toString())&&DocName.contains(tDoc.get(j).toString())){
		    String ss=sDoc.get(i).toString()+" "+tDoc.get(j).toString();
			int p=DocName.indexOf(sDoc.get(i).toString());  
			int q=DocName.indexOf(tDoc.get(j).toString()); 
            String s1=alist.get(p).toString();
			String t1[]=s1.split("\\t");
			String s2=alist.get(q).toString();
			String t2[]=s2.split("\\t");
			if (t1.length>1 &&t2.length>1){
				ArrayList value1=new ArrayList();
				ArrayList id1=new ArrayList();
				for (int k=1;k<t1.length;k++){
				    String w[]=t1[k].split("\\ ");
				    id1.add(w[0]);
				    value1.add(w[1]);
				}
				ArrayList id2=new ArrayList();
				ArrayList value2=new ArrayList();
				for (int k=1;k<t2.length;k++){
				    String w[]=t2[k].split("\\ ");
				    id2.add(w[0]);
				    value2.add(w[1]);
				}
				double sim=GETcosine(id1,value1,id2,value2);
				ss=ss+" "+sim; // for ET-EN dataset and LV-EN dataset
			//	String ss=s+" "+sim;
				
			}
			else{
				ss=ss+" "+0;
			//	System.out.println(ss);
			}
			bw.write(ss);
			bw.newLine();
		    }
        	else{
		    	System.out.println("no in the file list:"+s);
		    }
        	   }
		}
	    
	    bw.flush();
	    bw.close();  
	    System.out.println(" all done!!!");  
	}catch(Exception ex){
	    ex.printStackTrace();
	}
    }



public double GETcosine(ArrayList ID1, ArrayList Val1, ArrayList ID2, ArrayList Val2){
	  
	double sim=0;
	double sum1=0;
	for (int i=0;i<Val1.size();i++){
	    double v1=Double.parseDouble(Val1.get(i).toString());
	    sum1=sum1+v1*v1;
	}
	double sum2=0;
	for (int i=0;i<Val2.size();i++){
	    double v2=Double.parseDouble(Val2.get(i).toString());
	    sum2=sum2+v2*v2;
	}
	for (int i=0;i<ID1.size();i++){
	    if (ID2.contains(ID1.get(i).toString())){
		int p=ID2.indexOf(ID1.get(i).toString());
		double v1=Double.parseDouble(Val1.get(i).toString());
		double v2=Double.parseDouble(Val2.get(p).toString());
		sim=sim+v1*v2;
	    }
	}
	sim=sim/(Math.sqrt(sum1)*Math.sqrt(sum2));
	// System.out.println(sim);
	sim=Math.floor(sim*10000+0.5)/10000;
	//  System.out.println(sim);
	return sim;	 
    }
}
