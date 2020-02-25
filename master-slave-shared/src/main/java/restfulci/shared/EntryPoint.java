package restfulci.shared;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

/*
 * TODO:
 * Cannot move dao classes to shared, otherwise master-api errors out 
 * that CrubRepository DAO cannot be autowired.
 * > Field jobRepository in restfulci.master.service.JobServiceImpl required 
 * > a bean of type 'restfulci.shared.dao.JobRepository' that could not be found.
 * 
 * Seems `@ComponentScan` doesn't help either. 
 */
@Configuration
@EntityScan(basePackages = {"restfulci.shared"})
public class EntryPoint {

}
