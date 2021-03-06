/**
 * Copyright 2019 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.restclient.header;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * A storage of {@link ClientHeaderProvider}s
 *
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 1/12/19
 */
public class ClientHeaderProviders {

    private static final ClientHeadersFactory defaultHeadersFactory = new DefaultClientHeadersFactoryImpl();

    private static Map<Method, ClientHeaderProvider> providersForMethod = new ConcurrentHashMap<>();
    private static Map<Class<?>, ClientHeadersFactory> headerFactoriesForClass = new ConcurrentHashMap<>();

    /**
     * Get {@link ClientHeaderProvider} for a given method, if exists
     * @param method a method to get the provider for
     * @return the provider responsible for setting the headers
     */
    public static Optional<ClientHeaderProvider> getProvider(Method method) {
        return Optional.ofNullable(providersForMethod.get(method));
    }

    /**
     * Get {@link ClientHeadersFactory} for a given class, if exists
     * @param aClass a class to get the ClientHeadersFactory for
     * @return the factory used to adjust the headers
     */
    public static Optional<ClientHeadersFactory> getFactory(Class<?> aClass) {
        return Optional.ofNullable(headerFactoriesForClass.get(aClass));
    }

    /**
     * Register, in a static map, {@link ClientHeaderProvider}`s for the given class and all of its methods
     *
     * @param clientClass a class to scan for {@link org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam} and {@link RegisterClientHeaders}
     * @param clientProxy proxy of the clientClass, used to handle the default methods
     */
    public static void registerForClass(Class<?> clientClass, Object clientProxy) {
        Stream.of(clientClass.getMethods())
                .forEach(m -> registerForMethod(m, clientProxy));
        registerHeaderFactory(clientClass);
    }

    private static void registerHeaderFactory(Class<?> aClass) {
        RegisterClientHeaders annotation = aClass.getAnnotation(RegisterClientHeaders.class);
        if (annotation != null) {
            Optional<ClientHeadersFactory> clientHeadersFactory = getCustomHeadersFactory(annotation, aClass);

            headerFactoriesForClass.put(aClass, clientHeadersFactory.orElse(defaultHeadersFactory));
        }
    }

    private static Optional<ClientHeadersFactory> getCustomHeadersFactory(RegisterClientHeaders annotation, Class<?> source) {
        Class<? extends ClientHeadersFactory> factoryClass = annotation.value();
        if (factoryClass != null) {
            try {
                return Optional.of(factoryClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RestClientDefinitionException(
                        "Failed to instantiate " + factoryClass.getCanonicalName() + ", the client header factory for " + source.getCanonicalName(),
                        e
                );
            }
        } else {
            return Optional.empty();
        }
    }

    private static void registerForMethod(Method method, Object clientProxy) {
        ClientHeaderProvider.forMethod(method, clientProxy).ifPresent(
                provider -> providersForMethod.put(method, provider)
        );
    }

    private ClientHeaderProviders() {
    }
}
