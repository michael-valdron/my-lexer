/**
 * 
 */
import java.io.*;
import java.util.LinkedList;
/**
 * @author Michael Valdron
 *
 */
public class MyLexer {

	private static LinkedList<String> tokens;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tokenFilePath;
		String sourceFilePath;
		String line;
		FileInputStream tokenFile;
		FileInputStream sourceFile;
		BufferedReader rBuffer;
		
		tokens = new LinkedList<String>();
		
		if (args.length > 0)
		{
			tokenFilePath = args[0];
			//sourceFilePath = args[1];
		}
		else
		{
			System.err.println("Command should be formatted: \'java MyLexer token.txt <source_code>.java\'");
			return;
		}
		
		
		try {
			tokenFile = new FileInputStream(tokenFilePath);
			//sourceFile = new FileReader(sourceFilePath);
			
			rBuffer = new BufferedReader(new InputStreamReader(tokenFile));
			
			while((line = rBuffer.readLine()) != null)
			{
				tokens.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tokens = null;
	}

}
