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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.w3c.dom.Attr;


public class YobatisPlugin extends PluginAdapter {
	
	private final static Map<String, String[]> GENERATED_KEY_JAVADOCS = new HashMap<String, String[]>();
	
    //long countByCriteria(BookCriteria criteria);
	private final static String[] COUNT_BY_CRITERIA_JAVADOC = new String[] {
			"/**",
			" * Count the number of selected records.",
			" * @param criteria the criteria to select records.",
			" * @return the number.",
			" * @throws PersistenceException if null or empty criteria passed.",
			" */"};

	//long countAll();
	private final static String[] COUNT_ALL_JAVADOC = new String[] {
			"/**",
			" * Count the record number of the whole table.",
			" * @return the number.",
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
			" * @param %s the primary key.",
			" * @return 1 if the record has been deleted, 0 if not.",
			" */"};

	//int insertExceptPrimaryKey(Customer record);
	private final static String[] INSERT_EXCEPT_PK_JAVADOC = new String[] {
			"/**",
			" * Insert a record.",
			" * <p>All fields except the primary key will be inserted regardless of whether",
			" * null or not, the primary key field(id) will hold the generated value after insertion.",
			" * <p>Passing null has the same effect of passing a record whose fields are null.",
			" * @param record the record to insert.",
			" * @return 1 if the record has been inserted.",
			" */"};
	
	//int insert(Customer record);
	private final static String[] INSERT_JAVADOC = new String[] {
			"/**",
			" * Insert a record, all fields will be inserted regardless of whether null or not.",
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
			" * @param %s the primary key.",
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
			" * null fields are ignored. Passing null has the same effect of passing a",
			" * record whose fields are all null, shown as below.",
			" * <pre>update table set field1 = null, filed2 = null where pk = null</pre>",
			" * @param record the record that holds new values.",
			" * @return 1 the record is updated based on the primary key, 0 else.",
			" */"};

    //int updateByPrimaryKey(Customer record);
	private final static String[] UPDATE_BY_PK_JAVADOC = new String[] {
			"/**",
			" * Update record's columns to corresponding fields in {@code record}, regardless of",
			" * whether the field is null or not. Passing null has the same effect of passing a",
			" * record whose fields are all null, shown as below.",
			" * <pre>update table set field1 = null, filed2 = null where pk = null</pre>",
			" * @param record the record that holds new values.",
			" * @return 1 the record is updated based on the primary key, 0 else.",
			" */"};

	private final static Set<String> INTEGER_MAP = new HashSet<String>();
	

	static {
		GENERATED_KEY_JAVADOCS.put("countByCriteria", COUNT_BY_CRITERIA_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("countAll", COUNT_ALL_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("deleteByCriteria", DELETE_BY_CRITERIA_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("deleteByPrimaryKey", DELETE_BY_PK_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("insertExceptPrimaryKey", INSERT_EXCEPT_PK_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("insertSelective", INSERT_SELECTIVE_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("insert", INSERT_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("selectByCriteria", SELECT_CRITERIA_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("selectByPrimaryKey", SELECT_PK_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("updateByCriteriaSelective", UPDATE_SELECTIVE_BY_CRITERIA_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("updateByCriteria", UPDATE_BY_CRITERIA_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("updateByPrimaryKeySelective", UPDATE_SELECTIVE_BY_PK_JAVADOC);
		GENERATED_KEY_JAVADOCS.put("updateByPrimaryKey", UPDATE_BY_PK_JAVADOC);
		
		INTEGER_MAP.add("java.lang.Long");
		INTEGER_MAP.add("java.lang.Integer");
		INTEGER_MAP.add("java.lang.Short");
		INTEGER_MAP.add("java.lang.Byte");
	}

	private List<GeneratedJavaFile> baseDomains = new LinkedList<GeneratedJavaFile>();

	private Boolean enableBaseModel = false;
	
	private Context context;
	
	@Override
	public void setContext(Context context) {
		this.context = context;
	}


	@Override
	public boolean validate(List<String> warnings) {
		String tmp = properties.getProperty("enableBaseClass");
		enableBaseModel = Boolean.valueOf(tmp);
		if (enableBaseModel == null) {
			enableBaseModel = false;
		}
		return true;
	}
	
	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
		return baseDomains;
	}
	
	private void generateBaseAndSubDoamins(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String name = topLevelClass.getType().getFullyQualifiedName();
		String shortName = topLevelClass.getType().getShortName();
		name = name.replace(shortName, "Base" + shortName);
		topLevelClass.setSuperClass(new FullyQualifiedJavaType(name));

		TopLevelClass baseClass = new TopLevelClass(new FullyQualifiedJavaType(name));
		baseClass.setAbstract(true);
		baseClass.setVisibility(JavaVisibility.PUBLIC);
		for (Field field : topLevelClass.getFields()) {
			field.setVisibility(JavaVisibility.PROTECTED);
			baseClass.addField(field);
		}
		topLevelClass.getFields().clear();

		for (Method method : topLevelClass.getMethods()) {
			baseClass.addMethod(method);
		}
		topLevelClass.getMethods().clear();

		for (FullyQualifiedJavaType type : topLevelClass.getImportedTypes()) {
			baseClass.addImportedType(type);
		}
		topLevelClass.getImportedTypes().clear();
		
		baseClass.addJavaDocLine("/*");
		baseClass.addJavaDocLine(" * This class corresponds to the table '" + introspectedTable.getFullyQualifiedTable() 
		+ "', and it is generated by MyBatis Generator.");
		baseClass.addJavaDocLine(" * Do NOT modify as it will be overwrote every time MyBatis Generator runs,");
		baseClass.addJavaDocLine(" * put artificial code into " + shortName + " instead.");
		baseClass.addJavaDocLine(" */");

		topLevelClass.addJavaDocLine("/*");
		topLevelClass.addJavaDocLine(" * Add logical code here.");
		topLevelClass.addJavaDocLine(" */");
		JavaModelGeneratorConfiguration configuration = context.getJavaModelGeneratorConfiguration();
		GeneratedJavaFile javaFile = new GeneratedJavaFile(baseClass, 
				configuration.getTargetProject(), context.getJavaFormatter());
		baseDomains.add(javaFile);
	}

    private void renameExampledMethods(Method method) {
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
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		renameExampledMethods(method);
		return true;
	}

	@Override
	public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		renameExampledMethods(method);
		return true;
	}

	@Override
	public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		renameExampledMethods(method);
		return true;
	}

	@Override
	public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return false;
	}
    
	@Override
	public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		renameExampledMethods(method);
		return true;
	}

	@Override
	public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		renameExampledMethods(method);
		return true;
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		disableNullCriteria(element);
		return true;
	}

	@Override
	public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		return false;
	}

    private void addIfToUpdateByCriteria(XmlElement element) {
    	XmlElement ifElement = new XmlElement("if");
		ifElement.addAttribute(new Attribute("test", "record != null"));
		int position = 0;
		for (Element e : element.getElements()) {
			if (e instanceof TextElement) {
				TextElement textElement = (TextElement) e;
				if ("-->".equals(textElement.getContent().trim())) {
					++position;
					break;
				}
			}
			position++;
		}
		List<Element> firstPart = new LinkedList<Element>();
		for (Element tmp : element.getElements().subList(0, position)) {
			firstPart.add(tmp);
		}
		List<Element> lastPart = new LinkedList<Element>();
		for (Element tmp : element.getElements().subList(position, element.getElements().size())) {
			lastPart.add(tmp);
		}
		element.getElements().clear();
		element.getElements().addAll(firstPart);
		for (Element e : lastPart) {
			ifElement.addElement(e);
		}
		element.getElements().add(ifElement);
    }


    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(
        XmlElement element, IntrospectedTable introspectedTable) {
    	disableNullCriteria(element);
    	addIfToUpdateByCriteria(element);
    	return true;
    }
  
    
    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
    		disableNullCriteria(element);
    		return true;
    }
    
    @Override
    public boolean sqlMapCountByExampleElementGenerated(
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
		for (org.mybatis.generator.api.dom.java.Field field : topLevelClass.getFields()) {
			field.getJavaDocLines().clear();
		}

		for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
			innerClass.getJavaDocLines().clear();
		}
	}
    
	private void throwExceptionWhenEmptyCrtieriaList(TopLevelClass topLevelClass) {
		for (int i = 0; i < topLevelClass.getMethods().size(); i++) {
			Method method = topLevelClass.getMethods().get(i);
			if ("getOredCriteria".equals(method.getName())) {
				Method newMethod = new Method(method);
				newMethod.setName("getMybatisOredCriteria");
				newMethod.addBodyLine(0, "}");
				newMethod.addBodyLine(0, "throw new PersistenceException(\"Empty criteria.\");");
				newMethod.addBodyLine(0, "if (oredCriteria.isEmpty()) {");
				newMethod.addJavaDocLine("/*");
				newMethod.addJavaDocLine(" * Using empty oredCriteria is probably not desired, as it enables mybatis");
				newMethod.addJavaDocLine(
						" * to execute sql against the whole table, it may cause a catastrophe accidentally.");
				newMethod.addJavaDocLine(" */");
				topLevelClass.getMethods().add(i + 1, newMethod);
				break;
			}
		}
		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.exceptions.PersistenceException"));
	}


	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		FullyQualifiedJavaType type = topLevelClass.getType();
		try {
			addCriteriaToPackageName(type);
			removeJavadoc(topLevelClass);
			topLevelClass.addJavaDocLine("/*");
			topLevelClass.addJavaDocLine(" * This class is generated by MyBatis Generator, do NOT modify.");
			topLevelClass.addJavaDocLine(" * Extend this class if it is found insufficient.");
			topLevelClass.addJavaDocLine(" */");
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
    	if (!enableBaseModel) {
    		topLevelClass.addJavaDocLine("/*");
    		topLevelClass.addJavaDocLine(" * This class corresponds to the table '" + introspectedTable.getFullyQualifiedTable() 
    		+ "', it is generated by MyBatis Generator.");
    		topLevelClass.addJavaDocLine(" * Do NOT modify as it will be overwrote every time MyBatis Generator runs, extend or");
    		topLevelClass.addJavaDocLine(" * encapsulate it instead.");
    		topLevelClass.addJavaDocLine(" */");
    	} else {
    		generateBaseAndSubDoamins(topLevelClass, introspectedTable);
    	}
    	return true;
    }

    
	private void modifyMethodParameterTypeAndJavadoc(List<Method> methods, IntrospectedTable introspectedTable) {
		for (Method method : methods) {
			List<Parameter> parameters = method.getParameters();
			Iterator<Parameter> iterator = parameters.iterator();
			while (iterator.hasNext()) {
				Parameter parameter = iterator.next();
				Matcher matcher = PACKAGE_PATTERN.matcher(parameter.getType().getFullyQualifiedName());
				if (matcher.find()) {
					Parameter newParameter = new Parameter(
							new FullyQualifiedJavaType(matcher.replaceFirst(".criteria.$1")), parameter.getName());
					for (String annotation : parameter.getAnnotations()) {
						newParameter.addAnnotation(annotation);
					}
					iterator.remove();
					parameters.add(newParameter);
					break;
				}
			}
			method.getJavaDocLines().clear();
			if (isAutoincKey(introspectedTable)) {
				List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
				IntrospectedColumn column = columns.get(0);
				String[] javdoc = GENERATED_KEY_JAVADOCS.get(method.getName());
				if (javdoc != null) {
					for (String line : javdoc) {
						if (line.contains("%")) {
							line = String.format(line, column.getJavaProperty());
						}
						method.addJavaDocLine(line);
					}
				}
			}
		}
	}
    
    /* import xxx.XxxCriteria -> import xxx.criteria.XxxCriteria */
    private void addCriteriaToImport(Interface inerfaze) {
 		Set<FullyQualifiedJavaType> types = inerfaze.getImportedTypes();
		Iterator<FullyQualifiedJavaType> iterator = types.iterator();
		while (iterator.hasNext()) {
			FullyQualifiedJavaType type = iterator.next();
			Matcher matcher = PACKAGE_PATTERN.matcher(type.getFullyQualifiedName());
			if (matcher.find()) {
				iterator.remove();
				FullyQualifiedJavaType newType = new FullyQualifiedJavaType(matcher.replaceFirst(".criteria.$1"));
				types.add(newType);
				break;
			}
		}
    }
    
	private boolean isAutoincKey(IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
		if (introspectedTable.getPrimaryKeyColumns().size() != 1) {
			return false;
		}
		if (introspectedTable.getGeneratedKey() == null || !introspectedTable.getGeneratedKey().isIdentity()) {
			return false;
		}
		IntrospectedColumn column = columns.get(0);
		return INTEGER_MAP.contains(column.getFullyQualifiedJavaType().getFullyQualifiedName());
	}
	
	
    
	/* rename insert() method to insertExceptPrimaryKey(). */
	private List<Parameter> renameInsertToInsertExceptPrimaryKey(Interface interfaze) {
		for (Method method : interfaze.getMethods()) {
			if ("insert".equals(method.getName())) {
				method.setName("insertExceptPrimaryKey");
				return method.getParameters();
			}
		}
		return null;
	}


	private void addInsertMethod(Interface interfaze, List<Parameter> parameters) {
		for (int i = 0; i < interfaze.getMethods().size(); i++) {
			Method method = interfaze.getMethods().get(i);
			if ("insertExceptPrimaryKey".equals(method.getName())) {
				Method newMethod = new Method("insert");
				newMethod.setReturnType(new FullyQualifiedJavaType("int"));
				for (Parameter parameter : parameters) {
					newMethod.addParameter(parameter);
				}
				interfaze.getMethods().add(i + 1, newMethod);
				break;
			}
		}
	}


	private void addCountAllMethod(Interface interfaze) {
		int pos = 0;
		for (Method method : interfaze.getMethods()) {
			if (method.getName().contains("countBy")) {
				Method newMethod = new Method(method);
				newMethod.setName("countAll");
				newMethod.getParameters().clear();
				interfaze.getMethods();
				interfaze.getMethods().add(pos + 1, newMethod);
				return;
			}
			pos++;
		}
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		addCriteriaToImport(interfaze);
		addCountAllMethod(interfaze);
		if (isAutoincKey(introspectedTable)) {
			List<Parameter> parameters = renameInsertToInsertExceptPrimaryKey(interfaze);
			addInsertMethod(interfaze, parameters);
		}
		modifyMethodParameterTypeAndJavadoc(interfaze.getMethods(), introspectedTable);
		interfaze.addJavaDocLine("/*");
		interfaze.addJavaDocLine(" * This class is generated by MyBatis Generator, and it is safe to modify.");
		interfaze.addJavaDocLine(" */");
		return true;
	}
    

	private void addCriteriaToPackageName(FullyQualifiedJavaType type) {
		try {
			java.lang.reflect.Field field = type.getClass().getDeclaredField("packageName");
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

	// Prohibit passing null criteria which will
	// take effect on the whole table.
	private void disableNullCriteria(XmlElement element) {
		XmlElement ifElement = null;
		Iterator<Element> iterator = element.getElements().iterator();
		int position = 0;
		while (iterator.hasNext()) {
			Element e = iterator.next();
			if (!(e instanceof XmlElement)) {
				position++;
				continue;
			}
			XmlElement xmlElement = (XmlElement) e;
			if (!"if".equals(xmlElement.getName())) {
				position++;
				continue;
			}
			for (Attribute attribute : xmlElement.getAttributes()) {
				if ("test".equals(attribute.getName()) && "_parameter != null".equals(attribute.getValue())) {
					ifElement = xmlElement;
					break;
				}
			}
			if (ifElement != null) {
				List<Element> subElements = ifElement.getElements();
				iterator.remove();
				element.getElements().addAll(position, subElements);
				break;
			}
			position++;
		}
	}

	@Override
	public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		disableNullCriteria(element);
		return true;
	}
    
    
	private void replaceCriteriaPackageNameInXml(XmlElement xmlElement) {
		Iterator<Attribute> iterator = xmlElement.getAttributes().iterator();
		Attribute newAttribute = null;
		while (iterator.hasNext()) {
			Attribute attribute = iterator.next();
			if (!"parameterType".equals(attribute.getName())) {
				continue;
			}
			// Replace the criteria's package name.
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
			} else if (commentStart && (element.getFormattedContent(0).contains("This element was generated"))) {
				// Remove this comment since it contains a timestamp which
				// differs from time to time.
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
    
	private void renameInsertToInsertExceptPrimaryKey(Document document) {
		List<Element> elements = document.getRootElement().getElements();
		for (Element e : elements) {
			if (!(e instanceof XmlElement)) {
				continue;
			}
			XmlElement element = (XmlElement) e;
			if (!"insert".equals(element.getName())) {
				continue;
			}
			Iterator<Attribute> iterator = element.getAttributes().iterator();
			while (iterator.hasNext()) {
				Attribute attribute = iterator.next();
				if ("id".equals(attribute.getName()) && "insert".equals(attribute.getValue())) {
					Attribute newAttribute = new Attribute("id", "insertExceptPrimaryKey");
					iterator.remove();
					element.getAttributes().add(0, newAttribute);
					return;
				}
			}
		}
	}
    
	private XmlElement copyAttributesOfOriginalInsert(Document document) {
		List<Element> elements = document.getRootElement().getElements();
		XmlElement xmlElement = new XmlElement("insert");
		XmlElement orginalElement = null;
		for (Element e : elements) {
			if (!(e instanceof XmlElement)) {
				continue;
			}
			XmlElement element = (XmlElement) e;
			for (Attribute attribute : element.getAttributes()) {
				if ("id".equals(attribute.getName()) && "insertExceptPrimaryKey".equals(attribute.getValue())) {
					orginalElement = element;
					break;
				}
			}
		}
		if (orginalElement == null) {
			return null;
		}
		for (Attribute attribute : orginalElement.getAttributes()) {
			if ("id".equals(attribute.getName()) && "insertExceptPrimaryKey".equals(attribute.getValue())) {
				xmlElement.addAttribute(new Attribute("id", "insert"));
			} else {
				xmlElement.addAttribute(attribute);
			}
		}
		return xmlElement;
	}
	
	
	private XmlElement addContentToInsertXml(IntrospectedTable table, XmlElement xmlElement) {
 		StringBuilder stringBuilder = new StringBuilder();
 		xmlElement.addElement(new TextElement("<!--"));
 		xmlElement.addElement(new TextElement("  WARNING - @mbg.generated"));
 		xmlElement.addElement(new TextElement("  This element is automatically generated by MyBatis Generator, do not modify."));
 		xmlElement.addElement(new TextElement("-->"));
		stringBuilder.append("insert into ");
		stringBuilder.append(table.getFullyQualifiedTable().getIntrospectedTableName());
		stringBuilder.append(" (");
		for (int i = 0; i < table.getAllColumns().size(); i++) {
			IntrospectedColumn column = table.getAllColumns().get(i);
			stringBuilder.append(column.getActualColumnName());
			stringBuilder.append(i == table.getAllColumns().size() - 1? ")" : ", ");
			if (i != table.getAllColumns().size() - 1 && (i + 1) % 4 == 0) {
				xmlElement.addElement(new TextElement(stringBuilder.toString()));
				stringBuilder = new StringBuilder();
				stringBuilder.append("  ");
			}
		}
		TextElement textElement = new TextElement(stringBuilder.toString());
		xmlElement.addElement(textElement);
		stringBuilder = new StringBuilder();
		stringBuilder.append("values (");
		for (int i = 0; i < table.getAllColumns().size(); i++) {
			IntrospectedColumn column = table.getAllColumns().get(i);
			stringBuilder.append("#{");
			stringBuilder.append(column.getJavaProperty());
			stringBuilder.append(",jdbcType=");
			stringBuilder.append(column.getJdbcTypeName());
			stringBuilder.append("}");
			stringBuilder.append(i == table.getAllColumns().size() - 1? ")" : ", ");
			if (i != table.getAllColumns().size() - 1 &&  (i + 1) % 4 == 0) {
				xmlElement.addElement(new TextElement(stringBuilder.toString()));
				stringBuilder = new StringBuilder();
				stringBuilder.append("  ");
			}
		}
		textElement = new TextElement(stringBuilder.toString());
		xmlElement.addElement(textElement);
		return xmlElement;
	}
    
    
	private void addInsertXml(Document document, IntrospectedTable table) {
		XmlElement xmlElement = copyAttributesOfOriginalInsert(document);
		if (xmlElement == null) {
			return;
		}
		addContentToInsertXml(table, xmlElement);
		List<Element> elements = document.getRootElement().getElements();
		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i);
			if (!(e instanceof XmlElement)) {
				continue;
			}
			XmlElement element = (XmlElement) e;
			if (!"insert".equals(element.getName())) {
				continue;
			}
			for (Attribute attribute : element.getAttributes()) {
				if ("id".equals(attribute.getName()) && "insertExceptPrimaryKey".equals(attribute.getValue())) {
					elements.add(i + 1, xmlElement);
					return;
				}
			}
		}
	}
    
	@Override
	public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		XmlElement where = findXmlElement(element, "where");
		if (where == null) {
			return true;
		}
		XmlElement foreach = findXmlElement(where, "foreach");
		if (foreach == null) {
			return true;
		}
		Iterator<Attribute> iterator = foreach.getAttributes().iterator();
		while (iterator.hasNext()) {
			Attribute attribute = iterator.next();
			if ("collection".equals(attribute.getName())) {
				String tmp = "mybatisOredCriteria";
				if (attribute.getValue().startsWith("example.")) {
					tmp = "criteria.mybatisOredCriteria";
				}
				iterator.remove();
				foreach.getAttributes().add(new Attribute("collection", tmp));
				break;
			}
		}
		return true;
	}
    
    
    private void addCountAll(Document document, IntrospectedTable introspectedTable) {
    		XmlElement xmlElement = new XmlElement("select");
    		xmlElement.addAttribute(new Attribute("id", "countAll"));
    		xmlElement.addAttribute(new Attribute("resultType", "java.lang.Long"));
     	xmlElement.addElement(new TextElement("<!--"));
     	xmlElement.addElement(new TextElement("  WARNING - @mbg.generated"));
     	xmlElement.addElement(new TextElement("  This element is automatically generated by MyBatis Generator, do not modify."));
     	xmlElement.addElement(new TextElement("-->"));
     	xmlElement.addElement(new TextElement("select count(*) from " + introspectedTable.getFullyQualifiedTable().getIntrospectedTableName()));
     	List<Element> elements = document.getRootElement().getElements();
     	for (int i = 0; i < elements.size(); i++) {
     		Element element = elements.get(i);
     		if (!(element instanceof XmlElement)) {
     			continue;
     		}
     		XmlElement e = (XmlElement)element;
     		for (Attribute attribute : e.getAttributes()) {
     			if ("id".equals(attribute.getName()) && 
     					attribute.getValue().equals("countByCriteria")) {
     				elements.add(i + 1, xmlElement);
     				return;
     			}
     		}
     	}
    }

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		if (isAutoincKey(introspectedTable)) {
			renameInsertToInsertExceptPrimaryKey(document);
			addInsertXml(document, introspectedTable);
		}
		List<Element> elements = document.getRootElement().getElements();
		for (Element element : elements) {
			if (!(element instanceof XmlElement)) {
				continue;
			}
			XmlElement xmlElement = (XmlElement) element;
			replaceCriteriaPackageNameInXml(xmlElement);
			modifyCommentInXml(xmlElement);
		}
		addCountAll(document, introspectedTable);
		XmlElement root = document.getRootElement();
		root.addElement(0, new TextElement("-->"));
		root.addElement(0, new TextElement("  modify generated ones."));
		root.addElement(0, new TextElement("  This file is generated by MyBatis Generator, it is safe to add new elements but not"));
		root.addElement(0, new TextElement("<!--"));
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