package com.zeen.plagiarismchecker.impl;

import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import static com.google.common.base.Preconditions.checkNotNull;

public class StopWordUtil {
    private StopWordUtil() {
    }

    public static boolean isStopWord(CharSequence word) {
        checkNotNull(word, "word");
        return STOP_WORDS.contains(word);
    }

    private static Set<CharSequence> STOP_WORDS = Sets
            .newHashSet(Splitter
                    .on(',')
                    .split("a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your"));
}
