package cs276.assignments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BasicIndex implements BaseIndex {

	@Override
	public PostingList readPosting(FileChannel fc) {
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
		ByteBuffer buf = ByteBuffer.allocate(p.getList().size() * Integer.SIZE / Byte.SIZE);
		for (int i=0; i < p.getList().size(); i++)
		{
			buf.putInt(p.getList().get(i));
		}
		buf.flip();
		fc.write(buf);
	}
}
