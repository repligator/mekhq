/*
 * Copyright (c) 2014-2022 - The MegaMek Team. All Rights Reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.dialog;

import megamek.client.ui.models.XTableColumnModel;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.gui.CampaignGUI;
import mekhq.gui.enums.PersonnelFilter;
import mekhq.gui.model.AutoAwardsTableModel;
import mekhq.gui.sorter.PersonRankStringSorter;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

public class AutoAwardsDialog extends JDialog {
    Campaign campaign;
    CampaignGUI gui;

    private static final String PAN_AUTO_AWARDS = "PanAutoAwards";

    private CampaignGUI hqView;

    private Map<Integer, List<Object>> data;

    private JPanel panMain;
    private JTextArea txtInstructions;
    private CardLayout cardLayout;

    private JComboBox<PersonnelFilter> cboPersonnelFilter;
    private AutoAwardsTable personnelTable;
    private TableRowSorter<AutoAwardsTableModel> personnelSorter;

    private JButton btnSkip;
    private JButton btnSkipAll;
    private JButton btnDone;

    private boolean isSkipAll = true;

    private final ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.AutoAwardsDialog",
            MekHQ.getMHQOptions().getLocale());

    public AutoAwardsDialog(Campaign c, Map<Integer, List<Object>> awardData) {
        campaign = c;
        gui = campaign.getApp().getCampaigngui();
        hqView = gui;
        data = awardData;

        setSize(new Dimension(800, 600));
        initComponents();
        // TODO do I need this?
        // we might not need this
        btnDone.setEnabled(true);
        btnSkip.setEnabled(true);
        btnSkipAll.setEnabled(true);
        setLocationRelativeTo(gui.getFrame());
    }

    private void initComponents() {
        setTitle(resourceMap.getString("AutoAwardsDialog.title"));

        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        panMain = new JPanel(cardLayout);
        add(panMain, BorderLayout.CENTER);

        txtInstructions = new JTextArea();
        add(txtInstructions, BorderLayout.PAGE_START);
        txtInstructions.setEditable(false);
        txtInstructions.setWrapStyleWord(true);
        txtInstructions.setLineWrap(true);
        txtInstructions.setText(resourceMap.getString("txtInstructions.text"));
        txtInstructions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(resourceMap.getString("txtInstructions.title")),
                BorderFactory.createEmptyBorder(5,5,5,5)));

        JPanel autoAwardsPanel = new JPanel(new BorderLayout());

        cboPersonnelFilter = new JComboBox<>();
        for (PersonnelFilter filter : MekHQ.getMHQOptions().getPersonnelFilterStyle().getFilters(true)) {
            cboPersonnelFilter.addItem(filter);
        }

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
        upperPanel.add(cboPersonnelFilter);
        upperPanel.add(Box.createHorizontalGlue());
        upperPanel.add(Box.createRigidArea(new Dimension(5,0)));
        autoAwardsPanel.add(upperPanel, BorderLayout.PAGE_START);

        AutoAwardsTableModel model = new AutoAwardsTableModel(hqView.getCampaign());
        personnelTable = new AutoAwardsTable(model);

        personnelSorter = new TableRowSorter<>(model);
        personnelSorter.setComparator(AutoAwardsTableModel.COL_PERSON, new PersonRankStringSorter(hqView.getCampaign()));
        ArrayList<SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new SortKey(AutoAwardsTableModel.COL_PERSON, SortOrder.DESCENDING));
        personnelSorter.setSortKeys(sortKeys);

        cboPersonnelFilter.addActionListener(evt -> filterPersonnel(personnelSorter, cboPersonnelFilter));

        personnelTable.getSelectionModel().addListSelectionListener(ev -> {
            if (personnelTable.getSelectedRow() < 0) {
                return;
            }
            // TODO do I need this?
            int rowIndex = personnelTable.convertRowIndexToModel(personnelTable.getSelectedRow());
            UUID id = ((AutoAwardsTableModel)(personnelTable.getModel())).getPerson(rowIndex).getId();
        });

        personnelTable.getColumnModel().getColumn(personnelTable.convertColumnIndexToModel(AutoAwardsTableModel.COL_AWARD)).
                setCellEditor(new DefaultCellEditor(new JCheckBox()));

        // This is where we insert the external data
        model.setData(data);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(personnelTable);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        autoAwardsPanel.add(scrollPane, BorderLayout.CENTER);

        panMain.add(autoAwardsPanel, PAN_AUTO_AWARDS);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        btnDone = new JButton(resourceMap.getString("btnDone.text"));
        btnDone.addActionListener(buttonListener);
        btnSkip = new JButton(resourceMap.getString("btnSkip.text"));
        btnSkip.addActionListener(buttonListener);
        btnSkipAll = new JButton(resourceMap.getString("btnSkipAll.text"));
        btnSkipAll.addActionListener(buttonListener);

        // TODO do I need setVisible here?
        btnPanel.add(btnDone);
        btnDone.setVisible(true);
        btnPanel.add(btnSkip);
        btnSkip.setVisible(true);
        btnPanel.add(btnSkipAll);
        btnSkipAll.setVisible(true);

        add(btnPanel, BorderLayout.PAGE_END);
    }

    private ActionListener buttonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource().equals(btnDone)) {
                // TODO this is where we will commit Award issuing
//                for (UUID pid : ((RetirementTableModel) retireeTable.getModel()).getAltPayout().keySet()) {
//                    rdTracker.getPayout(pid).setPayoutAmount(((RetirementTableModel) retireeTable.getModel())
//                            .getAltPayout().get(pid));
//                }
                isSkipAll = false;
                setVisible(false);
            } else if (event.getSource().equals(btnSkip)) {
                isSkipAll = false;
                setVisible(false);
            } else if (event.getSource().equals(btnSkipAll)) {
                isSkipAll = true;
                setVisible(false);
            }
        }
    };

    private void filterPersonnel(TableRowSorter<AutoAwardsTableModel> sorter, JComboBox<PersonnelFilter> comboBox) {
        PersonnelFilter nGroup = (comboBox.getSelectedItem() != null)
                ? (PersonnelFilter) comboBox.getSelectedItem()
                : PersonnelFilter.ACTIVE;

        sorter.setRowFilter(new RowFilter<>() {

            @Override
            public boolean include(Entry<? extends AutoAwardsTableModel, ? extends Integer> entry) {
                Person person = entry.getModel().getPerson(entry.getIdentifier());

                return nGroup.getFilteredInformation(person, hqView.getCampaign().getLocalDate());
            }
        });
    }

    public boolean wasSkipAll() {
        return isSkipAll;
    }
}

class AutoAwardsTable extends JTable {
    public AutoAwardsTable(AutoAwardsTableModel model) {
        super(model);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        XTableColumnModel columnModel = new XTableColumnModel();
        setColumnModel(columnModel);
        createDefaultColumnsFromModel();
        TableColumn column;
        for (int columnIndex = 0; columnIndex < AutoAwardsTableModel.N_COL; columnIndex++) {
            column = getColumnModel().getColumn(convertColumnIndexToView(columnIndex));
            column.setPreferredWidth(model.getColumnWidth(columnIndex));
            if (columnIndex != AutoAwardsTableModel.COL_AWARD) {
                column.setCellRenderer(model.getRenderer(columnIndex));
            }
        }

        setRowHeight(80);
        setIntercellSpacing(new Dimension(0, 0));
        setShowGrid(false);

        getColumnModel().getColumn(convertColumnIndexToView(AutoAwardsTableModel.COL_AWARD))
                .setCellEditor(new DefaultCellEditor(new JCheckBox()));
    }
}
