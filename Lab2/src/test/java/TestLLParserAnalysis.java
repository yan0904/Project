import org.example.LLParserAnalysis;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class TestLLParserAnalysis {
    private LLParserAnalysis parser = new LLParserAnalysis();
    boolean isjoint = false;

    /**
     * 生成预测分析表
     */
    @Before
    public void start(){
        parser.getParsingTable();
    }

    /**
     * 判断文法是否为LL文法
     */
    @Test
    public void LLGrammartest(){
        for(String key : parser.first.keySet()){
            Set<String> first = parser.first.get(key);
            if(first.contains("E")){
                Set<String> follow = parser.follow.get(key);
                if(follow == null){
                    continue;
                }
                isjoint = Collections.disjoint(first,follow);
                Assert.assertTrue(isjoint);
            }
        }
    }

}
