import org.jdesktop.swingx.*;
import org.jdesktop.swingx.renderer.*;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Barak
 * Date: 27/09/13
 * Time: 01:55
 * To change this template use File | Settings | File Templates.
 */
public class WorkLogScr extends JXFrame
{
    public static Dimension DEFAULT_COMBO_SIZE = new Dimension(100, 25);
    public static Dimension DEFAULT_DATE_SIZE = new Dimension(100, 25);
    public static Dimension DEFAULT_WORKFILED_SIZE = new Dimension(250, 25);
    public static Dimension DEFAULT_BUTTON_SIZE = new Dimension(120, 25);
    public static Font DEFAULT_TEXT_FONT = new Font("Arial", Font.PLAIN, 14);
    public static Font DEFAULT_LABEL_FONT = new Font("Arial", Font.BOLD, 14);
    public static Font DEFAULT_TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static Color NEW_RECORD_COLOR = new Color(217,242,138);
    public static Color NEW_RECORD_SELECTED_COLOR = new Color(149,191,21);

    static
    {
        UIManager.put("ComboBox.background", new ColorUIResource(UIManager.getColor("TextField.background")));
        UIManager.put("ComboBox.font", new FontUIResource(DEFAULT_TEXT_FONT));
        UIManager.put("JXDatePicker.font", new FontUIResource(DEFAULT_TEXT_FONT));
        UIManager.put("Textfield.font", new FontUIResource(DEFAULT_TEXT_FONT));
        UIManager.put("Label.font", new FontUIResource(DEFAULT_LABEL_FONT));
        UIManager.put("Button.font", new FontUIResource(DEFAULT_LABEL_FONT));
        UIManager.put("OptionPane.okButtonText", "אישור");
        UIManager.put("OptionPane.cancelButtonText", "ביטול");
        Locale.setDefault(new Locale("he", "IL"));
    }

    private JXTable workTable = new JXTable(new WorkTableModel());
    private JXLabel currentDateLabel = new JXLabel();
    private JXLabel fromDateLabel = new JXLabel("מתאריך:");
    private JXDatePicker fromDatePicker = new JXDatePicker(Locale.getDefault());
    private JXLabel toDateLabel = new JXLabel("עד תאריך:");
    private JXButton filterDatesButton = new JXButton("סנן תאריכים");
    private JXLabel customerLabel = new JXLabel("לקוח:");
    private JXComboBox customerCombo = new JXComboBox();
    private JXButton resetButton = new JXButton("נקה חיפוש");
    private JXDatePicker toDatePicker = new JXDatePicker(Calendar.getInstance().getTime(), Locale.getDefault());
    private JXLabel workLabel = new JXLabel("עבודה שנעשתה:");
    private JXTextField workField = new JXTextField();
    private JXButton multipleAddButton = new JXButton("הוספה מרובה");
    private JXButton removeButton = new JXButton("מחק רשומה");
    private JXButton printButton = new JXButton("הדפס טבלה");

    public WorkLogScr() throws HeadlessException
    {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("tools.png")));
        this.setTitle("יומן עבודות נועם");
        Utils.setSoftSize(this, new Dimension(1200, 800));
        this.setLocationRelativeTo(null);

        try
        {
            initComponents();
        }
        catch (Exception e)
        {
            Utils.showExceptionMsg(this, e);
            System.exit(0);
        }

        JXPanel helloPanel = new JXPanel();
        Utils.setLineLayout(helloPanel);
        Utils.addStandardRigid(helloPanel);
        Image dadImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("dad.png"));
        dadImage = dadImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        helloPanel.add(new JXLabel(new ImageIcon(dadImage)));
        Utils.addStandardRigid(helloPanel);
        JXLabel helloLabel = new JXLabel("שלום נועם, התאריך היום");
        helloLabel.setFont(DEFAULT_TITLE_FONT);
        helloPanel.add(helloLabel);
        Utils.addSmallRigid(helloPanel);
        helloPanel.add(currentDateLabel);

        JXPanel filterPanel = new JXPanel();
        Utils.setLineLayout(filterPanel);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(fromDateLabel);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(fromDatePicker);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(toDateLabel);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(toDatePicker);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(filterDatesButton);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(customerLabel);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(customerCombo);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(workLabel);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(workField);
        Utils.addStandardRigid(filterPanel);
        filterPanel.add(resetButton);

        JXTitledPanel filterTitledPanel = new JXTitledPanel("סינון");
        filterTitledPanel.setTitleFont(DEFAULT_TITLE_FONT);
        Utils.setPageLayout(filterTitledPanel);
        Utils.addTinyRigid(filterTitledPanel);
        filterTitledPanel.add(filterPanel);
        Utils.addSmallRigid(filterTitledPanel);

        JScrollPane tableScrollPane = new JScrollPane(workTable);

        JXTitledPanel tableTitledPanel = new JXTitledPanel("רישומי עבודות");
        tableTitledPanel.setTitleFont(DEFAULT_TITLE_FONT);
        tableTitledPanel.add(tableScrollPane);

        JXPanel editButtonsPanel = new JXPanel();
        Utils.setLineLayout(editButtonsPanel);
        Utils.addStandardRigid(editButtonsPanel);
        editButtonsPanel.add(multipleAddButton);
        Utils.addStandardRigid(editButtonsPanel);
        editButtonsPanel.add(removeButton);
        editButtonsPanel.add(Box.createHorizontalGlue());

        Image printImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("print.png"));
        printImage = printImage.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        printButton.setIcon(new ImageIcon(printImage));

        JXPanel printButtonPanel = new JXPanel();
        Utils.setLineLayout(printButtonPanel);
        printButtonPanel.add(Box.createHorizontalGlue());
        Utils.addStandardRigid(printButtonPanel);
        printButtonPanel.add(printButton);
        Utils.addStandardRigid(printButtonPanel);

        JXPanel lineButtonsPanel = new JXPanel();
        lineButtonsPanel.setLayout(new BorderLayout());
        lineButtonsPanel.add(editButtonsPanel, BorderLayout.EAST);
        lineButtonsPanel.add(printButtonPanel, BorderLayout.WEST);

        JXPanel pageButtonPanel = new JXPanel();
        Utils.setPageLayout(pageButtonPanel);
        Utils.addStandardRigid(pageButtonPanel);
        pageButtonPanel.add(lineButtonsPanel);
        Utils.addStandardRigid(pageButtonPanel);

        JXPanel mainPanel = new JXPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(filterTitledPanel, BorderLayout.NORTH);
        mainPanel.add(tableTitledPanel, BorderLayout.CENTER);
        mainPanel.add(pageButtonPanel, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(helloPanel, BorderLayout.NORTH);
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        this.setVisible(true);
    }

    private void initComponents() throws Exception
    {
        Utils.setSoftSize(customerCombo, DEFAULT_COMBO_SIZE);
        Utils.setSoftSize(fromDatePicker, DEFAULT_DATE_SIZE);
        Utils.setSoftSize(toDatePicker, DEFAULT_DATE_SIZE);
        Utils.setSoftSize(workField, DEFAULT_WORKFILED_SIZE);
        Utils.setSoftSize(filterDatesButton, DEFAULT_BUTTON_SIZE);
        Utils.setSoftSize(multipleAddButton, DEFAULT_BUTTON_SIZE);
        Utils.setSoftSize(removeButton, DEFAULT_BUTTON_SIZE);
        Utils.setSoftSize(resetButton, DEFAULT_BUTTON_SIZE);
        Utils.setSoftSize(printButton, new Dimension(150, 25));

        Calendar monthBack = Calendar.getInstance();
        monthBack.add(Calendar.MONTH, -1);
        fromDatePicker.setDate(monthBack.getTime());

        workTable.getTableHeader().setFont(DEFAULT_LABEL_FONT);
        workTable.getColumn(WorkTableModel.JOBS_DESCR_COL).setMinWidth(350);
        workTable.getColumn(WorkTableModel.REMARKS_COL).setMinWidth(250);
        workTable.setDefaultRenderer(Object.class, new WorkTableRenderer());

        workField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                doFilter();
            }
        });

        filterDatesButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    ((WorkTableModel)workTable.getModel()).setPreservedJobList
                            (DBManager.getSingleton().getJobsByDates(fromDatePicker.getDate(), toDatePicker.getDate()));
                    doFilter();
                }
                catch (Exception e1)
                {
                    Utils.showExceptionMsg(WorkLogScr.this, e1);
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

        multipleAddButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JobRecordDialog jobRecordDialog = new JobRecordDialog(WorkLogScr.this);
                while (!jobRecordDialog.isFinished())
                {
                    Job job  = jobRecordDialog.getReturnedJob();
                    if (job == null)
                    {
                        return;
                    }

                    ((WorkTableModel)workTable.getModel()).getCurrentJobList().add(job);
                    ((WorkTableModel) workTable.getModel()).fireTableDataChanged();
                    jobRecordDialog = new JobRecordDialog(WorkLogScr.this);
                }
            }
        });

        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedRow = workTable.getSelectedRow();
                Job job = ((WorkTableModel)workTable.getModel()).getCurrentJobList().get(selectedRow);
                try
                {
                    DBManager.getSingleton().removeJob(job.getId());
                    ((WorkTableModel)workTable.getModel()).getCurrentJobList().remove(selectedRow);
                    ((WorkTableModel) workTable.getModel()).fireTableDataChanged();
                }
                catch (Exception e1)
                {
                    Utils.showExceptionMsg(WorkLogScr.this, e1);
                    e1.printStackTrace();
                }
            }
        });

        printButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    PrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
                    set.add(OrientationRequested.LANDSCAPE);
                    workTable.print(JTable.PrintMode.FIT_WIDTH, null, null, true, set, true);
                }
                catch (PrinterException e1)
                {
                    Utils.showExceptionMsg(WorkLogScr.this, e1);
                    e1.printStackTrace();
                }
            }
        });

        customerCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doFilter();
            }
        });

        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                workField.setText("");
                customerCombo.setSelectedIndex(0);
                doFilter();
            }
        });

        currentDateLabel.setText(DateFormat.getDateTimeInstance
                (DateFormat.SHORT, DateFormat.SHORT).format(Calendar.getInstance().getTime()));
        Timer helloTimer = new Timer(60000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                currentDateLabel.setText(DateFormat.getDateTimeInstance
                        (DateFormat.SHORT, DateFormat.SHORT).format(Calendar.getInstance().getTime()));

                currentDateLabel.repaint();
            }
        });
        helloTimer.start();

        initComponentsFromDB();
    }

    private void doFilter()
    {
        List<Job> preservedJobList = ((WorkTableModel)workTable.getModel()).getPreservedJobList();
        List<Job> filteredJobList = new ArrayList<Job>();
        for (Job job : preservedJobList)
        {
            if (job.getJobDescription().contains(workField.getText()))
            {
                if (customerCombo.getSelectedItem().equals(Customer.ALL_VALUES))
                {
                    filteredJobList.add(job);
                }
                else if (customerCombo.getSelectedItem().equals(job.getCustomer()))
                {
                    filteredJobList.add(job);
                }
            }
        }

        ((WorkTableModel)workTable.getModel()).setCurrentJobList(filteredJobList);
    }

    private void initComponentsFromDB() throws Exception
    {
        List<Customer> customers = DBManager.getSingleton().getCustomers();
        customers.add(0, Customer.ALL_VALUES);
        customerCombo.setModel(new DefaultComboBoxModel(customers.toArray()));
        ((WorkTableModel)workTable.getModel()).setPreservedJobList
                (DBManager.getSingleton().getJobsByDates(fromDatePicker.getDate(), toDatePicker.getDate()));
    }

    private class WorkTableModel extends AbstractTableModel
    {
        private static final int DATE_COL = 0;
        private static final int CUSTOMER_COL = 1;
        private static final int JOBS_DESCR_COL = 2;
        private static final int PRICE_COL = 3;
        private static final int REMARKS_COL = 4;

        private final String[] COLUMN_NAMES = new String[]{"תאריך", "לקוח", "תיאור עבודה שנעשתה", "מחיר", "הערות"};

        private List<Job> currentJobList = new ArrayList<Job>();
        private List<Job> preservedJobList = new ArrayList<Job>();

        private WorkTableModel()
        {
        }

        private List<Job> getPreservedJobList()
        {
            return preservedJobList;
        }

        private void setPreservedJobList(List<Job> preservedJobList)
        {
            this.preservedJobList = preservedJobList;
            this.currentJobList = new ArrayList<Job>(preservedJobList);
            this.fireTableDataChanged();
        }

        public void setCurrentJobList(List<Job> currentJobList)
        {
            this.currentJobList = currentJobList;
            this.fireTableDataChanged();
        }

        private List<Job> getCurrentJobList()
        {
            return currentJobList;
        }

        @Override
        public String getColumnName(int column)
        {
            return COLUMN_NAMES[column];
        }

        @Override
        public int getRowCount()
        {
            return currentJobList.size();
        }

        @Override
        public int getColumnCount()
        {
            return COLUMN_NAMES.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Job job = currentJobList.get(rowIndex);

            switch (columnIndex)
            {
                case DATE_COL: return job.getJobDate();
                case CUSTOMER_COL: return job.getCustomer();
                case JOBS_DESCR_COL: return job.getJobDescription();
                case PRICE_COL: return job.getPrice();
                case REMARKS_COL: return job.getRemarks();
                default: return null;
            }
        }
    }

    private class WorkTableRenderer extends DefaultTableRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (column == WorkTableModel.DATE_COL)
            {
                value = DateFormat.getDateInstance().format(value);
            }
            else if (column == WorkTableModel.PRICE_COL)
            {
                value = NumberFormat.getCurrencyInstance().format(value);
            }

            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (((WorkTableModel)workTable.getModel()).getCurrentJobList().get(row).isNewRecord())
            {
                component.setBackground(isSelected ? NEW_RECORD_SELECTED_COLOR : NEW_RECORD_COLOR);
            }

            return component;
        }
    }

    public static void main(String args[])
    {
        new WorkLogScr();
    }
}
