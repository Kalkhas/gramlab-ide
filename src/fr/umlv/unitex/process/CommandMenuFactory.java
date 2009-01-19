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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * @author S�bastien Paumier
 *  
 */
public class CommandMenuFactory {
    
	@SuppressWarnings("unchecked")
	public static JMenu makeCommandMenu() {
    Class[] commands={CheckDicCommand.class
    		,CompressCommand.class
            ,CompressKrCommand.class
            ,ConcordCommand.class
            ,ConcorDiffCommand.class
            ,ConsultDicCommand.class
            ,ConvertCommand.class
            ,DicoCommand.class
            ,ElagCommand.class
            ,ElagCompCommand.class
            ,EvambCommand.class
            ,ExplodeFst2Command.class
            ,ExtractCommand.class
            ,FlattenCommand.class
            ,Fst2GrfCommand.class
            ,Fst2ListCommand.class
            ,Fst2TxtCommand.class
            ,Fst2UnambigCommand.class
            ,Grf2Fst2Command.class
            ,ImplodeFst2Command.class
            ,InflectKrCommand.class
            ,Jamo2SylCommand.class
            ,LocateCommand.class
            ,MergeBinCommand.class
            ,MergeTextAutomatonCommand.class
            /* This is normal that MkdirCommand is not in this list,
             * since it's not a Unitex command */
            ,MultiFlexCommand.class
            ,NormalizeCommand.class
            ,PolyLexCommand.class
            ,ReconstrucaoCommand.class
            ,Reg2GrfCommand.class
            ,SortMorphCommand.class
            ,SortTxtCommand.class
            ,SufForm2RacCommand.class
            ,Syl2JamoCommand.class
            ,Table2GrfCommand.class
			,TagsetNormFst2Command.class
			,TEI2TxtCommand.class
            ,TokenizeCommand.class
            ,Txt2Fst2Command.class
            ,Txt2Fst2KrCommand.class
            ,XAlignCommand.class
            ,XMLizerCommand.class
            };
		JMenu menu = new JMenu("Help on commands");
    for (int i=0;i<commands.length;i++) {
      menu.add(makeItem(commands[i]));
    }
		return menu;
	}

	@SuppressWarnings("unchecked")
	private static JMenuItem makeItem(final Class c) {
		JMenuItem item = new JMenuItem(getCommandName(c));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					new ProcessInfoFrame((CommandBuilder)c.newInstance(),
							false);
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
		});
		return item;
	}
    
  @SuppressWarnings("unchecked")
private static String getCommandName(Class commandClass) {
    String name=commandClass.getName();
    int index=name.lastIndexOf('.');
    // "Command".length()==7
    return name.substring(index+1,name.length()-7);
  }

}