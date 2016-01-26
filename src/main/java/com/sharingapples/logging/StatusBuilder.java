package com.sharingapples.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A simple library to update the given file with the status sent
 * via {@link StatusProvider}
 *
 * Created by ranjan on 1/26/16.
 */
public class StatusBuilder {

  private final File outputFile;          /* The file to which the status is dumped */
  private final StatusProvider provider;  /* The status provider */


  /**
   * Create a status builder that dumps status to a temporary file.
   * @param provider The status provider
   * @throws IOException
   */
  public StatusBuilder(StatusProvider provider) throws IOException {
    this(File.createTempFile("tmp-", ".status"), provider);
  }

  /**
   * Create a status builder that dumps status output on the file
   * with the given name.
   *
   * @param filename The file where the status needs to be dumped
   * @param provider The status provider
   */
  public StatusBuilder(String filename, StatusProvider provider) {
    this(new File(filename), provider);
  }


  /**
   * Create a status builder that dumps status output on the given
   * file.
   *
   * @param file The file where the status needs to be dumped
   * @param provider The status provider
   */
  public StatusBuilder(File file, StatusProvider provider) {
    assert(file != null);
    assert(provider != null);

    this.outputFile = file;
    this.provider = provider;

    // Show a helpful command to watch the status file
    System.out.println("StatusBuilder MSG - Use the following command in linux to check your status file");
    System.out.println("\twatch -n 1 cat \"" + file.getAbsolutePath() + "\"");
  }

  /**
   * The only API method that needs to be called on the Status Builder.
   *
   * Whenever this method is invoked, the builder uses the {@link StatusProvider}
   * to build the dump output.
   */
  public void dump() {
    try(FileWriter writer = new FileWriter(outputFile)) {
      StringBuilder builder = new StringBuilder();
      provider.updateStatus(builder);
      writer.write(builder.toString());
    } catch(IOException e) {
      System.err.println("StatusBuilder Error while dumping content");
      e.printStackTrace();
    }
  }
}
