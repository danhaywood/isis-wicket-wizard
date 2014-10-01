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
package org.isisaddons.wicket.wizard.cpt.applib;

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * Convenience super class for all wizard view models.
 * 
 * <p>
 * Subclassing is NOT mandatory; the methods in this superclass can be pushed
 * down into domain objects and another superclass used if required.
 *
 * @see org.apache.isis.applib.DomainObjectContainer
 */
public abstract class AbstractWizard<W,S extends AbstractWizard.State<W>> extends AbstractViewModel implements Wizard<W> {

    //region > state

    public interface State<W> {
        public State next();
        public String disableNext(W w);

        public State previous();
        public String disablePrevious(W w);
    }

    private S state;

    @Programmatic
    public S getState() {
        return state;
    }

    public void setState(final S state) {
        this.state = state;
    }
    //endregion

    //region > next (action)
    @MemberOrder(sequence = "1")
    public W next() {
        setState((S) getState().next());
        return cloneThis();
    }

    public String disableNext() {
        return getState().disableNext((W) this);
    }
    //endregion

    //region > previous (action)
    @MemberOrder(sequence = "1")
    public W previous() {
        setState((S) getState().previous());
        return cloneThis();
    }

    public String disablePrevious() {
        return getState().disablePrevious((W) this);
    }
    //endregion


    //region > clone
    public abstract W clone();

    protected abstract W cloneThis();

    //endregion

}
