package org.example;

import java.util.*;

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc java LR语法分析器
 */
@SuppressWarnings("unchecked")
public class LRParserAnalysis
{
    /**
     * 输入的token队列
     */
    private static List<String> inputQueue = new LinkedList();

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
        productions.put("arithexprprime", new ArrayList(Arrays.asList("+ multexpr arithexprprime ","- multexpr arithexprprime","E")));
        productions.put("multexpr", new ArrayList(Arrays.asList("simpleexpr multexprprime")));
        productions.put("multexprprime", new ArrayList(Arrays.asList("* simpleexpr multexprprime","/ simpleexpr multexprprime","E")));
        productions.put("simpleexpr", new ArrayList(Arrays.asList("ID","NUM","( arithexpr )")));
    }

    /**
     * 规约式
     */
    private static Map<String,Pair<String, Integer>> reduction = new HashMap<>();
    static {
        reduction.put("r0",new Pair<>("program'",1));
        reduction.put("r1",new Pair<>("program",  1));
        reduction.put("r2",new Pair<>("stmt", 1));
        reduction.put("r3",new Pair<>("stmt", 1));
        reduction.put("r4",new Pair<>("stmt", 1));
        reduction.put("r5",new Pair<>("stmt", 1));
        reduction.put("r6",new Pair<>("compoundstmt", 3));
        reduction.put("r7",new Pair<>("stmts", 2));
        reduction.put("r8",new Pair<>("stmts", 0));
        reduction.put("r9",new Pair<>("ifstmt", 8));
        reduction.put("r10",new Pair<>("whilestmt", 5));
        reduction.put("r11",new Pair<>("assgstmt", 4));
        reduction.put("r12",new Pair<>("boolexpr", 3));
        reduction.put("r13",new Pair<>("boolop", 1));
        reduction.put("r14",new Pair<>("boolop", 1));
        reduction.put("r15",new Pair<>("boolop", 1));
        reduction.put("r16",new Pair<>("boolop", 1));
        reduction.put("r17",new Pair<>("boolop", 1));
        reduction.put("r18",new Pair<>("arithexpr", 2));
        reduction.put("r19",new Pair<>("arithexprprime", 3));
        reduction.put("r20",new Pair<>("arithexprprime", 3));
        reduction.put("r21",new Pair<>("arithexprprime", 0));
        reduction.put("r22",new Pair<>("multexpr", 2));
        reduction.put("r23",new Pair<>("multexprprime", 3));
        reduction.put("r24",new Pair<>("multexprprime", 3));
        reduction.put("r25",new Pair<>("multexprprime", 0));
        reduction.put("r26",new Pair<>("simpleexpr", 1));
        reduction.put("r27",new Pair<>("simpleexpr", 1));
        reduction.put("r28",new Pair<>("simpleexpr", 3));
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
     * actiontable
     */
    private static String[][] actiontable = new String[58][23];
    static {
        actiontable[0][terminal2num.get("{")] = "s3";
        actiontable[1][terminal2num.get("$")] = "acc";
        actiontable[2][terminal2num.get("$")] = "r1";
        actiontable[3][terminal2num.get("{")] = "s3";
        actiontable[3][terminal2num.get("}")] = "r8";
        actiontable[3][terminal2num.get("if")] = "s10";
        actiontable[3][terminal2num.get("while")] = "s11";
        actiontable[3][terminal2num.get("ID")] = "s12";
        actiontable[4][terminal2num.get("}")] = "s13";
        actiontable[5][terminal2num.get("{")] = "s3";
        actiontable[5][terminal2num.get("}")] = "r8";
        actiontable[5][terminal2num.get("if")] = "s10";
        actiontable[5][terminal2num.get("while")] = "s11";
        actiontable[5][terminal2num.get("ID")] = "s12";
        actiontable[6][terminal2num.get("{")] = "r2";
        actiontable[6][terminal2num.get("}")] = "r2";
        actiontable[6][terminal2num.get("if")] = "r2";
        actiontable[6][terminal2num.get("else")] = "r2";
        actiontable[6][terminal2num.get("while")] = "r2";
        actiontable[6][terminal2num.get("ID")] = "r2";
        actiontable[7][terminal2num.get("{")] = "r3";
        actiontable[7][terminal2num.get("}")] = "r3";
        actiontable[7][terminal2num.get("if")] = "r3";
        actiontable[7][terminal2num.get("else")] = "r3";
        actiontable[7][terminal2num.get("while")] = "r3";
        actiontable[7][terminal2num.get("ID")] = "r3";
        actiontable[8][terminal2num.get("{")] = "r4";
        actiontable[8][terminal2num.get("}")] = "r4";
        actiontable[8][terminal2num.get("if")] = "r4";
        actiontable[8][terminal2num.get("else")] = "r4";
        actiontable[8][terminal2num.get("while")] = "r4";
        actiontable[8][terminal2num.get("ID")] = "r4";
        actiontable[9][terminal2num.get("{")] = "r5";
        actiontable[9][terminal2num.get("}")] = "r5";
        actiontable[9][terminal2num.get("if")] = "r5";
        actiontable[9][terminal2num.get("else")] = "r5";
        actiontable[9][terminal2num.get("while")] = "r5";
        actiontable[9][terminal2num.get("ID")] = "r5";
        actiontable[10][terminal2num.get("(")] = "s15";
        actiontable[11][terminal2num.get("(")] = "s16";
        actiontable[12][terminal2num.get("=")] = "s17";
        actiontable[13][terminal2num.get("{")] = "r6";
        actiontable[13][terminal2num.get("}")] = "r6";
        actiontable[13][terminal2num.get("if")] = "r6";
        actiontable[13][terminal2num.get("else")] = "r6";
        actiontable[13][terminal2num.get("while")] = "r6";
        actiontable[13][terminal2num.get("ID")] = "r6";
        actiontable[13][terminal2num.get("$")] = "r6";
        actiontable[14][terminal2num.get("}")] = "r7";
        actiontable[15][terminal2num.get("(")] = "s24";
        actiontable[15][terminal2num.get("ID")] = "s22";
        actiontable[15][terminal2num.get("NUM")] = "s23";
        actiontable[16][terminal2num.get("(")] = "s24";
        actiontable[16][terminal2num.get("ID")] = "s22";
        actiontable[16][terminal2num.get("NUM")] = "s23";
        actiontable[17][terminal2num.get("(")] = "s24";
        actiontable[17][terminal2num.get("ID")] = "s22";
        actiontable[17][terminal2num.get("NUM")] = "s23";
        actiontable[18][terminal2num.get(")")] = "s27";
        actiontable[19][terminal2num.get("<")] = "s29";
        actiontable[19][terminal2num.get(">")] = "s30";
        actiontable[19][terminal2num.get("<=")] = "s31";
        actiontable[19][terminal2num.get(">=")] = "s32";
        actiontable[19][terminal2num.get("==")] = "s33";
        actiontable[20][terminal2num.get(")")] = "r21";
        actiontable[20][terminal2num.get(";")] = "r21";
        actiontable[20][terminal2num.get("<")] = "r21";
        actiontable[20][terminal2num.get(">")] = "r21";
        actiontable[20][terminal2num.get("<=")] = "r21";
        actiontable[20][terminal2num.get(">=")] = "r21";
        actiontable[20][terminal2num.get("==")] = "r21";
        actiontable[20][terminal2num.get("+")] = "s35";
        actiontable[20][terminal2num.get("-")] = "s36";
        actiontable[21][terminal2num.get(")")] = "r25";
        actiontable[21][terminal2num.get(";")] = "r25";
        actiontable[21][terminal2num.get("<")] = "r25";
        actiontable[21][terminal2num.get(">")] = "r25";
        actiontable[21][terminal2num.get("<=")] = "r25";
        actiontable[21][terminal2num.get(">=")] = "r25";
        actiontable[21][terminal2num.get("==")] = "r25";
        actiontable[21][terminal2num.get("+")] = "r25";
        actiontable[21][terminal2num.get("-")] = "r25";
        actiontable[21][terminal2num.get("*")] = "s38";
        actiontable[21][terminal2num.get("/")] = "s39";
        actiontable[22][terminal2num.get(")")] = "r26";
        actiontable[22][terminal2num.get(";")] = "r26";
        actiontable[22][terminal2num.get("<")] = "r26";
        actiontable[22][terminal2num.get(">")] = "r26";
        actiontable[22][terminal2num.get("<=")] = "r26";
        actiontable[22][terminal2num.get(">=")] = "r26";
        actiontable[22][terminal2num.get("==")] = "r26";
        actiontable[22][terminal2num.get("+")] = "r26";
        actiontable[22][terminal2num.get("-")] = "r26";
        actiontable[22][terminal2num.get("*")] = "r26";
        actiontable[22][terminal2num.get("/")] = "r26";
        actiontable[23][terminal2num.get(")")] = "r27";
        actiontable[23][terminal2num.get(";")] = "r27";
        actiontable[23][terminal2num.get("<")] = "r27";
        actiontable[23][terminal2num.get(">")] = "r27";
        actiontable[23][terminal2num.get("<=")] = "r27";
        actiontable[23][terminal2num.get(">=")] = "r27";
        actiontable[23][terminal2num.get("==")] = "r27";
        actiontable[23][terminal2num.get("+")] = "r27";
        actiontable[23][terminal2num.get("-")] = "r27";
        actiontable[23][terminal2num.get("*")] = "r27";
        actiontable[23][terminal2num.get("/")] = "r27";
        actiontable[23][terminal2num.get("}")] = "e1";  // ERROR HANDLING
        actiontable[24][terminal2num.get("(")] = "s24";
        actiontable[24][terminal2num.get("ID")] = "s22";
        actiontable[24][terminal2num.get("NUM")] = "s23";
        actiontable[25][terminal2num.get(")")] = "s41";
        actiontable[26][terminal2num.get(";")] = "s42";
        actiontable[27][terminal2num.get("then")] = "s43";
        actiontable[28][terminal2num.get("(")] = "s24";
        actiontable[28][terminal2num.get("ID")] = "s22";
        actiontable[28][terminal2num.get("NUM")] = "s23";
        actiontable[29][terminal2num.get("(")] = "r13";
        actiontable[29][terminal2num.get("ID")] = "r13";
        actiontable[29][terminal2num.get("NUM")] = "r13";
        actiontable[30][terminal2num.get("(")] = "r14";
        actiontable[30][terminal2num.get("ID")] = "r14";
        actiontable[30][terminal2num.get("NUM")] = "r14";
        actiontable[31][terminal2num.get("(")] = "r15";
        actiontable[31][terminal2num.get("ID")] = "r15";
        actiontable[31][terminal2num.get("NUM")] = "r15";
        actiontable[32][terminal2num.get("(")] = "r16";
        actiontable[32][terminal2num.get("ID")] = "r16";
        actiontable[32][terminal2num.get("NUM")] = "r16";
        actiontable[33][terminal2num.get("(")] = "r17";
        actiontable[33][terminal2num.get("ID")] = "r17";
        actiontable[33][terminal2num.get("NUM")] = "r17";
        actiontable[34][terminal2num.get(")")] = "r18";
        actiontable[34][terminal2num.get(";")] = "r18";
        actiontable[34][terminal2num.get("<")] = "r18";
        actiontable[34][terminal2num.get(">")] = "r18";
        actiontable[34][terminal2num.get("<=")] = "r18";
        actiontable[34][terminal2num.get(">=")] = "r18";
        actiontable[34][terminal2num.get("==")] = "r18";
        actiontable[35][terminal2num.get("(")] = "s24";
        actiontable[35][terminal2num.get("ID")] = "s22";
        actiontable[35][terminal2num.get("NUM")] = "s23";
        actiontable[36][terminal2num.get("(")] = "s24";
        actiontable[36][terminal2num.get("ID")] = "s22";
        actiontable[36][terminal2num.get("NUM")] = "s23";
        actiontable[37][terminal2num.get(")")] = "r22";
        actiontable[37][terminal2num.get(";")] = "r22";
        actiontable[37][terminal2num.get("<")] = "r22";
        actiontable[37][terminal2num.get(">")] = "r22";
        actiontable[37][terminal2num.get("<=")] = "r22";
        actiontable[37][terminal2num.get(">=")] = "r22";
        actiontable[37][terminal2num.get("==")] = "r22";
        actiontable[37][terminal2num.get("+")] = "r22";
        actiontable[37][terminal2num.get("-")] = "r22";
        actiontable[38][terminal2num.get("(")] = "s24";
        actiontable[38][terminal2num.get("ID")] = "s22";
        actiontable[38][terminal2num.get("NUM")] = "s23";
        actiontable[39][terminal2num.get("(")] = "s24";
        actiontable[39][terminal2num.get("ID")] = "s22";
        actiontable[39][terminal2num.get("NUM")] = "s23";
        actiontable[40][terminal2num.get(")")] = "s49";
        actiontable[41][terminal2num.get("{")] = "s3";
        actiontable[41][terminal2num.get("if")] = "s10";
        actiontable[41][terminal2num.get("while")] = "s11";
        actiontable[41][terminal2num.get("ID")] = "s12";
        actiontable[42][terminal2num.get("{")] = "r11";
        actiontable[42][terminal2num.get("}")] = "r11";
        actiontable[42][terminal2num.get("if")] = "r11";
        actiontable[42][terminal2num.get("else")] = "r11";
        actiontable[42][terminal2num.get("while")] = "r11";
        actiontable[42][terminal2num.get("ID")] = "r11";
        actiontable[43][terminal2num.get("{")] = "s3";
        actiontable[43][terminal2num.get("if")] = "s10";
        actiontable[43][terminal2num.get("while")] = "s11";
        actiontable[43][terminal2num.get("ID")] = "s12";
        actiontable[44][terminal2num.get(")")] = "r12";
        actiontable[45][terminal2num.get(")")] = "r21";
        actiontable[45][terminal2num.get(";")] = "r21";
        actiontable[45][terminal2num.get("<")] = "r21";
        actiontable[45][terminal2num.get(">")] = "r21";
        actiontable[45][terminal2num.get("<=")] = "r21";
        actiontable[45][terminal2num.get(">=")] = "r21";
        actiontable[45][terminal2num.get("==")] = "r21";
        actiontable[45][terminal2num.get("+")] = "s35";
        actiontable[45][terminal2num.get("-")] = "s36";
        actiontable[46][terminal2num.get(")")] = "r21";
        actiontable[46][terminal2num.get(";")] = "r21";
        actiontable[46][terminal2num.get("<")] = "r21";
        actiontable[46][terminal2num.get(">")] = "r21";
        actiontable[46][terminal2num.get("<=")] = "r21";
        actiontable[46][terminal2num.get(">=")] = "r21";
        actiontable[46][terminal2num.get("==")] = "r21";
        actiontable[46][terminal2num.get("+")] = "s35";
        actiontable[46][terminal2num.get("-")] = "s36";
        actiontable[47][terminal2num.get(")")] = "r25";
        actiontable[47][terminal2num.get(";")] = "r25";
        actiontable[47][terminal2num.get("<")] = "r25";
        actiontable[47][terminal2num.get(">")] = "r25";
        actiontable[47][terminal2num.get("<=")] = "r25";
        actiontable[47][terminal2num.get(">=")] = "r25";
        actiontable[47][terminal2num.get("==")] = "r25";
        actiontable[47][terminal2num.get("+")] = "r25";
        actiontable[47][terminal2num.get("-")] = "r25";
        actiontable[47][terminal2num.get("*")] = "s38";
        actiontable[47][terminal2num.get("/")] = "s39";
        actiontable[48][terminal2num.get(")")] = "r25";
        actiontable[48][terminal2num.get(";")] = "r25";
        actiontable[48][terminal2num.get("<")] = "r25";
        actiontable[48][terminal2num.get(">")] = "r25";
        actiontable[48][terminal2num.get("<=")] = "r25";
        actiontable[48][terminal2num.get(">=")] = "r25";
        actiontable[48][terminal2num.get("==")] = "r25";
        actiontable[48][terminal2num.get("+")] = "r25";
        actiontable[48][terminal2num.get("-")] = "r25";
        actiontable[48][terminal2num.get("*")] = "s38";
        actiontable[48][terminal2num.get("/")] = "s39";
        actiontable[49][terminal2num.get(")")] = "r28";
        actiontable[49][terminal2num.get(";")] = "r28";
        actiontable[49][terminal2num.get("<")] = "r28";
        actiontable[49][terminal2num.get(">")] = "r28";
        actiontable[49][terminal2num.get("<=")] = "r28";
        actiontable[49][terminal2num.get(">=")] = "r28";
        actiontable[49][terminal2num.get("==")] = "r28";
        actiontable[49][terminal2num.get("+")] = "r28";
        actiontable[49][terminal2num.get("-")] = "r28";
        actiontable[49][terminal2num.get("*")] = "r28";
        actiontable[49][terminal2num.get("/")] = "r28";
        actiontable[50][terminal2num.get("{")] = "r10";
        actiontable[50][terminal2num.get("}")] = "r10";
        actiontable[50][terminal2num.get("if")] = "r10";
        actiontable[50][terminal2num.get("else")] = "r10";
        actiontable[50][terminal2num.get("while")] = "r10";
        actiontable[50][terminal2num.get("ID")] = "r10";
        actiontable[51][terminal2num.get("else")] = "s56";
        actiontable[52][terminal2num.get(")")] = "r19";
        actiontable[52][terminal2num.get(";")] = "r19";
        actiontable[52][terminal2num.get("<")] = "r19";
        actiontable[52][terminal2num.get(">")] = "r19";
        actiontable[52][terminal2num.get("<=")] = "r19";
        actiontable[52][terminal2num.get(">=")] = "r19";
        actiontable[52][terminal2num.get("==")] = "r19";
        actiontable[53][terminal2num.get(")")] = "r20";
        actiontable[53][terminal2num.get(";")] = "r20";
        actiontable[53][terminal2num.get("<")] = "r20";
        actiontable[53][terminal2num.get(">")] = "r20";
        actiontable[53][terminal2num.get("<=")] = "r20";
        actiontable[53][terminal2num.get(">=")] = "r20";
        actiontable[53][terminal2num.get("==")] = "r20";
        actiontable[54][terminal2num.get(")")] = "r23";
        actiontable[54][terminal2num.get(";")] = "r23";
        actiontable[54][terminal2num.get("<")] = "r23";
        actiontable[54][terminal2num.get(">")] = "r23";
        actiontable[54][terminal2num.get("<=")] = "r23";
        actiontable[54][terminal2num.get(">=")] = "r23";
        actiontable[54][terminal2num.get("==")] = "r23";
        actiontable[54][terminal2num.get("+")] = "r23";
        actiontable[54][terminal2num.get("-")] = "r23";
        actiontable[55][terminal2num.get(")")] = "r24";
        actiontable[55][terminal2num.get(";")] = "r24";
        actiontable[55][terminal2num.get("<")] = "r24";
        actiontable[55][terminal2num.get(">")] = "r24";
        actiontable[55][terminal2num.get("<=")] = "r24";
        actiontable[55][terminal2num.get(">=")] = "r24";
        actiontable[55][terminal2num.get("==")] = "r24";
        actiontable[55][terminal2num.get("+")] = "r24";
        actiontable[55][terminal2num.get("-")] = "r24";
        actiontable[56][terminal2num.get("{")] = "s3";
        actiontable[56][terminal2num.get("if")] = "s10";
        actiontable[56][terminal2num.get("while")] = "s11";
        actiontable[56][terminal2num.get("ID")] = "s12";
        actiontable[57][terminal2num.get("{")] = "r9";
        actiontable[57][terminal2num.get("}")] = "r9";
        actiontable[57][terminal2num.get("if")] = "r9";
        actiontable[57][terminal2num.get("else")] = "r9";
        actiontable[57][terminal2num.get("while")] = "r9";
        actiontable[57][terminal2num.get("ID")] = "r9";
    }

    /**
     * gototable
     */
    private static int[][] gototable = new int[57][14];
    static {
        gototable[0][noneterminal2num.get("program")] = 1;
        gototable[0][noneterminal2num.get("compoundstmt")] = 2;
        gototable[3][noneterminal2num.get("stmt")] = 5;
        gototable[3][noneterminal2num.get("compoundstmt")] = 9;
        gototable[3][noneterminal2num.get("stmts")] = 4;
        gototable[3][noneterminal2num.get("ifstmt")] = 6;
        gototable[3][noneterminal2num.get("whilestmt")] = 7;
        gototable[3][noneterminal2num.get("assgstmt")] = 8;
        gototable[5][noneterminal2num.get("stmt")] = 5;
        gototable[5][noneterminal2num.get("compoundstmt")] = 9;
        gototable[5][noneterminal2num.get("stmts")] = 14;
        gototable[5][noneterminal2num.get("ifstmt")] = 6;
        gototable[5][noneterminal2num.get("whilestmt")] = 7;
        gototable[5][noneterminal2num.get("assgstmt")] = 8;
        gototable[15][noneterminal2num.get("boolexpr")] = 18;
        gototable[15][noneterminal2num.get("arithexpr")] = 19;
        gototable[15][noneterminal2num.get("multexpr")] = 20;
        gototable[15][noneterminal2num.get("simpleexpr")] = 21;
        gototable[16][noneterminal2num.get("boolexpr")] = 25;
        gototable[16][noneterminal2num.get("arithexpr")] = 19;
        gototable[16][noneterminal2num.get("multexpr")] = 20;
        gototable[16][noneterminal2num.get("simpleexpr")] = 21;
        gototable[17][noneterminal2num.get("arithexpr")] = 26;
        gototable[17][noneterminal2num.get("multexpr")] = 20;
        gototable[17][noneterminal2num.get("simpleexpr")] = 21;
        gototable[19][noneterminal2num.get("boolop")] = 28;
        gototable[20][noneterminal2num.get("arithexprprime")] = 34;
        gototable[21][noneterminal2num.get("multexprprime")] = 37;
        gototable[24][noneterminal2num.get("arithexpr")] = 40;
        gototable[24][noneterminal2num.get("multexpr")] = 20;
        gototable[24][noneterminal2num.get("simpleexpr")] = 21;
        gototable[28][noneterminal2num.get("arithexpr")] = 44;
        gototable[28][noneterminal2num.get("multexpr")] = 20;
        gototable[28][noneterminal2num.get("simpleexpr")] = 21;
        gototable[35][noneterminal2num.get("multexpr")] = 45;
        gototable[35][noneterminal2num.get("simpleexpr")] = 21;
        gototable[36][noneterminal2num.get("multexpr")] = 46;
        gototable[36][noneterminal2num.get("simpleexpr")] = 21;
        gototable[38][noneterminal2num.get("simpleexpr")] = 47;
        gototable[39][noneterminal2num.get("simpleexpr")] = 48;
        gototable[41][noneterminal2num.get("stmt")] = 50;
        gototable[41][noneterminal2num.get("compoundstmt")] = 9;
        gototable[41][noneterminal2num.get("ifstmt")] = 6;
        gototable[41][noneterminal2num.get("whilestmt")] = 7;
        gototable[41][noneterminal2num.get("assgstmt")] = 8;
        gototable[43][noneterminal2num.get("stmt")] = 51;
        gototable[43][noneterminal2num.get("compoundstmt")] = 9;
        gototable[43][noneterminal2num.get("ifstmt")] = 6;
        gototable[43][noneterminal2num.get("whilestmt")] = 7;
        gototable[43][noneterminal2num.get("assgstmt")] = 8;
        gototable[45][noneterminal2num.get("arithexprprime")] = 52;
        gototable[46][noneterminal2num.get("arithexprprime")] = 53;
        gototable[47][noneterminal2num.get("multexprprime")] = 54;
        gototable[48][noneterminal2num.get("multexprprime")] = 55;
        gototable[56][noneterminal2num.get("stmt")] = 57;
        gototable[56][noneterminal2num.get("compoundstmt")] = 9;
        gototable[56][noneterminal2num.get("ifstmt")] = 6;
        gototable[56][noneterminal2num.get("whilestmt")] = 7;
        gototable[56][noneterminal2num.get("assgstmt")] = 8;
    }

    /**
     * 状态栈
     */
    private static Stack<Integer> states = new Stack<>();

    /**
     * 符号队列
     */
    private static LinkedList<String> symbols = new LinkedList<>();

    /**
     * 判断是否为移入
     *
     * @param input 输入
     * @return boolean
     */
    public static boolean isShift(String input){
        if(input.length()>0){
            if(input.startsWith("s") || input.startsWith("S")){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否规约
     *
     * @param input 输入
     * @return boolean
     */
    public static boolean isReduce(String input){
        if(input.length()>0){
            if(input.startsWith("r") || input.startsWith("R")){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否遇到错误
     *
     * @param input 输入
     * @return boolean
     */
    public static boolean isError(String input){
        if(input.length()>0){
            if(input.startsWith("e") || input.startsWith("E")){
                return true;
            }
        }
        return false;
    }

    /**
     * 状态字符串转化为数字
     *
     * @param state 状态
     * @return int
     */
    public static int statestr2num(String state){
        return Integer.valueOf(state.substring(1));
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

    /**
     *  从标准输入读取输入队列
     */
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
    }

    /**
     * 分析主函数
     */
    private static void analysis()
    {
        readProg();
        states.push(0);

        String errorMsg = "";
        Integer linenumber = 0;
        int pos = 0;
        Stack<String> ans = new Stack<>();
        StringBuilder origin = new StringBuilder();
        boolean erroroccured = false;

        for (String str:inputQueue){
            if (!str.equals("\n") && !str.equals("$")) {
                origin.append(str+" ");
            }
        }
        ans.push(origin.toString());

        while (true) {
            if(erroroccured){
                pos = 0;
                linenumber = 0;
                while(!ans.empty()){
                    ans.pop();
                }
                origin= new StringBuilder();
                for (String str:inputQueue){
                    if (!str.equals("\n") && !str.equals("$")) {
                        origin.append(str+" ");
                    }
                }
                ans.push(origin.toString());
                while(!states.empty()){
                    states.pop();
                }
                states.push(0);
                symbols.clear();
                erroroccured = false;
            }
            if(inputQueue.get(pos).equals("\n")){
                linenumber++;
                pos++;
            }
            String action = actiontable[states.peek()][terminal2num.get(inputQueue.get(pos))];
            if (isShift(action)) {
                // 移入
                states.push(statestr2num(action));
                symbols.add(inputQueue.get(pos++));
            } else if (isReduce(action)){
                // 规约
                Pair r = reduction.get(action);
                //获取规约元素个数
                int count = (int) r.getEle2();
                //将规约元素移出堆栈
                for (int j = 0; j < count ; ++j) {
                    states.pop();
                    symbols.removeLast();
                }
                //将新的状态和符号压入堆栈
                String symbol = (String) r.getEle1();
                int index = noneterminal2num.get(symbol);
                states.push(gototable[states.peek()][index]);
                symbols.add(symbol);
               //符号队列+input队列剩余元素构造一个最右推导
                List<String> oneAns = new ArrayList<>();
                for(String str:symbols){
                    oneAns.add(str+" ");
                }
                for(int j=pos;j<inputQueue.size();j++){
                    if(inputQueue.get(j).equals("\n")||inputQueue.get(j).equals("$")){
                        continue;
                    }
                    oneAns.add(inputQueue.get(j)+" ");
                }
                StringBuilder ansstr = new StringBuilder();
                for(String str:oneAns){
                    ansstr.append(str);
                }
                ans.push(ansstr.toString());
            } else if (isError(action)) {
                errorMsg = "语法错误，第" + linenumber.toString() + "行，缺少\";\"";
                System.out.println(errorMsg);
                if(inputQueue.get(pos-1).equals("\n")){
                    inputQueue.add(--pos, ";");
                }else{
                    inputQueue.add(pos, ";");
                }
                erroroccured = true;
            } else {
                // ACC
                break;
            }
        }
        int count = ans.size();
        while(!ans.empty()){
            String oneAns = ans.pop();
            if(count!=1){
                System.out.println(oneAns+"=> ");
            }else{
                System.out.println(oneAns);
            }
            count--;
        }
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 自定义Pair类
 */
@SuppressWarnings("unchecked")
class Pair<T1,T2>{
    private T1 ele1;
    private T2 ele2;
    public Pair(T1 x,T2 y)
    {
        ele1=x;
        ele2=y;
    }

    public T2 getEle2(){
        return this.ele2;
    }

    public T1 getEle1(){
        return this.ele1;
    }

    @Override
    public boolean equals(Object anObject)
    {
        if(this==anObject){
            return true;
        }
        if(anObject instanceof Pair<?,?>)
        {
            Pair<?,?> that=(Pair<?,?>)anObject;
            return this.ele1.equals(that.ele1)&&this.ele2.equals(that.ele2);
        }
        return false;
    }
    @Override
    public int hashCode()
    {
        return ele1.hashCode()*107+ele2.hashCode();
    }
}