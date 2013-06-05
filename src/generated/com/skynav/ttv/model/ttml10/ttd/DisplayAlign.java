//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.04 at 06:31:29 PM JST 
//


package com.skynav.ttv.model.ttml10.ttd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for displayAlign.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="displayAlign">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="before"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="after"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "displayAlign", namespace = "http://www.w3.org/ns/ttml#datatype")
@XmlEnum
public enum DisplayAlign {

    @XmlEnumValue("before")
    BEFORE("before"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("after")
    AFTER("after");
    private final String value;

    DisplayAlign(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DisplayAlign fromValue(String v) {
        for (DisplayAlign c: DisplayAlign.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
