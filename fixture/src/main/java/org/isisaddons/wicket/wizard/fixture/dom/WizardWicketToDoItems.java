/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
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

import java.math.BigDecimal;
import java.util.List;
import org.isisaddons.wicket.wizard.fixture.dom.WizardWicketToDoItem.Category;
import org.isisaddons.wicket.wizard.fixture.dom.WizardWicketToDoItem.Subcategory;
import org.joda.time.LocalDate;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.clock.ClockService;

@Named("ToDos")
@DomainService(menuOrder = "10", repositoryFor = WizardWicketToDoItem.class)
public class WizardWicketToDoItems {

    //region > identification in the UI

    public String getId() {
        return "toDoItems";
    }

    public String iconName() {
        return "ToDoItem";
    }
    //endregion

    //region > notYetComplete (action)

    @Bookmarkable
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "10")
    public List<WizardWicketToDoItem> notYetComplete() {
        final List<WizardWicketToDoItem> items = notYetCompleteNoUi();
        if(items.isEmpty()) {
            container.informUser("All to-do items have been completed :-)");
        }
        return items;
    }

    @Programmatic
    public List<WizardWicketToDoItem> notYetCompleteNoUi() {
        return container.allMatches(
                new QueryDefault<WizardWicketToDoItem>(WizardWicketToDoItem.class,
                        "findByOwnedByAndCompleteIsFalse", 
                        "ownedBy", currentUserName()));
    }
    //endregion

    //region > complete (action)

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "20")
    public List<WizardWicketToDoItem> complete() {
        final List<WizardWicketToDoItem> items = completeNoUi();
        if(items.isEmpty()) {
            container.informUser("No to-do items have yet been completed :-(");
        }
        return items;
    }

    @Programmatic
    public List<WizardWicketToDoItem> completeNoUi() {
        return container.allMatches(
            new QueryDefault<WizardWicketToDoItem>(WizardWicketToDoItem.class,
                    "findByOwnedByAndCompleteIsTrue", 
                    "ownedBy", currentUserName()));
    }
    //endregion

    //region > newToDo (action)

    @MemberOrder(sequence = "5")
    public WizardWicketToDoItem newToDo(
            final @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") @Named("Description") String description, 
            final @Named("Category") Category category,
            final @Named("Subcategory") Subcategory subcategory,
            final @Optional @Named("Due by") LocalDate dueBy,
            final @Optional @Named("Cost") BigDecimal cost) {
        return newToDo(description, category, subcategory, currentUserName(), dueBy);
    }
    public Category default1NewToDo() {
        return Category.Professional;
    }
    public Subcategory default2NewToDo() {
        return Category.Professional.subcategories().get(0);
    }
    public LocalDate default3NewToDo() {
        return clockService.now().plusDays(14);
    }
    public List<Subcategory> choices2NewToDo(
            final String description, final Category category) {
        return Subcategory.listFor(category);
    }
    public String validateNewToDo(
            final String description, 
            final Category category, final Subcategory subcategory, 
            final LocalDate dueBy, final BigDecimal cost) {
        return Subcategory.validate(category, subcategory);
    }
    //endregion

    //region > allToDos (action)
    // //////////////////////////////////////

    @Prototype
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "50")
    public List<WizardWicketToDoItem> allToDos() {
        final List<WizardWicketToDoItem> items = container.allMatches(
                new QueryDefault<WizardWicketToDoItem>(WizardWicketToDoItem.class,
                        "findByOwnedBy", 
                        "ownedBy", currentUserName()));
        if(items.isEmpty()) {
            container.warnUser("No to-do items found.");
        }
        return items;
    }
    //endregion

    //region > autoComplete (programmatic)
    // //////////////////////////////////////

    @Programmatic // not part of metamodel
    public List<WizardWicketToDoItem> autoComplete(final String description) {
        return container.allMatches(
                new QueryDefault<WizardWicketToDoItem>(WizardWicketToDoItem.class,
                        "findByOwnedByAndDescriptionContains", 
                        "ownedBy", currentUserName(), 
                        "description", description));
    }
    //endregion

    //region > helpers
    // //////////////////////////////////////

    @Programmatic // for use by fixtures
    public WizardWicketToDoItem newToDo(
            final String description,
            final Category category,
            final Subcategory subcategory,
            final String userName,
            final LocalDate dueBy) {
        final WizardWicketToDoItem toDoItem = container.newTransientInstance(WizardWicketToDoItem.class);
        toDoItem.setDescription(description);
        toDoItem.setCategory(category);
        toDoItem.setSubcategory(subcategory);
        toDoItem.setOwnedBy(userName);
        toDoItem.setDueBy(dueBy);

        container.persist(toDoItem);
        container.flush();

        return toDoItem;
    }
    
    private String currentUserName() {
        return container.getUser().getName();
    }

    //endregion

    //region > common validation
    // //////////////////////////////////////
    private static final long ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L;

    @Programmatic
    public String validateDueBy(LocalDate dueBy) {
        return isMoreThanOneWeekInPast(dueBy) ? "Due by date cannot be more than one week old" : null;
    }
    @Programmatic
    boolean isMoreThanOneWeekInPast(final LocalDate dueBy) {
        return dueBy.toDateTimeAtStartOfDay().getMillis() < clockService.nowAsMillis() - ONE_WEEK_IN_MILLIS;
    }
    //endregion

    //region > injected services
    // //////////////////////////////////////
    
    @javax.inject.Inject
    private DomainObjectContainer container;

    @javax.inject.Inject
    private ClockService clockService;


    //endregion

}
