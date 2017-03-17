package com.winkelmeyer.salesforce.einsteinvision.ext;

import java.io.IOException;

import com.winkelmeyer.salesforce.einsteinvision.ext.http.AccessTokenRequest;
import com.winkelmeyer.salesforce.einsteinvision.ext.representations.AccessToken;

/**
 * Generates an OAuth token
 * 
 * Code taken from https://github.com/MetaMind/quickstart/blob/master/quickstart-java/
 * 
 */
public class AccessTokenProvider {

  private AccessToken accessToken;
  private final String email;
  private final String privateKey;
  private final long durationInSeconds;

  public static AccessTokenProvider getProvider(String email, String privateKey,
      long durationInSeconds) {
    return new AccessTokenProvider(email, privateKey, durationInSeconds);
  }

  private AccessTokenProvider(String email, String privateKey, long durationInSeconds) {
    this.email = email;
    this.privateKey = privateKey;
    this.durationInSeconds = durationInSeconds;
    refreshToken();
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }

  public void refreshToken() {
    String assertion = AssertionGenerator
        .generateJWTAssertion(email, privateKey, durationInSeconds);
    AccessTokenRequest tokenRequest = new AccessTokenRequest(assertion);
    try {
      accessToken = tokenRequest.submit();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
