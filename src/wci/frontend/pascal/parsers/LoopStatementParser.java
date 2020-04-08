package wci.frontend.pascal.parsers;

import java.util.EnumSet;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;

/**
 * <h1>LoopStatementParser</h1>
 *
 * <p>Parse a Loop statement.</p>
 *
 *
 *
 */
public class LoopStatementParser extends StatementParser
{
    /**
     * Constructor.
     * @param parent the parent parser.
     */
    public LoopStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    // Synchronization set for DO.
    private static final EnumSet<PascalTokenType> DO_SET =
            StatementParser.STMT_START_SET.clone();
    static {
        DO_SET.add(DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for BAR.
    private static final EnumSet<PascalTokenType> BAR_SET =
            StatementParser.STMT_START_SET.clone();
    static {
        BAR_SET.add(BAR);
        BAR_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    /**
     * Parse the Loop statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ICodeNode parse(Token token)
            throws Exception
    {
        token = nextToken();  // consume the Loop

        token = nextToken();  // consume the (

        Token targetToken = token;

        // Create the loop COMPOUND, LOOP, and TEST nodes.
        ICodeNode compoundNode = ICodeFactory.createICodeNode(COMPOUND);
        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.LOOP);
        ICodeNode testNode = ICodeFactory.createICodeNode(TEST);
        ICodeNode notNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

        // Parse the embedded initial assignment.
        AssignmentStatementParser assignmentParser =
                new AssignmentStatementParser(this);
        ICodeNode initAssignNode = assignmentParser.parse(token);

        token = currentToken();
        if (token.getType() == SEMICOLON){
            token = nextToken();
        }

        token = synchronize(BAR_SET);
        if (token.getType() == BAR) {
            token = nextToken();  // consume the BAR
        }
        else {
            errorHandler.flag(token, MISSING_BAR, this);
        }

        // Set the current line number attribute.
        setLineNumber(initAssignNode, targetToken);

        // The COMPOUND node adopts the initial ASSIGN and the LOOP nodes
        // as its first and second children.
        compoundNode.addChild(initAssignNode);
        compoundNode.addChild(loopNode);

        // parse the loop condition
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode expressionNode = expressionParser.parse(token);

        // The TEST node adopts the relational operator node as its only child.
        // The LOOP node adopts the TEST node as its first child.
        notNode.addChild(expressionNode);
        testNode.addChild(notNode);
        loopNode.addChild(testNode);

        // second identifier := expression
        // Synchronize at the BAR.
        token = synchronize(BAR_SET);
        if (token.getType() == BAR) {
            token = nextToken();  // consume the BAR
        }
        else {
            errorHandler.flag(token, MISSING_BAR, this);
        }

        AssignmentStatementParser assignmentParser2 =
                new AssignmentStatementParser(this);
        ICodeNode initAssignNode2 = assignmentParser2.parse(token);

        // loopNode.addChild(initAssignNode2); moved to after adding statement

        // Set the current line number attribute.
        setLineNumber(initAssignNode2, targetToken);

        token = nextToken();  // consume the )

        // Synchronize at the DO.
        token = synchronize(DO_SET);
        if (token.getType() == DO) {
            token = nextToken();  // consume the DO
        }
        else {
            errorHandler.flag(token, MISSING_DO, this);
        }

        // Parse the nested statement. The LOOP node adopts the statement
        // node as its second child.
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));
        loopNode.addChild(initAssignNode2);
        return compoundNode;
    }
}