package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }
    /** 查看下一个字符Token */
    public Token nextToken() throws TokenizeError {
        it.readAll();
        skipSpaceCharacters();
        //文件结尾返回
        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }
        char peek = it.peekChar();
        //无符号整数或浮点数
        if (Character.isDigit(peek)) {
            return lexUIntorDouble();
        }
        //标识符或关键字
        else if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        }
        //字符
        else if (peek == '\'') {
            return lexChar();
        }
        //字符串
        else if (peek == '\"') {
            return lexString();
        }
        //操作符或非法或注释
        else {
            Token peeked = lexOperatorOrUnknown();
            if (peeked.getTokenType() == TokenType.COMMENT) {
                return nextToken();
            }
            else {
                return peeked;
            }
        }
    }

    /** 无符号整数或浮点数 */
    private Token lexUIntorDouble() throws TokenizeError {
        Pos pre = it.currentPos();
        int top = 0, outint = 0;
        double outdouble = 0.0, k = 0.1;
        char op = '+';
        char now[] = new char[1024];
        while (Character.isDigit(it.peekChar())) {
            now[top++] = it.peekChar();
            it.nextChar();
        }
        for (int i = 0; i < top; i++) {
            outint = outint * 10 + now[i] - '0';
        }
        //查看有无'.'判断为无符号整数还是浮点数
        if (it.peekChar() == '.') {
            it.nextChar();
            top = 0;
            outdouble = (double) outint;
            while (Character.isDigit(it.peekChar())) {
                now[top++] = it.peekChar();
                it.nextChar();
            }
            for (int i = 0; i < top; i++) {
                outdouble = outdouble + k * (now[i] - '0');
                k = k / 10;
            }
            //查看有无[eE]
            if (it.peekChar() == 'e' || it.peekChar() == 'E') {
                it.nextChar();
                top = 0;
                outint = 0;
                k = 1.0;
                //查看有无[+-]
                if (it.peekChar() == '+' || it.peekChar() == '-') {
                    op = it.nextChar();
                } else {
                    //无[+-],摸了
                }
                if (Character.isDigit(it.peekChar())) {
                    while (Character.isDigit(it.peekChar())) {
                        now[top++] = it.peekChar();
                        it.nextChar();
                    }
                    for (int i = 0; i < top; i++) {
                        outint = outint * 10 + now[i] - '0';
                    }
                    for (int i = 0; i < outint; i++) {
                        k = k * 10;
                    }
                    if (op == '+') { 
                        outdouble = outdouble * k;
                    } else {
                        outdouble = outdouble / k;
                    }
                }
                //无数字，抛出错误
                else {
                    throw new TokenizeError(ErrorCode.InvalidInput, pre);
                }
            } else {
                //无[eE],摸了
            }
            return new Token(TokenType.DOUBLE_LITERAL, outdouble, pre, it.currentPos());
        } else {
            return new Token(TokenType.UINT_LITERAL, outint, pre, it.currentPos());
        }
    }

    /** 标识符或关键字 */
    private Token lexIdentOrKeyword() throws TokenizeError {
        Pos pre = it.currentPos();
        StringBuilder ob = new StringBuilder("");
        while (Character.isDigit(it.peekChar()) || Character.isAlphabetic(it.peekChar())||it.peekChar() == '_') {
            ob.append(it.peekChar());
            it.nextChar();
        }
        String obs = ob.toString();
        switch (obs.toLowerCase()) {
            case "fn":
                return new Token(TokenType.FN_KW, obs, pre, it.currentPos());
            case "let":
                return new Token(TokenType.LET_KW, obs, pre, it.currentPos());
            case "const":
                return new Token(TokenType.CONST_KW, obs, pre, it.currentPos());
            case "as":
                return new Token(TokenType.AS_KW, obs, pre, it.currentPos());
            case "while":
                return new Token(TokenType.WHILE_KW, obs, pre, it.currentPos());
            case "if":
                return new Token(TokenType.IF_KW, obs, pre, it.currentPos());
            case "else":
                return new Token(TokenType.ELSE_KW, obs, pre, it.currentPos());
            case "return":
                return new Token(TokenType.RETURN_KW, obs, pre, it.currentPos());
            case "break":
                return new Token(TokenType.BREAK_KW, obs, pre, it.currentPos());
            case "continue":
                return new Token(TokenType.CONTINUE_KW, obs, pre, it.currentPos());
            case "int":
                return new Token(TokenType.TY_KW, obs, pre, it.currentPos());
            case "double":
                return new Token(TokenType.TY_KW, obs, pre, it.currentPos());
            case "void":
                return new Token(TokenType.TY_KW, obs, pre, it.currentPos());
        }
        return new Token(TokenType.IDENT, obs, pre, it.currentPos());
    }
    
    /** 操作符或非法 */
    private Token lexOperatorOrUnknown() throws TokenizeError {
        Pos pre = it.currentPos();
        char now = it.nextChar();
        switch (now) {
            case '+':
                return new Token(TokenType.PLUS, '+', pre, it.currentPos());
            case '-':
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", pre, it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', pre, it.currentPos());
            case '*':
                return new Token(TokenType.MUL, '*', pre, it.currentPos());
            case '/':
                if (it.peekChar() == '/') {
                    return lexComment();
                }
                return new Token(TokenType.DIV, '/', pre, it.currentPos());
            case '=':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", pre, it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', pre, it.currentPos());
            case '!':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", pre, it.currentPos());
                }
                else throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
            case '<':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", pre, it.currentPos());
                }
                return new Token(TokenType.LT, '<', pre, it.currentPos());
            case '>':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", pre, it.currentPos());
                }
                return new Token(TokenType.GT, '>', pre, it.currentPos());
            case '(':
                return new Token(TokenType.L_PAREN, '(', pre, it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ')', pre, it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', pre, it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', pre, it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ',', pre, it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ':', pre, it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', pre, it.currentPos());
            default:
                System.out.println(now);
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }
    
    /** 字符 */
    private Token lexChar() throws TokenizeError {
        Pos pre = it.currentPos();
        it.nextChar();
        char obs = '\0';
        //判断单引号后是否是另一个单引号
        if (it.peekChar() == '\'') {
            throw new TokenizeError(ErrorCode.InvalidInput, pre);
        }
        //判断是否为转义字符
        if (it.peekChar() == '\\') {
            it.nextChar();
            obs = it.peekChar();
            switch (obs) {
                case '\\':
                    obs = '\\';
                    it.nextChar();
                    break;
                case '\"':
                    obs = '\"';
                    it.nextChar();
                    break;
                case '\'':
                    obs = '\'';
                    it.nextChar();
                    break;
                case 'n':
                    obs = '\n';
                    it.nextChar();
                    break;
                case 'r':
                    obs = '\r';
                    it.nextChar();
                    break;
                case 't':
                    obs = '\t';
                    it.nextChar();
                    break;
                default:
                    throw new TokenizeError(ErrorCode.InvalidInput, pre);
            }
        }
        else {
            obs = it.nextChar();
        }
        //判断是否为单引号
        if (it.peekChar() == '\'') {
            return new Token(TokenType.CHAR_LITERAL, obs, pre, it.currentPos());
        }
        else {
            throw new TokenizeError(ErrorCode.InvalidInput, pre);
        }
        
    }
    
    /** 字符串 */
    private Token lexString() throws TokenizeError {
        Pos pre = it.currentPos();
        it.nextChar();
        boolean flag=true;
        StringBuilder obs = new StringBuilder("");
        while (flag) {
            switch (it.peekChar()) {
                case '\"':
                    flag = false;
                    break;
                case '\n':
                    throw new TokenizeError(ErrorCode.InvalidInput, pre);
                case '\\': {
                    it.nextChar();
                    char peeked = it.peekChar();
                    switch (peeked) {
                        case '\\':
                            obs.append("\\");
                            it.nextChar();
                            break;
                        case '\"':
                            obs.append("\"");
                            it.nextChar();
                            break;
                        case '\'':
                            obs.append("\'");
                            it.nextChar();
                            break;
                        case 'n':
                            obs.append("\n");
                            it.nextChar();
                            break;
                        case 'r':
                            obs.append("\r");
                            it.nextChar();
                            break;
                        case 't':
                            obs.append("\t");
                            it.nextChar();
                            break;
                        default:
                            throw new TokenizeError(ErrorCode.InvalidInput, pre);
                    }
                    break;
                }
                default:
                    obs.append(it.nextChar());
            }
        }
        it.nextChar();
        System.out.println(obs.toString());
        return new Token(TokenType.STRING_LITERAL, obs, pre, it.currentPos());
    }

    /** 注释 */
    private Token lexComment() throws TokenizeError {
        Pos pre = it.currentPos();
        it.nextChar();
        StringBuilder obs = new StringBuilder("");
        while (it.peekChar() != '\n' && !it.isEOF()) {
            obs.append(it.nextChar());
        }
        return new Token(TokenType.COMMENT, obs, pre, it.currentPos());
    }
    /** 跳过之前的所有空白字符 */
    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
