package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.pascal.PascalTokenType.*;

/**
 * <h1>PascalWordToken</h1>
 *
 * <p> Pascal word tokens (identifiers and reserved words).</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class PascalWordToken extends PascalToken
{
	public static final char UNDERSCORE = '_';
	
	/**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public PascalWordToken(Source source)
        throws Exception
    {
        super(source);
    }

    /**
     * Extract a Pascal word token from the source.
     * @throws Exception if an error occurred.
     */
    protected void extract()
        throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();
        char currentChar = currentChar();

        // At this point, currentChar is the first character of the word token
        if (currentChar != UNDERSCORE) {
        	
	        // Get the word characters (letter or digit).  The scanner has
	        // already determined that the first character is a letter.
	        while (Character.isLetterOrDigit(currentChar) || currentChar == UNDERSCORE) {
	            textBuffer.append(currentChar);
	            currentChar = nextChar();  // consume character
	        }

	        // At this point, currentChar is the last character
	        if (currentChar != UNDERSCORE) {
	        	
		        text = textBuffer.toString();
		
		        // Is it a reserved word or an identifier?
		        type = (RESERVED_WORDS.contains(text.toLowerCase()))
		               ? PascalTokenType.valueOf(text.toUpperCase())  // reserved word
		               : IDENTIFIER;                                  // identifier
		        
	        }
	        
        }
    }
    
    
}