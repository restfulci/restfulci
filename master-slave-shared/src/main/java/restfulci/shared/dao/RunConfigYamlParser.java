package restfulci.shared.dao;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import restfulci.shared.domain.RunConfigBean;

class RunConfigYamlParser {

	static RunConfigBean parse(String yamlContent) throws YAMLException {
		
		Constructor constructor = new Constructor(RunConfigBean.class);
		Yaml yaml = new Yaml(constructor);
		
		RunConfigBean runConfig = yaml.load(yamlContent);
		return runConfig;
		
		/*
		 * TODO:
		 * Validate YAML file. Non-null element, either image/build, ...
		 * Not sure if that can be defined through annotations, or there need to
		 * be if-else here.
		 */
	}
}
