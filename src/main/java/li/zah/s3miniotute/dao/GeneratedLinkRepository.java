package li.zah.s3miniotute.dao;

import java.util.List;
import li.zah.s3miniotute.domain.GeneratedLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GeneratedLinkRepository extends MongoRepository<GeneratedLink, String> {

  List<GeneratedLink> findGeneratedLinksByProjectId(String projectId);
}
