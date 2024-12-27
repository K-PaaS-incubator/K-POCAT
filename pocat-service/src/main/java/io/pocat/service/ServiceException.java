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
 * Define a general exception that service can throw when it encounters difficulty
 */
public class ServiceException extends Exception {
    /**
     * Error code of encountered problem
     */
    private final int errorCode;

    /**
     * Constructs a new service exception
     * @param errorCode error code of encountered problem
     * @param message error message
     */
    public ServiceException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new service exception with root cause exception
     * @param errorCode error code of encountered problem
     * @param message error message
     * @param cause root cause exception
     */
    public ServiceException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code of this service exception
     * @return the error code of this service exception
     */
    public int getErrorCode() {
        return errorCode;
    }
}
