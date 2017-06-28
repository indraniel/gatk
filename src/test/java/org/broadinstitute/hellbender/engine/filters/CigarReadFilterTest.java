package org.broadinstitute.hellbender.engine.filters;

import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.utils.test.ReadClipperTestUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class CigarReadFilterTest {

    @DataProvider
    public Object[][] createGatkReadsForFilterTest() {
        return new Object[][] {
                { ReadClipperTestUtils.makeReadFromCigar("D") }
        };
    }

    @DataProvider
    public Object[][] createInvalidCigarFilterStrings() {
        return new Object[][]{
                {"^", false},
                {"$", false},
                {"SHS", false},
                {"SHSH", false},
                {"329", false},
                {"M%", false},
                {"<=P", false},
                {"10P<>34X", false},
                {"10P<>=34X", false},
                {"12H<3SH10P>=34X3S2H", false},
        };
    }

    @DataProvider
    public Object[][] createValidCigarFilterStrings() {
        return new Object[][]{
                {"*", true},
                {"H", true},
                {"HS", true},
                {"HHHHSSSSDSSSSHHH", true},
                {"SS==3I<=24DMMMDMDMDDSHHHH", true},
                {"SH", true},
                {"=H", true},
                {"105H", true},
                {"329H3S", true},
                {"^M$", true},
                {"M", true},
                {"M*", true},
                {"*M", true},
                {"10P", true},
                {"=10P", true},
                {"<=9=", true},
                {"10P=34X", true},
                {"10P<34X", true},
                {"10P<=34X", true},
                {"10P>34X", true},
                {"10P>=34X", true},
                {"10P>=34X3S2H", true},
                {"12H<3S10P>=34X3S2H", true},
                {"^12H<3S10P>=34X3S2H$", true},
        };
    }

    @DataProvider
    public Object[][] createExhaustiveValidCigarList() {

        ArrayList<String> masterList = new ArrayList<>();

        for (int i = 0; i < 10; ++i) {
            ReadClipperTestUtils.generateCigarList(i).stream().forEach(c -> masterList.add(c.toString()));
        }

        Object[][] exhaustiveValues = { masterList.toArray() };

        return exhaustiveValues;
    }

    @DataProvider
    public Object[][] createCigarFilterStrings() {

        return Stream.concat(   Arrays.stream( createValidCigarFilterStrings() ),
                                Arrays.stream( createInvalidCigarFilterStrings() )
                            ).toArray(Object[][]::new);

    }

    @DataProvider
    public Object[][] createCigarMatchElementStrings() {

//        "(\\^?(?:[<>]|[<>]=)?\\d*[SHMIDNPX=*]\\$?)"
        return new Object[][] {
                {"^", false},
                {"$", false},
                {"M$", true},
                {"^*$", true},
                {"107X$", true},
                {"=19X", false},
                {"<=19I", true},
                {">=19M", true},
                {">19D", true},
                {"<19N", true},
        };
    }

    @Test(dataProvider="createCigarFilterStrings")
    public void testValidate(String cigarFilterString, boolean isValid) {
        Assert.assertEquals( CigarReadFilter.validate(cigarFilterString), isValid );
    }

    @Test(dataProvider="createExhaustiveValidCigarList")
    public void testValidateExhaustivly(String cigarFilterString) {
        Assert.assertEquals( CigarReadFilter.validate(cigarFilterString), true );
    }

    @Test(dataProvider="createCigarMatchElementStrings")
    public void testNextCigarMatchElementPattern(String cigarFilterString, boolean isValid) {
        Assert.assertEquals( CigarReadFilter.nextCigarMatchElementPattern.matcher(cigarFilterString).matches(), isValid );
    }

    @Test(dataProvider="createValidCigarFilterStrings")
    public void testSetDescriptionValid(String cigarFilterString, boolean isValid) {
        CigarReadFilter crf = new CigarReadFilter();

        // Set the CigarReadFilter to have the given description:
        crf.setDescription(cigarFilterString);

        // Verify that the CigarReadDescription is the same as that which was given:
        Assert.assertEquals(crf.getDescription(), cigarFilterString);
    }

    @Test(dataProvider="createInvalidCigarFilterStrings", expectedExceptions=UserException.BadInput.class)
    public void testSetDescriptionInvalid(String cigarFilterString, boolean isValid) {
        CigarReadFilter crf = new CigarReadFilter();

        // Set the CigarReadFilter to have the given description:
        // NOTE: This should throw an exception each time
        crf.setDescription(cigarFilterString);
    }

    @Test(dataProvider="createValidCigarFilterStrings")
    public void testConstructorValidDescription(String cigarFilterString, boolean isValid) {
        CigarReadFilter crf = new CigarReadFilter(cigarFilterString);

        // Verify that the CigarReadDescription is the same as that which was given:
        Assert.assertEquals(crf.getDescription(), cigarFilterString);
    }

    @Test(dataProvider="createInvalidCigarFilterStrings", expectedExceptions=UserException.BadInput.class)
    public void testConstructorInvalidDescription(String cigarFilterString, boolean isValid) {
        CigarReadFilter crf = new CigarReadFilter(cigarFilterString);
    }


//    @Test
//    public void testCreateMatchElementFromMatchString() {
//        //FIXME
//        throw new RuntimeException("MUST BE IMPLEMENTED");
//    }

//    @Test
//    public void testReadFilterTestMethod() {
//        //FIXME
//        throw new RuntimeException("MUST BE IMPLEMENTED");
//    }

}