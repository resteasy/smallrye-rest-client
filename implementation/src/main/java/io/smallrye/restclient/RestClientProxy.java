/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.restclient;

import javax.ws.rs.client.Client;

/**
 * This interface is implemented by every proxy created by {@link RestClientBuilderImpl}.
 *
 * @author Martin Kouba
 */
public interface RestClientProxy {

    /**
     * Release/close all associated resources, including the underlying {@link Client} instance.
     */
    void close();

    /**
     *
     * @return the underlying {@link Client} instance
     */
    Client getClient();

}
