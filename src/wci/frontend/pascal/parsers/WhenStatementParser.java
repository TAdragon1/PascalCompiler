package wci.frontend.pascal.parsers;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.util.EnumSet;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.frontend.pascal.tokens.PascalNumberToken;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

/**
 * <h1>IfStatementParser</h1>
 *
 * <p>Parse a Pascal WHEN statement.</p>
 *
 *
 *
 */
public class WhenStatementParser extends StatementParser
{
    /**
     * Constructor.
     * @param parent the parent parser.
     */
    public WhenStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    // Synchronization set for LESSTHAN0.
    private static final EnumSet<PascalTokenType> LESSTHAN0_SET =
            StatementParser.STMT_START_SET.clone();
    static {
        LESSTHAN0_SET.add(LESSTHAN0);
        LESSTHAN0_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for EQUAL0.
    private static final EnumSet<PascalTokenType> EQUAL0_SET =
            StatementParser.STMT_START_SET.clone();
    static {
        EQUAL0_SET.add(EQUAL0);
        EQUAL0_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for GREATERTHAN0.
    private static final EnumSet<PascalTokenType> GREATERTHAN0_SET =
            StatementParser.STMT_START_SET.clone();
    static {
        GREATERTHAN0_SET.add(GREATERTHAN0);
        GREATERTHAN0_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    /**
     * Parse an WHEN statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ICodeNode parse(Token token)
            throws Exception
    {
        token = nextToken();  // consume the WHEN

        // Parse the when expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode whenExpression = expressionParser.parse(token);

        // Synchronize at the LESSTHAN0.
        token = synchronize(LESSTHAN0_SET);
        if (token.getType() == LESSTHAN0) {
            token = nextToken();  // consume the LESSTHAN0
        }
        else {
            errorHandler.flag(token, MISSING_LESSTHAN0, this);
        }

        // Parse the LESSTHAN0 statement.
        StatementParser ltStatementParser = new StatementParser(this);
        ICodeNode ltStatementNode = ltStatementParser.parse(token);
        token = currentToken();

        // LESSTHAN0
        ICodeNodeType nodeType = LT;
        ICodeNode lessThan0Node = ICodeFactory.createICodeNode(nodeType);
        lessThan0Node.addChild(whenExpression);

        //Add 0 node
        ICodeNode zeroNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
        zeroNode.setAttribute(VALUE, 0);
        lessThan0Node.addChild(zeroNode);
        
        // Create IF node
        ICodeNode ifLTNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);

        // Add Expression as child
        ifLTNode.addChild(lessThan0Node);
        ifLTNode.addChild(ltStatementNode);

        /*******/

        if (token.getType() == SEMICOLON){
            token = nextToken(); // CONSUME the ;
        }

        Token targetToken = currentToken();

        // Synchronize at the EQUAL0.
        token = synchronize(EQUAL0_SET);
        if (token.getType() == EQUAL0) {
            token = nextToken();  // consume the EQUAL0
        }
        else {
            errorHandler.flag(token, MISSING_EQUAL0, this);
        }

        // EQUAL0
        ICodeNodeType nodeTypeEQ = EQ;
        ICodeNode equal0Node = ICodeFactory.createICodeNode(nodeTypeEQ);
        equal0Node.addChild(whenExpression);

        // Add 0 node
        equal0Node.addChild(zeroNode);

        // Create IF node
        ICodeNode ifEQNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);

        // Set the current line number as an attribute.
        setLineNumber(ifEQNode, targetToken);

        // Add Expression as child
        ifEQNode.addChild(equal0Node);

        // Parse the EQUAL0 statement.
        //StatementParser eqStatementParser = new StatementParser(this);
        ifEQNode.addChild(ltStatementParser.parse(token));

        /********/

        token = currentToken();
        if (token.getType() == SEMICOLON){
            token = nextToken(); // CONSUME the ;
        }

        targetToken = currentToken();

        // Synchronize at the GREATERTHAN0.
        token = synchronize(GREATERTHAN0_SET);
        if (token.getType() == GREATERTHAN0) {
            token = nextToken();  // consume the GREATERTHAN0
        }
        else {
            errorHandler.flag(token, MISSING_GREATERTHAN0, this);
        }

        // GREATERTHAN0
        ICodeNodeType nodeTypeGT = GT;
        ICodeNode greaterThan0Node = ICodeFactory.createICodeNode(nodeTypeGT);
        greaterThan0Node.addChild(whenExpression);

        // Add 0 node
        greaterThan0Node.addChild(zeroNode);

        // Create IF node
        ICodeNode ifGTNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);

        // Set the current line number as an attribute.
        setLineNumber(ifGTNode, targetToken);

        // Add Expression as child
        ifGTNode.addChild(greaterThan0Node);

        // Parse the GREATERTHAN0 statement.
        //StatementParser gtStatementParser = new StatementParser(this);
        ifGTNode.addChild(ltStatementParser.parse(token));

        /*******/

        // IF EQUAL0 ELSE IF GREATERTHAN0
        ifEQNode.addChild(ifGTNode);

        // IF LESSTHAN0 ELSE IF EQUAL0
        ifLTNode.addChild(ifEQNode);

        return ifLTNode;
    }
}