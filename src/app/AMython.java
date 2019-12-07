package app;

/**
 * ##########################################################################################################################
 *  AMython is a customized interpreter for Python-like script programming language. The language itself is highly aligned
 *  with Python grammar in almost all the aspects: built-in data type, statement, expression, etc. Only small subset of
 *  functionality is implemented following the functional programming paradigm. Object-oriented programming module is to be
 *  constructed in the future.
 * ##########################################################################################################################
 *  Conventions compared with Python:
 *    1. The script file should follow <'PROGRAM' 'FILENAME' body '.'> pattern.(Usage in the terminal: java AMython filename)
 *    2. Statement block should be put within '{' and '}' in any case such as function declaration, if-elseif-else statement,
 *       while/for loop, etc.
 *    3. Comment format: SLASH STAR     STAR SLASH
 * ##########################################################################################################################
 *  Grammar will be introduced following Context-free Grammar definition:
 *  {A little description:
 *    uppercase element showed everywhere below stands for terminal(T)
 *    lowercase element stands for non-terminal(N)}
 * -------------------------------------------------------------------------------
 *  1. program -> PROGRAM var block
 * -------------------------------------------------------------------------------
 *  [Example]
 *     PROGRAM demo {
 *
 *     }
 * -------------------------------------------------------------------------------
 *  2. var -> ID
 * -------------------------------------------------------------------------------
 *  3. block -> LCB statement_list RCB
 * -------------------------------------------------------------------------------
 *  4. statement_list -> statement
 *                     | statement statement_list
 * -------------------------------------------------------------------------------
 *  5. statement -> assign_statement
 *                | function_declaration
 *                | function_call
 *                | if_statement
 *                | while_statement
 *                | return_statement
 * -------------------------------------------------------------------------------
 *  6. assign_statement -> var ASSIGN (expr_complement | lambda_expr)
 * -------------------------------------------------------------------------------
 *  7. function_declaration -> DEF var LP (var (COMMA var)* RP | RP) block
 * -------------------------------------------------------------------------------
 *  8. function_call -> var LP (expr_complement (COMMA expr_complement)* RP | RP)
 *     (built-in function)
 * -------------------------------------------------------------------------------
 *  9. return_statement -> RETURN expr
 * -------------------------------------------------------------------------------
 *  10. if_statement -> IF LP expr_complement RP block ELSE block
 * -------------------------------------------------------------------------------
 *  11. while_statement -> WHILE LP expr_complement RP block
 * -------------------------------------------------------------------------------
 *  12. expr_complement -> expr (GT|LT|GE|LE|EQ|NE) expr
 *                       | expr
 * -------------------------------------------------------------------------------
 *  13. expr -> term((PLUS|MINUS)term)*
 * -------------------------------------------------------------------------------
 *  14. term -> factor((MULT|DIV|TRUEDIV)factor)*
 * -------------------------------------------------------------------------------
 *  15. factor -> var
 *              | CONST_INT
 *              | CONST_DOUBLE
 *              | CONST_BOOLEAN
 *              | LP expr RP
 *              | PLUS factor
 *              | MINUS factor
 *              | function_call
 * -------------------------------------------------------------------------------
 *  16. lambda_expr -> LAMBDA LP (var (COMMA var)* RP | RP) COLON body
 * -------------------------------------------------------------------------------
 *  17. for_statement -> TODO
 * ##########################################################################################################################
 *   Current supported:
 *     1. basic data type:
 *          - int
 *          - double
 *          - boolean
 *     2. statement:
 *          - function declaration
 *          - function call
 *          - assignment(lambda expression)
 *          - while loop
 *          - if-else statement
 * ##########################################################################################################################
 */

import entity.Interpreter;
import entity.Reader;
import exception.NameError;
import exception.SyntaxError;
import exception.TypeError;
import exception.ZeroDivisionError;

import java.io.IOException;

/**
 *  Application Entrance (along with 'cast_test' file for language illustration)
 */
public class AMython {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Usage: javac AMython filename.");
        }
        try {
            Reader reader = Reader.getReader();
            String source = reader.read(args[0]);
            Interpreter interpreter = Interpreter.getInterpreter();
            interpreter.interpret(source);
        } catch (ZeroDivisionError | SyntaxError | NameError | TypeError e) {
            System.out.println(e.getClass().getName().substring(e.getClass().getName().indexOf('.')+1) + ": " + e.getMessage());
        }
    }
}
