/**
 *    Copyright 2006-2017 the original author or authors.
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

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class PagingAndLockPlugin extends PluginAdapter{

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	private Field buildPrivateField(String name, String fqName) {
    		Field field = new Field(name, new FullyQualifiedJavaType(fqName));
    		field.setVisibility(JavaVisibility.PRIVATE);
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
		method.addBodyLine("this." + name + " = " + name);
		return method;
	}
	
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
    		topLevelClass.addField(buildPrivateField("limit", "java.lang.Integer"));
    		topLevelClass.addField(buildPrivateField("offset", "java.lang.Integer"));
    		topLevelClass.addField(buildPrivateField("lockSelectedRows", "java.lang.Boolean"));

    		topLevelClass.addMethod(buildSetter("limit", "void", "java.lang.Integer"));
    		topLevelClass.addMethod(buildGetter("limit", "java.lang.Integer"));

    		topLevelClass.addMethod(buildSetter("offset", "void", "java.lang.Integer"));
    		topLevelClass.addMethod(buildGetter("offset", "java.lang.Integer"));

    		Method method = buildSetter("lockSelectedRows", "void", "java.lang.Boolean");
    		method.addJavaDocLine("/**");
    		method.addJavaDocLine(" * Set true to append 'for update' clause to this query.");
    		method.addJavaDocLine(" */");
    		topLevelClass.addMethod(method);
    		topLevelClass.addMethod(buildGetter("lockSelectedRows", "java.lang.Boolean"));
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

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    		element.addElement(ifElement("limit != null", "limit ${limit}"));
    		element.addElement(ifElement("offset != null", "offset ${offset}"));
    		element.addElement(ifElement("lockSelectedRows != null and lockSelectedRows == true", "for update"));
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    		element.addElement(ifElement("limit != null", "limit ${limit}"));
    		element.addElement(ifElement("offset != null", "offset ${offset}"));
    		element.addElement(ifElement("lockSelectedRows != null and lockSelectedRows == true", "for update"));
        return true;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    		element.addElement(ifElement("lockSelectedRows != null and lockSelectedRows == true", "for update"));
        return true;
    }

}
