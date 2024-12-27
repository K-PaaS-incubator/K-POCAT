/*
 * Copyright 2024. dongobi soft inc.
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

package io.pocat.service;

/**
 * Defines methods that all services must implement.
 * A service is a part of microservice architecture that runs within a PoCAT service node.
 * This interface defines methods to control service life cycle and to serve the business logic
 *
 * Service life-cycle methods are called in the following sequence:
 * <ol>
 *     <li>After service constructed, then initialized with the <code>init</code>method.</li>
 *     <li>After initialized, any message from binding channels to the <code>serve</code> method are handled.</li>
 *     <li>The service is taken out of service, then service will be destroyed with the <code>destroy</code> method.</li>
 * </ol>
 *
 */
public interface Service {
    /**
     * Called after service is constructed and initialize service
     * @param serviceConfig a configuration of this service
     * @throws InitializeFailedException if an exception occurred that service cannot be initialized.
     */
    void init(ServiceConfig serviceConfig) throws InitializeFailedException;

    /**
     * Method that contains service business logic.
     * Method cannot be called before <code>init</code> method is finished.
     * @param messageExchange message from any of binding channels
     * @throws ServiceException if an exception occurred that service cannot be proceeded.
     */
    void serve(MessageExchange messageExchange) throws ServiceException;

    /**
     * Before garbage collected this service, gives an opportunity
     * to clean up any resources that are being held.
     */
    void destroy();
}
