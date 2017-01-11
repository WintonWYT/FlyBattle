package flygame.extensions.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleExpCalculator {
	public static void main(String[] args) {
		SimpleExpCalculator calculator = new SimpleExpCalculator();
		calculator.setParam("a", 10);
		calculator.setParam("b", 2);
		calculator.setParam("c", 3);
		System.out.println(calculator.calculate("a*a + b*b + c"));
		System.out.println(calculator.roundCalculate("a*a + b*b + c"));

		System.out.println(calculator.calculate("33/0.7"));
		System.out.println(calculator.roundCalculate("((10- 1) / 5 + 1)"));
	}
	
	private Map<String, Number> params = new HashMap<>();
	public void setParam(String key, Number value) {
		params.put(key, value);		
	}
	
	public Number calculate(String expression) {
		expression = expression.replaceAll("\\s+", "");
		expression = replaceParams(expression);
		return exp(new ParamCls(expression, 0));
	}
	
	public Number roundCalculate(String expression) {
		expression = expression.replaceAll("\\s+", "");
		expression = replaceParams(expression);
		return roundExp(new ParamCls(expression, 0));
	}
	
	private Number roundExp(ParamCls param) {
		Number term = roundTerm(param);
		while (term != null && param.hasNext()) {
			char op = param.getChar();
			if (op != '+' && op != '-')
				break;
			param.incPos();
			Number result = roundTerm(param);
			if (result == null) {
				return null;
			}
			if (op == '+') {
				term = term.intValue() + result.intValue();
			} else {
				term = term.intValue() - result.intValue();
			}
		}
		return term;
	}
	
	private Number roundTerm(ParamCls param) {
		Number factor = factor(param);
		while (factor != null && param.hasNext()) {
			char op = param.getChar();
			if (op != '*' && op != '/') break;
			
			param.incPos();
			Number result = factor(param);
			if (result == null) {
				return null;
			}
			if (op == '*') {
				factor = factor.intValue() * result.intValue();
			} else {
				if (result.intValue() == 0 && result.doubleValue() == 0) {
					return null;
				}
				if (factor instanceof Integer && result instanceof Integer)
					factor = factor.intValue() / result.intValue();
				else 
					factor = factor.doubleValue() / result.doubleValue();
			}
		}
		return factor != null? factor.intValue(): null;
	}
	
	private Number exp(ParamCls param) {
		Number term = term(param);
		while (term != null && param.hasNext()) {
			char op = param.getChar();
			if (op != '+' && op != '-')
				break;
			param.incPos();
			Number result = term(param);
			if (result == null) {
				return null;
			}
			if (op == '+') {
				if (term instanceof Integer && result instanceof Integer)
					term = term.intValue() + result.intValue();
				else 
					term = term.doubleValue() + result.doubleValue();
			} else {
				if (term instanceof Integer && result instanceof Integer)
					term = term.intValue() - result.intValue();
				else 
					term = term.doubleValue() - result.doubleValue();
			}
		}
		return term;
	}
	
	private Number term(ParamCls param) {
		Number factor = factor(param);
		while (factor != null && param.hasNext()) {
			char op = param.getChar();
			if (op != '*' && op != '/' && op != '%')
				break;
			param.incPos();
			Number result = factor(param);
			if (result == null) {
				return null;
			}
			if (op == '*') {
				if (factor instanceof Integer && result instanceof Integer)
					factor = factor.intValue() * result.intValue();
				else 
					factor = factor.doubleValue() * result.doubleValue();
			} else if(op == '/'){
				if (result.intValue() == 0 && result.doubleValue() == 0)
					return null;
				if (factor instanceof Integer && result instanceof Integer)
					factor = factor.intValue() / result.intValue();
				else 
					factor = factor.doubleValue() / result.doubleValue();
			}else{
				if (result.intValue() == 0 && result.doubleValue() == 0)
					return null;
				if (factor instanceof Integer && result instanceof Integer)
					factor = factor.intValue() % result.intValue();
				else 
					factor = factor.doubleValue() % result.doubleValue();
			}
		}
		return factor;
	}
	
	private Number factor(ParamCls param) {
		if (!param.hasNext()) return null;
		if (Character.isDigit(param.getChar()) || param.getChar() == '.') {
			return digit(param);
		}
		if (param.getChar() == '(') {
			param.incPos();
			Number result = exp(param);
			if (!param.hasNext() || param.getChar() != ')') {
				return null;
			}
			param.incPos();
			return result;
		}
		return null;
	}
	
	private Number digit(ParamCls param) {
		StringBuilder builder = new StringBuilder();
		boolean hasDot = false;
		while (param.hasNext() && (Character.isDigit(param.getChar()) 
				|| (param.getChar() == '.') && !hasDot)) 
		{
			builder.append(param.getChar());
			if(param.getChar() == '.') hasDot = true;
			param.incPos();
		}
		if(hasDot) {
			return Double.parseDouble(builder.toString());
		} else {
			return Integer.parseInt(builder.toString());
		}
	}

	private String replaceParams(String expression) {
		if (params == null || params.isEmpty()) {
			return expression;
		}
		
		List<String> keys = new LinkedList<>();
		for (String key : params.keySet()) {
			int index = 0;
			for (String had : keys) {
				if (had.length() < key.length()) {
					break;
				}
				index ++;
			}
			keys.add(index, key);
		}
		
		String result = expression;
		for (String key : keys) {
			result = result.replace(key, String.valueOf(params.get(key)));
		}
		return result;
	}
	
	private class ParamCls {
		String expression;
		int pos;
		public ParamCls(String exp, int pos) {
			this.expression = exp;
			this.pos = pos;
		}
		public char getChar() {
			return expression.charAt(pos);
		}
		public void incPos() {
			pos ++;
		}
		public boolean hasNext() {
			return pos < expression.length();
		}
	}
}
