
package com.thesecretserver.service;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r ArrayOfSecretItemHistoryWebServiceResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSecretItemHistoryWebServiceResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SecretItemHistoryWebServiceResult" type="{urn:thesecretserver.com}SecretItemHistoryWebServiceResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSecretItemHistoryWebServiceResult", propOrder = {
    "secretItemHistoryWebServiceResult"
})
public class ArrayOfSecretItemHistoryWebServiceResult {

    @XmlElement(name = "SecretItemHistoryWebServiceResult", nillable = true)
    protected List<SecretItemHistoryWebServiceResult> secretItemHistoryWebServiceResult;

    /**
     * Gets the value of the secretItemHistoryWebServiceResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the secretItemHistoryWebServiceResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecretItemHistoryWebServiceResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecretItemHistoryWebServiceResult }
     * 
     * 
     */
    public List<SecretItemHistoryWebServiceResult> getSecretItemHistoryWebServiceResult() {
        if (secretItemHistoryWebServiceResult == null) {
            secretItemHistoryWebServiceResult = new ArrayList<SecretItemHistoryWebServiceResult>();
        }
        return this.secretItemHistoryWebServiceResult;
    }

}
