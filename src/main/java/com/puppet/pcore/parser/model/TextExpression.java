package com.puppet.pcore.parser.model;

import com.puppet.pcore.PN;
import com.puppet.pcore.parser.Expression;

public class TextExpression extends UnaryExpression {
	public TextExpression(Expression expr, Locator locator, int offset, int length) {
		super(expr, locator, offset, length);
	}

	@Override
	public PN toPN() {
		return unaryPN("str");
	}
}
