import java.util.ArrayList;

public class Parser {
	public static void main(String[] args) {
		System.out.println("Enter an expression, end with semi-colon!\n");
		Lexer.lex();
		new Program();
		Code.gen(Code.end());
		Code.output();
	}
}

class Program {
	Decls d;
	Stmts sts;
	End e;

	public Program() {

		d = new Decls();
		sts = new Stmts();

	}

}

class Decls {
	IdList idL;

	public Decls() {

		if (Lexer.nextToken == Token.KEY_INT) {
			idL = new IdList();
		}

	}

}

class IdList {
	IdList idL;
	Stmt st;
	int i;
	public static ArrayList<Character> idArrayList = new ArrayList<Character>();
	public static int idptr = 0;
	static char idExists = '~';

	public IdList() {

		Lexer.lex();

		if (Lexer.nextToken == Token.ID) {

			idArrayList.add(Lexer.ident);
			Lexer.lex();
			if (Lexer.nextToken == Token.COMMA) {
				idL = new IdList();
			}
		}

	}

}

class Stmts {
	Stmt st;
	Stmts sts;
	char op;
	int i;

	public Stmts() {

		st = new Stmt();

		if (Lexer.nextToken == Token.KEY_END)
			return;

		Lexer.lex();
		if (Lexer.nextToken != Token.KEY_END && Lexer.nextToken != Token.RIGHT_BRACE) {
			sts = new Stmts();
		}

	}

}

class Stmt {
	Assign a;
	Cmpd c;
	Cond co;
	Loop l;
	char op;
	End e;

	public Stmt() {

		if (Lexer.nextToken == Token.SEMICOLON || Lexer.nextToken == Token.RIGHT_BRACE) {
			Lexer.lex();
		}
		if (Lexer.nextToken == Token.ID) {
			a = new Assign();
		}

		if (Lexer.nextToken == Token.LEFT_BRACE) {
			Lexer.lex();
			c = new Cmpd();
		}

		if (Lexer.nextToken == Token.KEY_IF) {
			co = new Cond();
		}

		if (Lexer.nextToken == Token.KEY_FOR) {
			Lexer.lex(); // Right Brace After FOR
			l = new Loop();
		}

		if (Lexer.nextToken == Token.KEY_END) {
			return;
		}
	}

}

class Assign {
	Expr e;
	int i;
	char op;
	String id;
	int storecodeId;

	public Assign() {

		storecodeId = IdList.idArrayList.indexOf(Lexer.ident);

		Lexer.lex(); // Fwd ptr after ID
		if (Lexer.nextToken == Token.ASSIGN_OP) {
			Lexer.lex();
			e = new Expr();

			if (Loop.assignType == "for")
				Loop.forInsts.add(Integer.toString(storecodeId + 1));
			else
				Code.gen(Code.storecode(storecodeId + 1)); // istore after
															// integer assigned
		}
	}

}

class Cmpd {
	Stmts sts;

	public Cmpd() {
		sts = new Stmts();
	}

}

class Cond {

	Rexp re;
	Stmt s1, s2;
	public ArrayList<Integer> ifArrList;
	public ArrayList<Integer> goArrList;
	public static int gotoPtr = 0;
	public int endPtr;
	int sizeofIfArr;
	public static boolean isEndofIf = false;

	public Cond() {

		ifArrList = new ArrayList<Integer>();
		goArrList = new ArrayList<Integer>();
		endPtr = 0;
		if (Lexer.nextToken == Token.KEY_IF) {

			Lexer.lex();
			Lexer.lex();
			re = new Rexp(ifArrList);
			s1 = new Stmt();

			Lexer.lex();

			if (Lexer.nextToken != Token.KEY_END && Lexer.nextToken != Token.RIGHT_BRACE) {
				Code.gen(Code.gotocode(goArrList, ifArrList));
			} else if (Lexer.nextToken == Token.RIGHT_BRACE || Lexer.nextToken == Token.KEY_END) {
				Code.updateifcode(ifArrList);
			}

			if (Lexer.nextToken == Token.KEY_ELSE) {
				Lexer.lex();
				s2 = new Stmt();
			}

			if (goArrList.size() > 0)
				Code.updategotocode(goArrList, endPtr);

		}

	}
}

class Loop {
	public static ArrayList<String> forInsts = new ArrayList<String>();
	public ArrayList<Integer> ifArrayList;
	public ArrayList<Integer> gotoArrayList;
	Assign a, a2;
	public static String assignType;
	Rexp re;
	Stmt st;
	int forCnt, gotoAddressPtr;

	public Loop() {

		ifArrayList = new ArrayList<Integer>();
		gotoArrayList = new ArrayList<Integer>();
		gotoAddressPtr = 0;
		if (Lexer.nextToken == Token.LEFT_PAREN) {

			Lexer.lex(); // Forward ptr after Left Paren
			if (Lexer.nextToken == Token.ID) {
				a = new Assign();
			}

			if (Lexer.nextToken == Token.SEMICOLON) {

				Lexer.lex(); // Forward ptr after Semicolon

				gotoAddressPtr = Code.getCurrentAddress();
				if (Lexer.nextToken == Token.ID) {
					re = new Rexp(ifArrayList);
				}

			}

			if (Lexer.nextToken == Token.SEMICOLON) {

				Lexer.lex(); // Forward ptr after Semicolon
				if (Lexer.nextToken == Token.ID) {
					assignType = "for";

					a2 = new Assign();
					assignType = "";
				} else {
					Lexer.lex(); // Forward ptr after Right Brace
				}

			}

			st = new Stmt();

			// Print Instructions of 3rd argument in FOR after the stmt executed
			forCnt = (forInsts.size() / 4);

			if (forCnt > 0) {
				for (int cnt = 1; cnt > 0; --cnt) {

					Code.gen(Code.loadcode(Integer.parseInt(forInsts.get((forCnt - 1) * 4))));
					Code.gen(Code.intcode(Integer.parseInt(forInsts.get(((forCnt - 1) * 4) + 1))));
					Code.gen(Code.opcode(forInsts.get(((forCnt - 1) * 4) + 2).charAt(0)));
					Code.gen(Code.storecode(Integer.parseInt(forInsts.get(((forCnt - 1) * 4) + 3))));

					forInsts.remove(forInsts.get((forCnt - 1) * 4));
					forInsts.remove((forInsts.get((forCnt - 1) * 4) + 1));
					forInsts.remove((forInsts.get((forCnt - 1) * 4) + 2));
					forInsts.remove((forInsts.get((forCnt - 1) * 4) + 3));
				}
			}

			Code.gen(Code.gotocode(gotoArrayList, ifArrayList));
			Code.updateforgotocode(gotoArrayList, gotoAddressPtr);

		}

	}
}

class Rexp {
	Expr e1, e2;
	int op;

	// Type : 1 = IF , 2 = LOOP
	public Rexp(ArrayList<Integer> ifArrList) {

		e1 = new Expr();
		if (Lexer.nextToken == Token.LESSER_OP || Lexer.nextToken == Token.GREATER_OP || Lexer.nextToken == Token.EQ_OP
				|| Lexer.nextToken == Token.NOT_EQ) {

			op = Lexer.nextToken;
			Lexer.lex();
			e2 = new Expr();

			if (op == Token.LESSER_OP)
				Code.gen(Code.ifcode("if_icmpge", ifArrList));
			else if (op == Token.GREATER_OP)
				Code.gen(Code.ifcode("if_icmple", ifArrList));
			else if (op == Token.NOT_EQ)
				Code.gen(Code.ifcode("if_icmpeq", ifArrList));
			else if (op == Token.EQ_OP)
				Code.gen(Code.ifcode("if_icmpne", ifArrList));

		}

	}
}

class End {

	public End() {
		Lexer.lex();
	}

}

class Expr { // expr -> term (+ | -) expr | term
	Term t;
	Expr e;
	char op;

	public Expr() {
		t = new Term();
		if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			e = new Expr();

			if (Loop.assignType == "for")
				Loop.forInsts.add(Character.toString(op));
			else
				Code.gen(Code.opcode(op));
		}
	}
}

class Term { // term -> factor (* | /) term | factor
	Factor f;
	Term t;
	char op;

	public Term() {

		f = new Factor();

		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			t = new Term();

			if (Loop.assignType == "for")
				Loop.forInsts.add(Character.toString(op));
			else
				Code.gen(Code.opcode(op));

		} else if (Lexer.nextToken == Token.RIGHT_PAREN) {
			Lexer.lex();
		}
	}
}

class Factor { // factor -> number | '(' expr ')'
	Expr e;
	Stmts s;
	int i;

	public Factor() {

		switch (Lexer.nextToken) {
		case Token.INT_LIT: // number
			i = Lexer.intValue;

			if (Loop.assignType == "for")
				Loop.forInsts.add(Integer.toString(i));
			else
				Code.gen(Code.intcode(i));

			Lexer.lex();
			break;
		case Token.ID:
			if (Loop.assignType == "for")
				Loop.forInsts.add(Integer.toString(IdList.idArrayList.indexOf(Lexer.ident) + 1));
			else
				Code.gen(Code.loadcode(IdList.idArrayList.indexOf(Lexer.ident) + 1)); // As
																						// List
																						// Index
																						// starts
																						// at
																						// 0

			Lexer.lex();

			break;
		case Token.LEFT_PAREN: // '('
			Lexer.lex();
			e = new Expr();
			break;
		case Token.RIGHT_PAREN:
			Lexer.lex();
			break;
		default:
			break;
		}
	}
}

class Code {
	static String[] code = new String[100];
	static Integer[] arrayCodes = new Integer[100];
	static int codeptr = 0;
	static int s = 0;
	static int bytes = 0;

	public static void gen(String s) {
		code[codeptr] = s;
		codeptr++;

	}

	public static String intcode(int i) {
		if (i > 127) {
			arrayCodes[codeptr] = bytes;
			bytes += 3;
			return "sipush " + i;
		}
		if (i > 5) {
			arrayCodes[codeptr] = bytes;
			bytes += 2;
			return "bipush " + i;
		}
		arrayCodes[codeptr] = bytes;
		bytes += 1;
		return "iconst_" + i;
	}

	public static String storecode(int storecodeId) {
		arrayCodes[codeptr] = bytes;

		if (storecodeId == 0) {
			++s;

			if (s > 3) {
				bytes += 2;
				return "istore " + (s);
			}

			else {
				bytes += 1;
				return "istore_" + (s);
			}
		} else {

			if (storecodeId > 3) {
				bytes += 2;
				return "istore " + (storecodeId);
			}

			else {
				bytes += 1;
				return "istore_" + (storecodeId);
			}

		}

	}

	public static String loadcode(int l) {
		arrayCodes[codeptr] = bytes;

		if (l > 3) {
			bytes += 2;
			return "iload " + l;
		} else {
			bytes += 1;
			return "iload_" + l;
		}

	}

	public static String opcode(char op) {
		switch (op) {
		case '+':
			arrayCodes[codeptr] = bytes;
			bytes += 1;
			return "iadd";
		case '-':
			arrayCodes[codeptr] = bytes;
			bytes += 1;
			return "isub";
		case '*':
			arrayCodes[codeptr] = bytes;
			bytes += 1;
			return "imul";
		case '/':
			arrayCodes[codeptr] = bytes;
			bytes += 1;
			return "idiv";
		default:
			return "";
		}
	}

	public static String ifcode(String code, ArrayList<Integer> ifArrList) {
		arrayCodes[codeptr] = bytes;

		ifArrList.add(codeptr);
		bytes += 3;
		return code;
	}

	public static String gotocode(ArrayList<Integer> goArrList, ArrayList<Integer> ifArrList) {
		arrayCodes[codeptr] = bytes;
		bytes += 3;

		updateifcode(ifArrList);
		goArrList.add(codeptr);

		return "goto";
	}

	public static void updateifcode(ArrayList<Integer> ifArrList) {

		Integer ifInstNum = ifArrList.get(0);
		code[ifInstNum] = code[ifInstNum] + " " + bytes;

	}

	public static void updategotocode(ArrayList<Integer> goArrList, int endPtr) {

		Integer gotoInstNum = goArrList.get(0);
		code[gotoInstNum] = code[gotoInstNum] + " " + bytes;
	}

	public static int getCurrentAddress() {
		return bytes;
	}

	public static void updateforgotocode(ArrayList<Integer> gotoForArrayList, int address) {

		Integer gotoForInstNum = gotoForArrayList.get(0);
		code[gotoForInstNum] = code[gotoForInstNum] + " " + address;
	}

	public static String end() {
		arrayCodes[codeptr] = bytes;
		bytes += 1;
		return "return";
	}

	public static void output() {
		for (int i = 0; i < codeptr; i++)
			System.out.println(arrayCodes[i] + ": " + code[i]);
	}
}
