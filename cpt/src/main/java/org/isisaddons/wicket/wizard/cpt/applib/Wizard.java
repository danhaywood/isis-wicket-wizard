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

import org.apache.isis.applib.ViewModel;

/**
 * Indicates that a domain object is intended to be used as an (editable) wizard.
 */
public interface Wizard<W> extends ViewModel.Cloneable {

    public W next();
    public String disableNext();

    public W previous();
    public String disablePrevious();

    public Object finish();
    public String disableFinish();

}
