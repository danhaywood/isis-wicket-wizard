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
package org.isisaddons.wicket.wizard.fixture.dom;

import java.util.List;
import org.isisaddons.wicket.wizard.fixture.app.ToDoItemWizard;
import org.isisaddons.wicket.wizard.fixture.dom.WizardWicketToDoItem.Category;
import org.isisaddons.wicket.wizard.fixture.dom.WizardWicketToDoItem.Subcategory;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;

@DomainService
public class WizardWicketToDoItemContributions extends AbstractFactoryAndRepository {

    //region > updateCategory (contributed action)

    @DescribedAs("Update category and subcategory")
    @NotInServiceMenu
    @ActionSemantics(Of.IDEMPOTENT)
    public Categorized updateCategory(
            final Categorized item,
            final @Named("Category") Category category,
            final @Optional @Named("Subcategory") Subcategory subcategory) {
        item.setCategory(category);
        item.setSubcategory(subcategory);
        return item;
    }

    public boolean hideUpdateCategory(
            final Categorized item,
            final Category category,
            final Subcategory subcategory) {
        // bit nasty, I suppose; pushing the boundaries of "DCI"
        if(!(item instanceof ToDoItemWizard)) {
            return false;
        }
        final ToDoItemWizard toDoItemWizard = (ToDoItemWizard) item;
        return toDoItemWizard.getState().hideCategories();
    }
    public Category default1UpdateCategory(
            final Categorized item) {
        return item != null? item.getCategory(): null;
    }
    public Subcategory default2UpdateCategory(
            final Categorized item) {
        return item != null? item.getSubcategory(): null;
    }

    public List<Subcategory> choices2UpdateCategory(
            final Categorized item, final Category category) {
        return Subcategory.listFor(category);
    }
    
    public String validateUpdateCategory(
            final Categorized item, final Category category, final Subcategory subcategory) {
        return Subcategory.validate(category, subcategory);
    }
    //endregion

}
