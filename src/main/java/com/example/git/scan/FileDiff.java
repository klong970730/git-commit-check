package com.example.git.scan;

/**
 * @author klong
 */
public class FileDiff {
  private String insert;
  private String delete;
  private String fileDetails;

  public FileDiff(String insert, String delete, String fileDetails) {
    this.insert = insert;
    this.delete = delete;
    this.fileDetails = fileDetails;
  }

  public String getInsert() {
    return insert;
  }


  public String getDelete() {
    return delete;
  }

  public String getFileDetails() {
    return fileDetails;
  }

  @Override
  public String toString() {
    return String.join("\t", insert, delete, fileDetails);
  }
}
