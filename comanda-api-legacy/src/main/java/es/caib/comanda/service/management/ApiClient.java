/*
 * comanda-api-interna
 */


package es.caib.comanda.service.management;

import com.fasterxml.jackson.databind.JavaType;
import es.caib.comanda.service.management.auth.ApiKeyAuth;
import es.caib.comanda.service.management.auth.Authentication;
import es.caib.comanda.service.management.auth.HttpBasicAuth;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiClient {
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_ACCEPT = "Accept";
  private static final String HEADER_COOKIE = "Cookie";
  private static final String MEDIA_TYPE_JSON = "application/json";
  private static final String MEDIA_TYPE_FORM = "application/x-www-form-urlencoded";
  private static final String MEDIA_TYPE_MULTIPART = "multipart/form-data";
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  protected Map<String, String> defaultHeaderMap = new HashMap<String, String>();
  protected Map<String, String> defaultCookieMap = new HashMap<String, String>();
  protected String basePath = "http://localhost";
  protected boolean debugging = false;
  protected JSON json;
  protected String tempFolderPath = null;
  protected Map<String, Authentication> authentications;
  protected int statusCode;
  protected Map<String, List<String>> responseHeaders;
  protected DateFormat dateFormat;
  protected int connectTimeoutMillis;
  protected int readTimeoutMillis;

  public ApiClient() {
    json = new JSON();

    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.json.setDateFormat((DateFormat) dateFormat.clone());

    setUserAgent("OpenAPI-Generator/1.0.0/java");

    authentications = new HashMap<String, Authentication>();
    authentications = Collections.unmodifiableMap(authentications);
  }

  public JSON getJSON() {
    return json;
  }

  public String getBasePath() {
    return basePath;
  }

  public ApiClient setBasePath(String basePath) {
    this.basePath = basePath;
    return this;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Map<String, List<String>> getResponseHeaders() {
    return responseHeaders;
  }

  public Map<String, Authentication> getAuthentications() {
    return authentications;
  }

  public Authentication getAuthentication(String authName) {
    return authentications.get(authName);
  }

  public void setUsername(String username) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof HttpBasicAuth) {
        ((HttpBasicAuth) auth).setUsername(username);
        return;
      }
    }
    throw new RuntimeException("No HTTP basic authentication configured!");
  }

  public void setPassword(String password) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof HttpBasicAuth) {
        ((HttpBasicAuth) auth).setPassword(password);
        return;
      }
    }
    throw new RuntimeException("No HTTP basic authentication configured!");
  }

  public void setApiKey(String apiKey) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof ApiKeyAuth) {
        ((ApiKeyAuth) auth).setApiKey(apiKey);
        return;
      }
    }
    throw new RuntimeException("No API key authentication configured!");
  }

  public void setApiKeyPrefix(String apiKeyPrefix) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof ApiKeyAuth) {
        ((ApiKeyAuth) auth).setApiKeyPrefix(apiKeyPrefix);
        return;
      }
    }
    throw new RuntimeException("No API key authentication configured!");
  }

  public ApiClient setUserAgent(String userAgent) {
    addDefaultHeader("User-Agent", userAgent);
    return this;
  }

  public ApiClient addDefaultHeader(String key, String value) {
    defaultHeaderMap.put(key, value);
    return this;
  }

  public boolean isDebugging() {
    return debugging;
  }

  public ApiClient setDebugging(boolean debugging) {
    this.debugging = debugging;
    return this;
  }

  public String getTempFolderPath() {
    return tempFolderPath;
  }

  public ApiClient setTempFolderPath(String tempFolderPath) {
    this.tempFolderPath = tempFolderPath;
    return this;
  }

  public DateFormat getDateFormat() {
    return dateFormat;
  }

  public ApiClient setDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
    this.json.setDateFormat((DateFormat) dateFormat.clone());
    return this;
  }

  public Date parseDate(String str) {
    try {
      return dateFormat.parse(str);
    } catch (java.text.ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public String formatDate(Date date) {
    return dateFormat.format(date);
  }

  public String parameterToString(Object param) {
    if (param == null) {
      return "";
    } else if (param instanceof Date) {
      return formatDate((Date) param);
    } else if (param instanceof Collection) {
      StringBuilder builder = new StringBuilder();
      for (Object object : (Collection<?>) param) {
        if (builder.length() > 0) {
          builder.append(",");
        }
        builder.append(String.valueOf(object));
      }
      return builder.toString();
    } else {
      return String.valueOf(param);
    }
  }

  public List<Pair> parameterToPairs(String collectionFormat, String name, Object value) {
    List<Pair> params = new ArrayList<Pair>();
    if (name == null || name.length() == 0 || value == null) {
      return params;
    }

    Collection<?> valueCollection = null;
    if (value instanceof Collection) {
      valueCollection = (Collection<?>) value;
    } else {
      params.add(new Pair(name, parameterToString(value)));
      return params;
    }

    if (valueCollection.isEmpty()) {
      return params;
    }

    collectionFormat = collectionFormat == null || collectionFormat.length() == 0 ? "csv" : collectionFormat;

    if ("multi".equals(collectionFormat)) {
      for (Object item : valueCollection) {
        params.add(new Pair(name, parameterToString(item)));
      }
      return params;
    }

    String delimiter = ",";
    if ("ssv".equals(collectionFormat)) {
      delimiter = " ";
    } else if ("tsv".equals(collectionFormat)) {
      delimiter = "\t";
    } else if ("pipes".equals(collectionFormat)) {
      delimiter = "|";
    }

    StringBuilder builder = new StringBuilder();
    for (Object item : valueCollection) {
      if (builder.length() > 0) {
        builder.append(delimiter);
      }
      builder.append(parameterToString(item));
    }
    params.add(new Pair(name, builder.toString()));
    return params;
  }

  public boolean isJsonMime(String mime) {
    String jsonMime = "(?i)^(application/json|[^;/ \\t]+/[^;/ \\t]+[+]json)[ \\t]*(;.*)?$";
    return mime != null && (mime.matches(jsonMime) || "*/*".equals(mime));
  }

  public String selectHeaderAccept(String[] accepts) {
    if (accepts.length == 0) {
      return null;
    }
    for (String accept : accepts) {
      if (isJsonMime(accept)) {
        return accept;
      }
    }
    return StringUtil.join(accepts, ",");
  }

  public String selectHeaderContentType(String[] contentTypes) {
    if (contentTypes.length == 0) {
      return MEDIA_TYPE_JSON;
    }
    for (String contentType : contentTypes) {
      if (isJsonMime(contentType)) {
        return contentType;
      }
    }
    return contentTypes[0];
  }

  public String escapeString(String str) {
    try {
      return URLEncoder.encode(str, "utf8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      return str;
    }
  }

  public <T> T invokeAPI(String path, String method, List<Pair> queryParams, Object body, Map<String, String> headerParams,
      Map<String, String> cookieParams, Map<String, Object> formParams, String accept, String contentType,
      String[] authNames, GenericType<T> returnType) throws ApiException {

    updateParamsForAuth(authNames, queryParams, headerParams, cookieParams);

    HttpURLConnection connection = null;
    try {
      URL url = new URL(buildRequestUrl(path, queryParams));
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(false);
      if (connectTimeoutMillis > 0) {
        connection.setConnectTimeout(connectTimeoutMillis);
      }
      if (readTimeoutMillis > 0) {
        connection.setReadTimeout(readTimeoutMillis);
      }
      setRequestMethod(connection, method);

      applyHeaders(connection, headerParams, accept);
      applyCookies(connection, cookieParams);

      RequestBody requestBody = serialize(body, formParams, contentType);
      if (requestBody != null && requiresRequestBody(method)) {
        connection.setDoOutput(true);
        if (requestBody.contentType != null) {
          connection.setRequestProperty(HEADER_CONTENT_TYPE, requestBody.contentType);
        }
        connection.setRequestProperty("Content-Length", String.valueOf(requestBody.content.length));
        OutputStream outputStream = null;
        try {
          outputStream = connection.getOutputStream();
          outputStream.write(requestBody.content);
          outputStream.flush();
        } finally {
          closeQuietly(outputStream);
        }
      }

      statusCode = connection.getResponseCode();
      responseHeaders = buildResponseHeaders(connection.getHeaderFields());
      byte[] responseBody = readResponseBody(connection);

      if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
        return null;
      }
      if (statusCode >= 200 && statusCode < 300) {
        if (returnType == null) {
          return null;
        }
        return deserialize(responseBody, responseHeaders, returnType);
      }

      String message = decodeBody(responseBody, responseHeaders);
      if (message == null || message.length() == 0) {
        message = "error";
      }
      throw new ApiException(statusCode, message, responseHeaders, message);
    } catch (IOException e) {
      throw new ApiException(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public void setConnectTimeoutMillis(int connectTimeoutMillis) {
    this.connectTimeoutMillis = connectTimeoutMillis;
  }

  public void setReadTimeoutMillis(int readTimeoutMillis) {
    this.readTimeoutMillis = readTimeoutMillis;
  }

  protected <T> T deserialize(byte[] body, Map<String, List<String>> headers, GenericType<T> returnType) throws ApiException {
    if (returnType == null) {
      return null;
    }

    Class<?> rawType = returnType.getRawType();
    if (byte[].class.equals(rawType)) {
      @SuppressWarnings("unchecked")
      T result = (T) body;
      return result;
    }
    if (File.class.equals(rawType)) {
      @SuppressWarnings("unchecked")
      T result = (T) downloadFileFromResponse(body, headers);
      return result;
    }
    if (String.class.equals(rawType)) {
      @SuppressWarnings("unchecked")
      T result = (T) decodeBody(body, headers);
      return result;
    }
    if (body == null || body.length == 0) {
      return null;
    }

    try {
      JavaType javaType = json.getMapper().getTypeFactory().constructType(returnType.getType());
      return json.getMapper().readValue(body, javaType);
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  public File downloadFileFromResponse(byte[] body, Map<String, List<String>> headers) throws ApiException {
    OutputStream outputStream = null;
    try {
      File file = prepareDownloadFile(headers);
      outputStream = Files.newOutputStream(file.toPath());
      outputStream.write(body);
      outputStream.flush();
      return file;
    } catch (IOException e) {
      throw new ApiException(e);
    } finally {
      closeQuietly(outputStream);
    }
  }

  public File prepareDownloadFile(Map<String, List<String>> headers) throws IOException {
    String filename = null;
    String contentDisposition = getFirstHeader(headers, "Content-Disposition");
    if (contentDisposition != null && contentDisposition.length() > 0) {
      Pattern pattern = Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
      Matcher matcher = pattern.matcher(contentDisposition);
      if (matcher.find()) {
        filename = matcher.group(1);
      }
    }

    String prefix = null;
    String suffix = null;
    if (filename == null) {
      prefix = "download-";
      suffix = "";
    } else {
      int pos = filename.lastIndexOf(".");
      if (pos == -1) {
        prefix = filename + "-";
      } else {
        prefix = filename.substring(0, pos) + "-";
        suffix = filename.substring(pos);
      }
      if (prefix.length() < 3) {
        prefix = "download-";
      }
    }

    if (tempFolderPath == null) {
      return Files.createTempFile(prefix, suffix).toFile();
    }
    return Files.createTempFile(Paths.get(tempFolderPath), prefix, suffix).toFile();
  }

  protected Map<String, List<String>> buildResponseHeaders(Map<String, List<String>> rawHeaders) {
    Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
    if (rawHeaders == null) {
      return headers;
    }
    for (Entry<String, List<String>> entry : rawHeaders.entrySet()) {
      if (entry.getKey() != null) {
        headers.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
      }
    }
    return headers;
  }

  protected void updateParamsForAuth(String[] authNames, List<Pair> queryParams, Map<String, String> headerParams, Map<String, String> cookieParams) {
    for (String authName : authNames) {
      Authentication auth = authentications.get(authName);
      if (auth == null) {
        throw new RuntimeException("Authentication undefined: " + authName);
      }
      auth.applyToParams(queryParams, headerParams, cookieParams);
    }
  }

  protected RequestBody serialize(Object obj, Map<String, Object> formParams, String contentType) throws ApiException {
    try {
      if (contentType != null && contentType.startsWith(MEDIA_TYPE_MULTIPART)) {
        return buildMultipartRequest(formParams);
      }
      if (contentType != null && contentType.startsWith(MEDIA_TYPE_FORM)) {
        return buildFormRequest(formParams);
      }
      if (obj == null) {
        return null;
      }
      if (obj instanceof byte[]) {
        return new RequestBody((byte[]) obj, contentType);
      }
      if (obj instanceof String && !isJsonMime(contentType)) {
        return new RequestBody(((String) obj).getBytes(UTF_8), contentType);
      }
      if (obj instanceof File) {
        return new RequestBody(readFile((File) obj), contentType);
      }
      String resolvedContentType = contentType == null || contentType.length() == 0 ? MEDIA_TYPE_JSON : contentType;
      return new RequestBody(json.getMapper().writeValueAsBytes(obj), resolvedContentType);
    } catch (IOException e) {
      throw new ApiException("Could not serialize request body: " + e.getMessage());
    }
  }

  private RequestBody buildFormRequest(Map<String, Object> formParams) throws UnsupportedEncodingException {
    StringBuilder body = new StringBuilder();
    for (Entry<String, Object> entry : formParams.entrySet()) {
      if (body.length() > 0) {
        body.append('&');
      }
      body.append(escapeString(entry.getKey()));
      body.append('=');
      body.append(escapeString(parameterToString(entry.getValue())));
    }
    return new RequestBody(body.toString().getBytes("UTF-8"), MEDIA_TYPE_FORM);
  }

  private RequestBody buildMultipartRequest(Map<String, Object> formParams) throws IOException {
    String boundary = "----comanda-api-legacy-" + System.currentTimeMillis();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    for (Entry<String, Object> entry : formParams.entrySet()) {
      writeAscii(outputStream, "--" + boundary + "\r\n");
      if (entry.getValue() instanceof File) {
        File file = (File) entry.getValue();
        writeAscii(outputStream, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + file.getName() + "\"\r\n");
        writeAscii(outputStream, "Content-Type: application/octet-stream\r\n\r\n");
        outputStream.write(readFile(file));
      } else {
        writeAscii(outputStream, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n");
        writeAscii(outputStream, "Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        writeUtf8(outputStream, parameterToString(entry.getValue()));
      }
      writeAscii(outputStream, "\r\n");
    }
    writeAscii(outputStream, "--" + boundary + "--\r\n");

    return new RequestBody(outputStream.toByteArray(), MEDIA_TYPE_MULTIPART + "; boundary=" + boundary);
  }

  private String buildRequestUrl(String path, List<Pair> queryParams) {
    StringBuilder url = new StringBuilder();
    url.append(basePath);
    url.append(path);

    if (queryParams == null || queryParams.isEmpty()) {
      return url.toString();
    }

    boolean hasQuery = path.indexOf('?') >= 0;
    for (Pair queryParam : queryParams) {
      if (queryParam.getValue() == null) {
        continue;
      }
      url.append(hasQuery ? '&' : '?');
      hasQuery = true;
      url.append(escapeString(queryParam.getName()));
      url.append('=');
      url.append(escapeString(queryParam.getValue()));
    }
    return url.toString();
  }

  private void applyHeaders(HttpURLConnection connection, Map<String, String> headerParams, String accept) {
    if (accept != null) {
      connection.setRequestProperty(HEADER_ACCEPT, accept);
    }
    for (Entry<String, String> entry : defaultHeaderMap.entrySet()) {
      if (!headerParams.containsKey(entry.getKey()) && entry.getValue() != null) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
    }
    for (Entry<String, String> entry : headerParams.entrySet()) {
      if (entry.getValue() != null) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
    }
  }

  private void applyCookies(HttpURLConnection connection, Map<String, String> cookieParams) {
    Map<String, String> cookies = new LinkedHashMap<String, String>();
    cookies.putAll(defaultCookieMap);
    cookies.putAll(cookieParams);
    if (!cookies.isEmpty()) {
      StringBuilder cookieHeader = new StringBuilder();
      for (Entry<String, String> entry : cookies.entrySet()) {
        if (entry.getValue() == null) {
          continue;
        }
        if (cookieHeader.length() > 0) {
          cookieHeader.append("; ");
        }
        cookieHeader.append(entry.getKey()).append('=').append(entry.getValue());
      }
      if (cookieHeader.length() > 0) {
        connection.setRequestProperty(HEADER_COOKIE, cookieHeader.toString());
      }
    }
  }

  private boolean requiresRequestBody(String method) {
    return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
  }

  private byte[] readResponseBody(HttpURLConnection connection) throws IOException {
    InputStream stream = null;
    try {
      stream = connection.getErrorStream();
      if (stream == null) {
        stream = connection.getInputStream();
      }
      if (stream == null) {
        return new byte[0];
      }
      return readFully(stream);
    } finally {
      closeQuietly(stream);
    }
  }

  private String decodeBody(byte[] body, Map<String, List<String>> headers) {
    if (body == null || body.length == 0) {
      return null;
    }
    String charsetName = extractCharset(getFirstHeader(headers, HEADER_CONTENT_TYPE));
    Charset charset = charsetName == null ? UTF_8 : Charset.forName(charsetName);
    return new String(body, charset);
  }

  private String getFirstHeader(Map<String, List<String>> headers, String headerName) {
    if (headers == null) {
      return null;
    }
    for (Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(headerName) && entry.getValue() != null && !entry.getValue().isEmpty()) {
        return entry.getValue().get(0);
      }
    }
    return null;
  }

  private String extractCharset(String contentType) {
    if (contentType == null) {
      return null;
    }
    String[] parts = contentType.split(";");
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i].trim();
      if (part.startsWith("charset=")) {
        return part.substring("charset=".length()).replace("\"", "");
      }
    }
    return null;
  }

  private void setRequestMethod(HttpURLConnection connection, String method) throws ProtocolException {
    try {
      connection.setRequestMethod(method);
    } catch (ProtocolException e) {
      if (!"PATCH".equals(method)) {
        throw e;
      }
      setRequestMethodViaReflection(connection, method);
    }
  }

  private void setRequestMethodViaReflection(HttpURLConnection connection, String method) throws ProtocolException {
    try {
      Field methodField = null;
      Class<?> currentClass = connection.getClass();
      while (currentClass != null && methodField == null) {
        try {
          methodField = currentClass.getDeclaredField("method");
        } catch (NoSuchFieldException e) {
          currentClass = currentClass.getSuperclass();
        }
      }
      if (methodField == null) {
        throw new NoSuchFieldException("method");
      }
      methodField.setAccessible(true);
      methodField.set(connection, method);
    } catch (Exception ex) {
      ProtocolException protocolException = new ProtocolException("Invalid HTTP method: " + method);
      protocolException.initCause(ex);
      throw protocolException;
    }
  }

  private byte[] readFile(File file) throws IOException {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      return readFully(inputStream);
    } finally {
      closeQuietly(inputStream);
    }
  }

  private byte[] readFully(InputStream inputStream) throws IOException {
    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int read;
    while ((read = bufferedInputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, read);
    }
    return outputStream.toByteArray();
  }

  private void writeAscii(OutputStream outputStream, String value) throws IOException {
    outputStream.write(value.getBytes("US-ASCII"));
  }

  private void writeUtf8(OutputStream outputStream, String value) throws IOException {
    outputStream.write(value.getBytes("UTF-8"));
  }

  private void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Ignore close failures.
      }
    }
  }

  private static final class RequestBody {
    private final byte[] content;
    private final String contentType;

    private RequestBody(byte[] content, String contentType) {
      this.content = content;
      this.contentType = contentType;
    }
  }
}
