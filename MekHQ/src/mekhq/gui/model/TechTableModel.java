package mekhq.gui.model;

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.PersonnelOptions;
import mekhq.campaign.personnel.Skill;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.work.IPartWork;
import mekhq.gui.BasicInfo;
import mekhq.gui.CampaignGUI;
import mekhq.gui.ITechWorkPanel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

/**
 * A table model for displaying work items
 */
public class TechTableModel extends DataTableModel {

    /** Contains the skill levels to be displayed in a tech's description */
    private static final String[] DISPLAYED_SKILL_LEVELS = new String[] {
        SkillType.S_TECH_MECH,
        SkillType.S_TECH_MECHANIC,
        SkillType.S_TECH_BA,
        SkillType.S_TECH_AERO,
        SkillType.S_TECH_VESSEL,
    };

    private CampaignGUI tab;
    private ITechWorkPanel panel;

    public TechTableModel(CampaignGUI tab, ITechWorkPanel panel) {
        columnNames = new String[] { "Techs" };
        data = new ArrayList<Person>();
        this.tab = tab;
        this.panel = panel;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return getTechAt(row);
    }

    public Person getTechAt(int row) {
        return (Person) data.get(row);
    }

    public Campaign getCampaign() {
        return tab.getCampaign();
    }

    public Renderer getRenderer() {
        return new Renderer();
    }

    public class Renderer extends BasicInfo implements TableCellRenderer {
        public Renderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = this;
            int actualRow = table.convertRowIndexToModel(row);
            setOpaque(true);
            Person tech = getTechAt(actualRow);
            setImage(tech.getPortrait().getImage(54));
            setHtmlText(getTechDesc(tech, getCampaign().isOvertimeAllowed(), panel.getSelectedTask()));
            if (isSelected) {
                highlightBorder();
            } else {
                unhighlightBorder();
            }
            c.setBackground(table.getBackground());
            c.setForeground(table.getForeground());
            return c;
        }
    }

    public String getTechDesc(Person tech, boolean overtimeAllowed, IPartWork part) {
        StringBuilder toReturn = new StringBuilder(128);
        toReturn.append("<html><font size='3'");
        if ((null != part) && (null != part.getUnit()) && tech.getTechUnits().contains(part.getUnit())) {
            toReturn.append(" color='" + MekHQ.getMHQOptions().getFontColorPositiveHexColor() + "'><b>@");
        }
        else {
            toReturn.append("><b>");
        }
        toReturn.append(tech.getFullTitle()).append("</b><br/>");

        boolean first = true;
        for (String skillName : DISPLAYED_SKILL_LEVELS) {
            Skill skill = tech.getSkill(skillName);
            if (null == skill) {
                continue;
            } else if (!first) {
                toReturn.append("; ");
            }

            toReturn.append(SkillType.getExperienceLevelName(skill.getExperienceLevel()));
            toReturn.append(' ').append(skillName);
            first = false;
        }

        toReturn.append(String.format(" (%d XP", tech.getXP()));
        // if Edge usage is allowed for techs, display remaining edge in the dialogue
        if (getCampaign().getCampaignOptions().isUseSupportEdge()
            && tech.getOptions().booleanOption(PersonnelOptions.EDGE_REPAIR_BREAK_PART)) {
                toReturn.append(String.format(", %d Edge)", tech.getCurrentEdge()));
        } else {
            toReturn.append(")");
        }
        toReturn.append("<br/>");

        toReturn.append(String.format("%d minutes left", tech.getMinutesLeft()));
        if (overtimeAllowed) {
            toReturn.append(String.format(" + (%d overtime)", tech.getOvertimeLeft()));
        }
        toReturn.append("</font></html>");
        return toReturn.toString();
    }
}
