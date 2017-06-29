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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;


/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class OtherSettingsPresenter extends MbuiPresenter<OtherSettingsPresenter.MyView, OtherSettingsPresenter.MyProxy>
        implements SupportsExpertMode, ElytronPresenter {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value ={
        KEY_STORE, KEY_MANAGER, SERVER_SSL_CONTEXT, CLIENT_SSL_CONTEXT, TRUST_MANAGER, CREDENTIAL_STORE,
        FILTERING_KEY_STORE, LDAP_KEY_STORE, PROVIDER_LOADER, AGGREGATE_PROVIDERS, SECURITY_DOMAIN,
        DIR_CONTEXT, AUTHENTICATION_CONTEXT, AUTHENTICATION_CONF, FILE_AUDIT_LOG, SIZE_FILE_AUDIT_LOG,
        PERIODIC_FILE_AUDIT_LOG, SYSLOG_AUDIT_LOG
    })
    @NameToken(NameTokens.ELYTRON_OTHER)
    public interface MyProxy extends ProxyPlace<OtherSettingsPresenter> {}

    public interface MyView extends MbuiView<OtherSettingsPresenter> {
        void updateKeyStore(List<NamedNode> model);
        void updateKeyManagers(List<NamedNode> model);
        void updateServerSslContext(List<NamedNode> model);
        void updateClientSslContext(List<NamedNode> model);
        void updateTrustManagers(List<NamedNode> model);
        void updateCredentialStore(List<NamedNode> model);
        void updateFilteringKeyStore(List<NamedNode> model);
        void updateLdapKeyStore(List<NamedNode> model);
        void updateProviderLoader(List<NamedNode> model);
        void updateAggregateProviders(List<NamedNode> model);
        void updateSecurityDomain(List<NamedNode> model);
        void updateDirContext(List<NamedNode> model);
        void updateAuthenticationContext(List<NamedNode> model);
        void updateAuthenticationConfiguration(List<NamedNode> model);
        void updateFileAuditLog(List<NamedNode> model);
        void updateSizeFileAuditLog(List<NamedNode> model);
        void updatePeriodicFileAuditLog(List<NamedNode> model);
        void updateSyslogAuditLog(List<NamedNode> model);
    }
    // @formatter:on

    private Dispatcher dispatcher;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public OtherSettingsPresenter(final EventBus eventBus,
            final OtherSettingsPresenter.MyView view,
            final OtherSettingsPresenter.MyProxy proxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.OTHER_SETTINGS),
                        resources.constants().settings(), Names.OTHER_SETTINGS);
    }

    @Override
    public void reload() {

        ResourceAddress address = ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
        crud.readChildren(address, asList(
                "key-store",
                "key-manager",
                "server-ssl-context",
                "client-ssl-context",
                "trust-manager",
                "credential-store",
                "filtering-key-store",
                "ldap-key-store",
                "provider-loader",
                "aggregate-providers",
                "security-domain",
                "dir-context",
                "authentication-context",
                "authentication-configuration",
                "file-audit-log",
                "size-rotating-file-audit-log",
                "periodic-rotating-file-audit-log",
                "syslog-audit-log"
                ),
                result -> {
                    // @formatter:off
                    int i = 0;
                    getView().updateKeyStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateKeyManagers(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateServerSslContext(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateClientSslContext(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateTrustManagers(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateCredentialStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateFilteringKeyStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateLdapKeyStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateProviderLoader(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateAggregateProviders(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateSecurityDomain(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateDirContext(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateAuthenticationContext(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateAuthenticationConfiguration(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateFileAuditLog(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateSizeFileAuditLog(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updatePeriodicFileAuditLog(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateSyslogAuditLog(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    // @formatter:on
                });
    }

    @Override
    public void saveForm(final String title, final String name, final Map<String, Object> changedValues,
            final Metadata metadata) {

        ResourceAddress address = metadata.getTemplate().resolve(statementContext, name);
        crud.save(title, name, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void saveComplexForm(final String title, final String name, String complexAttributeName,
            final Map<String, Object> changedValues, final Metadata metadata) {

        ResourceAddress address = metadata.getTemplate().resolve(statementContext, name);
        crud.save(title, name, complexAttributeName, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void saveFormPage(String resource, String listAttributeName, Metadata metadata,
            NamedNode payload, Map<String, Object> changedValues) {
        ResourceAddress address = metadata.getTemplate().resolve(statementContext, resource);
        OperationFactory operationFactory = new OperationFactory(
                name -> listAttributeName + "[" + payload.getName() + "]." + name);
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        dispatcher.execute(operations, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifySingleResourceSuccess(Names.CLIENT_MAPPING)));
            reload();
        });
    }

    @Override
    public void listRemove(String title, String resourceName, String complexAttributeName, int index,
            AddressTemplate template) {

        ResourceAddress address = template.resolve(statementContext, resourceName);
        crud.listRemove(title, resourceName, complexAttributeName, index, address, () -> reload());
    }


    @Override
    public void resetComplexAttribute(final String type, final String name, final String attribute,
            final Metadata metadata, final Callback callback) {

        ResourceAddress address = metadata.getTemplate().resolve(statementContext, name);
        Set<String> attributeToReset = new HashSet<>();
        attributeToReset.add(attribute);
        crud.reset(type, name, address, attributeToReset, metadata, callback);
    }

    @Override
    public void launchAddDialog(Function<String, String> resourceNameFunction, String complexAttributeName,
            Metadata metadata, String title) {

        String id = Ids.build(complexAttributeName, Ids.FORM_SUFFIX, Ids.ADD_SUFFIX);
        ResourceAddress address = metadata.getTemplate().resolve(statementContext, resourceNameFunction.apply(null));

        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .fromRequestProperties()
                .build();

        AddResourceDialog.Callback callback = (name, model) -> crud
                .listAdd(title, name, complexAttributeName, address, model, () -> reload());
        AddResourceDialog dialog = new AddResourceDialog(title, form, callback);
        dialog.show();
    }

}