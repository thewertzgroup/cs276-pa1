import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christopher Manning
 */
public class Intersect {

  static boolean DEBUG = true;

  static String[][] intersectTestCases = {
      { "1:7,18,33,72,86,231; 2:1,17,74,222,255; 4:8,16,190,429,433; 5:363,367; 7:13,23,191; 13:28",
        "1:17,25; 4:17,191,291,430,434; 5:14,19,101; 6:19; 8:42; 10:11; 13:24",
        "[1, 4, 5, 13]" },
      { "1:11,35,77,98,104; 5:100",
        "1:21,92,93,94,95,97,99,100,101,102,103,105,106,107,108,109,110, 5:94,95",
        "[1]" },
      { "1:1,2,3,4,5,6,7",
        "1:1,2,3,4,5,6,7",
        "[1]" },
      { "1:11,92; 17:6,16; 21:103,113,114",
        "4:8; 5:2; 17:11; 21:3, 97,108",
        "[17, 21]" },
      { "5:4; 11:7,18; 12:1,17; 14:8,16; 15:363,367; 7:13,23,191; 103:28",
        "3:2; 8:9; 11:17,25; 14:17,434; 15:101; 16:19; 18:42; 100:11; 103:24; 109:11",
        "[11, 14, 15, 103]" },
      { "1:1; 5:1; 11:1; 13:1; 19:1; 43:1",
        "2:1; 3:1; 5:1; 9:1; 11:1; 15:1; 19:1; 33:1; 45:1",
        "[5, 11, 19]" },
      { "1:1",
        "2:1; 189:10",
        "[]" },
  };


  static class Posting {
    int docID;
    List<Integer> positions;
    public Posting(int docID, List<Integer> positions) {
      this.docID = docID;
      this.positions = positions;
    }
    public String toString() {
      return docID + ":" + positions;
    }
  }

  static Posting popNextOrNull(Iterator<Posting> p) {
    if (p.hasNext()) {
      return p.next();
    } else {
      return null;
    }
  }

  static List<Integer> intersect(Iterator<Posting> p1, Iterator<Posting> p2) {
    List<Integer> answer = new ArrayList<Integer>();
    Posting pp1 = popNextOrNull(p1);
    Posting pp2 = popNextOrNull(p2);

    // Your code starts here


    // Your code ends here

    return answer;
  }

  static Iterator<Posting> loadPostingsList(String list) {
    List<Posting> postingsList = new ArrayList<Posting>();
    String[] psts = list.split(";");
    for (String pst : psts) {
      String[] bits = pst.split(":");
      String[] poses = bits[1].split(",");
      int docID = Integer.valueOf(bits[0].trim());
      List<Integer> positions = new ArrayList<Integer>();
      for (String pos : poses) {
        positions.add(Integer.valueOf(pos.trim()));
      }
      Posting post = new Posting(docID, positions);
      postingsList.add(post);
    }
    if (DEBUG) {
      System.err.println("Loaded postings list: " + postingsList);
    }
    return postingsList.iterator();
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      for (String[] test : intersectTestCases) {
        Iterator<Posting> pl1 = loadPostingsList(test[0]);
        Iterator<Posting> pl2 = loadPostingsList(test[1]);
        System.out.println("Intersection of " + test[0]);
        System.out.println("            and " + test[1] + ": ");
        List<Integer> ans = intersect(pl1, pl2);
        System.out.println("Answer:         " + ans);
        if ( ! ans.toString().equals(test[2])) {
          System.out.println("Should be:      " + test[2]);
          System.out.println("*** ERROR ***");
        }
      }
    } else if (args.length != 2) {
      System.err.println("Usage: java Intersect postingsList1 postingsList2");
      System.err.println("       postingsList format: '1:17,25; 4:17,191,291,430,434; 5:14,19,10'");
    } else {
      Iterator<Posting> pl1 = loadPostingsList(args[0]);
      Iterator<Posting> pl2 = loadPostingsList(args[1]);
      List<Integer> ans = intersect(pl1, pl2);
      System.out.println(ans);
    }
  }

}