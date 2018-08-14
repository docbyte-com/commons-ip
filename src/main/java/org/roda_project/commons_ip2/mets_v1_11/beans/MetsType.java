/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/commons-ip
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.09.07 at 04:09:27 PM WEST 
//


package org.roda_project.commons_ip2.mets_v1_11.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for metsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="metsType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.loc.gov/METS/}metsType">
 *       &lt;redefine>
 *         &lt;complexType name="metsType">
 *           &lt;complexContent>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;sequence>
 *                 &lt;element name="metsHdr" minOccurs="0">
 *                   &lt;complexType>
 *                     &lt;complexContent>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                         &lt;sequence>
 *                           &lt;element name="agent" maxOccurs="unbounded" minOccurs="0">
 *                             &lt;complexType>
 *                               &lt;complexContent>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                   &lt;sequence>
 *                                     &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                     &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                                   &lt;/sequence>
 *                                   &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                                   &lt;attribute name="ROLE" use="required">
 *                                     &lt;simpleType>
 *                                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                         &lt;enumeration value="CREATOR"/>
 *                                         &lt;enumeration value="EDITOR"/>
 *                                         &lt;enumeration value="ARCHIVIST"/>
 *                                         &lt;enumeration value="PRESERVATION"/>
 *                                         &lt;enumeration value="DISSEMINATOR"/>
 *                                         &lt;enumeration value="CUSTODIAN"/>
 *                                         &lt;enumeration value="IPOWNER"/>
 *                                         &lt;enumeration value="OTHER"/>
 *                                       &lt;/restriction>
 *                                     &lt;/simpleType>
 *                                   &lt;/attribute>
 *                                   &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;attribute name="TYPE">
 *                                     &lt;simpleType>
 *                                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                         &lt;enumeration value="INDIVIDUAL"/>
 *                                         &lt;enumeration value="ORGANIZATION"/>
 *                                         &lt;enumeration value="OTHER"/>
 *                                       &lt;/restriction>
 *                                     &lt;/simpleType>
 *                                   &lt;/attribute>
 *                                   &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                 &lt;/restriction>
 *                               &lt;/complexContent>
 *                             &lt;/complexType>
 *                           &lt;/element>
 *                           &lt;element name="altRecordID" maxOccurs="unbounded" minOccurs="0">
 *                             &lt;complexType>
 *                               &lt;simpleContent>
 *                                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                   &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                                   &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                 &lt;/extension>
 *                               &lt;/simpleContent>
 *                             &lt;/complexType>
 *                           &lt;/element>
 *                           &lt;element name="metsDocumentID" minOccurs="0">
 *                             &lt;complexType>
 *                               &lt;simpleContent>
 *                                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                   &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                                   &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                 &lt;/extension>
 *                               &lt;/simpleContent>
 *                             &lt;/complexType>
 *                           &lt;/element>
 *                         &lt;/sequence>
 *                         &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                         &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *                         &lt;attribute name="CREATEDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                         &lt;attribute name="LASTMODDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                         &lt;attribute name="RECORDSTATUS" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;anyAttribute processContents='lax' namespace='##other'/>
 *                       &lt;/restriction>
 *                     &lt;/complexContent>
 *                   &lt;/complexType>
 *                 &lt;/element>
 *                 &lt;element name="dmdSec" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;element name="amdSec" type="{http://www.loc.gov/METS/}amdSecType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;element name="fileSec" minOccurs="0">
 *                   &lt;complexType>
 *                     &lt;complexContent>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                         &lt;sequence>
 *                           &lt;element name="fileGrp" maxOccurs="unbounded">
 *                             &lt;complexType>
 *                               &lt;complexContent>
 *                                 &lt;extension base="{http://www.loc.gov/METS/}fileGrpType">
 *                                   &lt;anyAttribute processContents='lax' namespace='##other'/>
 *                                 &lt;/extension>
 *                               &lt;/complexContent>
 *                             &lt;/complexType>
 *                           &lt;/element>
 *                         &lt;/sequence>
 *                         &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                         &lt;anyAttribute processContents='lax' namespace='##other'/>
 *                       &lt;/restriction>
 *                     &lt;/complexContent>
 *                   &lt;/complexType>
 *                 &lt;/element>
 *                 &lt;element name="structMap" type="{http://www.loc.gov/METS/}structMapType" maxOccurs="unbounded"/>
 *                 &lt;element name="structLink" minOccurs="0">
 *                   &lt;complexType>
 *                     &lt;complexContent>
 *                       &lt;extension base="{http://www.loc.gov/METS/}structLinkType">
 *                         &lt;anyAttribute processContents='lax' namespace='##other'/>
 *                       &lt;/extension>
 *                     &lt;/complexContent>
 *                   &lt;/complexType>
 *                 &lt;/element>
 *                 &lt;element name="behaviorSec" type="{http://www.loc.gov/METS/}behaviorSecType" maxOccurs="unbounded" minOccurs="0"/>
 *               &lt;/sequence>
 *               &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *               &lt;attribute name="OBJID" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;attribute name="PROFILE" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;anyAttribute processContents='lax' namespace='##other'/>
 *             &lt;/restriction>
 *           &lt;/complexContent>
 *         &lt;/complexType>
 *       &lt;/redefine>
 *       &lt;attribute name="OAISPACKAGETYPE" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="SIP"/>
 *             &lt;enumeration value="AIP"/>
 *             &lt;enumeration value="DIP"/>
 *             &lt;enumeration value="AIU"/>
 *             &lt;enumeration value="AIC"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="CONTENTTYPESPECIFICATION" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="SMURFERMS"/>
 *             &lt;enumeration value="SMURFSFSB"/>
 *             &lt;enumeration value="SIARD1"/>
 *             &lt;enumeration value="SIARD2"/>
 *             &lt;enumeration value="SIARDDK"/>
 *             &lt;enumeration value="GeoVectorGML"/>
 *             &lt;enumeration value="GeoRasterGeotiff"/>
 *             &lt;enumeration value="MIXED"/>
 *             &lt;enumeration value="OTHER"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="OTHERCONTENTTYPESPECIFICATION" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metsType", namespace = "http://www.loc.gov/METS/")
@XmlSeeAlso({
    Mets.class
})
public class MetsType
    extends OriginalMetsType
{

    @XmlAttribute(name = "OAISPACKAGETYPE", required = true)
    protected String oaispackagetype;
    @XmlAttribute(name = "CONTENTTYPESPECIFICATION", required = true)
    protected String contenttypespecification;
    @XmlAttribute(name = "OTHERCONTENTTYPESPECIFICATION")
    @XmlSchemaType(name = "anySimpleType")
    protected String othercontenttypespecification;

    /**
     * Gets the value of the oaispackagetype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOAISPACKAGETYPE() {
        return oaispackagetype;
    }

    /**
     * Sets the value of the oaispackagetype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOAISPACKAGETYPE(String value) {
        this.oaispackagetype = value;
    }

    /**
     * Gets the value of the contenttypespecification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCONTENTTYPESPECIFICATION() {
        return contenttypespecification;
    }

    /**
     * Sets the value of the contenttypespecification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCONTENTTYPESPECIFICATION(String value) {
        this.contenttypespecification = value;
    }

    /**
     * Gets the value of the othercontenttypespecification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOTHERCONTENTTYPESPECIFICATION() {
        return othercontenttypespecification;
    }

    /**
     * Sets the value of the othercontenttypespecification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOTHERCONTENTTYPESPECIFICATION(String value) {
        this.othercontenttypespecification = value;
    }

}
