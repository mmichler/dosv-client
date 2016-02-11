/*
 * dibs
 * Copyright (C) 2015  Humboldt-Universität zu Berlin
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class DosvClientTest {

    private String bid;
    private String ban;
    private DosvClient client;

    @Before
    public void setUp() throws Exception {
        Properties config = new Properties();
        config.load(new FileInputStream("test.default.properties"));
        try {
            config.load(new FileInputStream("test.properties"));
        } catch (FileNotFoundException e) {
            // ignorieren
        }

        String url = config.getProperty("dosv_url", "");
        String user = config.getProperty("dosv_user", "");
        String password = config.getProperty("dosv_password", "");
        String year = config.getProperty("dosv_year", "");
        String semester = config.getProperty("dosv_semester", "");
        this.bid = config.getProperty("dosv_test_bid", "");
        this.ban = config.getProperty("dosv_test_ban", "");

        assumeThat("Webservices not configured, skipping test cases", this.bid.isEmpty()
            || this.ban.isEmpty() || url.isEmpty() || user.isEmpty() || password.isEmpty()
            || year.isEmpty() || semester.isEmpty(), is(false));

        this.client = new DosvClient(config);
    }

    @Test
    public void testAbrufenStammdatenDurchHS() {
        client.abrufenStammdatenDurchHS(this.bid, this.ban);
    }

    @Test
    public void testAnlegenAendernStudienangeboteDurchHS() {
        Date date = new Date();

        DosvData studienangebot = new DosvData(
            "status", "in_Vorbereitung",
            "nameDe", "DosvClient Test-Studiengang",
            "beschreibungDe", "DosvClient Test-Studiengang",
            "anfangBewerbungsfrist", date,
            "endeBewerbungsfrist", new Date(date.getTime() + 100),
            "urlHSBewerbungsportal", "www.example.com",
            "studienfachSchluessel", String.valueOf(new Random().nextInt())
        );

        this.client.anlegenAendernStudienangeboteDurchHS(Collections.singletonList(studienangebot));
    }
}
