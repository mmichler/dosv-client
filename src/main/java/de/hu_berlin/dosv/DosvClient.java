/*
 * DoSV-Client
 * Copyright (C) 2016  Humboldt-Universität zu Berlin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */

package de.hu_berlin.dosv;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Log-Levels: <code>trace</code> zeichnet den Programmfluss und die SOAP-Messages auf,
 * <code>debug</code> Ausgaben für Entwickler und <code>info</code> administrative
 * Nachrichten
 *
 * @author Markus Michler
 */
public class DosvClient {
    private static Logger logger = Logger.getLogger(DosvClient.class.getPackage().getName());

    private Map<String, Dispatch<Source>> dispatches;
    private SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private SafeString context;

    // TODO Javadoc für SAFs zuende schreiben
    // TODO WsExceptions in Doku

    // Webservice Parameternamen für Konfig
    public static final String UNIVERSITY_ID = "university_id";
    public static final String SEMESTER = "dosv_semester";
    public static final String YEAR = "dosv_year";
    public static final String URL = "dosv_url";
    public static final String USER = "dosv_user";
    public static final String PW = "dosv_password";
    public static final String DEBUG = "debug";

    private Properties config;

    /**
     * Initialisiert die DoSV-Webservices für die DoSV-Client-Instanz.
     *
     * @param config
     *        Die Konfiguration als <code>java.util.Properties</code>. Mögliche Parameter:
     *        Siehe {@link #getConfig()}.
     * @throws IllegalArgumentException
     *         falls ein Parameter in <code>config</code> vom falschen Typ ist.
     * @see #getConfig()
     */
    public DosvClient(Properties config) {
        this.config = new Properties(config);

        // Format: "$Time $Level $Logger: $Message[\n$Error]\n"
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "%1$tH:M:S.L %4$s %3$s: %5$s%6$s%n");
//        LogManager.getLogManager().reset();
        ConsoleHandler handler = new ConsoleHandler();
        // handler lets all messages through; output verbosity is regulated via Logger.setLevel()
        handler.setLevel(Level.FINEST);
        Logger.getLogger(DosvClient.class.getPackage().getName()).addHandler(handler);

        if (this.config.getProperty("debug").equals("true")) {
            // limit debug logging to DosvClient
            logger.setLevel(Level.FINE);
        }

        // webservice configuration
        // TODO throw ConfigurationException when configuration is missing
        List<String> wsEndpointSuffixes = Arrays.asList("studiengaengeService", "benutzerService",
            "bewerbungenService", "bewerberauswahlService");
        this.dispatches = new HashMap<>();
        QName wsName = new QName("dosv");
        Service webService = Service.create(wsName);
        String wsUrl =  this.config.getProperty(URL);
        for (String wsEndpointSuffix : wsEndpointSuffixes) {
            QName portName = new QName(wsEndpointSuffix);
            webService.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, wsUrl + wsEndpointSuffix);
            Dispatch<Source> dispatch =
                webService.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            Map<String, Object> requestContext = dispatch.getRequestContext();
            requestContext.put(BindingProvider.USERNAME_PROPERTY, this.config.getProperty(USER));
            requestContext.put(BindingProvider.PASSWORD_PROPERTY, this.config.getProperty(PW));
            this.dispatches.put(wsEndpointSuffix, dispatch);
            if (logger.isLoggable(Level.FINE)) {
                @SuppressWarnings("rawtypes")
                // Typecast to Handler<SOAPMessageContext> not possible
                    List<Handler> handlerChain = dispatch.getBinding().getHandlerChain();
                handlerChain.add(new SoapLogger());
                dispatch.getBinding().setHandlerChain(handlerChain);
            }
        }

        this.context = new SafeString(renderTemplate("context",
            this.config.getProperty(UNIVERSITY_ID), this.config.getProperty(YEAR),
            this.config.getProperty(SEMESTER)));
    }

    // TODO validate input for SAF methods
    /*
     * Schnittstellen-Anwendungsfälle
     * ==================================================================================
     */

    /*
     * Studiengang-SAF 101/102
     * ----------------------------------------------------------------------------------
     */

    /**
     * Anlegen/Ändern von Studienangeboten - SAF 101
     *
     * @param studienangebotList
     * @return
     */
    public List<DosvData> anlegenAendernStudienangeboteDurchHS(List<DosvData> studienangebotList) {
        if (studienangebotList.isEmpty()) {
            return new ArrayList<>();
        }

        String studienangebotXml = "";
        for (DosvData sa : studienangebotList) {
            String name = (String) sa.get("nameDe");
            String startTime = this.xmlDateFormat.format(sa.get("anfangBewerbungsfrist"));
            String endTime = this.xmlDateFormat.format(sa.get("endeBewerbungsfrist"));
            studienangebotXml += renderTemplate("studienangebot", sa.get("status"), name,
                sa.get("beschreibungDe"), startTime, endTime, sa.get("urlHSBewerbungsportal"), name,
                sa.get("studienfachSchluessel"), name);
        }

        return getErgebnis(this.invoke("studiengaengeService",
            renderTemplate("studienangeboteRequest", this.context,
                new SafeString(studienangebotXml))));
    }

    // TODO SAF 102 (abrufenStudienangeboteDurchHS)

    /*
     * Benutzer-SAF 201-204
     * ----------------------------------------------------------------------------------
     */

    /**
     * Stammdatenabruf für einen Benutzer - SAF 201
     *
     * @param bewerberId Bewerber-ID as <code>String</code>.
     * @param ban Bewerber-Authentifizierungsnummer (BAN) as <code>String</code>.
     * @throws DosvException with code <code>UnbekannterBenutzerFehler</code> or
     * <code>AutorisierungsFehler</code> on failed authentification.
     * @return bewerber // TODO document
     */
    public DosvData abrufenStammdatenDurchHS(String bewerberId, String ban) {
        Node response;
        try {
            response = this.invoke("benutzerService", renderTemplate("stammdatenRequest",
                bewerberId, ban));
        } catch (SOAPFaultException soapEx) {
            Node fault = soapEx.getFault();
            String faultType =
                this.evaluate("./detail/*[local-name()='ServiceFehler']/@*[local-name()='type']",
                    fault);
            if (faultType.equals("UnbekannterBenutzerFehler")
                || faultType.equals("AutorisierungsFehler")) {
                throw new DosvException(faultType);
            } else {
                // unreachable
                throw soapEx;
            }
        }

        DosvData bewerber = new DosvData(
            "status", this.evaluateLn("status", response),
            "anrede", this.evaluateLn("anrede", response),
            "vorname", this.evaluateLn("vorname", response),
            "weitereVornamen", this.evaluateLn("weitereVornamen", response),
            "nachname", this.evaluateLn("nachname", response),
            "emailAdresse", this.evaluateLn("emailAdresse", response),
            "telefonnummer", this.evaluateLn("telefonnummer", response),
            "mobilnummer", this.evaluateLn("mobilnummer", response),
            "id", this.evaluateLn("id", response),
            "hatVerfahrensrichtlinienZugestimmt",
            Boolean.valueOf(this.evaluateLn("hatVerfahrensrichtlinienZugestimmt", response)),
            "geburtsname", this.evaluateLn("geburtsname", response),
            "geburtsdatum", this.evaluateLn("geburtsdatum", response),
            "geburtsort", this.evaluateLn("geburtsort", response),
            "staatsangehoerigkeitSchluessel",
            this.evaluate("//*[local-name()='staatsangehoerigkeit']/*[local-name()='schluessel']",
                response),
            "staatsangehoerigkeitNameDe",
            this.evaluate("//*[local-name()='staatsangehoerigkeit']/*[local-name()='nameDe']",
                response),
            "careOf", this.evaluateLn("careOf", response),
            "strasseHausNrOderPostfach",
            this.evaluateLn("strasseHausNrOderPostfach", response),
            "plz", this.evaluateLn("plz", response),
            "ort", this.evaluateLn("ort", response),
            "landSchluessel",
            this.evaluate("//*[local-name()='land']/*[local-name()='schluessel']", response),
            "landNameDE",
            this.evaluate("//*[local-name()='land']/*[local-name()='nameDe']", response),
            "region", this.evaluateLn("region", response));
        
        return bewerber;
    }

    // TODO SAF 202 (anfragenStammdatenaenderungenDurchHS)
    // TODO SAF 204 (AbrufenStammdatenDurchHSohneAutorisierung)

    /*
     * Bewerbungen-SAF 301-304
     * ----------------------------------------------------------------------------------
     */

    /**
     * Initiale Übertragung neuer Bewerbungen an SeSt - SAF 301
     *
     * @param bewerbungList Liste der zu Übertragenden Bewerbungen
     * @return Liste von Ergebnissen
     */
    public List<DosvData> uebermittelnNeueBewerbungenAnSeSt(List<DosvData> bewerbungList) {
        if (bewerbungList.isEmpty()) {
            return new ArrayList<>();
        }

       return getErgebnis(this.invoke("bewerbungenService",
           renderTemplate("neueBewerbungenAnSeStRequest", this.context,
               new SafeString(generateBewerbungListXml(bewerbungList)))));
    }

    /**
     * Übermittelt geänderte Bewerbungen an die SeSt - SAF 302
     *
     * @param bewerbungList Liste von Bewerbung-Objekten
     * @return Liste von BewerbungErgebnis-Objekten
     */
    public List<DosvData> uebermittelnGeaenderteBewerbungenAnSeSt(List<DosvData> bewerbungList) {
        if (bewerbungList.isEmpty()) {
            return new ArrayList<>();
        }

        return getErgebnis(this.invoke("bewerbungenService",
            renderTemplate("geaenderteBewerbungenAnSeStRequest", this.context,
                new SafeString(generateBewerbungListXml(bewerbungList)))));
    }

    private String generateBewerbungListXml(List<DosvData> bewerbungList) {
        // TODO Mehrfachstudienangebotsbewerbung
        String bewerbungListXml = "";
        for (DosvData bew : bewerbungList) {
            String bewerbungXml = renderTemplate("bewerbung",
                this.xmlDateFormat.format(bew.get("eingangsZeitpunkt")),
                bew.get("bearbeitungsstatus"), bew.get("versionSeSt"), bew.get("bewerberBAN"),
                bew.get("bewerberEmailAdresse"), bew.get("bewerberId"),
                bew.get("studienfachSchluessel"));
            bewerbungListXml += bewerbungXml;
        }
        return bewerbungListXml;
    }

    /**
     * Zeitscheiben-basierte Anfrage nach neuen/geänderten Bewerbungen für die eigene HS -
     * SAF 303 Anfrage Benutzung:
     *
     * <pre>
     * Date[] updateTimeOld = {null};
     * List&lt;String&gt; referenzen = client.anfragenNeueGeaenderteBewerbungenDurchHS(updateTimeOld);
     * Date updateTimeNew = updateTimeOld[0];
     * </pre>
     *
     * @param startEnde
     *        Date-Objekt, dass in einem Array an Position [0] eingepackt (boxed) ist.
     *        Darf <code>null</code> sein. Wird als Output-Parameter für die aktualisierte
     *        Zeit des letzten Updates genutzt.
     * @return Liste von Bewerbungsreferenzen
     */
    public List<String> anfragenNeueGeaenderteBewerbungenDurchHS(Date[] startEnde) {
        SafeString startXml = new SafeString(this.xmlDateFormat.format(startEnde[0]));
        Node response = this.invoke("bewerbungenService",
            renderTemplate("anfragenBewerbungenRequest", this.context, startXml));
        List<String> referenzList = new ArrayList<>();
        NodeList nodes = this.evaluateForNodes("//*[local-name()='referenz']", response);
        for (int i = 0; i < nodes.getLength(); i++) {
            referenzList.add(nodes.item(i).getTextContent());
        }
        try {
            startEnde[1] = this.xmlDateFormat.parse(this.evaluateLn("ende", response));
        } catch (ParseException e) {
            // unreachable
            throw new RuntimeException(e);
        }

        return referenzList;
    }

    /**
     * Zeitscheiben-basierte Übermittlung neuer/geänderter Bewerbungen für die eigene HS -
     * SAF 303 Übermittlung
     *
     * @param referenzList Liste der Bewerbungsreferenzen aus SAF 303 Anfrage
     * @return Liste der Bewerbungen
     */
    public List<DosvData> uebermittelnNeueGeaenderteBewerbungenAnHS(List<String> referenzList) {
        if (referenzList.isEmpty()) {
            return new ArrayList<>();
        }

        String referenzXml = "";
        for (String referenz : referenzList) {
            referenzXml += renderTemplate("referenz", new SafeString(referenz));
        }
        Node response = this.invoke("bewerbungenService", renderTemplate("bewerbungenAnHSRequest",
            this.context, new SafeString(referenzXml)));
        List<DosvData> bewerbungList = new ArrayList<>();
        NodeList nodes = this.evaluateForNodes("//*[local-name()='bewerbung']", response);
        for (int i = 0; i < nodes.getLength(); i++) {
            String bewerbungType =
                this.evaluate("/@*[local-name()='type']", nodes.item(i));
            if (!bewerbungType.equals("Einfachstudienangebotsbewerbung")) {
                throw new UnsupportedOperationException("Mehrfachstudienangebotsbewerbung ist not supported by DosvClient");
                // TODO Mehrfachstudienangebotsbewerbung
            }
            Date eingangsZeitpunkt;
            Date letzteAenderung;
            try {
                eingangsZeitpunkt =
                    this.xmlDateFormat.parse(this.evaluateLn("eingangsZeitpunkt", response));
                letzteAenderung =
                    this.xmlDateFormat.parse(this.evaluateLn("letzteAenderung", response));
            } catch (ParseException e) {
                // unreachable
                throw new RuntimeException(e);
            }
            DosvData bewerbung = new DosvData(
                "ortDesEingangs", this.evaluateLn("ortDesEingangs", response),
                "eingangsZeitpunkt", eingangsZeitpunkt,
                "bearbeitungsstatus", this.evaluateLn("bearbeitungsstatus", response),
                "aktivitaetsstatus", this.evaluateLn("aktivitaetsstatus", response),
                "letzteAenderung", letzteAenderung,
                "statusanmerkung", this.evaluateLn("statusanmerkung", response),
                "sonderantragsentscheidung", this.evaluateLn("sonderantragsentscheidung", response),
                "versionSeSt", Integer.valueOf(this.evaluateLn("versionSeSt", response)),
                "bewerberId", this.evaluateLn("bewerberId", response),
                // TODO bestandteil, bescheid
                "einfachstudienangebotAbschlussSchluessel",
                this.evaluateLn("abschlussSchluessel", response),
                "einfachstudienangebotStudienfachSchluessel",
                this.evaluateLn("studienfachSchluessel", response)
            );
            bewerbungList.add(bewerbung);
        }

        return bewerbungList;
    }

    // TODO SAF 304 (abrufenBewerbungenDurchHS)

    /*
     * Bewerberauswahl-SAF 401-406
     * ----------------------------------------------------------------------------------
     */

    /**
     * Studienpakete anlegen/ändern durch HS - SAF 401
     *
     * @param studienpaketList
     * @return Liste von StudienpaketErgebnis
     */
    public List<DosvData> anlegenAendernStudienpaketeDurchHS(List<DosvData> studienpaketList) {
        if (studienpaketList.isEmpty()) {
            return new ArrayList<>();
        }

        String studienpaketXml = "";
        for (DosvData sp : studienpaketList) {
            studienpaketXml += renderTemplate("studienpaket", sp.get("schluessel"),
                sp.get("nameDe"), sp.get("kapazitaet"), sp.get("studienfachSchluessel"));
        }

        return getErgebnis(this.invoke("bewerberauswahlService",
            renderTemplate("studienpaketeRequest", this.context, new SafeString(studienpaketXml))));
    }

    /**
     * Übermitteln der Ranglisten an SeSt - SAF 402
     *
     * @param ranglisteList
     * @return Ergebnis Liste
     */
    public List<DosvData> uebermittelnRanglistenAnSeSt(List<DosvData> ranglisteList) {
        if (ranglisteList.isEmpty()) {
            return new ArrayList<>();
        }

        String ranglisteListXml = "";
        for (DosvData rl : ranglisteList) {
            String eintragListXml = "";
            for (DosvData eintrag : (List<DosvData>) rl.get("eintragList")) {
                eintragListXml +=
                    renderTemplate("eintrag",((int) eintrag.get("rang")) + 1,
                        eintrag.get("bewerberId"));
            }
            ranglisteListXml += renderTemplate("rangliste", rl.get("schluessel"),
                rl.get("studienpaketSchluessel"), rl.get("nameDe"),
                rl.get("plaetzeProzentual"), new SafeString(eintragListXml));
        }

        return getErgebnis(this.invoke("bewerberauswahlService", renderTemplate("ranglistenRequest",
            this.context, new SafeString(ranglisteListXml))));
    }

    // TODO SAF 403 (uebermittelnGeaenderteRanglistenstatusAnHS)
    // TODO SAF 404 (Ranglistenstatus durch HS abrufen)
    // TODO SAF 405 (Frühzeitige Zulassungsangebote durch HS auslösen)
    // TODO SAF 406 (Studienpakete durch HS abrufen)

    /*
     * Getter und Setter
     * ----------------------------------------------------------------------------------
     */

    /**
     * Konfiguration. Folgende Einstellungen können gesetzt sein:
     * <ul>
     * <li><code>dosv_url</code>: URL der Webservices. Der Standardwert ist
     * <code>https://hsst.hochschulstart.de/hochschule/webservice/2/</code>
     * (Testumgebung).
     * <li><code>university_id</code>: Hochschulnummer nach dem Statistischen Bundesamt.
     * <li><code>dosv_user</code>: Benutzername für die Webservices. Der Standardwert ist
     * "".
     * <li><code>dosv_password</code>: Passwort für die Webservices. Der Standardwert ist
     * "".
     * <li><code>dosv_year</code>: Jahr im Format <code>yyyy</code>. Der Standardwert ist
     * <code>2014</code> (Permanente Bewerbungsphase).
     * <li><code>dosv_semester</code>: Semester des Serviceverfahrens. Entweder
     * <code>WS</code> oder <code>SS</code>. Der Standardwert ist <code>WS</code>
     * (Permanente Bewerbungsphase).
     * <code>Koordinierung</code> oder <code>Clearing</code>. Der Standardwert ist
     * <code>Koordinierung</code>.
     * </ul>
     */
    public Properties getConfig() {
        return config;
    }

    /*
     * Hilfsmethoden und -klassen
     * ----------------------------------------------------------------------------------
     */

    private static String renderTemplate(String name, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                // escape XML
                args[i] = ((String) args[i]).replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("&", "&amp;").replace("'", "&apos;");
            } else if (args[i] instanceof SafeString) {
                args[i] = ((SafeString) args[i]).value;
            }
        }
        return String.format(Templates.get(name), args);
    }

    private Node invoke(String dispatch, String xml) {
        Source request = new StreamSource(new StringReader(xml));
        Source source = this.dispatches.get(dispatch).invoke(request);

        Transformer transformer;
        DOMResult result = new DOMResult();
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, result);
        } catch (TransformerException | TransformerFactoryConfigurationError e) {
            // unreachable
            throw new RuntimeException(e);
        }

        return result.getNode();
    }

    private List<DosvData> getErgebnis(Node response) {
        NodeList nodes = this.evaluateForNodes("//*[local-name()='ergebnisStatus']", response);
        List<DosvData> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(new DosvData(
                "ergebnisStatus", this.evaluateLn("ergebnisStatus", nodes.item(i)),
                "grundZurueckweisung", this.evaluateLn("grundZurueckweisung", nodes.item(i)),
                "bewerberId", this.evaluateLn("bewerberId", nodes.item(i)),
                "studienfachSchluessel", this.evaluateLn("studienfachSchluessel", nodes.item(i)),
                "studienpaketSchluessel", this.evaluateLn("studienpaketSchluessel", nodes.item(i))
            ));
        }
        return result;
    }

    private String evaluate(String xPathExpression, Node node) {
        try {
            return this.xPath.evaluate(xPathExpression, node);
        } catch (XPathExpressionException e) {
            // unreachable
            throw new RuntimeException(e);
        }
    }

    private String evaluateLn(String localName, Node node) {
        try {
            String xPathExpression = String.format("//*[local-name()='%s']", localName);
            return this.xPath.evaluate(xPathExpression, node);
        } catch (XPathExpressionException e) {
            // unreachable
            throw new RuntimeException(e);
        }
    }

    private NodeList evaluateForNodes(String xPathExpression, Node node) {
        try {
            return (NodeList) this.xPath.evaluate(xPathExpression, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // unreachable
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrapper for Strings that contain XML and may not be escaped.
     */
    private class SafeString  {
        private String value;

        public SafeString(String value) {
            this.value = value;
        }
    }

    /**
     * Logger for SOAP-Messages. Connects to the webservice <code>Handler</code>-chain and logs the
     * message XML.
     */
    private class SoapLogger implements SOAPHandler<SOAPMessageContext> {
        @Override
        public boolean handleMessage(SOAPMessageContext context) {
            Document xml = context.getMessage().getSOAPPart();
            Transformer transformer;
            try {
                transformer = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
                // unreachable
                throw new RuntimeException(e);
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            Writer out = new StringWriter();
            try {
                transformer.transform(new DOMSource(xml), new StreamResult(out));
            } catch (TransformerException e) {
                // unreachable
                throw new RuntimeException(e);
            }
            logger.fine(String.format("\n%s", out.toString()));

            return true;
        }

        @Override
        public boolean handleFault(SOAPMessageContext context) {
            return this.handleMessage(context);
        }

        @Override
        public Set<QName> getHeaders() {
            return null;
        }

        @Override
        public void close(MessageContext context) { }
    }
}
