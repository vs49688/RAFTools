package net.vs49688.rafview.inibin;

import java.util.*;

public final class Value {
	public enum Type {
		STRING,
		INTEGER,
		FLOAT,
		BOOLEAN,
		LIST
	}
	
	private final Type m_Type;
	private final String m_String;
	private final Integer m_Integer;
	private final Float m_Float;
	private final Boolean m_Boolean;
	private final List<Value> m_List;
	
	public Value(Type type, String s, int i, float f, boolean b, List<Value> l) {
		m_Type = type;
		m_String = s;
		m_Integer = i;
		m_Float = f;
		m_Boolean = b;
		m_List = l;
	}
	
	public String getString() {
		return m_String;
	}
	
	public Integer getInteger() {
		return m_Integer;
	}
	
	public Float getFloat() {
		return m_Float;
	}
	
	public Boolean getBoolean() {
		return m_Boolean;
	}
	
	public List<Value> getList() {
		return m_List;
	}
	
	public Type getType() {
		return m_Type;
	}
	
	@Override
	public String toString() {
		switch(m_Type) {
			case STRING:
				return m_String;
			case INTEGER:
				return m_Integer.toString();
			case FLOAT:
				return m_Float.toString();
			case BOOLEAN:
				return m_Boolean.toString();
			case LIST:
				return m_List.toString();
		}
		
		return "";
	}
}
