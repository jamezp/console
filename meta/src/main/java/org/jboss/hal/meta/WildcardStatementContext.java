/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.meta;

import org.jboss.hal.config.Environment;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_KEY;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_GROUP;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_PROFILE;

/**
 * A statement context which resolves
 * <ul>
 * <li>{@code selected.profile}</li>
 * <li>{@code selected.group} and</li>
 * <li>{@code selection}</li>
 * </ul>
 * to "*".
 * <p>
 * Used by registries such as {@link MetadataRegistry} which need generic templates.
 *
 * @author Harald Pehl
 */
public class WildcardStatementContext extends FilteringStatementContext
        implements StatementContext {

    public WildcardStatementContext(final StatementContext delegate, Environment environment) {
        super(delegate, new Filter() {
            @Override
            public String filter(final String placeholder) {
                if (SELECTION_KEY.equals(placeholder)) {
                    return "*";
                }
                return delegate.resolve(placeholder);
            }

            @Override
            public String[] filterTuple(final String placeholder) {
                if (!environment.isStandalone()) {
                    Tuple t = Tuple.from(placeholder);
                    if (t == SELECTED_PROFILE || t == SELECTED_GROUP) {
                        return new String[]{t.resource(), "*"};
                    }
                }
                return delegate.resolveTuple(placeholder);
            }
        });
    }
}