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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.*;

public class AccessLogger {
    private static final Formatter ACCESS_LOG_FORMATTER = new AccessLogFormatter();
    private final Logger logger;
    public AccessLogger() {
        this.logger = initLogger();
    }

    private Logger initLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        Handler[] handlers = logger.getHandlers();
        for(Handler handler:handlers) {
            logger.removeHandler(handler);
        }
        return logger;
    }

    public void addHandler(Handler handler) {
        handler.setLevel(Level.ALL);
        handler.setFormatter(ACCESS_LOG_FORMATTER);
        this.logger.addHandler(handler);
    }

    public void log(AccessLogRecord record) {
        this.logger.log(Level.ALL, record.toString());
    }

    public static class AccessLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(
                    record.getInstant(), ZoneId.systemDefault());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return zdt.toString() + " " + record.getMessage();
        }
    }

    private class AccessLogRecord {
    }
}
