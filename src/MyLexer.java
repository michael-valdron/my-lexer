/*
 * Name: Michael Valdron
 * Student ID: 100487615
 * Submission Date: March 6, 2017
 * 
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Referenced from: Dr. Marzieh Ahmadzadeh,
 * 					ALSU Dragon Book (by: Alfred V. Aho, 
 * 										  Monica S. Lam,
 * 										  Ravi Sethi,
 * 										  Jeffrey D. Ullman)<br/><br/>
 * A Lexical Analyzer.
 * @author Michael Valdron (100487615)
 * @version March 6, 2017
 */
public class MyLexer {
	/**
	 * Token output file.
	 */
	private static FileWriter tokenFile;
	/**
	 * Token output string buffer.
	 */
	private static StringBuffer outB;
	
	/**
	 * Number of characters allowed on each line of the output buffer.
	 */
	private static int bLineC = 72;
	
	/**
	 * Contents of the input source file.
	 */
	private static String sourceFile;
	
	/**
	 * Index of the current character.
	 */
	private static int currChar = 0;
	
	/**
	 * Line number of selected line.
	 */
	private static int line = 1;
	
	/**
	 * True - Currently inside a comment block.<br/>
	 * False - Not currently inside a comment block.
	 */
	private static boolean isCommented = false;
	
	/**
	 * True - Currently in a line comment.<br/>
	 * False - Not currently in a line comment.
	 */
	private static boolean isLineC = false;
	
	/**
	 * Next available identifier pointer to symbol table.
	 */
	private static int nextAddr = 1;
	
	
	/**
	 * Referenced from: Dr. Marzieh Ahmadzadeh,
	 * 					ALSU Dragon Book (by: Alfred V. Aho, 
	 * 										  Monica S. Lam,
	 * 										  Ravi Sethi,
	 * 										  Jeffrey D. Ullman)<br/><br/>
	 * Token Object
	 * @author Michael Valdron
	 * @version March 6, 2017
	 * 
	 */
	private static class Token {
		public final String tokenName;
		public String lexeme;
		public Token(String t) {
			tokenName = t;
			lexeme = null;
		}
		public Token(String t, String s) {
			tokenName = t;
			lexeme = new String(s);
		}
		public void print() throws IOException {
			if (lexeme != null) {
				outB.append("<" + tokenName + ", " + lexeme.replaceAll("\"", "\'") + "> ");
			}
			else {
				outB.append("<" + tokenName + "> ");
			}
			if (outB.length() > bLineC) { 
				outB.append("\n");
				bLineC += 72;
			}
		}
	}
	
	/**
	 * Referenced from: Dr. Marzieh Ahmadzadeh,
	 * 					ALSU Dragon Book (by: Alfred V. Aho, 
	 * 										  Monica S. Lam,
	 * 										  Ravi Sethi,
	 * 										  Jeffrey D. Ullman)<br/><br/>
	 * Symbol Object
	 * @author Michael Valdron
	 * @version March 6, 2017
	 * 
	 */
	private static class Symbol {
		public String name;
		public String type;
		public String value;
		public Symbol(String n) {
			name = new String(n);
			type = null;
			value = null;
		}
		public Symbol(String n, String t) {
			name = new String(n);
			type = new String(t);
			value = null;
		}
		public Symbol(String n, String t, String v) {
			name = new String(n);
			type = new String(t);
			value = new String(v);
		}
	}
	
	/**
	 * Symbol table
	 */
	private static Hashtable<Integer, Symbol> symTable;
	
	/**
	 * 
	 * @param name - Name of identifier to find in the symbol table.
	 * @return Whether or not the name of identifier passed was found in the symbol table.
	 */
	private static boolean exist(String name) {
		for(Symbol s : symTable.values()) {
			if (s.name.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param path - File system path to file.
	 * @return File Contents.
	 * @throws IOException
	 */
	private static String readAllLines(String path) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(path));
		String slines = "";
		
		for (String line : lines) {
			slines += line + "\n";
		}
		
		return slines;
	}
	
	/**
	 * Prints Symbol Table to the console.
	 */
	private static void printST() {
		System.out.printf("%3s %10s %10s %10s\n", "No.", "Name", "Type", "Value");
		System.out.println("------------------------------------");
		for(int i = 1; i <= symTable.size(); i++) {
			System.out.printf("%3d %10s %10s %10s\n", i, symTable.get(i).name, (symTable.get(i).type != null) ? symTable.get(i).type : "No Type", (symTable.get(i).value != null) ? symTable.get(i).value : "No Value");
		}
	}
	
	/**
	 * 
	 * @param lexeme
	 * @return The name of the token corresponding to the lexeme found.
	 */
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
	
	/**
	 * Referenced from: Dr. Marzieh Ahmadzadeh,
	 * 					ALSU Dragon Book (by: Alfred V. Aho, 
	 * 										  Monica S. Lam,
	 * 										  Ravi Sethi,
	 * 										  Jeffrey D. Ullman)
	 * @return The token found at the current character.
	 * @throws IOException
	 */
	public static Token scan() throws IOException {
		char peek = ' ';
		for(int i = currChar; i < sourceFile.length(); i++) {
			char c = sourceFile.charAt(i);
			if(c == '/' && sourceFile.charAt(i + 1) == '*')
			{
				isCommented = true;
			}
			else if(c == '/' && sourceFile.charAt(i + 1) == '/')
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
			else if(c == '\n') {
				line = line + 1;
				if (isLineC) isLineC = false;
			}
			else if(i != 0)
			{
				if(c == '/' && sourceFile.charAt(i - 1) == '*')
				{
					isCommented = false;
				}
			}
			if ((i) > (sourceFile.length() - 2)) {
				return null;
			}
		}
		if(Character.isDigit(peek)) {
			int v = Character.digit(peek, 10);
			for (int i = currChar + 1; Character.isDigit(sourceFile.charAt(i)); i++) {
				v = 10*v + Character.digit(sourceFile.charAt(i), 10);
				currChar = i;
			}
			currChar++;
			return new Token("Number", Integer.toString(v));
		}
		if(Character.isLetter(peek)) {
			StringBuffer b = new StringBuffer();
			boolean isCall = false;
			for (int i = currChar; Character.isLetterOrDigit(sourceFile.charAt(i)); i++) {
				b.append(sourceFile.charAt(i));
				currChar = i;
				isCall = (!Character.isLetterOrDigit(sourceFile.charAt(i+1)) && sourceFile.charAt(i+1) == '(' && getTokenName(b.toString()).equals("id"));
			}
			String lexeme = b.toString();
			Token t;
			if(isCall) t = new Token("T_call", lexeme);
			else if(!getTokenName(b.toString()).matches("id|T_ref|Prim_type")) t = new Token(getTokenName(lexeme));
			else t = new Token(getTokenName(lexeme), lexeme);
			currChar++;
			return t;
		}
		if(peek == '*' || peek == '+' || peek == '-' || peek == '/') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			if (peek == '+' && sourceFile.charAt(currChar + 1) == '+') {
				b.append(sourceFile.charAt(currChar + 1));
				currChar++;
			}
			else if (peek == '-' && sourceFile.charAt(currChar + 1) == '-') {
				b.append(sourceFile.charAt(currChar + 1));
				currChar++;
			}
			String lexeme = b.toString();
			Token t = new Token(getTokenName(lexeme), "\'" + lexeme + "\'");
			currChar++;
			return t;
		}
		if(peek == '<' || peek == '>' || peek == '!') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			if (sourceFile.charAt(currChar + 1) == '=') {
				b.append(sourceFile.charAt(currChar + 1));
				currChar++;
			}
			String lexeme = b.toString();
			Token t = new Token(getTokenName(lexeme), "\'" + lexeme + "\'");
			currChar++;
			return t;
		}
		if(peek == '=') {
			StringBuffer b = new StringBuffer();
			b.append(peek);
			if (sourceFile.charAt(currChar + 1) == '=') {
				b.append(sourceFile.charAt(currChar + 1));
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
			for(int i = currChar + 1; sourceFile.charAt(i) != '\"'; i++) {
				b.append(sourceFile.charAt(i));
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
	 * The Lexical Analyzer's main procedure.
	 * @param args - Passed input and output files.
	 */
	public static void main(String[] args) {
		ArrayList<Token> tokens;
		try {
			if (args.length == 2) {
				tokenFile = new FileWriter(args[0]);
				sourceFile = readAllLines(args[1]);
				outB = new StringBuffer();
				symTable = new Hashtable<Integer, Symbol>();
				tokens = new ArrayList<Token>();
				
				System.out.println("Tokenizing source..");
				
				while(currChar < sourceFile.length()) {
					Token t = scan();
					if (t == null) {
						break;
					}
					else if (t.tokenName.equals("id")) {
						if (!exist(t.lexeme)) {
							Token nt;
							Token nnt = null;
							if(!tokens.get(tokens.size() - 1).tokenName.matches("T_class|Prim_type|T_ref|T_Comma|T_cls_brkt")) {
								System.err.println("Lexical Error in line " + Integer.toString(line) + ": No Such Lexeme can be matched.");
								return;
							}
							Symbol s;
							String type = "";
							String value = "";
							
							if (tokens.get(tokens.size() - 1).tokenName.equals("T_cls_brkt")) {
								type = "]" + type;
								if (tokens.get(tokens.size() - 2).tokenName.equals("T_op_brkt")) {
									type = "[" + type;
									if (tokens.get(tokens.size() - 3).tokenName.matches("Prim_type|T_ref")) {
										type = tokens.get(tokens.size() - 3).lexeme + type;
									}
								}
							}
							else if (tokens.get(tokens.size() - 1).tokenName.matches("Prim_type|T_ref")) {
								type = tokens.get(tokens.size() - 1).lexeme;
							}
							else if (tokens.get(tokens.size() - 1).tokenName.equals("T_Comma")) {
								for(int i = tokens.size() - 2; i > 0; i--) {
									if (tokens.get(i).tokenName.equals("T_cls_brkt")) {
										type = "]" + type;
										if (tokens.get(i - 1).tokenName.equals("T_op_brkt")) {
											type = "[" + type;
											if (tokens.get(i - 2).tokenName.matches("Prim_type|T_ref")) {
												type = tokens.get(i - 2).lexeme + type;
												break;
											}
										}
									}
									else if (tokens.get(i).tokenName.matches("Prim_type|T_ref")) {
										type = tokens.get(i).lexeme;
										break;
									}
								}
							}
							
							if((nt = scan()).tokenName.equals("T_assign")) {
								nnt = scan();
								if(type.matches("int|short|long|float|byte") && nnt.tokenName.equals("Number") && nnt.lexeme.matches("[0-9]+")) {
									value = nnt.lexeme;
								}
								else if(type.matches("float") && nnt.tokenName.equals("Number") && nnt.lexeme.matches("[0-9]*.[0-9]+")) {
									value = nnt.lexeme;
								}
								else {
									value = "\"" + nnt.lexeme.replaceAll("\"|\'", "") + "\"";
								}
							}
							
							if (type.equals("") && value.equals("")) {
								s = new Symbol(t.lexeme);
								t.lexeme = Integer.toString(nextAddr);
								tokens.add(t);
								t.print();
								tokens.add(nt);
								nt.print();
							}
							else if (value.equals("")) {
								s = new Symbol(t.lexeme, type);
								t.lexeme = Integer.toString(nextAddr);
								tokens.add(t);
								t.print();
								tokens.add(nt);
								nt.print();
							}
							else {
								s = new Symbol(t.lexeme, type, value);
								t.lexeme = Integer.toString(nextAddr);
								tokens.add(t);
								t.print();
								tokens.add(nt);
								nt.print();
								tokens.add(nnt);
								nnt.print();
							}
							symTable.put(nextAddr, s);
							nextAddr++;
							
						}
						else {
							for (int i = 1; i <= symTable.size(); i++) {
								if (symTable.get(i).name.equals(t.lexeme)) {
									t.lexeme = Integer.toString(i);
								}
							}
							tokens.add(t);
							t.print();
						}
					}
					else {
						tokens.add(t);
						t.print();
					}
				}
				tokenFile.write(outB.toString());
				tokenFile.flush();
				tokenFile.close();
			}
			else {
				System.err.println("Command should be formatted: \'java MyLexer token.txt <source_code>.java\'");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Tokenization complete!");
		System.out.println("\nTokens:\n" + outB.toString() + "\n");
		System.out.println("Symbol Table:");
		printST();
	}
}
