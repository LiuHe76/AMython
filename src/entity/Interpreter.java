package entity;

import exception.NameError;
import exception.SyntaxError;
import component.*;
import exception.TypeError;
import exception.ZeroDivisionError;

import java.util.*;

/**
 *  [Singleton]
 *  jobs:
 *     - interpret input program
 *     - do semantic checking
 *        - checking variable existence
 *        - checking matching between formal parameters and real parameters
 *        - division by zero
 */
public class Interpreter {
    private static Interpreter interpreter = new Interpreter();
    private static Parser parser = Parser.getParser();
    private static Lexer lexer = Lexer.getLexer();
    private static Frame globalFrame = new Frame(0, null);
    private static Stack stack = new Stack();

    static {
        BuiltinFunctionInit();
    }

    /**
     *  AST traversing process will always be based on the frame(active) standing at the top of the stack
     *  stand Stack data structure
     */
    private static class Stack {
        private LinkedList<Frame> stack = new LinkedList<>();

        public void pop() {
            stack.removeFirst();
        }

        public void push(Frame frame) {
            stack.addFirst(frame);
        }

        public Frame peek() {
            return stack.getFirst();
        }
    }

    /**
     *  Entity class used to store name-value pair and track its parent frame
     *  support put and lookup method
     */
    private static class Frame {
        private int level;
        private Map<String, Object> mapping;
        private Frame parent;

        public Frame(int level, Frame parent) {
            this.level = level;
            this.parent = parent;
            mapping = new HashMap<>();
        }

        private void put(String name, Object val) {
            mapping.put(name, val);
        }

        private Object lookup(Token token) throws NameError {
            Frame f = this;
            String name = (String) token.getValue();
            while (f != null) {
                if (f.mapping.containsKey(name)) {
                    return f.mapping.get(name);
                }
                f = f.parent;
            }
            lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
            throw new NameError("name '" + name + "' not found.");
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("level: ").append(level).append("\n");
            for (Map.Entry<String, Object> entry : mapping.entrySet()) {
                sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }
            return sb.toString();
        }
    }

    private Interpreter() {}

    public static Interpreter getInterpreter() { return interpreter; }

    /**
     *  client method
     *  start to interpret input program through visiting each node in the AST
     */
    public Object interpret(String source) throws SyntaxError, ZeroDivisionError, NameError, TypeError {
        lexer.input(source);
        parser.parse(lexer);
        return visit(parser.getRoot());
    }

    /**
     *  visiting entrance to dispatcher
     */
    private Object visit(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        return dispatcher(node);
    }

    /**
     *  core method to call corresponding visit_method based on the node type
     */
    private Object dispatcher(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        if (node instanceof AssignAST) {
            return visitAssignAST(node);
        } else if (node instanceof BlockAST) {
            return visitBlockAST(node);
        } else if (node instanceof DoubleOpAST) {
            return visitDoubleOpAST(node);
        } else if (node instanceof FunctionCallAST) {
            return visitFunctionCallAST(node);
        } else if (node instanceof FunctionDelAST) {
            return visitFunctionDelAST(node);
        } else if (node instanceof NumericDoubleAST) {
            return visitNumericDoubleAST(node);
        } else if (node instanceof NumericIntAST) {
            return visitNumericIntAST(node);
        }  else if (node instanceof BooleanType) {
            return visitBooleanType(node);
        } else if (node instanceof ProgramAST) {
            return visitProgramAST(node);
        } else if (node instanceof SingleOpAST) {
            return visitSingleOpAST(node);
        } else if (node instanceof VarAST) {
            return visitVarAST(node);
        } else if (node instanceof ReturnAST) {
            return visitReturnAST(node);
        } else if (node instanceof IfStmtAST) {
            return visitIfStmtAST(node);
        } else if (node instanceof WhileStmtAST) {
            return visitWhileStmtAST(node);
        } else if (node instanceof LambdaExprAST) {
            return visitLambdaExprAST(node);
        }
        return -1;
    }

    /**
     *  visit Assign node:
     *    do assigning operation
     *    return nothing
     */
    private Object visitAssignAST(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        AssignAST assignNode = (AssignAST)node;
        Frame frame = stack.peek();
        String name = ((VarAST)assignNode.getVarNode()).getName();
        Object val = visit(assignNode.getExprNode());
        frame.put(name, val);
        return null;
    }

    /**
     *  visit Block node:
     *    visit each statement node
     *    keep track of return value of each statement, return the last one
     */
    private Object visitBlockAST(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        BlockAST blockNode = (BlockAST)node;
        Object res = null;
        for (AST stmt : blockNode.getStmtnode()) {
            res = visit(stmt);
        }
        return res;
    }

    /**
     *  DoubleOp node
     */
    /**
     *  helper method converting True to 1, False to 0
     */
    private int boolean_to_int(Object o) {
        if (o.equals(true)) {
            return 1;
        } else {
            return 0;
        }
    }


    /**
     *  do corresponding operation for two double operands
     */
    private Object double_ops(String operator, double left, double right) {
        if (operator.equals("MULT")) {
            return left * right;
        } else if (operator.equals("DIV") || operator.equals("TRUEDIV")) {
            return left / right;
        } else if (operator.equals("PLUS")) {
            return left + right;
        } else if (operator.equals("MINUS")){
            return left - right;
        } else if (operator.equals("GT")) {
            return left > right;
        } else if (operator.equals("LT")) {
            return left < right;
        } else if (operator.equals("GE")) {
            return left >= right;
        } else if (operator.equals("LE")) {
            return left <= right;
        } else if (operator.equals("EQ")) {
            return left == right;
        } else {
            return left != right;
        }
    }

    /**
     *  do corresponding operation for two int operands
     */
    private Object int_ops(String operator, int left, int right, DoubleOpAST node) throws ZeroDivisionError {
        if (operator.equals("MULT")) {
            return left * right;
        } else if (operator.equals("DIV")) {
            try {
                return left / right;
            } catch (Exception e) {
                lexer.generatorErrorMsg(node.getOperator().getLineNo(), node.getOperator().getColumnNo());
                throw new ZeroDivisionError("division by zero.");
            }
        } else if (operator.equals("TRUEDIV")) {
            return (double) left / right;
        } else if (operator.equals("PLUS")) {
            return left + right;
        } else if (operator.equals("MINUS")){
            return left - right;
        } else if (operator.equals("GT")) {
            return left > right;
        } else if (operator.equals("LT")) {
            return left < right;
        } else if (operator.equals("GE")) {
            return left >= right;
        } else if (operator.equals("LE")) {
            return left <= right;
        } else if (operator.equals("EQ")) {
            return left == right;
        } else {
            return left != right;
        }
    }

    /**
     *  visit DoubleOp node:
     *    do type converting for two operands
     *    apply operator to two operands, return the gotten result
     */
    private Object visitDoubleOpAST(AST node) throws ZeroDivisionError, NameError, SyntaxError, TypeError {
        DoubleOpAST doubleOpNode = (DoubleOpAST)node;
        String operator = doubleOpNode.getOperator().getName();
        Object leftOperand = visit(doubleOpNode.getLeftOperand());
        Object rightOperand = visit(doubleOpNode.getRightOperand());
        if (leftOperand instanceof Number && rightOperand instanceof Number) {
            if (leftOperand instanceof Double || rightOperand instanceof Double) {
                double left = leftOperand instanceof Double ? (double) leftOperand : (int) leftOperand;
                double right = rightOperand instanceof Double ? (double) rightOperand : (int) rightOperand;
                return double_ops(operator, left, right);
            } else {
                int left = (int) leftOperand;
                int right = (int) rightOperand;
                return int_ops(operator, left, right, doubleOpNode);
            }
        } else {
            if (leftOperand instanceof Double || rightOperand instanceof Double) {
                double left = leftOperand instanceof Double ? (double)leftOperand : boolean_to_int(leftOperand);
                double right = rightOperand instanceof Double ? (double)rightOperand : boolean_to_int(rightOperand);
                return double_ops(operator, left, right);
            } else {
                int left = leftOperand instanceof Integer ? (int)leftOperand : boolean_to_int(leftOperand);
                int right = rightOperand instanceof Integer ? (int)rightOperand : boolean_to_int(rightOperand);
                return int_ops(operator, left, right, doubleOpNode);
            }
        }
    }

    /**
     *  visit FunctionCall node:
     *    eval function name and parameters passed into, eval function body and return gotten result
     */
    /**
     *  tracing back looking for value bound to name starting from current frame(active)\
     *  report error when name not found
     */
    private Frame getParentFrame(String name) {
        Frame frame = stack.peek();
        while (frame != null) {
            if (frame.mapping.containsKey(name)) {
                return frame;
            }
            frame = frame.parent;
        }
        return null;
    }

    /**
     *  bind real parameters passed into to formal parameters in the current frame(active)
     *  report error when number of parameters mismatch
     */
    private void formalParamsInit(Frame frame, List<AST> formalParams, List<Object> vals, Token token) throws SyntaxError {
        if (formalParams.size() != vals.size()) {
            lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
            throw new SyntaxError("number of formal params should match with real params, required " + formalParams.size() + ", but got " + vals.size() + ".");
        }
        for (int i = 0; i < formalParams.size(); i += 1) {
            frame.put(((VarAST)formalParams.get(i)).getName(), vals.get(i));
        }
    }

    /**
     *  core method for function call execution
     *  dispatch based on function type to built-in functions, user-defined function, lambda expression
     */
    private Object visitFunctionCallAST(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        FunctionCallAST functionCallNode = (FunctionCallAST)node;
        Object findRes = visit(functionCallNode.getVarNode());
        if (!(findRes instanceof LambdaExprAST) && !(findRes instanceof FunctionDelAST) && (!(findRes instanceof String) || !(((String)findRes).startsWith("$")))) {
            Token token = ((VarAST)functionCallNode.getVarNode()).getToken();
            lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
            throw new TypeError(((VarAST) functionCallNode.getVarNode()).getName() + " is not callable.");
        }

        List<Object> vals = new ArrayList<>();
        for (int i = 0; i < functionCallNode.getExprNode().size(); i += 1) {
            vals.add(visit(functionCallNode.getExprNode().get(i)));
        }
        if (findRes instanceof String) {
            return BuiltinFunctionDispatcher(((VarAST)functionCallNode.getVarNode()).getToken(), vals);
        } else if (findRes instanceof FunctionDelAST){
            FunctionDelAST declaredfunction = (FunctionDelAST) findRes;
            Frame parent = getParentFrame(((VarAST) declaredfunction.getVarNode()).getName());
            Frame frame = new Frame(parent.level + 1, parent);
            formalParamsInit(frame, declaredfunction.getParamNode(), vals, ((VarAST) functionCallNode.getVarNode()).getToken());
            stack.push(frame);
            Object res = visit(declaredfunction.getBlockNode());
            stack.pop();
            return res;
        } else {
            LambdaExprAST lambdaExprNode = (LambdaExprAST)findRes;
            Frame parent = getParentFrame(lambdaExprNode.getName());
            Frame frame = new Frame(parent.level + 1, parent);
            formalParamsInit(frame, lambdaExprNode.getParams(), vals, lambdaExprNode.getToken());
            stack.push(frame);
            Object res = visit(lambdaExprNode.getBody());
            stack.pop();
            return res;
        }
    }

    /**
     *  visit FunctionDec node:
     *    bound function body to its name in the current frame(active)
     *    return nothing
     */
    private Object visitFunctionDelAST(AST node) {
        FunctionDelAST functionDelNode = (FunctionDelAST)node;
        Frame frame = stack.peek();
        String name = ((VarAST)functionDelNode.getVarNode()).getName();
        frame.put(name, node);
        return null;
    }

    /**
     *  visit Numeric(double) node:
     *    return its val attr
     */
    private Object visitNumericDoubleAST(AST node) {
        return ((NumericDoubleAST)node).getVal();
    }

    /**
     *  visit Numeric(int) node:
     *    return its val attr
     */
    private Object visitNumericIntAST(AST node) {
        return ((NumericIntAST)node).getVal();
    }

    /**
     *  visit Boolean node:
     *    return its val attr
     */
    private Object visitBooleanType(AST node) {
        return ((BooleanType)node).getVal();
    }

    /**
     *  visit Program node:
     *    program logic flows to visit program block
     *    return nothing
     */
    private Object visitProgramAST(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        ProgramAST programNode = (ProgramAST)node;
        stack.push(globalFrame);
        globalFrame.put(((VarAST)programNode.getVarNode()).getName(), "CASE TEST");
        visit(programNode.getBlockNode());
        stack.pop();
        return null;
    }

    /**
     *  visit SingleOp node:
     *    apply "+/-" to its operand, return gotten result
     */
    private Object visitSingleOpAST(AST node) throws ZeroDivisionError, SyntaxError, NameError, TypeError {
        SingleOpAST singleOpNode = (SingleOpAST)node;
        Token token = singleOpNode.getOperator();
        Object val = visit(singleOpNode.getOperand());
        if (token.getName().equals("PLUS")) {
            if (val instanceof Double) {
                return (double)val;
            } else {
                return val instanceof Integer ? (int)val : boolean_to_int(val);
            }
        } else {
            if (val instanceof Double) {
                return -(double)val;
            } else {
                return val instanceof Integer ? -(int)val : -boolean_to_int(val);
            }
        }
    }

    /**
     *  visit Variable node:
     *    return value bound to name in the latest frame
     *    report error when name not found
     */
    private Object visitVarAST(AST node) throws NameError {
        VarAST var = (VarAST)node;
        Token token = var.getToken();
        Frame frame = stack.peek();
        Object val = frame.lookup(token);
        return val;
    }

    /**
     *  visit Return node:
     *    return value of expr returned
     */
    private Object visitReturnAST(AST node) throws NameError, ZeroDivisionError, SyntaxError, TypeError {
        ReturnAST returnNode = (ReturnAST)node;
        return visit(returnNode.getExpr());
    }

    /**
     *  FunctionCall node branch: built-in functions
     *  initialization for built-in function by putting name-$name pair in the global frame
     */
    private static void BuiltinFunctionInit() {
        String[] function_list = new String[] {"print", "min", "max", "abs", "sum"};
        for (String function : function_list) {
            globalFrame.put(function, "$"+function);
        }
    }

    /**
     *  built-in functions dispatcher for executing corresponding function based on called name
     *  check validity based on corresponding rule of called function
     */
    private Object BuiltinFunctionDispatcher(Token token, List<Object> vals) throws TypeError {
        String name = (String) token.getValue();
        if (name.equals("print")) {
            StringBuffer sb = new StringBuffer();
            for (Object o : vals) {
                sb.append(o).append(" ");
            }
            System.out.println(sb.toString());
            return null;
        } else if (name.equals("min")) {
            if (vals.size() < 1) {
                lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
                throw new TypeError("min requires at least 1 argument but got 0.");
            }
            int idx = 0;
            double min = vals.get(idx) instanceof Double ? (double)vals.get(idx) : (int)vals.get(idx);
            for (int i = 1; i < vals.size(); i += 1) {
                double other = vals.get(i) instanceof Double ? (double)vals.get(i) : (int) vals.get(i);
                if (other < min) {
                    min = other;
                    idx = i;
                }
            }
            return vals.get(idx);
        } else if (name.equals("max")) {
            if (vals.size() < 1) {
                lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
                throw new TypeError("max requires at least 1 argument but got 0.");
            }
            int idx = 0;
            double max = vals.get(idx) instanceof Double ? (double)vals.get(idx) : (int)vals.get(idx);
            for (int i = 1; i < vals.size(); i += 1) {
                double other = vals.get(i) instanceof Double ? (double)vals.get(i) : (int) vals.get(i);
                if (other > max) {
                    max = other;
                    idx = i;
                }
            }
            return vals.get(idx);
        } else if (name.equals("sum")) {
            if (vals.size() < 1) {
                lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
                throw new TypeError("sum requires at least 1 argument but got 0.");
            }
            double acc = 0;
            int count = 0;
            for (Object o : vals) {
                if (o instanceof Double) {
                    count += 1;
                    acc += (double)o;
                } else {
                    acc += (int)o;
                }
            }
            if (count > 0) {
                return acc;
            } else {
                return (int)acc;
            }
        } else if (name.equals("abs")){
            if (vals.size() != 1) {
                lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
                throw new TypeError("abs requires exact 1 argument but got " + vals.size() + ".");
            }
            Object o = vals.get(0);
            if (o instanceof Double) {
                return Math.abs((double)o);
            } else {
                return Math.abs((int)o);
            }
        } else {
            lexer.generatorErrorMsg(token.getLineNo(), token.getColumnNo());
            throw new TypeError("Not defined built-in function.");
        }
    }

    /**
     *  visit If-else node:
     *    return value of if-block or else-block based on the value of predicate
     */
    /**
     *  helper method converting basic data type to boolean type
     */
    private boolean convertToBoolean(Object o) {
        if (o instanceof Boolean) {
            return (boolean)o;
        } else if (o instanceof Integer) {
            return (int) o != 0;
        } else {
            return (double) o != 0;
        }
    }

    /**
     *  core method for visiting is-else statement node
     */
    private Object visitIfStmtAST(AST node) throws ZeroDivisionError, TypeError, SyntaxError, NameError {
        IfStmtAST ifStmtNode = (IfStmtAST)node;
        Object predicate = visit(ifStmtNode.getPredicate());

        if (convertToBoolean(predicate)) {
            return visit(ifStmtNode.getIf_body());
        } else {
            return visit(ifStmtNode.getElse_body());
        }
    }


    /**
     *  visit While loop node:
     *    keep visiting while body until the evaluated value of its predicate is false
     *    return nothing
     */
    private Object visitWhileStmtAST(AST node) throws ZeroDivisionError, TypeError, SyntaxError, NameError {
        WhileStmtAST whileStmtNOde = (WhileStmtAST)node;
        while (convertToBoolean(visit(whileStmtNOde.getPredicate()))) {
            visit(whileStmtNOde.getBody());
        }
        return null;
    }

    /**
     *  visit lambda node:
     *    return itself
     */
    private Object visitLambdaExprAST(AST node) {
        return node;
    }

}
