package cs276.assignments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class VBIndex implements BaseIndex {
	
	private static boolean debug = true;
	
	  /**
	   * Gap encodes a postings list.  The DocIds in the postings list are provided in the array
	   * inputDocIdsOutputGaps.  The output gaps are placed right back into this array, replacing
	   * each docId with the corresponding gap.
	   * <p/>
	   * Example:
	   * If inputDocIdsOutputGaps is initially {5, 1000, 1005, 1100}
	   * then at the end inputDocIdsOutputGaps is set to {5, 995, 5, 95}
	   *
	   * @param inputDocIdsOutputGaps The array of input docIds.
	   *                              The output gaps are placed back into this array!
	   */
	  private static void gapEncode(int[] inputDocIdsOutputGaps) {
	    // TODO: Fill in your code here

		  for (int i=inputDocIdsOutputGaps.length-1; i > 0; i--)
		  {
			  inputDocIdsOutputGaps[i] = inputDocIdsOutputGaps[i] - inputDocIdsOutputGaps[i-1];
		  }
	  }


	  /**
	   * Decodes a gap encoded postings list into the corresponding docIds.  The input gaps are provided
	   * in inputGapsOutputDocIds.  The output docIds are placed right back into this array, replacing
	   * each gap with the corresponding docId.
	   * <p/>
	   * Example:
	   * If inputGapsOutputDocIds is initially {5, 905, 5, 95}
	   * then at the end inputGapsOutputDocIds is set to {5, 1000, 1005, 1100}
	   *
	   * @param inputGapsOutputDocIds The array of input gaps.
	   *                              The output docIds are placed back into this array.
	   */
	  private static void gapDecode(int[] inputGapsOutputDocIds) {
	    // TODO: Fill in your code here
		  for (int i=1; i < inputGapsOutputDocIds.length; i++)
		  {
			  inputGapsOutputDocIds[i] = inputGapsOutputDocIds[i-1] + inputGapsOutputDocIds[i];
		  }
	  }

	  /**
	   * Encodes gap using a VB code.  The encoded bytes are placed in outputVBCode.  Returns the number
	   * bytes placed in outputVBCode.
	   *
	   * @param gap          gap to be encoded.  Assumed to be greater than or equal to 0.
	   * @param outputVBCode VB encoded bytes are placed here.  This byte array is assumed to be large
	   *                     enough to hold the VB code for gap (e.g., Integer.SIZE/7 + 1).
	   * @return Number of bytes placed in outputVBCode.
	   */
	  private static int VBEncodeInteger(int gap, byte[] outputVBCode) {
	    int numBytes = 0;
	    // TODO: Fill in your code here    
		for (int i=0; i<outputVBCode.length; i++) outputVBCode[i] = 0;
		
		if (gap==0)
		{
	    	outputVBCode[0] += 128;
	    	return 1;
		}

	    numBytes = (int)(Math.log(gap) / Math.log(128)) + 1;
	    
	    int i = numBytes - 1;
	    do
	    {
	    	outputVBCode[i--] = (byte)(gap % 128);
	    	gap /= 128;
	    } while (i >= 0);
	    
	    outputVBCode[numBytes-1] += 128;
	    
	    return numBytes;
	  }


	  /**
	   * Decodes the first integer encoded in inputVBCode starting at index startIndex.  The decoded
	   * number is placed in the element zero of the numberEndIndex array and the index position
	   * immediately after the encoded value is placed in element one of the numberEndIndex array.
	   *
	   * @param inputVBCode    Byte array containing the VB encoded number starting at index startIndex.
	   * @param startIndex     Index in inputVBCode where the VB encoded number starts
	   * @param numberEndIndex Outputs are placed in this array.  The first element is set to the
	   *                       decoded number and the second element is set to the index of inputVBCode
	   *                       immediately after the end of the VB encoded number.
	   * @throws IllegalArgumentException If not a valid variable byte code
	   */
	  private static void VBDecodeInteger(byte[] inputVBCode, int startIndex, int[] numberEndIndex) {
	    // TODO: Fill in your code here
		  int gap = 0;
		  
		  while (true)
		  {
			  if (startIndex >= inputVBCode.length) throw new IllegalArgumentException("Not a valid variable byte code: " + inputVBCode);
				  
			  if ((inputVBCode[startIndex] & 0xff) < 128)
			  {
				  gap = 128 * gap + inputVBCode[startIndex++];
			  }
			  else
			  {
				  gap = (128 * gap + ((inputVBCode[startIndex++] - 128) & 0xff));
				  break;
			  }
		  }
			  	  
		  numberEndIndex[0] = gap;
		  numberEndIndex[1] = startIndex;
	  }


	@Override
	public PostingList readPosting(FileChannel fc, Integer termId, Long position, Integer frequency) {
		/*
		 * Your code here
		 */
		return null;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p) throws IOException {
		/*
		 * Your code here
		 */
		Integer last = 0;
		for (Integer current : p.getList())
		{
			Integer gap = current - last;
			if (debug) System.out.println();
		}
		ByteBuffer buf = ByteBuffer.allocate(p.getList().size() * Integer.SIZE / Byte.SIZE);
		for (int i=0; i < p.getList().size(); i++)
		{
			buf.putInt(p.getList().get(i));
		}
		buf.flip();
		fc.write(buf);

	}
}
