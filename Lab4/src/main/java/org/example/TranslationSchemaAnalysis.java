package org.example;

import java.util.*;

public class TranslationSchemaAnalysis
{
    /**
     * 输入的token队列
     */
    private static List<String> inputQueue = new LinkedList();

    /**
     *建立字符串和标识符之间的映射
     */
    private static Map<String,Identifier> idMap = new HashMap<>();

    /**
     * 当前位置
     */
    private static int pos = 0;

    /**
     * 行数
     */
    private static int linenumber = 1;

    /**
     * 是否出现错误
     */
    private static boolean erroroccurred = false;

    /**
     * 类型错误
     *
     * @param errorline errorline
     */
    public static void typeError(int errorline){
        System.out.printf("error message:line %d,realnum can not be translated into int type\n", errorline);
    }

    /**
     * 跳过处理过的token
     *
     * @param count 数
     */
    public static void forward(int count){
        for(int i = 0; i < count;) {
            if (inputQueue.get(pos).equals("\n")) {
                linenumber++;
            }
            else {
                i++;
            }
            pos++;
        }
        if (inputQueue.get(pos).equals("\n")) {
            linenumber++;
            pos++;
        }
    }

    /**
     * 处理声明语句
     */
    public static void declare(){
        String name = inputQueue.get(pos+1);
        String type = inputQueue.get(pos);
        double value = Float.valueOf(inputQueue.get(pos+3));
        Identifier id = new Identifier();
        if(type.equals("int")){
            id = new Identifier(name,Type.INT,value);
            if(inputQueue.get(pos+3).contains(".")){
                typeError(linenumber);
                erroroccurred = true;
            }
        }
        else if(type.equals("real")){
            id = new Identifier(name,Type.REAL,value);
        }
        idMap.put(name,id);
        forward(4);
    }

    /**
     * 处理赋值语句
     *
     * @return {@link AssignExpr}
     */
    public static AssignExpr assign(){
        Identifier target = idMap.get(inputQueue.get(pos));
        Identifier left;
        Identifier right;
        String leftstr = inputQueue.get(pos+2);
        String rightstr = inputQueue.get(pos+4);
        //判断leftstr是数字或者字符
        int leftvalue = leftstr.charAt(0)-'0';
        if(leftvalue >= 0 && leftvalue <= 9){
            left = new Identifier("null",Type.TEMP,leftvalue);
        }
        else{
            left = idMap.get(inputQueue.get(pos+2));
        }
        int rightvalue = rightstr.charAt(0)-'0';
        if(rightvalue >= 0 && rightvalue <= 9){
            right = new Identifier("null",Type.TEMP,rightvalue);
        }
        else{
            right = idMap.get(inputQueue.get(pos+4));
        }
        char operator = inputQueue.get(pos+3).charAt(0);
        AssignExpr assignExpr = null;
        switch (operator){
            case '+':
                assignExpr = new AssignExpr(target,left,right,Operator.PLUS);
                break;
            case '-':
                assignExpr = new AssignExpr(target,left,right,Operator.SUB);
                break;
            case '*':
                assignExpr = new AssignExpr(target,left,right,Operator.MULT);
                break;
            case '/':
                assignExpr = new AssignExpr(target,left,right,Operator.DIVIDE);
                break;
        }
        if (assignExpr != null) {
            assignExpr.linenumber = linenumber;
        }
        forward(6);
        String next = inputQueue.get(pos-1);
        if(next.equals("+") || next.equals("-") || next.equals("*") || next.equals("/")){
            double tempvalue = assignExpr.execute();
            int rvalue = Integer.parseInt(inputQueue.get(pos));
            Identifier r = new Identifier("null",Type.TEMP,rvalue);
            Identifier l = new Identifier("null" , Type.TEMP,tempvalue);
            char opt = next.charAt(0);
            switch (opt){
                case '+':
                    assignExpr = new AssignExpr(target,l,r,Operator.PLUS);
                    break;
                case '-':
                    assignExpr = new AssignExpr(target,l,r,Operator.SUB);
                    break;
                case '*':
                    assignExpr = new AssignExpr(target,l,r,Operator.MULT);
                    break;
                case '/':
                    assignExpr = new AssignExpr(target,l,r,Operator.DIVIDE);
                    break;
            }
            forward(2);
        }
        return assignExpr;
    }

    /**
     * 处理if语句
     *
     * @return {@link IfExpr}
     */
    public static IfExpr dealIf(){
        forward(2); // Forward if (
        BoolExpr  boolExpr = dealBool();
        forward(2); // Forward ) then
        AssignExpr  thenExpr = assign();
        forward(1); // Forward else
        AssignExpr elseExpr = assign();
        return new IfExpr(boolExpr, thenExpr, elseExpr);
    }

    /**
     * 处理条件表达式
     *
     * @return {@link BoolExpr}
     */
    public static BoolExpr dealBool(){
        Identifier left = idMap.get(inputQueue.get(pos));
        Identifier right = idMap.get(inputQueue.get(pos+2));
        String cmp = inputQueue.get(pos+1);
        BoolExpr  boolExpr = null;
        if (cmp.equals("<")) {
            boolExpr = new BoolExpr(left, right, Comparator.LESS);
        }else if(cmp.equals("<=")){
            boolExpr = new BoolExpr(left, right, Comparator.LESSEQUAL);
        }
        else if(cmp.equals(">")) {
            boolExpr = new BoolExpr(left, right, Comparator.GREATER);
        }
        else if(cmp.equals(">=")){
            boolExpr = new BoolExpr(left, right, Comparator.GREATEREQUAL);
        }
        else if(cmp.equals("==")){
            boolExpr = new BoolExpr(left, right, Comparator.EQUAL);
        }
        forward(3);
        return boolExpr;
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
     *  从标准输入读入输入队列
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
     * 处理主函数
     */
    public static void analysis()
    {
        readProg();
        while(true){
            String next = inputQueue.get(pos);
            //如果处理完所有输入token
            if(next.equals("$")){
                if(!erroroccurred){
                    for(String str:idMap.keySet()){
                        if(idMap.get(str).type==Type.INT){
                            System.out.printf("%s: %d\n",str,(int)idMap.get(str).value);
                        }
                        else{
                            if(idMap.get(str).value<1){
                                System.out.printf("%s: %.2f\n", str,idMap.get(str).value);
                            }
                            else {
                                System.out.printf("%s: %s\n", str, String.format("%.1f", idMap.get(str).value));
                            }
                        }
                    }
                }
                break;
            }
            //如果遇到换行
            if(next.equals("\n")){
                linenumber++;
                pos++;
                continue;
            }
            //如果遇到结尾标识符
            if(next.equals(";")||next.equals("{")||next.equals("}")){
                pos++;
                continue;
            }
            //如果遇到表达式，分三种情况处理：声明、条件语句、赋值语句
            if(next.equals("real") || next.equals("int")){
                declare();
            } else if (next.equals("if")) {
                dealIf().execute();
            }
            else {
                assign().execute();
            }
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
 * @doc 比较运算符
 */
enum Comparator{
    GREATER,
    GREATEREQUAL,
    LESS,
    LESSEQUAL,
    EQUAL
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 操作符
 */
enum Operator{
    PLUS,
    SUB,
    DIVIDE,
    MULT
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 数据类型
 */
enum Type{
    REAL,
    INT,
    TEMP
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 标识符
 */
class Identifier{
    public String name;
    public Type type;
    public double value;
    public Identifier(String name, Type type,double value){
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Identifier() {
    }
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 赋值表达式
 */
class AssignExpr{
    private Identifier target;
    private Identifier left;
    private Identifier right;
    private Operator operator;
    public int linenumber = 0;

    public AssignExpr(Identifier target,Identifier left,Identifier right,Operator operator){
        this.target = target;
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    /**
     * 除0错误
     *
     * @param errorline errorline
     */
    public static void divisionError(int errorline){
        System.out.printf("error message:line %d,division by zero\n", errorline);
    }

    /**
     * 执行算术运算
     *
     * @return double
     */
    public double execute(){
        switch (operator){
            case PLUS:
                target.value = left.value + right.value;
                break;
            case SUB:
                target.value = left.value - right.value;
                break;
            case MULT:
                target.value = left.value * right.value;
                break;
            case DIVIDE:
                if(right.value==0){
                    divisionError(linenumber);
                    return 0;
                }
                target.value = left.value / right.value;
                break;
            default:
                break;
        }
        return target.value;
    }
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 布尔表达式
 */
class BoolExpr{
    private Identifier left;
    private Identifier right;
    private Comparator comparator;

    public  BoolExpr(Identifier left,Identifier right,Comparator comparator){
        this.left = left;
        this.right = right;
        this.comparator = comparator;
    }

    /**
     * 判断大小关系
     *
     * @return boolean
     */
    public boolean evaluate(){
        switch (comparator){
            case GREATER:
                return left.value > right.value;
            case GREATEREQUAL:
                return left.value >= right.value;
            case LESS:
                return left.value < right.value;
            case LESSEQUAL:
                return left.value <= right.value;
            case EQUAL:
                return left.value == right.value;
        }
        return false;
    }
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 条件表达式
 */
class IfExpr{
    private BoolExpr boolExpr;
    private AssignExpr thenExpr;
    private AssignExpr elseExpr;
    public IfExpr(BoolExpr boolExpr,AssignExpr thenExpr,AssignExpr elseExpr){
        this.boolExpr = boolExpr;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    /**
     * 执行语句
     */
    public void execute(){
        if(boolExpr.evaluate()){
            thenExpr.execute();
        }
        else {
            elseExpr.execute();
        }
    }

}