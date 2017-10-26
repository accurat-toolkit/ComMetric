package leeds.cts.nlp;

import java.io.*;
import java.util.*;

import javax.script.*;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
public class dfki {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
    dfki a=new dfki();
     String s="abc????de!fg.go.to.";
     String t[]=s.split("!|\\?|\\.");
     System.out.println(".\\query_accurat.py");
        for (int i=0;i<t.length;i++){
     System.out.println(t[i]);}
    a.sentence("/home/fzsu/MT/de.txt");
   //    a.translation("german","english","/home/fzsu/MT/de.txt","/home/fzsu/MT/translation");
	}

	/*
	 * use standford tool for sentence splitting and tokenization
	 */

	public  void sentence(String input){	
		try{
		BufferedReader br=new BufferedReader(new FileReader(input));
		String s="";
		while (true){
			s=br.readLine();
			if (s==null){
				break;
			}else{
		DocumentPreprocessor docPreprocessor = new DocumentPreprocessor(s);
	   BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(s+".SENT"), "UTF8"));
				    docPreprocessor.setEncoding("UTF-8");
				//     System.out.println();
				    for (List<HasWord> sentence : docPreprocessor) {
				      boolean printSpace = false;
				      String sent="";
				      for (HasWord word : sentence) {
				        if (printSpace) {
				        //	System.out.print(" ");
				        	sent=sent+" ";
				        }
				        printSpace = true;
			//	        System.out.print(word.word());
				        
				        sent=sent+word.word();
				      }
				//      System.out.println();
				//      System.out.println(sent);
				 //        System.out.println(sent);
				  //       sent="en-lt is not included as we do n't have an -RRB- improved system -LRB- yet -RRB- .";
				        sent= sent.replaceAll("-LRB-", "(");
				        sent= sent.replaceAll("-RRB-", ")");
				   //      sent=sent.replaceAll("\\\\", "");
				    //      System.out.println(sent);
				      if (sent.length()>0){
				    	  bw.write(sent);
				    	  bw.newLine();
				      }
				    }
				    bw.flush();
				    bw.close();
		}
		}
		System.out.println("Sentence splitting is DONE");
			}catch(Exception ex){
				ex.printStackTrace();
			}
	}
	

public void translation(String source, String target, String input, String output){
  try {
	  Map<String, String> language = new HashMap<String, String>();
		language.put("english", "eng");
		language.put("german", "ger");
		language.put("croatian", "hrv");
		language.put("greek", "gre");
		language.put("estonian", "est");
		language.put("lithuanian", "lit");
		language.put("latvian", "lav");
		language.put("romanian", "rum");
		language.put("slovenian", "slv");
		String sl=language.get(source.toLowerCase());
		String tl=language.get(target.toLowerCase());
		String transpath=output+File.separator+source+"-translation";
			File f5=new File(transpath);
			if (!f5.exists()){
				f5.mkdir();
			}
	       String s="";
      /*      String[] callAndArgs= {"python","query_accurat.py",input, sl,tl,transpath};
           	
	    //    String[] callAndArgs= {"query_accurat.py",input, sl,tl,transpath};
        //    Process p = Runtime.getRuntime().exec(callAndArgs);
             System.out.println("P="+p);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            // read the output
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            // read any errors
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }  */
	      
	      
	       Process p = null;
	       try {
	    	   String[] callAndArgs= {"python","query_accurat.py",input, sl,tl,transpath};
	           	
	   	    //   String[] callAndArgs= {"query_accurat.py",input, sl,tl,transpath};
	           //    Process p = Runtime.getRuntime().exec(callAndArgs);
	          p = Runtime.getRuntime().exec(callAndArgs);
	       }
	       catch (IOException e) {
	       //   e.printStackTrace();
	       //   String[] callAndArgs= {".\\query_accurat.py",input, sl,tl,transpath};
	    	     String[] callAndArgs= {"cmd.exe", "/C", ".\\query_accurat.py",input, sl,tl,transpath};
	           //    Process p = Runtime.getRuntime().exec(callAndArgs);
	          p = Runtime.getRuntime().exec(callAndArgs);
	   //       System.out.println("p="+p);
	       }
	       if (p != null) {
	    	   BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	            // read the output
	            while ((s = stdInput.readLine()) != null) {
	                System.out.println(s);
	            }
	            // read any errors
	            while ((s = stdError.readLine()) != null) {
	                System.out.println(s);
	            }
	       }
            
            System.out.println("TEXT TRANSLATION IS DONE!!!"); 
            File f=new File(transpath);
            File[] flist=f.listFiles();
             BufferedWriter bw=new BufferedWriter(new FileWriter(output+File.separator+source+"-translation.txt"));
             for (int i=0;i<flist.length;i++){
            	 bw.write(flist[i].getAbsolutePath());
            	 bw.newLine();
             }
             bw.flush();
             bw.close();
             
            BufferedReader br=new BufferedReader(new FileReader(input));
             while (true){
            	 s=br.readLine();
            	 if (s==null){
            		 break;
            	 }else{
            		 File ff=new File(s+".SENT");
            		 if (ff.exists()){
            			 ff.delete();
            		 }
            	 }
             }
       }
        catch (IOException e) {
            System.out.println("exception occured");
            e.printStackTrace();

      //      System.exit(-1);

        }
}


}
