package cs276.assignments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;

public class GammaIndex implements BaseIndex {
	
	  /**
	   * Encodes a number using unary code.  The unary code for the number is placed in the BitSet
	   * outputUnaryCode starting at index startIndex.  The method returns the BitSet index that
	   * immediately follows the end of the unary encoding.  Use startIndex = 0 to place the unary
	   * encoding at the beginning of the outputUnaryCode.
	   * <p/>
	   * Examples:
	   * If number = 5, startIndex = 3, then unary code 111110 is placed in outputUnaryCode starting
	   * at the 4th bit position and the return value 9.
	   *
	   * @param number          The number to be unary encoded
	   * @param outputUnaryCode The unary code for number is placed into this BitSet
	   * @param startIndex      The unary code for number starts at this index position in outputUnaryCode
	   * @return The next index position in outputUnaryCode immediately following the unary code for number
	   */
	  private int unaryEncodeInteger(int number, BitSet outputUnaryCode, int startIndex) {
	    int nextIndex = startIndex;
	    // TODO: Fill in your code here
	    
	    while (nextIndex < startIndex+number)
	    {
	    	outputUnaryCode.set(nextIndex++, true);
	    }

	    outputUnaryCode.set(nextIndex++, false);    

	    return nextIndex; //++nextIndex;
	  }


	  /**
	   * Decodes the unary coded number in BitSet inputUnaryCode starting at (0-based) index startIndex.
	   * The decoded number is returned in numberEndIndex[0] and the index position immediately following
	   * the encoded value in inputUnaryCode is returned in numberEndIndex[1].
	   *
	   * @param inputUnaryCode BitSet containing the unary code
	   * @param startIndex     Unary code starts at this index position
	   * @param numberEndIndex Return values: index 0 holds the decoded number; index 1 holds the index
	   *                       position in inputUnaryCode immediately following the unary code.
	   */
	  private void unaryDecodeInteger(BitSet inputUnaryCode, int startIndex, int[] numberEndIndex) {
		// TODO: Fill in your code here
		int number = 0;
		while (inputUnaryCode.get(startIndex++))
		{
			number++;
		}
		numberEndIndex[0] = number;
		numberEndIndex[1] = startIndex;
	  }

	  
	  /**
	   * Gamma encodes number.  The encoded bits are placed in BitSet outputGammaCode starting at
	   * (0-based) index position startIndex.  Returns the index position immediately following the
	   * encoded bits.  If you try to gamma encode 0, then the return value should be startIndex (i.e.,
	   * it does nothing).
	   *
	   * @param number          Number to be gamma encoded
	   * @param outputGammaCode Gamma encoded bits are placed in this BitSet starting at startIndex
	   * @param startIndex      Encoded bits start at this index position in outputGammaCode
	   * @return Index position in outputGammaCode immediately following the encoded bits
	   */
	  public int gammaEncodeInteger(int number, BitSet outputGammaCode, int startIndex) {
	    int nextIndex = startIndex;
	    // TODO: Fill in your code here
	    
	    BitSet tmpBitSet = new BitSet();

	    int lastSet = startIndex;
	    while (number != 0)
	    {
	    	if (number % 2 != 0)
	    	{
	    		tmpBitSet.set(nextIndex,true);
	    		lastSet = nextIndex;
	    	}
	    	else tmpBitSet.set(nextIndex,false);
	    	++nextIndex;
	    	number = number >>> 1;
	    }

	    tmpBitSet.set(lastSet, false);
	    
	    int length = lastSet - startIndex;
	    
	    nextIndex = unaryEncodeInteger(length, tmpBitSet, nextIndex)-1;
	    
	    for (int i=startIndex; i < nextIndex; i++)
	    {
	    	outputGammaCode.set(i, tmpBitSet.get(nextIndex-i-1));
	    }
	    
	    return nextIndex;
	  }


	  /**
	   * Decodes the Gamma encoded number in BitSet inputGammaCode starting at (0-based) index startIndex.
	   * The decoded number is returned in numberEndIndex[0] and the index position immediately following
	   * the encoded value in inputGammaCode is returned in numberEndIndex[1].
	   *
	   * @param inputGammaCode BitSet containing the gamma code
	   * @param startIndex     Gamma code starts at this index position
	   * @param numberEndIndex Return values: index 0 holds the decoded number; index 1 holds the index
	   *                       position in inputGammaCode immediately following the gamma code.
	   */
	  private void gammaDecodeInteger(BitSet inputGammaCode, int startIndex, int[] numberEndIndex) {
	    // TODO: Fill in your code here
		  unaryDecodeInteger(inputGammaCode, startIndex, numberEndIndex);
		  
		  int length = numberEndIndex[0];
		  int nextIndex = numberEndIndex[1];
		  
		  int number = length == 0 ? 0 : 1;
		  for (int i=0; i<length; i++)
		  {
			  number <<= 1;
			  if (inputGammaCode.get(nextIndex++))
				  number |= 1;
				  
		  }
		  
		  numberEndIndex[0] = number;
		  numberEndIndex[1] = nextIndex;
	  }


	@Override
	public PostingList readPosting(FileChannel fc, Integer termId, Long position, Integer frequency) throws IOException {
		/*
		 * Your code here
		 */
		ByteBuffer buf = ByteBuffer.allocate(frequency * Integer.SIZE / Byte.SIZE);
		fc.position(position);
		fc.read(buf);
		buf.rewind();
		
		IntBuffer intBuf = buf.order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
	
		PostingList postings = new PostingList(termId);
		for (int i=0; i < frequency; i++)
		{
			postings.getList().add(array[i]);
		}

		return postings;
	}

	
	@Override
	public void writePosting(FileChannel fc, PostingList p) throws IOException {
		/*
		 * Your code here
		 */
		
		int startIndex = fc.position();
		ByteBuffer buf = ByteBuffer.allocate(p.getList().size() * Integer.SIZE / Byte.SIZE);
		for (int i=0; i < p.getList().size(); i++)
		{
			buf.putInt(p.getList().get(i));
		}
		buf.flip();
		fc.write(buf);
	}

}
