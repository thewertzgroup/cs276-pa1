package cs276.assignments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

public class BasicIndex implements BaseIndex {

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
		ByteBuffer buf = ByteBuffer.allocate(p.getList().size() * Integer.SIZE / Byte.SIZE);
		for (int i=0; i < p.getList().size(); i++)
		{
			buf.putInt(p.getList().get(i));
		}
		buf.flip();
		fc.write(buf);
	}
}
