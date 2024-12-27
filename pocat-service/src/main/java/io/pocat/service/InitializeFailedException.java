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
 * Define an exception that service can throw when its initialization process is failed.
 */
public class InitializeFailedException extends Exception {
    /**
     * Constructs a new InitializeFailedException
     * @param message error message
     */
    public InitializeFailedException(String message) {
        super(message);
    }
    /**
     * Constructs a new InitializeFailedException with root cause
     * @param message error message
     * @param cause root cause exception
     */
    public InitializeFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
