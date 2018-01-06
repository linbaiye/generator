/**
 *    Copyright 2006-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.plugins;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InitializationBlock;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class YobatisCriteriaPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	private Field buildProtectedField(String name, String fqName) {
    		Field field = new Field(name, new FullyQualifiedJavaType(fqName));
    		field.setVisibility(JavaVisibility.PROTECTED);
    		return field;
	}
	
	private Method buildMethod(String prefix, String name, String type) {
		String s1 = name.substring(0, 1).toUpperCase();
		String nameCapitalized = s1 + name.substring(1);
		Method method = new Method(prefix + nameCapitalized);
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType(type));
		return method;
	}
	
	private Method buildGetter(String name, String returnFqType) {
		Method method = buildMethod("get", name, returnFqType);
		method.addBodyLine("return " + name + ";");
		return method;
	}
	
	private Method buildSetter(String name, String returnFqType,
			String paramType) {
		Method method = buildMethod("set", name, returnFqType);
		method.addParameter(
				new Parameter(new FullyQualifiedJavaType(paramType), name));
		method.addBodyLine("this." + name + " = " + name + ";");
		return method;
	}
	
	
	private void appendClear(TopLevelClass topLevelClass) {
		for (Method method : topLevelClass.getMethods()) {
			if ("clear".equals(method.getName())) {
				method.addBodyLine("limit = null;");
				method.addBodyLine("offset = null;");
				method.addBodyLine("lockSelectedRows = null;");
			}
		}
	}
	
	
	private void addPaging(TopLevelClass topLevelClass, 
			IntrospectedTable introspectedTable) {
		topLevelClass.addField(buildProtectedField("limit", "java.lang.Long"));
		topLevelClass.addField(buildProtectedField("offset", "java.lang.Long"));
		topLevelClass.addField(buildProtectedField("lockSelectedRows", "java.lang.Boolean"));

		topLevelClass.addMethod(buildSetter("limit", "void", "java.lang.Long"));
		topLevelClass.addMethod(buildGetter("limit", "java.lang.Long"));

		topLevelClass.addMethod(buildSetter("offset", "void", "java.lang.Long"));
		topLevelClass.addMethod(buildGetter("offset", "java.lang.Long"));

		Method method = buildSetter("lockSelectedRows", "void", "java.lang.Boolean");
		method.addJavaDocLine("/**");
		method.addJavaDocLine(" * Set true to append 'for update' clause to this query.");
		method.addJavaDocLine(" */");
		topLevelClass.addMethod(method);
		topLevelClass.addMethod(buildGetter("lockSelectedRows", "java.lang.Boolean"));
    	appendClear(topLevelClass);
	}
	
	
	private void adjustOrMethod(TopLevelClass topLevelClass) {
		for (Method method : topLevelClass.getMethods()) {
			if (!method.getName().equals("or") || !method.getParameters().isEmpty()) {
				continue;
			}
			method.setReturnType(topLevelClass.getType());
			method.getBodyLines().clear();
			method.addBodyLine("oredCriteria.add(createCriteriaInternal());");
			method.addBodyLine("return this;");
		}
	}
	
	
	private void addJavaDoc(TopLevelClass topLevelClass) {
		topLevelClass.addJavaDocLine("/**");
		topLevelClass.addJavaDocLine(" * A " + topLevelClass.getType().getShortName() + " provides methods to construct 'where', 'limit', 'offset', 'for update'");
		topLevelClass.addJavaDocLine(
				" * clauses. Although building 'limit', 'offset', 'for update' and simple 'where' clauses are pretty");
		topLevelClass.addJavaDocLine(" * intuitive, a complex 'where' clause requires a little bit more attention.");
		topLevelClass.addJavaDocLine(
				" * <p>A complex 'where' consists of multiple expressions that are ORed together, such as <br>");
		topLevelClass.addJavaDocLine(" * {@code (id = 1 and field = 2) or (filed <= 3) or( ... ) ...}");
		topLevelClass.addJavaDocLine(" * <p>Suppose we had a Book model which has an author field and a name field,");
		topLevelClass.addJavaDocLine(
				" * here is an example that utilizes BookCriteria to build a where clause of<br>");
		topLevelClass.addJavaDocLine(
				" * '{@code author = \"Some guy\" and name = \"Some book\") or (name not in (\"hated ones\", \"boring ones\"))}");
		topLevelClass.addJavaDocLine(" * <pre>");
		topLevelClass.addJavaDocLine(" * CustomerCriteria.authorEqualTo(\"Some guy\")");
		topLevelClass.addJavaDocLine(" * .andNameEqualTo(\"Some book\")");
		topLevelClass.addJavaDocLine(" * .or()");
		topLevelClass.addJavaDocLine(" * .andNameNotIn(Arrays.asList(\"hated ones\", \"boring ones\"));");
		topLevelClass.addJavaDocLine(" * </pre>");
		topLevelClass.addJavaDocLine(" */");
	}

	
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
    		addPaging(topLevelClass, introspectedTable);
			ascdentCriteria(topLevelClass, introspectedTable);
			fixOrderBy(topLevelClass, introspectedTable);
			addStaticConstructor(topLevelClass);
			adjustOrMethod(topLevelClass);
			addJavaDoc(topLevelClass);
    		return true;
	}
    
    private XmlElement ifElement(String testClause, String text) {
    		XmlElement xmlElement = new XmlElement("if");
    		Attribute attribute = new Attribute("test", testClause);
    		xmlElement.addAttribute(attribute);
    		TextElement textElement = new TextElement(text);
    		xmlElement.addElement(textElement);
    		return xmlElement;
    }

    private XmlElement findXmlElement(XmlElement element, String name, Attribute attribute) {
		for (Element e : element.getElements()) {
			if (!(e instanceof XmlElement)) {
				continue;
			}
			XmlElement tmp = (XmlElement)e;
			if (name.equals(tmp.getName())) {
				for (Attribute attribute2 : tmp.getAttributes()) {
					if (attribute.getName().equals(attribute2.getName()) &&
						attribute.getValue().equals(attribute2.getValue())) {
						return tmp;
					}
				}
			}
		}
		return null;
	}


    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    	element.addElement(ifElement("limit != null", "limit #{limit}"));
    	element.addElement(ifElement("offset != null", "offset #{offset}"));
    	element.addElement(ifElement("lockSelectedRows != null and lockSelectedRows == true", "for update"));
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }
    
	
	private void addLastCriteriaMethod(TopLevelClass topLevelClass, 
			FullyQualifiedJavaType returnType) {
		Method newMethod = new Method("lastCriteria");
		newMethod.setVisibility(JavaVisibility.PRIVATE);
		newMethod.setReturnType(returnType);
		newMethod.addBodyLine("if (oredCriteria.isEmpty()) {");
		newMethod.addBodyLine("oredCriteria.add(createCriteriaInternal());");
		newMethod.addBodyLine("}");
		newMethod.addBodyLine("return oredCriteria.get(oredCriteria.size() - 1);");
		topLevelClass.addMethod(newMethod);
	}
	
	
	
	private void depcrateCreateCriteria(TopLevelClass topLevelClass) {
		Method method = findMethod(topLevelClass, "createCriteria");
		method.addAnnotation("@Deprecated");
	}

	private Method proxyMethod(Method sourceMethod, TopLevelClass topLevelClass) {
		Method method = new Method(sourceMethod);
		method.getBodyLines().clear();
		method.setReturnType(topLevelClass.getType());
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("lastCriteria().");
		stringBuilder.append(method.getName());
		stringBuilder.append("(");
		for (int i = 0; i < method.getParameters().size(); i++) {
			Parameter parameter = method.getParameters().get(i);
			stringBuilder.append(parameter.getName());
			if (i == method.getParameters().size() - 1) {
				stringBuilder.append(");");
			} else {
				stringBuilder.append(", ");
			}
		}
		if (method.getParameters().isEmpty()) {
			stringBuilder.append(");");
		}
		method.addBodyLine(stringBuilder.toString());
		method.addBodyLine("return this;");
		return method;
	}
	
	
	private void buildColumnMap(TopLevelClass topLevelClass, IntrospectedTable table) {
		topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
		topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.HashMap"));
		Field field = new Field("PROPERTY_TO_COLUMN", new FullyQualifiedJavaType("java.util.Map<String, String>"));
		field.setVisibility(JavaVisibility.PROTECTED);
		field.setFinal(true);
		field.setStatic(true);
		topLevelClass.addField(field);
		InitializationBlock initializationBlock = new InitializationBlock(true);
		initializationBlock.addBodyLine("PROPERTY_TO_COLUMN = new HashMap<String, String>();");
		List<IntrospectedColumn> introspectedColumns =  table.getAllColumns();
		for (IntrospectedColumn column : introspectedColumns) {
			initializationBlock.addBodyLine(String.format("PROPERTY_TO_COLUMN.put(\"%s\", \"%s\");", column.getJavaProperty(), column.getActualColumnName()));
		}
		topLevelClass.addInitializationBlock(initializationBlock);
	}
	
	
	private Method findMethod(TopLevelClass topLevelClass, String methodName) {
		Iterator<Method> iterator = topLevelClass.getMethods().iterator();
		while (iterator.hasNext()) {
			Method method = iterator.next();
			if (methodName.equals(method.getName())) {
				return method;
			}
		}
		return null;
	}
	
	private Field findField(TopLevelClass topLevelClass, String fieldName) {
		for (Field field : topLevelClass.getFields()) {
			if ("orderByClause".equals(field.getName())) {
				return field;
			}
		}
		return null;
	}
	

	private final static Pattern PATTERN = Pattern.compile("and(.{1})(.+)");
	private void addStaticConstructor(TopLevelClass topLevelClass) {
		List<Method> newMethods = new LinkedList<Method>();
		for (Method m: topLevelClass.getMethods()) {
			if (!m.getName().startsWith("and") || m.getParameters().isEmpty()) {
				continue;
			}
			Matcher matcher = PATTERN.matcher(m.getName());
			if (!matcher.find()) {
				continue;
			}
			String name = matcher.group(1).toLowerCase() + matcher.group(2);
			Method method = new Method(m);
			method.getBodyLines().clear();
			method.setName(name);
			method.setStatic(true);
			method.setVisibility(JavaVisibility.PUBLIC);
			method.setReturnType(topLevelClass.getType());
			StringBuilder stringBuilder = new StringBuilder("return new ");
			stringBuilder.append(topLevelClass.getType().getShortName());
			stringBuilder.append("().");
			stringBuilder.append(m.getName());
			stringBuilder.append("(");
			for (Parameter parameter : m.getParameters()) {
				stringBuilder.append(parameter.getName());
				stringBuilder.append(", ");
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append(");");
			method.addBodyLine(stringBuilder.toString());
			newMethods.add(method);
		}
		for (Method method : newMethods) {
			topLevelClass.addMethod(method);
		}
	}


	private void addOrderBy(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Method method = findMethod(topLevelClass, "setOrderByClause");
		method.getParameters().clear();
		method.addParameter(new Parameter(new FullyQualifiedJavaType("String "), " order"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("String "), " ... fields"));
		method.setVisibility(JavaVisibility.PRIVATE);
		method.setName("orderBy");
		method.getBodyLines().clear();
		method.addBodyLine("if ( fields == null || fields.length == 0) {");
		method.addBodyLine("throw new IllegalArgumentException(\"Empty fields passed.\");");
		method.addBodyLine("}");
		method.addBodyLine("StringBuilder stringBuilder = new StringBuilder();");
		method.addBodyLine("if (orderByClause != null) {");
		method.addBodyLine("stringBuilder.append(orderByClause);");
		method.addBodyLine("stringBuilder.append(',');");
		method.addBodyLine("}");
		method.addBodyLine("for (String field : fields) {");
		method.addBodyLine("if (!PROPERTY_TO_COLUMN.containsKey(field)) {");
		method.addBodyLine("throw new IllegalArgumentException(\"Unrecognizable field:\" + field);");
		method.addBodyLine("}");
		method.addBodyLine("stringBuilder.append(PROPERTY_TO_COLUMN.get(field));");
		method.addBodyLine("stringBuilder.append(\" \");");
		method.addBodyLine("stringBuilder.append(order);");
		method.addBodyLine("stringBuilder.append(',');");
		method.addBodyLine("}");
		method.addBodyLine("stringBuilder.deleteCharAt(stringBuilder.length() - 1);");
		method.addBodyLine("orderByClause = stringBuilder.toString();");
	}
	
	
	private void appendMethodAfter(TopLevelClass topLevelClass, String name, Method method) {
		for (int i = 0; i < topLevelClass.getMethods().size(); i++) {
			Method tmp = topLevelClass.getMethods().get(i);
			if (name.equals(tmp.getName())) {
				topLevelClass.getMethods().add(i + 1, method);
				break;
			}
		}
	}
	

	private void addAscOrderBy(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Method method = new Method("ascOrderBy");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(topLevelClass.getType());
		method.addParameter(new Parameter(new FullyQualifiedJavaType("String "), " ... fields"));
		method.addBodyLine("orderBy(\"asc\", fields);");
		method.addBodyLine("return this;");
		method.addJavaDocLine("/**");
		method.addJavaDocLine(" * Add the 'order by field1 asc, field2 asc, ...' clause to query, only fields in {@code Customer} are allowed.");
		method.addJavaDocLine(" * By invoking this method and {@link #descOrderBy(String...) descOrderBy} alternately, a more complex 'order by' clause");
		method.addJavaDocLine(" * can be constructed, shown as below.");
		method.addJavaDocLine(" * <pre>");
		method.addJavaDocLine(" * criteria.ascOrderBy('field1');");
		method.addJavaDocLine(" * criteria.descOrderBy('field2');");
		method.addJavaDocLine(" * -> 'order by field1 asc, field2 desc'");
		method.addJavaDocLine(" * </pre>");
		method.addJavaDocLine(" * @param fields the fields to sort.");
		method.addJavaDocLine(" * @throws IllegalArgumentException if fields is empty, or any of the fields is invalid.");
		method.addJavaDocLine(" * @return this criteria.");
		method.addJavaDocLine(" */");
		appendMethodAfter(topLevelClass, "orderBy", method);
	}
	
	private void addDescOrderBy(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Method method = new Method("descOrderBy");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(topLevelClass.getType());
		method.addParameter(new Parameter(new FullyQualifiedJavaType("String "), " ... fields"));
		method.addBodyLine("orderBy(\"desc\", fields);");
		method.addBodyLine("return this;");
		method.addJavaDocLine("/**");
		method.addJavaDocLine(" * Add the 'order by field1 desc, field2 desc, ...' clause to query, only fields in {@code Customer} are allowed.");
		method.addJavaDocLine(" * By invoking this method and {@link #ascOrderBy(String...) ascOrderBy} alternately, a more complex 'order by' clause");
		method.addJavaDocLine(" * can be constructed, shown as below.");
		method.addJavaDocLine(" * <pre>");
		method.addJavaDocLine(" * criteria.ascOrderBy('field1');");
		method.addJavaDocLine(" * criteria.descOrderBy('field2');");
		method.addJavaDocLine(" * -> 'order by field1 asc, field2 desc'");
		method.addJavaDocLine(" * </pre>");
		method.addJavaDocLine(" * @param fields the fields to sort.");
		method.addJavaDocLine(" * @throws IllegalArgumentException if fields is empty, or any of the fields is invalid.");
		method.addJavaDocLine(" * @return this criteria.");
		method.addJavaDocLine(" */");
		appendMethodAfter(topLevelClass, "ascOrderBy", method);
	}
	

	
	private void fixOrderBy(TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		buildColumnMap(topLevelClass, introspectedTable);
		addOrderBy(topLevelClass, introspectedTable);
		addAscOrderBy(topLevelClass, introspectedTable);
		addDescOrderBy(topLevelClass, introspectedTable);
	}


	private void ascdentCriteria(TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		InnerClass criteriaClass = null;
		for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
			if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
				criteriaClass = innerClass;
				break;
			}
		}
		addLastCriteriaMethod(topLevelClass, criteriaClass.getType());
		depcrateCreateCriteria(topLevelClass);
		for (Method method : criteriaClass.getMethods()) {
			if (method.getReturnType() != null && 
				"Criteria".equals(method.getReturnType().getShortName())) {
				topLevelClass.addMethod(proxyMethod(method, topLevelClass));
			}
		}
	}
}
