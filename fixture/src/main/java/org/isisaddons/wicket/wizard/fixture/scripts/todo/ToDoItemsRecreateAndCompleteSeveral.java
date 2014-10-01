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
package org.isisaddons.wicket.wizard.fixture.scripts.todo;

import java.util.Collection;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.isisaddons.wicket.wizard.fixture.dom.WizardWicketToDoItem;
import org.apache.isis.applib.fixturescripts.FixtureScript;

public class ToDoItemsRecreateAndCompleteSeveral extends FixtureScript {

    //region > constructor
    private final String user;
    /**
     * @param user - if null then executes for the current user or will use any {@link #run(String) parameters} provided when run.
     */
    public ToDoItemsRecreateAndCompleteSeveral(final String user) {
        super(null, Util.localNameFor("complete", user));
        this.user = user;
    }
    //endregion

    //region > doRun
    @Override
    protected void execute(ExecutionContext executionContext) {
        final String ownedBy = Util.coalesce(user, executionContext.getParameters(), getContainer().getUser().getName());

        // prereqs
        execute(new ToDoItemsRecreate(null), executionContext);

        // this fixture
        complete(ownedBy, "Buy stamps", executionContext);
        complete(ownedBy, "Write blog post", executionContext);
    }

    private void complete(final String user, final String description, final ExecutionContext executionContext) {
        final WizardWicketToDoItem toDoItem = findToDoItem(description, user);
        toDoItem.setComplete(true);
        executionContext.add(this, toDoItem);
    }

    private WizardWicketToDoItem findToDoItem(final String description, final String user) {
        final Collection<WizardWicketToDoItem> filtered = Collections2.filter(getContainer().allInstances(WizardWicketToDoItem.class), new Predicate<WizardWicketToDoItem>() {
            @Override
            public boolean apply(WizardWicketToDoItem input) {
                return Objects.equal(description, input.getDescription()) &&
                       Objects.equal(user, input.getOwnedBy());
            }
        });
        return filtered.isEmpty()? null: filtered.iterator().next();
    }
    //endregion
}