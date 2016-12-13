/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/commons-ip
 */
package org.roda_project.commons_ip.model.impl.eark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.roda_project.commons_ip.mets_v1_11.beans.DivType;
import org.roda_project.commons_ip.mets_v1_11.beans.DivType.Fptr;
import org.roda_project.commons_ip.mets_v1_11.beans.DivType.Mptr;
import org.roda_project.commons_ip.mets_v1_11.beans.FileType;
import org.roda_project.commons_ip.mets_v1_11.beans.FileType.FLocat;
import org.roda_project.commons_ip.mets_v1_11.beans.MdSecType.MdRef;
import org.roda_project.commons_ip.mets_v1_11.beans.Mets;
import org.roda_project.commons_ip.mets_v1_11.beans.MetsType;
import org.roda_project.commons_ip.mets_v1_11.beans.StructMapType;
import org.roda_project.commons_ip.model.AIP;
import org.roda_project.commons_ip.model.IPConstants;
import org.roda_project.commons_ip.model.IPContentType;
import org.roda_project.commons_ip.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip.model.IPFile;
import org.roda_project.commons_ip.model.IPMetadata;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.MetadataType;
import org.roda_project.commons_ip.model.MetsWrapper;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.model.RepresentationContentType;
import org.roda_project.commons_ip.model.RepresentationStatus;
import org.roda_project.commons_ip.model.ValidationEntry;
import org.roda_project.commons_ip.model.impl.AIPWrap;
import org.roda_project.commons_ip.model.impl.BasicAIP;
import org.roda_project.commons_ip.utils.IPEnums;
import org.roda_project.commons_ip.utils.IPEnums.IPStatus;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip.utils.METSFileTypeZipEntryInfo;
import org.roda_project.commons_ip.utils.METSMdRefZipEntryInfo;
import org.roda_project.commons_ip.utils.METSUtils;
import org.roda_project.commons_ip.utils.METSZipEntryInfo;
import org.roda_project.commons_ip.utils.Utils;
import org.roda_project.commons_ip.utils.ValidationConstants;
import org.roda_project.commons_ip.utils.ValidationUtils;
import org.roda_project.commons_ip.utils.ZIPUtils;
import org.roda_project.commons_ip.utils.ZipEntryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * EARK AIP. This implementation of {@link AIP} can read/write AIPs from/to a
 * folder.
 * 
 * @author Rui Castro (rui.castro@gmail.com)
 */
public class EARKAIP extends AIPWrap {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKAIP.class);

  private static final String TEMP_DIR = "EARKAIP";
  private static final String FILE_EXTENSION = ".zip";

  private static boolean VALIDATION_FAIL_IF_REPRESENTATION_METS_DOES_NOT_HAVE_TWO_PARTS = false;

  /**
   * Constructor.
   *
   * @param aip
   *          the {@link AIP} to warp.
   */
  public EARKAIP(final AIP aip) {
    super(aip);
  }

  @Override
  public Path build(final Path destinationDirectory) throws IPException, InterruptedException {
    return build(destinationDirectory, null);
  }

  @Override
  public Path build(final Path destinationDirectory, final boolean onlyManifest)
    throws IPException, InterruptedException {
    return build(destinationDirectory, null, onlyManifest);
  }

  @Override
  public Path build(final Path destinationDirectory, final String fileNameWithoutExtension)
    throws IPException, InterruptedException {
    return build(destinationDirectory, fileNameWithoutExtension, false);
  }

  @Override
  public Path build(final Path destinationDirectory, final String fileNameWithoutExtension, final boolean onlyManifest)
    throws IPException, InterruptedException {
    final Path buildDir = createBuildDir();
    Path zipPath = null;
    try {
      final List<ZipEntryInfo> zipEntries = getZipEntries();
      zipPath = getDirPath(destinationDirectory, fileNameWithoutExtension, false);

      // 20160407 hsilva: as METS does not have an attribute 'otherType', the
      // other type must be put in the 'type' attribute allowing this way other
      // values besides the ones in the Enum
      final String contentType = this.getContentType().asString();

      final MetsWrapper mainMETSWrapper = EARKMETSUtils.generateMETS(StringUtils.join(this.getIds(), " "),
        this.getDescription(), this.getType() + ":" + contentType, this.getProfile(), this.getAgents(), true,
        Optional.ofNullable(this.getAncestors()), null, this.getStatus());

      addDescriptiveMetadataToZipAndMETS(zipEntries, mainMETSWrapper, getDescriptiveMetadata(), null);

      addPreservationMetadataToZipAndMETS(zipEntries, mainMETSWrapper, getPreservationMetadata(), null);

      addOtherMetadataToZipAndMETS(zipEntries, mainMETSWrapper, getOtherMetadata(), null);

      addRepresentationsToZipAndMETS(zipEntries, mainMETSWrapper, buildDir);

      addDefaultSchemas(buildDir);

      addSchemasToZipAndMETS(zipEntries, mainMETSWrapper, getSchemas(), null);

      addDocumentationToZipAndMETS(zipEntries, mainMETSWrapper, getDocumentation(), null);

      addSubmissionsToZipAndMETS(zipEntries, mainMETSWrapper, getSubmissions());

      addMainMETSToZip(zipEntries, mainMETSWrapper, buildDir);

      writeToPath(zipEntries, zipPath, onlyManifest);

      return zipPath;
    } catch (final InterruptedException e) {
      cleanUpUponInterrupt(zipPath);
      throw e;
    } finally {
      deleteBuildDir(buildDir);
    }
  }

  public static AIP parse(final Path source) throws ParseException {
    try {
      if (Files.isDirectory(source)) {
        return parseEARKAIPFromPath(source);
      } else {
        return parse(source, Files.createTempDirectory("unzipped"));
      }
    } catch (final IOException e) {
      throw new ParseException("Error creating temporary directory for E-ARK AIP parse", e);
    }
  }

  public static AIP parse(Path source, Path destinationDirectory) throws ParseException {
    return parseEARKAIP(source, destinationDirectory);
  }

  private Path createBuildDir() throws IPException {
    try {
      return Files.createTempDirectory(TEMP_DIR);
    } catch (IOException e) {
      throw new IPException("Unable to create temporary directory to hold AIP files", e);
    }
  }

  private void cleanUpUponInterrupt(Path zipPath) {
    if (zipPath != null && Files.exists(zipPath)) {
      try {
        Utils.deletePath(zipPath);
      } catch (IOException e) {
        LOGGER.error("Error while cleaning up unneeded files", e);
      }
    }
  }

  private void deleteBuildDir(Path buildDir) throws IPException {
    try {
      Utils.deletePath(buildDir);
    } catch (IOException e) {
      throw new IPException("Error while deleting temporary directory that was created to hold AIP files", e);
    }
  }

  private Path getDirPath(final Path targetPath, final String name, final boolean deleteExisting) throws IPException {
    final Path path;
    if (name != null) {
      path = targetPath.resolve(name);
    } else {
      path = targetPath.resolve(getId());
    }

    try {
      if (deleteExisting && Files.exists(path)) {
        Files.delete(path);
      }
    } catch (final IOException e) {
      throw new IPException("Error deleting existing path - " + e.getMessage(), e);
    }
    return path;
  }

  public void addDescriptiveMetadataToZipAndMETS(List<ZipEntryInfo> zipEntries, MetsWrapper metsWrapper,
    List<IPDescriptiveMetadata> descriptiveMetadata, String representationId) throws IPException, InterruptedException {
    if (descriptiveMetadata != null && !descriptiveMetadata.isEmpty()) {
      for (IPDescriptiveMetadata dm : descriptiveMetadata) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        IPFile file = dm.getMetadata();

        String descriptiveFilePath = IPConstants.DESCRIPTIVE_FOLDER + getFoldersFromList(file.getRelativeFolders())
          + file.getFileName();
        MdRef mdRef = EARKMETSUtils.addDescriptiveMetadataToMETS(metsWrapper, dm, descriptiveFilePath);

        if (representationId != null) {
          descriptiveFilePath = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
            + descriptiveFilePath;
        }
        ZIPUtils.addMdRefFileToZip(zipEntries, file.getPath(), descriptiveFilePath, mdRef);
      }
    }
  }

  public void addPreservationMetadataToZipAndMETS(List<ZipEntryInfo> zipEntries, MetsWrapper metsWrapper,
    List<IPMetadata> preservationMetadata, String representationId) throws IPException, InterruptedException {
    if (preservationMetadata != null && !preservationMetadata.isEmpty()) {
      for (IPMetadata pm : preservationMetadata) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        IPFile file = pm.getMetadata();

        String preservationMetadataPath = IPConstants.PRESERVATION_FOLDER
          + getFoldersFromList(file.getRelativeFolders()) + file.getFileName();
        MdRef mdRef = EARKMETSUtils.addPreservationMetadataToMETS(metsWrapper, pm, preservationMetadataPath);

        if (representationId != null) {
          preservationMetadataPath = IPConstants.REPRESENTATIONS_FOLDER + representationId
            + IPConstants.ZIP_PATH_SEPARATOR + preservationMetadataPath;
        }
        ZIPUtils.addMdRefFileToZip(zipEntries, file.getPath(), preservationMetadataPath, mdRef);
      }
    }
  }

  public void addOtherMetadataToZipAndMETS(List<ZipEntryInfo> zipEntries, MetsWrapper metsWrapper,
    List<IPMetadata> otherMetadata, String representationId) throws IPException, InterruptedException {
    if (otherMetadata != null && !otherMetadata.isEmpty()) {
      for (IPMetadata om : otherMetadata) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        IPFile file = om.getMetadata();

        String otherMetadataPath = IPConstants.OTHER_FOLDER + getFoldersFromList(file.getRelativeFolders())
          + file.getFileName();
        MdRef mdRef = EARKMETSUtils.addOtherMetadataToMETS(metsWrapper, om, otherMetadataPath);

        if (representationId != null) {
          otherMetadataPath = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
            + otherMetadataPath;
        }
        ZIPUtils.addMdRefFileToZip(zipEntries, file.getPath(), otherMetadataPath, mdRef);
      }
    }
  }

  public void addRepresentationsToZipAndMETS(List<ZipEntryInfo> zipEntries, MetsWrapper mainMETSWrapper, Path buildDir)
    throws IPException, InterruptedException {
    // representations
    if (getRepresentations() != null && !getRepresentations().isEmpty()) {
      for (IPRepresentation representation : getRepresentations()) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        String representationId = representation.getObjectID();
        // 20160407 hsilva: not being used by Common Specification v0.13
        String representationProfile = "";
        String representationContentType = representation.getContentType().asString();

        MetsWrapper representationMETSWrapper = EARKMETSUtils.generateMETS(representationId,
          representation.getDescription(),
          IPConstants.METS_REPRESENTATION_TYPE_PART_1 + ":" + representationContentType, representationProfile,
          representation.getAgents(), false, Optional.empty(), null, IPEnums.IPStatus.NEW);
        representationMETSWrapper.getMainDiv().setTYPE(representation.getStatus().asString());

        // representation data
        addRepresentationDataFilesToZipAndMETS(zipEntries, representationMETSWrapper, representation, representationId);

        // representation descriptive metadata
        addDescriptiveMetadataToZipAndMETS(zipEntries, representationMETSWrapper,
          representation.getDescriptiveMetadata(), representationId);

        // representation preservation metadata
        addPreservationMetadataToZipAndMETS(zipEntries, representationMETSWrapper,
          representation.getPreservationMetadata(), representationId);

        // representation other metadata
        addOtherMetadataToZipAndMETS(zipEntries, representationMETSWrapper, representation.getOtherMetadata(),
          representationId);

        // representation schemas
        addSchemasToZipAndMETS(zipEntries, representationMETSWrapper, representation.getSchemas(), representationId);

        // representation documentation
        addDocumentationToZipAndMETS(zipEntries, representationMETSWrapper, representation.getDocumentation(),
          representationId);

        // add representation METS to Zip file and to main METS file
        EARKMETSUtils.addRepresentationMETSToZipAndToMainMETS(zipEntries, mainMETSWrapper, representationId,
          representationMETSWrapper, IPConstants.REPRESENTATIONS_FOLDER + representationId
            + IPConstants.ZIP_PATH_SEPARATOR + IPConstants.METS_FILE,
          buildDir);
      }
    }
  }

  private void addRepresentationDataFilesToZipAndMETS(List<ZipEntryInfo> zipEntries,
    MetsWrapper representationMETSWrapper, IPRepresentation representation, String representationId)
    throws IPException, InterruptedException {
    if (representation.getData() != null && !representation.getData().isEmpty()) {
      for (IPFile file : representation.getData()) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }

        String dataFilePath = IPConstants.DATA_FOLDER + getFoldersFromList(file.getRelativeFolders())
          + file.getFileName();
        FileType fileType = EARKMETSUtils.addDataFileToMETS(representationMETSWrapper, dataFilePath, file.getPath());

        dataFilePath = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
          + dataFilePath;
        ZIPUtils.addFileTypeFileToZip(zipEntries, file.getPath(), dataFilePath, fileType);
      }
    }
  }

  private void addDefaultSchemas(Path buildDir) throws InterruptedException {
    try {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Path metsSchema = Utils.copyResourceFromClasspathToDir(EARKAIP.class, buildDir, "mets.xsd",
        "/schemas/mets1_11.xsd");
      getSchemas().add(new IPFile(metsSchema, "mets.xsd"));
      Path xlinkSchema = Utils.copyResourceFromClasspathToDir(EARKAIP.class, buildDir, "xlink.xsd",
        "/schemas/xlink.xsd");
      getSchemas().add(new IPFile(xlinkSchema, "xlink.xsd"));
    } catch (IOException e) {
      LOGGER.error("Error while trying to add default schemas", e);
    }
  }

  public void addSchemasToZipAndMETS(List<ZipEntryInfo> zipEntries, MetsWrapper metsWrapper, List<IPFile> schemas,
    String representationId) throws IPException, InterruptedException {
    if (schemas != null && !schemas.isEmpty()) {
      for (IPFile schema : schemas) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }

        String schemaFilePath = IPConstants.SCHEMAS_FOLDER + getFoldersFromList(schema.getRelativeFolders())
          + schema.getFileName();
        FileType fileType = EARKMETSUtils.addSchemaFileToMETS(metsWrapper, schemaFilePath, schema.getPath());

        if (representationId != null) {
          schemaFilePath = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
            + schemaFilePath;
        }
        ZIPUtils.addFileTypeFileToZip(zipEntries, schema.getPath(), schemaFilePath, fileType);
      }
    }
  }

  public void addSubmissionsToZipAndMETS(final List<ZipEntryInfo> zipEntries, final MetsWrapper metsWrapper,
    final List<IPFile> submissions) throws IPException, InterruptedException {
    if (submissions != null && !submissions.isEmpty()) {
      for (IPFile submission : submissions) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        final String submissionFilePath = IPConstants.SUBMISSION_FOLDER
          + getFoldersFromList(submission.getRelativeFolders()) + submission.getFileName();
        final FileType fileType = EARKMETSUtils.addSubmissionFileToMETS(metsWrapper, submissionFilePath,
          submission.getPath());
        ZIPUtils.addFileTypeFileToZip(zipEntries, submission.getPath(), submissionFilePath, fileType);
      }
    }
  }

  public void addDocumentationToZipAndMETS(List<ZipEntryInfo> zipEntries, MetsWrapper metsWrapper,
    List<IPFile> documentation, String representationId) throws IPException, InterruptedException {
    if (documentation != null && !documentation.isEmpty()) {
      for (IPFile doc : documentation) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }

        String documentationFilePath = IPConstants.DOCUMENTATION_FOLDER + getFoldersFromList(doc.getRelativeFolders())
          + doc.getFileName();
        FileType fileType = EARKMETSUtils.addDocumentationFileToMETS(metsWrapper, documentationFilePath, doc.getPath());

        if (representationId != null) {
          documentationFilePath = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
            + documentationFilePath;
        }
        ZIPUtils.addFileTypeFileToZip(zipEntries, doc.getPath(), documentationFilePath, fileType);
      }
    }
  }

  private void addMainMETSToZip(List<ZipEntryInfo> zipEntries, MetsWrapper mainMETSWrapper, Path buildDir)
    throws IPException {
    EARKMETSUtils.addMainMETSToZip(zipEntries, mainMETSWrapper, IPConstants.METS_FILE, buildDir);
  }

  private void writeToPath(final List<ZipEntryInfo> zipEntryInfos, final Path path, final boolean onlyMets)
    throws IPException, InterruptedException {
    try {
      Files.createDirectories(path);
      for (ZipEntryInfo zipEntryInfo : zipEntryInfos) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }

        zipEntryInfo.prepareEntryforZipping();
        LOGGER.debug("Writing file {}", zipEntryInfo.getFilePath());
        final Path outputPath = Paths.get(path.toString(), zipEntryInfo.getName());
        writeFileToPath(zipEntryInfo, outputPath, onlyMets);
      }
    } catch (final IOException | NoSuchAlgorithmException e) {
      LOGGER.debug("Error in write method", e);
      throw new IPException(e.getMessage(), e);
    }
  }

  private void writeFileToPath(final ZipEntryInfo zipEntryInfo, final Path outputPath, final boolean onlyMets)
    throws IOException, NoSuchAlgorithmException {
    InputStream is = null;
    OutputStream os = null;
    try {

      is = Files.newInputStream(zipEntryInfo.getFilePath());

      if (!onlyMets || zipEntryInfo instanceof METSZipEntryInfo) {
        Files.createDirectories(outputPath.getParent());
        os = Files.newOutputStream(outputPath);
      } else {
        os = new NullOutputStream();
      }

      final byte[] buffer = new byte[4096];
      final MessageDigest complete = MessageDigest.getInstance(IPConstants.CHECKSUM_ALGORITHM);
      int numRead;
      do {
        numRead = is.read(buffer);
        if (numRead > 0) {
          complete.update(buffer, 0, numRead);
          if (!onlyMets || zipEntryInfo instanceof METSZipEntryInfo) {
            os.write(buffer, 0, numRead);
          }
        }
      } while (numRead != -1);

      setChecksum(zipEntryInfo, DatatypeConverter.printHexBinary(complete.digest()), IPConstants.CHECKSUM_ALGORITHM);
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(os);
    }
  }

  private void setChecksum(final ZipEntryInfo zipEntryInfo, final String checksum, final String checksumType)
    throws IOException, NoSuchAlgorithmException {
    zipEntryInfo.setChecksum(checksum);
    zipEntryInfo.setChecksumAlgorithm(checksumType);
    if (zipEntryInfo instanceof METSFileTypeZipEntryInfo) {
      METSFileTypeZipEntryInfo f = (METSFileTypeZipEntryInfo) zipEntryInfo;
      f.getMetsFileType().setCHECKSUM(checksum);
      f.getMetsFileType().setCHECKSUMTYPE(checksumType);
    } else if (zipEntryInfo instanceof METSMdRefZipEntryInfo) {
      METSMdRefZipEntryInfo f = (METSMdRefZipEntryInfo) zipEntryInfo;
      f.getMetsMdRef().setCHECKSUM(checksum);
      f.getMetsMdRef().setCHECKSUMTYPE(checksumType);
    }
  }

  private String getFoldersFromList(List<String> folders) {
    StringBuilder sb = new StringBuilder();
    for (String folder : folders) {
      sb.append(folder);
      if (sb.length() > 0) {
        sb.append(IPConstants.ZIP_PATH_SEPARATOR);
      }
    }
    return sb.toString();
  }

  private static AIP parseEARKAIP(final Path source, final Path destinationDirectory) throws ParseException {
    Path aipPath = ZIPUtils.extractIPIfInZipFormat(source, destinationDirectory, FILE_EXTENSION);
    return parseEARKAIPFromPath(aipPath);
  }

  private static AIP parseEARKAIPFromPath(final Path aipPath) throws ParseException {
    try {
      final AIP aip = new EARKAIP(new BasicAIP());
      aip.setBasePath(aipPath);
      final MetsWrapper metsWrapper = processMainMets(aip, aipPath);

      if (aip.isValid()) {

        final StructMapType structMap = getEARKStructMap(metsWrapper, aip, true);

        if (structMap != null) {
          preProcessStructMap(metsWrapper, structMap);

          processDescriptiveMetadata(metsWrapper, aip, null, aip.getBasePath());

          processOtherMetadata(metsWrapper, aip, null, aip.getBasePath());

          processPreservationMetadata(metsWrapper, aip, null, aip.getBasePath());

          processRepresentations(metsWrapper, aip);

          processSchemasMetadata(metsWrapper, aip, aip.getBasePath());

          processDocumentationMetadata(metsWrapper, aip, aip.getBasePath());

          processSubmissionMetadata(metsWrapper, aip, aip.getBasePath());

          processAncestors(metsWrapper, aip);
        }
      }

      return aip;
    } catch (final IPException e) {
      throw new ParseException("Error parsing E-ARK AIP", e);
    }
  }

  private static MetsWrapper processMainMets(AIP aip, Path aipPath) {
    Path mainMETSFile = aipPath.resolve(IPConstants.METS_FILE);
    Mets mainMets = null;
    if (Files.exists(mainMETSFile)) {
      ValidationUtils.addInfo(aip.getValidationReport(), ValidationConstants.MAIN_METS_FILE_FOUND, aipPath,
        mainMETSFile);
      try {
        mainMets = METSUtils.instantiateMETSFromFile(mainMETSFile);
        aip.setIds(Arrays.asList(mainMets.getOBJID().split(" ")));
        aip.setCreateDate(mainMets.getMetsHdr().getCREATEDATE());
        aip.setModificationDate(mainMets.getMetsHdr().getLASTMODDATE());
        aip.setStatus(IPStatus.parse(mainMets.getMetsHdr().getRECORDSTATUS()));
        setAIPContentType(mainMets, aip);
        addAgentsToMETS(mainMets, aip, null);

        ValidationUtils.addInfo(aip.getValidationReport(), ValidationConstants.MAIN_METS_IS_VALID, aipPath,
          mainMETSFile);
      } catch (final JAXBException | ParseException | SAXException e) {
        mainMets = null;
        ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.MAIN_METS_NOT_VALID,
          ValidationEntry.LEVEL.ERROR, e, aip.getBasePath(), mainMETSFile);
      }
    } else {
      ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.MAIN_METS_FILE_NOT_FOUND,
        ValidationEntry.LEVEL.ERROR, aip.getBasePath(), mainMETSFile);
    }
    return new MetsWrapper(mainMets, mainMETSFile);
  }

  private static MetsWrapper processRepresentationMets(AIP aip, Path representationMetsFile,
    IPRepresentation representation) {
    Mets representationMets = null;
    if (Files.exists(representationMetsFile)) {
      ValidationUtils.addInfo(aip.getValidationReport(), ValidationConstants.REPRESENTATION_METS_FILE_FOUND,
        aip.getBasePath(), representationMetsFile);
      try {
        representationMets = METSUtils.instantiateMETSFromFile(representationMetsFile);
        setRepresentationContentType(representationMets, representation);
        ValidationUtils.addInfo(aip.getValidationReport(), ValidationConstants.REPRESENTATION_METS_IS_VALID,
          aip.getBasePath(), representationMetsFile);
      } catch (JAXBException | ParseException | SAXException e) {
        representationMets = null;
        ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.REPRESENTATION_METS_NOT_VALID,
          ValidationEntry.LEVEL.ERROR, e, aip.getBasePath(), representationMetsFile);
      }
    } else {
      ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.REPRESENTATION_METS_FILE_NOT_FOUND,
        ValidationEntry.LEVEL.ERROR, aip.getBasePath(), representationMetsFile);
    }
    return new MetsWrapper(representationMets, representationMetsFile);
  }

  private static void setAIPContentType(Mets mets, AIP aip) throws ParseException {
    String metsType = mets.getTYPE();

    if (StringUtils.isBlank(metsType)) {
      throw new ParseException("METS 'TYPE' attribute does not contain any value");
    }

    String[] contentTypeParts = metsType.split(":");
    if (contentTypeParts.length != 2 || StringUtils.isBlank(contentTypeParts[0])
      || StringUtils.isBlank(contentTypeParts[1])) {
      throw new ParseException("METS 'TYPE' attribute does not contain a valid value");
    }

    IPEnums.IPType packageType;
    try {
      packageType = IPEnums.IPType.valueOf(contentTypeParts[0]);
      if (IPEnums.IPType.AIP != packageType) {
        throw new ParseException("METS 'TYPE' attribute should start with 'AIP:'");
      }
    } catch (IllegalArgumentException e) {
      throw new ParseException("METS 'TYPE' attribute does not contain a valid package type");
    }

    aip.setContentType(new IPContentType(contentTypeParts[1]));
  }

  private static void setRepresentationContentType(Mets mets, IPRepresentation representation) throws ParseException {
    String metsType = mets.getTYPE();

    if (StringUtils.isBlank(metsType)) {
      throw new ParseException("METS 'TYPE' attribute does not contain any value");
    }

    if ("representation".equals(metsType)) {
      if (VALIDATION_FAIL_IF_REPRESENTATION_METS_DOES_NOT_HAVE_TWO_PARTS) {
        throw new ParseException(
          "METS 'TYPE' attribute is not valid as it should be 'representation:REPRESENTATION_TYPE'");
      } else {
        return;
      }
    }

    String[] contentTypeParts = metsType.split(":");
    if (contentTypeParts.length != 2 || StringUtils.isBlank(contentTypeParts[0])
      || !"representation".equals(contentTypeParts[0]) || StringUtils.isBlank(contentTypeParts[1])) {
      throw new ParseException("METS 'TYPE' attribute does not contain a valid value");
    }

    representation.setContentType(new RepresentationContentType(contentTypeParts[1]));
  }

  private static Mets addAgentsToMETS(Mets mets, AIP aip, IPRepresentation representation) {
    if (mets.getMetsHdr() != null && mets.getMetsHdr().getAgent() != null) {
      for (MetsType.MetsHdr.Agent agent : mets.getMetsHdr().getAgent()) {
        if (representation == null) {
          aip.addAgent(EARKMETSUtils.createIPAgent(agent));
        } else {
          representation.addAgent(EARKMETSUtils.createIPAgent(agent));
        }
      }
    }

    return mets;
  }

  private static StructMapType getEARKStructMap(MetsWrapper metsWrapper, AIP aip, boolean mainMets) {
    Mets mets = metsWrapper.getMets();
    StructMapType res = null;
    for (StructMapType structMap : mets.getStructMap()) {
      if (StringUtils.equals(structMap.getLABEL(), IPConstants.E_ARK_STRUCTURAL_MAP)) {
        res = structMap;
        break;
      }
    }
    if (res == null) {
      ValidationUtils.addIssue(aip.getValidationReport(),
        mainMets ? ValidationConstants.MAIN_METS_HAS_NO_E_ARK_STRUCT_MAP
          : ValidationConstants.REPRESENTATION_METS_HAS_NO_E_ARK_STRUCT_MAP,
        ValidationEntry.LEVEL.ERROR, res, aip.getBasePath(), metsWrapper.getMetsPath());
    } else {
      ValidationUtils.addInfo(aip.getValidationReport(),
        mainMets ? ValidationConstants.MAIN_METS_HAS_E_ARK_STRUCT_MAP
          : ValidationConstants.REPRESENTATION_METS_HAS_E_ARK_STRUCT_MAP,
        res, aip.getBasePath(), metsWrapper.getMetsPath());
    }
    return res;
  }

  private static void preProcessStructMap(MetsWrapper metsWrapper, StructMapType structMap) {

    DivType aipDiv = structMap.getDiv();
    if (aipDiv.getDiv() != null) {
      metsWrapper.setMainDiv(aipDiv);
      for (DivType firstLevel : aipDiv.getDiv()) {
        if (IPConstants.METADATA.equalsIgnoreCase(firstLevel.getLABEL()) && firstLevel.getDiv() != null) {
          for (DivType secondLevel : firstLevel.getDiv()) {
            if (IPConstants.DESCRIPTIVE.equalsIgnoreCase(secondLevel.getLABEL())) {
              metsWrapper.setDescriptiveMetadataDiv(secondLevel);
            } else if (IPConstants.PRESERVATION.equalsIgnoreCase(secondLevel.getLABEL())) {
              metsWrapper.setPreservationMetadataDiv(secondLevel);
            } else if (IPConstants.OTHER.equalsIgnoreCase(secondLevel.getLABEL())) {
              metsWrapper.setOtherMetadataDiv(secondLevel);
            }
          }
        } else if (IPConstants.REPRESENTATIONS.equalsIgnoreCase(firstLevel.getLABEL())) {
          metsWrapper.setRepresentationsDiv(firstLevel);
        } else if (IPConstants.DATA.equalsIgnoreCase(firstLevel.getLABEL())) {
          metsWrapper.setDataDiv(firstLevel);
        } else if (IPConstants.SCHEMAS.equalsIgnoreCase(firstLevel.getLABEL())) {
          metsWrapper.setSchemasDiv(firstLevel);
        } else if (IPConstants.DOCUMENTATION.equalsIgnoreCase(firstLevel.getLABEL())) {
          metsWrapper.setDocumentationDiv(firstLevel);
        } else if (IPConstants.SUBMISSION.equalsIgnoreCase(firstLevel.getLABEL())) {
          metsWrapper.setSubmissionsDiv(firstLevel);
        }
      }
    }
  }

  private static AIP processDescriptiveMetadata(MetsWrapper metsWrapper, AIP aip, IPRepresentation representation,
    Path basePath) throws IPException {

    return processMetadata(aip, metsWrapper, representation, metsWrapper.getDescriptiveMetadataDiv(),
      IPConstants.DESCRIPTIVE, basePath);
  }

  private static AIP processOtherMetadata(MetsWrapper metsWrapper, AIP aip, IPRepresentation representation,
    Path basePath) throws IPException {

    return processMetadata(aip, metsWrapper, representation, metsWrapper.getOtherMetadataDiv(), IPConstants.OTHER,
      basePath);
  }

  private static AIP processPreservationMetadata(MetsWrapper metsWrapper, AIP aip, IPRepresentation representation,
    Path basePath) throws IPException {

    return processMetadata(aip, metsWrapper, representation, metsWrapper.getPreservationMetadataDiv(),
      IPConstants.PRESERVATION, basePath);
  }

  private static AIP processMetadata(AIP aip, MetsWrapper representationMetsWrapper, IPRepresentation representation,
    DivType div, String metadataType, Path basePath) throws IPException {
    if (div != null && div.getFptr() != null) {
      for (Fptr fptr : div.getFptr()) {
        MdRef mdRef = (MdRef) fptr.getFILEID();
        String href = Utils.extractedRelativePathFromHref(mdRef);
        Path filePath = basePath.resolve(href);
        if (Files.exists(filePath)) {
          List<String> fileRelativeFolders = Utils
            .getFileRelativeFolders(basePath.resolve(IPConstants.METADATA).resolve(metadataType), filePath);

          processMetadataFile(aip, representation, metadataType, mdRef, filePath, fileRelativeFolders);
        } else {
          ValidationUtils.addIssue(aip.getValidationReport(),
            ValidationConstants.getMetadataFileNotFoundString(metadataType), ValidationEntry.LEVEL.ERROR,
            aip.getBasePath(), filePath);
        }
      }
    } else {
      ValidationUtils.addIssue(aip.getValidationReport(),
        ValidationConstants.getMetadataFileFptrNotFoundString(metadataType), ValidationEntry.LEVEL.ERROR,
        aip.getBasePath(), representationMetsWrapper.getMetsPath());
    }

    return aip;
  }

  private static void processMetadataFile(AIP aip, IPRepresentation representation, String metadataType, MdRef mdRef,
    Path filePath, List<String> fileRelativeFolders) throws IPException {
    Optional<IPFile> metadataFile = validateMetadataFile(aip, filePath, mdRef, fileRelativeFolders);
    if (metadataFile.isPresent()) {
      ValidationUtils.addInfo(aip.getValidationReport(),
        ValidationConstants.getMetadataFileFoundWithMatchingChecksumString(metadataType), aip.getBasePath(), filePath);

      if (IPConstants.DESCRIPTIVE.equalsIgnoreCase(metadataType)) {
        MetadataType dmdType = new MetadataType(mdRef.getMDTYPE().toUpperCase());
        String dmdVersion = null;
        try {
          dmdVersion = mdRef.getMDTYPEVERSION();
          if (StringUtils.isNotBlank(mdRef.getOTHERMDTYPE())) {
            dmdType.setOtherType(mdRef.getOTHERMDTYPE());
          }
          LOGGER.debug("Metadata type valid: {}", dmdType);
        } catch (NullPointerException | IllegalArgumentException e) {
          // do nothing and use already defined values for metadataType &
          // metadataVersion
          LOGGER.debug("Setting metadata type to {}", dmdType);
          ValidationUtils.addEntry(aip.getValidationReport(), ValidationConstants.UNKNOWN_DESCRIPTIVE_METADATA_TYPE,
            ValidationEntry.LEVEL.WARN, "Setting metadata type to " + dmdType, aip.getBasePath(), filePath);
        }

        IPDescriptiveMetadata descriptiveMetadata = new IPDescriptiveMetadata(mdRef.getID(), metadataFile.get(),
          dmdType, dmdVersion);
        descriptiveMetadata.setCreateDate(mdRef.getCREATED());
        if (representation == null) {
          aip.addDescriptiveMetadata(descriptiveMetadata);
        } else {
          representation.addDescriptiveMetadata(descriptiveMetadata);
        }
      } else if (IPConstants.PRESERVATION.equalsIgnoreCase(metadataType)) {
        IPMetadata preservationMetadata = new IPMetadata(metadataFile.get());
        preservationMetadata.setCreateDate(mdRef.getCREATED());
        if (representation == null) {
          aip.addPreservationMetadata(preservationMetadata);
        } else {
          representation.addPreservationMetadata(preservationMetadata);
        }
      } else if (IPConstants.OTHER.equalsIgnoreCase(metadataType)) {
        IPMetadata otherMetadata = new IPMetadata(metadataFile.get());
        otherMetadata.setCreateDate(mdRef.getCREATED());
        if (representation == null) {
          aip.addOtherMetadata(otherMetadata);
        } else {
          representation.addOtherMetadata(otherMetadata);
        }
      }
    }
  }

  private static Optional<IPFile> validateFile(AIP aip, Path filePath, FileType fileType,
    List<String> fileRelativeFolders) {
    return Utils.validateFile(aip, filePath, fileRelativeFolders, fileType.getCHECKSUM(), fileType.getCHECKSUMTYPE(),
      fileType.getID());
  }

  private static Optional<IPFile> validateMetadataFile(AIP aip, Path filePath, MdRef mdRef,
    List<String> fileRelativeFolders) {
    return Utils.validateFile(aip, filePath, fileRelativeFolders, mdRef.getCHECKSUM(), mdRef.getCHECKSUMTYPE(),
      mdRef.getID());
  }

  private static AIP processFile(AIP aip, DivType div, String folder, Path basePath) throws IPException {
    if (div != null && div.getFptr() != null) {
      for (Fptr fptr : div.getFptr()) {
        FileType fileType = (FileType) fptr.getFILEID();

        if (fileType.getFLocat() != null) {
          FLocat fLocat = fileType.getFLocat().get(0);
          String href = Utils.extractedRelativePathFromHref(fLocat.getHref());
          Path filePath = basePath.resolve(href);

          if (Files.exists(filePath)) {
            List<String> fileRelativeFolders = Utils.getFileRelativeFolders(basePath.resolve(folder), filePath);
            Optional<IPFile> file = validateFile(aip, filePath, fileType, fileRelativeFolders);

            if (file.isPresent()) {
              if (IPConstants.SCHEMAS.equalsIgnoreCase(folder)) {
                ValidationUtils.addInfo(aip.getValidationReport(),
                  ValidationConstants.SCHEMA_FILE_FOUND_WITH_MATCHING_CHECKSUMS, aip.getBasePath(), filePath);
                aip.addSchema(file.get());
              } else if (IPConstants.DOCUMENTATION.equalsIgnoreCase(folder)) {
                ValidationUtils.addInfo(aip.getValidationReport(),
                  ValidationConstants.DOCUMENTATION_FILE_FOUND_WITH_MATCHING_CHECKSUMS, aip.getBasePath(), filePath);
                aip.addDocumentation(file.get());
              } else if (IPConstants.SUBMISSION.equalsIgnoreCase(folder)) {
                ValidationUtils.addInfo(aip.getValidationReport(),
                  ValidationConstants.SUBMISSION_FILE_FOUND_WITH_MATCHING_CHECKSUMS, aip.getBasePath(), filePath);
                aip.addSubmission(file.get());
              }
            }
          } else {
            if (IPConstants.SCHEMAS.equalsIgnoreCase(folder)) {
              ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.SCHEMA_FILE_NOT_FOUND,
                ValidationEntry.LEVEL.ERROR, div, aip.getBasePath(), filePath);
            } else if (IPConstants.DOCUMENTATION.equalsIgnoreCase(folder)) {
              ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.DOCUMENTATION_FILE_NOT_FOUND,
                ValidationEntry.LEVEL.ERROR, div, aip.getBasePath(), filePath);
            } else if (IPConstants.SUBMISSION.equalsIgnoreCase(folder)) {
              ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.SUBMISSION_FILE_NOT_FOUND,
                ValidationEntry.LEVEL.ERROR, div, aip.getBasePath(), filePath);
            }
          }
        }
      }
    }

    return aip;
  }

  private static AIP processRepresentations(MetsWrapper metsWrapper, AIP aip) throws IPException {

    if (metsWrapper.getRepresentationsDiv() != null && metsWrapper.getRepresentationsDiv().getDiv() != null) {
      for (DivType representationDiv : metsWrapper.getRepresentationsDiv().getDiv()) {
        if (representationDiv.getMptr() != null && !representationDiv.getMptr().isEmpty()) {
          // we can assume one and only one mets for each representation div
          Mptr mptr = representationDiv.getMptr().get(0);
          String href = Utils.extractedRelativePathFromHref(mptr.getHref());
          Path metsFilePath = aip.getBasePath().resolve(href);
          IPRepresentation representation = new IPRepresentation(representationDiv.getLABEL());
          MetsWrapper representationMetsWrapper = processRepresentationMets(aip, metsFilePath, representation);

          if (representationMetsWrapper.getMets() != null) {
            Path representationBasePath = metsFilePath.getParent();

            StructMapType representationStructMap = getEARKStructMap(representationMetsWrapper, aip, false);
            if (representationStructMap != null) {

              preProcessStructMap(representationMetsWrapper, representationStructMap);
              representation.setStatus(new RepresentationStatus(representationMetsWrapper.getMainDiv().getTYPE()));
              aip.addRepresentation(representation);

              // process representation agents
              processRepresentationAgents(representationMetsWrapper, representation);

              // process files
              processRepresentationFiles(aip, representationMetsWrapper, representation, representationBasePath);

              // process descriptive metadata
              processDescriptiveMetadata(representationMetsWrapper, aip, representation, representationBasePath);

              // process preservation metadata
              processPreservationMetadata(representationMetsWrapper, aip, representation, representationBasePath);

              // process other metadata
              processOtherMetadata(representationMetsWrapper, aip, representation, representationBasePath);

              // process schemas
              processSchemasMetadata(representationMetsWrapper, aip, representationBasePath);

              // process documentation
              processDocumentationMetadata(representationMetsWrapper, aip, representationBasePath);
            }
          }
        }
      }

      // post-process validations
      if (aip.getRepresentations().isEmpty()) {
        ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.MAIN_METS_NO_REPRESENTATIONS_FOUND,
          ValidationEntry.LEVEL.WARN, metsWrapper.getRepresentationsDiv(), aip.getBasePath(),
          metsWrapper.getMetsPath());
      }
    }

    return aip;

  }

  private static void processRepresentationAgents(MetsWrapper representationMetsWrapper,
    IPRepresentation representation) {

    addAgentsToMETS(representationMetsWrapper.getMets(), null, representation);
  }

  private static void processRepresentationFiles(AIP aip, MetsWrapper representationMetsWrapper,
    IPRepresentation representation, Path representationBasePath) throws IPException {

    if (representationMetsWrapper.getDataDiv() != null && representationMetsWrapper.getDataDiv().getFptr() != null) {
      for (Fptr fptr : representationMetsWrapper.getDataDiv().getFptr()) {
        FileType fileType = (FileType) fptr.getFILEID();

        if (fileType != null && fileType.getFLocat() != null) {
          FLocat fLocat = fileType.getFLocat().get(0);
          String href = Utils.extractedRelativePathFromHref(fLocat.getHref());
          Path filePath = representationBasePath.resolve(href);
          if (Files.exists(filePath)) {
            List<String> fileRelativeFolders = Utils
              .getFileRelativeFolders(representationBasePath.resolve(IPConstants.DATA), filePath);
            Optional<IPFile> file = validateFile(aip, filePath, fileType, fileRelativeFolders);

            if (file.isPresent()) {
              representation.addFile(file.get());
              ValidationUtils.addInfo(aip.getValidationReport(),
                ValidationConstants.REPRESENTATION_FILE_FOUND_WITH_MATCHING_CHECKSUMS, aip.getBasePath(), filePath);
            }
          } else {
            ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.REPRESENTATION_FILE_NOT_FOUND,
              ValidationEntry.LEVEL.ERROR, aip.getBasePath(), filePath);
          }
        } else {
          ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.REPRESENTATION_FILE_HAS_NO_FLOCAT,
            ValidationEntry.LEVEL.ERROR, fileType, aip.getBasePath(), representationMetsWrapper.getMetsPath());
        }
      }

      // post-process validations
      if (representation.getData().isEmpty()) {
        ValidationUtils.addIssue(aip.getValidationReport(), ValidationConstants.REPRESENTATION_HAS_NO_FILES,
          ValidationEntry.LEVEL.WARN, representationMetsWrapper.getDataDiv(), aip.getBasePath(),
          representationMetsWrapper.getMetsPath());
      }
    }

  }

  private static AIP processSchemasMetadata(MetsWrapper metsWrapper, AIP aip, Path basePath) throws IPException {

    return processFile(aip, metsWrapper.getSchemasDiv(), IPConstants.SCHEMAS, basePath);
  }

  private static AIP processDocumentationMetadata(MetsWrapper metsWrapper, AIP aip, Path basePath) throws IPException {

    return processFile(aip, metsWrapper.getDocumentationDiv(), IPConstants.DOCUMENTATION, basePath);
  }

  private static AIP processSubmissionMetadata(final MetsWrapper metsWrapper, final AIP aip, final Path basePath)
    throws IPException {

    return processFile(aip, metsWrapper.getSubmissionsDiv(), IPConstants.SUBMISSION, basePath);

  }

  private static AIP processAncestors(MetsWrapper metsWrapper, AIP aip) {
    Mets mets = metsWrapper.getMets();

    if (mets.getStructMap() != null && !mets.getStructMap().isEmpty()) {
      aip.setAncestors(EARKMETSUtils.extractAncestorsFromStructMap(mets));
    }

    return aip;
  }
}
