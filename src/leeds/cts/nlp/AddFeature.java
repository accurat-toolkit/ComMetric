package leeds.cts.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.InputStreamReader;


import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;



public class AddFeature {

	/**
	 * @param args
	 */
	

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		   AddFeature a=new AddFeature();
	       //    a.simContentWord();
	       //    a.simSent();
		   a.sentenceRecognition("/home/fzsu/temp/en-zh/en.txt","/home/fzsu/temp/en-zh/zh.txt","/home/fzsu/temp/en-zh/temp");
	 //      a.POStagging("ENGLISH", "/home/fzsu/temp/en-zh/en.txt", "/home/fzsu/temp/en-zh/temp");
	//       a.POStagging("CHINESE", "/home/fzsu/temp/en-zh/temp/CHINESE-translation.txt", "/home/fzsu/temp/en-zh/temp");
		//      a.NumberOfContentWords("ENGLISH","CHINESE","/home/fzsu/temp/en-zh/temp");
		//    a.KeyWordviaTFIDF("/home/fzsu/temp/en-zh/temp");
		   //  a.getNumberOfSentence();
	     //   a.SimKeyWords();
	     //   a.simSent();
		  //    a.simContentWord();
	       //  a.SimpleSentenceDetection();
	       //      a.POStagging();
	       //      a.countPOS();
	       //       a.POSCosine();
		 //  a.lemmatization();
		 //  a.text2vecter4keyword();
		   //a.KeyWordviaTFIDF();
	        //     a.SentenceDisbribution();
	        //   a.Hybrid();
	}

	/*
	 * only count the sentence number of the documents to be evaluated
	 * 
	 * use stanford toolkit for sentence detection
	 */
	public void sentenceRecognition(String sourcePath, String targetPath, String result){
		try{
	
			BufferedReader br1=new BufferedReader(new InputStreamReader(new FileInputStream(sourcePath), "UTF8"));
			  BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(result+File.separator+"SENT.info"), "UTF8"));
			  String s="";
			  while (true){
				  s=br1.readLine();
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
				  DocumentPreprocessor docPreprocessor = new DocumentPreprocessor(s);
				  docPreprocessor.setEncoding("UTF-8");
			    	Iterator iterator=docPreprocessor.iterator();
	    			int count=0;
	    			while (iterator.hasNext()){
	    			//	System.out.println(iterator.toString());
	    				iterator.next();
	    				count++;
	    			}
	    			bw.write(fullname+"\t"+count);
	    			bw.newLine();
	    		//	System.out.println(count);
				/*	for (List<HasWord> sentence : docPreprocessor) {
							      boolean printSpace = false;
							      String sent="";
							      for (HasWord word : sentence) {
							        if (printSpace) {
							        	sent=sent+" ";
							        }
							        printSpace = true;
							        sent=sent+word.word();
							      }				
						      System.out.println(sent);			   
							    }  */		  
				  }	
			  }
			  BufferedReader br2=new BufferedReader(new InputStreamReader(new FileInputStream(targetPath), "UTF8"));
			  while (true){
				  s=br2.readLine();
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
				  DocumentPreprocessor docPreprocessor = new DocumentPreprocessor(s);
				  docPreprocessor.setEncoding("UTF-8");
			    	Iterator iterator=docPreprocessor.iterator();
	    			int count=0;
	    			while (iterator.hasNext()){
	    				iterator.next();
	    				count++;
	    			}
	    		     bw.write(fullname+"\t"+count);
	    		     bw.newLine();
				  }	
			  }
	         bw.flush();
		     bw.close();
		     System.out.println("Sentence counting is done");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/*
	 * another simple way to detect sentences
	 */
	public void SimpleSentenceDetection(String sourcePath, String targetPath){
		try{
	/*		BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/sl-en-TF.cosine"));
			BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/EN-SL/sl-en-simple.addinfo"));
			ArrayList docName=new ArrayList();	
		    String s="";
		    while (true){
		    	s=br.readLine();
		    	if (s==null){
		    		break;
		    	}else{
		    		String t[]=s.split("\\ ");
		    		if (!docName.contains(t[0])){
		    			docName.add(t[0]);
		    			System.out.println(t[0]);
		    			BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/EN-SL/original/"+t[0]));
		//	BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/b.txt"));
		    			String ss="";
		    			int count=0;
		    		//	String sent="";
		    			while (true){
		    				ss=br1.readLine();
		    				if (ss==null){
		    					break;
		    				}else{
		    					if (ss.length()>1){
		    					//	System.out.println(ss);
		    						for (int i=0;i<ss.length();i++){
		    						//	sent=sent+ss.charAt(i);
		    							if ((ss.charAt(i)=='?')||(ss.charAt(i)=='!')){
		    								count++;
		    						//		System.out.println("C1::"+sent);
		    						//		sent="";
		    							}
		    							
		    							if ((ss.charAt(i)=='.') &&(i==ss.length()-1)){
		    								count++;
		    						//		System.out.println("C3::"+sent);
		    							//	sent="";
		    							}
		    							if ((ss.charAt(i)=='.')&&(i<ss.length()-1)){
		    								if (ss.charAt(i+1)==' '){
		    									count++;
		    							//		System.out.println("C4::"+sent);
			    						//		sent="";
		    								}
		    								
		    							}
		    						}
		    						if ((ss.charAt(ss.length()-1)!='!')&&(ss.charAt(ss.length()-1)!='?')&&(ss.charAt(ss.length()-1)!='.')){
	    								if ((ss.charAt(ss.length()-2)!='.')&&(ss.charAt(ss.length()-2)!='?')&&(ss.charAt(ss.length()-2)!='!')){
		    							count++;
	    							//	System.out.println("C2::"+ss.charAt(ss.length()-2)+"->"+ss.charAt(ss.length()-1));
	    							//	System.out.println("C2::"+sent);
	    							//	sent="";
	    								}
	    							}
		    					}
		    				}
		    			}
		    			System.out.println(count);
		    			bw.write(t[0]+" "+count);
		    			bw.newLine();
		    			br1.close();
		    		}
		    		if (!docName.contains(t[1])){
		    			docName.add(t[1]);
		    			System.out.println(t[1]);
		    			BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/EN-SL/original/"+t[1]));
		    			//	BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/b.txt"));
		    			    			String ss="";
		    			    			int count=0;
		    			    	//		String sent="";
		    			    			while (true){
		    			    				ss=br1.readLine();
		    			    				if (ss==null){
		    			    					break;
		    			    				}else{
		    			    					if (ss.length()>1){
		    			    					//	System.out.println(ss);
		    			    						for (int i=0;i<ss.length();i++){
		    			    					//		sent=sent+ss.charAt(i);
		    			    							if ((ss.charAt(i)=='?')||(ss.charAt(i)=='!')){
		    			    								count++;
		    			    						//		System.out.println("C1::"+sent);
		    			    					//			sent="";
		    			    							}
		    			    							
		    			    							if ((ss.charAt(i)=='.') &&(i==ss.length()-1)){
		    			    								count++;
		    			    						//		System.out.println("C3::"+sent);
		    			    						//		sent="";
		    			    							}
		    			    							if ((ss.charAt(i)=='.')&&(i<ss.length()-1)){
		    			    								if (ss.charAt(i+1)==' '){
		    			    									count++;
		    			    						//			System.out.println("C4::"+sent);
		    				    					//			sent="";
		    			    								}
		    			    								
		    			    							}
		    			    						}
		    			    						if ((ss.charAt(ss.length()-1)!='!')&&(ss.charAt(ss.length()-1)!='?')&&(ss.charAt(ss.length()-1)!='.')){
		    		    								if ((ss.charAt(ss.length()-2)!='.')&&(ss.charAt(ss.length()-2)!='?')&&(ss.charAt(ss.length()-2)!='!')){
		    			    							count++;
		    		    							//	System.out.println("C2::"+ss.charAt(ss.length()-2)+"->"+ss.charAt(ss.length()-1));
		    		    							//	System.out.println("C2::"+sent);
		    		    							//	sent="";
		    		    								}
		    		    							}
		    			    					}
		    			    				}
		    			    			}
		    			    			System.out.println(count);
		    			    			bw.write(t[1]+" "+count);
		    			    			bw.newLine();
		    			    			br1.close();
		    		}
		    	}
		    }    */
			
		//	  File f=new File("/home/fzsu/ICC-translation/LV-EN/original");
		//	File f=new File("/home/fzsu/ICC-translation/RO-DE/original");
			File f=new File(sourcePath);  
			
			File[] list=f.listFiles();
		//	  BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/DE-EN/de-en-allsent-simple.info"));
		//	  BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-stem/LV-EN/lv-en.simplesent"));
		//	  BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/RO-DE/ro-de.simplesent"));
			 BufferedWriter bw=new BufferedWriter(new FileWriter(targetPath));  
			for (int k=0;k<list.length;k++){
	    			System.out.println(list[k].getName());
	    			BufferedReader br1=new BufferedReader(new FileReader(list[k].getAbsolutePath()));
	    			    			String ss="";
	    			    			int count=0;
	    			    	//		String sent="";
	    			    			while (true){
	    			    				ss=br1.readLine();
	    			    				if (ss==null){
	    			    					break;
	    			    				}else{
	    			    					if (ss.length()>1){
	    			    					//	System.out.println(ss);
	    			    						for (int i=0;i<ss.length();i++){
	    			    					//		sent=sent+ss.charAt(i);
	    			    							if ((ss.charAt(i)=='?')||(ss.charAt(i)=='!')){
	    			    								count++;
	    			    						//		System.out.println("C1::"+sent);
	    			    					//			sent="";
	    			    							}
	    			    							
	    			    							if ((ss.charAt(i)=='.') &&(i==ss.length()-1)){
	    			    								count++;
	    			    						//		System.out.println("C3::"+sent);
	    			    						//		sent="";
	    			    							}
	    			    							if ((ss.charAt(i)=='.')&&(i<ss.length()-1)){
	    			    								if (ss.charAt(i+1)==' '){
	    			    									count++;
	    			    						//			System.out.println("C4::"+sent);
	    				    					//			sent="";
	    			    								}
	    			    								
	    			    							}
	    			    						}
	    			    						if ((ss.charAt(ss.length()-1)!='!')&&(ss.charAt(ss.length()-1)!='?')&&(ss.charAt(ss.length()-1)!='.')){
	    		    								if ((ss.charAt(ss.length()-2)!='.')&&(ss.charAt(ss.length()-2)!='?')&&(ss.charAt(ss.length()-2)!='!')){
	    			    							count++;
	    		    							//	System.out.println("C2::"+ss.charAt(ss.length()-2)+"->"+ss.charAt(ss.length()-1));
	    		    							//	System.out.println("C2::"+sent);
	    		    							//	sent="";
	    		    								}
	    		    							}
	    			    					}
	    			    				}
	    			    			}
	    			    			System.out.println(count);
	    			    			bw.write(list[k].getName()+" "+count);
	    			    			bw.newLine();
	    			    			br1.close();
			  }
			
	         bw.flush();
		     bw.close();
		     System.out.println("done"); 
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	
	 /*
	  * use standford POS tagger for text pos tagging
	  */
	 
	 public void POStagging(String language,String input, String targetpath){
		 try{
	//		 MaxentTagger tagger = new MaxentTagger("/home/fzsu/Desktop/fzsu/stanford-postagger-full-2011-05-18/models/bidirectional-distsim-wsj-0-18.tagger");
	//		 MaxentTagger tagger = new MaxentTagger("/home/fzsu/Desktop/fzsu/stanford-postagger-full-2011-05-18/models/left3words-distsim-wsj-0-18.tagger");
		//	 File file = new File(".");  
		//		String CurrentPath = file.getCanonicalPath();
			 String savepath=targetpath+File.separator+language+"-POS";
			 File dir=new File(savepath); 
			 dir.mkdirs();	
			 MaxentTagger tagger = new MaxentTagger("left3words-distsim-wsj-0-18.tagger");
			 	String s="";
				BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
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
	                 BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+fullname), "UTF8"));
	    	        String ss="";
	    	        while (true){
	    	        	ss=br1.readLine();
	    	        	if (ss==null){
	    	        		break;
	    	        	}else{
	    	        		if (ss.length()>0){
	    	        			String result=tagger.tagString(ss);
	    	        			bw.write(result);
	    	        			bw.newLine();
	    	        		}
	    	        	}
	    	        }
	    	        bw.flush();
	    	        bw.close();
	    	        br1.close();  
			  }
			    }
		     System.out.println("POS tagging is done");
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }	
	
	

	public void NumberOfContentWords(String source, String target, String path){
		try{
			System.out.println("Start content word counting...");
			 String[][] ru={{"JJ","adj"},{"JJR","adj"},{"JJS","adj"},{"NN","noun"}, 
	                {"NNS","noun"},{"NNP","propernoun"},{"NNPS","propernoun"},{"RB","adverb"},{"RBR","adverb"},{"RBS","adverb"},{"VB","verb"},                                                              
	                {"VBD","verb"},{"VBG","verb"},{"VBN","verb"},{"VBP","verb"},{"VBZ","verb"}                                                  
	            };                                                                                                                                          
	Map map=new HashMap();                                                                                                                      
	for (int i=0;i<ru.length;i++){                                                                                                              
	        map.put(ru[i][0], ru[i][1]);                                                                                                        
	} 
	BufferedWriter bw=new BufferedWriter(new FileWriter(path+File.separator+"ContentWord.info"));
	File f=new File(path+File.separator+source+"-POS");
	File[] list=f.listFiles();
	for (int k=0;k<list.length;k++){
	BufferedReader br=new BufferedReader(new FileReader(list[k].getAbsolutePath()));
	//System.out.println(list[k].getName());
	String s="";
	int Nadj=0;
	int Nadv=0;
	int Nnoun=0;
	int Nverb=0;
	int Nprop=0;
	while (true){
		 s=br.readLine();
		 if (s==null){
			 break;
		 }else{
			 if (s.length()>0 &&s.contains("/")){
			 String t[]=s.split("\\ ");
			 for (int i=0;i<t.length;i++){
				 if (t[i].contains("/")){
					 String str[]=t[i].split("\\/");
				//	 System.out.println(t[i]);
					 if (str.length>1&& map.containsKey(str[1])){                                                                                     
	                    String pos=map.get(str[1]).toString();
	                    if (pos.equals("adj")){
	                   	 Nadj++;
	                    }
	                    if (pos.equals("adverb")){
	                   	 Nadv++;
	                    }
	                    if (pos.equals("noun")){
	                   	 Nnoun++;
	                    }
	                    
	                    if (pos.equals("verb")){
	                   	 Nverb++;
	                    }
	                    if (pos.equals("propernoun")){
	                   	 Nprop++;
	                    }
					 }
				 }
			 }
		 }
		 }
	}
	//  System.out.println("adj:"+Nadj+" "+"noun:"+Nnoun+" "+"verb:"+Nverb+" "+"adv:"+Nadv+" "+"prop:"+Nprop+" ");
	  int count=Nadj+Nnoun+Nadv+Nverb+Nprop;
	  bw.write(list[k].getName()+"\t"+count);
	  bw.newLine();
	  br.close();
	}
	
	f=new File(path+File.separator+target+"-POS");
	list=f.listFiles();
	for (int k=0;k<list.length;k++){
	BufferedReader br=new BufferedReader(new FileReader(list[k].getAbsolutePath()));
//	System.out.println(list[k].getName());
	String s="";
	int Nadj=0;
	int Nadv=0;
	int Nnoun=0;
	int Nverb=0;
	int Nprop=0;
	while (true){
		 s=br.readLine();
		 if (s==null){
			 break;
		 }else{
			 if (s.length()>0 &&s.contains("/")){
			 String t[]=s.split("\\ ");
			 for (int i=0;i<t.length;i++){
				 if (t[i].contains("/")){
					 String str[]=t[i].split("\\/");
				//	 System.out.println(t[i]);
					 if (str.length>1&& map.containsKey(str[1])){                                                                                     
	                    String pos=map.get(str[1]).toString();
	                    if (pos.equals("adj")){
	                   	 Nadj++;
	                    }
	                    if (pos.equals("adverb")){
	                   	 Nadv++;
	                    }
	                    if (pos.equals("noun")){
	                   	 Nnoun++;
	                    }
	                    
	                    if (pos.equals("verb")){
	                   	 Nverb++;
	                    }
	                    if (pos.equals("propernoun")){
	                   	 Nprop++;
	                    }
					 }
				 }
			 }
		 }
		 }
	}
	//  System.out.println("adj:"+Nadj+" "+"noun:"+Nnoun+" "+"verb:"+Nverb+" "+"adv:"+Nadv+" "+"prop:"+Nprop+" ");
	  int count=Nadj+Nnoun+Nadv+Nverb+Nprop;
	  bw.write(list[k].getName()+"\t"+count);
	  bw.newLine();
	  br.close();
	}
	bw.flush();
	bw.close();
	System.out.println("content word counting is done");
	}catch(Exception ex){
			 ex.printStackTrace();
		 }
	}

	/*
	 * compute similarity based on sentence number
	 */

	public void simSent(String simpsentPath, String sentPath, String alignmentPath, String result){
		try{
		//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/sl-en.simplesent"));
		//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.simplesent"));
			BufferedReader br=new BufferedReader(new FileReader(simpsentPath));
			String s="";
			ArrayList simpleDoc=new ArrayList();
			ArrayList simpleSent=new ArrayList();
			while (true){
				s=br.readLine();
				if (s==null){
					break;
				}else{
					String t[]=s.split("\\ ");
				/*	String a[]=t[0].split("\\.");
					t[0]=a[0]; */
					simpleDoc.add(t[0]);
					simpleSent.add(t[1]);
				}
			}
		//	BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.sent"));
			BufferedReader br1=new BufferedReader(new FileReader(sentPath));
			ArrayList Doc=new ArrayList();
			ArrayList Sent=new ArrayList();
			while (true){
				s=br1.readLine();
				if (s==null){
					break;
				}else{
					String t[]=s.split("\\ ");
				//	String a[]=t[0].split("\\.");
				//	t[0]=a[0];
					Doc.add(t[0]);
					Sent.add(t[1]);
				}
			}
		//	BufferedReader br2=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/all/comparability-info.txt"));
		//	BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-stem/SL-EN/all/sl-en-sent.similarity"));
		//	BufferedReader br2=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.label"));
		//	BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/RO-DE/ro-de-sent.similarity"));
			BufferedReader br2=new BufferedReader(new FileReader(alignmentPath));
			BufferedWriter bw=new BufferedWriter(new FileWriter(result));
			int count=0;
			while (true){
				s=br2.readLine();
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
					/*   if (t[0].contains("/")){  // for ET-EN, lt-en datasets and LV-EN, sl-en dataset
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
						    } */
					 String ss="";
					  if (simpleDoc.contains(t[0])&&simpleDoc.contains(t[1])){
						  ss=t[0]+" "+t[1]+" "+t[2];
						  int p=simpleDoc.indexOf(t[0]);
						  int q=simpleDoc.indexOf(t[1]);
						  int length1=Integer.parseInt(simpleSent.get(p).toString());
						  int length2=Integer.parseInt(simpleSent.get(q).toString());
						  double sim=0;
						  if (length1>length2){
							  sim=(double)length2/length1;
						  }else{
							  sim=(double)length1/length2;
						  }
						  sim=Math.floor(sim*10000+0.5)/10000;
						  ss=ss+" "+sim;
						  
					  }else{
						  System.out.println("not in the list simpleSENT:"+t[0]+" "+t[1]);
					  }
					  
					  if (Doc.contains(t[0])&&Doc.contains(t[1])){
						  int p=Doc.indexOf(t[0]);
						  int q=Doc.indexOf(t[1]);
						  int length1=Integer.parseInt(Sent.get(p).toString());
						  int length2=Integer.parseInt(Sent.get(q).toString());
						  double sim=0;
						  if (length1>length2){
							  sim=(double)length2/length1;
						  }else{
							  sim=(double)length1/length2;
						  }
						  sim=Math.floor(sim*10000+0.5)/10000;
						  ss=ss+" "+sim;
					  }else{
						  System.out.println("not in the list SENT:"+t[0]+" "+t[1]);
					  } 
					  if (ss.length()>0){
						  String str[]=ss.split("\\ ");
					/*	  if (str.length!=5){
							  System.out.println("LENGTH IS NOT 5: "+ss);
						  }*/
						  bw.write(ss);
						  bw.newLine();
						  count++;
					  }
				}
			}
			bw.flush();
			bw.close();
			System.out.println(count+"  done!!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	/*
	 * compute similarity based on sentence number, without alignment file provided
	 */

	public void simSentWithoutAlignment(String simpsentPath, String sentPath, String source, String target, String result){
		try{
		//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/sl-en.simplesent"));
		//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.simplesent"));
			BufferedReader br=new BufferedReader(new FileReader(simpsentPath));
			String s="";
			ArrayList simpleDoc=new ArrayList();
			ArrayList simpleSent=new ArrayList();
			while (true){
				s=br.readLine();
				if (s==null){
					break;
				}else{
					String t[]=s.split("\\ ");
				/*	String a[]=t[0].split("\\.");
					t[0]=a[0]; */
					simpleDoc.add(t[0]);
					simpleSent.add(t[1]);
				}
			}
		//	BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.sent"));
			BufferedReader br1=new BufferedReader(new FileReader(sentPath));
			ArrayList Doc=new ArrayList();
			ArrayList Sent=new ArrayList();
			while (true){
				s=br1.readLine();
				if (s==null){
					break;
				}else{
					String t[]=s.split("\\ ");
				//	String a[]=t[0].split("\\.");
				//	t[0]=a[0];
					Doc.add(t[0]);
					Sent.add(t[1]);
				}
			}
			BufferedWriter bw=new BufferedWriter(new FileWriter(result));
			 BufferedReader br2=new BufferedReader(new FileReader(source));
	         ArrayList sDoc=new ArrayList();
	         while (true){
	        	 s=br2.readLine();
	        	 if (s==null){
	        		 break;
	        	 }else{
	        		 String t[]=s.replaceAll("\\\\","/").split("/");
	        		 sDoc.add(t[t.length-1]);
	        	 }
	         }
	         BufferedReader br3=new BufferedReader(new FileReader(target));
	         ArrayList tDoc=new ArrayList();
	         while (true){
	        	 s=br3.readLine();
	        	 if (s==null){
	        		 break;
	        	 }else{
	        		 String t[]=s.replaceAll("\\\\","/").split("/");
	        		 tDoc.add(t[t.length-1]);
	        	 }
	         }
			
	         for (int i=0;i<sDoc.size();i++){
	        	   for (int j=0;j<tDoc.size();j++){
					 String ss="";
					  if (simpleDoc.contains(sDoc.get(i).toString())&&simpleDoc.contains(tDoc.get(j).toString())){
						  ss=sDoc.get(i).toString()+" "+tDoc.get(j).toString();
						  int p=simpleDoc.indexOf(sDoc.get(i).toString());
						  int q=simpleDoc.indexOf(tDoc.get(j).toString());
						  int length1=Integer.parseInt(simpleSent.get(p).toString());
						  int length2=Integer.parseInt(simpleSent.get(q).toString());
						  double sim=0;
						  if (length1>length2){
							  sim=(double)length2/length1;
						  }else{
							  sim=(double)length1/length2;
						  }
						  sim=Math.floor(sim*10000+0.5)/10000;
						  ss=ss+" "+sim;
						  
					  }else{
						  System.out.println("not in the list simpleSENT:"+sDoc.get(i).toString()+" "+tDoc.get(j).toString());
					  }
					  
					  if (Doc.contains(sDoc.get(i).toString())&&Doc.contains(tDoc.get(j).toString())){
						  int p=Doc.indexOf(sDoc.get(i).toString());
						  int q=Doc.indexOf(tDoc.get(j).toString());
						  int length1=Integer.parseInt(Sent.get(p).toString());
						  int length2=Integer.parseInt(Sent.get(q).toString());
						  double sim=0;
						  if (length1>length2){
							  sim=(double)length2/length1;
						  }else{
							  sim=(double)length1/length2;
						  }
						  sim=Math.floor(sim*10000+0.5)/10000;
						  ss=ss+" "+sim;
					  }else{
						  System.out.println("not in the list SENT:"+sDoc.get(i).toString()+" "+tDoc.get(j).toString());
					  } 
					  if (ss.length()>0){
						//  String str[]=ss.split("\\ ");
						  bw.write(ss);
						  bw.newLine();
						
					  }
				}
			}
			bw.flush();
			bw.close();
			System.out.println("done!!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}	
	
/*
 * compute the similarity by content word number, alignment file provided	
 */
	
	
	public void simContentWord(String ContentWordPath, String alignment, String result){
		try{
		//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/sl-en.contentword"));
		//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.contentword"));
			BufferedReader br=new BufferedReader(new FileReader(ContentWordPath));
			String s="";
			ArrayList Doc=new ArrayList();
			ArrayList length=new ArrayList();
			while (true){
				s=br.readLine();
				if (s==null){
					break;
				}else{
					String t[]=s.split("\\ ");
				/*	String a[]=t[0].split("\\.");
					t[0]=a[0]; */
					Doc.add(t[0]);
					length.add(t[1]);
				}
			}
		//	BufferedReader br2=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/SL-EN/all/comparability-info.txt"));
		//	BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-stem/SL-EN/all/sl-en-content.similarity"));
		//	BufferedReader br2=new BufferedReader(new FileReader("/home/fzsu/ICC-translation/RO-DE/ro-de.label"));
		//	BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-translation/RO-DE/ro-de-content.similarity"));
			BufferedReader br2=new BufferedReader(new FileReader(alignment));
			BufferedWriter bw=new BufferedWriter(new FileWriter(result));
			
			int count=0;
			while (true){
				s=br2.readLine();
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
				/*	   if (t[0].contains("/")){  // for ET-EN, lt-en datasets and LV-EN, sl-en dataset
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
						    }  */
					
					  if (Doc.contains(t[0])&&Doc.contains(t[1])){
						String ss=t[0]+" "+t[1]+" "+t[2];
						  int p=Doc.indexOf(t[0]);
						  int q=Doc.indexOf(t[1]);
						  int length1=Integer.parseInt(length.get(p).toString());
						  int length2=Integer.parseInt(length.get(q).toString());
						  double sim=0;
						  if (length1>length2){
							  sim=(double)length2/length1;
						  }else{
							  sim=(double)length1/length2;
						  }
						  sim=Math.floor(sim*10000+0.5)/10000;
						  ss=ss+" "+sim;
						  bw.write(ss);
						  bw.newLine();
						  count++;
						  
					  }else{
						  System.out.println("not in the list:"+t[0]+" "+t[1]);
					  }
				}
			}
			bw.flush();
			bw.close();
			System.out.println(count+"  done!!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	/*
	 * similiarity based on content word number, without alignment file
	 */
	
	public void simContentWordWithoutAlignment(String ContentWordPath, String source, String target, String result){
		try{
		
			BufferedReader br=new BufferedReader(new FileReader(ContentWordPath));
			String s="";
			ArrayList Doc=new ArrayList();
			ArrayList length=new ArrayList();
			while (true){
				s=br.readLine();
				if (s==null){
					break;
				}else{
					String t[]=s.split("\\ ");
				/*	String a[]=t[0].split("\\.");
					t[0]=a[0]; */
					Doc.add(t[0]);
					length.add(t[1]);
				}
			}
		
			BufferedWriter bw=new BufferedWriter(new FileWriter(result));
			 BufferedReader br2=new BufferedReader(new FileReader(source));
	         ArrayList sDoc=new ArrayList();
	         while (true){
	        	 s=br2.readLine();
	        	 if (s==null){
	        		 break;
	        	 }else{
	        		 String t[]=s.replaceAll("\\\\","/").split("/");
	        		 sDoc.add(t[t.length-1]);
	        	 }
	         }
	         BufferedReader br3=new BufferedReader(new FileReader(target));
	         ArrayList tDoc=new ArrayList();
	         while (true){
	        	 s=br3.readLine();
	        	 if (s==null){
	        		 break;
	        	 }else{
	        		 String t[]=s.replaceAll("\\\\","/").split("/");
	        		 tDoc.add(t[t.length-1]);
	        	 }
	         }
			
	         for (int i=0;i<sDoc.size();i++){
	        	   for (int j=0;j<tDoc.size();j++){
					  if (Doc.contains(sDoc.get(i).toString())&&Doc.contains(tDoc.get(j).toString())){
						String ss=sDoc.get(i).toString()+" "+tDoc.get(j).toString();
						  int p=Doc.indexOf(sDoc.get(i).toString());
						  int q=Doc.indexOf(tDoc.get(j).toString());
						  int length1=Integer.parseInt(length.get(p).toString());
						  int length2=Integer.parseInt(length.get(q).toString());
						  double sim=0;
						  if (length1>length2){
							  sim=(double)length2/length1;
						  }else{
							  sim=(double)length1/length2;
						  }
						  sim=Math.floor(sim*10000+0.5)/10000;
						  ss=ss+" "+sim;
						  bw.write(ss);
						  bw.newLine();  
					  }else{
						  System.out.println("not in the list:"+sDoc.get(i).toString()+" "+tDoc.get(j).toString());
					  }
				}
			}
			bw.flush();
			bw.close();
			System.out.println("  done!!");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}	
	
	
	

	public boolean containsNumbers(String content){
		 boolean flag=false;
		  Pattern p=Pattern.compile(".*\\d+.*");
		  Matcher m=p.matcher(content);
		  if(m.matches())
		  flag=true;
		  return flag;
	}	

	
/*
 * stop word filtering, and convert the document texts into vectors.
 */
	 public void text2vecter4keyword(String s1,String s2, String s3){
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
            
      //       BufferedWriter bw3=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/media/FreeAgent Drive/ICC-missing/lv-lt/keyword"+File.separator+"word.vectors"), "UTF8"));
            
       //      convert(stopword, "/media/FreeAgent Drive/ICC-missing/lv-lt/keyword/latvian-lemma", bw3);
       //      convert(stopword, "/media/FreeAgent Drive/ICC-missing/lv-lt/keyword/lithuanian-lemma", bw3);
             BufferedWriter bw3=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(s3), "UTF8"));
             
             convert(stopword, s1, bw3);
             convert(stopword, s2, bw3);
             bw3.flush();
             bw3.close();
             System.out.println("The conversion of the document text into feature vectors is done!");
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }

public void convert(ArrayList stopword,  String targetpath, BufferedWriter bw3){
try{ 
	File f=new File(targetpath);
	 File[] list=f.listFiles();
    String s="";
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
       		 String t[]=s.split(" ");
       		 for (int j=0;j<t.length;j++){
       			 if (t[j].length()>2){  // the word should be longer than 2 characters
       			//	 System.out.println(t[j]);
       				 t[j]=t[j].toLowerCase();
       				 if (t[j].matches("[a-z-]+")==true &&t[j].charAt(0)!='-'){ //judge an English word
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
   	 String wordvector=list[i].getName();
   	 for (int k=0;k<word.size();k++){ 
   		 wordvector=wordvector+"	"+word.get(k).toString()+" "+count.get(k).toString();
   	 }
   	br.close();
   	bw3.write(wordvector);
   	bw3.newLine();
    }

}catch(Exception ex){
	ex.printStackTrace();
}
}



/*
 * use the formula FIDFi,j = ( Ni,j / N*,j ) * log( D / Di ), then cosine normalized. 
 * Ni,j = the number of times word i appears in document j (the original cell count).
 * N*,j = the number of total words in document j (just add the counts in column j).
 * D = the number of documents (the number of columns).
 * Di = the number of documents in which word i appears (the number of non-zero columns in row i).
 */
public  void KeyWordviaTFIDF(String path){
	   try{	   
	       System.out.println("Start keyword extraction ...");
		   BufferedReader br=new BufferedReader(new FileReader(path+File.separator+"index.vectors"));
		   BufferedReader br1=new BufferedReader(new FileReader(path+File.separator+"index.vectors"));
		   BufferedWriter bw=new BufferedWriter(new FileWriter(path+File.separator+"keyword.tfidf"));// result 	  
	   ArrayList a1=new ArrayList(); //store the feature index
	   ArrayList a2=new ArrayList(); //store the document frequency: DF
	   String s="";
	   int docnum=0; //the total number of documents in the collection
	   /*
	    * compute the document frequency DF. 
	    */
	   while (true){
		   s=br.readLine();
		   if (s==null){
			   break;
		   }
		   else {
			   docnum++;
			   String t[]=s.split("\\t");
		/*	   if (t.length<=11){
				   System.out.println(t.length+" :"+s);
			   }  */
			   for (int i=1;i<t.length;i++){
				   String str[]=t[i].split("\\ ");
				   
				   if (!a1.contains(str[0])){
					   a1.add(str[0]); //store feature
					   a2.add(1); //store document frequency of that feature
				   }
				   else {
					   int p=a1.indexOf(str[0]);
					   int m=(Integer)a2.get(p)+1;
					   a2.set(p,m);
					   
				   }
			   
			   }
		   }
	   }
	   System.out.println("lexicon size:"+a1.size());
	   br.close();
	   /*
	    * compute the TFIDF score and then cosine normalization. 
	    */
	
	   while (true){
		   s=br1.readLine();
		   if (s==null){
			   break;
		   }
		   else {
			   String t[]=s.split("\\t");
			   //System.out.println(t[0]);
			   double weight[]=new double[t.length-1];
			   String word[]=new String[t.length-1];
			   String WF[]=new String[t.length-1]; // word frequency in a document
			   String ss=t[0];
			   double sum=0;
			   int DocSize=0; // the total number of words in  a document
			   for (int i=1;i<t.length;i++){
				   String str[]=t[i].split("\\ ");
				   DocSize=DocSize+Integer.parseInt(str[1]);
				   
			   }
			   for (int i=1;i<t.length;i++){
				   String str[]=t[i].split("\\ ");
				   word[i-1]=str[0];
				   WF[i-1]=str[1];
				   int p=a1.indexOf(str[0]);
				   int DF=(Integer)a2.get(p);
				   double TF=(double)Integer.parseInt(str[1])/DocSize; //.tfidf, standard TF, relative term frequency
				//   int TF=Integer.parseInt(str[1]);
				//   double TF=Double.parseDouble(str[1]); // use really term frequency .tfidf1
				//   double TF=Math.log(Double.parseDouble(str[1]))+1; //  .tfidf2, use log(tf)+1
				   double value=TF*Math.log((double)docnum/DF);  //tf*idf value
				   sum=sum+Math.pow(value, 2);
				   weight[i-1]=value;
			   }
			   SortWeight(word,weight,WF);
			   for (int i=0;i<weight.length;i++){  //cosine normalization
				//   System.out.print(index[i]+":"+weight[i]+" ");
				//   weight[i]=weight[i]/Math.sqrt(sum); //normalization
				   weight[i]=Math.floor(weight[i]*1000+0.5)/1000;
				   if (weight[i]>=0.001){
				   ss=ss+"\t"+word[i]+" "+weight[i]+":"+WF[i];
				   }
			   }
			//   System.out.println();
			   bw.write(ss);
			   bw.newLine();
		   }
	   }
	   bw.flush();
	   bw.close();
	
	   System.out.println("Key word extraction is done");
	   }catch (Exception ex){
		   ex.printStackTrace();
	   }
  }


public static void SortWeight(String[] word, double[] weight, String[] WF) {   
	  for (int i = 0; i < weight.length; i++) {   
	   for (int j = i + 1; j < weight.length; j++) { 
	       if (weight[i]<weight[j]){
	    	double temp=weight[i];
	    	weight[i]=weight[j];
	    	weight[j]=temp;
	    	String tempWord=word[i];
	    	word[i]=word[j];
	    	word[j]=tempWord;
	    	String tempWF=WF[i];
	    	WF[i]=WF[j];
	    	WF[j]=tempWF;
	    }
	   }   
	  }   
	 }
	
	 /*
	  * compute the similarity of keywords via cosine, with alignment file provided
	  */

	 public void SimKeyWords(String keyPath, String alignment,String result){
	 	try{
	 	//	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("/home/fzsu/ICC-stem/LV-EN/lemmatization/word.tfidf"), "UTF8"));
	 	//	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("/home/fzsu/ICC-translation/RO-DE/word.tfidf"), "UTF8"));
	 		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(keyPath), "UTF8"));
	 		String s="";
	         ArrayList DocName=new ArrayList();
	         ArrayList alist=new ArrayList();
	         while (true){
	         	s=br.readLine();
	         	if (s==null){
	         		break;
	         	}else{
	         		String t[]=s.split("\\t");
	         		/*	String str[]=t[0].split("\\.");
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
	         	//	if (t.length<=10){
	         	//		System.out.println(s);
	         	//	}
	         	}
	         }
	         System.out.println(DocName.size()+"  "+alist.size());
	     //    BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/ICC-stem/LV-EN/all/lv-en.gold"));  
	 	//    BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/ICC-stem/LV-EN/all/lv-en.keyword"));
	    //     BufferedReader br1=new BufferedReader(new FileReader("/home/fzsu/Desktop/fzsu/ICC-translation/RO-DE/ro-de.label"));  
	 	 //   BufferedWriter bw=new BufferedWriter(new FileWriter("/home/fzsu/Desktop/fzsu/ICC-translation/RO-DE/ro-de.keyword"));
	        
	         BufferedReader br1=new BufferedReader(new FileReader(alignment));  
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
	 		/*    if (t[0].contains("/")){  // for ET-EN, lt-en datasets and LV-EN, sl-en dataset
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
	 			ArrayList TFvalue1=new ArrayList();
	 			ArrayList TFIDFvalue1=new ArrayList();
	 			ArrayList id1=new ArrayList();
	 			for (int k=1;k<t1.length;k++){
	 				  String w[]=t1[k].split("\\ ");
	 				  String u[]=w[1].split(":");
	 				  id1.add(w[0]);
	 				  TFvalue1.add(u[1]);  // term frequency
	 				  TFIDFvalue1.add(u[0]);  // tfidf value
	 				}
	 			ArrayList id2=new ArrayList();
	 			ArrayList TFvalue2=new ArrayList();
	 			ArrayList TFIDFvalue2=new ArrayList();
	 			for (int k=1;k<t2.length;k++){
	 				String w[]=t2[k].split("\\ ");
	 				String u[]=w[1].split(":");
	 				id2.add(w[0]);
	 				TFvalue2.add(u[1]); // term frequency
	 				TFIDFvalue2.add(u[0]); // tfidf value
	 				}
	 			//	double sim=GETcosine(15,id1,TFvalue1,id2,TFvalue2);
	 			//	ss=ss+" "+sim; // for ET-EN dataset and LV-EN dataset
	 				double sim=GETcosine(20,id1,TFvalue1,id2,TFvalue2);
	 				ss=ss+" "+sim;
	 			/*	sim=GETcosine(30,id1,TFvalue1,id2,TFvalue2);
	 				ss=ss+" "+sim;
	 				sim=GETcosine(40,id1,TFvalue1,id2,TFvalue2);
	 				ss=ss+" "+sim;
	 				sim=GETcosine(15,id1,TFIDFvalue1,id2,TFIDFvalue2);
	 				ss=ss+" "+sim;
	 				sim=GETcosine(20,id1,TFIDFvalue1,id2,TFIDFvalue2);
	 				ss=ss+" "+sim;
	 				sim=GETcosine(30,id1,TFIDFvalue1,id2,TFIDFvalue2);
	 				ss=ss+" "+sim;
	 				sim=GETcosine(40,id1,TFIDFvalue1,id2,TFIDFvalue2);
	 				ss=ss+" "+sim; */
	 			//	System.out.println(ss);
	 		
	 			
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


	 public void SimKeyWordsWithoutAlignment(String keyPath, String source, String target,String result){
		 	try{
		 
		 		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(keyPath), "UTF8"));
		 		String s="";
		         ArrayList DocName=new ArrayList();
		         ArrayList alist=new ArrayList();
		         while (true){
		         	s=br.readLine();
		         	if (s==null){
		         		break;
		         	}else{
		         		String t[]=s.split("\\t");
		         		
		 				DocName.add(t[0]);
		 				alist.add(s);
		        
		         	}
		         }
		         System.out.println(DocName.size()+"  "+alist.size());
		    
			 	    BufferedWriter bw=new BufferedWriter(new FileWriter(result));
			 	   BufferedReader br2=new BufferedReader(new FileReader(source));
			         ArrayList sDoc=new ArrayList();
			         while (true){
			        	 s=br2.readLine();
			        	 if (s==null){
			        		 break;
			        	 }else{
			        		 String t[]=s.replaceAll("\\\\","/").split("/");
			        		 sDoc.add(t[t.length-1]);
			        	 }
			         }
			         BufferedReader br3=new BufferedReader(new FileReader(target));
			         ArrayList tDoc=new ArrayList();
			         while (true){
			        	 s=br3.readLine();
			        	 if (s==null){
			        		 break;
			        	 }else{
			        		 String t[]=s.replaceAll("\\\\","/").split("/");
			        		 tDoc.add(t[t.length-1]);
			        	 }
			         }
					
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
		 			ArrayList TFvalue1=new ArrayList();
		 			ArrayList TFIDFvalue1=new ArrayList();
		 			ArrayList id1=new ArrayList();
		 			for (int k=1;k<t1.length;k++){
		 				  String w[]=t1[k].split("\\ ");
		 				  String u[]=w[1].split(":");
		 				  id1.add(w[0]);
		 				  TFvalue1.add(u[1]);  // term frequency
		 				  TFIDFvalue1.add(u[0]);  // tfidf value
		 				}
		 			ArrayList id2=new ArrayList();
		 			ArrayList TFvalue2=new ArrayList();
		 			ArrayList TFIDFvalue2=new ArrayList();
		 			for (int k=1;k<t2.length;k++){
		 				String w[]=t2[k].split("\\ ");
		 				String u[]=w[1].split(":");
		 				id2.add(w[0]);
		 				TFvalue2.add(u[1]); // term frequency
		 				TFIDFvalue2.add(u[0]); // tfidf value
		 				}

		 				double sim=GETcosine(20,id1,TFvalue1,id2,TFvalue2);
		 				ss=ss+" "+sim;
		 		    	bw.write(ss);
		 		    	bw.newLine();
		 		    }else{
		 		    	System.out.println("no in the file list:"+sDoc.get(i).toString()+" "+tDoc.get(j).toString());
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
	 
	 
	 
	 public double GETcosine(int keyNum, ArrayList ID1, ArrayList Val1, ArrayList ID2, ArrayList Val2){
	 	  
	 	double sim=0;
	 	double sum1=0;
	 	ArrayList subID1=new ArrayList();
	 	ArrayList subID2=new ArrayList();
	 	ArrayList subVal1=new ArrayList();
	 	ArrayList subVal2=new ArrayList();
	 	if (Val1.size()>keyNum){
	 	for (int i=0;i<keyNum;i++){
	 	    double v1=Double.parseDouble(Val1.get(i).toString());
	 	    sum1=sum1+v1*v1;
	 	    subID1.add(ID1.get(i).toString());
	 	    subVal1.add(Val1.get(i).toString());
	 	}
	 	}else{
	 		for (int i=0;i<Val1.size();i++){
	 		    double v1=Double.parseDouble(Val1.get(i).toString());
	 		    sum1=sum1+v1*v1;
	 		    subID1.add(ID1.get(i).toString());
	 		    subVal1.add(Val1.get(i).toString());
	 		}
	 	}
	 	double sum2=0;
	 	if (Val2.size()>keyNum){
	 		for (int i=0;i<keyNum;i++){
	 		    double v2=Double.parseDouble(Val2.get(i).toString());
	 		    sum2=sum2+v2*v2;
	 		    subID2.add(ID2.get(i).toString());
	 		    subVal2.add(Val2.get(i).toString());
	 		}
	 	}else{
	 	for (int i=0;i<Val2.size();i++){
	 	    double v2=Double.parseDouble(Val2.get(i).toString());
	 	    sum2=sum2+v2*v2;
	 	    subID2.add(ID2.get(i).toString());
	 	    subVal2.add(Val2.get(i).toString());
	 	}
	 	}
	 	for (int i=0;i<subID1.size();i++){
	 	    if (subID2.contains(subID1.get(i).toString())){
	 		int p=subID2.indexOf(subID1.get(i).toString());
	 		double v1=Double.parseDouble(subVal1.get(i).toString());
	 		double v2=Double.parseDouble(subVal2.get(p).toString());
	 		sim=sim+v1*v2;
	 	    }
	 	}
	 	sim=sim/(Math.sqrt(sum1)*Math.sqrt(sum2));
	 	// System.out.println(sim);
	 	sim=Math.floor(sim*10000+0.5)/10000;
	 	//  System.out.println(sim);
	 	return sim;	 
	     }	 
	 


	 public void Hybrid(String lexPath, String keyPath, String nerPath, String sentPath, String contentWordPath, String output){
		 try{
			 BufferedReader br1=new BufferedReader(new FileReader(lexPath));                            
			 BufferedReader br2=new BufferedReader(new FileReader(keyPath));  
			 BufferedReader br3=new BufferedReader(new FileReader(nerPath));                            
			 BufferedReader br4=new BufferedReader(new FileReader(sentPath));      
			 BufferedReader br5=new BufferedReader(new FileReader(contentWordPath));                            
			 BufferedWriter bw=new BufferedWriter(new FileWriter(output));
		     String s1="";
		     String s2="";
		     String s3="";
		     String s4="";
		     String s5="";
		     while (true){
		    	 s1=br1.readLine();
		    	 s2=br2.readLine();
		    	 s3=br3.readLine();
		    	 s4=br4.readLine();
		    	 s5=br5.readLine();
		    	 if (s1==null){
		    		 break;
		    	 }else{
		    		 String a[]=s1.split("\\ ");
		    		 String b[]=s2.split("\\ ");
		    		 String c[]=s3.split("\\ ");
		    		 String d[]=s4.split("\\ ");
		    		 String e[]=s5.split("\\ ");
		    		 double simLex=Double.valueOf(a[2]);
		    		 double simKey=Double.valueOf(b[2]);
		    		 double simNER=Double.valueOf(c[2]);
		    		 double simSent=Double.valueOf(d[2]);
		    		 double simContentWord=Double.valueOf(e[2]);
		    		 double v[]=new double[15];
		    		 for (int i=0;i<v.length;i++){
		    			 v[i]=0;
		    		 }
		    		 double sim=0.5*simLex+0.2*(simKey+simNER)+0.1*0.5*(simSent+simContentWord); 
		    		 sim=Math.floor(sim*10000+0.5)/10000;
		    		 String ss=a[0]+" "+a[1]+" "+sim;
		    		 bw.write(ss);
		    		 bw.newLine();
		    	 }
		     }
		     bw.flush();
		     bw.close();
		     System.out.println("done!");
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }	 
	 
	 
}

