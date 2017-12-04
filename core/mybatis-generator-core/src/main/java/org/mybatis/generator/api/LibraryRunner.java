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
package org.mybatis.generator.api;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

/**
 * This class allows the code generator to be run from the command line.
 * 
 * @author Jeff Butler
 */
public class LibraryRunner {

	private MyBatisGenerator myBatisGenerator;

	public void parse(String configFile) throws InvalidConfigurationException {

		List<String> warnings = new ArrayList<String>();

		File configurationFile = new File(configFile);
		if (!configurationFile.exists()) {
			throw new InvalidConfigurationException(Arrays.asList("Configuration file does not exist."));
		}

		try {
			ConfigurationParser cp = new ConfigurationParser(warnings);
			Configuration config = cp.parseConfiguration(configurationFile);

			DefaultShellCallback shellCallback = new DefaultShellCallback(false);

			myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);

			myBatisGenerator.generate(null, null, null, false);

		} catch (XMLParserException e) {
			throw new InvalidConfigurationException(e);
		} catch (SQLException e) {
			throw new InvalidConfigurationException(e);
		} catch (IOException e) {
			throw new InvalidConfigurationException(e);
		} catch (InterruptedException e) {
			// ignore (will never happen with the DefaultShellCallback)
		}
	}

	public List<GeneratedXmlFile> getGeneratedXmlFiles() {
		return myBatisGenerator.getGeneratedXmlFiles();

	}

	public List<GeneratedJavaFile> getGeneratedJavaFiles() {
		return myBatisGenerator.getGeneratedJavaFiles();
	}
}
