/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/commons-ip
 */
package org.roda_project.commons_ip2.model;

import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IPFile implements Serializable {
  private static final long serialVersionUID = -8653651803476080935L;

  private transient Path path;
  private String pathString;
  private String renameTo;
  private List<String> relativeFolders;
  private String checksum = "";
  private String checksumAlgorithm = "";
  private List<String> relatedTags;
  private FileType fileType;

  public IPFile(Path path) {
    super();
    this.path = path;
    this.pathString = this.path.toAbsolutePath().toString();
    this.renameTo = null;
    this.relativeFolders = new ArrayList<>();
    this.relatedTags = new ArrayList<>();
  }

  public IPFile(Path path, List<String> relativeFolders) {
    super();
    this.path = path;
    this.pathString = this.path.toAbsolutePath().toString();
    this.renameTo = null;
    this.relativeFolders = relativeFolders;
    this.relatedTags = new ArrayList<>();
  }

  public IPFile(Path path, String renameTo) {
    super();
    this.path = path;
    this.pathString = this.path.toAbsolutePath().toString();
    this.renameTo = renameTo;
    this.relativeFolders = new ArrayList<>();
    this.relatedTags = new ArrayList<>();
  }

  public Path getPath() {
    return path;
  }

  public IPFile setPath(Path path) {
    this.path = path;
    this.pathString = this.path.toAbsolutePath().toString();
    return this;
  }

  public List<String> getRelativeFolders() {
    return relativeFolders;
  }

  public IPFile setRelativeFolders(List<String> relativeFolders) {
    this.relativeFolders = relativeFolders;
    return this;
  }

  public String getRenameTo() {
    return renameTo;
  }

  public IPFile setRenameTo(String renameTo) {
    this.renameTo = renameTo;
    return this;
  }

  public String getFileName() {
    final String filename;
    if (renameTo != null) {
      filename = renameTo;
    } else {
      filename = path.getFileName().toString();
    }
    return filename;
  }

  public String getChecksum() {
    return checksum;
  }

  public IPFile setChecksum(String checksum) {
    this.checksum = checksum;
    return this;
  }

  public String getChecksumAlgorithm() {
    return checksumAlgorithm;
  }

  public IPFile setChecksumAlgorithm(String checksumAlgorithm) {
    this.checksumAlgorithm = checksumAlgorithm;
    return this;
  }

  public IPFile setChecksumAndAlgorithm(String checksum, String checksumAlgorithm) {
    this.checksum = checksum == null ? "" : checksum;
    this.checksumAlgorithm = checksumAlgorithm == null ? "" : checksumAlgorithm;
    return this;
  }

  public List<String> getRelatedTags() {
    return relatedTags;
  }

  public IPFile setRelatedTags(List<String> relatedTags) {
    this.relatedTags = relatedTags;
    return this;
  }

  public FileType getFileType() {
    return fileType;
  }

  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  @Override
  public String toString() {
    return "IPFile [path=" + path + ", renameTo=" + renameTo + ", relativeFolders=" + relativeFolders + ", checksum="
      + checksum + ", checksumAlgorithm=" + checksumAlgorithm + ", relatedTags=" + relatedTags + "]";
  }

  private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
    inputStream.defaultReadObject();
    this.path = Paths.get(this.pathString);
  }

}
