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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;


public class YobatisPlugin extends PluginAdapter {
	
	private final static Map<String, String[]> JAVADOCS = new HashMap<String, String[]>();
	
    //long countByCriteria(BookCriteria criteria);
	private final static String[] COUNT_BY_CRITERIA_JAVADOC = new String[] {
			"/**",
			" * Count the number of selected records.",
			" * @param criteria the criteria to select records.",
			" * @return the number.",
			" * @throws PersistenceException if null or empty criteria passed.",
			" */"};

    //int deleteByCriteria(BookCriteria criteria);
	private final static String[] DELETE_BY_CRITERIA_JAVADOC = new String[] {
			"/**",
			" * Delete records based on the {@code criteria}.",
			" * @param criteria the criteria to select records.",
			" * @return the number of deleted records.",
			" * @throws PersistenceException if null or empty criteria passed.",
			" */"};

    //int deleteByPrimaryKey(Long id);
	private final static String[] DELETE_BY_PK_JAVADOC = new String[] {
			"/**",
			" * Delete record by primary key. The query will be evaluated as",
			" * <pre>delete from table where pk = null</pre> if null passed.",
			" * @param id the primary key.",
			" * @return 1 if the record has been deleted, 0 if not.",
			" */"};

	//int insert(Customer record);
	private final static String[] INSERT_JAVADOC = new String[] {
			"/**",
			" * Insert a record.",
			" * <p>All fields except the primary key will be inserted regardless of whether",
			" * null or not, the primary key field(id) will hold the generated value after insertion.",
			" * <p>Passing null has the same effect of passing a record whose fields are null.",
			" * @param record the record to insert.",
			" * @return 1 if the record has been inserted.",
			" */"};

	//int insertSelective(Customer record);
	private final static String[] INSERT_SELECTIVE_JAVADOC = new String[] {
			"/**",
			" * Insert a record ignoring null fields.",
			" * <p>All non-null fields except the primary key will be inserted,",
			" * the primary key will hold the generated value after insertion.",
			" * <p>Passing null has the same effect of passing a record whose fields are null.",
			" * @param record the record to insert.",
			" * @return 1 if the record has been inserted.",
			" */"};

	//List<Customer> selectByCriteria(CustomerCriteria criteria);
	private final static String[] SELECT_CRITERIA_JAVADOC = new String[] {
			"/**",
			" * Select records according to the {@code criteria}.",
			" * @param criteria the criteria to select records.",
			" * @return a list of selected records if any, an empty list if none meets the criteria.",
			" * @throws PersistenceException if null or empty criteria passed.",
			" */"};

    //Customer selectByPrimaryKey(Long id);
	private final static String[] SELECT_PK_JAVADOC = new String[] {
			"/**",
			" * Select record by primary key. The query will be evaluated as",
			" * <pre>select fields from table where pk = null</pre> if null passed.",
			" * @param id the primary key.",
			" * @return the selected record if found, null else.",
			" */"};

    //int updateByCriteriaSelective(@Param("record") Customer record, @Param("criteria") CustomerCriteria criteria);
	private final static String[] UPDATE_SELECTIVE_BY_CRITERIA_JAVADOC = new String[] {
			"/**",
			" * Update columns of the records, selected by {@code criteria}, to corresponding",
			" * non-null fields in {@code record}(null fields are ignored).",
			" * @param record the record that holds new values.",
			" * @param criteria the criteria to query records to update.",
			" * @return the number of rows updated.",
			" * @throws PersistenceException if the criteria is null or empty, or null record passed.",
			" */"};

    //int updateByCriteria(@Param("record") Customer record, @Param("criteria") CustomerCriteria criteria);
	private final static String[] UPDATE_BY_CRITERIA_JAVADOC = new String[] {
			"/**",
			" * Update columns of the records, selected by {@code criteria}, to corresponding fields",
			" * in {@code record}, regardless of whether the field is null or not.", 
			" * @param record the record that holds new values.",
			" * @param criteria the criteria to query records to update.",
			" * @return the number of rows updated.",
			" * @throws PersistenceException if the criteria is null or empty, or null record passed.",
			" */"};

    //int updateByPrimaryKeySelective(Customer record);
	private final static String[] UPDATE_SELECTIVE_BY_PK_JAVADOC = new String[] {
			"/**",
			" * Update record's columns to corresponding non-null fields in {@code record},",
			" * null fields are ignored.",
			" * @param record the record that holds new values.",
			" * @return 1 the record with the primary key can be found, 0 else.",
			" */"};

    //int updateByPrimaryKey(Customer record);
	private final static String[] UPDATE_BY_PK_JAVADOC = new String[] {
			"/**",
			" * Update record's columns to corresponding fields in {@code record}, regardless of",
			" * whether the field is null or not.",
			" * @param record the record that holds new values.",
			" * @return 1 the record can be found based on the primary key, 0 else.",
			" */"};

    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>();
	static {
		JAVADOCS.put("countByCriteria", COUNT_BY_CRITERIA_JAVADOC);
		JAVADOCS.put("deleteByCriteria", DELETE_BY_CRITERIA_JAVADOC);
		JAVADOCS.put("deleteByPrimaryKey", DELETE_BY_PK_JAVADOC);
		JAVADOCS.put("insert", INSERT_JAVADOC);
		JAVADOCS.put("insertSelective", INSERT_SELECTIVE_JAVADOC);
		JAVADOCS.put("selectByCriteria", SELECT_CRITERIA_JAVADOC);
		JAVADOCS.put("selectByPrimaryKey", SELECT_PK_JAVADOC);
		JAVADOCS.put("updateByCriteriaSelective", UPDATE_SELECTIVE_BY_CRITERIA_JAVADOC);
		JAVADOCS.put("updateByCriteria", UPDATE_BY_CRITERIA_JAVADOC);
		JAVADOCS.put("updateByPrimaryKeySelective", UPDATE_SELECTIVE_BY_PK_JAVADOC);
		JAVADOCS.put("updateByPrimaryKey", UPDATE_BY_PK_JAVADOC);
		
		TYPE_MAP.put("java.lang.Long", "long");
		TYPE_MAP.put("java.lang.Integer", "int");
		TYPE_MAP.put("java.lang.Double", "double");
		TYPE_MAP.put("java.lang.Float", "float");
		TYPE_MAP.put("java.lang.Short", "short");
		TYPE_MAP.put("java.lang.Byte", "byte");
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

    private void adjustExampledMethod(Method method) {
   		String oldName = method.getName();
		String newName = oldName.replaceAll("Example", "Criteria");
		method.setName(newName);
		List<Parameter> parameters = method.getParameters();
		List<Parameter> newParameters = new ArrayList<Parameter>(parameters.size());
		for (Parameter parameter: parameters) {
			String tmp = parameter.getName().replace("example", "criteria");
			Parameter newParameter = new Parameter(parameter.getType(), tmp);
			for (String annotatioName: parameter.getAnnotations()) {
				newParameter.addAnnotation(annotatioName.replace("example", "criteria"));
			}
			newParameters.add(newParameter);
		}
		parameters.clear();
		parameters.addAll(newParameters);
    }
    
    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		adjustExampledMethod(method);
    		return true;
    }

    
    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method,
    			Interface interfaze, IntrospectedTable introspectedTable) {
    		adjustExampledMethod(method);
        return true;
    }
    
    
    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		adjustExampledMethod(method);
    		return true;
    }
    
    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		return false;
    }
    
    
    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		return false;
    }
    
    
    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		adjustExampledMethod(method);
    		return true;
    }
    
    @Override
    public boolean clientCountByExampleMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		adjustExampledMethod(method);
    		return true;
    }
    

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    		return false;
    }
    
    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
    		return false;
    }
    
    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    		return false;
    }
    
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
    		return false;
    }
    
    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
    		disableNullCriteria(element);
    		return true;
    }
    
    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
    		disableNullCriteria(element);
    		return true;
    }
    
    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    		return false;
    }
    
    @Override
    public boolean sqlMapBlobColumnListElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
    		return false;
    }

    private final static Pattern PACKAGE_PATTERN = Pattern.compile("\\.([a-zA-Z_0-9]+Criteria)$");
    
    
    private void removeJavadoc(TopLevelClass topLevelClass) {
    		for (Method method : topLevelClass.getMethods()) {
    			method.getJavaDocLines().clear();
    		}
    		for (org.mybatis.generator.api.dom.java.Field field: topLevelClass.getFields()) {
    			field.getJavaDocLines().clear();
    		}

    		for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
    			innerClass.getJavaDocLines().clear();
    		}
    }
    
    
    private void throwExceptionWhenEmptyCrtieriaList(TopLevelClass topLevelClass) {
    		for (Method method: topLevelClass.getMethods()) {
    			if ("getOredCriteria".equals(method.getName())) {
    				method.addBodyLine(0, "}");
    				method.addBodyLine(0, "throw new PersistenceException(\"Empty criteria.\");");
    				method.addBodyLine(0, "if (oredCriteria.isEmpty()) {");
    				break;
    			}
    		}
    		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.exceptions.PersistenceException"));
    	
    }


    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable)  {
    		FullyQualifiedJavaType type = topLevelClass.getType();
    		try {
    			addCriteriaToPackageName(type);
    			removeJavadoc(topLevelClass);
        		topLevelClass.addJavaDocLine(
        				"/**");
        		topLevelClass.addJavaDocLine(
        				" * This class is generated by MyBatis Generator, do NOT modify.");
        		topLevelClass.addJavaDocLine(
        				" * Extend this class if it is found insufficient.");
        		topLevelClass.addJavaDocLine(
        				" */");
    			throwExceptionWhenEmptyCrtieriaList(topLevelClass);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
    		return true;
    }

    

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable)  {
    		removeJavadoc(topLevelClass);
    		topLevelClass.addJavaDocLine("/**");
    		topLevelClass.addJavaDocLine(" * This class corresponds to the table: " + introspectedTable.getFullyQualifiedTable() 
    		+ ", it is generated by");
    		topLevelClass.addJavaDocLine(" * Mybatis Generator, do NOT modify.");
    		topLevelClass.addJavaDocLine(" * Extend this class if it is found insufficient.");
    		topLevelClass.addJavaDocLine(" */");
    		return true;
    }

    
    private void modifyMethodParameterTypeAndJavadoc(List<Method> methods) {
    		for (Method method: methods) {
    			List<Parameter> parameters =  method.getParameters();
    			Parameter newParameter = null;
    			Iterator<Parameter> iterator = parameters.iterator();
    			while (iterator.hasNext()) {
    				Parameter parameter = iterator.next();
    				Matcher matcher = PACKAGE_PATTERN.matcher(parameter.getType().getFullyQualifiedName());
    				if (matcher.find()) {
    					newParameter = new Parameter(
    							new FullyQualifiedJavaType(matcher.replaceFirst(".criteria.$1")), 
    							parameter.getName());
    					for (String annotation: parameter.getAnnotations()) {
    						newParameter.addAnnotation(annotation);
    					}
    					iterator.remove();
    					break;
    				}
    			}
    			method.getJavaDocLines().clear();
    			String[] javdoc = JAVADOCS.get(method.getName());
    			if (javdoc != null) {
    				for (String line: javdoc) {
    					method.addJavaDocLine(line);
    				}
    			}
    			if (newParameter != null) {
    				parameters.add(newParameter);
    			}
    		}
    }
    
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
    		Set<FullyQualifiedJavaType> types = interfaze.getImportedTypes();
    		Iterator<FullyQualifiedJavaType> iterator = types.iterator();
    		FullyQualifiedJavaType newType = null;
    		while (iterator.hasNext()) {
    			FullyQualifiedJavaType type = iterator.next();
    			Matcher matcher = PACKAGE_PATTERN.matcher(type.getFullyQualifiedName());
    			if (matcher.find()) {
    				iterator.remove();
    				newType = new FullyQualifiedJavaType(matcher.replaceFirst(".criteria.$1"));
    			}
    		}
    		if (newType != null) {
    			types.add(newType);
    		}
    		modifyMethodParameterTypeAndJavadoc(interfaze.getMethods());
    		interfaze.addJavaDocLine("/**");
    		interfaze.addJavaDocLine(" * This class is generated by Mybatis Generator, and it is safe to modify.");
    		interfaze.addJavaDocLine(" */");
    		return true;
    }
    

    
    private void addCriteriaToPackageName(FullyQualifiedJavaType type) {
    		try {
    			Field field = type.getClass().getDeclaredField("packageName");
			field.setAccessible(true);
			field.set(type, type.getPackageName() + ".criteria");

			Matcher matcher = PACKAGE_PATTERN.matcher(type.getFullyQualifiedName());
			if (matcher.find()) {
				field = type.getClass().getDeclaredField("baseQualifiedName");
				field.setAccessible(true);
				field.set(type, matcher.replaceFirst(".criteria.$1"));
			}
    		} catch (Exception e) {
    			throw new IllegalStateException(e);
		}
    }
    
    //Prohibit passing null criteria which will
    //take effect on the whole table.
    private void disableNullCriteria(XmlElement element) {
    		XmlElement ifElement = null;
    		Iterator<Element> iterator = element.getElements().iterator();
    		while(iterator.hasNext()) {
    			Element e = iterator.next();
    			if (!(e instanceof XmlElement)) {
    				continue;
    			}
			XmlElement xmlElement = (XmlElement) e;
			if (!"if".equals(xmlElement.getName())) {
				continue;
			}
			for (Attribute attribute : xmlElement.getAttributes()) {
				if ("test".equals(attribute.getName()) && "_parameter != null".equals(attribute.getValue())) {
					ifElement = xmlElement;
					break;
				}
			}
			if (ifElement == null) {
				continue;
			}
			List<Element> subElements = ifElement.getElements();
			iterator.remove();
			element.getElements().addAll(subElements);
			break;
    		}
    }
    
    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element,
           IntrospectedTable introspectedTable) {
    		disableNullCriteria(element);
    		return true;
    }
    

    private void replaceExampleParameter(XmlElement xmlElement)  {
    		XmlElement where = findXmlElement(xmlElement, "where");
    		XmlElement foreach = findXmlElement(where, "foreach");
    		Iterator<Attribute> iterator = foreach.getAttributes().iterator();
    		while (iterator.hasNext()) {
    			Attribute attribute = iterator.next();
    			if ("collection".equals(attribute.getName())) {
    				iterator.remove();
    				Attribute newAttribute = new Attribute("collection", "criteria.oredCriteria");
    				foreach.getAttributes().add(newAttribute);
    				break;
    			}
    		}
    }
    
    
    private void replaceCriteriaPackageNameInXml(XmlElement xmlElement) {
    		Iterator<Attribute> iterator = xmlElement.getAttributes().iterator();
    		Attribute newAttribute = null;
    		while (iterator.hasNext()) {
    			Attribute attribute = iterator.next();
    			if (!"parameterType".equals(attribute.getName())) {
    				continue;
    			}
    			//Replace the criteria's package name.
    			Matcher matcher = PACKAGE_PATTERN.matcher(attribute.getValue());
    			if (matcher.find()) {
    				newAttribute = new Attribute(attribute.getName(), matcher.replaceFirst(".criteria.$1"));
    				iterator.remove();
    				break;
    			}
    		}
    		if (newAttribute != null) {
    			xmlElement.getAttributes().add(1, newAttribute);
    		}
    }
    
    private void modifyCommentInXml(XmlElement xmlElement) {
    		Iterator<Element> iterator = xmlElement.getElements().iterator();
    		boolean commentStart = false;
    		while (iterator.hasNext()) {
    			Element element = iterator.next();
    			if (element.getFormattedContent(0).contains("<!--")) {
    				commentStart = true;
    			} else if (element.getFormattedContent(0).contains("-->")) {
    				return;
    			} else if (commentStart &&
    					(element.getFormattedContent(0).contains("This element was generated"))) {
    				//Remove this comment since it contains a timestamp which differs from time to time.
    				iterator.remove();
    			}
    		}
    }
    
    private XmlElement findXmlElement(XmlElement element, String name) {
		for (Element e : element.getElements()) {
			if (!(e instanceof XmlElement)) {
				continue;
			}
			XmlElement tmp = (XmlElement)e;
			if (name.equals(tmp.getName())) {
				return tmp;
			}
		}
		return null;
    }
    	
    

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
            IntrospectedTable introspectedTable) {
    		List<Element> elements = document.getRootElement().getElements();
    		for (Element element: elements) {
    			if (!(element instanceof XmlElement)) {
    				continue;
			}
			XmlElement xmlElement = (XmlElement) element;
			for (Attribute attribute : xmlElement.getAttributes()) {
				if ("id".equals(attribute.getName()) &&
						"WHERE_CLAUSE_FOR_UPDATE".equals(attribute.getValue())) {
					replaceExampleParameter(xmlElement);
					break;
				}
			}
			replaceCriteriaPackageNameInXml(xmlElement);
			modifyCommentInXml(xmlElement);
		}
    		return true;
    }

    
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String oldType = introspectedTable.getExampleType();
        Pattern pattern = Pattern.compile("Example$");
        Matcher matcher = pattern.matcher(oldType);
        String newType = matcher.replaceAll("Criteria");
        introspectedTable.setExampleWhereClauseId("WHERE_CLAUSE");
        introspectedTable.setExampleType(newType);
        introspectedTable.setSelectByExampleStatementId("selectByCriteria");
        introspectedTable.setDeleteByExampleStatementId("deleteByCriteria");
        introspectedTable.setUpdateByExampleSelectiveStatementId("updateByCriteriaSelective");
        introspectedTable.setUpdateByExampleStatementId("updateByCriteria");
        introspectedTable.setBaseColumnListId("BASE_COLUMN_LIST");
        if (introspectedTable.hasBLOBColumns()) {
        		introspectedTable.getBaseColumns().addAll(introspectedTable.getBLOBColumns());
        		introspectedTable.setResultMapWithBLOBsId("BASE_RESULT_MAP");
        		introspectedTable.getBLOBColumns().clear();
        }
        introspectedTable.setBaseResultMapId("BASE_RESULT_MAP");
        introspectedTable.setCountByExampleStatementId("countByCriteria");
        introspectedTable.setMyBatis3UpdateByExampleWhereClauseId("WHERE_CLAUSE_FOR_UPDATE");
    }
}