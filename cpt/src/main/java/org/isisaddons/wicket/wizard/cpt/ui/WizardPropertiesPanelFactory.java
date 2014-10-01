/*
 *  Copyright 2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.wicket.wizard.cpt.ui;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

/**
 * {@link org.apache.isis.viewer.wicket.ui.ComponentFactory} for {@link WizardPropertiesPanel}.
 */
public class WizardPropertiesPanelFactory extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public WizardPropertiesPanelFactory() {
        super(ComponentType.ENTITY_PROPERTIES, WizardPropertiesPanel.class);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;
        return new WizardPropertiesPanel(id, entityModel);
    }

    @Override
    protected ApplicationAdvice appliesTo(IModel<?> model) {
        final ApplicationAdvice applicationAdvice = super.appliesTo(model);
        if(!applicationAdvice.applies()) {
            return applicationAdvice;
        }
        // applies and so therefore must be an EntityModel
        final EntityModel entityModel = (EntityModel) model;
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        if(specification.isWizard()) {
            return ApplicationAdvice.APPLIES_EXCLUSIVELY;
        }
        return ApplicationAdvice.DOES_NOT_APPLY;
    }
}
