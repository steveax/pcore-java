package com.puppet.pcore.impl;

import com.puppet.pcore.DynamicObject;
import com.puppet.pcore.Type;
import com.puppet.pcore.impl.types.AnyType;
import com.puppet.pcore.impl.types.ObjectType;
import com.puppet.pcore.impl.types.ObjectTypeExtension;
import com.puppet.pcore.impl.types.ParameterInfo;
import com.puppet.pcore.serialization.ArgumentsAccessor;

import java.io.IOException;
import java.util.Objects;

import static java.lang.String.format;

public class DynamicObjectImpl implements DynamicObject {
	private final Object[] attributes;
	private AnyType ptype;

	public DynamicObjectImpl(ArgumentsAccessor argumentsAccessor) throws IOException {
		argumentsAccessor.remember(this);
		ObjectType baseType = (ObjectType)argumentsAccessor.getType();
		ptype = baseType;
		attributes = argumentsAccessor.getAll();
		if(baseType.isParameterized())
			ptype = new ObjectTypeExtension(baseType, this);
	}

	public DynamicObjectImpl(ObjectType ptype, Object... args) {
		ObjectType baseType = ptype;
		this.ptype = baseType;
		this.attributes = new GivenArgumentsAccessor(ptype, args).getAll();
		if(baseType.isParameterized())
			this.ptype = new ObjectTypeExtension(baseType, this);
	}

	@Override
	public Type _pcoreType() {
		return ptype;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof DynamicObjectImpl))
			return false;

		DynamicObjectImpl dynObj = (DynamicObjectImpl)o;
		if(ptype.isEqualityIncludeType()) {
			if(dynObj.ptype.isEqualityIncludeType()) {
				if(!ptype.equals(dynObj.ptype))
					return false;
			} else if(!ptype.isAssignable(dynObj.ptype))
				return false;
		} else {
			if(!dynObj.ptype.isAssignable(ptype))
				return false;
		}

		int[] ei = ptype.parameterInfo().equalityAttributeIndexes;
		for(int i : ei)
			if(!Objects.equals(attributes[i], dynObj.attributes[i]))
				return false;
		return true;
	}

	@Override
	public Object get(String attrName) {
		ParameterInfo pi = ptype.parameterInfo();
		Integer idx = pi.attributeIndex.get(attrName);
		if(idx == null)
			throw new IllegalArgumentException(format("%s has no attribute named '%s'", ptype.name(), attrName));
		int index = idx;
		return index < attributes.length ? attributes[index] : pi.attributes.get(index).value();
	}

	public Object[] getAttributes() {
		return attributes;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		int[] ei = ptype.parameterInfo().equalityAttributeIndexes;
		for(int i : ei)
			hash = hash * 31 + Objects.hashCode(attributes[i]);
		return hash;
	}
}
