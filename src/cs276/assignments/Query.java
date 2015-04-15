package cs276.assignments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Query {

	// Term id -> position in index file
	private static Map<Integer, Long> posDict = new TreeMap<Integer, Long>();
	// Term id -> document frequency
	private static Map<Integer, Integer> freqDict = new TreeMap<Integer, Integer>();
	// Doc id -> doc name dictionary
	private static Map<Integer, String> docDict = new TreeMap<Integer, String>();
	// Term -> term id dictionary
	private static Map<String, Integer> termDict = new TreeMap<String, Integer>();
	// Index
	private static BaseIndex index = null;

	private static boolean debug = false;
	
	/*
	 * Write a posting list with a given termID from the file
	 * You should seek to the file position of this specific
	 * posting list and read it back.
	 * */
	private static PostingList readPosting(FileChannel fc, int termId) throws IOException
	{
		/*
		 * Your code here
		 */
		Long pos = posDict.get(termId);
		Integer freq = freqDict.get(termId);
		
		if (debug) System.out.println("termId: " + termId + " pos: " + pos + " freq: " + freq);
		
		PostingList postings = index.readPosting(fc, termId, pos, freq);
		
		if (debug) System.out.println(postings);
		
		return postings;
	}
	
	static Integer popNextOrNull(Iterator<Integer> d)
	{
		if (d.hasNext()) {
		  return d.next();
		} else {
		  return null;
		}
	}

	static List<Integer> intersect(List<Integer> docList1, List<Integer> docList2)
	{
	    List<Integer> answer = new ArrayList<Integer>();

	    Iterator<Integer> docIds1 = docList1.iterator();
	    Iterator<Integer> docIds2 = docList2.iterator();
	    
	    Integer docId1 = popNextOrNull(docIds1);
	    Integer docId2 = popNextOrNull(docIds2);

	    while (docId1 != null && docId2 != null)
	    {
	    	if (docId1.intValue() == docId2.intValue())
	    	{
	    		answer.add(docId1);
	    		docId1 = popNextOrNull(docIds1);
	    		docId2 = popNextOrNull(docIds2);
	    	}
	    	else
	    	{
	    		if(docId1.intValue() < docId2.intValue())
	    		{
	    			docId1 = popNextOrNull(docIds1);
	    		}
	    		else
	    		{
	    			docId2 = popNextOrNull(docIds2);    
	    		}
	    	}
	    }

		return answer;
	}
	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e1.getValue().compareTo(e2.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}

	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 2 && !debug) {
			System.err.println("Usage: java Query [Basic|VB|Gamma] index_dir");
			return;
		}

		/* Get index */
		String className = "cs276.assignments." + args[0] + "Index";
		try {
			Class<?> indexClass = Class.forName(className);
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get index directory */
		String input = args[1];
		File inputdir = new File(input);
		if (!inputdir.exists() || !inputdir.isDirectory()) {
			System.err.println("Invalid index directory: " + input);
			return;
		}

		/* Index file */
		RandomAccessFile indexFile = new RandomAccessFile(new File(input,
				"corpus.index"), "r");

		String line = null;
		/* Term dictionary */
		BufferedReader termReader = new BufferedReader(new FileReader(new File(
				input, "term.dict")));
		while ((line = termReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			termDict.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		termReader.close();

		/* Doc dictionary */
		BufferedReader docReader = new BufferedReader(new FileReader(new File(
				input, "doc.dict")));
		while ((line = docReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			docDict.put(Integer.parseInt(tokens[1]), tokens[0]);
		}
		docReader.close();

		/* Posting dictionary */
		BufferedReader postReader = new BufferedReader(new FileReader(new File(
				input, "posting.dict")));
		while ((line = postReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			posDict.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
			freqDict.put(Integer.parseInt(tokens[0]),
					Integer.parseInt(tokens[2]));
		}
		postReader.close();

		/* Processing queries */
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		if (debug && args.length == 3)
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
		}

		/* For each query */
		while ((line = br.readLine()) != null) {
			/*
			 * Your code here
			 */
			Map<Integer, Integer> termFreqDict = new TreeMap<Integer, Integer>();

			String[] tokens = line.trim().split("\\s+");
			for (String term : tokens)
			{
				Integer termId = termDict.get(term);

				if (debug) System.out.println("term: " + term + " termId: " + termId);
				
				if (null == termId)
				{
					System.out.println("no results found");
					System.exit(0);
				}
				
				termFreqDict.put(termId, freqDict.get(termId));
			}
			
			SortedSet<Map.Entry<Integer, Integer>> sortedTermFreqDict = entriesSortedByValues(termFreqDict);
			if (debug) System.out.println(sortedTermFreqDict);
			
			Iterator<Map.Entry<Integer,  Integer>> termIter = sortedTermFreqDict.iterator();

			List<Integer> answer = readPosting(indexFile.getChannel(), termIter.next().getKey()).getList();
			
			while (termIter.hasNext())
			{
				answer = intersect(answer, readPosting(indexFile.getChannel(), termIter.next().getKey()).getList());
			}
			
			if (debug) System.out.println("docIds: " + answer);
			
			for (Integer docId : answer)
			{
				System.out.println(docDict.get(docId));
			}

		}
		br.close();
		indexFile.close();
	}
}
