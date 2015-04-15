package cs276.assignments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class VBIndex implements BaseIndex {
	
	private static boolean debug = true;

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
