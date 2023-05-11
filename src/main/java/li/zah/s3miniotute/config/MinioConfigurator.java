package li.zah.s3miniotute.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfigurator {

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.key.access}")
  private String accessKey;

  @Value("${minio.key.secret}")
  private String secretKey;

  private MinioClient minioClient;

  @Bean
  public MinioClient minioClient() {
    if (minioClient == null) {
      minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }
    return minioClient;
  }

}
