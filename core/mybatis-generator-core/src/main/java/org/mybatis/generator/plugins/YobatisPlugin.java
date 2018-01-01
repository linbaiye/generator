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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class YobatisPlugin extends PluginAdapter {

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
    
    private void replaceExampleParameter(XmlElement xmlElement)  {
    		XmlElement where = null;
    		for (Element element : xmlElement.getElements()) {
    			if (element instanceof XmlElement) {
    				where = (XmlElement)element;
    				break;
    			}
    		}
    		XmlElement foreach = null;
    		for (Element element: where.getElements()) {
    			if (element instanceof XmlElement) {
    				foreach = (XmlElement)element;
    				break;
    			}
    		}
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
    
    @Override
    public boolean sqlMapDocumentGenerated(Document document,
            IntrospectedTable introspectedTable) {
    		List<Element> elements = document.getRootElement().getElements();
    		for (Element element: elements) {
    			if (!(element instanceof XmlElement)) {
    				continue;
			}
			XmlElement xmlElement = (XmlElement) element;
    			XmlElement target = null;
			for (Attribute attribute : xmlElement.getAttributes()) {
				if ("id".equals(attribute.getName()) &&
						"WHERE_CLAUSE_FOR_UPDATE".equals(attribute.getValue())) {
					target = xmlElement;
					break;
				}
			}
			if (target != null) {
				replaceExampleParameter(target);
			}
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