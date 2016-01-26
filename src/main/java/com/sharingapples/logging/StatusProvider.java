package com.sharingapples.logging;

/**
 * The Status provider that facilitates the {@link StatusBuilder}
 * to create status snapshot.
 *
 * Created by ranjan on 1/26/16.
 */
public interface StatusProvider {

  /**
   * Update the {@link StringBuilder} with the current
   * status snapshot
   * @param status The string builder that needs to be updated
   *               with the current status snapshot.
   */
  void updateStatus(StringBuilder status);
}
