package wci.frontend.pascal.parsers;

import java.util.EnumSet;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

/**
 * <h1>WhenStatementParser</h1>
 *
 * <p>Parse a WHEN statement.</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
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

    /*
     * WHEN  Expression
     * LESSTHAN0   Statement   
     * EQUAL0   Statement   
     * GREATERTHAN0   Statement
     * 
     * If Expression Lessthan0 -> do Lessthan0 statement
     * If Expression Equal0 -> do Equal0 statement
     * If Expression Greaterthan0 -> do Greaterthan0 statement
     */
    
    // Synchronization set for TO or DOWNTO.
    private static final EnumSet<PascalTokenType> TO_DOWNTO_SET =
        ExpressionParser.EXPR_START_SET.clone();
    static {
        TO_DOWNTO_SET.add(TO);
        TO_DOWNTO_SET.add(DOWNTO);
        TO_DOWNTO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
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
     * Parse the When statement.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ICodeNode parse(Token token)
        throws Exception
    {
        token = nextToken();  // consume the When
        
        token = nextToken();  // consume the (
        
        Token targetToken = token;

        // TODO do I need a WHEN node type?
        
        // Create the IF nodes.
        ICodeNode ifLTNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);
        ICodeNode ifEQNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);
        ICodeNode ifGTNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);

        // Parse the expression
        ExpressionParser expressionParser = new ExpressionParser(this);
        ICodeNode expressionNode = expressionParser.parse(token);
        
        // Set the current line number attribute.
        setLineNumber(expressionNode, targetToken);

        // Add child to all IF nodes
        ifLTNode.addChild(expressionNode);
        ifEQNode.addChild(expressionNode);
        ifGTNode.addChild(expressionNode);
        
        // Consume the )
        token = nextToken();
        
        // Synchronize at the LESSTHAN0.
        token = synchronize(LESSTHAN0_SET);
        if (token.getType() == LESSTHAN0) {
            token = nextToken();  // consume the LESSTHAN0
        }
        else {
            errorHandler.flag(token, MISSING_DO, this);	// TODO add error types
        }

        // Parse the LESSTHAN0 statement.
        StatementParser ltStatementParser = new StatementParser(this);
        ifLTNode.addChild(ltStatementParser.parse(token));
        
        
        // Synchronize at the EQUAL0.
        token = synchronize(EQUAL0_SET);
        if (token.getType() == EQUAL0) {
            token = nextToken();  // consume the EQUAL0
        }
        else {
            errorHandler.flag(token, MISSING_DO, this);	// TODO add error types
        }

        // Parse the EQUAL0 statement.
        StatementParser eqStatementParser = new StatementParser(this);
        ifEQNode.addChild(eqStatementParser.parse(token));
        
        
        // Synchronize at the GREATERTHAN0.
        token = synchronize(GREATERTHAN0_SET);
        if (token.getType() == GREATERTHAN0) {
            token = nextToken();  // consume the GREATERTHAN0
        }
        else {
            errorHandler.flag(token, MISSING_DO, this);	// TODO add error types
        }

        // Parse the GREATERTHAN0 statement.
        StatementParser gtStatementParser = new StatementParser(this);
        ifGTNode.addChild(gtStatementParser.parse(token));
        
        
        return compoundNode; // TODO how to return all 3 nodes in correct way
        // TODO do i need to make my own WHEN node type?
    }
}
