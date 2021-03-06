import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christopher Manning
 */
public class PositionalIntersect {

  static boolean DEBUG = false;

  static String[][] intersectTestCases = {
      { "1:7,18,33,72,86,231; 2:1,17,74,222,255; 4:8,16,190,429,433; 5:363,367; 7:13,23,191; 13:28",
        "1:17,25; 4:17,191,291,430,434; 5:14,19,101; 6:19; 8:42; 10:11; 13:24",
        "[(1,18,17), (4,16,17), (4,190,191), (4,429,430), (4,429,434), (4,433,430), (4,433,434), (13,28,24)]" },
      { "1:11,35,77,98,104; 5:100",
        "1:21,92,93,94,95,97,99,100,101,102,103,105,106,107,108,109,110; 5:94,95",
        "[(1,98,93), (1,98,94), (1,98,95), (1,98,97), (1,98,99), (1,98,100), (1,98,101), (1,98,102), (1,98,103), (1,104,99), (1,104,100), (1,104,101), (1,104,102), (1,104,103), (1,104,105), (1,104,106), (1,104,107), (1,104,108), (1,104,109), (5,100,95)]" },
      { "1:1,2,3,4,5,6,7",
        "1:1,2,3,4,5,6,7",
        "[(1,1,1), (1,1,2), (1,1,3), (1,1,4), (1,1,5), (1,1,6), (1,2,1), (1,2,2), (1,2,3), (1,2,4), (1,2,5), (1,2,6), (1,2,7), (1,3,1), (1,3,2), (1,3,3), (1,3,4), (1,3,5), (1,3,6), (1,3,7), (1,4,1), (1,4,2), (1,4,3), (1,4,4), (1,4,5), (1,4,6), (1,4,7), (1,5,1), (1,5,2), (1,5,3), (1,5,4), (1,5,5), (1,5,6), (1,5,7), (1,6,1), (1,6,2), (1,6,3), (1,6,4), (1,6,5), (1,6,6), (1,6,7), (1,7,2), (1,7,3), (1,7,4), (1,7,5), (1,7,6), (1,7,7)]" },
      { "1:11,92; 17:6,16; 21:103,113,114",
        "4:8; 5:2; 17:11; 21:3, 97,108",
        "[(17,6,11), (17,16,11), (21,103,108), (21,113,108)]" },
      { "5:4; 11:7,18; 12:1,17; 14:8,16; 15:363,367; 7:13,23,191; 103:28",
        "3:2; 8:9; 11:17,25; 14:17,434; 15:101; 16:19; 18:42; 100:11; 103:24; 109:11",
        "[(11,18,17), (14,16,17), (103,28,24)]" },
      { "1:1; 5:1; 11:1; 13:1; 19:1; 43:1",
        "2:1; 3:1; 5:1; 9:1; 11:1; 15:1; 19:1; 33:1; 45:1",
        "[(5,1,1), (11,1,1), (19,1,1)]" },
      { "1:1",
        "2:1; 189:10",
        "[]" },
  };

  /** Stores an individual posting (positions of token in a single document. */
  static class Posting {
    int docID;
    List<Integer> positions;
    public Posting(int docID, List<Integer> positions) {
      this.docID = docID;
      this.positions = positions;
    }
    public Iterator<Integer> positions() { return positions.iterator(); }
    public String toString() {
      return docID + ":" + positions;
    }
  }

  /* Stores a single positional answer: a document and the token position of each word. */
  static class AnswerElement {
    int docID;
    int p1pos;
    int p2pos;

    AnswerElement(int docID, int p1pos, int p2pos) {
      this.docID = docID;
      this.p1pos = p1pos;
      this.p2pos = p2pos;
    }

    public String toString() {
      return "(" + docID + "," + p1pos + "," + p2pos + ")";
    }
  }


  /** Find proximity matches where the two words are within k words in the two postings lists.
   *  Returns a list of (document, position_of_p1_word, position_of_p2_word) items.
   */
  static List<AnswerElement> positionalIntersect(Iterator<Posting> p1, Iterator<Posting> p2, int k) {
    List<AnswerElement> answer = new ArrayList<AnswerElement>();
    Posting p1posting = popNextOrNull(p1);
    Posting p2posting = popNextOrNull(p2);
    while (p1posting != null && p2posting != null) {
      if (DEBUG) System.err.println("Working on docs " + p1posting.docID + " and " + p2posting.docID);
      if (p1posting.docID == p2posting.docID) {
        // look for positional matches satisfying within k
        List<Integer> l = new ArrayList<Integer>(); // This maintains a local buffer of positions in pp2
        Iterator<Integer> pp1 = p1posting.positions();
        Iterator<Integer> pp2 = p2posting.positions();
        Integer pp1offset = popNextOrNull(pp1);
        Integer pp2offset = popNextOrNull(pp2);
        // Go through everything in pp1 in turn
        while (pp1offset != null) {
          if (DEBUG) System.err.println("Working on position " + pp1offset + " in 1st list; l is " + l);
          // Move forward on pp2 until exceed far end of window from pp1, accumulating answers in l.
          while (pp2offset != null) {
            if (DEBUG) System.err.println("Working on position " + pp2offset + " in 2nd list");
            if (Math.abs(pp1offset - pp2offset) <= k) {
              l.add(pp2offset);
            } else if (pp2offset > pp1offset) {
                break;
            }
            pp2offset = popNextOrNull(pp2);
          }
          // Remove old answers in l which are now too early in the document
          while ( ! l.isEmpty() && Math.abs(l.get(0) - pp1offset) > k) {
            l.remove(0);
          }
          // The remaining things in l are good matches with the current pp1
          for (Integer ps : l) {
            AnswerElement ae = new AnswerElement(p1posting.docID,
                                                 pp1offset, ps);
            if (DEBUG) System.err.println("Found answer: " + ae);
            answer.add(ae);
          }
          pp1offset = popNextOrNull(pp1);
        } // end while pp1 loop
        p1posting = popNextOrNull(p1);
        p2posting = popNextOrNull(p2);
        if (DEBUG) System.err.println("Popping both, now p1 is " + p1posting + " and p2 is " + p2posting);
      } else if (p1posting.docID < p2posting.docID) {
        p1posting = popNextOrNull(p1);
      } else {
        p2posting = popNextOrNull(p2);
      }
    }
    return answer;
  }


  static <X> X popNextOrNull(Iterator<X> p) {
    if (p.hasNext()) {
      return p.next();
    } else {
      return null;
    }
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
        List<AnswerElement> ans = positionalIntersect(pl1, pl2, 5);
        System.out.println("Answer:         " + ans);
        if ( ! ans.toString().equals(test[2])) {
          System.out.println("Should be:      " + test[2]);
          System.out.println("*** ERROR ***");
        }
        System.out.println();
      }
    } else if (args.length != 2) {
      System.err.println("Usage: java Intersect postingsList1 postingsList2");
      System.err.println("       postingsList format: '1:17,25; 4:17,191,291,430,434; 5:14,19,10'");
    } else {
      Iterator<Posting> pl1 = loadPostingsList(args[0]);
      Iterator<Posting> pl2 = loadPostingsList(args[1]);
      List<AnswerElement> ans = positionalIntersect(pl1, pl2, 5);
      System.out.println(ans);
    }
  }

}