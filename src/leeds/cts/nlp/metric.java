package leeds.cts.nlp;

import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.URL;



import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.mit.jwi.morph.SimpleStemmer;


class  nonENSourceVector implements Runnable {
	String input="";
	ArrayList stopword;
	String docname="";
	BufferedWriter bw;
	private CountDownLatch threadsSignal; 
//	ConcurrentLinkedQueue<String> queue;
	public nonENSourceVector(ArrayList stopword, BufferedWriter bw, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
//	public textvectorization(ArrayList stopword, ConcurrentLinkedQueue<String> queue, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
	{
   	  this.input=input;
	  this.stopword=stopword;
	  this.docname=docname;
	//  this.queue=queue;
	  this.bw=bw;
	  this.threadsSignal=threadsSignal;
	 }
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void run(){
	   try{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
       ArrayList word=new ArrayList();
       ArrayList count=new ArrayList();
       String s="";
  	 while (true){
      	 s=br.readLine();
      	 if (s==null){
      		 break;
      	 }else{
      		 String t[]=s.split("\\ ");
      		 for (int j=0;j<t.length;j++){
      			 if (t[j].length()>2){	
      				 t[j]=t[j].toLowerCase();
      		//		 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
      					 if (!stopword.contains(t[j])){
      						   if (!word.contains(t[j])){
      							   word.add(t[j]);
      							   count.add(1);
      						 }else{
      							 int p=word.indexOf(t[j]);
      							 int num=(Integer)count.get(p)+1;
      							 count.set(p, num);
      						 }
      					 }
      			//	 }
      			 }
      		 }
      	 }
       }
  	 String ss=docname;    	
  	 for (int k=0;k<word.size();k++){        		
  		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight             		
  	 }
  	 write(ss);
  //	 bw.write(ss);
  //	 bw.newLine();
  //	 queue.add(ss);
  	 br.close();
  	threadsSignal.countDown();
   }catch(Exception ex){
	   ex.printStackTrace();
   }
}
}



class nonENTargetVector implements Runnable{
	String input="";
	ArrayList stopword;
	BufferedWriter bw;
	private CountDownLatch threadsSignal; 
	public nonENTargetVector(ArrayList stopword, BufferedWriter bw,String input,CountDownLatch threadsSignal){
		this.input=input;
		this.stopword=stopword;
		this.bw=bw;
		this.threadsSignal=threadsSignal;
	}
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void run(){
		try{
			String names[]=input.replaceAll("\\\\","/").split("/");	
			String fullname="";
			if (names[0].contains(":")){
				names[0]=names[0].replace(":", "@@@");
			}
			for (int k=0;k<names.length-1;k++){
				fullname=fullname+names[k]+"###";
			} 
			fullname=fullname+names[names.length-1]; 
			
			BufferedReader br2=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
			String s=""; 
			ArrayList word=new ArrayList();
          ArrayList count=new ArrayList();
			while (true){
       	 s=br2.readLine();
       	 if (s==null){
       		 break;
       	 }else{
       		 if (s.length()>0){
               	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
            		while (tokenizer.hasNext()) {
            		  CoreLabel token = tokenizer.next();
            		  String tok=token.word().toLowerCase();
            		  String tokens[]=tok.split("\\ "); //<a href...> is a token in the standford tokenizer, but contains space
            		  for (int i=0;i<tokens.length;i++){	  
            		  if (tokens[i].length()>2 &&!tokens[i].startsWith("-")){
            				 if (!stopword.contains(tokens[i])){
      						   if (!word.contains(tokens[i])){
      							   word.add(tokens[i]);
      							   count.add(1);
      						 }else{
      							 int p=word.indexOf(tokens[i]);
      							 int number=(Integer)count.get(p)+1;
      							 count.set(p, number);
      						 }
      					 }
            		   }
            		}
            		}
       		 }
       	 }
			}
	String ss=fullname;
	 for (int k=0;k<word.size();k++){
		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight
	 }
	 write(ss);
	 br2.close();
	 threadsSignal.countDown(); 
	}catch(Exception ex){
		ex.printStackTrace();
	}
	}
}



class textvectorization  implements Runnable {
	String input="";
	ArrayList stopword;
	String docname="";
	BufferedWriter bw;
	private CountDownLatch threadsSignal; 
//	ConcurrentLinkedQueue<String> queue;
	public textvectorization(ArrayList stopword, BufferedWriter bw, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
//	public textvectorization(ArrayList stopword, ConcurrentLinkedQueue<String> queue, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
	{
   	  this.input=input;
	  this.stopword=stopword;
	  this.docname=docname;
	//  this.queue=queue;
	  this.bw=bw;
	  this.threadsSignal=threadsSignal;
	 }
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void run(){
	   try{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
       ArrayList word=new ArrayList();
       ArrayList count=new ArrayList();
       String s="";
  	 while (true){
      	 s=br.readLine();
      	 if (s==null){
      		 break;
      	 }else{
      		 String t[]=s.split("\\ ");
      		 for (int j=0;j<t.length;j++){
      			 if (t[j].length()>2){	
      				 t[j]=t[j].toLowerCase();
      				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
      					 if (!stopword.contains(t[j])){
      						   if (!word.contains(t[j])){
      							   word.add(t[j]);
      							   count.add(1);
      						 }else{
      							 int p=word.indexOf(t[j]);
      							 int num=(Integer)count.get(p)+1;
      							 count.set(p, num);
      						 }
      					 }
      				 }
      			 }
      		 }
      	 }
       }
  	 String ss=docname;    	
  	 for (int k=0;k<word.size();k++){        		
  		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight             		
  	 }
  	 write(ss);
  //	 bw.write(ss);
  //	 bw.newLine();
  //	 queue.add(ss);
  	 br.close();
  	threadsSignal.countDown();
   }catch(Exception ex){
	   ex.printStackTrace();
   }
}
}
class cosine  implements Runnable {
//	ArrayList DocName;
//	ArrayList alist;
	Hashtable ht;
	Hashtable sent;
	Hashtable content;
	Hashtable keyword;
	Hashtable ne;
	String sn="";
	String tn="";
	BufferedWriter bw;
	double threshold=0;
	CountDownLatch threadSignal;
//	public cosine(BufferedWriter bw, ArrayList DocName, ArrayList alist,String sn,String tn,double threshold,CountDownLatch threadSignal){
	public cosine(BufferedWriter bw, Hashtable ht, Hashtable sent,Hashtable content,Hashtable keyword,Hashtable ne,String sn,String tn,double threshold,CountDownLatch threadSignal){
	//	this.DocName=DocName;
		this.sn=sn;
		this.tn=tn;
	//	this.alist=alist;
		this.ht=ht;
		this.sent=sent;
		this.content=content;
		this.keyword=keyword;
		this.ne=ne;
		this.bw=bw;
		this.threshold=threshold;
		this.threadSignal=threadSignal;
	}
	
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
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
			sim=Math.floor(sim*1000+0.5)/1000;
			//  System.out.println(sim);
			return sim;				 
		    }
	 
	 public double SentSim(int sentnum1, int sentnum2, int wordnum1,int wordnum2){
		 
			 double v1=0;
			 double v2=0;
			 if (sentnum1>sentnum2){
				 v1=(double)sentnum2/sentnum1;
			 }else{
				 v1=(double)sentnum1/sentnum2;
			 }
			 if (wordnum1>wordnum2){
				 v2=(double)wordnum2/wordnum1;
			 }else{
				 v2=(double)wordnum1/wordnum2;
			 }
			 double sim=0; 
			 sim=0.5*v1+0.5*v2;
			 sim=Math.floor(sim*1000+0.5)/1000;
			return sim;
		 
	 }
	   public void run(){
		   try {
			   if (ht.containsKey(sn) &&ht.containsKey(tn)){
				   // compute lexical similarity
				   String s1=ht.get(sn).toString();
					String t1[]=s1.split("\\	");
					ArrayList value1=new ArrayList();
					ArrayList id1=new ArrayList();
					for (int k=1;k<t1.length;k++){
					    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
					    id1.add(w[0]);
					    value1.add(w[1]);
					}
					String s2=ht.get(tn).toString();
					String t2[]=s2.split("\\	");
					ArrayList id2=new ArrayList();
					ArrayList value2=new ArrayList();
					for (int k=1;k<t2.length;k++){
					    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
					    id2.add(w[0]);
					    value2.add(w[1]);
					}
					double lexsim=GETcosine(id1,value1,id2,value2);
					String snames[]=sn.split("###");
					if (snames[0].contains("@@@")){
						snames[0]=snames[0].replace("@@@", ":");
					}
					String sname=snames[0];
					for (int k=1;k<snames.length;k++){
						sname=sname+File.separator+snames[k];
					}
					String tnames[]=tn.split("###");
					if (tnames[0].contains("@@@")){
						tnames[0]=tnames[0].replace("@@@", ":");
					}
					String tname=tnames[0];
					for (int k=1;k<tnames.length;k++){
						tname=tname+File.separator+tnames[k];
					}
					
					// compute structural similarity
					double strucsim=0;
					if(sent.containsKey(sn) &&sent.containsKey(tn)&&content.containsKey(sn) &&content.containsKey(tn)){
						String sent1=sent.get(sn).toString();
						String sent2=sent.get(tn).toString();
						String word1=content.get(sn).toString();
						String word2=content.get(tn).toString();
					   String a[]=sent1.split("\t");
					   String b[]=sent2.split("\t");
					   String c[]=word1.split("\t");
					   String d[]=word2.split("\t");
					   int sentnum1=Integer.parseInt(a[1]);
					   int sentnum2=Integer.parseInt(b[1]);
					   int wordnum1=Integer.parseInt(c[1]);
					   int wordnum2=Integer.parseInt(d[1]);
					   strucsim=SentSim(sentnum1,sentnum2,wordnum1,wordnum2);   
					}
					
					// compute keyword similarity
					double keysim=0;
					if(keyword.containsKey(sn) &&keyword.containsKey(tn)){
						String keyword1=keyword.get(sn).toString();
						String keyword2=keyword.get(tn).toString();
						String keys1[]=keyword1.split("\t");
						String keys2[]=keyword2.split("\t");
						if (keys1.length>1 &&keys2.length>1){
						ArrayList word1=new ArrayList();
						ArrayList wcount1=new ArrayList();
						int len1=21;
						if (keys1.length<=21){
							len1=keys1.length;
						}
						for (int i=1;i<len1;i++){
							  String w[]=keys1[i].split("\\ ");
							  String u[]=w[1].split(":");
							  word1.add(w[0]);
							  wcount1.add(u[1]); 
						}
						
						ArrayList word2=new ArrayList();
						ArrayList wcount2=new ArrayList();
						int len2=21;
						if (keys2.length<=21){
							len2=keys2.length;
						}
						for (int i=1;i<len2;i++){
							  String w[]=keys2[i].split("\\ ");
							  String u[]=w[1].split(":");
							  word2.add(w[0]);
							  wcount2.add(u[1]); 
						}
						
						keysim=GETcosine(word1,wcount1,word2,wcount2);
						}	
					}
					
					//compute named entity similarity
					double nersim=0;
					if(ne.containsKey(sn) &&ne.containsKey(tn)){
						String ne1=ne.get(sn).toString();
						String ne2=ne.get(tn).toString();
					    String ners1[]=ne1.split("\t");
					    String ners2[]=ne2.split("\t");
					    if (ners1.length>1 &&ners2.length>1){
					    	ArrayList word1=new ArrayList();
					    	ArrayList wcount1=new ArrayList();
					    	for (int i=1;i<ners1.length;i++){
					    		String w[]=ners1[i].split("\\ ");
					    		word1.add(w[0]);
					    		wcount1.add(w[1]);
					    	}
					    	ArrayList word2=new ArrayList();
					    	ArrayList wcount2=new ArrayList();
					    	for (int i=1;i<ners2.length;i++){
					    		String w[]=ners2[i].split("\\ ");
					    		word2.add(w[0]);
					    		wcount2.add(w[1]);
					    	}
					    	nersim=GETcosine(word1,wcount1,word2,wcount2);
					    }
					}
					
					//hybrid of 4 independent similarity
					double sim=lexsim;
					if (nersim>=0 &&keysim>=0){
						sim=0.5*lexsim+0.2*keysim+0.2*nersim+0.1*strucsim;
						 sim=Math.floor(sim*1000+0.5)/1000;
					}
				//	String s=sname+"\t"+tname+"\t"+sim+"\t"+lexsim+"\t"+keysim+"\t"+nersim+"\t"+strucsim; 
					String s=sname+"\t"+tname+"\t"+sim;
					if (sim>=threshold){
					write(s);
					}    
				    }
			    
		   threadSignal.countDown();
	   }catch(Exception ex){
			   ex.printStackTrace();
		   }
	   }
	}



class nonENCosine implements Runnable{
	Hashtable ht;
	BufferedWriter bw;
	String sn="";
	String tn="";
	double threshold=0;
	private CountDownLatch threadsSignal;
   public nonENCosine(Hashtable ht, BufferedWriter bw, String sn, String tn, double threshold,CountDownLatch threadsSignal){
	   this.ht=ht;
	   this.bw=bw;
	   this.sn=sn;
	   this.tn=tn;
	   this.threshold=threshold;
	   this.threadsSignal=threadsSignal;
   }
   
   public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
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
	public void run(){
		try{
			   if (ht.containsKey(sn)&&ht.containsKey(tn)){ 
					//    System.out.println(sdoc[i].getName()+" "+tdoc[j].getName());
					   
			            String s1=ht.get(sn).toString();
						String t1[]=s1.split("\\	");
						ArrayList value1=new ArrayList();
						ArrayList id1=new ArrayList();
						for (int k=1;k<t1.length;k++){
						    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
						    id1.add(w[0]);
						    value1.add(w[1]);
						}
						String s2=ht.get(tn).toString();
						String t2[]=s2.split("\\	");
						ArrayList id2=new ArrayList();
						ArrayList value2=new ArrayList();
						for (int k=1;k<t2.length;k++){
						    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
						    id2.add(w[0]);
						    value2.add(w[1]);
						}
						double sim=GETcosine(id1,value1,id2,value2);
						String snames[]=sn.split("###");
						if (snames[0].contains("@@@")){
							snames[0]=snames[0].replace("@@@", ":");
						}
						String sname=snames[0];
						for (int k=1;k<snames.length;k++){
							sname=sname+File.separator+snames[k];
						}
						String tnames[]=tn.split("###");
						if (tnames[0].contains("@@@")){
							tnames[0]=tnames[0].replace("@@@", ":");
						}
						String tname=tnames[0];
						for (int k=1;k<tnames.length;k++){
							tname=tname+File.separator+tnames[k];
						}
						String s=sname+"	"+tname+"	"+sim; 
						if (sim>=threshold){
						write(s);
						}
					    }
			   threadsSignal.countDown();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}

public class metric {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		metric a=new metric();
	//	System.out.println("get it in running");
	//	int x=5;
	//	int y=7;
	//	System.out.println((double)x/y);
	/*	a.Dictstemmer("CHINESE","/home/fzsu/temp/en-zh/temp/CHINESE-translation.txt","/home/fzsu/WordNet-3.0","/home/fzsu/temp/en-zh/temp");
		a.Dictstemmer("ENGLISH","/home/fzsu/temp/en-zh/en.txt","/home/fzsu/WordNet-3.0","/home/fzsu/temp/en-zh/temp");
		a.Dictnewtext2vectors("CHINESE","ENGLISH","/home/fzsu/temp/en-zh/temp");
		AddFeature feature=new AddFeature();
		feature.sentenceRecognition("/home/fzsu/temp/en-zh/en.txt","/home/fzsu/temp/en-zh/zh.txt","/home/fzsu/temp/en-zh/temp");
		feature.POStagging("CHINESE","/home/fzsu/temp/en-zh/temp/CHINESE-translation.txt","/home/fzsu/temp/en-zh/temp");
		feature.POStagging("ENGLISH","/home/fzsu/temp/en-zh/en.txt", "/home/fzsu/temp/en-zh/temp");
		 feature.NumberOfContentWords("CHINESE","ENGLISH","/home/fzsu/temp/en-zh/temp");
		 feature.KeyWordviaTFIDF("/home/fzsu/temp/en-zh/temp");
		 NER ne=new NER();
		 ne.NERbyString("/home/fzsu/temp/en-zh/temp/CHINESE-translation.txt","/home/fzsu/temp/en-zh/en.txt","/home/fzsu/temp/en-zh/temp");
		a.DictSelectedCOSINESimilarityWithoutDocPair("/home/fzsu/temp/en-zh/temp","/home/fzsu/temp/en-zh/temp"+File.separator+"CHINESE-stem", "/home/fzsu/temp/en-zh/temp"+File.separator+"ENGLISH-stem", "/home/fzsu/temp/en-zh/temp/result.txt", 0); */
	
		
		if (args.length!=18){
			System.out.println("Usage: java -jar ComMetric.jar --source SourceLanguage --target TargetLanguage --WN Path2WordNet --threshold value --translationAPI [google|bing] --input path2SourceFileList --input path2TargetFileList --output path2result --tempDir path2TemporaryDirectory");
			System.out.println("--source SourceLanguage: any supported language by the chosen translation API");
			System.out.println("--target TargetLanguage: any supported language by the chosen translation API");
			System.out.println("--WN path2WordNet: the full path to the WordNet installation directory");
			System.out.println("--threshold value: output the document pairs with a comparability score >= threshold");
			System.out.println("--translationAPI [google|bing|dfki]: use either google, bing or DFKI translation API");
			System.out.println("--input path2SourceFileList: path to the file that lists the full path to the documents in source language");
			System.out.println("--input path2TargetFileList: path to the file that lists the full path to the documents in target language");
			System.out.println("--output path2result: path to the file that store comparable document pairs with comparability scores");
			System.out.println("--tempDir path2TemporaryDirectory: specify a path to a temporary directory (must exist) for storing intermediate outputs ");
		//	System.out.println("Example (Linux): java -jar ComMetric.jar -SL /home/fzsu/Metric/sample/EN -TL /home/fzsu/Metric/sample/LV-translation -WN /usr/local/WordNet-3.0 -AF /home/fzsu/Metric/alignment.txt -threshold 0.4 -TP /home/fzsu/Metric");
		//	System.out.println("Example (Windows): java -jar ComMetric.jar -SL C:\\Metric\\sample\\EN -TL C:\\Metric\\sample\\LV-translation -WN C:\\WordNet\\2.1 -AF C:\\Metric\\alignment.txt -threshold 0.4 -TP C:\\Metric");
		}else{
			String SL=args[1].toUpperCase();
			String TL=args[3].toUpperCase();
			String WN=args[5];
			double threshold=Double.parseDouble(args[7]);
			String api=args[9].toLowerCase();
			String SP=args[11];
			String TP=args[13];
			String result=args[15];
			String tempDir=args[17];
			if (api.equals("google") ||(api.equals("bing"))){
			if (api.equals("google")){
				googleTranslation gt=new googleTranslation();
				gt.translationList(SL, "ENGLISH", SP, tempDir);
				if (!TL.equals("ENGLISH")){
					gt.translationList(TL, "ENGLISH", TP, tempDir);
				}
			}
			if (api.equals("bing")){
				BingTranslation bt=new BingTranslation();
				bt.translationList(SL, "ENGLISH", SP, tempDir);
				if (!TL.equals("ENGLISH")){
					bt.translationList(TL, "ENGLISH", TP, tempDir);
				}
			} 
			a.stemmer(SL,tempDir+File.separator+SL+"-translation.txt",WN,tempDir);
			if (!TL.equals("ENGLISH")){
			//	a.stemmer(TL,tempDir+File.separator+TL+"-translation.txt",WN,tempDir);
				a.Dictstemmer(TL,tempDir+File.separator+TL+"-translation.txt",WN,tempDir);
			}else{
			//	a.stemmer(TL,TP,WN,tempDir);
				a.Dictstemmer(TL,TP,WN,tempDir);
			}
		
		//	a.newtext2vectors(SL, TL, tempDir);
		//	a.SelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
			a.Dictnewtext2vectors(SL, TL, tempDir);
			AddFeature feature=new AddFeature();
			feature.sentenceRecognition(SP, TP, tempDir);
			feature.POStagging(SL, tempDir+File.separator+SL+"-translation.txt", tempDir);
			 if (TL.equals("ENGLISH")){
				feature.POStagging(TL, TP, tempDir);
			 }else{
				 feature.POStagging(TL, tempDir+File.separator+TL+"-translation.txt", tempDir);
			 }
			 feature.NumberOfContentWords(SL,TL,tempDir);
			 feature.KeyWordviaTFIDF(tempDir);
			 NER ne=new NER();
			 if (TL.equals("ENGLISH")){
			 ne.NERbyString(tempDir+File.separator+SL+"-translation.txt",TP,tempDir);
			 }else{
			 ne.NERbyString(tempDir+File.separator+SL+"-translation.txt",tempDir+File.separator+TL+"-translation.txt",tempDir); 
			 }
			 a.DictSelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
			}else{
			if (api.equals("dfki")){
				dfki df=new dfki();
				df.sentence(SP);
				df.translation(SL,TL,SP,tempDir);
				if (TL.equals("ENGLISH")){
					a.Dictstemmer(SL,tempDir+File.separator+SL+"-translation.txt",WN,tempDir);
					a.Dictstemmer(TL,TP,WN,tempDir);
					a.Dictnewtext2vectors(SL, TL, tempDir);
					AddFeature feature=new AddFeature();
					feature.sentenceRecognition(SP, TP, tempDir);
					feature.POStagging(SL, tempDir+File.separator+SL+"-translation.txt", tempDir);
					feature.POStagging(TL, TP, tempDir);
					 feature.NumberOfContentWords(SL,TL,tempDir);
					 feature.KeyWordviaTFIDF(tempDir);
					 NER ne=new NER();
					 ne.NERbyString(tempDir+File.separator+SL+"-translation.txt",TP,tempDir);
					a.DictSelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
				}else{
					a.nonENtext2vectors(SL, TL, TP, tempDir);
    				a.nonENCOSINESimilarityWithoutDocPair(tempDir, SP, TP, result, threshold);
				}
			}
		}
		}  
		
	//	a.Dictstemmer("CHINESE","/home/fzsu/temp/en-zh/temp"+File.separator+"CHINESE"+"-translation.txt","/home/fzsu/WordNet-3.0","/home/fzsu/temp/en-zh/temp");
	//	a.Dictstemmer("ENGLISH","/home/fzsu/temp/en-zh/en.txt","/home/fzsu/WordNet-3.0","/home/fzsu/temp/en-zh/temp"); 
	//new NER().NERbyString("/home/fzsu/temp/en-zh/en.txt", "/home/fzsu/temp/en-zh/temp/CHINESE-translation.txt","/home/fzsu/temp/en-zh/temp");
		//	a.Dictnewtext2vectors("CHINESE", "ENGLISH", "/home/fzsu/temp/en-zh/temp");
	//	a.DictSelectedCOSINESimilarityWithoutDocPair("/home/fzsu/temp/en-zh/temp", "/home/fzsu/temp/en-zh/temp"+File.separator+"CHINESE-stem", "/home/fzsu/temp/en-zh/temp"+File.separator+"ENGLISH-stem", "/home/fzsu/temp/en-zh/temp/out.txt", 0);
		
	//	new dfki().sentence("/home/fzsu/MT/tt.txt");
	//	new dfki().translation("ENGLISH","LATVIAN","/home/fzsu/MT/tt.txt","/home/fzsu/MT/TEMP");
	//  a.Dictstemmer("GREEK","/home/fzsu/ComMetric/el-en-sample/temp"+File.separator+"GREEK"+"-translation.txt","/home/fzsu/WordNet-3.0","/home/fzsu/ComMetric/el-en-sample/temp");
	//	a.Dictstemmer("ENGLISH","/home/fzsu/ComMetric/el-en-sample/en.txt","/home/fzsu/WordNet-3.0","/home/fzsu/ComMetric/el-en-sample/temp");
	//	a.Dictnewtext2vectors("GREEK", "ENGLISH", "/home/fzsu/ComMetric/el-en-sample/temp");
	//	a.DictSelectedCOSINESimilarityWithoutDocPair("/home/fzsu/ComMetric/el-en-sample/temp", "/home/fzsu/ComMetric/el-en-sample/temp"+File.separator+"GREEK-stem", "/home/fzsu/ComMetric/el-en-sample/temp/"+File.separator+"ENGLISH-stem", "/home/fzsu/ComMetric/el-en-sample/temp/output.txt", 0);
	
	//	a.nonENtext2vectors("ENGLISH", "LATVIAN", "/home/fzsu/MT/dd.txt", "/home/fzsu/MT/translation");
	//	a.nonENCOSINESimilarityWithoutDocPair("/home/fzsu/MT/translation", "/home/fzsu/MT/tt.txt", "/home/fzsu/MT/dd.txt", "/home/fzsu/MT/translation/lv-en-output1.txt", 0);
		
		//	a.newtext2vectors("LATVIAN", "ENGLISH", "/home/fzsu/sample");
	//	a.SelectedCOSINESimilarityWithoutDocPair("/home/fzsu/sample", "/home/fzsu/sample"+File.separator+"LATVIAN"+"-stem", "/home/fzsu/sample"+File.separator+"ENGLISH"+"-stem", "/home/fzsu/sample/result.txt", 0.4);
	/*	if (args.length<6){
			System.out.println("Usage: java -jar ComMetric.jar -SL path2SourceLanguage -TL path2TargetLanguage -WN path2WordNet [-AF path2AlignmentFile] [-threshold value] -TP TargetPath");
			System.out.println("-SL path2SourceLanguage: the full path to the documents in Language 1 (source Language)");
			System.out.println("-TL path2SourceLanguage: the full path to the documents in Language 2 (target Language)");
			System.out.println("-WN path2WordNet: the full path to the WordNet installation directory");
			System.out.println("-AF path2AlignmentFile: optional, the full path to the alignment document that contains a list of compararable document pairs");
			System.out.println("-threshold value: optional, only output the document pairs with a comparability score >= threshold");
			System.out.println("TargetPath: specify a path to a target directory for storing results");
			System.out.println("Example (Linux): java -jar ComMetric.jar -SL /home/fzsu/Metric/sample/EN -TL /home/fzsu/Metric/sample/LV-translation -WN /usr/local/WordNet-3.0 -AF /home/fzsu/Metric/alignment.txt -threshold 0.4 -TP /home/fzsu/Metric");
			System.out.println("Example (Windows): java -jar ComMetric.jar -SL C:\\Metric\\sample\\EN -TL C:\\Metric\\sample\\LV-translation -WN C:\\WordNet\\2.1 -AF C:\\Metric\\alignment.txt -threshold 0.4 -TP C:\\Metric");
		}else{
			double threshold =0;
			String slpath="";
			String tlpath="";
			String targetpath="";
			String wnpath="";
			String alignmentpath="";
			for (int i=0;i<args.length;i++){
				if (args[i].toLowerCase().equals("-threshold") &&i<args.length-1){
					threshold=Double.parseDouble(args[i+1]);
				}
				if (args[i].toLowerCase().equals("-sl") &&i<args.length-1){
					slpath=args[i+1];
				}
				if (args[i].toLowerCase().equals("-tl") &&i<args.length-1){
					tlpath=args[i+1];
				}
				if (args[i].toLowerCase().equals("-wn") &&i<args.length-1){
					wnpath=args[i+1];
				}
				if (args[i].toLowerCase().equals("-af") &&i<args.length-1){
					alignmentpath=args[i+1];
				}
				if (args[i].toLowerCase().equals("-tp") &&i<args.length-1){
					targetpath=args[i+1];
				}
			}
			if (alignmentpath.equals("")){ //wighout alignment file
				a.stemmer(slpath,wnpath,targetpath);
				a.stemmer(tlpath,wnpath,targetpath);
				a.text2vectors(slpath,tlpath,targetpath);
				a.SelectedCOSINESimilarityWithoutDocPair(targetpath,slpath,tlpath,threshold);
			}else{  //with alignment file
				a.stemmer4alignmentdocs(slpath,wnpath,alignmentpath,targetpath);
				a.stemmer4alignmentdocs(tlpath,wnpath,alignmentpath,targetpath);
				a.text2vectors(slpath,tlpath,targetpath);
				a.SelectedCOSINESimilarityWithDocPair(alignmentpath,targetpath,threshold);
			}
		}    */
		//      a.stemmer("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/EN", "/usr/local/WordNet-3.0", "/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test");
		//      a.stemmer("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/LV-translation/tranalsation", "/usr/local/WordNet-3.0", "/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test");
		//      a.text2vectors("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/EN","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/LV-translation/tranalsation","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test");
		//a.SelectedCOSINESimilarityWithoutDocPair("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/EN","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/LV-translation/tranalsation",0);        
		
	//	a.stemmer4alignmentdocs("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/EN","/usr/local/WordNet-3.0","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/alignment.txt","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test");
	//	a.stemmer4alignmentdocs("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/LV-translation","/usr/local/WordNet-3.0","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/alignment.txt","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test");
	//	a.text2vectors("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/EN","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/LV-translation","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test");	
	//	a.SelectedCOSINESimilarityWithDocPair("/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/align.txt","/home/fzsu/Desktop/fzsu/ComMetric/ComMetric/sample/test",0);
	           
	} 
	
	
	/*
	 * use stanford tokenizer (in Stanford POS tagger API) for word tokenization, and MIT JWI (the MIT Java Wordnet Interface) API for 
	 * word-based stemming. 
	 */
	public void stemming(String path, String WNHome, String targetpath){
		try{
			System.out.println("start tokenization and lemmatization:");
		//	 String wnhome="/usr/local/WordNet-3.0";
			 String wnpath = WNHome + File.separator + "dict";
			 URL url = new URL("file", null, wnpath);
			  System.out.println(wnpath);
			 // construct the dictionary object and open it
			 IDictionary dict = new Dictionary(url);
			 dict.open();
			 WordnetStemmer stem=new WordnetStemmer(dict);
			 String savepath=targetpath+File.separator+"stem";
			 File dir=new File(savepath); 
			 dir.mkdirs();
			 File f=new File(path);
             File[] list=f.listFiles();
             for (int k=0;k<list.length;k++){
            	 System.out.println(k+":"+list[k].getName());
            	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[k].getAbsolutePath()), "UTF8"));
                 BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+list[k].getName()), "UTF8"));
            	 String s="";
                 while (true){
                	 s=br.readLine();
                	 if (s==null){
                		 break;
                	 }else{
                		 if (s.length()>0){
                			 String ss="";
                        	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
                     		while (tokenizer.hasNext()) {
                     		  CoreLabel token = tokenizer.next();
                     		  String word=token.word();
                     		 List alist=stem.findStems(word);
            				 if (alist.size()>0){
            				 if (alist.size()>1){
            					 BubbleSort(alist);
            				 }
            				 word=alist.get(0).toString();
            				 }
            				 ss=ss+word+" ";
                     		}
                     		if (ss.length()>0){
                     			bw.write(ss);
                     			bw.newLine();
                     		}
                		 }
                	 }
                 }
         		bw.flush();
         		bw.close();
         		br.close();
             }
             System.out.println("tokenization and lemamtaization are done!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/*
	 * requested by TILDE, use stanford tokenizer (in Stanford POS tagger API) for word tokenization, and MIT JWI (the MIT Java Wordnet Interface) API for 
	 * word-based stemming. 
	 * this is used in the priveous final version of comMetric
	 */
	public void stemmer(String language, String path, String WNHome, String targetpath){
		try{
			System.out.println("start tokenization and lemmatization:");
		//	 String wnhome="/usr/local/WordNet-3.0";
			 String wnpath = WNHome + File.separator + "dict";
			 URL url = new URL("file", null, wnpath);
			  System.out.println(wnpath);
			 // construct the dictionary object and open it
			 IDictionary dict = new Dictionary(url);
			 dict.open();
			 WordnetStemmer stem=new WordnetStemmer(dict);
			 String savepath=targetpath+File.separator+language+"-stem";
			 File dir=new File(savepath); 
			 dir.mkdirs();
			 BufferedReader br1=new BufferedReader(new FileReader(path));
			 String s="";
			 int count=0;
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
					count++;
					 System.out.println(count+":"+s);
					 File f5=new File(s);
					 String fullname=f5.getName();
					 if (!s.contains("###")){
						 fullname="";
						 String names[]=s.replaceAll("\\\\","/").split("/");
					/*	 for (int k=0;k<names.length;k++){
							 System.out.println(k+" "+names[k]);
						 } */
					//	 System.out.println(names[0].contains(":"));
						    if (names[0].contains(":")){
						    	names[0]=names[0].replace(":", "@@@");
						    }
					
							for (int k=0;k<names.length-1;k++){
								fullname=fullname+names[k]+"###";
							}
							fullname=fullname+names[names.length-1];
					 }
            	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(s), "UTF8"));
                 BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+fullname), "UTF8"));
                 while (true){
                	 s=br.readLine();
                	 if (s==null){
                		 break;
                	 }else{
                		 if (s.length()>0){
                			 String ss="";
                        	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
                     		while (tokenizer.hasNext()) {
                     		  CoreLabel token = tokenizer.next();
                     		  String word=token.word();
                     		 List alist=stem.findStems(word);
            				 if (alist.size()>0){
            				 if (alist.size()>1){
            					 BubbleSort(alist);
            				 }
            				 word=alist.get(0).toString();
            				 }
            				 ss=ss+word+" ";
                     		}
                     		if (ss.length()>0){
                     			bw.write(ss);
                     			bw.newLine();
                     		}
                		 }
                	 }
                 }
         		bw.flush();
         		bw.close();
         		br.close();
				 }	 
             }
		/*	 File f6=new File(savepath);
			 File flist[]=f6.listFiles();
			 BufferedWriter bw6=new BufferedWriter(new FileWriter(targetpath+File.separator+language+"-stem.txt"));
             for (int i=0;i<flist.length;i++){
            	 bw6.write(flist[i].getAbsolutePath());
            	 bw6.newLine();
             }
             bw6.flush();
             bw6.close(); */
			 System.out.println("tokenization and lemamtaization are done!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}	



	/* merge stemmer and stopword filtering in one step.
	 * requested by TILDE, use stanford tokenizer (in Stanford POS tagger API) for word tokenization, and MIT JWI (the MIT Java Wordnet Interface) API for 
	 * word-based stemming and stopword filtering. 
	 */
	public void newstemmer(String language, String path, String WNHome, String targetpath){
		try{
			System.out.println("start tokenization and lemmatization:");
		//	 String wnhome="/usr/local/WordNet-3.0";
			 String wnpath = WNHome + File.separator + "dict";
			 URL url = new URL("file", null, wnpath);
			  System.out.println(wnpath);
			 // construct the dictionary object and open it
			 IDictionary dict = new Dictionary(url);
			 dict.open();
			 WordnetStemmer stem=new WordnetStemmer(dict);
			 String savepath=targetpath+File.separator+language+"-stem";
			 File dir=new File(savepath); 
			 dir.mkdirs();
			 
			 File file = new File("."); 
 			 String CurrentPath = file.getCanonicalPath();
 			 String stoppath=CurrentPath+File.separator+"en-stopwords.txt"; 
 			 BufferedReader br5=new BufferedReader(new FileReader(stoppath));
 			 String s="";
 			 ArrayList stopword=new ArrayList();
 			 while (true){
 				 s=br5.readLine();
 				 if (s==null){
 					 break;
 				 }else{
 					 stopword.add(s);
 				 }
 			 }
			 
			 BufferedReader br1=new BufferedReader(new FileReader(path));
			 s="";
			 int count=0;
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
					count++;
					 System.out.println(count+":"+s);
					 File f5=new File(s);
					 String fullname=f5.getName();
					 if (!s.contains("###")){
						 fullname="";
						 String names[]=s.replaceAll("\\\\","/").split("/");
					/*	 for (int k=0;k<names.length;k++){
							 System.out.println(k+" "+names[k]);
						 } */
					//	 System.out.println(names[0].contains(":"));
						    if (names[0].contains(":")){
						    	names[0]=names[0].replace(":", "@@@");
						    }
					
							for (int k=0;k<names.length-1;k++){
								fullname=fullname+names[k]+"###";
							}
							fullname=fullname+names[names.length-1];
					 }
            	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(s), "UTF8"));
                 BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+fullname), "UTF8"));
                 while (true){
                	 s=br.readLine();
                	 if (s==null){
                		 break;
                	 }else{
                		 if (s.length()>0){
                			 String ss="";
                        	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
                     		while (tokenizer.hasNext()) {
                     		  CoreLabel token = tokenizer.next();
                     		  String word=token.word();
                     		 List alist=stem.findStems(word);
            				 if (alist.size()>0){
            				 if (alist.size()>1){
            					 BubbleSort(alist);
            				 }
            				 word=alist.get(0).toString();
            				 }
            				 if (word.length()>2){
            				 word=word.toLowerCase();
            				 if (word.charAt(0)>='a' &&word.charAt(word.length()-1)<='z'){ 
            				 if (!stopword.contains(word)){
            				 ss=ss+word+" ";
            				 }
            				 }
            				 }
                     		}
                     		if (ss.length()>0){
                     			bw.write(ss);
                     			bw.newLine();
                     		}
                		 }
                	 }
                 }
         		bw.flush();
         		bw.close();
         		br.close();
				 }	 
             }
		/*	 File f6=new File(savepath);
			 File flist[]=f6.listFiles();
			 BufferedWriter bw6=new BufferedWriter(new FileWriter(targetpath+File.separator+language+"-stem.txt"));
             for (int i=0;i<flist.length;i++){
            	 bw6.write(flist[i].getAbsolutePath());
            	 bw6.newLine();
             }
             bw6.flush();
             bw6.close(); */
			 System.out.println("tokenization and lemamtaization are done!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}	
	
	
	/*
	 *  
	 *  use stanford tokenizer (in Stanford POS tagger API) for word tokenization, and MIT JWI (the MIT Java Wordnet Interface) API for 
	 * word-based stemming. 
	 *  language: the original language to be stemmed.
	 *  path: the path to the directory which contains texts to be stemmed;
	 *  WNHome: the path to WordNet installation directory
	 *  targetpath: the path to the directory which stores stemmed texts. 
	 * 
	 */
	public void Dictstemmer(String language, String path, String WNHome, String targetpath){
		try{
			System.out.println("start tokenization and lemmatization:");
		//	 String wnhome="/usr/local/WordNet-3.0";
			 String wnpath = WNHome + File.separator + "dict";
			 URL url = new URL("file", null, wnpath);
			  System.out.println(wnpath);
			 // construct the dictionary object and open it
			 IDictionary dict = new Dictionary(url);
			 dict.open();
			 WordnetStemmer stem=new WordnetStemmer(dict);
			 String savepath=targetpath+File.separator+language+"-stem";
			 File dir=new File(savepath); 
			 dir.mkdirs();
			 int count=0;
			 String s="";
			/* BufferedReader br4=new BufferedReader(new FileReader(path));
			 while (true){
				 s=br4.readLine();
				 if (s==null){
					 break;
				 }else{
					 count++;
				 }
			 }
			 br4.close();*/
		//	 long startTime=System.currentTimeMillis();
			// CountDownLatch threadSignal = new CountDownLatch(count);
			// ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
			 BufferedReader br1=new BufferedReader(new FileReader(path));
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
			      //    textStemming ts=new textStemming(s, savepath, stem,threadSignal);
				   //    threadExecutor.submit(ts);
						count++;
						 System.out.println(count+":"+s);
						 File f5=new File(s);
						 String fullname=f5.getName();
						 if (!s.contains("###")){
							 fullname="";
							 String names[]=s.replaceAll("\\\\","/").split("/");
						/*	 for (int k=0;k<names.length;k++){
								 System.out.println(k+" "+names[k]);
							 } */
						//	 System.out.println(names[0].contains(":"));
							    if (names[0].contains(":")){
							    	names[0]=names[0].replace(":", "@@@");
							    }
						
								for (int k=0;k<names.length-1;k++){
									fullname=fullname+names[k]+"###";
								}
								fullname=fullname+names[names.length-1];
						 }
		        	  BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(s), "UTF8"));
		             BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+fullname), "UTF8"));
		             while (true){
		            	 s=br.readLine();
		            	 if (s==null){
		            		 break;
		            	 }else{
		            		 if (s.length()>0){
		            			 String ss="";
		                    	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
		                 		while (tokenizer.hasNext()) {
		                 		  CoreLabel token = tokenizer.next();
		                 		  String word=token.word();
		                 		 List alist=stem.findStems(word);
		        				 if (alist.size()>0){
		        				 if (alist.size()>1){
		        					 BubbleSort(alist);
		        				 }
		        				 word=alist.get(0).toString();
		        				 }
		        				 ss=ss+word+" ";
		                 		}
		                 		if (ss.length()>0){
		                 			bw.write(ss);
		                 			bw.newLine();
		                 		}
		            		 }
		            	 }
		             }
		     		bw.flush();
		     		bw.close();
		     		br.close();
				 }	 
	         }
	    //     threadSignal.await();
	    //     threadExecutor.shutdown();
	    //     long  endTime=System.currentTimeMillis();
			//  System.out.println("耗时:"+(endTime-startTime)+"毫秒");
	     //    System.out.println("processing time of text stemming: "+(endTime-startTime)+" milliseconds");
			 System.out.println("tokenization and lemamtaization are done!");
		}catch(Exception ex){
			ex.printStackTrace();
		}			
	}		
	
	public void stemmer4alignmentdocs(String path, String WNHome, String alignmentdocpath,String targetpath){
		try{
			System.out.println("start tokenization and lemmatization:");
		//	 String wnhome="/usr/local/WordNet-3.0";
			 String wnpath = WNHome + File.separator + "dict";
			 URL url = new URL("file", null, wnpath);
			  System.out.println(wnpath);
			 // construct the dictionary object and open it
			 IDictionary dict = new Dictionary(url);
			 dict.open();
			 WordnetStemmer stem=new WordnetStemmer(dict);
			 File f1=new File(path);
			 String savepath=targetpath+File.separator+f1.getName()+"-stem";
			 File dir=new File(savepath); 
			 dir.mkdirs();
			 ArrayList docName=new ArrayList();
			 BufferedReader br1=new BufferedReader(new FileReader(alignmentdocpath));
			 String s="";
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
					 String t[]=s.split("\\t");
				//	 System.out.println(t[0]);
				//	 System.out.println(t[1]);
					 f1=new File(t[0]);
					 File f2=f1.getParentFile();
					 String sname=f2.getAbsolutePath()+"-translation"+File.separator+f1.getName();
					 File f=new File(sname);
					 if (!f.exists()){
						 sname=t[0];
					 }
					 if (!docName.contains(sname)){
						 docName.add(sname);
					 }
					 f1=new File(t[1]);
					 f2=f1.getParentFile();
					 String tname=f2.getAbsolutePath()+"-translation"+File.separator+f1.getName();
					 f=new File(tname);
					 if (!f.exists()){
						 tname=t[1];
					 }
					 if (!docName.contains(tname)){
						 docName.add(tname);
					 }
				 }
			 }
		/*	 for (int i=0;i<docName.size();i++){
				 System.out.println(i+" :"+docName.get(i).toString());
			 } */
			 File f=new File(path);
             File[] list=f.listFiles();
             for (int k=0;k<list.length;k++){
            	 System.out.println(k+":"+list[k].getAbsolutePath());
            	 if (docName.contains(list[k].getAbsolutePath())){
          //  		 System.out.println("go");
            	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[k].getAbsolutePath()), "UTF8"));
                 BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+list[k].getName()), "UTF8"));
            	 s="";
                 while (true){
                	 s=br.readLine();
                	 if (s==null){
                		 break;
                	 }else{
                		 if (s.length()>0){
                			 String ss="";
                        	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
                     		while (tokenizer.hasNext()) {
                     		  CoreLabel token = tokenizer.next();
                     		  String word=token.word();
                     		 List alist=stem.findStems(word);
            				 if (alist.size()>0){
            				 if (alist.size()>1){
            					 BubbleSort(alist);
            				 }
            				 word=alist.get(0).toString();
            				 }
            				 ss=ss+word+" ";
                     		}
                     		if (ss.length()>0){
                     			bw.write(ss);
                     			bw.newLine();
                     		}
                		 }
                	 }
                 }
         		bw.flush();
         		bw.close();
         		br.close();
             }
             }
             System.out.println("tokenization and lemamtaization are done!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}		
	
	 public static void BubbleSort(List x) {   
		  for (int i = 0; i < x.size(); i++) {   
		   for (int j = i + 1; j < x.size(); j++) {   		 
		    if (x.get(i).toString().length()>x.get(j).toString().length()){
		    	String temp=x.get(i).toString();
		    	x.set(i, x.get(j).toString());
		    	x.set(j, temp);
		    }
		   }   
		  }   
		 } 
/*
 * stop word filtering, and convert the document texts into vectors.
 */
	 public void text2vector(String targetpath){
		 try{
			 System.out.println("start stopword filtering, and converting text into vector:");
			 File file = new File("."); 
			 String CurrentPath = file.getCanonicalPath();
			 String path=CurrentPath+File.separator+"en-stopwords.txt"; 
			 BufferedReader br1=new BufferedReader(new FileReader(path));
			 String s="";
			 ArrayList stopword=new ArrayList();
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
					 stopword.add(s);
				 }
			 }
			 
			 File f=new File(targetpath+File.separator+"stem");
             File[] list=f.listFiles();
             BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
             BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"word.index"), "UTF8"));
             BufferedWriter bw3=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"word.vectors"), "UTF8"));
             ArrayList wordlist=new ArrayList();
             for (int i=0;i<list.length;i++){
            	 System.out.println(i+": "+list[i].getName());
            	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[i].getAbsolutePath()), "UTF8"));
                 ArrayList word=new ArrayList();
                 ArrayList count=new ArrayList();
            	 while (true){
                	 s=br.readLine();
                	 if (s==null){
                		 break;
                	 }else{
                		 String t[]=s.split("\\ ");
                		 for (int j=0;j<t.length;j++){
                			 if (t[j].length()>2){
                			//	 System.out.println(t[j]);
                				 t[j]=t[j].toLowerCase();
                				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
                					 if (!stopword.contains(t[j])){
                						   if (!word.contains(t[j])){
                							   word.add(t[j]);
                							   count.add(1);
                						 }else{
                							 int p=word.indexOf(t[j]);
                							 int num=(Integer)count.get(p)+1;
                							 count.set(p, num);
                						 }
                					 }
                				 }
                			 }
                		 }
                	 }
                 }
            	 String ss=list[i].getName();
            	 String wordvector=list[i].getName();
            	 for (int k=0;k<word.size();k++){
            		 int index=0;
            		 if (!wordlist.contains(word.get(k).toString())){
            			 wordlist.add(word.get(k).toString());
            			 index=wordlist.size()-1;
            		 }else{
            			 index=wordlist.indexOf(word.get(k).toString());
            		 }
            		 ss=ss+"	"+index+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight
            		 wordvector=wordvector+"	"+word.get(k).toString()+" "+count.get(k).toString();
            	 }
            	 bw1.write(ss);
            	 bw1.newLine();
            	 br.close();
            	bw3.write(wordvector);
            	bw3.newLine();
             }
             for (int i=0;i<wordlist.size();i++){
            	 bw2.write(i+" "+wordlist.get(i).toString());
            	 bw2.newLine();
             }
             bw1.flush();
             bw1.close();
             bw2.flush();
             bw2.close();
             bw3.flush();
             bw3.close();
             System.out.println("The conversion of the document text into feature vectors is done!");
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }
	 

	 /*
	  *requested by TILDE,  stop word filtering, and convert the document texts into vectors.
	  */
	 	 public void text2vectors(String source, String target, String targetpath){
	 		 try{
	 			 System.out.println("start stopword filtering, and converting text into vector:");
	 			 File file = new File("."); 
	 			 String CurrentPath = file.getCanonicalPath();
	 			 String path=CurrentPath+File.separator+"en-stopwords.txt"; 
	 			 BufferedReader br1=new BufferedReader(new FileReader(path));
	 			 String s="";
	 			 ArrayList stopword=new ArrayList();
	 			 while (true){
	 				 s=br1.readLine();
	 				 if (s==null){
	 					 break;
	 				 }else{
	 					 stopword.add(s);
	 				 }
	 			 }
	              BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
	              BufferedWriter bw2=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"word.index"), "UTF8"));
	              BufferedWriter bw3=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"word.vectors"), "UTF8"));
	              ArrayList wordlist=new ArrayList();
	              File f=new File(targetpath+File.separator+source+"-stem");
	              File[] list=f.listFiles();
	              for (int i=0;i<list.length;i++){
	             	 System.out.println(i+": "+list[i].getName());
	             	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[i].getAbsolutePath()), "UTF8"));
	                  ArrayList word=new ArrayList();
	                  ArrayList count=new ArrayList();
	             	 while (true){
	                 	 s=br.readLine();
	                 	 if (s==null){
	                 		 break;
	                 	 }else{
	                 		 String t[]=s.split("\\ ");
	                 		 for (int j=0;j<t.length;j++){
	                 			 if (t[j].length()>2){
	                 			//	 System.out.println(t[j]);
	                 				 t[j]=t[j].toLowerCase();
	                 				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
	                 					 if (!stopword.contains(t[j])){
	                 						   if (!word.contains(t[j])){
	                 							   word.add(t[j]);
	                 							   count.add(1);
	                 						 }else{
	                 							 int p=word.indexOf(t[j]);
	                 							 int num=(Integer)count.get(p)+1;
	                 							 count.set(p, num);
	                 						 }
	                 					 }
	                 				 }
	                 			 }
	                 		 }
	                 	 }
	                  }
	             	 String ss=list[i].getName();
	             	 String wordvector=list[i].getName();
	             	 for (int k=0;k<word.size();k++){
	             		 int index=0;
	             		 if (!wordlist.contains(word.get(k).toString())){
	             			 wordlist.add(word.get(k).toString());
	             			 index=wordlist.size()-1;
	             		 }else{
	             			 index=wordlist.indexOf(word.get(k).toString());
	             		 }
	             		 ss=ss+"	"+index+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight
	             		 wordvector=wordvector+"	"+word.get(k).toString()+" "+count.get(k).toString();
	             	 }
	             	 bw1.write(ss);
	             	 bw1.newLine();
	             	 br.close();
	             	bw3.write(wordvector);
	             	bw3.newLine();
	              }
	              
	              
	              f=new File(targetpath+File.separator+target+"-stem");
	              list=f.listFiles();
	              for (int i=0;i<list.length;i++){
	             	 System.out.println(i+": "+list[i].getName());
	             	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[i].getAbsolutePath()), "UTF8"));
	                  ArrayList word=new ArrayList();
	                  ArrayList count=new ArrayList();
	             	 while (true){
	                 	 s=br.readLine();
	                 	 if (s==null){
	                 		 break;
	                 	 }else{
	                 		 String t[]=s.split("\\ ");
	                 		 for (int j=0;j<t.length;j++){
	                 			 if (t[j].length()>2){
	                 			//	 System.out.println(t[j]);
	                 				 t[j]=t[j].toLowerCase();
	                 				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
	                 					 if (!stopword.contains(t[j])){
	                 						   if (!word.contains(t[j])){
	                 							   word.add(t[j]);
	                 							   count.add(1);
	                 						 }else{
	                 							 int p=word.indexOf(t[j]);
	                 							 int num=(Integer)count.get(p)+1;
	                 							 count.set(p, num);
	                 						 }
	                 					 }
	                 				 }
	                 			 }
	                 		 }
	                 	 }
	                  }
	             	String ss=list[i].getName();
	             	 String wordvector=list[i].getName();
	             	 for (int k=0;k<word.size();k++){
	             		 int index=0;
	             		 if (!wordlist.contains(word.get(k).toString())){
	             			 wordlist.add(word.get(k).toString());
	             			 index=wordlist.size()-1;
	             		 }else{
	             			 index=wordlist.indexOf(word.get(k).toString());
	             		 }
	             		 ss=ss+"	"+index+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight
	             		 wordvector=wordvector+"	"+word.get(k).toString()+" "+count.get(k).toString();
	             	 }
	             	 bw1.write(ss);
	             	 bw1.newLine();
	             	 br.close();
	             	bw3.write(wordvector);
	             	bw3.newLine();
	              }
	              
	              
	              for (int i=0;i<wordlist.size();i++){
	             	 bw2.write(i+" "+wordlist.get(i).toString());
	             	 bw2.newLine();
	              }
	              bw1.flush();
	              bw1.close();
	              bw2.flush();
	              bw2.close();
	              bw3.flush();
	              bw3.close();
	              System.out.println("The conversion of the document text into feature vectors is done!");
	 		 }catch(Exception ex){
	 			 ex.printStackTrace();
	 		 }
	 	 }	 
	 

	 	 
	 	 /*
		  *requested by TILDE,  stop word filtering, and convert the document texts into vectors.
		  */
		 	 public void newtext2vectors(String source, String target, String targetpath){
		 		 try{
		 			 System.out.println("start stopword filtering, and converting text into vector:");
		 			 File file = new File("."); 
		 			 String CurrentPath = file.getCanonicalPath();
		 			 String path=CurrentPath+File.separator+"en-stopwords.txt"; 
		 			 BufferedReader br1=new BufferedReader(new FileReader(path));
		 			 String s="";
		 			 ArrayList stopword=new ArrayList();
		 			 while (true){
		 				 s=br1.readLine();
		 				 if (s==null){
		 					 break;
		 				 }else{
		 					 stopword.add(s);
		 				 }
		 			 }
		              BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
		             
		              ArrayList wordlist=new ArrayList();
		              File f=new File(targetpath+File.separator+source+"-stem");
		              File[] list=f.listFiles();
		              for (int i=0;i<list.length;i++){
		             	 System.out.println(i+": "+list[i].getName());
		             	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[i].getAbsolutePath()), "UTF8"));
		                  ArrayList word=new ArrayList();
		                  ArrayList count=new ArrayList();
		             	 while (true){
		                 	 s=br.readLine();
		                 	 if (s==null){
		                 		 break;
		                 	 }else{
		                 		 String t[]=s.split("\\ ");
		                 		 for (int j=0;j<t.length;j++){
		                 			 if (t[j].length()>2){
		                 			//	 System.out.println(t[j]);
		                 				 t[j]=t[j].toLowerCase();
		                 				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
		                 					 if (!stopword.contains(t[j])){
		                 						   if (!word.contains(t[j])){
		                 							   word.add(t[j]);
		                 							   count.add(1);
		                 						 }else{
		                 							 int p=word.indexOf(t[j]);
		                 							 int num=(Integer)count.get(p)+1;
		                 							 count.set(p, num);
		                 						 }
		                 					 }
		                 				 }
		                 			 }
		                 		 }
		                 	 }
		                  }
		             	 String ss=list[i].getName();    	
		             	 for (int k=0;k<word.size();k++){        		
		             		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight             		
		             	 }
		             	 bw1.write(ss);
		             	 bw1.newLine();
		             	 br.close();
		              }        
		              f=new File(targetpath+File.separator+target+"-stem");
		              list=f.listFiles();
		              for (int i=0;i<list.length;i++){
		             	 System.out.println(i+": "+list[i].getName());
		             	 BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(list[i].getAbsolutePath()), "UTF8"));
		                  ArrayList word=new ArrayList();
		                  ArrayList count=new ArrayList();
		             	 while (true){
		                 	 s=br.readLine();
		                 	 if (s==null){
		                 		 break;
		                 	 }else{
		                 		 String t[]=s.split("\\ ");
		                 		 for (int j=0;j<t.length;j++){
		                 			 if (t[j].length()>2){
		                 			//	 System.out.println(t[j]);
		                 				 t[j]=t[j].toLowerCase();
		                 				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
		                 					 if (!stopword.contains(t[j])){
		                 						   if (!word.contains(t[j])){
		                 							   word.add(t[j]);
		                 							   count.add(1);
		                 						 }else{
		                 							 int p=word.indexOf(t[j]);
		                 							 int num=(Integer)count.get(p)+1;
		                 							 count.set(p, num);
		                 						 }
		                 					 }
		                 				 }
		                 			 }
		                 		 }
		                 	 }
		                  }
		             	String ss=list[i].getName();
		             	 for (int k=0;k<word.size();k++){
		             		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight
		             	 }
		             	 bw1.write(ss);
		             	 bw1.newLine();
		             	 br.close();
		              }
		              bw1.flush();
		              bw1.close();
		              System.out.println("The conversion of the document text into feature vectors is done!");
		 		 }catch(Exception ex){
		 			 ex.printStackTrace();
		 		 }
		 	 }	 

		 	 
		 	/*
		 	 * stop word filtering, and convert the document texts into vectors.
		 	 *  source: source language
		 	 *  target: target language
		 	 *  targetpath: path to the directory that store the index vectors.
		 	 */
		 		 public void Dictnewtext2vectors(String source, String target, String targetpath){
		 			 
		 			 try{
		 				 System.out.println("start stopword filtering, and converting text into vector:");
		 				 File file = new File("."); 
		 				 String CurrentPath = file.getCanonicalPath();
		 				 String path=CurrentPath+File.separator+"stopwords"+File.separator+"stopwords_en.txt"; 
		 				 BufferedReader br1=new BufferedReader(new FileReader(path));
		 				 String s="";
		 				 ArrayList stopword=new ArrayList();
		 				 while (true){
		 					 s=br1.readLine();
		 					 if (s==null){
		 						 break;
		 					 }else{
		 						 stopword.add(s);
		 					 }
		 				 }
		 			//	 long startTime=System.currentTimeMillis();
		 			//	 ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
		 				 ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
		 			//	 bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"abc.vectors"), "UTF8"));
		 	             BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
		 	             File f=new File(targetpath+File.separator+source+"-stem");
		 	             File[] list=f.listFiles();
		 	             CountDownLatch threadSignal = new CountDownLatch(list.length);//初始化countDown
		 	             for (int i=0;i<list.length;i++){
		 	            	 System.out.println(i+": "+list[i].getName());
		 	            	 String input=list[i].getAbsolutePath();
		 	            	 String docname=list[i].getName();
		 	            	 textvectorization tv=new textvectorization(stopword,bw1,input,docname,threadSignal);
		 	           // 	 textvectorization tv=new textvectorization(stopword,queue,input,docname,threadSignal);
		 	           	 threadExecutor.submit(tv);
		 	            	
		 	           // 	 Thread t=new Thread(tv);
		 	           //      t.start();
		 	            	 
		 	             }   
		 	             threadSignal.await();//等待所有子线程执行完 
		 	          /*     while (!queue.isEmpty()){
		 	            	   bw1.write(queue.poll().toString());
		 	            	   bw1.newLine();
		 	               }  */
		 	             f=new File(targetpath+File.separator+target+"-stem");
		 	             list=f.listFiles();
		 	             threadSignal = new CountDownLatch(list.length);//初始化countDown
		 	             for (int i=0;i<list.length;i++){
		 	            	 System.out.println(i+": "+list[i].getName());
		 	            	String input=list[i].getAbsolutePath();
		 	            	String docname=list[i].getName();
		 	            	textvectorization tv=new textvectorization(stopword,bw1,input,docname,threadSignal);
		 	           // 	textvectorization tv=new textvectorization(stopword,queue,input,docname,threadSignal);
		 	            //	   Thread t=new Thread(tv);
		 	           //    t.start();
		 	            		 threadExecutor.submit(tv);
		 	            	

		 	             }  
		 	             
		 	             threadSignal.await();//等待所有子线程执行完 
		 	            /* while (!queue.isEmpty()){
		 	          	   bw1.write(queue.poll().toString());
		 	          	   bw1.newLine();
		 	             }  */
		 	              bw1.flush();
		 	              bw1.close();
		 				   threadExecutor.shutdown();
		 			//	   System.out.println(threadExecutor.isTerminated());
		 				//   if (threadExecutor.isTerminated()){
		 				 //  while(!threadExecutor.isTerminated());  
		 			//	   long  endTime=System.currentTimeMillis();
		 				//   System.out.println("耗时:"+(endTime-startTime)+"毫秒");
		 			//	   System.out.println("processing time of text vectorization: "+(endTime-startTime)+" milliseconds");
		 				   //  }
		 	             System.out.println("The conversion of the document text into feature vectors is done!");
		 	             
		 			 }catch(Exception ex){
		 				 ex.printStackTrace();
		 			 }
		 		 }	 	 
		 	 
		 	 
		 	/*
		 	 * This is for non-english language pair, and the translation is also not based on English
		 	 * so word stemming will be skipped, and stopword filtering and text to vector conversion will be executed right after translation.	 
		 	 * source: source language
		 	 * target: target language
		 	 * path: path to the file which lists the full path to the documents in target language
		 	 * targetpath: path to the directory to store the index vectors.
		 	 */

		 		 public void nonENtext2vectors(String source, String target, String path, String targetpath){
		 			 try{
		 				 
		 				 System.out.println("start stopword filtering, and converting text into vector:");
		 				 Map<String, String> language = new HashMap<String, String>();
		 					language.put("english", "en");
		 					language.put("german", "de");
		 					language.put("croatian", "hr");
		 					language.put("greek", "el");
		 					language.put("estonian", "et");
		 					language.put("lithuanian", "lt");
		 					language.put("latvian", "lv");
		 					language.put("romanian", "ro");
		 					language.put("slovenian", "sl");
		 					String e=language.get(target.toLowerCase());
		 				 File file = new File("."); 
		 				 String CurrentPath = file.getCanonicalPath();
		 				 String stoppath=CurrentPath+File.separator+"stopwords"+File.separator+"stopwords_"+e+".txt"; 
		 				 File stopFile=new File(stoppath);
		 				 ArrayList stopword=new ArrayList();
		 				 String s="";
		 				 if (stopFile.exists()){
		 				 BufferedReader br1=new BufferedReader(new FileReader(stoppath));
		 				 while (true){
		 					 s=br1.readLine();
		 					 if (s==null){
		 						 break;
		 					 }else{
		 						 stopword.add(s);
		 					 }
		 				 }
		 				 br1.close();
		 			 }
		 			//	 System.out.println("stopword= "+stopword.size()+stopword.get(stopword.size()-1).toString());
		 			//	 long startTime=System.currentTimeMillis();
		 				 BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
		 	             File f=new File(targetpath+File.separator+source+"-translation");
		 	             File[] list=f.listFiles();
		 	             ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
		 	             CountDownLatch threadSignal = new CountDownLatch(list.length);//初始化countDown
		 	             for (int i=0;i<list.length;i++){
		 	            	 System.out.println(i+": "+list[i].getName());
		 	            	 String input=list[i].getAbsolutePath();
		 	            	 String docname=list[i].getName();
		 	            	 nonENSourceVector tv=new nonENSourceVector(stopword,bw1,input,docname,threadSignal);
		 	           	 threadExecutor.submit(tv);           	 
		 	             }   
		 	             threadSignal.await();//等待所有子线程执行完  
		 	             
		 	             BufferedReader br5=new BufferedReader(new FileReader(path)); //count the number of documents so that count down for thread numbers can be fixed.
		 	             int num=0;
		 	             while (true){
		 	            	 s=br5.readLine();
		 	            	 if (s==null){
		 	            		 break;
		 	            	 }else{
		 	            		 num++;
		 	            	 }
		 	             }
		 	           BufferedReader br1=new BufferedReader(new FileReader(path)); //path to the input file which lists the full path to the documents to be translated
		 	           threadSignal = new CountDownLatch(num);//初始化countDown
		 	     		while (true){
		 	     			s=br1.readLine();
		 	     			if (s==null){
		 	     				break;
		 	     			}else{	
		 	     				nonENTargetVector tv=new nonENTargetVector(stopword,bw1,s,threadSignal);
		 	     				threadExecutor.submit(tv);
		 	     			}
		 	     		}
		 	     		threadSignal.await(); 
		 	     		threadExecutor.shutdown();
		 	             bw1.flush();
		 	             bw1.close();
		 	       //      long  endTime=System.currentTimeMillis();
		 				//   System.out.println("耗时:"+(endTime-startTime)+"毫秒");
		 	       //      System.out.println("processing time of text vectorization: "+(endTime-startTime)+" milliseconds");
		 	             System.out.println("The conversion of the document text into feature vectors is done!");
		 			 }catch(Exception ex){
		 				 ex.printStackTrace();
		 			 }
		 		 }	 
		 	 
		 	 
		 	 
	/*
	 * given a list of comparable document pairs, compute their comparability score by cosine measure 
	 */
	 
	 public void SelectedCOSINESimilarity(String path, String targetpath){
			try{
				System.out.println("start computing comparability score via cosine measure:");
				 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
			     BufferedWriter bw=new BufferedWriter(new FileWriter(targetpath+File.separator+"result.txt"));             
				String s="";
			    ArrayList alist=new ArrayList();
			    ArrayList DocName=new ArrayList();
			    while (true){
				s=br.readLine();
				if (s==null){
				    break;
				}else{
				    alist.add(s);
				    String t[]=s.split("\\	"); //use tab   
				    DocName.add(t[0]);
				 }
			    }
			    BufferedReader br1=new BufferedReader(new FileReader(path));  
			    int count=0;
			    while (true){
				s=br1.readLine();
				if (s==null){
				    break;
				}else{
				    String t[]=s.split("\\	"); // tab seperator 
				//    System.out.println(t[0]+" "+t[1]);
				//    t[0]=t[0]+".txt";
				//    t[1]=t[1]+".txt";
				    if (DocName.contains(t[0])&&DocName.contains(t[1])){ 
				    	System.out.println(t[0]+" "+t[1]);
				    int p=DocName.indexOf(t[0]);  
					int q=DocName.indexOf(t[1]); 
		            String s1=alist.get(p).toString();
					String t1[]=s1.split("\\	");
					ArrayList value1=new ArrayList();
					ArrayList id1=new ArrayList();
					for (int k=1;k<t1.length;k++){
					    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
					    id1.add(w[0]);
					    value1.add(w[1]);
					}
					String s2=alist.get(q).toString();
					String t2[]=s2.split("\\	");
					ArrayList id2=new ArrayList();
					ArrayList value2=new ArrayList();
					for (int k=1;k<t2.length;k++){
					    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
					    id2.add(w[0]);
					    value2.add(w[1]);
					}
					double sim=GETcosine(id1,value1,id2,value2);
					s=s+"	"+sim; 
					bw.write(s);
					bw.newLine();
				    }else{
				    	System.out.println(t[0]+" "+t[1]+ " do not both occur in the corpus");
				    }
				}
			    }
			    bw.flush();
			    bw.close();  
			    System.out.println("The computation of comparability metrics is all done!");
			}catch(Exception ex){
			    ex.printStackTrace();
			}
		    }
	 
	 /*
		 * requested by TILDE, given a list of comparable document pairs, compute their comparability score by cosine measure 
		 */
		 
		 public void SelectedCOSINESimilarityWithDocPair(String alignmentpath, String targetpath, double threshold){
				try{
					System.out.println("start computing comparability score via cosine measure:");
					 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
				     BufferedWriter bw=new BufferedWriter(new FileWriter(targetpath+File.separator+"result.txt"));             
					String s="";
				    ArrayList alist=new ArrayList();
				    ArrayList DocName=new ArrayList();
				    while (true){
					s=br.readLine();
					if (s==null){
					    break;
					}else{
					    alist.add(s);
					    String t[]=s.split("\\	"); //use tab   
					    DocName.add(t[0]);
					 }
				    }
				    BufferedReader br1=new BufferedReader(new FileReader(alignmentpath));  
				    int count=0;
				    while (true){
					s=br1.readLine();
					if (s==null){
					    break;
					}else{
					    String t[]=s.split("\\	"); // tab seperator 
					//    System.out.println(t[0]+" "+t[1]);
					//    t[0]=t[0]+".txt";
					//    t[1]=t[1]+".txt";
					    File f1=new File(t[0]);
						 File f2=f1.getParentFile();
						 String sname=f2.getAbsolutePath()+"-translation"+File.separator+f1.getName();
						 File f=new File(sname);
						 if (f.exists()){
							 t[0]=sname;
						 }
						 f1=new File(t[1]);
						 f2=f1.getParentFile();
						 String tname=f2.getAbsolutePath()+"-translation"+File.separator+f1.getName();
						 f=new File(tname);
						 if (f.exists()){
							 t[1]=tname;
						 }
					//	 System.out.println(t[0]);
					//	 System.out.println(t[1]);
					    if (DocName.contains(t[0])&&DocName.contains(t[1])){ 
					    	System.out.println(t[0]+" "+t[1]);
					    int p=DocName.indexOf(t[0]);  
						int q=DocName.indexOf(t[1]); 
			            String s1=alist.get(p).toString();
						String t1[]=s1.split("\\	");
						ArrayList value1=new ArrayList();
						ArrayList id1=new ArrayList();
						for (int k=1;k<t1.length;k++){
						    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
						    id1.add(w[0]);
						    value1.add(w[1]);
						}
						String s2=alist.get(q).toString();
						String t2[]=s2.split("\\	");
						ArrayList id2=new ArrayList();
						ArrayList value2=new ArrayList();
						for (int k=1;k<t2.length;k++){
						    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
						    id2.add(w[0]);
						    value2.add(w[1]);
						}
						double sim=GETcosine(id1,value1,id2,value2);
						s=s+"	"+sim;
						if (sim>=threshold){
						bw.write(s);
						bw.newLine();
						}
					    }else{
					    	System.out.println(t[0]+" "+t[1]+ " do not both occur in the corpus");
					    }
					}
				    }
				    bw.flush();
				    bw.close();  
				    System.out.println("The computation of comparability metrics is all done!");
				}catch(Exception ex){
				    ex.printStackTrace();
				}
			    }
	
		 /*
			 * requested by TILDE, given a list of comparable document pairs, compute their comparability score by cosine measure 
			 */
			 
			 public void SelectedCOSINESimilarityWithoutDocPair(String targetpath, String spath, String tpath, String outputpath, double threshold){
					try{
						System.out.println("start computing comparability score via cosine measure:");
						 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
					     BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
						String s="";
					    ArrayList alist=new ArrayList();
					    ArrayList DocName=new ArrayList();
					    while (true){
						s=br.readLine();
						if (s==null){
						    break;
						}else{
						    alist.add(s);
						    String t[]=s.split("\\	"); //use tab   
						    DocName.add(t[0]);
						 }
					    }
					    File file=new File(spath);
					    File sdoc[]=file.listFiles();
					    file=new File(tpath);
					    File tdoc[]=file.listFiles();
					    for (int i=0;i<sdoc.length;i++){
					    	for (int j=0;j<tdoc.length;j++){
						    if (DocName.contains(sdoc[i].getName())&&DocName.contains(tdoc[j].getName())){ 
						//    System.out.println(sdoc[i].getName()+" "+tdoc[j].getName());
						    int p=DocName.indexOf(sdoc[i].getName());  
							int q=DocName.indexOf(tdoc[j].getName()); 
				            String s1=alist.get(p).toString();
							String t1[]=s1.split("\\	");
							ArrayList value1=new ArrayList();
							ArrayList id1=new ArrayList();
							for (int k=1;k<t1.length;k++){
							    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
							    id1.add(w[0]);
							    value1.add(w[1]);
							}
							String s2=alist.get(q).toString();
							String t2[]=s2.split("\\	");
							ArrayList id2=new ArrayList();
							ArrayList value2=new ArrayList();
							for (int k=1;k<t2.length;k++){
							    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
							    id2.add(w[0]);
							    value2.add(w[1]);
							}
							double sim=GETcosine(id1,value1,id2,value2);
							String snames[]=sdoc[i].getName().split("###");
							if (snames[0].contains("@@@")){
								snames[0]=snames[0].replace("@@@", ":");
							}
							String sname=snames[0];
							for (int k=1;k<snames.length;k++){
								sname=sname+File.separator+snames[k];
							}
							String tnames[]=tdoc[j].getName().split("###");
							if (tnames[0].contains("@@@")){
								tnames[0]=tnames[0].replace("@@@", ":");
							}
							String tname=tnames[0];
							for (int k=1;k<tnames.length;k++){
								tname=tname+File.separator+tnames[k];
							}
							s=sname+"	"+tname+"	"+sim; 
							if (sim>=threshold){
							bw.write(s);
							bw.newLine();
							}
						    }//else{
						    //	System.out.println(sdoc[i].getName()+" "+tdoc[j].getName()+ " do not both occur in the corpus");
						    //}
					    	}
					    	System.out.println((i+1)*tdoc.length +" document pairs is done!");
					    }
					    bw.flush();
					    bw.close();  
					    System.out.println("The computation of comparability metrics is all done!");
					}catch(Exception ex){
					    ex.printStackTrace();
					}
				    }		 
		 


			 /*
				 * requested by TILDE, given a list of comparable document pairs, compute their comparability score by cosine measure 
				 */
				 
				 public void newSelectedCOSINESimilarityWithoutDocPair(String targetpath, String spath, String tpath, String outputpath, double threshold){
						try{
							System.out.println("start computing comparability score via cosine measure:");
						/*	 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
						     BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
							String s="";
						    ArrayList alist=new ArrayList();
						    ArrayList DocName=new ArrayList();
						    while (true){
							s=br.readLine();
							if (s==null){
							    break;
							}else{
							    alist.add(s);
							    String t[]=s.split("\\	"); //use tab   
							    DocName.add(t[0]);
							 }
						    }  */
							BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
							String s="";
						    File file=new File(spath);
						    File sdoc[]=file.listFiles();
						    file=new File(tpath);
						    File tdoc[]=file.listFiles();
						    for (int i=0;i<sdoc.length;i++){
						    	for (int j=0;j<tdoc.length;j++){
							/*    if (DocName.contains(sdoc[i].getName())&&DocName.contains(tdoc[j].getName())){ 
							    System.out.println(sdoc[i].getName()+" "+tdoc[j].getName());
							    int p=DocName.indexOf(sdoc[i].getName());  
								int q=DocName.indexOf(tdoc[j].getName()); 
					            String s1=alist.get(p).toString();
								String t1[]=s1.split("\\	");
								ArrayList value1=new ArrayList();
								ArrayList id1=new ArrayList();
								for (int k=1;k<t1.length;k++){
								    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
								    id1.add(w[0]);
								    value1.add(w[1]);
								}
								String s2=alist.get(q).toString();
								String t2[]=s2.split("\\	");
								ArrayList id2=new ArrayList();
								ArrayList value2=new ArrayList();
								for (int k=1;k<t2.length;k++){
								    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
								    id2.add(w[0]);
								    value2.add(w[1]);
								}  */
						    	BufferedReader br1=new BufferedReader(new InputStreamReader(new FileInputStream(sdoc[i].getAbsolutePath()), "UTF8"));
						    	ArrayList id1=new ArrayList();
				                ArrayList value1=new ArrayList();
				             	 while (true){
				                 	 s=br1.readLine();
				                 	 if (s==null){
				                 		 break;
				                 	 }else{
				                 		 String t[]=s.split("\\ ");
				                 		 for (int k=0;k<t.length;k++){
				                 			if (!id1.contains(t[k])){
				                 							   id1.add(t[k]);
				                 							   value1.add(1);
				                 						 }else{
				                 							 int p=id1.indexOf(t[k]);
				                 							 int num=(Integer)value1.get(p)+1;
				                 							 value1.set(p, num);
				                 						 }	
				                 		 }
				                 	 }
				                  }
				             	 
						    	BufferedReader br2=new BufferedReader(new InputStreamReader(new FileInputStream(tdoc[j].getAbsolutePath()), "UTF8"));
						    	ArrayList id2=new ArrayList();
				                ArrayList value2=new ArrayList();
				             	 while (true){
				                 	 s=br2.readLine();
				                 	 if (s==null){
				                 		 break;
				                 	 }else{
				                 		 String t[]=s.split("\\ ");
				                 		 for (int k=0;k<t.length;k++){
				                 			if (!id2.contains(t[k])){
				                 							   id2.add(t[k]);
				                 							   value2.add(1);
				                 						 }else{
				                 							 int p=id2.indexOf(t[k]);
				                 							 int num=(Integer)value2.get(p)+1;
				                 							 value2.set(p, num);
				                 						 }	
				                 		 }
				                 	 }
				                  }
						    	
						    	
								double sim=GETcosine(id1,value1,id2,value2);
								String snames[]=sdoc[i].getName().split("###");
								if (snames[0].contains("@@@")){
									snames[0]=snames[0].replace("@@@", ":");
								}
								String sname=snames[0];
								for (int k=1;k<snames.length;k++){
									sname=sname+File.separator+snames[k];
								}
								String tnames[]=tdoc[j].getName().split("###");
								if (tnames[0].contains("@@@")){
									tnames[0]=tnames[0].replace("@@@", ":");
								}
								String tname=tnames[0];
								for (int k=1;k<tnames.length;k++){
									tname=tname+File.separator+tnames[k];
								}
								s=sname+"	"+tname+"	"+sim; 
								if (sim>=threshold){
								bw.write(s);
								bw.newLine();
								}
							   // }else{
							    //	System.out.println(sdoc[i].getName()+" "+tdoc[j].getName()+ " do not both occur in the corpus");
							    //}
						    	}
						    }
						    bw.flush();
						    bw.close();  
						    System.out.println("The computation of comparability metrics is all done!");
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
	
	 /*
		 *  given a list of comparable document pairs, compute their comparability score by cosine measure 
		 *  targetpath: path to the directory that contains index vectors.
		 *  spath: path to the directory that contains the stemmed texts in source languages
		 *  tpath: path to the directory that contains the stemmed texts in target languages
		 *  outputpath: path to the file that store the comparability results
		 *  threshold: only output document pairs with comparability score>threshold.  
		 */
			 
			 public void DictSelectedCOSINESimilarityWithoutDocPair(String targetpath, String spath, String tpath, String outputpath, double threshold){
					try{
						System.out.println("start computing comparability score via cosine measure:");
						 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
					     BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
						String s="";
					//    ArrayList alist=new ArrayList();
					//    ArrayList DocName=new ArrayList();
						Hashtable ht=new Hashtable();
					    while (true){
						s=br.readLine();
						if (s==null){
						    break;
						}else{
					//	    alist.add(s);
						    String t[]=s.split("\\	"); //use tab   
					//	    DocName.add(t[0]);
						    ht.put(t[0], s);
						 }
					    }
					    br.close();
					    
					    
					    br=new BufferedReader(new FileReader(targetpath+File.separator+"SENT.info")); 
					    Hashtable sent=new Hashtable();
					    while (true){
					    	s=br.readLine();
					    	if (s==null){
					    		break;
					    	}else{
					    		 String t[]=s.split("\t"); //use tab   
								 sent.put(t[0], s);
					    	}
					    }
                         br.close();
					    br=new BufferedReader(new FileReader(targetpath+File.separator+"ContentWord.info")); 
					    Hashtable content=new Hashtable();
					    while (true){
					    	s=br.readLine();
					    	if (s==null){
					    		break;
					    	}else{
					    		 String t[]=s.split("\t"); //use tab
					    	//	 System.out.println("t[0]::"+t[0]);
								 content.put(t[0], s);
					    	}
					    }
					    br.close();
					    br=new BufferedReader(new FileReader(targetpath+File.separator+"keyword.tfidf")); 
					    Hashtable keyword=new Hashtable();
					    while (true){
					    	s=br.readLine();
					    	if (s==null){
					    		break;
					    	}else{
					    		 String t[]=s.split("\t"); //use tab   
								 keyword.put(t[0], s);
					    	}
					    }
					    br.close();
					    br=new BufferedReader(new FileReader(targetpath+File.separator+"ner.info")); 
					    Hashtable ne=new Hashtable();
					    while (true){
					    	s=br.readLine();
					    	if (s==null){
					    		break;
					    	}else{
					    		 String t[]=s.split("\t"); //use tab   
								 ne.put(t[0], s);
					    	}
					    }
					    br.close();
					    
           		    File file=new File(spath);
					    File sdoc[]=file.listFiles();
					    file=new File(tpath);
					    File tdoc[]=file.listFiles();
				//	    long startTime=System.currentTimeMillis();
					    ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
					//    CountDownLatch threadSignal = new CountDownLatch(sdoc.length*tdoc.length);//初始化countDown
					   
					    for (int i=0;i<sdoc.length;i++){
					    	 CountDownLatch threadSignal = new CountDownLatch(tdoc.length);//初始化countDown
					    	for (int j=0;j<tdoc.length;j++){
					    	 String sn=sdoc[i].getName();
						    String tn=tdoc[j].getName();
						//    cosine cos=new cosine(bw, DocName, alist,sn,tn,threshold,threadSignal);
						    cosine cos=new cosine(bw, ht,sent,content,keyword,ne,sn,tn,threshold,threadSignal);
						//    Thread thread=new Thread(cos);
						    threadExecutor.submit(cos);
						//    cos=null;
					    	}
					    	threadSignal.await();
					    	bw.flush();
					    	System.out.println((i+1)*tdoc.length +" document pairs is done!");
					    }
					 //   threadSignal.await();
					//    bw.flush();
					    bw.close();
					    threadExecutor.shutdown();
			     //      long  endTime=System.currentTimeMillis();
					//    System.out.println("耗时:"+(endTime-startTime)+"毫秒");
			    //       System.out.println("processing time of computing comparability scores: "+(endTime-startTime)+"milliseconds");
			           System.out.println("The computation of comparability metrics is all done!");
					}catch(Exception ex){
					    ex.printStackTrace();
					}
				    }		 
	 
	 
	 
	 /*
	  * for non-english language pairs only
	  *  targetpath: path to the directory that contains index vectors.
	  *  spath: path to the file that lists full path to texts in source languages
	  *  tpath: path to the file that lists full path to texts in target languages
	  *  outputpath: path to the file that store the comparability results
	  *  threshold: only output document pairs with comparability score>threshold.  
	  */

	 		 public void nonENCOSINESimilarityWithoutDocPair(String targetpath, String spath, String tpath, String outputpath, double threshold){
	 				try{
	 					System.out.println("start computing comparability score via cosine measure:");
	 					 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
	 				     BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
	 					String s="";
	 					Hashtable ht=new Hashtable();
	 				    while (true){
	 					s=br.readLine();
	 					if (s==null){
	 					    break;
	 					}else{
	 					    String t[]=s.split("\\	"); //use tab   
	 					    ht.put(t[0], s);
	 					 }
	 				    }
	 				    ArrayList sdoc=new ArrayList();
	 				    BufferedReader br1=new BufferedReader(new FileReader(spath)); //path to the input file which lists the full path to the documents to be translated
	 		     		while (true){
	 		     			s=br1.readLine();
	 		     			if (s==null){
	 		     				break;
	 		     			}else{		
	 		     				String names[]=s.replaceAll("\\\\","/").split("/");	
	 		     				String fullname="";
	 		     				if (names[0].contains(":")){
	 		     					names[0]=names[0].replace(":", "@@@");
	 		     				}
	 		     				for (int k=0;k<names.length-1;k++){
	 		     					fullname=fullname+names[k]+"###";
	 		     				} 
	 		     				fullname=fullname+names[names.length-1]; 
	 		     				sdoc.add(fullname);
	 		     			}
	 		     		}
	 		     		br1.close();
	 		     		ArrayList tdoc=new ArrayList();
	 				    BufferedReader br2=new BufferedReader(new FileReader(tpath)); //path to the input file which lists the full path to the documents to be translated
	 		     		while (true){
	 		     			s=br2.readLine();
	 		     			if (s==null){
	 		     				break;
	 		     			}else{		
	 		     				String names[]=s.replaceAll("\\\\","/").split("/");	
	 		     				String fullname="";
	 		     				if (names[0].contains(":")){
	 		     					names[0]=names[0].replace(":", "@@@");
	 		     				}
	 		     				for (int k=0;k<names.length-1;k++){
	 		     					fullname=fullname+names[k]+"###";
	 		     				} 
	 		     				fullname=fullname+names[names.length-1]; 
	 		     				tdoc.add(fullname);
	 		     			}
	 		     		}
	 		   //  		long startTime=System.currentTimeMillis();
	 				    ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
	 				    for (int i=0;i<sdoc.size();i++){
	 				    	CountDownLatch threadSignal = new CountDownLatch(tdoc.size());//初始化countDown
	 				    	for (int j=0;j<tdoc.size();j++){
	 					         String sn=sdoc.get(i).toString();
	 					         String tn=tdoc.get(j).toString();
	 					         nonENCosine cos=new nonENCosine(ht,bw,sn,tn,threshold,threadSignal);
	 					         threadExecutor.submit(cos);
	 				    	}
	 				    	threadSignal.await();
	 				    	System.out.println((i+1)*tdoc.size() +" document pairs is done!");
	 				    }
	 				    bw.flush();
	 				    bw.close(); 
	 				    threadExecutor.shutdown();
	 			    //    long  endTime=System.currentTimeMillis();
	 				//	 System.out.println("耗时:"+(endTime-startTime)+"毫秒");
	 			    //    System.out.println("processing time of computing comparbiltiy scores: "+(endTime-startTime)+"milliseconds");
	 			        System.out.println("The computation of comparability metrics is all done!");
	 				}catch(Exception ex){
	 				    ex.printStackTrace();
	 				}
	 			    }		 

	 		 
}
