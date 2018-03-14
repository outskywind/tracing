
package com.dafy.skye.storage.zipkincopy;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import zipkin2.elasticsearch.internal.client.HttpCall;

import java.io.IOException;

/** Ensures the index template exists and saves off the version */
public class EnsureIndexTemplate {

  static MediaType APPLICATION_JSON= MediaType.parse("application/json");

  /**
   * This is a blocking call, used inside a lazy. That's because no writes should occur until the
   * template is available.
   */
  public static void apply(HttpCall.Factory callFactory, String name, String indexTemplate)
    throws IOException {
    HttpUrl templateUrl = callFactory.baseUrl.newBuilder("_template").addPathSegment(name).build();
    Request getTemplate = new Request.Builder().url(templateUrl).tag("get-template").build();
    try {
      callFactory.newCall(getTemplate, BodyConverters.NULL).execute();
    } catch (IllegalStateException e) { // TODO: handle 404 slightly more nicely
      Request updateTemplate = new Request.Builder()
        .url(templateUrl)
        .put(RequestBody.create(APPLICATION_JSON, indexTemplate))
        .tag("update-template").build();
      callFactory.newCall(updateTemplate, BodyConverters.NULL).execute();
    }
  }
}
