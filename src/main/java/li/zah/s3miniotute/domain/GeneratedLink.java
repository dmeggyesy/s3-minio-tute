package li.zah.s3miniotute.domain;

import java.net.URL;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ephemeral.links")
@AllArgsConstructor
@RequiredArgsConstructor
public class GeneratedLink {

  @Id
  private String id;

  @Indexed
  private String projectId;

  @Indexed(expireAfterSeconds = 0)
  private LocalDateTime expiry;

  private int ttl = 20;

  private URL link;

}
