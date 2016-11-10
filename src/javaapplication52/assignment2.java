package javaapplication52;

import java.io.*;
import java.util.*;

/**
 * Purpose of Module :- This is the main module which has the "main" function. This module reads the datafile and the required support size.
 *                      It then computes frequent itemsets based on methods in this class. 
 * 
 * @authors Ashutosh Agarwal
 */


public class assignment2 extends Observable 
{
    public static void main(String[] args) throws Exception 
    {
        assignment2 ap = new assignment2(args);
    }

    private List<int[]> IS ;       // list of items that exist in sets 
    private String fileN;          // The name of file that is input by the user.
    private int sizeI;             // Number of distinct itemsets in the given file.
    private int sizeT;             // Total number of transactions that exist in the given file.
    private double supMin;         // Minimum support that is to be used as provided by the user.
    private boolean isUsed = false; //Check if command line has been used to provide the input.

    //Main instantiable constructor to be used for the program to work.
    public assignment2(String[] args, Observer o) throws Exception
    {
    	isUsed = true;
    	modify(args);
    	this.addObserver(o);
    	proceed();
    }
    
    public assignment2(String[] args) throws Exception     // creates the list of itemsets from the given input.
    {
        modify(args);
        proceed();
    }

    private void proceed() throws Exception 
    {
        long st = System.currentTimeMillis();
        int FS=0;        
        int isN=1; 
        createIS();
        
        while (IS.size()>0)
        {

            calc();
            if(!(IS.size()==0))
            {
                FS+=IS.size();
                disp("Found "+IS.size()+" frequent itemsets of size " + isN + " (with support "+(supMin*100)+"%)");;
                createPrev();
            }

            isN++;
        } 

        long end = System.currentTimeMillis();
        disp("Total time taken: "+((double)(end-st)/1000) + " seconds.");
        disp("Number of "+FS+ " sets that are frequent for support "+(supMin*100)+"% (absolute "+Math.round(sizeT*supMin)+")");
        disp("Finished execution");
    }

    private void success(int[] itemset, int support) 
    {
    	if (isUsed) 
        {
            this.setChanged();
            notifyObservers(itemset);
    	}
    	else 
        {
            System.out.println(Arrays.toString(itemset) + "  ("+ ((support / (double) sizeT))+" "+support+")");
        }
    }

    private void disp(String message) 
    {
    	if (isUsed == false) 
        {
    		System.err.println(message);
    	}
    }

    private void modify(String[] args) throws Exception
    {        
        Scanner sc = new Scanner(System.in) ;
        if (!(args.length==0)) 
            fileN = args[0];
        else fileN = sc.next(); 
    	
    	if (args.length>=2) 
            supMin=(Double.valueOf(args[1]).doubleValue());    	
    	else 
            supMin = sc.nextDouble();
    	if (supMin>1 || supMin<0) throw new Exception("Error: Support should be between 0 and 1.");
    	
    	sizeI = 0;
    	sizeT=0;
    	BufferedReader din = new BufferedReader(new FileReader(fileN));
    	while (din.ready()) 
        {    		
    		String sl=din.readLine();
    		if (sl.matches("\\s*")) 
                    continue; 
    		sizeT++;
    		StringTokenizer tok = new StringTokenizer(sl," ");
    		while (tok.hasMoreTokens()) 
                {
    			int x = Integer.parseInt(tok.nextToken());
    			if (x+1>sizeI) sizeI=x+1;
    		}    		
    	}  
    	
        oconf();

    }

	private void oconf() 
        {
		 disp("Input : "+sizeI+" items, "+sizeT+" transactions, ");
		 disp("Minimum Support = "+supMin+"%");
	}

	private void createIS() 
        {
            IS = new ArrayList<int[]>();
            for(int i=0; i<sizeI; i++)
            {
        	int[] c = {i};
        	IS.add(c);
            }
	}
			
    private void createPrev()
    {
    	int sizeIS = IS.get(0).length;    		
    	HashMap<String, int[]> cand = new HashMap<String, int[]>(); 
        disp("Creating itemsets of size "+(sizeIS+1)+" based on "+IS.size()+" itemsets of size "+sizeIS);

        for(int i=0; i<IS.size(); i++)
        {
            for(int j=i+1; j<IS.size(); j++)
            {
                int[] a1 = IS.get(i);
                int[] a2 = IS.get(j);

                assert (a1.length==a2.length);
                
                int [] candNew = new int[sizeIS+1];
                for(int s=0; s<candNew.length-1; s++) {
                	candNew[s] = a1[s];
                }
                    
                int dif = 0;
                for(int k=0; k<a2.length; k++)
                {
                	boolean exists = false;
                    for(int l=0; l<a2.length; l++) 
                    {
                    	if (a1[l]==a2[k]) 
                        { 
                    		exists = true;
                    		break;
                    	}
                    }
                    if (!exists)
                    { 
                		dif++;
                		candNew[candNew.length -1] = a2[k];
                	}
            	
            	}
                
                assert(dif>0);
                
                
                if (dif==1) 
                {
                	Arrays.sort(candNew);
                	cand.put(Arrays.toString(candNew),candNew);
                }
            }
        }
        
        //make the new itemsets
        IS = new ArrayList<int[]>(cand.values());
    	disp("Successfully made "+IS.size()+" unique itemsets having a length of "+(sizeIS+1));

    }

    private void convert(String l, boolean[] trans) 
    {
	    Arrays.fill(trans, false);
	    StringTokenizer sF = new StringTokenizer(l, " "); 
	    while (sF.hasMoreTokens())
	    {
	    	
	        int i = Integer.parseInt(sF.nextToken());
			trans[i]=true; //in case non-zero, initialize with true.
	    }
    }

    private void calc() throws Exception
    {
        disp("Moving through data to compute " + IS.size()+ " itemsets having a length of "+IS.get(0).length);

        List<int[]> frequentCandidates = new ArrayList<int[]>(); //Frequent sets found for the given itemset.

        boolean equal; //check if a transaction has items from a given itemset.
        int ctr[] = new int[IS.size()]; //the number of successful matches, initialized by zeros

		BufferedReader din = new BufferedReader(new InputStreamReader(new FileInputStream(fileN)));

		boolean[] trans = new boolean[sizeI];
		
		// for each individiual transaction that exists in the input file.
		for (int i = 0; i < sizeT; i++) 
                {
                    String line = din.readLine();
                    convert(line, trans);
                    for (int x= 0; x < IS.size(); x++) 
                    {
                    	equal = true;          
			int[] cand = IS.get(x);
			for (int tz : cand) 
                        {
                            if (trans[tz] == false) 
                            {
				equal = false;
				break;
                            }
			}
			if(!(!equal)) 
                        {
                            ctr[x]++; // in case they match, increase the counter.
			}
                    }

		}
		
		din.close();

		for (int i = 0; i < IS.size(); i++) 
                {
			if ((ctr[i] / (double) (sizeT)) >= supMin) 
                        {
				success(IS.get(i),ctr[i]);
				frequentCandidates.add(IS.get(i));
			}
		}

        IS = frequentCandidates; // Since the candidates for the previous iterations will be itemsets for next.
    }
}