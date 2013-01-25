/*******************************************************************************
 * Copyright (c) 2013, Salesforce.com, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     Neither the name of Salesforce.com nor the names of its contributors may 
 *     be used to endorse or promote products derived from this software without 
 *     specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package phoenix.query;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.StringReader;
import java.sql.*;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import phoenix.jdbc.PhoenixConnection;
import phoenix.util.CSVUtil;
import phoenix.util.PhoenixRuntime;

public class CSVUpsertTest extends BaseHBaseManagedTimeTest {
	
	private static final String TABLE = "STOCK_SYMBOL";
	private static final String CSV_FILE = "../../../phoenix/main/examples/stock.csv";
    
    @Test
    public void testCSVUpsert() throws Exception {
    	// Create table
        String statements = "CREATE TABLE IF NOT EXISTS " + TABLE + "(SYMBOL VARCHAR NOT NULL) CF (COMPANY VARCHAR);";
        PhoenixConnection conn = DriverManager.getConnection(getUrl()).unwrap(PhoenixConnection.class);
        PhoenixRuntime.executeStatements(conn, new StringReader(statements), null);
        
        // Upsert CSV file
        CSVUtil csvUtil = new CSVUtil(conn, TABLE); 
        csvUtil.upsert(CSV_FILE);

        // Compare Phoenix ResultSet with CSV file content
        PreparedStatement statement = conn.prepareStatement("SELECT SYMBOL, COMPANY FROM " + TABLE);
        ResultSet phoenixResultSet = statement.executeQuery();
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE));
        reader.readNext();
        String[] csvData;
        while ((csvData = reader.readNext()) != null) {
        	assertTrue (phoenixResultSet.next());
        	for (int i=0; i<csvData.length; i++) {
        		assertEquals(csvData[i], phoenixResultSet.getString(i+1));
        	}
        }
        
        assertFalse(phoenixResultSet.next());
        conn.close();
    }
}