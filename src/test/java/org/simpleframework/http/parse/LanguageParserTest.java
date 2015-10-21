package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class LanguageParserTest {

    @Test
    public void testLanguages() throws Exception {
        final LanguageParser parser = new LanguageParser();

        parser.parse("en-gb,en;q=0.5");

        AssertJUnit.assertEquals(parser.list().get(0).getLanguage(), "en");
        AssertJUnit.assertEquals(parser.list().get(0).getCountry(), "GB");
        AssertJUnit.assertEquals(parser.list().get(1).getLanguage(), "en");
        AssertJUnit.assertEquals(parser.list().get(1).getCountry(), "");

        parser.parse("en-gb,en;q=0.5,*;q=0.9");

        AssertJUnit.assertEquals(parser.list().get(0).getLanguage(), "en");
        AssertJUnit.assertEquals(parser.list().get(0).getCountry(), "GB");
        AssertJUnit.assertEquals(parser.list().get(1).getLanguage(), "*");
        AssertJUnit.assertEquals(parser.list().get(2).getLanguage(), "en");
        AssertJUnit.assertEquals(parser.list().get(2).getCountry(), "");
    }

}
