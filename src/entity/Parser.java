package entity;

import component.*;
import exception.SyntaxError;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Deque;


/**
 *  [Singleton]
 *  jobs:
 *     - do grammar checking using recursive decent complying with Context Free Grammar
 *     - generate Abstract Syntax Tree for semantic checking, evaluation
 *     - report error message
 */
public class Parser {
    private Deque<Token> queue;
    private Lexer lexer;
    private AST root;

    private static Parser parser = new Parser();

    private Parser() {
        queue = new LinkedList<>();
    }

    public static Parser getParser() { return parser; }

    /**
     *  Deque used to implement forward peeking
     *  abstraction layer occurs
     */
    private Token getNextToken() throws SyntaxError {
        if (queue.isEmpty()) {
            return lexer.getNextToken();
        } else {
            return queue.removeFirst();
        }
    }

    /**
     *  reserved invariant: put checked token used for decision making back into Deque
     */
    private void rollback(Token token) {
        queue.addFirst(token);
    }

    /**
     *  Assertion for token-level checking
     */
    private void Assert(Token token, String type) throws SyntaxError {
        if (!token.getName().equals(type)) {
            lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
            throw new SyntaxError(type + " required but got <" + token.getName() + ", " + token.getValue() + ">.");
        }
    }

    /**
     *  helper method for checking token type
     */
    private boolean checkType(Token token, String type) {
        if (token.getName().equals(type)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  client method
     *  do end checking to make sure all input stream has been consumed
     */
    public void parse(Lexer lexer) throws SyntaxError {
        this.lexer = lexer;
        root = parseProgram();
        endCheck();
    }

    /**
     *  do end checking
     *  error happened when there is any token left
     */
    private void endCheck() throws SyntaxError {
        try {
            getNextToken();
        } catch (Exception ignored) {
            return;
        }
        throw new SyntaxError("Invalid content following '}' at the end of block.");
    }

    /**
     *  return root of constructed AST
     */
    public AST getRoot() { return root; }



    /**
     *   program -> PROGRAM var block
     */
    private AST parseProgram() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "PROGRAM");

        ProgramAST programNode = new ProgramAST();
        programNode.setToken(token);
        programNode.setVarNode(parseVar());

        programNode.setBlockNode(parseBlock());
        return programNode;
    }

    /**
     *   var -> ID
     */
    private AST parseVar() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "ID");

        VarAST varNode = new VarAST();
        varNode.setName((String) token.getValue());
        varNode.setToken(token);
        return varNode;
    }

    /**
     *   block -> LCB statement_list RCB
     */
    private AST parseBlock() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "LCB");

        BlockAST blockNode = new BlockAST();
        List<AST> stmts = parseStatementList();
        token = getNextToken();
        Assert(token, "RCB");
        blockNode.setStmtnode(stmts);
        return blockNode;
    }

    /**
     *   statement_list -> statement
     *                   | statement statement_list
     */
    private List<AST> parseStatementList() throws SyntaxError {
        List<AST> stmts = new ArrayList<>();

        Token token = getNextToken();
        while (!checkType(token, "RCB")) {
            rollback(token);
            stmts.add(parseStatement());

            token = getNextToken();
        }
        rollback(token);
        return stmts;
    }

    /**
     *   statement -> assign_statement
     *              | function_declaration
     *              | function_call
     *              | if_statement
     *              | while_statement
     *              | return_statement
     */
    private AST parseStatement() throws SyntaxError {
        Token token = getNextToken();
        if (checkType(token, "DEF") || checkType(token, "IF") || checkType(token, "WHILE") || checkType(token, "RETURN")) {
            rollback(token);
            if (checkType(token, "DEF")) {
                return parseFunctionDec();
            } else if (checkType(token, "IF")) {
                return parseIfStmt();
            } else if (checkType(token, "WHILE")) {
                return parseWhileStmt();
            } else {
                return parseReturnStmt();
            }
        }
        Assert(token, "ID");
        Token next = getNextToken();
        if (checkType(next, "ASSIGN") || checkType(next, "LP")) {
            rollback(next);
            rollback(token);
            if (checkType(next, "ASSIGN")) {
                return parseAssignStmt();
            } else {
                return parseFunctionCall();
            }
        } else {
            lexer.generatorErrorMsg(next.getLineNo(), next.getColumnNo());
            throw new SyntaxError("Invalid token got: <" + next.getName() + ", " + next.getValue() + ">.");
        }
    }

    /**
     *   assign_statement -> var ASSIGN (expr_complement | lambda_expr)
     */
    private AST parseAssignStmt() throws SyntaxError {
        AssignAST assignNode = new AssignAST();

        assignNode.setVarNode(parseVar());
        Token token = getNextToken();
        Assert(token, "ASSIGN");

        Token next = getNextToken();
        if (checkType(next, "LAMBDA")) {
            rollback(next);
            assignNode.setExprNode(parseLambdaExpr(((VarAST)assignNode.getVarNode()).getToken()));
        } else {
            rollback(next);
            assignNode.setExprNode(parseExprComplement());
        }
        return assignNode;
    }

    /**
     *   function_declaration -> DEF var LP (var (COMMA var)* RP | RP) block
     */
    private AST parseFunctionDec() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "DEF");

        FunctionDelAST functionDelNode = new FunctionDelAST();
        functionDelNode.setVarNode(parseVar());

        token = getNextToken();
        Assert(token, "LP");

        List<AST> params = new ArrayList<>();
        token = getNextToken();
        if (!checkType(token, "RP")) {
            rollback(token);
            params.add(parseVar());
            token = getNextToken();
            while (!checkType(token, "RP")) {
                Assert(token, "COMMA");
                params.add(parseVar());
                token = getNextToken();
            }
        }
        functionDelNode.setParamNode(params);
        functionDelNode.setBlockNode(parseBlock());
        return functionDelNode;
    }

    /**
     *   function_call -> var LP (expr_complement (COMMA expr_complement)* RP | RP)
     */

    private AST parseFunctionCall() throws SyntaxError {
        FunctionCallAST functionCallNode = new FunctionCallAST();
        functionCallNode.setVarNode(parseVar());

        Token token = getNextToken();
        Assert(token, "LP");

        List<AST> exprs = new ArrayList<>();
        token = getNextToken();
        if (!checkType(token, "RP")) {
            rollback(token);
            exprs.add(parseExprComplement());
            token = getNextToken();
            while (!checkType(token, "RP")) {
                Assert(token, "COMMA");
                exprs.add(parseExprComplement());
                token = getNextToken();
            }
        }
        functionCallNode.setExprNode(exprs);
        return functionCallNode;
    }

    /**
     *   if_statement -> IF LP expr_complement RP block ELSE block
     */
    private AST parseIfStmt() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "IF");
        IfStmtAST ifStmtNode = new IfStmtAST();

        token = getNextToken();
        Assert(token, "LP");
        ifStmtNode.setPredicate(parseExprComplement());
        token = getNextToken();
        Assert(token, "RP");


        ifStmtNode.setIf_body(parseBlock());
        token = getNextToken();
        Assert(token, "ELSE");
        ifStmtNode.setElse_body(parseBlock());

        return ifStmtNode;
    }

    /**
     *   while_statement -> WHILE LP expr_complement RP block
     */
    private AST parseWhileStmt() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "WHILE");
        WhileStmtAST whileStmtNode = new WhileStmtAST();

        token = getNextToken();
        Assert(token, "LP");
        whileStmtNode.setPredicate(parseExprComplement());
        token = getNextToken();
        Assert(token, "RP");

        whileStmtNode.setBody(parseBlock());
        return whileStmtNode;
    }


    /**
     *  expr_complement -> expr (GT|LT|GE|LE|EQ|NE) expr
     *                   | expr
     */
    private AST parseExprComplement() throws SyntaxError {
        AST leftOperand = parseExpr();
        Token token = getNextToken();
        if (checkType(token, "GT") || checkType(token, "LT") || checkType(token, "GE") || checkType(token, "LE") || checkType(token, "EQ") || checkType(token, "NE")) {
            DoubleOpAST operator = new DoubleOpAST();
            operator.setOperator(token);
            operator.setLeftOperand(leftOperand);
            operator.setRightOperand(parseExpr());
            leftOperand = operator;
            token = getNextToken();
        }
        rollback(token);
        return leftOperand;
    }

    /**
     *   expr -> term((PLUS|MINUS)term)*
     */
    private AST parseExpr() throws SyntaxError {
        AST leftOperand = parseTerm();
        Token token = getNextToken();
        while (checkType(token, "PLUS") || checkType(token, "MINUS")) {
            DoubleOpAST operator = new DoubleOpAST();
            operator.setOperator(token);
            operator.setLeftOperand(leftOperand);
            operator.setRightOperand(parseTerm());
            leftOperand = operator;
            token = getNextToken();
        }
        rollback(token);
        return leftOperand;
    }

    /**
     *   term -> factor((MULT|DIV|TRUEDIV)factor)*
     */
    private AST parseTerm() throws SyntaxError {
        AST leftOperand = parseFactor();
        Token token = getNextToken();
        while (checkType(token, "MULT") || checkType(token, "DIV") || checkType(token, "TRUEDIV")) {
            DoubleOpAST operator = new DoubleOpAST();
            operator.setOperator(token);
            operator.setLeftOperand(leftOperand);
            AST f = parseFactor();
            operator.setRightOperand(f);
            leftOperand = operator;
            token = getNextToken();
        }
        rollback(token);
        return leftOperand;
    }

    /**
     *   factor -> var
     *           | CONST_INT
     *           | CONST_DOUBLE
     *           | CONST_BOOLEAN
     *           | LP expr RP
     *           | PLUS factor
     *           | MINUS factor
     *           | function_call
     */
    private AST parseFactor() throws SyntaxError {
        Token token = getNextToken();
        if (checkType(token, "ID")) {
            Token next = getNextToken();
            if (checkType(next, "LP")) {
                rollback(next);
                rollback(token);
                return parseFunctionCall();
            } else {
                rollback(next);
                rollback(token);
                return parseVar();
            }
        } else if (checkType(token, "CONST_INT")) {
            return new NumericIntAST((Integer) token.getValue());
        } else if (checkType(token, "CONST_DOUBLE")) {
            return new NumericDoubleAST((Double) token.getValue());
        } else if (checkType(token, "CONST_BOOLEAN")) {
            return new BooleanType((Boolean) token.getValue());
        } else if (checkType(token, "LP")) {
            AST operand = parseExprComplement();
            token = getNextToken();
            Assert(token, "RP");
            return operand;
        } else if (checkType(token, "PLUS") || checkType(token, "MINUS")) {
            SingleOpAST operator = new SingleOpAST();
            operator.setOperator(token);
            operator.setOperand(parseFactor());
            return operator;
        } else {
            lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
            throw new SyntaxError("Unexpected token got: <" + token.getName() + ", " + token.getValue() + ">.");
        }
    }

    /**
     *   return_statement -> RETURN expr
     */
    private AST parseReturnStmt() throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "RETURN");
        ReturnAST returnNode = new ReturnAST();
        returnNode.setExpr(parseExprComplement());
        return returnNode;
    }

    /**
     *   lambda_expr -> LAMBDA LP (var (COMMA var)* RP | RP) COLON body
     */
    private AST parseLambdaExpr(Token t) throws SyntaxError {
        Token token = getNextToken();
        Assert(token, "LAMBDA");
        LambdaExprAST lambdaExprNode = new LambdaExprAST();
        lambdaExprNode.setName((String) t.getValue());
        lambdaExprNode.setToken(t);

        token = getNextToken();
        Assert(token, "LP");

        List<AST> params = new ArrayList<>();
        token = getNextToken();
        if (!checkType(token, "RP")) {
            rollback(token);
            params.add(parseVar());
            token = getNextToken();
            while (!checkType(token, "RP")) {
                Assert(token, "COMMA");
                params.add(parseVar());
                token = getNextToken();
            }
        }
        lambdaExprNode.setParams(params);

        token = getNextToken();
        Assert(token, "COLON");
        lambdaExprNode.setBody(parseBlock());
        return lambdaExprNode;
    }

}
