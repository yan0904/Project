package org.example;

import javafx.util.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 词法分析
 */
public class LexAnalysis
{
    private static StringBuilder prog = new StringBuilder();

    /**
     *  this method is to read the standard input
     */
    private static void readProg()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            prog.append(sc.nextLine()+'\n');
        }
    }
    private static void analysis() {
        readProg();
        Analyzer analyzer =new Analyzer(prog.toString());
        for (Token token = analyzer.nextToken(); token.getType() != TokenType.END; token = analyzer.nextToken()) {
            token.print();
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
 * @date 2022/11/07
 * @doc 枚举类表示token的类型名称
 */
enum  TokenType {
    KEYWORDS,
    OPERATOR,
    COMMENTS,
    CONSTANT,
    IDENTIFIER,
    END
}


/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/11/05
 * @doc A token is an analysed unit during lexical analysis
 */
class Token{
    /**
     * store all the errors
     */
    private List<ErrorMessage> errorList = new ArrayList<>();
    /**
     * each tokenId identify a token uniquely
     */
    private int tokenId;
    /**
     * the type of a token
     */
    private TokenType type;
    /**
     * the content of a token
     */
    private String lexeme;

    /**
     * establish the map between type and typeid
     */
    private static final Map<String, Integer> map = new HashMap<>();
    static {
        map.put("auto",       1);
        map.put("break",      2);
        map.put("case",       3);
        map.put("char",       4);
        map.put("const",      5);
        map.put("continue",   6);
        map.put("default",    7);
        map.put("do",         8);
        map.put("double",     9);
        map.put("else",       10);
        map.put("enum",       11);
        map.put("extern",     12);
        map.put("float",      13);
        map.put("for",        14);
        map.put("goto",       15);
        map.put("if",         16);
        map.put("int",        17);
        map.put("long",       18);
        map.put("register",   19);
        map.put("return",     20);
        map.put("short",      21);
        map.put("signed",     22);
        map.put("sizeof",     23);
        map.put("static",     24);
        map.put("struct",     25);
        map.put("switch",     26);
        map.put("typedef",    27);
        map.put("union",      28);
        map.put("unsigned",   29);
        map.put("void",       30);
        map.put("volatile",   31);
        map.put("while",      32);
        map.put("-",          33);
        map.put("--",         34);
        map.put("-=",         35);
        map.put("->",         36);
        map.put("!",          37);
        map.put("!=",         38);
        map.put("%",          39);
        map.put("%=",         40);
        map.put("&",          41);
        map.put("&&",         42);
        map.put("&=",         43);
        map.put("(",          44);
        map.put(")",          45);
        map.put("*",          46);
        map.put("*=",         47);
        map.put(",",          48);
        map.put(".",          49);
        map.put("/",          50);
        map.put("/=",         51);
        map.put(":",          52);
        map.put(";",          53);
        map.put("?",          54);
        map.put("[",          55);
        map.put("]",          56);
        map.put("^",          57);
        map.put("^=",         58);
        map.put("{",          59);
        map.put("|",          60);
        map.put("||",         61);
        map.put("|=",         62);
        map.put("}",          63);
        map.put("~",          64);
        map.put("+",          65);
        map.put("++",         66);
        map.put("+=",         67);
        map.put("<",          68);
        map.put("<<",         69);
        map.put("<<=",        70);
        map.put("<=",         71);
        map.put("=",          72);
        map.put("==",         73);
        map.put(">",          74);
        map.put(">=",         75);
        map.put(">>",         76);
        map.put(">>=",        77);
        map.put("\"",         78);
        map.put("Comment",    79);
        map.put("CONSTANT",   80);
        map.put("IDENTIFIER", 81);
    }

    public TokenType getType(){
        return this.type;
    }
    /**
     * different classes of types
     */
    Token(TokenType type,String lexeme){
        this.type = type;
        this.lexeme = lexeme;
    }
    Token(TokenType type,String lexeme, int tokenId){
        this.type = type;
        this.lexeme = lexeme;
        this.tokenId = tokenId;
    }

    /**
     * get id of a token
     *
     * @return tokenId
     */
    public int getTypeId(){
        switch (type){
            case KEYWORDS:
                return map.get(this.lexeme);
            case OPERATOR:
                return map.get(this.lexeme);
            case COMMENTS:
                return 79;
            case CONSTANT:
                return 80;
            case IDENTIFIER:
                return 81;
            default:
                return  0;
        }
    }
    void print(){
        if (tokenId != 1){
            System.out.println();
        }
        System.out.printf("%d: <%s,%d>", this.tokenId, this.lexeme, getTypeId());
    }
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/09
 * @doc Analyzer
 */
class Analyzer{

    /**
     * counter
     */
    private Integer tokenCount;

    /**
     * current position
     */
    private Integer position;

    /**
     * current row for errortable
     */
    private Integer row;

    /**
     * current column for errortable
     */
    private Integer column;

    /**
     * deal with quotation
     */
    private Integer quotestate;

    /**
     * current string
     */
    private String str;

    /**
     * store all the errors
     */
    private IdentityHashMap<Pair<Integer, Integer>, ErrorMessage> errorList;

    /**
     * record all the keywords
     */
    private String[] keywords = new String[]{
            "auto", "break", "case", "char", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extern", "float", "for", "goto", "if",
            "int", "long", "register", "return",
            "short", "signed", "sizeof", "static", "struct",
            "switch", "typedef", "union", "unsigned", "void",
            "volatile"};

    /**
     * match number string
     */
    private static Pattern pattern = Pattern.compile("[\\+\\-]?\\d+((\\.)(\\d+))?([Ee][\\+\\-]?\\d+)?");
    /**
     * @author Anyan Huang
     * @version 1.0.0
     * @date 2022/11/07
     * @doc the type of each char
     */
    enum charType {
        ALPHABET,
        NUMBER,
        SYMBOL,
        SLASH,
        BLANK,
        QUOTE,
        ENDL,
        STAR,
        DOT,
        EOF;
    }

    /**
     * initialize Analyzer
     *
     * @param str str
     */
    public Analyzer(String str){
        this.position = 0;
        this.quotestate = 0;
        this.tokenCount = 0;
        this.str = str;
        this.column = 1;
        this.row = 1;
    }

    public boolean isKeywords(String str){
        for(int i = 0 ; i < keywords.length; i++){
            if(keywords[i].equals(str)){
                return true;
            }
        }
        return false;
    }

    /**
     * scan char by char
     *
     * @return {@link charType}
     */
    public charType strScanner(){
        //reach the end of a string
        if(position>=str.length()){
            return charType.EOF;
        }
        //get next char
        char next = this.str.charAt(position);
        if (next >= '0' && next <= '9') {
            return charType.NUMBER;
        }
        if (next >= 'A' && next <= 'Z' || next >= 'a' && next <= 'z') {
            return charType.ALPHABET;
        }
        switch (next) {
            case '\n':
                this.row++;
                this.column = 0;
                return charType.ENDL;
            case ' ':
            case '\t':
                column++;
                return charType.BLANK;
            case '.':
                column++;
                return charType.DOT;
            case '*':
                column++;
                return charType.STAR;
            case '/':
                column++;
                return charType.SLASH;
            case '"':
                column++;
                return charType.QUOTE;
            default:
                column++;
                return charType.SYMBOL;
        }
    }

    /**
     * get next char from str and increase position
     *
     * @return char
     */
    public char getChar(){
        return this.str.charAt(position++);
    }

    /**
     * scan token by token to itterate the str
     *
     * @return {@link Token}
     */
    Token nextToken() {
        ErrorMessage errorMessage;
        while (strScanner() == charType.BLANK || strScanner() == charType.ENDL) {
            getChar();
        }
        tokenCount++;
        if (this.quotestate != 0) {
            // Currently in quotes
            return getQuoteOrIdentifier();
        }
        switch (strScanner()) {
            case ALPHABET:
                return getIdentifierOrKeyword();
            case NUMBER:
                return getNumber();
            case SLASH:
                // three possible cases://,/*,/=
                return getCommentOrOperator();
            case DOT:
                //the same with symbol
            case SYMBOL:
                return getOperator();
            case QUOTE:
                return getQuoteOrIdentifier();
            case EOF:
                return new Token(TokenType.END, "[end]", this.tokenCount);
            default:
                errorMessage = new ErrorMessage(String.valueOf(this.str.charAt(position)), ErrorCode.NOT_MATCH);
                errorList.put(new Pair<>(row,column),  errorMessage);
        }
        return new Token(TokenType.END, "[end]", this.tokenCount);
    }

    /**
     * get IDENTIFIER
     *
     * @return {@link Token}
     */
    public Token getQuoteOrIdentifier() {
        StringBuilder out = new StringBuilder();
        Token token = null;
        switch (this.quotestate) {
            case (0):
                out.append(getChar());
                this.quotestate = 1;
                token = new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                break;
            case (1):
                while (strScanner() != charType.QUOTE) {
                    out.append(getChar());
                }
                this.quotestate = 2;
                token = new Token(TokenType.IDENTIFIER, out.toString(), tokenCount);
                break;
            case (2):
                out.append(getChar());
                this.quotestate = 0;
                token = new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                break;
            default:
                break;
        }
        return token;
    }

    /**
     * if current char is an alphabet,we will get either an identifier or a keyword
     *
     * @return {@link Token}
     */
    public Token getIdentifierOrKeyword() {
        StringBuilder content = new StringBuilder();
        while (strScanner() == charType.ALPHABET || strScanner() == charType.NUMBER) {
            content.append(getChar());
        }
        if (isKeywords(content.toString())) {
            return new Token(TokenType.KEYWORDS, content.toString(), tokenCount);
        } else {
            return new Token(TokenType.IDENTIFIER,content.toString(), tokenCount);
        }
    }

    /**
     * get a pure number or number with exponent ending using RE when encountering a char which is a number
     *
     * @return {@link Token}
     */
    public Token getNumber(){
        //"content" is a substring starts with number
        String content = str.substring(position);
        //use re to match legal number,including exponent number
        Matcher matcher = pattern.matcher(content);
        //if a number is legal
        if (matcher.find()) {
            int length = matcher.end()-matcher.start();
            String lexeme = content.substring(0,length);
            position += length;
            row += length;
            return new Token(TokenType.CONSTANT,lexeme,tokenCount);
        }
        else{
            ErrorMessage errorMessage = new ErrorMessage(String.valueOf(this.str.charAt(position)), ErrorCode.NOT_MATCH);
            errorList.put(new Pair<>(row,column),  errorMessage);
        }
        return null;
    }

    /**
     * get comments or an operator when encountering a '/'
     * there are there cases possible://,/*,/=
     *
     * @return {@link Token}
     */
    public Token getCommentOrOperator() {
        StringBuilder out = new StringBuilder();
        //move the position
        out.append(getChar());
        int state = 0;
        while (true) {
            switch (state) {
                //case 0 indicate the second time when meeting a slash
                case 0:
                    switch (str.charAt(position)) {
                        case '=':
                            out.append(getChar());
                            return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                        case '*':
                            // Multiline comment
                            out.append(getChar());
                            state = 2;
                            break;
                        case '/':
                            // Single line comment
                            out.append(getChar());
                            state = 1;
                            break;
                        default:
                            return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                    }
                    break;
                //single line comment
                case 1:
                    if(strScanner()==charType.ENDL) {
                            position++;
                            return new Token(TokenType.COMMENTS, out.toString(), tokenCount);
                    }
                    else{
                        out.append(getChar());
                    }
                    break;
                //multiple line comment
                case 2:
                    if (strScanner()==charType.STAR) {
                        out.append(getChar());
                        state = 3;
                        break;
                    }else{
                    out.append(getChar());
                }
                    break;
                case 3:
                    if (strScanner()==charType.SLASH) {
                        out.append(getChar());
                        return new Token(TokenType.COMMENTS, out.toString(), tokenCount);
                    }else{
                        out.append(getChar());
                        state = 2;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * get all the operators mapping to the map from 33 to 78
     *
     * @return {@link Token}
     */
    public Token getOperator() {
        StringBuilder out = new StringBuilder();
        short state = 0;
        while (true) {
            switch (state) {
                case 0:
                    switch (str.charAt(position)) {
                        case '(':
                        case ')':
                        case ',':
                        case '.':
                        case ':':
                        case ';':
                        case '?':
                        case '[':
                        case ']':
                        case '{':
                        case '}':
                        case '~':
                            out.append(getChar());
                            return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                        default:
                            out.append(getChar());
                            if (strScanner() == charType.SYMBOL) {
                                state = 1;
                            } else {
                                return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                            }
                    }
                    break;
                case 1:
                    switch (str.charAt(position)) {
                        case '-':
                        case '=':
                        case '&':
                        case '|':
                        case '+':
                            out.append(getChar());
                            return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                        default:
                            out.append(getChar());
                            if (strScanner() == charType.SYMBOL) {
                                state = 2;
                            } else {
                                return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                            }
                    }
                    break;
                case 2:
                    out.append(getChar());
                    return new Token(TokenType.OPERATOR, out.toString(), tokenCount);
                default:
                    break;
            }
        }
    }

}

class ErrorMessage {
    private String tokenContent;
    private ErrorCode errorCode;
    private String stage;


    public ErrorMessage(String tokenContent, ErrorCode errorCode) {
        this.tokenContent = tokenContent;
        this.errorCode = errorCode;
        this.stage = "词法分析阶段";
    }

    public ErrorMessage(String tokenContent, ErrorCode errorCode, String stage) {
        this.tokenContent = tokenContent;
        this.errorCode = errorCode;
        this.stage = stage;
    }

    public String getTokenContent() {
        return tokenContent;
    }

    public void setTokenContent(String tokenContent) {
        this.tokenContent = tokenContent;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    @Override
    public String toString() {
        return "WrongMessage{" +
                "tokenContent='" + tokenContent + '\'' +
                ", errorCode=" + errorCode +
                ", stage='" + stage + '\'' +
                '}';
    }
}

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 错误代码
 */
enum ErrorCode {
    INT_GREATER_LIMIT("1001", "Intnumber数值最大不超过2^31"),
    NOT_MATCH("1002", "输入的程序存在无法识别的程序模块"),
    EXPONENT_GREATER_LIMIT("1003", "Exponent类型值数字位不能超过128"),
    REALNUMBER_FORMAT_ERROR("1004", "不是正确的Realnumber格式"),


    MISS_SEMICOLON("2001", "程序段可能遗漏分号"),
    MISS_START_OPENCURLYBRACES("2002", "程序缺失开始的左花括号"),
    MISS_END_CLOSECURLYBRACES("2003", "程序缺失结束的右花括号"),
    EXTRA_VARIABLE_USE("2004", "出现了额外的标识符，请确认此处是否应该存在该变量"),
    MISS_OR_EXTRA_OPENBRACE("2005", "请查看程序是否缺失了左括号或出现了多余的右括号"),
    MISS_OR_EXTRA_CLOSEBRACE("2006", "请查看程序是否缺失了右括号或出现了多余的左括号"),
    EXTRA_SEMICOLON("2007", "程序段可能多了额外的分号"),
    WRONG_TYPE_COMPARE("2008", "非法的操作符比较"),
    NO_MATCH_IFELSE_WRONG("2009", "if-else对不匹配"),
    NOT_REASONABLE_SYMBOL("2010", "不合理的操作符，请确认此处是否存在不合理的输入"),
    EXTRA_EQUAL("2011", "不合理的赋值符号出现，请确认是否多加了‘=’"),
    MISS_OR_EXTRA_OPENCURLYBRACE("2012", "请查看程序是否缺失了左花括号或出现了多余的右花括号"),
    MISS_OR_EXTRA_CLOSECURLYBRACE("2013", "请查看程序是否缺失了左花括号或出现了多余的右花括号"),
    WRONG_GRAMMER_PARSER("2014", "非法的语法匹配"),

    NO_EXIST_VARIABLE("3001", "该变量未定义，请检查是否存在未定义先使用的情况"),
    DIFFERENT_TYPES_WARNINGS1("3002", "不同的数据类型不能赋值"),
    DIFFERENT_TYPES_WARNINGS2("3003", "不同的数据类型不能进行比较");


    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode get(String str) {
        for (ErrorCode e : values()) {
            if (e.getCode().equals(str)){
                return e;
            }
        }
        return null;
    }
}
