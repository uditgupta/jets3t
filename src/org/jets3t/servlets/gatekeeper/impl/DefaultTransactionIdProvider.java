/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2008 James Murty
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.jets3t.servlets.gatekeeper.impl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.jets3t.service.utils.gatekeeper.GatekeeperMessage;
import org.jets3t.servlets.gatekeeper.ClientInformation;
import org.jets3t.servlets.gatekeeper.TransactionIdProvider;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Default TransactionIdProvider implementation that generated random-based UUIDs using the
 * <a href="http://jug.safehaus.org/Home">Java Uuid Generator</a>. 
 *  
 * @author James Murty
 */
public class DefaultTransactionIdProvider extends TransactionIdProvider {

    /**
     * Constructs the TransactionIdProvider - no configuration parameters are required. 
     * 
     * @param servletConfig
     * @throws ServletException
     */
    public DefaultTransactionIdProvider(ServletConfig servletConfig) throws ServletException {
        super(servletConfig);
    }

    /**
     * Returns a random-based UUID.
     */
    public String getTransactionId(GatekeeperMessage requestMessage, ClientInformation clientInformation)
    {
        // Generate a UUID based on a random generation.
        UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
        return uuid.toString();
    }

}
