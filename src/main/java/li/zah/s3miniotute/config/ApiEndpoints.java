package li.zah.s3miniotute.config;

public class ApiEndpoints {

  public final static String BASE = "/api";

  public final static String LIST_ALL_BUCKETS = BASE + "/list";

  public final static String ALL_PROJECTS = BASE + "/project";

  public final static String PROJECT = ALL_PROJECTS + "/{projectId}";

}
