/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/commons-ip
 */
package org.roda_project.commons_ip2.model.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.roda_project.commons_ip.utils.IPEnums;
import org.roda_project.commons_ip.utils.IPEnums.IPType;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.model.AIP;
import org.roda_project.commons_ip2.model.IP;
import org.roda_project.commons_ip2.model.IPContentType;
import org.roda_project.commons_ip2.model.IPFileInterface;
import org.roda_project.commons_ip2.model.impl.eark.out.writers.strategy.WriteStrategy;

/**
 * AIP basic implementation. This implementation just holds values in memory. It
 * can't read or write AIPs.
 * 
 * Build methods: {@link #build(Path)}, {@link #build(Path, boolean)},
 * {@link #build(Path, String)} and {@link #build(Path, String, boolean)} throw
 * a {@link IPException}.
 *
 * Parse methods: {@link #parse(Path)}, {@link #parse(Path, Path)} throw a
 * {@link org.roda_project.commons_ip.model.ParseException}.
 *
 * @author Rui Castro (rui.castro@gmail.com)
 */
public class BasicAIP extends IP implements AIP {

  /**
   * The state.
   */
  private String state;

  /**
   * List of submission files.
   */
  private List<IPFileInterface> submissions;

  /**
   * Constructor.
   */
  public BasicAIP() {
    super();
    this.setType(IPType.AIP);
    this.submissions = new ArrayList<>();
  }

  /**
   * Constructor.
   *
   * @param id
   *          the ID.
   */
  public BasicAIP(final String id) {
    this();
    setId(id);
  }

  /**
   * Constructor.
   *
   * @param id
   *          the ID.
   * @param contentType
   *          the {@link IPContentType}.
   */
  public BasicAIP(final String id, final IPContentType contentType) {
    this(id);
    setContentType(contentType);
  }

  @Override
  public String toString() {
    return "BasicAIP [super=" + super.toString() + ", submissions=" + submissions + "]";
  }

  @Override
  public void setState(final String state) {
    this.state = state;
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public List<IPFileInterface> getSubmissions() {
    return submissions;
  }

  @Override
  public AIP addSubmission(final IPFileInterface submission) {
    this.submissions.add(submission);
    return this;
  }

  @Override
  public Path build(WriteStrategy writeStrategy) throws IPException, InterruptedException {
    throw new IPException("Not implemented");
  }

  @Override
  public Path build(WriteStrategy writeStrategy, final boolean onlyManifest)
    throws IPException, InterruptedException {
    throw new IPException("Not implemented");
  }

  @Override
  public Path build(WriteStrategy writeStrategy, final String fileNameWithoutExtension)
    throws IPException, InterruptedException {
    throw new IPException("Not implemented");
  }

  @Override
  public Path build(WriteStrategy writeStrategy, final String fileNameWithoutExtension, final boolean onlyManifest)
    throws IPException, InterruptedException {
    throw new IPException("Not implemented");
  }

  @Override
  public Path build(WriteStrategy writeStrategy, String fileNameWithoutExtension, boolean onlyManifest,
    IPEnums.SipType sipType) throws IPException, InterruptedException {
    throw new IPException("Not implemented");
  }

  @Override
  public Path build(WriteStrategy writeStrategy, final String fileNameWithoutExtension, IPEnums.SipType sipType)
    throws IPException, InterruptedException {
    throw new IPException("Not implemented");
  }

  @Override
  public Set<String> getExtraChecksumAlgorithms() {
    return Collections.emptySet();
  }
}
