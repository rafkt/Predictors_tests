package ca.ipredict.predictor.CPT.CPT_Approx;

import java.util.*;


public class LevenshteinDistance{


	public static final double distance(final ArrayList<Integer> s1, final ArrayList<Integer> s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        boolean flag = true;
        for(int itemList1 : s1)
        {
            if(!s2.contains(itemList1)) flag = false;
        }
        if (flag) return 0;

        if (s1.size() == 0) {
            return s2.size();
        }

        if (s2.size() == 0) {
            return s1.size();
        }

        // create two work vectors of integer distances
        int[] v0 = new int[s2.size() + 1];
        int[] v1 = new int[s2.size() + 1];
        int[] vtemp;

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s1.size(); i++) {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            // use formula to fill in the rest of the row
            for (int j = 0; j < s2.size(); j++) {
                int cost = 1;
                if (s1.get(i) == s2.get(j)) {
                    cost = 0;
                }
                v1[j + 1] = Math.min(
                        v1[j] + 1,              // Cost of insertion
                        Math.min(
                                v0[j + 1] + 1,  // Cost of remove
                                v0[j] + cost)); // Cost of substitution
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);

            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }

        //normalise


        int m_len = Math.max(s1.size(), s2.size());

        if (m_len == 0) {
            return 0;
        }

        return v0[s2.size()] / m_len;

        //return v0[s2.size()];
    }

   //  public static void main (String[] args){

   //  		Sequence s1 = new Sequence(0);
   //  		Sequence s2 = new Sequence(0);

			// s1.add(10);
			// s1.add(20);
			// s1.add(30);
			// s1.add(40);

			// s2.add(10);
			// s2.add(20);
			// s2.add(30);
			// s2.add(40);


	  //       System.out.println(distance(s1, s2));
	  //       System.out.println(l.distance("My string", "My $tring"));
	  //       System.out.println(l.distance("My string", "My $tring"));
   //  }



}