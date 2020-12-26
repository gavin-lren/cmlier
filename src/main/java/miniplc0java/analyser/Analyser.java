package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.instruction.*;
import miniplc0java.symbol.*;
import miniplc0java.tokenizer.*;
import miniplc0java.util.Pos;

import java.util.*;



public final class Analyser {

    Tokenizer tokenizer;

    public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    public SymbolList symbolList;

    /** 下一个变量的栈偏移 */
    int exprnum = 0;

    /** block的层级 */
    int level;

    int slotloc;
    int slotparam;
    int breakins;
    int continueins;
    Symbol fun;
    SymbolBlock funParam;

    boolean isRet;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
        this.exprnum = 0;
        this.level = 0;
        this.symbolList = new SymbolList();
        this.fun = symbolList.getFun().getValue("_start");
        this.funParam = new SymbolBlock(0);
        this.isRet = false;
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        exprnum++;
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    
    /*
     * <主程序> -> <声明>
     */
    public void analyseProgram() throws CompileError {
        
        analyseDeclaration();
    }

    /**
     * <声明> -> (<声明语句>|<函数声明>)* && <声明语句> -> <let声明>|<const声明>
     *                      ↓
     * <声明> -> (<let声明>|<const声明>|<函数声明>)*
     * @throws CompileError
     */
    private void analyseDeclaration() throws CompileError {
        
        boolean end = true;
        while (end) {
            var peeked = peek();
            switch (peeked.getTokenType()) {
                case LET_KW:
                    analyseLetDec();
                    break;
                case CONST_KW:
                    analyseConDec();
                    break;
                case FN_KW:
                    analyseFunction();
                    break;
                default:
                    if(peeked.getTokenType()==TokenType.EOF)
                        end = false;
                    else
                        throw new AnalyzeError(ErrorCode.ExpectedToken, peeked.getStartPos());
            }
        }
        Symbol main = symbolList.findFun("main");
        if (main == null) {
            throw new AnalyzeError(ErrorCode.NoMain, new Pos(0, 0));
        }
        InstructionList start = symbolList.findFun("_start").getInList();
        start.add(new Instruction(Operation.STACKALLOC, true, main.getSlotret()));
        start.add(new Instruction(Operation.CALL, true, main.getGlobal()));
        if (main.getSlotret() != 0) {
            start.add(new Instruction(Operation.POPN, true, main.getSlotret()));
        }
        //test
        symbolList.print();
    }

    /**
     * <函数声明> -> 'fn' 函数名 '(' <参数列表>? ')' '->' 返回类型 <块语句>
     * @throws CompileError
     */
    public void analyseFunction() throws CompileError {
        expect(TokenType.FN_KW);

        fun = new Symbol(true, symbolList.getFun().getSize(), symbolList.getSize(), -1);
        funParam = new SymbolBlock(0);
        int slotret = 0;
        slotloc = 0;
        slotparam = 0;
        isRet = false;
        breakins = -1;
        continueins = -1;

        Token name = expect(TokenType.IDENT);
        //定义为int double void
        if (name.getValueString().toLowerCase().equals("int")||
            name.getValueString().toLowerCase().equals("double")||
            name.getValueString().toLowerCase().equals("void")) {
                throw new AnalyzeError(ErrorCode.InvalidName, name.getStartPos());
            }
        //重复定义
        if (symbolList.findFun(name.getValueString()) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, name.getStartPos());
        }

        expect(TokenType.L_PAREN);
        var peeked = peek();

        if (peeked.getTokenType()!=TokenType.R_PAREN) {
            analyseFunParList();
        }
        fun.setSlotparam(slotparam);
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        Token type = expect(TokenType.TY_KW);
        fun.setType(type.getValueString());

        if (type.getValueString().toLowerCase().equals("void")) {
            slotret = 0;
        } else {
            slotret = 1;
        }
        fun.setSlotret(slotret);

        symbolList.getFun().putValue(name.getValueString(), fun);

        analyseBlockstmt();

        fun.setSlotloc(slotloc);

        if (!isRet && fun.getType().toLowerCase().equals("void")) {
            fun.inList.add(new Instruction(Operation.RET));
        } else if (!isRet) {
            throw new AnalyzeError(ErrorCode.InvalidReturn, type.getStartPos());
        }
        fun = symbolList.getFun().getValue("_start");
    }

    /**
     * <参数列表> -> <参数> (',' <参数>)*
     * @throws CompileError
     */
    private void analyseFunParList() throws CompileError {
        analyseFunPar();
        var peeked = peek();
        while (peeked.getTokenType() == TokenType.COMMA) {
            expect(TokenType.COMMA);
            analyseFunPar();
            peeked = peek();
        }
    }

    /**
     * <参数> -> 'const'? IDENT ':' 类型
     * @throws CompileError
     */
    private void analyseFunPar() throws CompileError {
        Symbol var = new Symbol(false, -1, -1, slotparam);
        //var.setInitialized(true);

        var peeked = peek();
        if (peeked.getTokenType() == TokenType.CONST_KW) {
            var.setConstant(true);
            expect(TokenType.CONST_KW);
        }

        Token name = expect(TokenType.IDENT);
        if (name.getValueString().toLowerCase().equals("int")||
                name.getValueString().toLowerCase().equals("double")) {
            throw new AnalyzeError(ErrorCode.InvalidName, name.getStartPos());
        }
        
        expect(TokenType.COLON);

        Token type = expect(TokenType.TY_KW);
        if (type.getValueString().toLowerCase().equals("void"))
            throw new AnalyzeError(ErrorCode.InvalidName, type.getStartPos());
        var.setType(type.getValueString());
        var.setParam(true);
        funParam.putValue(name.getValueString(), var);
        slotparam++;
    }

    /**
     * <let声明> -> 'let' IDENT ':' 类型 ('=' <表达式>)? ';'
     * @throws CompileError
     */
    private void analyseLetDec() throws CompileError {
        expect(TokenType.LET_KW);
        Symbol var = new Symbol(false, symbolList.getGloba().getSize(), slotloc, slotparam);
        var.setConstant(false);
        var.setGlobal(level == 0);
        
        Token name = expect(TokenType.IDENT);
        if (name.getValueString().toLowerCase().equals("int")||
                name.getValueString().toLowerCase().equals("double")) {
            throw new AnalyzeError(ErrorCode.InvalidName, name.getStartPos());
        }
        if (symbolList.findDupSymbol(name.getValueString()) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, name.getStartPos());
        }

        expect(TokenType.COLON);
        Token type = expect(TokenType.TY_KW);
        if (type.getValueString().toLowerCase().equals("void"))
            throw new AnalyzeError(ErrorCode.InvalidName, type.getStartPos());
        var.setType(type.getValueString());
        
        if (level == 0) {
            symbolList.getGloba().putValue(name.getValueString(), var);
        } else {
            symbolList.getNow().putValue(name.getValueString(), var);
            slotloc++;
        }


        var peeked = peek();
        if (peeked.getTokenType() == TokenType.ASSIGN) {
            expect(TokenType.ASSIGN);
            if (level == 0) {
                fun.inList.add(new Instruction(Operation.GLOBA,true,var.getGlobal()));
            } else {
                fun.inList.add(new Instruction(Operation.LOCA,true,var.getLocal()));
            }
            if(!var.getType().equals(analyseExprB())){
                throw new AnalyzeError(ErrorCode.InvalidReturn, name.getStartPos());
            }
            fun.inList.add(new Instruction(Operation.STORE_64));
            //var.setInitialized(true);
        }
        expect(TokenType.SEMICOLON);
    }

    /**
     * <const声明> -> 'const' IDENT ':' 类型 '=' <表达式> ';'
     * 
     * @throws CompileError
     */
    private void analyseConDec() throws CompileError {
        expect(TokenType.CONST_KW);
        Symbol var = new Symbol(false, symbolList.getGloba().getSize(), slotloc, slotparam);
        var.setConstant(true);
        var.setGlobal(level == 0);
        
        Token name = expect(TokenType.IDENT);
        if (name.getValueString().toLowerCase().equals("int")||
                name.getValueString().toLowerCase().equals("double")) {
            throw new AnalyzeError(ErrorCode.InvalidName, name.getStartPos());
        }
        if (symbolList.findDupSymbol(name.getValueString()) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, name.getStartPos());
        }

        expect(TokenType.COLON);
        Token type = expect(TokenType.TY_KW);
        if (type.getValueString().toLowerCase().equals("void"))
            throw new AnalyzeError(ErrorCode.InvalidName, type.getStartPos());
        var.setType(type.getValueString());
        
        if (level == 0) {
            symbolList.getGloba().putValue(name.getValueString(), var);
        } else {
            symbolList.getNow().putValue(name.getValueString(), var);
            slotloc++;
        }

        expect(TokenType.ASSIGN);

        if (level == 0) {
            fun.inList.add(new Instruction(Operation.GLOBA,true,var.getGlobal()));
        } else {
            fun.inList.add(new Instruction(Operation.LOCA,true,var.getLocal()));
        }
        if(var.getType().equals(analyseExprB())){
            throw new AnalyzeError(ErrorCode.InvalidReturn, name.getStartPos());
        }
        fun.inList.add(new Instruction(Operation.STORE_64));
        //var.setInitialized(true);

        expect(TokenType.SEMICOLON);
    }

    /**
     * <块语句> -> '{' <语句>* '}'
     * 
     * @throws CompileError
     */
    private void analyseBlockstmt() throws CompileError {
        expect(TokenType.L_BRACE);
        level++;
        symbolList.addSymbolBlock(level);
        symbolList.getFunLoc(fun).cat(funParam);
        funParam.clear();
        
        var peeked=peek();
        while (true) {
            if (peeked.getTokenType() == TokenType.R_BRACE||peeked.getTokenType()==TokenType.EOF) {
                break;
            }
            analyseStmt();
            peeked = peek();
        }
        expect(TokenType.R_BRACE);
        level--;
        if (level != 0) {
            symbolList.removeSymbolBlock();
        }
    }

    /**
     * <语句> -> <表达式语句>|<赋值表达式>|<声明语句>|<if语句>|<while语句>|<返回语句>|<块语句>|<空语句>
     * 
     * @throws CompileError
     */
    private void analyseStmt() throws CompileError {
            var peeked = peek();
            switch (peeked.getTokenType()) {
                // <表达式语句>
                case IDENT:
                case MINUS:
                case L_PAREN:
                case UINT_LITERAL:
                case DOUBLE_LITERAL:
                case STRING_LITERAL:
                    if (!analyseExprA()) {
                        fun.inList.add(new Instruction(Operation.POPN,true,1));
                    }
                    break;
                // <声明语句>
                case LET_KW:
                    analyseLetDec();

                    break;
                case CONST_KW:
                    analyseConDec();
                    break;
                // <if语句>
                case IF_KW:
                    analyseIf();
                    break;
                // <while语句>
                case WHILE_KW:
                    analyseWhile();
                    break;
                // <返回语句>
                case RETURN_KW:
                    analyseReturn();
                    break;
                // <块语句>
                case L_BRACE:
                    analyseBlockstmt();
                    break;
                // <空语句>
                case SEMICOLON:
                    expect(TokenType.SEMICOLON);
                    break;
                case BREAK_KW:
                    analyseBreak();
                    break;
                case CONTINUE_KW:
                    analyseContinue();
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidReturn, peeked.getStartPos());
            }
    }

    /**
     * <if语句> -> 'if' <表达式> <块语句> ('else' (<块语句>|<if语句>))?
     * 
     * @throws CompileError
     */
    private void analyseIf() throws CompileError {
        boolean temp = isRet;
        boolean ifhasret = true;
        isRet = false;

        expect(TokenType.IF_KW);
        analyseExprB();
        fun.inList.add(new Instruction(Operation.BR_TRUE,true,1));
        fun.inList.add(new Instruction(Operation.BR, true, 0));
        int ifsize = fun.inList.size();

        analyseBlockstmt();
        fun.inList.add(new Instruction(Operation.BR, true, 0));
        fun.inList.get(ifsize - 1).setNum_32(fun.inList.size() - ifsize);
        
        if (!isRet) {
            ifhasret = false;
        }
        isRet = false;

        var peeked = peek();
        if (peeked.getTokenType() == TokenType.ELSE_KW) {
            expect(TokenType.ELSE_KW);
            int elsesize = fun.inList.size();

            peeked = peek();
            if (peeked.getTokenType() == TokenType.L_BRACE) {
                analyseStmt();
            } else if (peeked.getTokenType() == TokenType.IF_KW) {
                analyseIf();
            }
            fun.inList.get(elsesize - 1).setNum_32(fun.inList.size() - elsesize);
            peeked = peek();
        }
        if (!isRet) {
            ifhasret = false;
        }
        isRet = temp || ifhasret;
    }

    /**
     * <while语句> -> 'while' <表达式> <块语句>
     * 
     * @throws CompileError
     */
    private void analyseWhile() throws CompileError {
        boolean temp = isRet;
        int tempbreakins = breakins;
        int tempcontinueins = continueins;

        expect(TokenType.WHILE_KW);
        
        continueins = fun.inList.size();
        analyseExprB();

        fun.inList.add(new Instruction(Operation.BR_TRUE, true, 1));
        breakins = fun.inList.size();
        fun.inList.add(new Instruction(Operation.BR, true, 0));        
        int whilesize = fun.inList.size();

        analyseBlockstmt();
        fun.inList.add(new Instruction(Operation.BR, true, 0));
        fun.inList.get(fun.inList.size()-1).setNum_32(continueins - fun.inList.size());
        fun.inList.get(whilesize - 1).setNum_32(fun.inList.size() - whilesize);
        breakins = tempbreakins;
        continueins = tempcontinueins;
        isRet = temp;
    }

    /**
     * <返回语句> -> 'return' <表达式>? ';'
     * 
     * @throws CompileError
     */
    private void analyseReturn() throws CompileError {
        isRet = true;
        String ret;
        Token type = expect(TokenType.RETURN_KW);
        var peeked=peek();
        if (peeked.getTokenType() != TokenType.SEMICOLON) {
            fun.inList.add(new Instruction(Operation.ARGA, 0));
            ret = analyseExprB();
            fun.inList.add(new Instruction(Operation.STORE_64));
        } else {
            ret = "void";
            expect(TokenType.SEMICOLON);
        }
        if (!ret.equals(fun.getType())) {
            throw new AnalyzeError(ErrorCode.InvalidReturn, type.getStartPos());
        }
        fun.inList.add(new Instruction(Operation.RET));
    }

    private void analyseContinue() throws CompileError {
        expect(TokenType.CONTINUE_KW);
        if (continueins == -1) {
            throw new AnalyzeError(ErrorCode.NoError, peek().getStartPos());
        }
        fun.inList.add(new Instruction(Operation.BR, true, 0));
        fun.inList.get(fun.inList.size() - 1).setNum_32(continueins - fun.inList.size());
        expect(TokenType.SEMICOLON);
    }

    private void analyseBreak()throws CompileError {
        expect(TokenType.BREAK_KW);
        if (continueins == -1) {
            throw new AnalyzeError(ErrorCode.NoError, peek().getStartPos());
        }
        fun.inList.add(new Instruction(Operation.BR, true, 0));
        fun.inList.get(fun.inList.size() - 1).setNum_32(breakins - fun.inList.size());
        expect(TokenType.SEMICOLON);
    }
    /**
     * <exprA> -> <exprB> ('=' <exprB>)?
     * @throws CompileError
     */
    private boolean analyseExprA() throws CompileError {
        exprnum = 0;
        var peeked = peek();
        String ret = analyseExprB();
        var now = peek();
        if (now.getTokenType() == TokenType.ASSIGN) {
            if (peeked.getTokenType() != TokenType.IDENT || exprnum != 1) {
                throw new AnalyzeError(ErrorCode.ExpectedToken, now.getStartPos());
            }
            expect(TokenType.ASSIGN);

            fun.inList.pop();
            Symbol var = symbolList.find(peeked.getValueString());
            if (var == null || var.isFunOrVar() == true) {
                throw new AnalyzeError(ErrorCode.NotDeclared, peeked.getStartPos());
            }
            if (!var.getType().equals(analyseExprB())) {
                throw new AnalyzeError(ErrorCode.NotDeclared, peeked.getStartPos());
            }

            fun.inList.add(new Instruction(Operation.STORE_64));
            var.setInitialized(true);
            return true;
        }
        if (ret.equals("int") || ret.equals("double")) {
            return false;
        }
        return true;

    }
    
    /**
     * <exprB> -> <xprC> ('=='|'!='|'<'|'>'|'<='|'>=' <exprC>)?
     * @throws CompileError
     */
    private String analyseExprB() throws CompileError {
        String ret = analyseExprC();
        String flag;
        var peeked = peek();
        switch (peeked.getTokenType()) {
            case EQ:
                expect(TokenType.EQ);
                flag = analyseExprC();
                break;
            case NEQ:
                expect(TokenType.NEQ);
                flag = analyseExprC();
                break;
            case LT:
                expect(TokenType.LT);
                flag = analyseExprC();
                break;
            case GT:
                expect(TokenType.GT);
                flag = analyseExprC();
                break;
            case LE:
                expect(TokenType.LE);
                flag = analyseExprC();
                break;
            case GE:
                expect(TokenType.GE);
                flag = analyseExprC();
                break;
            default:
                return ret;
        }
        if (!ret.equals(flag)) {
            throw new AnalyzeError(ErrorCode.InvalidReturn, peeked.getStartPos());
        }
        
        switch (ret) {
            case "int":
                fun.inList.add(new Instruction(Operation.CMP_I));
                break;
            case "double":
                fun.inList.add(new Instruction(Operation.CMP_F));
                break;
            default:
                throw new AnalyzeError(ErrorCode.InvalidReturn, peeked.getStartPos());
        }
        switch (peeked.getTokenType()) {
            case EQ:
                fun.inList.add(new Instruction(Operation.NOT));
                break;
            case NEQ:
                break;
            case LT:
                fun.inList.add(new Instruction(Operation.SET_LT));
                break;
            case GT:
                fun.inList.add(new Instruction(Operation.SET_GT));
                break;
            case LE:
                fun.inList.add(new Instruction(Operation.SET_GT));
                fun.inList.add(new Instruction(Operation.NOT));
                break;
            case GE:
                fun.inList.add(new Instruction(Operation.SET_LT));
                fun.inList.add(new Instruction(Operation.NOT));
                break;
            default:
        }
        return ret;
    }

    /**
     * <exprC> -> <exprD> { '+' | '-' <exprD>}*
     * @throws CompileError
     */
    private String analyseExprC() throws CompileError {
        
        String ret = analyseExprD();
        String flag;
        Token name;
        var peeked = peek();
        while (peeked.getTokenType() == TokenType.PLUS || peeked.getTokenType() == TokenType.MINUS) {
            if (peeked.getTokenType() == TokenType.PLUS)
                name = expect(TokenType.PLUS);
            else
                name = expect(TokenType.MINUS);
            flag = analyseExprE();
            if (!ret.equals(flag)) {
                throw new AnalyzeError(ErrorCode.InvalidReturn, name.getStartPos());
            }

            switch (flag) {
                case "int":
                    if (name.getTokenType() == TokenType.PLUS) {
                        fun.inList.add(new Instruction(Operation.ADD_I));
                    } else {
                        fun.inList.add(new Instruction(Operation.SUB_I));
                    }
                    break;
                case "double":
                    if (name.getTokenType() == TokenType.PLUS) {
                        fun.inList.add(new Instruction(Operation.ADD_F));
                    } else {
                        fun.inList.add(new Instruction(Operation.SUB_F));
                    }
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidReturn, name.getStartPos());
            }
            peeked = peek();
        }
        return ret;
    }

    /**
     * <exprD> -> <exprD> { '*' | '/' <exprE>}
     * @throws CompileError
     */
    private String analyseExprD() throws CompileError {
        String ret = analyseExprE();
        String flag;
        Token name;
        var peeked = peek();
        while (peeked.getTokenType() == TokenType.MUL || peeked.getTokenType() == TokenType.DIV) {
            if (peeked.getTokenType() == TokenType.MUL)
                name = expect(TokenType.MUL);
            else
                name = expect(TokenType.DIV);
            flag = analyseExprE();
            if (!ret.equals(flag)) {
                throw new AnalyzeError(ErrorCode.InvalidReturn, name.getStartPos());
            }

            switch (flag) {
                case "int":
                    if (name.getTokenType() == TokenType.MUL) {
                        fun.inList.add(new Instruction(Operation.MUL_I));
                    } else {
                        fun.inList.add(new Instruction(Operation.DIV_I));
                    }
                    break;
                case "double":
                    if (name.getTokenType() == TokenType.MUL) {
                        fun.inList.add(new Instruction(Operation.MUL_F));
                    } else {
                        fun.inList.add(new Instruction(Operation.DIV_F));
                    }
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidReturn, name.getStartPos());
            }
            peeked = peek();
        }
        return ret;
    }

    /**
     * <exprE> -> <exprF> ( 'as' INT | DOUBLE)?
     * @throws CompileError
     */
    private String analyseExprE() throws CompileError {
        String ret = analyseExprF();
        var peeked = peek();
        while (peeked.getTokenType() == TokenType.AS_KW) {
            expect(TokenType.AS_KW);
            Token name = expect(TokenType.IDENT);
            switch (name.getValueString().toLowerCase()) {
                case "int":
                    if (ret.equals("double")) {
                        fun.inList.add(new Instruction(Operation.FTOI));
                        ret = "int";
                    }
                    break;
                case "double":
                    if (ret.equals("int")) {
                        fun.inList.add(new Instruction(Operation.ITOF));
                    }
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.ExpectedToken, name.getStartPos());
            }
            peeked = peek();
        }
        return ret;
    }

    /**
     * <exprF> -> ('-')? <exprG>
     * @throws CompileError
     */
    private String analyseExprF() throws CompileError {
        String ret;
        Boolean flag = false;
        var peeked = peek();
        while (peeked.getTokenType() == TokenType.MINUS) {
            flag = !flag;
            expect(TokenType.MINUS);
            peeked = peek();
        }
        ret = analyseExprG();
        if (flag) {
            switch (ret) {
                case "int":
                    fun.inList.add(new Instruction(Operation.NEG_I));
                    break;
                case "double":
                    fun.inList.add(new Instruction(Operation.NEG_F));
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidReturn, peeked.getStartPos());
            }
        }
        return ret;
    }

    /**
     * <exprG> -> INT | STRING | DOUBLE | '(' <exprB> ')' | 
     *            IDENT ('(' call_param_list? ')')?
     * @throws CompileError
     */
    private String analyseExprG() throws CompileError {
        
        var peeked = peek();
        switch (peeked.getTokenType()) {
            case UINT_LITERAL:
                expect(TokenType.UINT_LITERAL);
                long int64 = Long.valueOf(peeked.getValue().toString());
                
                fun.inList.add(new Instruction(Operation.PUSH, false, int64));
                return "int";
            case STRING_LITERAL:
                expect(TokenType.STRING_LITERAL);
                Symbol string=symbolList.getString().getValue(peeked.getValueString());
                if(string ==null){
                    string = new Symbol(false, symbolList.getString().getSize(), -1, -1);
                    string.setGlobal(symbolList.getString().getSize());
                    symbolList.getString().putValue(peeked.getValueString(), string);
                }
                Instruction ins=new Instruction(Operation.PUSH,false,(long)(string.getGlobal()));
                ins.setIsreloc(true);
                fun.inList.add(ins);
                return "int";
            case DOUBLE_LITERAL:
                expect(TokenType.DOUBLE_LITERAL);
                double double64 = (double) (peeked.getValue());
                fun.inList.add(new Instruction(Operation.PUSH,double64));
                return "double";
            case CHAR_LITERAL:
                expect(TokenType.CHAR_LITERAL);
                int ch =(int)(peeked.getValue());
                fun.inList.add(new Instruction(Operation.PUSH,false,(long)ch));
            case L_PAREN:
                expect(TokenType.L_PAREN);
                String ret=analyseExprB();
                expect(TokenType.R_PAREN);
                return ret;
            case IDENT:
                Token name=expect(TokenType.IDENT);
                var nextpeeked = peek();
                
                if (nextpeeked.getTokenType() == TokenType.L_PAREN) {
                    expect(TokenType.L_PAREN);

                    Symbol funG =symbolList.findFun(name.getValueString());
                    if(funG==null){
                        throw new AnalyzeError(ErrorCode.NotDeclared, name.getStartPos());
                    }
                    if(funG.getGlobal()==0){
                        throw new AnalyzeError(ErrorCode.InvalidName, name.getStartPos());
                    }
                    fun.inList.add(new Instruction(Operation.STACKALLOC,true ,funG.getSlotret()));

                    boolean flag=false;
                    for(Map.Entry<String,Symbol>entry:symbolList.getFunLoc(funG).getSymbolBlock().entrySet()){
                        Symbol var =entry.getValue();
                        if (!var.isParam()) {
                            break;
                        }
                        
                        if(flag){
                            expect(TokenType.COMMA);
                        }
                        flag=true;
                        if(!var.getType().equals(analyseExprB())){
                            throw new AnalyzeError(ErrorCode.InvalidReturn, peeked.getStartPos());
                        }
                    }
                    expect(TokenType.R_PAREN);
                    if(funG.getGlobal()<9){
                        ins=new Instruction(Operation.CALLNAME,true,funG.getGlobal());
                        ins.setIsreloc(true);
                        fun.inList.add(ins);
                    }else{
                        fun.inList.add(new Instruction(Operation.CALL,true,funG.getGlobal()));
                    }

                    return funG.getType();
                } 
                else {
                    Symbol var = symbolList.find(name.getValueString());
                    if (var == null || var.isFunOrVar() == true) {
                        throw new AnalyzeError(ErrorCode.NotDeclared, name.getStartPos());
                    }
                    if(var.isGlobal()){
                        fun.inList.add(new Instruction(Operation.GLOBA,true,var.getGlobal()));
                    }else if(!var.isParam()){
                        fun.inList.add(new Instruction(Operation.LOCA,true,var.getLocal()));
                    } else {
                        fun.inList.add(new Instruction(Operation.ARGA, true,
                                var.getParam() + (fun.getType().equals("void") ? 0 : 1)));
                    }
                    
                    fun.inList.add(new Instruction(Operation.LOAD_64));
                    return var.getType();
                }
            default:
                throw new AnalyzeError(ErrorCode.ExpectedToken, peeked.getStartPos());
        }
    }
}
