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

import java.util.HashMap;
import java.util.Map;

// TODO document
class Templates {
    private static final Map<String, String> XML_TEMPLATES = new HashMap<>();

    static {
        XML_TEMPLATES.put("context",
            "<version>2</version>"
            + "<hochschulnummer>%s</hochschulnummer>"
            + "<serviceverfahren>"
            + "<serviceverfahren:jahr>%s</serviceverfahren:jahr>"
            + "<serviceverfahren:semester>%s</serviceverfahren:semester>"
            + "</serviceverfahren>"
            + "<vermittlungsprozessTyp>Koordinierung</vermittlungsprozessTyp>");
        XML_TEMPLATES.put("stammdatenRequest",
            "<benutzerService:abrufenStammdatenDurchHSRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:benutzerService=\"http://BenutzerServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "<version>2</version>"
                + "<benutzerService:bewerberId>%s</benutzerService:bewerberId>"
                + "<benutzerService:BAN>%s</benutzerService:BAN>"
            + "</benutzerService:abrufenStammdatenDurchHSRequest>");
        XML_TEMPLATES.put("studienpaketeRequest",
            "<bewerberauswahlService:anlegenAendernStudienpaketeDurchHSRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerberauswahl=\"http://BewerberauswahlV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerberauswahlService=\"http://BewerberauswahlServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:studiengaenge=\"http://StudiengaengeV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s%s"
            + "</bewerberauswahlService:anlegenAendernStudienpaketeDurchHSRequest>");
        XML_TEMPLATES.put("studienpaket",
            "<bewerberauswahlService:studienpaket>"
                + "<bewerberauswahl:schluessel>%s</bewerberauswahl:schluessel>"
                + "<bewerberauswahl:nameDe>%s</bewerberauswahl:nameDe>"
                + "<bewerberauswahl:kapazitaet>%d</bewerberauswahl:kapazitaet>"
                + "<bewerberauswahl:paketbestandteil>"
                + "<bewerberauswahl:einfachstudienangebotsSchluessel>"
                    + "<studiengaenge:abschlussSchluessel>bachelor</studiengaenge:abschlussSchluessel>"
                    + "<studiengaenge:studienfachSchluessel>%s</studiengaenge:studienfachSchluessel>"
                + "</bewerberauswahl:einfachstudienangebotsSchluessel>"
                + "<bewerberauswahl:bewerberplatzbedarf>"
                    + "<bewerberauswahl:zaehler>1</bewerberauswahl:zaehler>"
                    + "<bewerberauswahl:nenner>1</bewerberauswahl:nenner>"
                + "</bewerberauswahl:bewerberplatzbedarf>"
                + "</bewerberauswahl:paketbestandteil>"
            + "</bewerberauswahlService:studienpaket>");
        XML_TEMPLATES.put("studienangeboteRequest",
            "<studiengaengeService:anlegenAendernStudienangeboteDurchHSRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:studiengaenge=\"http://StudiengaengeV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:studiengaengeService=\"http://StudiengaengeServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s%s"
            + "</studiengaengeService:anlegenAendernStudienangeboteDurchHSRequest>");
        XML_TEMPLATES.put("studienangebot",
            "<studiengaengeService:studienangebot xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"studiengaenge:Einfachstudienangebot\">"
            + "<studiengaenge:status>%s</studiengaenge:status>"
                + "<studiengaenge:nameDe>%s</studiengaenge:nameDe>"
                + "<studiengaenge:beschreibungDe>%s</studiengaenge:beschreibungDe>"
                + "<studiengaenge:koordinierungsangebotsdaten>"
                    + "<studiengaenge:anfangBewerbungsfrist>%s</studiengaenge:anfangBewerbungsfrist>"
                    + "<studiengaenge:endeBewerbungsfrist>%s</studiengaenge:endeBewerbungsfrist>"
                    + "<studiengaenge:urlHSBewerbungsportal>http://www.example.com</studiengaenge:urlHSBewerbungsportal>"
                + "</studiengaenge:koordinierungsangebotsdaten>"
                + "<studiengaenge:integrationseinstellungen>"
                    + "<studiengaenge:bewerbungsort>Hochschule</studiengaenge:bewerbungsort>"
                    + "<studiengaenge:rueckstellungsBescheidVersandart>Hochschule</studiengaenge:rueckstellungsBescheidVersandart>"
                    + "<studiengaenge:zulassungsBescheidVersandart>Hochschule</studiengaenge:zulassungsBescheidVersandart>"
                    + "<studiengaenge:hzbPruefungGewuenscht>false</studiengaenge:hzbPruefungGewuenscht>"
                + "</studiengaenge:integrationseinstellungen>"
                + "<studiengaenge:studiengang>"
                    + "<studiengaenge:nameDe>%s</studiengaenge:nameDe>"
                    + "<studiengaenge:istNCStudiengang>true</studiengaenge:istNCStudiengang>"
                    + "<studiengaenge:abschluss>"
                        + "<studiengaenge:schluessel>bachelor</studiengaenge:schluessel>"
                        + "<studiengaenge:nameDe>Bachelor</studiengaenge:nameDe>"
                    + "</studiengaenge:abschluss>"
                    + "<studiengaenge:studienfach>"
                        + "<studiengaenge:schluessel>%s</studiengaenge:schluessel>"
                        + "<studiengaenge:nameDe>%s</studiengaenge:nameDe>"
                    + "</studiengaenge:studienfach>"
                + "</studiengaenge:studiengang>"
            + "</studiengaengeService:studienangebot>");
        XML_TEMPLATES.put("anfragenBewerbungenRequest",
            "<bewerbungenService:anfragenNeueGeaenderteBewerbungenDurchHSRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerbungenService=\"http://BewerbungenServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s"
                + "<bewerbungenService:start>%s</bewerbungenService:start>"
            + "</bewerbungenService:anfragenNeueGeaenderteBewerbungenDurchHSRequest>");
        XML_TEMPLATES.put("bewerbungenAnHSRequest",
            "<bewerbungenService:uebermittelnNeueGeaenderteBewerbungenAnHSRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerbungenService=\"http://BewerbungenServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s%s"
            + "</bewerbungenService:uebermittelnNeueGeaenderteBewerbungenAnHSRequest>");
        XML_TEMPLATES.put("referenz",
            "<bewerbungenService:referenz>%s</bewerbungenService:referenz>");
        XML_TEMPLATES.put("neueBewerbungenAnSeStRequest",
            "<bewerbungenService:uebermittelnNeueBewerbungenAnSeStRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerbungen=\"http://BewerbungenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerbungenService=\"http://BewerbungenServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:studiengaenge=\"http://StudiengaengeV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s%s"
            + "</bewerbungenService:uebermittelnNeueBewerbungenAnSeStRequest>");
        XML_TEMPLATES.put("geaenderteBewerbungenAnSeStRequest",
            "<bewerbungenService:uebermittelnGeaenderteBewerbungenAnSeStRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerbungen=\"http://BewerbungenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerbungenService=\"http://BewerbungenServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:studiengaenge=\"http://StudiengaengeV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s%s"
                + "</bewerbungenService:uebermittelnGeaenderteBewerbungenAnSeStRequest>");
        XML_TEMPLATES.put("bewerbung",
            "<bewerbungenService:bewerbung xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"bewerbungen:Einfachstudienangebotsbewerbung\">"
                + "<bewerbungen:eingangsZeitpunkt>%s</bewerbungen:eingangsZeitpunkt>"
                + "<bewerbungen:bearbeitungsstatus>%s</bewerbungen:bearbeitungsstatus>"
                + "<bewerbungen:versionSeSt>%d</bewerbungen:versionSeSt>"
                + "<bewerbungen:bewerberBAN>%s</bewerbungen:bewerberBAN>"
                + "<bewerbungen:bewerberEmailAdresse>%s</bewerbungen:bewerberEmailAdresse>"
                + "<bewerbungen:bewerberId>%s</bewerbungen:bewerberId>"
                + "<bewerbungen:einfachstudienangebotsSchluessel>"
                    + "<studiengaenge:abschlussSchluessel>bachelor</studiengaenge:abschlussSchluessel>"
                    + "<studiengaenge:studienfachSchluessel>%s</studiengaenge:studienfachSchluessel>"
                + "</bewerbungen:einfachstudienangebotsSchluessel>"
            + "</bewerbungenService:bewerbung>");
        XML_TEMPLATES.put("ranglistenRequest",
            "<bewerberauswahlService:uebermittelnRanglistenAnSeStRequest xmlns=\"http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerberauswahl=\"http://BewerberauswahlV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:bewerberauswahlService=\"http://BewerberauswahlServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:serviceverfahren=\"http://ServiceverfahrenV1_0.HochschulSchnittstelle.hochschulstart.de\" xmlns:studiengaenge=\"http://StudiengaengeV1_0.HochschulSchnittstelle.hochschulstart.de\">"
                + "%s%s"
            + "</bewerberauswahlService:uebermittelnRanglistenAnSeStRequest>");
        XML_TEMPLATES.put("rangliste",
            "<bewerberauswahlService:rangliste>"
                + "<bewerberauswahl:schluessel>%s</bewerberauswahl:schluessel>"
                + "<bewerberauswahl:studienpaketSchluessel>%s</bewerberauswahl:studienpaketSchluessel>"
                + "<bewerberauswahl:nameDe>%s</bewerberauswahl:nameDe>"
                + "<bewerberauswahl:status>befuellt</bewerberauswahl:status>"
                + "<bewerberauswahl:abarbeitungsposition>1</bewerberauswahl:abarbeitungsposition>"
                + "<bewerberauswahl:plaetzeProzentual>%d</bewerberauswahl:plaetzeProzentual>"
                + "<bewerberauswahl:ebene>Hauptquoten</bewerberauswahl:ebene>"
                + "<bewerberauswahl:istVorwegzulasserrangliste>false</bewerberauswahl:istVorwegzulasserrangliste>"
                + "<bewerberauswahl:istChancenrangliste>false</bewerberauswahl:istChancenrangliste>"
                + "%s"
            + "</bewerberauswahlService:rangliste>");
        XML_TEMPLATES.put("eintrag",
            "<bewerberauswahl:eintrag>"
                + "<bewerberauswahl:rang>%d</bewerberauswahl:rang>"
                + "<bewerberauswahl:bewerberId>%s</bewerberauswahl:bewerberId>"
            + "</bewerberauswahl:eintrag>");
    }

    private Templates() { }

    static String get(String name) {
        return XML_TEMPLATES.get(name);
    }
}
