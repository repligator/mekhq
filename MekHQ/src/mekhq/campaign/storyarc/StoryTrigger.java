/*
 * StoryTrigger.java
 *
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.storyarc;

import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.MekHqXmlSerializable;
import mekhq.campaign.Campaign;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;

/**
 * A Story Trigger can be added to a StoryEvent or a StoryOutcome and when the StoryEvent is completed the StoryTrigger
 * will be executed and will do some things. This is a way to have StoryEvents affect things other than just the
 * next event
 */
public abstract class StoryTrigger implements Serializable, MekHqXmlSerializable {

    /** The story arc that this trigger is a part of **/
    private StoryArc arc;

    protected static final String NL = System.lineSeparator();

    public StoryTrigger() {
        //nothing here at the moment
    }

    public void setStoryArc(StoryArc a) { this.arc = a; }

    protected StoryArc getStoryArc() { return arc; }

    /**
     * Execute whatever the trigger does
     */
    protected abstract void execute();

    //region I/O
    @Override
    public abstract void writeToXml(PrintWriter pw1, int indent);

    protected void writeToXmlBegin(PrintWriter pw1, int indent) {
        String level = MekHqXmlUtil.indentStr(indent),
                level1 = MekHqXmlUtil.indentStr(indent + 1);

        StringBuilder builder = new StringBuilder(256);
        builder.append(level)
                .append("<storyTrigger type=\"")
                .append(this.getClass().getName())
                .append("\">")
                .append(NL);

        pw1.print(builder.toString());
    }

    protected void writeToXmlEnd(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</storyTrigger>");
    }

    protected abstract void loadFieldsFromXmlNode(Node wn, Campaign c) throws ParseException;

    public static StoryTrigger generateInstanceFromXML(Node wn, Campaign c) {
        StoryTrigger retVal = null;
        NamedNodeMap attrs = wn.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            // Instantiate the correct child class, and call its parsing
            // function.
            retVal = (StoryTrigger) Class.forName(className).getDeclaredConstructor().newInstance();

            retVal.loadFieldsFromXmlNode(wn, c);

        } catch (Exception ex) {
            MekHQ.getLogger().error(ex);
        }

        return retVal;
    }
    //endregion I/O

}
