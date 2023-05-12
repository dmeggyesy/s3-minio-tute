package li.zah.s3miniotute.service;

import io.minio.GetObjectArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import li.zah.s3miniotute.config.MinioConfigurator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

  private final static String PROJECT_KEY = "project";

  private final static String USER_KEY = "user";

  @NonNull
  private MinioClient minioClient;

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
    System.out.println(MinioConfigurator.defaultBucket);
    ListObjectsArgs args = ListObjectsArgs.builder().bucket(MinioConfigurator.defaultBucket).build();
    for (Result<Item> item : minioClient.listObjects(args)) {

      System.out.println(item.get().objectName());

    }
    return null;
  }

  public List<String> listAllObjectsByProjectAndUser(String projectId, String user)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    List<Item> results = new ArrayList<>();

    ListObjectsArgs args = ListObjectsArgs.builder().bucket(MinioConfigurator.defaultBucket).build();
    for (Result<Item> res : minioClient.listObjects(args)) {

      Item item = res.get();

      GetObjectTagsArgs objArgs = GetObjectTagsArgs.builder().bucket(MinioConfigurator.defaultBucket)
          .object(item.objectName()).build();
      Tags tags = minioClient.getObjectTags(objArgs);
      Map<String, String> tagMap = tags.get();
      if (tagMap.containsKey(PROJECT_KEY) && tagMap.containsKey(USER_KEY) && projectId.equals(tagMap.get(PROJECT_KEY))
          && user.equals(tagMap.get(USER_KEY))) {
        results.add(item);
      }

    }
    return results.stream().map(Item::objectName).collect(Collectors.toList());
  }

  public byte[] getObjectsByProjectAndUserAndObjectName(String projectId, String user, String objectName)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    GetObjectTagsArgs tagArgs = GetObjectTagsArgs.builder().bucket(MinioConfigurator.defaultBucket).object(objectName)
        .build();

    Tags tags = minioClient.getObjectTags(tagArgs);

    Map<String, String> tagMap = tags.get();
    if (!tagMap.containsKey(PROJECT_KEY) && !tagMap.containsKey(USER_KEY) && !projectId.equals(tagMap.get(PROJECT_KEY))
        && !user.equals(tagMap.get(USER_KEY))) {
      throw new IllegalArgumentException("No such file for given user/project");
    }

    GetObjectArgs args = GetObjectArgs.builder().bucket(MinioConfigurator.defaultBucket).object(objectName).build();

    return minioClient.getObject(args).readAllBytes();
  }

}
