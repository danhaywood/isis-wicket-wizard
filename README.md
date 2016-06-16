# isis-wicket-wizard #

*THIS COMPONENT IS STILL WORK-IN-PROGRESS, HAS NOT YET BEEN RELEASED*

[![Build Status](https://travis-ci.org/isisaddons/isis-wicket-wizard.png?branch=master)](https://travis-ci.org/isisaddons/isis-wicket-wizard)

This component, intended for use with [Apache Isis](http://isis.apache.org)'s Wicket viewer, provides a simple wizard 
with next, previous, finish and cancel actions.

## Screenshots

The following screenshots show an example app's usage of the component.

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page1.png)

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page2.png)

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page3.png)

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page4.png)

Or, the "update category" action can be used to create a slightly different flow:

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page5.png)

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page6.png)

![](https://raw.github.com/isisaddons/isis-wicket-wizard/master/images/page7.png)

## API & Usage ##

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

    
## How to run the Demo App ##

The prerequisite software is:

* Java JDK 8 (>= 1.9.0) or Java JDK 7 (<= 1.8.0)
* [maven 3](http://maven.apache.org) (3.2.x is recommended).

To build the demo app:

    git clone https://github.com/isisaddons/isis-wicket-wizard.git
    mvn clean install

To run the demo app:

    mvn antrun:run -P self-host
    
Then log on using user: `sven`, password: `pass`


## How to configure/use ##

You can either use this component "out-of-the-box", or you can fork this repo and extend to your own requirements. 

#### "Out-of-the-box" ####

To use "out-of-the-box", add this component to your project's `dom` module's `pom.xml`:

<pre>
        &lt;dependency&gt;
            &lt;groupId&gt;org.isisaddons.wicket.wizard&lt;/groupId&gt;
            &lt;artifactId&gt;isis-wicket-wizard-cpt&lt;/artifactId&gt;
            &lt;version&gt;1.6.0&lt;/version&gt;
        &lt;/dependency&gt;
</pre>

Check for later releases by searching [Maven Central Repo](http://search.maven.org/#search|ga|1|isis-wicket-wizard-cpt).


In `WEB-INF\isis.properties`, register the `WizardInterfaceFacetFactory` facet factory:

<pre>
    isis.reflector.facets.include=...,\
            org.isisaddons.wicket.wizard.metamodel.WizardInterfaceFacetFactory
</pre>

There is no requirement to explicitly register the Wicket UI component (`WizardPropertiesPanelFactory`); it will be automatically discovered from the classpath.


#### "Out-of-the-box" (-SNAPSHOT) ####

If you want to use the current `-SNAPSHOT`, then the steps are the same as above, except:

* when updating the classpath, specify the appropriate -SNAPSHOT version:

<pre>
    &lt;version&gt;1.8.0-SNAPSHOT&lt;/version&gt;
</pre>

* add the repository definition to pick up the most recent snapshot (we use the Cloudbees continuous integration service).  We suggest defining the repository in a `<profile>`:

<pre>
    &lt;profile&gt;
        &lt;id&gt;cloudbees-snapshots&lt;/id&gt;
        &lt;activation&gt;
            &lt;activeByDefault&gt;true&lt;/activeByDefault&gt;
        &lt;/activation&gt;
        &lt;repositories&gt;
            &lt;repository&gt;
                &lt;id&gt;snapshots-repo&lt;/id&gt;
                &lt;url&gt;http://repository-estatio.forge.cloudbees.com/snapshot/&lt;/url&gt;
                &lt;releases&gt;
                    &lt;enabled&gt;false&lt;/enabled&gt;
                &lt;/releases&gt;
                &lt;snapshots&gt;
                    &lt;enabled&gt;true&lt;/enabled&gt;
                &lt;/snapshots&gt;
            &lt;/repository&gt;
        &lt;/repositories&gt;
    &lt;/profile&gt;
</pre>


#### Forking the repo ####


If instead you want to extend this component's functionality, then we recommend that you fork this repo.  The repo is 
structured as follows:

* `pom.xml      ` - parent pom
* `cpt          ` - the component' own parent pom
* `fixture      ` - fixtures, holding a sample domain objects and fixture scripts
* `webapp       ` - demo webapp (see above screenshots); depends on `ext` and `fixture`

Only the `cpt` project (and its submodules) is released to Maven central.  The versions of the other modules 
are purposely left at `0.0.1-SNAPSHOT` because they are not intended to be released.


## Change Log ##

* `x.x.x` - ... not yet released ...

    
## Legal Stuff ##

#### License  ####

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

#### Dependencies ####

Other than Apache Isis, this component has no third-party dependencies.

##  Maven deploy notes ##

Only the `dom` module is deployed, and is done so using Sonatype's OSS support (see 
[user guide](http://central.sonatype.org/pages/apache-maven.html)).

#### Release to Sonatype's Snapshot Repo ####

To deploy a snapshot, use:

    pushd cpt
    mvn clean deploy
    popd

The artifacts should be available in Sonatype's 
[Snapshot Repo](https://oss.sonatype.org/content/repositories/snapshots).

#### Release to Maven Central ####

The `release.sh` script automates the release process.  It performs the following:

* performs a sanity check (`mvn clean install -o`) that everything builds ok
* bumps the `pom.xml` to a specified release version, and tag
* performs a double check (`mvn clean install -o`) that everything still builds ok
* releases the code using `mvn clean deploy`
* bumps the `pom.xml` to a specified release version

For example:

    sh release.sh 1.13.0 \
                  1.14.0-SNAPSHOT \
                  dan@haywood-associates.co.uk \
                  "this is not really my passphrase"
    
where
* `$1` is the release version
* `$2` is the snapshot version
* `$3` is the email of the secret key (`~/.gnupg/secring.gpg`) to use for signing
* `$4` is the corresponding passphrase for that secret key.

Other ways of specifying the key and passphrase are available, see the `pgp-maven-plugin`'s 
[documentation](http://kohsuke.org/pgp-maven-plugin/secretkey.html)).

If the script completes successfully, then push changes:

    git push origin master
    git push origin 1.13.0

If the script fails to complete, then identify the cause, perform a `git reset --hard` to start over and fix the issue
before trying again.  Note that in the `cpt`'s `pom.xml` the `nexus-staging-maven-plugin` has the 
`autoReleaseAfterClose` setting set to `true` (to automatically stage, close and the release the repo).  You may want
to set this to `false` if debugging an issue.
 
According to Sonatype's guide, it takes about 10 minutes to sync, but up to 2 hours to update [search](http://search.maven.org).
