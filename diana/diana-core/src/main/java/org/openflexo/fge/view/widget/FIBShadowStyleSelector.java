/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.fge.view.widget;

import javax.swing.JComponent;

import org.openflexo.fge.ShadowStyle;
import org.openflexo.fib.model.FIBCustom.FIBCustomComponent;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to view and edit a ShadowStyle
 * 
 * @author sguerin
 * 
 */
// TODO: suppress reference to Swing (when FIB library will be independant from SWING technology)
public interface FIBShadowStyleSelector<C extends JComponent> extends FIBCustomComponent<ShadowStyle, C> {

	public static  Resource FIB_FILE = ResourceLocator.locateResource("Fib/ShadowStyleSelector.fib");

}