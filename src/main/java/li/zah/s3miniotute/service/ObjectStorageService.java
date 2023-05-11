package li.zah.s3miniotute.service;

import io.minio.MinioClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

  @NonNull
  private MinioClient minioClient;

}
