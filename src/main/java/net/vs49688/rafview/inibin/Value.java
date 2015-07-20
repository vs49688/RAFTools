package net.vs49688.rafview.inibin;

import java.util.*;

public final class Value {
	public enum Type {
		STRING,
		INTEGER,
		FLOAT,
		BOOLEAN,
		VECTOR3F,
		LIST
	}
	
	private final Type m_Type;
	private final Object m_Data;
	
	public Value(String s) {
		m_Data = s;
		m_Type = Type.STRING;
	}
	
	public Value(int i) {
		m_Data = i;
		m_Type = Type.INTEGER;
	}
	
	public Value(float f) {
		m_Data = f;
		m_Type = Type.FLOAT;
	}
	
	public Value(boolean b) {
		m_Data = b;
		m_Type = Type.BOOLEAN;
	}
	
	public Value(Vector3f v) {
		m_Data = v;
		m_Type = Type.VECTOR3F;
	}
	
	public Value(List<Value> l) {
		m_Data = l;
		m_Type = Type.LIST;
	}
	
	public String getString() {
		return (String)m_Data;
	}
	
	public Integer getInteger() {
		return (Integer)m_Data;
	}
	
	public Float getFloat() {
		return (Float)m_Data;
	}
	
	public Boolean getBoolean() {
		return (Boolean)m_Data;
	}
	
	public List<Value> getList() {
		return (List<Value>)m_Data;
	}
	
	public Type getType() {
		return m_Type;
	}
	
	@Override
	public String toString() {
		return m_Data.toString();
		/*switch(m_Type) {
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
		
		return "";*/
	}
}
