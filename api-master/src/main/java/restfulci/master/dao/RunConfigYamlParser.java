package restfulci.master.dao;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import restfulci.master.domain.RunConfigBean;

class RunConfigYamlParser {

	static RunConfigBean parse(String yamlContent) throws YAMLException {
		
		Constructor constructor = new Constructor(RunConfigBean.class);
		Yaml yaml = new Yaml(constructor);
		
		RunConfigBean runConfig = yaml.load(yamlContent);
		return runConfig;
	}
}
