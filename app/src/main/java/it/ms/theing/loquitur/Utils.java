/*
    Loquitur, Utilities

    Copyright (C) 2015 by TheIng
    http://github.com/theing/Loquitur

    This file is part of Loquitur.

    Loquitur is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package it.ms.theing.loquitur;

public class Utils {


    private static int minimum(int a,int b,int c) {
        int d=((a<b) ? a : b);
        return ((d<c) ? d : c);
    }

    public static int score(String s1, String s2)
    {

        int m=s1.length()+1;
        int n=s2.length()+1;

        int d[][]=new int[m+1][n+1];

        for (int i=0;i<m;++i)
        {
            for (int j=0;j<n;++j)
            {
                d[i][j]=0;
            }
        }


        for (int i=1;i<m;++i)
        {
            d[i][0] = i;
        }

        for (int j=1;j<n;++j)
        {
            d[0][j] = j;
        }


        for (int j=1;j<n;++j)
        {
            for(int i=1;i<m;++i)
            {
                if (s1.charAt(i-1) == s2.charAt(j-1))
                    d[i][j] = d[i-1][j-1];       // no operation required
                else
                    d[i][j] = minimum
                            (
                                    d[i-1][j] + 1,  // a deletion
                                    d[i][j-1] + 1,  // an insertion
                                    d[i-1][j-1] + 1 // a substitution
                            );
            }
        }

        return d[m-1][n-1];
    }

    /**
     * Calculate the normalized Levenshtein distance between two strings
     * @param s1
     * String to compare
     * @param s2
     * Reference string
     * @return
     * The score between 0 and 1
     */

    public static float match(String s1, String s2) {
        try {
            float score = score(s1, s2);
            score = 1 - (score / ((float) s2.length()));
            if (score < 0) return 0;
            return score;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void safe(Exception e) {
        // Do nothing
    }


}
