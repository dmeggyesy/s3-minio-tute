package li.zah.s3miniotute.controller;

import static li.zah.s3miniotute.config.ApiEndpoints.ALL_LINKS;
import static li.zah.s3miniotute.config.ApiEndpoints.ALL_PROJECTS;
import static li.zah.s3miniotute.config.ApiEndpoints.LIST_ALL_BUCKETS;
import static li.zah.s3miniotute.config.ApiEndpoints.OBJECT;
import static li.zah.s3miniotute.config.ApiEndpoints.OBJECT_LINK;
import static li.zah.s3miniotute.config.ApiEndpoints.PROJECT;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import li.zah.s3miniotute.domain.GeneratedLink;
import li.zah.s3miniotute.service.ObjectStorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ObjectStorageController {

  @NonNull
  private final ObjectStorageService objectStorageService;

  private List<String> projects = new ArrayList<>();

  @RequestMapping(value = { LIST_ALL_BUCKETS }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody List<String> getBucketList()
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    log.info("List all buckets");

    return objectStorageService.listBuckets();
  }

  @RequestMapping(value = { ALL_PROJECTS }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody List<String> getAllProjects() {
    log.info("List all projects");

    return projects;
  }

  @RequestMapping(value = { PROJECT }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody List<String> getProjectObjects(@PathVariable String projectId)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    log.info("List objects by project and user {} - {}", projectId, auth.getName());

    return objectStorageService.listAllObjectsByProjectAndUser(projectId, auth.getName());
  }

  @RequestMapping(value = { OBJECT }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<StreamingResponseBody> getProjectObject(@PathVariable String projectId,
      @PathVariable String objectName) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    log.info("Get objects by project, user and name {} - {} - {}", projectId, auth.getName(), objectName);

    StreamingResponseBody responseBody = outputStream -> {
      try {
        outputStream.write(
            objectStorageService.getObjectsByProjectAndUserAndObjectName(projectId, auth.getName(), objectName));
      } catch (ServerException | InvalidKeyException | NoSuchAlgorithmException | InsufficientDataException |
               ErrorResponseException | InvalidResponseException | XmlParserException | InternalException e) {
        throw new RuntimeException(e);
      }
    };

    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + objectName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(responseBody);

  }

  @RequestMapping(value = { OBJECT_LINK }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public GeneratedLink getProjectObjectLink(@PathVariable String projectId, @PathVariable String objectName)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    log.info("Get object magic link by project, user and name {} - {} - {}", projectId, auth.getName(), objectName);

    return objectStorageService.getPreSignedUrlByProjectAndUserAndObjectName(projectId, auth.getName(), objectName);

  }

  @RequestMapping(value = { ALL_LINKS }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public List<GeneratedLink> getAllProjectObjectLinks(@PathVariable String projectId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    log.info("Get  all object magic link by project, user and name {} - {} ", projectId, auth.getName());

    return objectStorageService.getAllCurrentPresignedLinks(projectId);

  }

}
