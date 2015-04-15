package cs276.assignments;

import java.io.IOException;
import java.nio.channels.FileChannel;

public interface BaseIndex {
	
	public PostingList readPosting (FileChannel fc, Integer termId, Long position, Integer frequency) throws IOException;
	
	public void writePosting (FileChannel fc, PostingList p) throws IOException;
}
