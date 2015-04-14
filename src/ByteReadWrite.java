import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;


public class ByteReadWrite 
{
	
	private static final int INT_BYTES = Integer.SIZE / Byte.SIZE;
	
	public static void main(String[] args) throws IOException
	{
		File f = new File("test.txt");
		if (!f.exists())
		{
			f.createNewFile();
		}
		
		RandomAccessFile rwf = new RandomAccessFile(f, "rw");
		for (int i=0; i < 5; i++)
		{
			ByteBuffer buf = ByteBuffer.allocate(INT_BYTES);
			buf.putInt(i);
			buf.flip();
			rwf.getChannel().write(buf);
		}
		rwf.close();
		
		RandomAccessFile rf = new RandomAccessFile(f, "r");
		ByteBuffer buf = ByteBuffer.allocate(INT_BYTES * 5);
		rf.getChannel().read(buf);
		buf.rewind();
		for (int i=0; i < 5; i++)
		{
			System.out.println("Read back " + buf.getInt());
		}
		rf.close();
	}

}
