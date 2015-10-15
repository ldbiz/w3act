
package com.thesecretserver.service;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r ArrayOfSecretField complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSecretField">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SecretField" type="{urn:thesecretserver.com}SecretField" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSecretField", propOrder = {
    "secretField"
})
public class ArrayOfSecretField {

    @XmlElement(name = "SecretField", nillable = true)
    protected List<SecretField> secretField;

    /**
     * Gets the value of the secretField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the secretField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecretField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecretField }
     * 
     * 
     */
    public List<SecretField> getSecretField() {
        if (secretField == null) {
            secretField = new ArrayList<SecretField>();
        }
        return this.secretField;
    }

}
