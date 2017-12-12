/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.swift;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Set;

/**
 * A collection of binaries associated with a component.
 *
 * <p>Each element in this collection passes through several states. The element is created and becomes 'known'. The element is passed to any actions registered using {@link #whenElementKnown(Action)}. The element is then configured using any actions registered using {@link #configureEach(Action)} and becomes 'finalized'. The element is passed to any actions registered using {@link #whenElementFinalized(Action)}. Elements are created and configured only when required.
 *
 * @since 4.5
 */
@Incubating
public interface SwiftBinaryContainer {
    /**
     * Returns a {@link Provider} that contains the single binary matching the specified type and specification. Querying the return value will fail when there is not exactly one such binary.
     *
     * @param type subtype to match
     * @param spec specification to satisfy
     * @param <T> type of the binary to return
     * @return a binary from the collection in a finalized state
     */
    <T extends SwiftBinary> Provider<T> get(Class<T> type, Spec<? super T> spec);

    /**
     * Returns a {@link Provider} that contains the single binary with the given name. Querying the return value will fail when there is not exactly one such binary.
     *
     * @param name The name of the binary
     * @return a binary from the collection in a finalized state
     */
    Provider<SwiftBinary> getByName(String name);

    /**
     * Returns a {@link Provider} that contains the single binary matching the given specification. Querying the return value will fail when there is not exactly one such binary.
     *
     * @param spec specification to satisfy
     * @return a binary from the collection in a finalized state
     */
    Provider<SwiftBinary> get(Spec<? super SwiftBinary> spec);

    /**
     * Registers an action to execute when an element becomes known.
     *
     * @param action The action to execute for each element becomes known.
     */
    void whenElementKnown(Action<? super SwiftBinary> action);

    /**
     * Registers an action to execute when an element is finalized.
     *
     * @param action The action to execute for each element when finalized.
     */
    void whenElementFinalized(Action<? super SwiftBinary> action);

    /**
     * Configures each elements in the collection.
     *
     * @param action The action to execute on each element for configuration.
     */
    void configureEach(Action<? super SwiftBinary> action);

    /**
     * Returns the set of binaries from the collection in a finalized state.
     */
    Set<SwiftBinary> get();
}
