package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,

    /** ---关键字--- */
    /** 'fn' */
    FN_KW,
    /** 'let' */
    LET_KW,
    /** 'const' */
    CONST_KW,
    /** 'as' */
    AS_KW,
    /** 'while' */
    WHILE_KW,
    /** 'if' */
    IF_KW,
    /** 'else' */
    ELSE_KW,
    /** 'return' */
    RETURN_KW,
    /** 'break' */
    BREAK_KW,
    /** 'continue' */
    CONTINUE_KW,
    /** 'ty' */
    TY_KW,
    
    /** ---字面量--- */
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串常量 */
    STRING_LITERAL,
    /** 浮点数常量 */
    DOUBLE_LITERAL,
    /** 字符常量 */
    CHAR_LITERAL,

    /** ---标识符--- */
    IDENT,

    /** ---运算符--- */
    /** '+' */
    PLUS,
    /** '-' */
    MINUS,
    /** '*' */
    MUL,
    /** '/' */
    DIV,
    /** '=' */
    ASSIGN,
    /** '==' */
    EQ,
    /** '!=' */
    NEQ,
    /** '<' */
    LT,
    /** '>' */
    GT,
    /** '<=' */
    LE,
    /** '>=' */
    GE,
    /** '(' */
    L_PAREN,
    /** ')' */
    R_PAREN,
    /** '{' */
    L_BRACE,
    /** '}' */
    R_BRACE,
    /** '->' */
    ARROW,
    /** ',' */
    COMMA,
    /** ':' */
    COLON,
    /** ';' */
    SEMICOLON,

    /** ---注释--- */
    COMMENT,

    /** ---文件结束--- */
    EOF;
    
    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";    
            /** ---关键字--- */
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";
            case TY_KW:
                return "type";
                
            /** ---字面量--- */
            case UINT_LITERAL:
            	return "UnsignedInteger";
            case STRING_LITERAL:
            	return "String";
            case DOUBLE_LITERAL:
            	return "Double";
            case CHAR_LITERAL:
            	return "Char";
            
            	
            	/** 标识符 */
            case IDENT:
            	return "Identifier";
            	
            	
            	/** 运算符 */
            case PLUS:
            	return "PlusSign";
            case MINUS:
            	return "MinusSign";
            case MUL:
            	return "MultipleSign";
            case DIV:
            	return "DivisionSign";
            case ASSIGN:
            	return "AssignmentSign";
            case EQ:
            	return "EqualSign";
            case NEQ:
            	return "UnEqualSign";
            case LT:
            	return "LessThanSign";
            case GT:
            	return "GreaterThanSign";
            case LE:
            	return "LessOrEqualSign";
            case GE:
            	return "GreaterOrEqualSign";
            case L_PAREN:
            	return "LeftParenthesis";
            case R_PAREN:
            	return "RightParenthesis";
            case L_BRACE:
            	return "LeftBrace";
            case R_BRACE:
            	return "RightBrace";
            case ARROW:
            	return "ArrowSign";
            case COMMA:
            	return "Comma";
            case COLON:
            	return "Colon";
            case SEMICOLON:
            	return "Semicolon";
            	
            	
            	/** 注释 */
            case COMMENT:
            	return "Comment";
            	
            	
            	/** 文件末尾 */
            case EOF:
            	return "EndOfFile";
            	
            default:
                return "InvalidToken";
        }
    }
}
