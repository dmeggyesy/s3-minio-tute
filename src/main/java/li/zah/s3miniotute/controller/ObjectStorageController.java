package li.zah.s3miniotute.controller;

import static li.zah.s3miniotute.config.ApiEndpoints.ALL_PROJECTS;
import static li.zah.s3miniotute.config.ApiEndpoints.LIST_ALL_BUCKETS;
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
import li.zah.s3miniotute.service.ObjectStorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

}