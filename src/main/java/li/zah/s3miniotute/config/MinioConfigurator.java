package li.zah.s3miniotute.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfigurator {

  private MinioClient minioClient;

  @Bean
  public MinioClient minioClient() {
    if (minioClient == null) {
      minioClient = MinioClient.builder().build();
    }
    return minioClient;
  }

}
