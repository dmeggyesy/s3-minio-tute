package li.zah.s3miniotute.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import li.zah.s3miniotute.config.MinioConfigurator;
import li.zah.s3miniotute.dao.GeneratedLinkRepository;
import li.zah.s3miniotute.domain.GeneratedLink;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectStorageService {

  private final static String PROJECT_KEY = "project";

  private final static String USER_KEY = "user";

  @NonNull
  private MinioClient minioClient;

  @NonNull
  private GeneratedLinkRepository generatedLinkRepository;

  @PostConstruct
  public void init()
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(MinioConfigurator.defaultBucket).build())) {
      log.warn("Object Storage bucket does not exist - creating bucket {}", MinioConfigurator.defaultBucket);
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(MinioConfigurator.defaultBucket).build());
    }

  }

  public List<String> listBuckets()
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    List<String> buckets = new ArrayList<>();
    for (Bucket bucket : minioClient.listBuckets()) {
      buckets.add(bucket.name());
    }
    return buckets;
  }

  public List<String> listAllObjectsByTag()
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    List<String> res = new ArrayList<>();

    ListObjectsArgs args = ListObjectsArgs.builder().bucket(MinioConfigurator.defaultBucket).build();
    for (Result<Item> item : minioClient.listObjects(args)) {
      res.add(item.get().objectName());
    }
    return res;
  }

  public List<String> listAllObjectsByProjectAndUser(String projectId, String user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    List<Item> results = new ArrayList<>();

    ListObjectsArgs args = ListObjectsArgs.builder().bucket(MinioConfigurator.defaultBucket).build();
    for (Result<Item> res : minioClient.listObjects(args)) {

      Item item = res.get();

      GetObjectTagsArgs objArgs = GetObjectTagsArgs.builder().bucket(MinioConfigurator.defaultBucket).object(item.objectName()).build();
      Tags tags = minioClient.getObjectTags(objArgs);
      Map<String, String> tagMap = tags.get();
      if (tagMap.containsKey(PROJECT_KEY) && tagMap.containsKey(USER_KEY) && projectId.equals(tagMap.get(PROJECT_KEY))
          && user.equals(tagMap.get(USER_KEY))) {
        results.add(item);
      }

    }
    return results.stream().map(Item::objectName).collect(Collectors.toList());
  }

  public byte[] getObjectsByProjectAndUserAndObjectName(String projectId, String user, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    GetObjectTagsArgs tagArgs = GetObjectTagsArgs.builder().bucket(MinioConfigurator.defaultBucket).object(objectName).build();

    Tags tags = minioClient.getObjectTags(tagArgs);

    Map<String, String> tagMap = tags.get();
    if (!tagMap.containsKey(PROJECT_KEY) && !tagMap.containsKey(USER_KEY) && !projectId.equals(tagMap.get(PROJECT_KEY))
        && !user.equals(tagMap.get(USER_KEY))) {
      throw new IllegalArgumentException("No such file for given user/project");
    }

    GetObjectArgs args = GetObjectArgs.builder().bucket(MinioConfigurator.defaultBucket).object(objectName).build();

    return minioClient.getObject(args).readAllBytes();
  }

  public GeneratedLink getPreSignedUrlByProjectAndUserAndObjectName(String projectId, String user, String objectName)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    GetObjectTagsArgs tagArgs = GetObjectTagsArgs.builder().bucket(MinioConfigurator.defaultBucket).object(objectName).build();

    Tags tags = minioClient.getObjectTags(tagArgs);

    Map<String, String> tagMap = tags.get();
    if (!tagMap.containsKey(PROJECT_KEY) && !tagMap.containsKey(USER_KEY) && !projectId.equals(tagMap.get(PROJECT_KEY))
        && !user.equals(tagMap.get(USER_KEY))) {
      throw new IllegalArgumentException("No such file for given user/project");
    }

    GeneratedLink link = new GeneratedLink();
    link.setProjectId(projectId);

    // 10mins
    int expiry = 10 * 60;

    link.setExpiry(LocalDateTime.now().plusSeconds(expiry));
    link.setTtl(expiry);

    String linkString = minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(MinioConfigurator.defaultBucket).object(objectName).expiry(expiry, TimeUnit.SECONDS).build());

    link.setLink(new URL(linkString));

    return generatedLinkRepository.save(link);
  }

  public List<GeneratedLink> getAllCurrentPresignedLinks(String projectId) {
    return generatedLinkRepository.findGeneratedLinksByProjectId(projectId);
  }

}
