public class MiniGoPrintListener extends MiniGoBaseListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	
	static class Util {
		static boolean isBinaryOperation(MiniGoParser.ExprContext ctx) {
			return ctx.getChildCount() == 3 &&
					ctx.getChild(1) != ctx.expr() && ! ctx.getChild(1).getText().equals("=");
		}
		
		static boolean isIfStmt(ParseTree ctx) {
			return ctx.getChildCount() > 0 &&
					ctx.getChild(0).getText().equals("if");
		}
		
		static boolean isForStmt(ParseTree ctx) {
			return ctx.getChildCount() > 0 &&
					ctx.getChild(0).getText().equals("for");
		}
		
		static boolean isCompoundStmt(ParseTree ctx) {
			return	ctx.getChildCount() > 0 &&
					ctx.getChild(0).getChildCount() > 0 &&
					ctx.getChild(0).getChild(0).getText().equals("{");	
		}
		
	}
	
	ParseTreeProperty<Integer> nesting = new ParseTreeProperty<Integer>();
	
	String tabs(ParseTree ctx) {
		String ts = "";
		for (int i=0; i < nesting.get(ctx); i++) {
			ts+= "....";	
		}
		return ts;	
	}
	
	/***********************************************************************************/
	@Override
	public void exitExpr(MiniGoParser.ExprContext ctx) {
		if (ctx.getChildCount() == 1) {
			if (ctx.getChild(0) == ctx.LITERAL())
				newTexts.put(ctx, ctx.getChild(0).getText());
			else if (ctx.getChild(0) == ctx.IDENT())
				newTexts.put(ctx, ctx.getChild(0).getText());
		}
		
		else if (ctx.getChildCount() == 2) 	// unary
			newTexts.put(ctx, ctx.getChild(0).getText() + newTexts.get(ctx.getChild(1)));
		
		else if (ctx.getChildCount() == 3) {
			if (ctx.getChild(0).getText().equals("(") )
				newTexts.put(ctx, "(" + ctx.getChild(1).getText() + ")");
			
			else if (Util.isBinaryOperation(ctx)) {
				// 예: expr '+' expr
				String s1  = null, s2 = null, op = null;
				s1 = newTexts.get(ctx.expr(0));
				s2 = newTexts.get(ctx.expr(1));
				op = ctx.getChild(1).getText();
				newTexts.put(ctx, s1 + " " + op + " " + s2);
			}
			else if (ctx.getChild(0) == ctx.IDENT()) {
				newTexts.put(ctx, ctx.getChild(0).getText() + " = " + newTexts.get(ctx.getChild(2)));
			}	
			return;
		}
		
		else if (ctx.getChildCount() == 4) {	
			if (ctx.getChild(0) == ctx.IDENT()) {
				String s = ctx.IDENT().getText() + ctx.getChild(1).getText() 
							+ newTexts.get(ctx.getChild(2))
							+ ctx.getChild(3).getText() ;
				newTexts.put(ctx, s);			
			}
		}
		else if (ctx.getChildCount() == 6) {
			String s = ctx.IDENT().getText() + "[" 
					+ newTexts.get(ctx.getChild(2)) + "] = "
					+ newTexts.get(ctx.getChild(6));
			newTexts.put(ctx, s);	
		}
	}
	
	@Override public void exitProgram(MiniGoParser.ProgramContext ctx)  { 		
		String s = "";
		for (int i=0; ctx.getChild(i) != null ;  i++) {
			s += newTexts.get(ctx.getChild(i)); }
		newTexts.put(ctx, s);
		System.out.println(s);
	}
}  
