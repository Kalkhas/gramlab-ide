 /*
  * Unitex
  *
  * Copyright (C) 2001-2009 Universit� Paris-Est Marne-la-Vall�e <unitex@univ-mlv.fr>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
  *
  */

package fr.umlv.unitex.process;

import java.io.*;

import fr.umlv.unitex.*;

/**
 * @author S�bastien Paumier
 *  
 */
public class Fst2TxtCommand extends CommandBuilder {

	public Fst2TxtCommand() {
		super("Fst2Txt");
	}

	public Fst2TxtCommand text(File s) {
		protectElement("-t"+s.getAbsolutePath());
		return this;
	}

    public Fst2TxtCommand fst2(File s) {
        protectElement(s.getAbsolutePath());
        return this;
    }

    public Fst2TxtCommand alphabet() {
      protectElement("-a"+Config.getAlphabet().getAbsolutePath());
      return this;
  }

    public Fst2TxtCommand mode(boolean merge) {
      element(merge?"-M":"-R");
      return this;
  }

    public Fst2TxtCommand charByChar(boolean morphologicalUseOfSpace) {
      if (morphologicalUseOfSpace) element("--start_on_space");
      element("--char_by_char");
      return this;
  }

}