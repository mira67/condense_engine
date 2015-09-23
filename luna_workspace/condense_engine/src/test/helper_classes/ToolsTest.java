package test.helper_classes;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import helper_classes.Tools;

import java.nio.ByteBuffer;

/**
 * Tools Tester.
 *
 * @author Richard McAllister
 * @version 1.0
 * @since <pre>Sep 16, 2015</pre>
 */
public class ToolsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: randomInt(int n)
     *
     * I suppose you'd have to run this a bunch of times in order to get a good idea that it's compliant.
     *      Do this if you wish ;)
     */
    @Test
    public void testRandomInt() throws Exception {

        int numLessThan = 50;
        int randomInt = Tools.randomInt(numLessThan);
        Assert.assertTrue(randomInt < numLessThan);
        Assert.assertTrue(randomInt >= 0);

    }

    /**
     * Method: inByteRange(int i)
     */
    @Test
    public void testInByteRange() throws Exception {

        int greaterThan = 256;
        int adjustedGreaterThan = Tools.inByteRange(greaterThan);
        Assert.assertEquals(adjustedGreaterThan, 255);

        int lessThan = -5;
        int adjustedLessThan = Tools.inByteRange(lessThan);
        Assert.assertEquals(adjustedLessThan, 0);

        int middle = 100;
        int adjustedMiddle = Tools.inByteRange(middle);
        Assert.assertEquals(adjustedMiddle, middle);

    }

    /**
     * Method: LSBtoMSB(byte[] inBytes)
     *
     * TODO: This needs to have some input testing.
     */
    @Test
    public void testLSBtoMSB() throws Exception {

        byte[] littleEndianByteArray = new byte[8];
        littleEndianByteArray[0] = 0b0001_1000;
        littleEndianByteArray[1] = 0b0110_0001;
        littleEndianByteArray[2] = 0b0011_1110;
        littleEndianByteArray[3] = 0b0101_0110;
        littleEndianByteArray[4] = 0b0000_1001;
        littleEndianByteArray[5] = 0b0000_1101;
        littleEndianByteArray[6] = 0b0000_0011;
        littleEndianByteArray[7] = 0b0111_1111;

        double msbDouble = Tools.LSBtoMSB(littleEndianByteArray);

//        // Leave this here, please.  It should print out 3.023557212554879E-191 when uncommented.
//        System.out.println(ByteBuffer.wrap(littleEndianByteArray).getDouble());

        // TODO: Pick a more appropriate delta for this.  I just pulled one out of a hat.
        Assert.assertEquals(msbDouble, 6.532233196936644E303, 0.000000001);

    }

    /**
     * Method: intToByteArray(int value)
     */
    @Test
    public void testIntToByteArray() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: intArrayToByteArray(int[] values)
     */
    @Test
    public void testIntArrayToByteArray() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: byteArrayToInt(byte[] bytes)
     */
    @Test
    public void testByteArrayToInt() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: unsignedByteToInt(byte b)
     */
    @Test
    public void testUnsignedByteToInt() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: unsignedByteToFloat(byte b)
     */
    @Test
    public void testUnsignedByteToFloat() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: intToUnsignedByte(int i)
     */
    @Test
    public void testIntToUnsignedByte() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: parseIntByte(int value, int byteNumber)
     */
    @Test
    public void testParseIntByte() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: insertByte(int newByte, int existingInt, int position)
     */
    @Test
    public void testInsertByte() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: uniqueFileName(String filename)
     */
    @Test
    public void testUniqueFileName() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: scaleIntArray2D(int[][] array, int min, int max)
     */
    @Test
    public void testScaleIntArray2D() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: discardBadData(int[][] array, int min, int max)
     */
    @Test
    public void testDiscardBadData() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: doubleArrayToInteger(double array[][])
     */
    @Test
    public void testDoubleArrayToInteger() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: doubleArrayToFloat(double array[][])
     */
    @Test
    public void testDoubleArrayToFloat() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: printMemory()
     */
    @Test
    public void testPrintMemory() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: printMemoryShort()
     */
    @Test
    public void testPrintMemoryShort() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: errorMessage(String className, String method, String message, Exception e)
     */
    @Test
    public void testErrorMessage() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: warningMessage(String message)
     */
    @Test
    public void testWarningMessage() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: message(String message)
     */
    @Test
    public void testMessage() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: setWarnings(boolean flag)
     */
    @Test
    public void testSetWarnings() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: warnings()
     */
    @Test
    public void testWarnings() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: debugMessage(String message)
     */
    @Test
    public void testDebugMessage() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: setDebug(boolean flag)
     */
    @Test
    public void testSetDebug() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: debug()
     */
    @Test
    public void testDebug() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: statusMessage(String message)
     */
    @Test
    public void testStatusMessage() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: setStatus(boolean flag)
     */
    @Test
    public void testSetStatus() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: status()
     */
    @Test
    public void testStatus() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: exit(int s)
     */
    @Test
    public void testExit() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: parseString(String line, int word, String separator)
     */
    @Test
    public void testParseString() throws Exception {

        Assert.assertEquals(true, false);

    }

    /**
     * Method: removeCharacters(String line, Character c)
     */
    @Test
    public void testRemoveCharacters() throws Exception {

        String beforeString = "All your base are belong to us!";
        String afterString = "A your base are beong to us!";
        Assert.assertEquals(Tools.removeCharacters(beforeString, 'l'), afterString);

    }


} 
