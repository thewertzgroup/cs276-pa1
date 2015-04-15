package cs276.assignments;

import cs276.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;

public class Index 
{
	private static boolean debug = true;
	
	// Term id -> (position in index file, doc frequency) dictionary
	private static Map<Integer, Pair<Long, Integer>> postingDict = new TreeMap<Integer, Pair<Long, Integer>>();
	
	// Doc name -> doc id dictionary
	private static Map<String, Integer> docDict = new TreeMap<String, Integer>();
	
	// Term -> term id dictionary
	private static Map<String, Integer> termDict = new TreeMap<String, Integer>();
	
	// Block queue
	private static LinkedList<File> blockQueue = new LinkedList<File>();

	// Total file counter
	private static int totalFileCount = 0;
	// Document counter
	private static int docIdCounter = 0;
	// Term counter
	private static int wordIdCounter = 0;
	// Index
	private static BaseIndex index = null;

	
	/* 
	 * Write a posting list to the file 
	 * You should record the file position of this posting list
	 * so that you can read it back during retrieval
	 * 
	 * */
	private static void writePosting(FileChannel fc, PostingList posting)
			throws IOException {
		/*
		 * Your code here
		 */
		Long position = fc.position();
		Integer frequency = posting.getList().size();
		
		postingDict.put(posting.getTermId(), new Pair<Long, Integer>(position , frequency));
		
		index.writePosting(fc, posting);
	}
	
	private static void writePosting(RandomAccessFile f, PostingList posting) throws IOException
	{
		// Write blocks as: 
		//	<TERM_ID><FREQUENCY><DOC_IDS>
		if (blockQueue.size() == 0)
		{
			writePosting(f.getChannel(), posting);
		}
		else
		{
			f.writeInt(posting.getTermId());
			f.writeInt(posting.getList().size());
			for (Integer docId : posting.getList())
			{
				f.writeInt(docId);
			}
		}
	}
	
	private static PostingList readPosting(RandomAccessFile f) throws IOException
	{
		if (f.getFilePointer() == f.length()) return null;
			
		Integer docId = f.readInt();
		Integer frequency = f.readInt();

		PostingList postings = new PostingList(docId);

		byte[] byteArray = new byte[frequency * Integer.SIZE / Byte.SIZE];
		
		f.read(byteArray);
		
		IntBuffer intBuf =
					ByteBuffer.wrap(byteArray)
					.order(ByteOrder.BIG_ENDIAN)
					.asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
						
		for (int i=0; i < frequency; i++)
		{
			postings.getList().add(array[i]);
//			postings.getList().add(f.readInt());
		}

		return postings;
	}
	
	private static PostingList mergePostings(PostingList postings1, PostingList postings2)
	{
		if (postings1.getTermId() != postings2.getTermId()) 
		{
			throw new RuntimeException("Un-Equal postings terms in merge: termId1: " + postings1.getTermId() 
					+ " termId2: " + postings2.getTermId());
		}
		
		if (debug)
		{
			System.out.println("postings1:");
			System.out.println(postings1);
			System.out.println("postings2:");
			System.out.println(postings2);
		}
		
		PostingList mergedPostings = new PostingList(postings1.getTermId());
		
		List<Integer> list1 = postings1.getList();
		List<Integer> list2 = postings2.getList();
		
		int i1 = 0;
		int i2 = 0;
		
		while (i1 < list1.size() || i2 < list2.size())
		{
			if (i1 < list1.size() && i2 < list2.size())
			{
				if (list1.get(i1) == list2.get(i2))
				{
					mergedPostings.getList().add(list1.get(i1));
					i1++;
					i2++;
				}
				else if (list1.get(i1) < list2.get(i2))
				{
					mergedPostings.getList().add(list1.get(i1++));
				}
				else if (list2.get(i2) < list1.get(i1))
				{
					mergedPostings.getList().add(list2.get(i2++));
				}
			}
			else if (i1 < list1.size())
			{
				mergedPostings.getList().add(list1.get(i1++));
			}
			else if (i2 < list2.size())
			{
				mergedPostings.getList().add(list2.get(i2++));
			}
		}
		
		if (debug)
		{
			System.out.println("MergedPostings:");
			System.out.println(mergedPostings);
		}
		
		return mergedPostings;
	}

	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 3) {
			System.err
					.println("Usage: java Index [Basic|VB|Gamma] data_dir output_dir");
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

		/* Get root directory */
		String root = args[1];
		File rootdir = new File(root);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + root);
			return;
		}

		/* Get output directory */
		String output = args[2];
		File outdir = new File(output);
		if (outdir.exists() && !outdir.isDirectory()) {
			System.err.println("Invalid output directory: " + output);
			return;
		}

		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				System.err.println("Create output directory failure");
				return;
			}
		}

		/* BSBI indexing algorithm */
		File[] dirlist = rootdir.listFiles();

		/* For each block */
		for (File block : dirlist) {
			File blockFile = new File(output, block.getName());
			blockQueue.add(blockFile);

			File blockDir = new File(root, block.getName());
			File[] filelist = blockDir.listFiles();
			
			Map<Integer, PostingList> blockPostingLists = new TreeMap<Integer, PostingList>();
			
			/* For each file */
			for (File file : filelist) {
				++totalFileCount;
				String fileName = block.getName() + "/" + file.getName();
				docDict.put(fileName, docIdCounter++);
				
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");
					for (String token : tokens) {
						/*
						 * Your code here
						 */
						Integer termId = termDict.get(token);
						if (null == termId)
						{
							termDict.put(token, termId=wordIdCounter++);
							if (debug)
							{
								System.out.println(token);
							}
						}
						
						PostingList termPostingList = blockPostingLists.get(termId);
						if (null == termPostingList)
						{
							termPostingList = new PostingList(termId);
							blockPostingLists.put(termId, termPostingList);
						}
						if (!termPostingList.getList().contains(docIdCounter-1))
						{
							termPostingList.getList().add(docIdCounter-1);
						}
					}
				}
				reader.close();
			}

			/* Sort and output */
			if (!blockFile.createNewFile()) {
				System.err.println("Create new block failure.");
				return;
			}
			
			RandomAccessFile bfc = new RandomAccessFile(blockFile, "rw");
			
			if (debug)
			{
				System.out.println(blockFile+":");
			}
			
			/*
			 * Your code here
			 */
			for (Map.Entry<Integer, PostingList> entry : blockPostingLists.entrySet())
			{
				// Write blocks as: 
				//	<TERM_ID><FREQUENCY><DOC_IDS>
				
				if (debug)
				{
					System.out.println(entry.getValue());
				}
				writePosting(bfc, entry.getValue());
/*				
				bfc.writeInt(entry.getKey());
				bfc.writeInt(entry.getValue().getList().size());
				for (Integer docId : entry.getValue().getList())
				{
					bfc.writeInt(docId);
				}
*/				
			}
			
			bfc.close();
		}
		
		if (debug)
		{
			System.out.println();
			System.out.println("--- Term Dictionary ---");
			System.out.println("--- Term Dictionary ---");
			System.out.println("--- Term Dictionary ---");
			System.out.println();
			
			for (Map.Entry<String, Integer> entry : termDict.entrySet())
			{
				System.out.println(entry.getValue() + " : " + entry.getKey());
			}
		}

		/* Required: output total number of files. */
		System.out.println(totalFileCount);

		/* Merge blocks */
		while (true) {
			if (blockQueue.size() <= 1)
				break;
			
			File b1 = blockQueue.removeFirst();
			File b2 = blockQueue.removeFirst();
			
			if (debug)
			{
				System.out.println();
				System.out.println("--- BlockQueue Size: " + blockQueue.size() + " ---");
				System.out.println("--- BlockQueue Size: " + blockQueue.size() + " ---");
				System.out.println("--- BlockQueue Size: " + blockQueue.size() + " ---");
				System.out.println();
			}

			File combfile = new File(output, b1.getName() + "+" + b2.getName());
			if (!combfile.createNewFile()) {
				System.err.println("Create new block failure.");
				return;
			}

			RandomAccessFile bf1 = new RandomAccessFile(b1, "r");
			RandomAccessFile bf2 = new RandomAccessFile(b2, "r");
			RandomAccessFile mf = new RandomAccessFile(combfile, "rw");
			
			/*
			 * Your code here
			 */
			PostingList postings1 = readPosting(bf1);
			PostingList postings2 = readPosting(bf2);
			
			while (null != postings1 || null != postings2)
			{
				if (null != postings1 && null != postings2)
				{
					if (postings1.getTermId() == postings2.getTermId())
					{
						// Merge postings
						PostingList mergedPostings = mergePostings(postings1, postings2);
						writePosting(mf, mergedPostings);
						postings1 = readPosting(bf1);
						postings2 = readPosting(bf2);
					}
					else if (postings1.getTermId() < postings2.getTermId())
					{
						writePosting(mf, postings1);
						postings1 = readPosting(bf1);
					}
					else if (postings2.getTermId() < postings1.getTermId())
					{
						writePosting(mf, postings2);
						postings2 = readPosting(bf2);
					}
				}
				else if (null != postings1)
				{
					writePosting(mf, postings1);
					postings1 = readPosting(bf1);
				}
				else if (null != postings2)
				{
					writePosting(mf, postings2);
					postings2 = readPosting(bf2);
				}
			}
			
/*			

			Integer termFreq1;
			Integer termFreq2;
			
			Integer termId1 = bf1.readInt();
			Integer termId2 = bf2.readInt();
			
			while (termId1 != null || termId2 != null)
			{
				if (termId1 < termId2 || termId2 == null)
				{
					termFreq1 = bf1.readInt();

					mf.writeInt(termId1);
					mf.writeInt(termFreq1);
					for (int i=0; i<termFreq1; i++)
					{
						mf.writeInt(bf1.readInt());
					}
					
					termId1 = bf1.readInt();
				}
				else if (termId2 < termId1 || termId1 == null)
				{
					termFreq2 = bf2.readInt();
					
					mf.writeInt(termId2);
					mf.writeInt(termFreq2);
					for (int i=0; i<termFreq2; i++)
					{
						mf.writeInt(bf2.readInt());
					}
					
					termId2 = bf2.readInt();
				}
				else // termId1 == termId2
				{
					termFreq1 = bf1.readInt();
					termFreq2 = bf2.readInt();
					
					Integer termFreq = 0;
	
					Integer i1 = 0;
					Integer i2 = 0;
	
					Integer docId1 = bf1.readInt();
					Integer docId2 = bf2.readInt();
					
					ArrayList<Integer> docIds = new ArrayList<Integer>();
	
					while (i1 < termFreq1 || i2 < termFreq2)
					{
						
						if (i1 < termFreq1 && (docId1 < docId2 || i2 == termFreq2))
						{
							docIds.add(docId1);
							i1++;
							
							if (i1 < termFreq1) docId1 = bf1.readInt();
						}
						else if (i2 < termFreq2 && (docId2 < docId1 || i1 == termFreq1))
						{
							docIds.add(docId2);
							i2++;

							if (i2 < termFreq2) docId2 = bf2.readInt();
						}
						else
						{
							// docId1 == docId2
							docIds.add(docId1);
							i1++;
							i2++;

							if (i1 < termFreq1) docId1 = bf1.readInt();
							if (i2 < termFreq2) docId2 = bf2.readInt();
						}
						termFreq++;
					}
					
					mf.writeInt(termId1);
					mf.writeInt(termFreq);
					for (Integer docId : docIds)
					{
						mf.writeInt(docId);
					}
					
					termId1 = bf1.readInt();
					termId2 = bf2.readInt();
				}
			}
*/			
			bf1.close();
			bf2.close();
			mf.close();
			b1.delete();
			b2.delete();
			blockQueue.add(combfile);
		}

		/* Dump constructed index back into file system */
		File indexFile = blockQueue.removeFirst();
		indexFile.renameTo(new File(output, "corpus.index"));

		BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File(
				output, "term.dict")));
		for (String term : termDict.keySet()) {
			termWriter.write(term + "\t" + termDict.get(term) + "\n");
		}
		termWriter.close();

		BufferedWriter docWriter = new BufferedWriter(new FileWriter(new File(
				output, "doc.dict")));
		for (String doc : docDict.keySet()) {
			docWriter.write(doc + "\t" + docDict.get(doc) + "\n");
		}
		docWriter.close();

		BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File(
				output, "posting.dict")));
		for (Integer termId : postingDict.keySet()) {
			postWriter.write(termId + "\t" + postingDict.get(termId).getFirst()
					+ "\t" + postingDict.get(termId).getSecond() + "\n");
		}
		postWriter.close();
	}

}
