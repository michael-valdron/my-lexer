/**
 * 
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Michael Valdron
 *
 */
public class MyLexer {
	private static PrintWriter tokenFile;
	private static String fileLines;
	
	private static class Token {
		public final String tokenName;
		public final String lexeme;
		public Token(String t) {
			tokenName = t;
			lexeme = null;
		}
		public Token(String t, String s) {
			tokenName = t;
			lexeme = new String(s);
		}
		public void print() {
			if (lexeme != null && !tokenName.equals("T_Const")) {
				System.out.print("<" + tokenName + ", " + lexeme + "> ");
			}
			else {
				System.out.print("<" + tokenName + "> ");
			}
		}
	}
	
	private static class Symbol {
		public String name;
		public String type;
		public String value;
		public Symbol(String n, String t, String v) {
			name = new String(n);
			type = new String(t);
			value = new String(v);
		}
	}
	
	
	public static int currChar = 0;
	public static int line = 1;
	private static boolean isCommented = false;
	private static boolean isLineC = false;
	private static Hashtable<String, Token> words = new Hashtable<String, Token>();
	private static Hashtable<Token, Token> tokens;
	private static LinkedList<Hashtable<String, Symbol>> symTable;
	
	private static void reserve(Token t) { words.put(t.lexeme, t); }
	
	private static String readAllLines(String path) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(path));
		String slines = "";
		
		for (String line : lines) {
			slines += line + "\n";
		}
		
		return slines;
	}
	
	private static String getTokenName(String lexeme) {
		if (lexeme.equals("int") || lexeme.equals("byte") || lexeme.equals("short") 
				|| lexeme.equals("char") || lexeme.equals("float") || lexeme.equals("long") 
				|| lexeme.equals("byte")) {
			return "Prim_type";
		}
		else if (lexeme.equals("<") || lexeme.equals("<=") || lexeme.equals("==") 
				|| lexeme.equals("!=") || lexeme.equals(">=") || lexeme.equals(">")) {
			return "Comp_op";
		}
		else if (lexeme.equals("+") || lexeme.equals("-") || lexeme.equals("*") 
				|| lexeme.equals("/")) {
			return "T_binary_op";
		}
		else if (lexeme.equals("?")) {
			return "T_qu_Mark";
		}
		else if (lexeme.equals(":")) {
			return "T-colon";
		}
		else if (lexeme.equals("++") || lexeme.equals("--")) {
			return "T_incr_decr";
		}
		else if (lexeme.equals("public") || lexeme.equals("static") || lexeme.equals("class") 
				|| lexeme.equals("void") || lexeme.equals("Main") || lexeme.equals("main") || lexeme.equals("if") 
				|| lexeme.equals("else") || lexeme.equals("for") || lexeme.equals("while") 
				|| lexeme.equals("do") || lexeme.equals("return")) {
			return "T_" + lexeme.toLowerCase();
		}
		else if (lexeme.equals("=")) {
			return "T_assign";
		}
		else if (lexeme.equals("{")) {
			return "T_op_curly";
		}
		else if (lexeme.equals("(")) {
			return "T_op_paren";
		}
		else if (lexeme.equals(".")) {
			return "T_dot_op";
		}
		else if (lexeme.equals("[")) {
			return "T_op_brkt";
		}
		else if (lexeme.equals("]")) {
			return "T_cls_brkt";
		}
		else if (lexeme.equals(";")) {
			return "T_semiC";
		}
		else if (lexeme.equals(",")) {
			return "T_Comma";
		}
		else if (lexeme.equals("}")) {
			return "T_cls_curly";
		}
		else if (lexeme.equals(")")) {
			return "T_cls_paren";
		}
		else if (lexeme.contains("\"")) {
			return "T_Const";
		}
		else if (lexeme.equals("String") || lexeme.equals("System") || lexeme.equals("out")) {
			return "T_ref";
		}
		else {
			return "id";
		}
	}
	
	public static Token scan() throws IOException {
		char peek = ' ';
		for(int i = currChar; i < fileLines.length(); i++) {
			char c = fileLines.charAt(i);
			if(c == '/' && fileLines.charAt(i + 1) == '*')
			{
				isCommented = true;
			}
			else if(c == '/' && fileLines.charAt(i + 1) == '/')
			{
				isLineC = true;
			}
			
			if (!isCommented && !isLineC)
			{
				if(c == ' ' || c == '\t') continue;
				else if(c == '\n') line = line + 1;
				else {
					peek = c;
					currChar = i;
					break;
				}
			}
			else if(isLineC && c == '\n') {
				line = line + 1;
				isLineC = false;
			}
			else if(i != 0)
			{
				if(c == '/' && fileLines.charAt(i - 1) == '*')
				{
					isCommented = false;
				}
			}
			if ((i) > (fileLines.length() - 2)) {
				return null;
			}
		}
		if(Character.isDigit(peek)) {
			int v = Character.digit(peek, 10);
			for (int i = currChar + 1; Character.isDigit(fileLines.charAt(i)); i++) {
				v = 10*v + Character.digit(fileLines.charAt(i), 10);
				currChar = i;
			}
			currChar++;
			return new Token("Number", Integer.toString(v));
		}
		if(Character.isLetter(peek)) {
			StringBuffer b = new StringBuffer();
			boolean isCall = false;
			for (int i = currChar; Character.isLetterOrDigit(fileLines.charAt(i)); i++) {
				b.append(fileLines.charAt(i));
				currChar = i;
				isCall = (!Character.isLetterOrDigit(fileLines.charAt(i+1)) && fileLines.charAt(i+1) == '(' && getTokenName(b.toString()).equals("id"));
			}
			String lexeme = b.toString();
			Token t;
			if(isCall) t = new Token("T_call", lexeme);
			else if(!getTokenName(b.toString()).equals("id") && !getTokenName(b.toString()).equals("T_ref")) t = new Token(getTokenName(lexeme));
			else t = new Token(getTokenName(lexeme), lexeme);
			currChar++;
			return t;
		}
		if(peek == '*' || peek == '+' || peek == '-' || peek == '/') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			if (peek == '+' && fileLines.charAt(currChar + 1) == '+') {
				b.append(fileLines.charAt(currChar + 1));
				currChar++;
			}
			else if (peek == '-' && fileLines.charAt(currChar + 1) == '-') {
				b.append(fileLines.charAt(currChar + 1));
				currChar++;
			}
			String lexeme = b.toString();
			Token t = new Token(getTokenName(lexeme));
			currChar++;
			return t;
		}
		if(peek == '<' || peek == '>' || peek == '!') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			if (fileLines.charAt(currChar + 1) == '=') {
				b.append(fileLines.charAt(currChar + 1));
				currChar++;
			}
			String lexeme = b.toString();
			Token t = new Token(getTokenName(lexeme));
			currChar++;
			return t;
		}
		if(peek == '=') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			if (fileLines.charAt(currChar + 1) == '=') {
				b.append(fileLines.charAt(currChar + 1));
				currChar++;
			}
			String lexeme = b.toString();
			Token t = new Token(getTokenName(lexeme));
			currChar++;
			return t;
		}
		if(peek == '\"') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			for(int i = currChar + 1; fileLines.charAt(i) != '\"'; i++) {
				b.append(fileLines.charAt(i));
				currChar = i;
			}
			b.append(peek);
			String lexeme = b.toString();
			Token t = new Token(getTokenName(lexeme), lexeme);
			currChar += 2;
			return t;
		}
		
		Token t = new Token(getTokenName(Character.toString(peek)));
		currChar++;
		return t;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				fileLines = readAllLines(args[1]);
				while(currChar < fileLines.length()) {
					Token t = scan();
					if (t == null) {
						break;
					}
					t.print();
				}
			}
			else {
				System.err.println("Command should be formatted: \'java MyLexer token.txt <source_code>.java\'");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

}
