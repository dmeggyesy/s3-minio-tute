package li.zah.s3miniotute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import li.zah.s3miniotute.dao.GeneratedLinkRepository;
import li.zah.s3miniotute.domain.GeneratedLink;
import li.zah.s3miniotute.service.ObjectStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ObjectStorageServiceIT {

  @Autowired
  private ObjectStorageService objectStorageService;

  @Autowired
  private GeneratedLinkRepository generatedLinkRepository;

  @Test
  public void objectStorageServiceTest()
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    objectStorageService.listBuckets();
  }

  @Test
  public void mongoTTL() throws InterruptedException {

    GeneratedLink link = new GeneratedLink();
    link.setProjectId("a");
    LocalDateTime expiry = LocalDateTime.now();

    link.setExpiry(expiry.plusSeconds(20));
    generatedLinkRepository.save(link);

    GeneratedLink link2 = new GeneratedLink();
    link2.setProjectId("a");
    link2.setTtl(120);
    link2.setExpiry(expiry.plusSeconds(250));
    generatedLinkRepository.save(link2);

    assertEquals(generatedLinkRepository.count(), 2);

    Thread.sleep(61000);

    assertEquals(generatedLinkRepository.count(), 1);

  }

}
