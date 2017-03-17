package com.winkelmeyer.salesforce.einsteinvision.ext.http;

import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import com.winkelmeyer.salesforce.einsteinvision.ext.representations.AccessToken;

/**
 * 
 * Code taken from https://github.com/MetaMind/quickstart/blob/master/quickstart-java/
 *
 */

public class AccessTokenRequest extends Request {

  private final String assertion;

  public AccessTokenRequest(String assertion) {
    super("");
    this.assertion = assertion;
  }

  public AccessToken submit() throws IOException {
    Form form = new Form();
    form.param("assertion", assertion);
    form.param("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

    Entity<Form> entity = Entity.form(form);

    Response response = client.target(EINSTEIN_VISION_URL + "/v1/oauth2/token")
        .request()
        .post(entity);

    if (!isSuccessful(response)) {
      throw new IOException("Error occurred while fetching Access Token " + response);
    }
    return readResponseAs(response, AccessToken.class);
  }
}