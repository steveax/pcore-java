package com.puppet.pcore.parser.model;

import com.puppet.pcore.PN;
import com.puppet.pcore.impl.pn.ListPN;
import com.puppet.pcore.parser.Expression;

import java.util.List;

import static com.puppet.pcore.impl.Helpers.*;

public class AccessExpression extends Positioned {
	public final Expression operand;

	public final List<Expression> keys;

	public AccessExpression(Expression operand, List<Expression> keys, Locator locator, int offset, int length) {
		super(locator, offset, length);
		this.operand = operand;
		this.keys = unmodifiableCopy(keys);
	}

	public boolean equals(Object o) {
		return super.equals(o) && operand.equals(((AccessExpression)o).operand) && keys.equals(((AccessExpression)o).keys);
	}

	@Override
	public PN toPN() {
		return new ListPN(concat(asList(operand.toPN()), map(keys, Expression::toPN))).asCall("access");
	}
}
