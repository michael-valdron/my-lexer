/**
 * 
 */

/**
 * @author Michael Valdron
 *
 */
public class MyLexer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tokenFile;
		String sourceFile;
		
		if (args.length > 0)
		{
			tokenFile = args[0];
			sourceFile = args[1];
		}
		else
		{
			System.err.println("Command should be formatted: \'java MyLexer token.txt <source_code>.java\'");
			return;
		}
		
		
	}

}
