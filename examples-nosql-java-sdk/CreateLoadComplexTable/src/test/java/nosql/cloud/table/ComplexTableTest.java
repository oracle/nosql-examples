/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package nosql.cloud.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nosql.cloud.table.config.Config;
import nosql.cloud.table.config.Configuration;

/**
 * Test template for the CreateLoadComplexTable example class.
 */
public class ComplexTableTest extends ComplexTableTestBase {

    private static final Class THIS_CLASS = ComplexTableTest.class;
    private static final String THIS_CLASS_FQN = THIS_CLASS.getName();
    private static final Logger logger = Logger.getLogger(THIS_CLASS_FQN);
    
    @Before
    public void setUp() {
        logger.finest("--- setUp ---");
    }

    @After
    public void teardown() {
        logger.finest("--- tearDown ---");
    }

    @Test
    public void testStub() throws IOException {

        /*
         * Example of the form test methods should take.
         *
         * Verify that if ... .
         */
        logger.finest("--- testStub ---");

    }
}
