package com.puppet.pcore.parser.model;

import com.puppet.pcore.PN;
import com.puppet.pcore.impl.pn.LiteralPN;

public class QualifiedName extends Positioned implements NameExpression {
	public final String name;

	public QualifiedName(String name, Locator locator, int offset, int length) {
		super(locator, offset, length);
		this.name = name;
	}

	public boolean equals(Object o) {
		return super.equals(o) && name.equals(((QualifiedName)o).name);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public PN toPN() {
		return new LiteralPN(name).asCall("qn");
	}
}
