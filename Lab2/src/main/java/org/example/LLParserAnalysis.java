package org.example;

import java.util.*;

@SuppressWarnings("unchecked")
public class LLParserAnalysis {
    /**
     * 输入的token队列
     */
    private static List<String> inputQueue = new LinkedList();

    /**
     * 语法分析栈
     */
    private static Stack<String> analysisStack = new Stack<>();

    /**
     * 终结符
     */
    private static  Set<String> terminals = new HashSet<>();
    static {
        terminals.add("{");
        terminals.add("}");
        terminals.add("if");
        terminals.add("(");
        terminals.add(")");
        terminals.add("then");
        terminals.add("else");
        terminals.add("while");
        terminals.add("ID");
        terminals.add("=");
        terminals.add(">");
        terminals.add("<");
        terminals.add(">=");
        terminals.add("<=");
        terminals.add("==");
        terminals.add("+");
        terminals.add("-");
        terminals.add("*");
        terminals.add("/");
        terminals.add("NUM");
        terminals.add("E");
        terminals.add(";");
        terminals.add("$");
        }

    /**
     * 非终结符
     */
    @SuppressWarnings( " unchecked " )
    private static  Set<String> noneterminals = new HashSet<>();
    static {
        noneterminals.add("program");
        noneterminals.add("stmt");
        noneterminals.add("compoundstmt");
        noneterminals.add("stmts");
        noneterminals.add("ifstmt");
        noneterminals.add("whilestmt");
        noneterminals.add("assgstmt");
        noneterminals.add("boolexpr");
        noneterminals.add("boolop");
        noneterminals.add("arithexpr");
        noneterminals.add("arithexprprime");
        noneterminals.add("multexpr");
        noneterminals.add("multexprprime");
        noneterminals.add("simpleexpr");
    }

    /**
     * 产生式
     */
    private static HashMap<String, ArrayList<String>> productions = new HashMap();
    static {
        productions.put("program", new ArrayList(Arrays.asList("compoundstmt")));
        productions.put("stmt", new ArrayList(Arrays.asList("ifstmt","whilestmt","assgstmt","compoundstmt")));
        productions.put("compoundstmt", new ArrayList(Arrays.asList("{ stmts }")));
        productions.put("stmts", new ArrayList(Arrays.asList("stmt stmts","E")));
        productions.put("ifstmt", new ArrayList(Arrays.asList("if ( boolexpr ) then stmt else stmt")));
        productions.put("whilestmt", new ArrayList(Arrays.asList("while ( boolexpr ) stmt")));
        productions.put("assgstmt", new ArrayList(Arrays.asList("ID = arithexpr ;")));
        productions.put("boolexpr", new ArrayList(Arrays.asList("arithexpr boolop arithexpr")));
        productions.put("boolop", new ArrayList(Arrays.asList("<",">","<=",">=","==")));
        productions.put("arithexpr", new ArrayList(Arrays.asList("multexpr arithexprprime")));
        productions.put("arithexprprime", new ArrayList(Arrays.asList("+ multexpr arithexprprime","- multexpr arithexprprime","E")));
        productions.put("multexpr", new ArrayList(Arrays.asList("simpleexpr multexprprime")));
        productions.put("multexprprime", new ArrayList(Arrays.asList("* simpleexpr multexprprime","/ simpleexpr multexprprime","E")));
        productions.put("simpleexpr", new ArrayList(Arrays.asList("ID","NUM","( arithexpr )")));
    }

    /**
     * First集
     */
    public static HashMap<String,Set<String>> first = new HashMap<>();

    /**
     * 过滤之后的First集
     */
    private static HashMap<String,Set<String>> firstFiltered = new HashMap<>();

    /**
     * Follow集
     */
    public static HashMap<String,Set<String>> follow = new HashMap<>();
    static{
        follow.put("program",new HashSet<>(Arrays.asList("$")));
        follow.put("stmt",new HashSet<>());
        follow.put("compoundstmt",new HashSet<>());
        follow.put("stmts",new HashSet<>());
        follow.put("ifstmt",new HashSet<>());
        follow.put("whilestmt",new HashSet<>());
        follow.put("assgstmt",new HashSet<>());
        follow.put("boolexpr",new HashSet<>());
        follow.put("boolop",new HashSet<>());
        follow.put("arithexpr",new HashSet<>());
        follow.put("arithexprprime",new HashSet<>());
        follow.put("multexpr",new HashSet<>());
        follow.put("multexprprime",new HashSet<>());
        follow.put("simpleexpr",new HashSet<>());
    }

    /**
     * 用于建立非终结符和数字之间的映射，用于构造parsingTable
     */
    private static HashMap<String,Integer> noneterminal2num = new HashMap<>();
    static {
        noneterminal2num.put("program",0);
        noneterminal2num.put("stmt",1);
        noneterminal2num.put("compoundstmt",2);
        noneterminal2num.put("stmts",3);
        noneterminal2num.put("ifstmt",4);
        noneterminal2num.put("whilestmt",5);
        noneterminal2num.put("assgstmt",6);
        noneterminal2num.put("boolexpr",7);
        noneterminal2num.put("boolop",8);
        noneterminal2num.put("arithexpr",9);
        noneterminal2num.put("arithexprprime",10);
        noneterminal2num.put("multexpr",11);
        noneterminal2num.put("multexprprime",12);
        noneterminal2num.put("simpleexpr",13);
    }

    /**
     * 用于建立终结符和数字之间的映射，用于构造parsingTable
     */
    private static HashMap<String,Integer> terminal2num = new HashMap<>();
    static {
        terminal2num.put("$",0);
        terminal2num.put("{",1);
        terminal2num.put("}",2);
        terminal2num.put("if",3);
        terminal2num.put("(",4);
        terminal2num.put(")",5);
        terminal2num.put("then",6);
        terminal2num.put("else",7);
        terminal2num.put("while",8);
        terminal2num.put("ID",9);
        terminal2num.put("=",10);
        terminal2num.put(">",11);
        terminal2num.put("<",12);
        terminal2num.put(">=",13);
        terminal2num.put("<=",14);
        terminal2num.put("==",15);
        terminal2num.put("+",16);
        terminal2num.put("-",17);
        terminal2num.put("*",18);
        terminal2num.put("/",19);
        terminal2num.put("NUM",20);
        terminal2num.put("E",21);
        terminal2num.put(";",22);
    }
    /**
     * LL1预测分析表
     */
    private static String[][] parsingTable = new String[14][23];

    /**
     * 获取产生式右端的字符
     *
     * @param key 非终结符
     * @return {@link Queue}<{@link String}>
     */
    private static Queue<String> getRightExprItems(String key){
        Queue<String> wordset = new LinkedList<>();
        String tempStr = key;
        int index = tempStr.indexOf(' ');
        while (index != -1) {
            String word = tempStr.substring(0, index);
            wordset.add(word);
            tempStr = tempStr.substring(index + 1);
            index = tempStr.indexOf(' ');
        }
        wordset.add(tempStr);
        return wordset;
    }

    /**
     * 从标准输入读取并划分得到token队列，初始化输入队列和分析栈
     */

    public static void split(StringBuilder prog){
        String progStr = prog.toString();
        String[] items = progStr.split(" ");
        if(progStr.length()==0){
            return;
        }
        if(progStr.equals(" ")){
            return;
        }
        if(items.length!=0){
            for(String item : items){
                if(item.length()!=0) {
                    inputQueue.add(item);
                }
            }
        }
        inputQueue.add("\n");
    }
    private static void readProg()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            StringBuilder prog = new StringBuilder();
            prog.append(sc.nextLine());
            split(prog);
        }
        inputQueue.add("$");
        analysisStack.add("$");
        analysisStack.add("program");
    }

    /**
     * 计算非终结符First集合
     *
     * @param rightExpr 一条1产生式中的右侧
     * @return {@link Set}<{@link String}>
     */
    public static Set<String> calcuFirst(String rightExpr){
        Set<String> first = new HashSet<>();
        //如果遍历到了一个终结符，就加入集合直接返回
        if(terminals.contains(rightExpr)){
            first.add(rightExpr);
            return first;
        }
        //一个非终结符或者多个符号，如果是多个符号的组合：多个终结符，多个非终结符，终结符+非终结符，需要先进行分割
        else {
            String tempStr = rightExpr;
            Queue<String> wordset = new LinkedList<>();
            int index = tempStr.indexOf(' ');
            if(index == -1){
                ArrayList<String> rightExprs= productions.get(tempStr);
                for (int i = 0 ; i <  rightExprs.size() ; i++){
                    first.addAll(calcuFirst(rightExprs.get(i)));
                }
                return first;
            }
            while(index != -1){
                String word = tempStr.substring(0,index);
                wordset.add(word);
                tempStr = tempStr.substring(index+1);
                index = tempStr.indexOf(' ');
            }
            wordset.add(tempStr);
            //遍历每一个符号
            for(String word : wordset){
                //如果是终结符
                if(terminals.contains(word)){
                    first.add(word);
                    break;
                }
                //如果是非终结符
                else{
                    //如果开头的非终结符不为空
                    if(!productions.get(word).contains('E')) {
                        first.addAll(calcuFirst(word));
                        break;
                    }
                    else {
                        first.addAll(calcuFirst(word));
                    }
                }
            }
        }
        return first;
    }

    /**
     * 计算所有符号的First集
     */
    public static void getFirstSet(){
        //先计算终结符的First集
        for(String str : terminals){
            Set<String> terminal = new HashSet<>();
            terminal.add(str);
            first.put(str,terminal);
        }
        //计算非终结符的Fisrt集
        for(String str : noneterminals){
            ArrayList<String> rightExprs= productions.get(str);
            Set<String> firstitems = new HashSet<>();
            for (int i = 0 ; i <  rightExprs.size() ; i++){
                //拿出一个产生式右部
                String rightExpr = rightExprs.get(i);
                //计算first集合子集
                Set<String> subFirst = calcuFirst(rightExpr);
                firstitems.addAll(subFirst);
                //遍历first集合子集中的终结符，将产生式右部填入预测分析表
                for(String s:subFirst){
                    Integer terminalIndex = terminal2num.get(s);
                    Integer noneterminalIndex = noneterminal2num.get(str);
                    parsingTable[noneterminalIndex][terminalIndex] = rightExpr;
                }
            }
            first.put(str,firstitems);
        }
    }

    /**
     * 计算Follow集
     */
    public static void getFollowSet(){
        //准备1：过滤E，用于计算非终结符的follow集
        for (String key : first.keySet()){
            Set<String> valueSet = new HashSet<>();
            for(String value : first.get(key)){
                if(!value.equals("E")){
                    valueSet.add(value);
                }
            }
            firstFiltered.put(key,valueSet);
        }
    boolean changed = true;
    while(changed){
        changed = false;
        for(String key : productions.keySet()){
            for(String value : productions.get(key)){
                //将产生式右侧的token进行分割获得wordset
                Queue<String> wordset = getRightExprItems(value);

                //遍历产生式右侧的每个字符，如果是非终结符就计算它的follow集，并查看是否发生变化
                boolean isend = false ;//用于标记是否有非终结符结尾
                String word = "";
                String alpha = "";
                while(!wordset.isEmpty()){
                    //取出一个字符
                    word = wordset.poll();
                   //获取当前非终结符的follow集
                    Set<String> presentFollow = follow.get(word);
                   //如果该字符是非终结符且不在队尾
                   if(noneterminals.contains(word)){
                       if(!wordset.isEmpty()){
                           //peek获取alpha
                           alpha = wordset.peek();
                           //获取alpha的filtered first集合
                           Set<String> alphaFirst = firstFiltered.get(alpha);
                           //获取alpha的unfiltered first集合
                           Set<String> unfilteredFirst = first.get(alpha);
                           //遍历查看first集合中的元素是否在当前follow集中
                           if(alphaFirst!=null){
                               for(String alphaFirstItem:alphaFirst){
                                   //如果不在，就添加并且标记改变
                                   if(!presentFollow.contains(alphaFirstItem)){
                                       changed = true;
                                       presentFollow.add(alphaFirstItem);
                                   }
                               }
                               //如果改变了就改变当前元素的follow集合
                               if(changed){
                                   follow.put(word,presentFollow);
                               }
                               //如果alpha可以为空
                               if(unfilteredFirst.contains("E")){
                                   //如果alpha是产生式右部的结尾，就需要向前看找follow集
                                   if(wordset.size()==1){
                                       isend = true;
                                   }
                               }
                           }
                       }
                       else {
                           isend = true;
                            }
                       //如果是非终结符直接或者间接结尾，需要向前看找follow集，结尾的非终结符是word
                       if(isend){
                           Set<String> rightFollow = follow.get(key);
                           presentFollow = follow.get(word);
                           if(!rightFollow.isEmpty()){
                               for(String item:rightFollow){
                                   if(!presentFollow.contains(item)){
                                       presentFollow.add(item);
                                   }
                               }
                               follow.put(word,presentFollow);
                           }
                       }
                       }
                }
            }
        }
    }
    }


    public static List<String> getSplit(String str){
        List<String> reverse = new ArrayList<>();
        int index = str.indexOf(' ');
        while(index != -1){
            String token = str.substring(0,index);
            reverse.add(token);
            str = str.substring(index+1);
            index = str.indexOf(' ');
        }
        reverse.add(str);
        return reverse;
    }

    public static void getParsingTable(){
        //获取first集，并用first集填一部分预测分析表
        getFirstSet();
        //获取follow集
        getFollowSet();
        //用follow集填一部分预测分析表
        for(String none :noneterminals){
            //如果非终结符的first集中包含空,就使用follow集合构造预测分析表
            if(first.get(none).contains("E")){
                Set<String> presentFollow = follow.get(none);
                for(String item:presentFollow){
                    parsingTable[noneterminal2num.get(none)][terminal2num.get(item)] = "E";
                }
            }
        }
        //输出查看预测分析表
        /*
        for(Integer row = 0;row<parsingTable.length;row++){
            int temp_row = row;
            String key = String.valueOf(noneterminal2num.entrySet().stream().filter(m -> m.getValue().equals(temp_row)).collect(Collectors.toList()));
            for(Integer col = 0;col<parsingTable[0].length;col++){
                int temp_col = col;
                String value = String.valueOf(terminal2num.entrySet().stream().filter(m -> m.getValue().equals(temp_col)).collect(Collectors.toList()));
                System.out.printf("<%s,%s>:%s  ",key,value,parsingTable[row][col]);
                System.out.println();
            }
            System.out.println("-----------------------------------");
        }*/
    }

    /**
     * 行
     */
    private static int lines = 0;

    /**
     * 错误行
     */
    private static int errorline = 0;

    /**
     * 位置
     */
    private static int pos = 0;

    /**
     * 模式，0为检查，1为输出
     */
    private static int mode = 0;


    public static void parse(String top,int level){
        if(mode == 1){
            if(!top.equals("program")){
                System.out.println();
            }
            for(int i = 0;i<level;i++){
                System.out.print("\t");
            }
            System.out.print(top);
        }
        if(errorline!=0 ||inputQueue.get(pos).equals("$")){
            return;
        }
        while(inputQueue.get(pos).equals("\n")){
            pos++;
            lines++;
        }
        String inputStr = inputQueue.get(pos);
        String contentInTable = parsingTable[noneterminal2num.get(top)][terminal2num.get(inputStr)];
        if(contentInTable == null){
            errorline = lines;
            int tempPos = pos - 1;
            while(inputQueue.get(tempPos).equals("\n")){
                errorline--;
                tempPos--;
            }
            errorline++;//将多减去的1加回来是正确的插入位置
            System.out.printf("语法错误,第%d行,缺少\";\"",errorline);
            System.out.println();
            inputQueue.add(tempPos+1,";");
            pos = tempPos+1;
        }
        else{
            List<String> derives = getSplit(contentInTable);
            for(String derive : derives){
                if(terminals.contains(derive)){
                    if(mode==1){
                        System.out.println();
                        for(int i=0;i<level+1;i++){
                            System.out.print("\t");
                        }
                        System.out.print(derive);
                    }
                    if(inputQueue.get(pos).equals("\n")){
                        pos++;
                        lines++;
                    }
                    if(inputQueue.get(pos).equals(derive)){
                        pos++;
                    }
                    if(derive.equals("E")){
                        return;
                    }
                }
                else{
                    parse(derive,level+1);
                }
            }

        }
    }
    private static void analysis()
    {
        readProg();
        getParsingTable();
        mode = 0;
        parse("program",0);
        lines = 0;
        pos = 0;
        mode = 1;
        errorline = 0;
        parse("program",0);
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }

    public LLParserAnalysis(){

    }
}
