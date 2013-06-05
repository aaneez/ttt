//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.04 at 06:31:29 PM JST 
//


package com.skynav.ttv.model.ttml10.tt;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.skynav.ttv.model.ttml10.tt package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Div_QNAME = new QName("http://www.w3.org/ns/ttml", "div");
    private final static QName _Br_QNAME = new QName("http://www.w3.org/ns/ttml", "br");
    private final static QName _Region_QNAME = new QName("http://www.w3.org/ns/ttml", "region");
    private final static QName _Body_QNAME = new QName("http://www.w3.org/ns/ttml", "body");
    private final static QName _Set_QNAME = new QName("http://www.w3.org/ns/ttml", "set");
    private final static QName _Metadata_QNAME = new QName("http://www.w3.org/ns/ttml", "metadata");
    private final static QName _Span_QNAME = new QName("http://www.w3.org/ns/ttml", "span");
    private final static QName _Head_QNAME = new QName("http://www.w3.org/ns/ttml", "head");
    private final static QName _Styling_QNAME = new QName("http://www.w3.org/ns/ttml", "styling");
    private final static QName _Layout_QNAME = new QName("http://www.w3.org/ns/ttml", "layout");
    private final static QName _P_QNAME = new QName("http://www.w3.org/ns/ttml", "p");
    private final static QName _Style_QNAME = new QName("http://www.w3.org/ns/ttml", "style");
    private final static QName _Tt_QNAME = new QName("http://www.w3.org/ns/ttml", "tt");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.skynav.ttv.model.ttml10.tt
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Break }
     * 
     */
    public Break createBreak() {
        return new Break();
    }

    /**
     * Create an instance of {@link Metadata }
     * 
     */
    public Metadata createMetadata() {
        return new Metadata();
    }

    /**
     * Create an instance of {@link Division }
     * 
     */
    public Division createDivision() {
        return new Division();
    }

    /**
     * Create an instance of {@link TimedText }
     * 
     */
    public TimedText createTimedText() {
        return new TimedText();
    }

    /**
     * Create an instance of {@link Set }
     * 
     */
    public Set createSet() {
        return new Set();
    }

    /**
     * Create an instance of {@link Styling }
     * 
     */
    public Styling createStyling() {
        return new Styling();
    }

    /**
     * Create an instance of {@link Style }
     * 
     */
    public Style createStyle() {
        return new Style();
    }

    /**
     * Create an instance of {@link Span }
     * 
     */
    public Span createSpan() {
        return new Span();
    }

    /**
     * Create an instance of {@link Body }
     * 
     */
    public Body createBody() {
        return new Body();
    }

    /**
     * Create an instance of {@link Layout }
     * 
     */
    public Layout createLayout() {
        return new Layout();
    }

    /**
     * Create an instance of {@link Paragraph }
     * 
     */
    public Paragraph createParagraph() {
        return new Paragraph();
    }

    /**
     * Create an instance of {@link Head }
     * 
     */
    public Head createHead() {
        return new Head();
    }

    /**
     * Create an instance of {@link Region }
     * 
     */
    public Region createRegion() {
        return new Region();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Division }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "div")
    public JAXBElement<Division> createDiv(Division value) {
        return new JAXBElement<Division>(_Div_QNAME, Division.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Break }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "br")
    public JAXBElement<Break> createBr(Break value) {
        return new JAXBElement<Break>(_Br_QNAME, Break.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Region }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "region")
    public JAXBElement<Region> createRegion(Region value) {
        return new JAXBElement<Region>(_Region_QNAME, Region.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Body }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "body")
    public JAXBElement<Body> createBody(Body value) {
        return new JAXBElement<Body>(_Body_QNAME, Body.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Set }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "set")
    public JAXBElement<Set> createSet(Set value) {
        return new JAXBElement<Set>(_Set_QNAME, Set.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Metadata }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "metadata")
    public JAXBElement<Metadata> createMetadata(Metadata value) {
        return new JAXBElement<Metadata>(_Metadata_QNAME, Metadata.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Span }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "span")
    public JAXBElement<Span> createSpan(Span value) {
        return new JAXBElement<Span>(_Span_QNAME, Span.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Head }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "head")
    public JAXBElement<Head> createHead(Head value) {
        return new JAXBElement<Head>(_Head_QNAME, Head.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Styling }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "styling")
    public JAXBElement<Styling> createStyling(Styling value) {
        return new JAXBElement<Styling>(_Styling_QNAME, Styling.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Layout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "layout")
    public JAXBElement<Layout> createLayout(Layout value) {
        return new JAXBElement<Layout>(_Layout_QNAME, Layout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Paragraph }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "p")
    public JAXBElement<Paragraph> createP(Paragraph value) {
        return new JAXBElement<Paragraph>(_P_QNAME, Paragraph.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Style }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "style")
    public JAXBElement<Style> createStyle(Style value) {
        return new JAXBElement<Style>(_Style_QNAME, Style.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimedText }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/ns/ttml", name = "tt")
    public JAXBElement<TimedText> createTt(TimedText value) {
        return new JAXBElement<TimedText>(_Tt_QNAME, TimedText.class, null, value);
    }

}
