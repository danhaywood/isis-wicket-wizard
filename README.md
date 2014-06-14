isis-wicket-wizard
==================

[![Build Status](https://travis-ci.org/danhaywood/isis-wicket-wizard.png?branch=master)](https://travis-ci.org/danhaywood/isis-wicket-wizard)

(Requires [Apache Isis](http://isis.apache.org) `1.6.0-SNAPSHOT` or later).

Extension for Apache Isis' Wicket Viewer, to support an wizard with next, previous, finish and cancel actions.

## Screenshots

The following screenshots are taken from the `zzzdemo` app (adapted from Isis' quickstart archetype).  See below for further details.

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page1.png)

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page2.png)

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page3.png)

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page4.png)

Or, the "update category" action can be used to create a slightly different flow:

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page5.png)

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page6.png)

![](https://raw.github.com/danhaywood/isis-wicket-wizard/master/images/page7.png)

## API & Usage

The wizard implements an `Wizard` interface that is itself extends from `ViewModel.Cloneable` interface:

    public interface Wizard<W> extends ViewModel.Cloneable {

        public W next();
        public String disableNext();

        public W previous();
        public String disablePrevious();

        public Object finish();
        public String disableFinish();

    }

In the demo app, the `ToDoItemWizard` implements `Wizard` (by way of the `AbstractWizard` convenience abstract class).  It has eight properties (four of them for the summary page) and uses a `State` class to determine which should be shown or hidden:

    public class ToDoItemWizard
            extends AbstractWizard<ToDoItemWizard, ToDoItemWizard.State>
            implements Categorized {

        public ToDoItemWizard() { setState(State.DESCRIPTION); }

        public String title() {
            return !Strings.isNullOrEmpty(getDescription()) ? getDescription() : "New item";
        }

        //region > viewModel implementation
        @Override
        public String viewModelMemento() {
            return toDoItemWizardSupport.mementoFor(this);
        }

        @Override
        public void viewModelInit(String memento) {
            toDoItemWizardSupport.initOf(memento, this);
        }

        @Override
        public ToDoItemWizard clone() {
            return cloneThis();
        }
        @Override
        protected ToDoItemWizard cloneThis() {
            return toDoItemWizardSupport.clone(this);
        }

        public enum State implements AbstractWizard.State<ToDoItemWizard> {
            DESCRIPTION,
            CATEGORIES,
            DUE_BY,
            SUMMARY_PAGE;
            public boolean hideDescription() { return this != DESCRIPTION; }
            public boolean hideCategories() { return this != CATEGORIES; }
            public boolean hideDueBy() { return this != DUE_BY; }
            public boolean hideSummary() { return this != SUMMARY_PAGE; }
            @Override
            public State next() {
                switch (this) {
                    case DESCRIPTION: return CATEGORIES;
                    case CATEGORIES: return DUE_BY;
                    case DUE_BY: return SUMMARY_PAGE;
                    default: return null;
                }
            }
            @Override
            public String disableNext(ToDoItemWizard w) { return w.getState().next() == null? "No more pages": null; }

            @Override
            public State previous() {
                switch (this) {
                    case SUMMARY_PAGE: return DUE_BY;
                    case DUE_BY: return CATEGORIES;
                    case CATEGORIES: return DESCRIPTION;
                    default: return null;
                }
            }
            @Override
            public String disablePrevious(ToDoItemWizard w) { return w.getState().previous() == null? "No more pages": null; }
        }

        private String description;
        @MaxLength(100)
        @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
        @TypicalLength(50)
        public String getDescription() { return description; }
        public void setDescription(final String description) { this.description = description; }
        public boolean hideDescription() { return getState().hideDescription(); }

        private Category category;
        public Category getCategory() { return category; }
        public void setCategory(final Category category) { this.category = category; }
        public boolean hideCategory() { return getState().hideCategories(); }
        public String disableCategory() { return subcategory != null? "Use the update action to change both category and subcategory": null; }

        private ToDoItem.Subcategory subcategory;
        @Disabled(reason = "Use the update action to change both category and subcategory")
        @Optional
        public ToDoItem.Subcategory getSubcategory() { return subcategory; }
        public void setSubcategory(final ToDoItem.Subcategory subcategory) { this.subcategory = subcategory; }
        public boolean hideSubcategory() { return subcategory == null || getState().hideCategories(); }

        private LocalDate dueBy;
        @Optional
        public LocalDate getDueBy() { return dueBy; }
        public void setDueBy(final LocalDate dueBy) { this.dueBy = dueBy; }
        public String validateDueBy(final LocalDate dueBy) { 
            if (dueBy == null) { 
                return null;
            }
            return toDoItems.validateDueBy(dueBy);
        }
        public boolean hideDueBy() { return getState().hideDueBy(); }
        //endregion

        public String getDescriptionOnSummary() { return getDescription(); }
        public boolean hideDescriptionOnSummary() { return getState().hideSummary(); }

        public Category getCategoryOnSummary() { return getCategory(); }
        public boolean hideCategoryOnSummary() { return getState().hideSummary(); }

        public ToDoItem.Subcategory getSubcategoryOnSummary() { return getSubcategory(); }
        public boolean hideSubcategoryOnSummary() { return getState().hideSummary() || getSubcategory() == null; }

        public LocalDate getDueByOnSummary() { return getDueBy(); }
        public boolean hideDueByOnSummary() { return getState().hideSummary(); }

        @MemberOrder(sequence = "1")
        public ToDoItem finish() { 
            return toDoItems.newToDo(getDescription(), getCategory(), getSubcategory(), getDueBy(), null);
        }
        @Override
        public String disableFinish() { return null; }


        @javax.inject.Inject
        private ToDoItemWizardSupport toDoItemWizardSupport;

        @javax.inject.Inject
        private ToDoItems toDoItems;
    }


    
The `ToDoItemWizard.layout.json` file is also worth reviewing; it shows how the wizard's properties are organized into pages:

    {
      "columns": [
        {
          "span": 12,
          "memberGroups": {
            "What task do you need to do?": {
              "members": {
                "description": {
                  "typicalLength": {
                    "value": 50
                  }
                }
              }
            },
            "Is it similar to other todo items?": {
              "members": {
                "category": { },
                "subcategory": { }
              }
            },
            "Does it need to be done by a particular date?": {
              "members": {
                "dueBy": {
                  "typicalLength": {
                    "value": 12
                  }
                }
              }
            },
            "Confirm details are correct": {
              "members": {
                "descriptionOnSummary": {
                  "named": {
                    "value": "Description"
                  },
                  "typicalLength": {
                    "value": 50
                  }
                },
                "categoryOnSummary": {
                  "named": {
                    "value": "Category"
                  }
                },
                "subcategoryOnSummary": {
                  "named": {
                    "value": "Subcategory"
                  }
                },
                "dueByOnSummary": {
                  "named": {
                    "value": "Due by"
                  }
                }
              }
            }
          }
        },
        {
          "span": 0,
          "memberGroups": {}
        },
        {
          "span": 0,
          "memberGroups": {}
        },
        {
          "span": 12,
          "collections": {}
        }
      ],
      "actions": {
        "previous": {
          "named": {
            "value": "Previous"
          }
        },
        "next": {
          "named": {
            "value": "Next"
          }
        },
        "finish": {
          "named": {
            "value": "Finish"
          }
        }
      }
    }

## Isis Configuration

In `WEB-INF\isis.properties`, register the `WizardInterfaceFacetFactory` facet factory:

    isis.reflector.facets.include=com.danhaywood.isis.wicket.wizard.metamodel.WizardInterfaceFacetFactory

There is no requirement to explicitly register the Wicket UI component (`WizardPropertiesPanelFactory`); it will be automatically discovered from the classpath.
    
## Maven Configuration

In your project's parent `pom.xml`, add to the `<dependencyManagement>` section:

    <dependencyManagement>
        <dependencies>
            ...
            <dependency>
                <groupId>com.danhaywood.isis.wicket</groupId>
                <artifactId>danhaywood-isis-wicket-wizard</artifactId>
                <version>x.y.z</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            ...
        </dependencies>
    </dependencyManagement>

where `x.y.z` is the latest available version (search the [Maven Central Repo](http://search.maven.org/#search|ga|1|isis-wicket-wizard)).

In your project's DOM `pom.xml`, add a dependency on the `applib` module:

    <dependencies>
        ...
        <dependency>
            <groupId>com.danhaywood.isis.wicket</groupId>
            <artifactId>danhaywood-isis-wicket-wizard-applib</artifactId>
        </dependency>
        ...
    </dependencies> 

In your project's webapp `pom.xml`, add a dependency on the `metamodel` and `ui` modules:

    <dependencies>
        ...
        <dependency>
            <groupId>com.danhaywood.isis.wicket</groupId>
            <artifactId>danhaywood-isis-wicket-wizard-metamodel</artifactId>
        </dependency>

        <dependency>
            <groupId>com.danhaywood.isis.wicket</groupId>
            <artifactId>danhaywood-isis-wicket-wizard-ui</artifactId>
        </dependency>
        ...
    </dependencies> 

## Legal Stuff

### License

    Copyright 2014 Dan Haywood

    Licensed under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

### Dependencies

No third-party dependencies.
